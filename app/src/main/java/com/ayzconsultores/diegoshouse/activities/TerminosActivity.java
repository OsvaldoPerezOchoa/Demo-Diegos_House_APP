package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ayzconsultores.diegoshouse.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TerminosActivity extends AppCompatActivity {

    WebView mWebView;
    private static final String API_URL = "https://app-cbt33jvn5q-uc.a.run.app/api/document/terminosycondiciones";
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terminos);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mWebView = findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());

        // Inicializar el diálogo de carga
        loadingDialog = new LoadingDialog(this);

        // Mostrar el diálogo de carga antes de iniciar la tarea de obtener la URL
        loadingDialog.showLoadingDialog();

        // Llamar a la API para obtener la URL real del PDF
        new GetPdfUrlTask().execute(API_URL);
    }

    private class GetPdfUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                return jsonObject.getString("url");

            } catch (Exception e) {
                Log.e("PDF_ERROR", "Error obteniendo URL del PDF", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String pdfUrl) {
            // Ocultar el diálogo de carga después de obtener la URL
            loadingDialog.ocultarDialog();

            if (pdfUrl != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                try {
                    startActivity(intent);
                    // Cerrar TerminosActivity después de abrir el PDF
                    finish();
                } catch (Exception e) {
                    Toast.makeText(TerminosActivity.this, "No hay aplicación para abrir PDF", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("PDF_ERROR", "No se pudo obtener la URL del PDF");
                Toast.makeText(TerminosActivity.this, "Error al obtener el documento", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
