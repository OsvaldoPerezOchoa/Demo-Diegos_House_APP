package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.adapters.ProductosAdapter;
import com.ayzconsultores.diegoshouse.models.ProductosModel;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class FiltroProductosActivity extends AppCompatActivity {


    LinearLayout btnPizza, btnMortaza, btnBebidas, btnPromociones;
    ProductosAdapter mProductosAdapter;
    ProductosProvider mProductosProvider;
    RecyclerView mRecyclerViewProductos;
    MaterialToolbar mToolbar;
    ImageView imageViewresultado;
    TextView resultado, txt_aviso, txt_pizaza, txt_mortaza, txt_bebidas, txt_promociones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filtro_productos);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mRecyclerViewProductos = findViewById(R.id.recycler_filtro);
        btnPizza = findViewById(R.id.btn_pizza);
        btnMortaza = findViewById(R.id.btn_mortaza);
        btnBebidas = findViewById(R.id.btn_bebidas);
        btnPromociones = findViewById(R.id.btn_promociones);
        resultado = findViewById(R.id.txt_resultado);
        imageViewresultado = findViewById(R.id.imageViewresultado);
        txt_aviso = findViewById(R.id.TextViewresultados);
        txt_pizaza = findViewById(R.id.txt_pizza);
        txt_mortaza = findViewById(R.id.txt_mortaza);
        txt_bebidas = findViewById(R.id.txt_bebidas);
        txt_promociones = findViewById(R.id.txt_promociones);


        // Inicializa mToolbar
        mToolbar = findViewById(R.id.topAppBarFiltro);

        // Configura el listener para el botón de navegación
        mToolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(FiltroProductosActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        GridLayoutManager managerProductos = new GridLayoutManager(this, 2);
        mRecyclerViewProductos.setLayoutManager(managerProductos);
        mProductosProvider = new ProductosProvider();


        btnPizza.setOnClickListener(v -> {
            loadProducts("Pizza");
            resultado.setText(getString(R.string.resultados_pizzas));
            setSelectedButton(btnPizza, txt_pizaza);
        });

        btnMortaza.setOnClickListener(v -> {
            loadProducts("Mortaza");
            resultado.setText(getString(R.string.resultados_mortaza));
            setSelectedButton(btnMortaza, txt_mortaza);
        });

        btnBebidas.setOnClickListener(v -> {
            loadProducts("Bebidas");
            resultado.setText(getString(R.string.resultados_bebidas));
            setSelectedButton(btnBebidas, txt_bebidas);
        });

        btnPromociones.setOnClickListener(v -> {
            loadProducts("Promociones");
            resultado.setText(getString(R.string.resultados_promociones));
            setSelectedButton(btnPromociones, txt_promociones);
        });


        String category = getIntent().getStringExtra("category");
        if (category != null) {
            switch (category) {
                case "Pizza":
                    setSelectedButton(btnPizza, txt_pizaza);
                    break;
                case "Mortaza":
                    setSelectedButton(btnMortaza, txt_mortaza);
                    break;
                case "Bebidas":
                    setSelectedButton(btnBebidas, txt_bebidas);
                    break;
                case "Promociones":
                    setSelectedButton(btnPromociones, txt_promociones);
                    break;
            }
            loadProducts(category);
        } else {
            loadProducts("default");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Obtén la categoría del Intent
        String category = getIntent().getStringExtra("category");

        // Carga productos con la categoría obtenida
        if (category != null) {
            loadProducts(category);
            resultado.setText(getString(R.string.resultados_generico, category));
        } else {
            loadProducts("default");
        }
    }

    private void loadProducts(String category) {
        Query productosQuery;

        // Usa getAll si la categoría es "default"; si no, usa getFiltro
        if ("default".equals(category)) {
            productosQuery = mProductosProvider.getAll();
        } else {
            productosQuery = mProductosProvider.getFiltro(category);
        }

        FirestoreRecyclerOptions<ProductosModel> productosOptions =
                new FirestoreRecyclerOptions.Builder<ProductosModel>()
                        .setQuery(productosQuery, ProductosModel.class)
                        .build();

        // Configuración del adaptador para los productos
        mProductosAdapter = new ProductosAdapter(productosOptions, this) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (getItemCount() > 0) {
                    mRecyclerViewProductos.setVisibility(View.VISIBLE);
                    imageViewresultado.setVisibility(View.GONE);
                    txt_aviso.setVisibility(View.GONE);
                } else {
                    mRecyclerViewProductos.setVisibility(View.GONE);
                    imageViewresultado.setVisibility(View.VISIBLE);
                    txt_aviso.setVisibility(View.VISIBLE);
                }
            }
        };
        mRecyclerViewProductos.setAdapter(mProductosAdapter);
        mProductosAdapter.startListening();
    }


    private void setSelectedButton(LinearLayout selectedButton, TextView selected) {
        // Restablece el fondo de todos los botones
        btnPizza.setBackgroundResource(R.drawable.border_without_color);
        btnMortaza.setBackgroundResource(R.drawable.border_without_color);
        btnBebidas.setBackgroundResource(R.drawable.border_without_color);
        btnPromociones.setBackgroundResource(R.drawable.border_without_color);
        txt_pizaza.setTextColor(ContextCompat.getColor(this, R.color.black));
        txt_mortaza.setTextColor(ContextCompat.getColor(this, R.color.black));
        txt_bebidas.setTextColor(ContextCompat.getColor(this, R.color.black));
        txt_promociones.setTextColor(ContextCompat.getColor(this, R.color.black));

        // Establece el fondo del botón seleccionado
        selectedButton.setBackgroundResource(R.drawable.border_layout);
        selected.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProductosAdapter.stopListening();
    }
}
