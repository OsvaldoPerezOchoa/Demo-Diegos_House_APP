package com.ayzconsultores.diegoshouse.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.adapters.CarritoAdapter;
import com.ayzconsultores.diegoshouse.models.CarritoModel;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.CarritoProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;

public class HistorialActivity extends AppCompatActivity {

    RecyclerView mRecyclerViewCarritos;
    CarritoAdapter mCarritosAdapter;
    CarritoProvider mCarritosProvider;
    ProgressBar mProgressBar;
    AuthProvider mAuthProvider;
    ConstraintLayout mcl_mensaje;
    LinearLayout mll_pedidos;
    MaterialToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mll_pedidos = findViewById(R.id.layout_pedidos);
        mcl_mensaje = findViewById(R.id.layout_mensaje);
        mToolbar = findViewById(R.id.topAppBarHistorial);
        mRecyclerViewCarritos = findViewById(R.id.recyclerview_carritos);
        mProgressBar = findViewById(R.id.progres_carritos);
        mAuthProvider = new AuthProvider();

        LinearLayoutManager managerBanner = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewCarritos.setLayoutManager(managerBanner);
        mCarritosProvider = new CarritoProvider();

        // Configura el listener para el botón de navegación
        mToolbar.setNavigationOnClickListener(v -> finish());
    }
    @Override
    public void onStart() {
        super.onStart();

        String id = mAuthProvider.getUid();

        // Configuración del adaptador para los productos
        Query carritosQuery = mCarritosProvider.CarritoFinalizado(id);
        FirestoreRecyclerOptions<CarritoModel> carritosOptions = new
                FirestoreRecyclerOptions.Builder<CarritoModel>()
                .setQuery(carritosQuery, CarritoModel.class)
                .build();
        mCarritosAdapter = new CarritoAdapter(carritosOptions, this) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                mProgressBar.setVisibility(View.GONE); // Oculta el ProgressBar cuando los datos están listos

                // Si no hay carritos, muestra el layout de mensaje
                if (getItemCount() == 0) {
                    mcl_mensaje.setVisibility(View.VISIBLE);
                    mll_pedidos.setVisibility(View.GONE);
                } else {
                    mcl_mensaje.setVisibility(View.GONE);
                    mll_pedidos.setVisibility(View.VISIBLE);
                }
            }
        };
        mRecyclerViewCarritos.setAdapter(mCarritosAdapter);
        mCarritosAdapter.startListening();
        mProgressBar.setVisibility(View.VISIBLE); // Muestra el ProgressBar al iniciar la carga
    }

    @Override
    public void onStop() {
        super.onStop();
        mCarritosAdapter.stopListening();
    }
}