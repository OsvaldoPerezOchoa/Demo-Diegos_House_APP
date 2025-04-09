package com.ayzconsultores.diegoshouse.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.activities.TimerWorker;
import com.ayzconsultores.diegoshouse.adapters.ProductoReciboAdapter;
import com.ayzconsultores.diegoshouse.models.ProductosCarritoModel;
import com.ayzconsultores.diegoshouse.providers.AgregarProductosProvider;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.CarritoProvider;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PedidosFragment extends Fragment {

    private static final String LOG_TAG = "PedidosFragment";

    private ImageView mImageQR, mImageticket;
    private RecyclerView mRecyclerProductos;
    private TextView txt_preciototal, txt_puntos, txt_idCarrito, txt_leyenda, txt_puntosUsados, txt_estado, txt_tiempoRestante;
    private ProgressBar progressBar;
    private CarritoProvider mCarritoProvider;
    private ScrollView scrollView;
    private ProductosProvider mProductosProvider;
    private AuthProvider mAuthProvider;
    private AgregarProductosProvider mAgregarProductosProvider;
    private ProductoReciboAdapter mProductoReciboAdapter;

    private RelativeLayout mTimerContainer;

    private Timestamp tiempoEspera; // Variable que almacena el tiempo de espera

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pedidos, container, false);

        // Inicializar elementos UI
        mImageQR = root.findViewById(R.id.image_codigoqr);
        mRecyclerProductos = root.findViewById(R.id.recyclerProductos);
        txt_preciototal = root.findViewById(R.id.txt_preciototal);
        txt_puntos = root.findViewById(R.id.txt_puntos);
        txt_puntosUsados = root.findViewById(R.id.txt_puntosUsados);
        txt_estado = root.findViewById(R.id.txt_estado);
        txt_idCarrito = root.findViewById(R.id.txt_idCarrito);
        txt_tiempoRestante = root.findViewById(R.id.txt_tiempoRestante); // TextView para mostrar el tiempo restante
        scrollView = root.findViewById(R.id.scrollView2);
        progressBar = root.findViewById(R.id.progressBar);
        txt_leyenda = root.findViewById(R.id.txt_pedidos);
        mImageticket = root.findViewById(R.id.img_ticket);
        mTimerContainer = root.findViewById(R.id.timer_container);
        mCarritoProvider = new CarritoProvider();
        mAuthProvider = new AuthProvider();
        mProductosProvider = new ProductosProvider();
        mAgregarProductosProvider = new AgregarProductosProvider();

        setupRecyclerView();
        loadCarritoData();

        mImageQR.setOnClickListener(v -> {
            // Obtener el Bitmap actual del ImageView
            Drawable drawable = mImageQR.getDrawable();
            Bitmap qrBitmap = null;  // Declarar la variable qrBitmap fuera del bloque

            if (drawable instanceof BitmapDrawable) {
                qrBitmap = ((BitmapDrawable) drawable).getBitmap();  // Asignar el Bitmap
            }

            // Verificar que qrBitmap no sea nulo antes de pasar a la siguiente acción
            if (qrBitmap != null) {
                // Mostrar la imagen ampliada en el Dialog
                showZoomedImage(qrBitmap);
            } else {
                Log.e(LOG_TAG, "Error: No se pudo obtener el Bitmap del ImageView");
            }
        });

        return root;
    }

    private void setupRecyclerView() {
        mRecyclerProductos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    private void loadCarritoData() {
        // Mostrar el ProgressBar mientras cargamos los datos
        progressBar.setVisibility(View.VISIBLE);

        String userId = mAuthProvider.getUid();

        mCarritoProvider.CarritoProduccionORecoleccion(userId).get()
                .addOnCompleteListener(task -> {
                    // Ocultar el ProgressBar cuando los datos estén cargados
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String carritoId = task.getResult().getDocuments().get(0).getId();
                        String carritoIdTexto = "Pedido: " + carritoId;
                        txt_idCarrito.setText(carritoIdTexto);

                        String estado = task.getResult().getDocuments().get(0).getString("estado");
                        txt_estado.setText("Estado del pedido: " + estado);

                        // Obtener el timestamp "tiempo_espera" de Firestore
                        tiempoEspera = task.getResult().getDocuments().get(0).getTimestamp("tiempo_espera");

                        // Si el estado es "producción", mostrar el temporizador
                        if ("produccion".equalsIgnoreCase(estado)) {
                            mTimerContainer.setVisibility(View.VISIBLE);
                            calcularDiferenciaTiempo(tiempoEspera, carritoId);
                        } else {
                            // Si el estado es "aceptado", ocultar el temporizador
                            mTimerContainer.setVisibility(View.GONE);
                        }

                        // Generar y mostrar el código QR con el carritoId
                        generateQRCode(carritoId);

                        setupProductosCarrito(carritoId);

                        Double total = task.getResult().getDocuments().get(0).getDouble("total");
                        if (total != null) {
                            txt_preciototal.setText(String.format(new Locale("es", "MX"), "Total: $%.2f", total));
                        }

                        Long puntos = task.getResult().getDocuments().get(0).getLong("puntos");
                        if (puntos != null) {
                            txt_puntos.setText(String.format(Locale.getDefault(), "Puntos: %d", puntos));
                        } else {
                            txt_puntos.setText(R.string.cero_puntos);
                        }

                        Long puntos_usados = task.getResult().getDocuments().get(0).getLong("puntos_usados");
                        if (puntos_usados != null) {
                            txt_puntosUsados.setText(String.format(Locale.getDefault(), "Puntos: %d", puntos_usados));
                        } else {
                            txt_puntosUsados.setText(R.string.cero_puntos);
                        }

                        hideProductosView();
                    } else {
                        showProductosView();
                    }
                });
    }

    // Método para calcular la diferencia entre el tiempo actual y el tiempo_espera
    private void calcularDiferenciaTiempo(Timestamp tiempoEspera, String carritoId) {
        if (tiempoEspera != null) {
            long tiempoActual = System.currentTimeMillis();
            long tiempoEsperaMillis = tiempoEspera.getSeconds() * 1000;

            long diferencia = tiempoEsperaMillis - tiempoActual;

            if (diferencia > 0) {
                // Iniciar TimerWorker (si es necesario)
                TimerWorker.startTimer(getContext(), diferencia, carritoId);

                // Iniciar el handler para actualizar la UI en tiempo real
                startTimerUpdate(diferencia, carritoId);
            } else {
                txt_tiempoRestante.setText("Tiempo agotado");
                notificarCambioEstadoPedido(carritoId);
            }
        }
    }

    private void startTimerUpdate(long diferencia, String carritoId) {
        runnable = new Runnable() {
            long tiempoRestante = diferencia;

            @Override
            public void run() {
                if (tiempoRestante > 0) {
                    // Verificar el estado del carrito en cada actualización del temporizador
                    verificarEstadoCarrito(carritoId);

                    // Actualizar el tiempo restante
                    updateTimeRemaining(tiempoRestante);
                    tiempoRestante -= 1000; // Reducir 1 segundo
                    handler.postDelayed(this, 1000);
                } else {
                    txt_tiempoRestante.setText("Tiempo agotado");
                    notificarCambioEstadoPedido(carritoId);
                }
            }
        };
        handler.post(runnable);
    }

    private void verificarEstadoCarrito(String carritoId) {
        mCarritoProvider.CarritoProduccionORecoleccion(mAuthProvider.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String estado = task.getResult().getDocuments().get(0).getString("estado");

                        if (!"produccion".equalsIgnoreCase(estado)) {
                            // Detener el temporizador si el estado ha cambiado
                            handler.removeCallbacks(runnable);
                            mTimerContainer.setVisibility(View.GONE); // Ocultar el temporizador
                            txt_estado.setText("Estado actualizado: " + estado);
                        }
                    }
                });
    }


    private void updateTimeRemaining(long diferencia) {
        int seconds = (int) (diferencia / 1000) % 60;
        int minutes = (int) ((diferencia / (1000 * 60)) % 60);
        int hours = (int) ((diferencia / (1000 * 60 * 60)) % 24);

        String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        txt_tiempoRestante.setText(time);
    }

    // Método para notificar el cambio de estado del pedido
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

                    // Cuando el tiempo se agote, actualizar el fragmento
                    getActivity().runOnUiThread(() -> {
                        // Reemplazar el fragmento actual con una nueva instancia de PedidosFragment
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.pedidos_fragment, new PedidosFragment()); // Cambia R.id.fragment_container por el contenedor donde se carga tu fragmento
                        transaction.commit();
                    });

                } else {
                    // Si la respuesta no fue exitosa
                    Log.e(LOG_TAG, "Error al actualizar el estado del pedido: " + response.message());
                }
            }
        });
    }

    private void generateQRCode(String carritoId) {
        new Thread(() -> {
            try {
                QRCodeWriter writer = new QRCodeWriter();
                Bitmap bitmap = createQRCodeBitmap(writer, carritoId);

                if (bitmap != null) {
                    // Verificar si el fragmento sigue asociado a la actividad
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> mImageQR.setImageBitmap(bitmap));
                    }
                }
            } catch (WriterException e) {
                Log.e(LOG_TAG, "Error al generar el código QR", e);
            }
        }).start();
    }

    private Bitmap createQRCodeBitmap(QRCodeWriter writer, String content) throws WriterException {
        int width = 512;
        int height = 512;
        com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return bitmap;
    }

    private void showZoomedImage(Bitmap bitmap) {
        // Crear un Dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_image_zoom);

        // Vincular el ImageView del Dialog
        ImageView zoomedImageView = dialog.findViewById(R.id.zoomedImageView);

        // Establecer el Bitmap en el ImageView
        zoomedImageView.setImageBitmap(bitmap);

        // Cerrar el Dialog al hacer clic en cualquier parte
        zoomedImageView.setOnClickListener(v -> dialog.dismiss());

        // Mostrar el Dialog
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }


    private void setupProductosCarrito(String carritoId) {
        Query productosCarritoQuery = mAgregarProductosProvider.AllProductosCarrito(carritoId);
        FirestoreRecyclerOptions<ProductosCarritoModel> options = new FirestoreRecyclerOptions.Builder<ProductosCarritoModel>()
                .setQuery(productosCarritoQuery, ProductosCarritoModel.class)
                .build();

        mProductoReciboAdapter = new ProductoReciboAdapter(options, getContext());
        mRecyclerProductos.setAdapter(mProductoReciboAdapter);
        mProductoReciboAdapter.startListening();
    }

    private void showProductosView() {
        txt_leyenda.setVisibility(View.VISIBLE);
        mImageticket.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    private void hideProductosView() {
        txt_leyenda.setVisibility(View.GONE);
        mImageticket.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProductoReciboAdapter != null) {
            mProductoReciboAdapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCarritoData();
    }

}
