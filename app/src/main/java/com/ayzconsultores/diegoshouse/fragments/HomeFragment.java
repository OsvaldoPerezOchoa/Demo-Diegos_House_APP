package com.ayzconsultores.diegoshouse.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.activities.FiltroProductosActivity;
import com.ayzconsultores.diegoshouse.adapters.BannerAdapter;
import com.ayzconsultores.diegoshouse.adapters.ProductosAdapter;
import com.ayzconsultores.diegoshouse.models.ProductosModel;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment {

    // Elementos de la interfaz de usuario (UI)
    LinearLayout btnPizza, btnMortaza, btnBebidas, btnPromociones;
    RecyclerView mRecyclerViewBanenr, mRecyclerViewProductos;
    ProgressBar progressBar;
    LinearLayout layoutContenido;

    // Proveedores y adaptadores
    BannerAdapter mBannerAdapter;
    ProductosAdapter mProductosAdapter;
    ProductosProvider mProductosProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar los elementos de la interfaz de usuario
        initializeUIElements(root);

        // Configurar los botones de categorías con su acción correspondiente
        setupCategoryButtons();

        // Configurar RecyclerView para los banners
        setupRecyclerViewBanner();

        // Configurar RecyclerView para los productos
        setupRecyclerViewProductos();

        // Inicializar el proveedor de productos
        mProductosProvider = new ProductosProvider();

        return root;
    }

    // Inicializa los elementos de la interfaz de usuario
    private void initializeUIElements(View root) {
        mRecyclerViewProductos = root.findViewById(R.id.recycler_recomendacion);
        mRecyclerViewBanenr = root.findViewById(R.id.recycler_banner);
        btnPizza = root.findViewById(R.id.btn_pizza);
        btnMortaza = root.findViewById(R.id.btn_mortaza);
        btnBebidas = root.findViewById(R.id.btn_bebidas);
        btnPromociones = root.findViewById(R.id.btn_promociones);
        progressBar = root.findViewById(R.id.progressBar);
        layoutContenido = root.findViewById(R.id.layout_contenido);
    }

    // Configura los botones de las categorías para que abran la actividad FiltroProductosActivity
    private void setupCategoryButtons() {
        btnPizza.setOnClickListener(v -> openFiltroProductos("Pizza"));
        btnMortaza.setOnClickListener(v -> openFiltroProductos("Mortaza"));
        btnBebidas.setOnClickListener(v -> openFiltroProductos("Bebidas"));
        btnPromociones.setOnClickListener(v -> openFiltroProductos("Promociones"));
    }

    // Abre la actividad FiltroProductosActivity con la categoría seleccionada
    private void openFiltroProductos(String category) {
        Intent intent = new Intent(getActivity(), FiltroProductosActivity.class);
        intent.putExtra("category", category); // Pasar la categoría como extra
        startActivity(intent);
    }

    // Configura el RecyclerView para los banners (en orientación horizontal)
    private void setupRecyclerViewBanner() {
        LinearLayoutManager managerBanner = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewBanenr.setLayoutManager(managerBanner);
    }

    // Configura el RecyclerView para los productos (en un Grid con 2 columnas)
    private void setupRecyclerViewProductos() {
        GridLayoutManager managerProductos = new GridLayoutManager(getContext(), 2);
        mRecyclerViewProductos.setLayoutManager(managerProductos);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Mostrar el ProgressBar mientras se cargan los datos
        progressBar.setVisibility(View.VISIBLE);
        layoutContenido.setVisibility(View.GONE); // Ocultar el contenido mientras se cargan los datos

        // Configurar el BannerAdapter para cargar los banners promocionales
        setupBannerAdapter();

        // Configurar el ProductosAdapter para cargar los productos
        setupProductosAdapter();
    }

    // Configura el BannerAdapter con los datos de Firestore
    private void setupBannerAdapter() {
        Query bannerQuery = mProductosProvider.getPromo();
        FirestoreRecyclerOptions<ProductosModel> bannerOptions =
                new FirestoreRecyclerOptions.Builder<ProductosModel>()
                        .setQuery(bannerQuery, ProductosModel.class)
                        .build();

        mBannerAdapter = new BannerAdapter(bannerOptions, getContext()) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                // Ocultar el ProgressBar y mostrar el contenido cuando los datos se cargan
                if (getItemCount() > 0) {
                    progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar
                    layoutContenido.setVisibility(View.VISIBLE); // Mostrar el contenido
                }
            }
        };
        mRecyclerViewBanenr.setAdapter(mBannerAdapter);
        mBannerAdapter.startListening(); // Iniciar la escucha de los datos en Firestore
    }

    // Configura el ProductosAdapter con los datos de Firestore
    private void setupProductosAdapter() {
        // Obtiene la consulta de todos los productos activos
        Query productosQuery = mProductosProvider.getAll();

        // Configura las opciones para FirestoreRecyclerAdapter
        FirestoreRecyclerOptions<ProductosModel> productosOptions =
                new FirestoreRecyclerOptions.Builder<ProductosModel>()
                        .setQuery(productosQuery, ProductosModel.class)
                        .build();

        // Crea un nuevo adaptador con las opciones
        mProductosAdapter = new ProductosAdapter(productosOptions, getContext()) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                // Ocultar el ProgressBar y mostrar el contenido cuando los datos se cargan
                if (getItemCount() > 0) {
                    progressBar.setVisibility(View.GONE);  // Ocultar el ProgressBar
                    layoutContenido.setVisibility(View.VISIBLE);  // Mostrar el contenido
                }
            }
        };

        // Asocia el adaptador al RecyclerView
        mRecyclerViewProductos.setAdapter(mProductosAdapter);

        // Inicia la escucha de los datos en Firestore
        mProductosAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        // Detener la escucha de los adaptadores cuando el fragmento se detiene
        mBannerAdapter.stopListening();
        mProductosAdapter.stopListening();
    }

}
