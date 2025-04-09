package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.CarritoProvider;
import com.ayzconsultores.diegoshouse.providers.TokenProvider;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;

public class PagoActivity extends AppCompatActivity {

    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    CarritoProvider mCarritoProvider;
    TokenProvider mTokenProvider;
    String mExtraCarrito, mExtraid_Usuario;
    Double mExtraTotal;
    int mExtraPuntosUsados, mExtraPuntosGanados;
    Timestamp mExtraFechaCompra;
    WebView mWebView;
    MaterialToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mCarritoProvider = new CarritoProvider();
        mTokenProvider = new TokenProvider();

        mExtraCarrito = getIntent().getStringExtra("id_carrito");
        mExtraPuntosUsados = getIntent().getIntExtra("puntos_usados", 0);
        mExtraPuntosGanados = getIntent().getIntExtra("puntos_ganados", 0);
        mExtraFechaCompra = getIntent().getParcelableExtra("fecha_compra");
        mExtraid_Usuario = getIntent().getStringExtra("id_usuario");
        mExtraTotal = getIntent().getDoubleExtra("total", 0.0);
        mToolbar = findViewById(R.id.topAppBar);

        Log.d("PagoActivity", "id_usuario: " + mExtraid_Usuario);

        String url = "https://diego-s-house.web.app/pago/" + mExtraid_Usuario + "?nocache=" + System.currentTimeMillis();
        loadWebView(url);

        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadWebView(String url) {
        mWebView = findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.clearCache(true);
        mWebView.clearHistory();

        mWebView.addJavascriptInterface(new WebAppInterface(), "Android");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("WebViewError", "Error loading page: " + error.getDescription());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("WebView", "Page loaded: " + url);
            }
        });

        mWebView.loadUrl(url);
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void onPaymentSuccess() {
            runOnUiThread(() -> {
                Toast.makeText(PagoActivity.this, "Pago exitoso, regresando...", Toast.LENGTH_SHORT).show();
                processPostPayment();
                regresarAHome();
            });
        }
    }

    private void processPostPayment() {
        mCarritoProvider.updateCarritoTotal(mExtraCarrito, mExtraTotal)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PagoActivity", "Total del carrito actualizado.");
                    mCarritoProvider.updateCarritoPuntosYFecha(mExtraCarrito, mExtraPuntosGanados, mExtraFechaCompra, mExtraPuntosUsados)
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("PagoActivity", "Puntos y fecha de compra actualizados.");
                                mUsersProvider.actualizarPuntos(mExtraid_Usuario, mExtraPuntosUsados, mExtraPuntosGanados)
                                        .addOnSuccessListener(aVoid2 -> Log.d("PagoActivity", "Puntos del usuario actualizados."))
                                        .addOnFailureListener(e -> Log.e("PagoActivity", "Error al actualizar puntos del usuario: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> Log.e("PagoActivity", "Error al actualizar datos del carrito: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("PagoActivity", "Error al actualizar total del carrito: " + e.getMessage()));
    }

    private void regresarAHome() {
        Intent intent = new Intent(PagoActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
