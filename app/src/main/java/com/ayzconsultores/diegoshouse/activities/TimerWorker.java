package com.ayzconsultores.diegoshouse.activities;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TimerWorker extends Worker {

    private static final String LOG_TAG = "TimerWorker";

    public static final String CARRO_ID_KEY = "carrito_id";
    public static final String TIEMPO_RESTANTE_KEY = "tiempo_restante";

    public TimerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long diferencia = getInputData().getLong(TIEMPO_RESTANTE_KEY, 0);
        String carritoId = getInputData().getString(CARRO_ID_KEY);

        // Realiza la tarea de cuenta regresiva (en segundo plano)
        if (diferencia > 0) {
            try {
                Thread.sleep(diferencia); // Espera el tiempo restante
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Result.failure();
            }
        }

        // Verificamos el estado del carrito antes de proceder con la actualización
        if (carritoId != null) {
            verificarEstadoCarrito(carritoId);
        }

        return Result.success();
    }

    private void verificarEstadoCarrito(String carritoId) {
        // Crear un cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Crear la solicitud GET para verificar el estado del carrito
        Request request = new Request.Builder()
                .url("https://app-cbt33jvn5q-uc.a.run.app/api/pedidos/estado/" + carritoId) // URL del endpoint
                .get()
                .build();

        // Realizar la solicitud de manera asíncrona
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Si hay un error, se maneja aquí
                Log.e(LOG_TAG, "Error al verificar el estado del carrito: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Si la solicitud fue exitosa
                    String responseBody = response.body().string();
                    Log.d(LOG_TAG, "Estado del carrito: " + responseBody);

                    // Aquí parseamos el JSON para obtener el estado exacto
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String estado = jsonResponse.optString("estado", "");

                        // Realizamos la acción solo si el estado es 'produccion'
                        if (estado.equals("produccion")) {
                            notificarCambioEstadoPedido(carritoId);
                        } else {
                            Log.d(LOG_TAG, "El carrito no está en estado 'producción', no se realiza ninguna acción.");
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error al parsear el estado del carrito: " + e.getMessage());
                    }
                } else {
                    // Si la respuesta no fue exitosa
                    Log.e(LOG_TAG, "Error al verificar el estado del carrito: " + response.message());
                }
            }
        });
    }

    private void notificarCambioEstadoPedido(String carritoId) {
        // Crear un cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Crear el cuerpo de la solicitud (JSON)
        String json = "{ \"estado\": \"recoleccion\" }";

        // Crear la solicitud
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://app-cbt33jvn5q-uc.a.run.app/api/pedidos/notificar/" + carritoId) // Reemplaza con tu URL de la API
                .post(body)
                .build();

        // Realizar la solicitud de manera asíncrona
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Si hay un error, se maneja aquí
                Log.e(LOG_TAG, "Error al notificar el cambio de estado: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Si la solicitud fue exitosa
                    Log.d(LOG_TAG, "Estado de pedido actualizado a 'recoleccion'");
                } else {
                    // Si la respuesta no fue exitosa
                    Log.e(LOG_TAG, "Error al actualizar el estado del pedido: " + response.message());
                }
            }
        });
    }

    public static void startTimer(Context context, long tiempoRestante, String carritoId) {
        Data inputData = new Data.Builder()
                .putLong(TIEMPO_RESTANTE_KEY, tiempoRestante)
                .putString(CARRO_ID_KEY, carritoId)
                .build();

        // Configurar el Worker
        OneTimeWorkRequest timerWorkRequest = new OneTimeWorkRequest.Builder(TimerWorker.class)
                .setInputData(inputData)
                .build();

        // Encolar el Worker
        WorkManager.getInstance(context).enqueue(timerWorkRequest);
    }
}
