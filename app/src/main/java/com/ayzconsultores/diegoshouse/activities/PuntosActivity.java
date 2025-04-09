package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.google.android.material.appbar.MaterialToolbar;

public class PuntosActivity extends AppCompatActivity {

    TextView txtPuntos;
    Button btn_category;
    AuthProvider mAuth;
    UsersProvider user;
    MaterialToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_puntos);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtPuntos = findViewById(R.id.txt_Points);
        btn_category = findViewById(R.id.btn_canjear);
        mAuth = new AuthProvider();
        user = new UsersProvider();
        obtenerPuntos();
        mToolbar = findViewById(R.id.topAppBar);
        mToolbar.setNavigationOnClickListener(v -> finish());

        btn_category.setOnClickListener(view -> {
            openFiltroProductos();
        });
    }

    private void openFiltroProductos() {
        Intent intent = new Intent(this, FiltroProductosActivity.class);
        intent.putExtra("category", "Promociones"); // Pasar la categoría como extra
        startActivity(intent);
    }

    private void obtenerPuntos() {
        String userId = mAuth.getCurrentUser().getUid();
        user.getPuntosPorId(userId).addOnSuccessListener(puntos -> {
            // Mostrar los puntos en el TextView
            txtPuntos.setText(String.valueOf(puntos));
        }).addOnFailureListener(e -> {
            // Manejo de errores
            Toast.makeText(PuntosActivity.this, "Error al obtener los puntos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

}