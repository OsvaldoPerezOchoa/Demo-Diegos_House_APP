package com.ayzconsultores.diegoshouse.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.activities.PagoActivity;
import com.ayzconsultores.diegoshouse.adapters.ProductosCarritoAdapter;
import com.ayzconsultores.diegoshouse.models.ProductosCarritoModel;
import com.ayzconsultores.diegoshouse.providers.*;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;


public class CarritoFragment extends Fragment {

    private static final String LOG_TAG = "CarritoFragment";

    // Elementos UI
    private RecyclerView mRecyclerCarrito;
    private TextView txt_total, txt_puntosganados, txt_puntosusados, txt_vacioCarrito;
    private ImageView img_carrito;
    private Button btn_pago;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    // Providers para manejar datos
    private AuthProvider mAuthProvider;
    private UsersProvider mUsersProvider;
    private AgregarProductosProvider mAgregarProductosProvider;
    private CarritoProvider mCarritoProvider;
    private ProductosProvider mProductosProvider;

    // Adapter para la lista de productos en el carrito
    private ProductosCarritoAdapter mProductosCarritoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_carrito, container, false);

        // Inicialización de elementos UI y datos
        initializeUIElements(root);
        setupRecyclerView();
        initializeProviders();

        // Configurar acción del botón de pago
        btn_pago.setOnClickListener(v -> handlePago());

        return root;
    }

    // Inicializa los elementos de UI
    private void initializeUIElements(View root) {
        mRecyclerCarrito = root.findViewById(R.id.recyclerProductos);
        txt_total = root.findViewById(R.id.txt_preciototal);
        txt_puntosganados = root.findViewById(R.id.txt_puntosganados);
        txt_puntosusados = root.findViewById(R.id.txt_puntosusados);
        btn_pago = root.findViewById(R.id.btn_pago);
        txt_vacioCarrito = root.findViewById(R.id.txt_carritoVacio);
        img_carrito = root.findViewById(R.id.img_carrito);
        progressBar = root.findViewById(R.id.progressBar);
        scrollView = root.findViewById(R.id.scrollView2);
    }

    // Configura el RecyclerView con un LinearLayoutManager
    private void setupRecyclerView() {
        mRecyclerCarrito.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    // Inicializa los proveedores de datos
    private void initializeProviders() {
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mAgregarProductosProvider = new AgregarProductosProvider();
        mCarritoProvider = new CarritoProvider();
        mProductosProvider = new ProductosProvider();
    }

    // Maneja el evento de pago
    private void handlePago() {
        String userId = mAuthProvider.getUid();

        mUsersProvider.getPuntosPorId(userId).addOnSuccessListener(userPoints -> {
            int puntosAUsar = parsePuntosUsados(txt_puntosusados.getText().toString());

            if (puntosAUsar > userPoints) {
                showMessage("No tienes suficientes puntos para realizar esta compra");
                return;
            }
            mCarritoProvider.CarritoProduccionORecoleccion(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            showMessage("Ya hay un pedido en producción o recolección");
                        } else {
                            processCarrito(userId);
                        }
                    });
        });
    }


    // Procesa el carrito actual
    private void processCarrito(String userId) {
        double totalValue = parseTotal(txt_total.getText().toString());
        int puntos = parsePuntosGanados(txt_puntosganados.getText().toString());
        int puntosUsados = parsePuntosUsados(txt_puntosusados.getText().toString());
        Timestamp fechaCompra = Timestamp.now();

        mCarritoProvider.CarritoActivo(userId).get()
                .addOnCompleteListener(carritoTask -> {
                    if (carritoTask.isSuccessful() && carritoTask.getResult() != null && !carritoTask.getResult().isEmpty()) {
                        String carritoId = carritoTask.getResult().getDocuments().get(0).getId();
                        updateCarritoPuntosYFecha(carritoId, puntos, fechaCompra,userId, totalValue);
                    } else {
                        Log.e(LOG_TAG, "No se encontró ningún carrito activo para actualizar.");
                    }
                });
    }


    // Actualiza los puntos y usuario asociado al carrito
    private void updateCarritoPuntosYFecha(String carritoId, int puntosGanados, Timestamp fechaCompra, String userId, double totalValue) {
        // Obtener puntos usados del TextView
        int puntosUsados = parsePuntosUsados(txt_puntosusados.getText().toString());

        // Actualizar los puntos del usuario usando getPuntosPorId
        mUsersProvider.getPuntosPorId(userId).addOnSuccessListener(currentPoints -> {
            mCarritoProvider.preciototalCarrito(carritoId,totalValue);
            Intent intent = new Intent(this.getContext(), PagoActivity.class);  // Crea la intención para abrir los detalles del producto
            intent.putExtra("id_carrito", carritoId);
            intent.putExtra("puntos_usados", puntosUsados);
            intent.putExtra("puntos_ganados", puntosGanados);
            intent.putExtra("fecha_compra", fechaCompra);
            intent.putExtra("id_usuario", userId);
            intent.putExtra("total", totalValue);
            startActivity(intent);
        });
    }

    // Métodos auxiliares para parsear datos
    private double parseTotal(String totalText) {
        return Double.parseDouble(totalText.replace("$", "").replace(",", ""));
    }

    private int parsePuntosGanados(String puntosText) {
        try {
            return Integer.parseInt(puntosText.replace("Puntos a ganar:", "").trim());
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Error parseando puntos ganados: " + puntosText, e);
            return 0;
        }
    }

    private int parsePuntosUsados(String puntosText) {
        try {
            return Integer.parseInt(puntosText.replace("Puntos a usar:", "").trim());
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Error parseando puntos usados: " + puntosText, e);
            return 0;
        }
    }

    // Muestra un mensaje con Toast
    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        loadCarrito();
    }

    // Carga el carrito activo
    private void loadCarrito() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuthProvider.getUid();
        mCarritoProvider.CarritoActivo(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String carritoId = task.getResult().getDocuments().get(0).getId();
                        setupProductosCarrito(carritoId);
                    } else {
                        showEmptyCarritoView();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    showEmptyCarritoView();
                    progressBar.setVisibility(View.GONE);
                });
    }

    // Configura el adaptador para los productos del carrito
    private void setupProductosCarrito(String carritoId) {
        Query productosCarritoQuery = mAgregarProductosProvider.AllProductosCarrito(carritoId);
        FirestoreRecyclerOptions<ProductosCarritoModel> options = new FirestoreRecyclerOptions.Builder<ProductosCarritoModel>()
                .setQuery(productosCarritoQuery, ProductosCarritoModel.class)
                .build();

        mProductosCarritoAdapter = new ProductosCarritoAdapter(options, getContext());
        mRecyclerCarrito.setAdapter(mProductosCarritoAdapter);
        mProductosCarritoAdapter.startListening();

        productosCarritoQuery.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e(LOG_TAG, "Error al escuchar cambios en el carrito: ", error);
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (snapshots != null && !snapshots.isEmpty()) {
                showCarritoView();
                calculateTotal(snapshots);
            } else {
                showEmptyCarritoView();
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    // Calcula el total y los puntos del carrito
    private void calculateTotal(@NonNull QuerySnapshot snapshots) {
        AtomicReference<Double> total = new AtomicReference<>(0.0);
        AtomicReference<Integer> puntosGanados = new AtomicReference<>(0);
        AtomicReference<Integer> puntosUsados = new AtomicReference<>(0);
        List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            ProductosCarritoModel productoCarrito = doc.toObject(ProductosCarritoModel.class);
            if (productoCarrito != null) {
                String idProducto = productoCarrito.getId_producto();
                int cantidad = productoCarrito.getCantidad();

                com.google.android.gms.tasks.Task<DocumentSnapshot> task = mProductosProvider.getProductoId(idProducto)
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Double precio = documentSnapshot.getDouble("precio");
                                Integer puntosCosto = documentSnapshot.getLong("puntos") != null ?
                                        documentSnapshot.getLong("puntos").intValue() : 0;
                                String categoria = documentSnapshot.getString("categoria");

                                if ("pizza".equalsIgnoreCase(categoria)) {
                                    puntosGanados.updateAndGet(v -> v + cantidad);
                                }

                                if (puntosCosto > 0) {
                                    // Si el producto cuesta puntos
                                    puntosUsados.updateAndGet(v -> v + (puntosCosto * cantidad));
                                } else if (precio != null) {
                                    // Si el producto cuesta dinero
                                    total.updateAndGet(v -> v + precio * cantidad);
                                }
                            }
                        }).addOnFailureListener(e -> Log.e(LOG_TAG, "Error al obtener datos del producto", e));

                tasks.add(task);
            }
        }

        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(task -> {
                    txt_total.setText(String.format(new Locale("es", "MX"), "$%.2f", total.get()));
                    txt_puntosganados.setText(String.format(Locale.getDefault(), "Puntos a ganar: %d", puntosGanados.get()));
                    txt_puntosusados.setText(String.format(Locale.getDefault(), "Puntos a usar: %d", puntosUsados.get()));
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    // Muestra la vista del carrito lleno
    private void showCarritoView() {
        txt_vacioCarrito.setVisibility(View.GONE);
        img_carrito.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    // Muestra la vista del carrito vacío
    private void showEmptyCarritoView() {
        txt_vacioCarrito.setVisibility(View.VISIBLE);
        img_carrito.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProductosCarritoAdapter != null) {
            mProductosCarritoAdapter.stopListening();
        }
    }
}
