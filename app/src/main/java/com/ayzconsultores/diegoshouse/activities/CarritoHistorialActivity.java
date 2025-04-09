package com.ayzconsultores.diegoshouse.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.adapters.ProductoReciboAdapter;
import com.ayzconsultores.diegoshouse.models.ProductosCarritoModel;
import com.ayzconsultores.diegoshouse.providers.AgregarProductosProvider;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.CarritoProvider;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Locale;

public class CarritoHistorialActivity extends AppCompatActivity {

    private static final String LOG_TAG = "PedidosFragment";

    String mExtraProductoId;
    ImageView mImageQR, mImageticket;
    RecyclerView mRecyclerProductos;
    TextView txt_preciototal, txt_puntos, txt_idCarrito, txt_leyenda, txt_puntosusados, txt_estado;
    ProgressBar progressBar; // Declarar el ProgressBar
    CarritoProvider mCarritoProvider;
    NestedScrollView scrollView;
    ProductosProvider mProductosProvider;
    AuthProvider mAuthProvider;
    AgregarProductosProvider mAgregarProductosProvider;
    ProductoReciboAdapter mProductoReciboAdapter;
    MaterialToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrito_historial);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar elementos UI
        mExtraProductoId = getIntent().getStringExtra("id");
        mImageQR = findViewById(R.id.image_codigoqr);
        mRecyclerProductos = findViewById(R.id.recyclerProductos);
        txt_preciototal = findViewById(R.id.txt_preciototal);
        txt_puntos = findViewById(R.id.txt_puntos);
        txt_idCarrito = findViewById(R.id.txt_idCarrito);
        txt_puntosusados = findViewById(R.id.txt_puntosusados);
        txt_estado = findViewById(R.id.txt_estado);
        scrollView = findViewById(R.id.scrollView2);
        progressBar = findViewById(R.id.progressBar);
        txt_leyenda = findViewById(R.id.txt_pedidos);
        mImageticket = findViewById(R.id.img_ticket);
        mToolbar = findViewById(R.id.topAppBarHistorial);
        mCarritoProvider = new CarritoProvider();
        mAuthProvider = new AuthProvider();
        mProductosProvider = new ProductosProvider();
        mAgregarProductosProvider = new AgregarProductosProvider();

        setupRecyclerView();
        loadCarritoData();

        // Configura el listener para el botón de navegación
        mToolbar.setNavigationOnClickListener(v -> finish());

    }
    private void setupRecyclerView() {
        mRecyclerProductos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void loadCarritoData() {
        // Mostrar el ProgressBar mientras cargamos los datos
        progressBar.setVisibility(View.VISIBLE);

        mCarritoProvider.getCarritoId(mExtraProductoId)
                .addOnCompleteListener(task -> {
                    // Ocultar el ProgressBar cuando los datos estén cargados
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Establecer el ID del carrito en txt_idCarrito
                        String carritoIdTexto = "Pedido: " + mExtraProductoId;
                        txt_idCarrito.setText(carritoIdTexto);

                        // Establecer el estado del carrito en txt_estado
                        String estado = "Estado del pedido0" + task.getResult().getString("estado");
                        txt_estado.setText(estado);

                        // Generar y mostrar el código QR con el carritoId
                        generateQRCode(mExtraProductoId);

                        // Configurar el RecyclerView para mostrar los productos
                        setupProductosCarrito(mExtraProductoId);

                        // Obtener y mostrar el total
                        Double total = task.getResult().getDouble("total");

                        Double puntos = task.getResult().getDouble("puntos");

                        Double puntosUsados = task.getResult().getDouble("puntos_usados");

                        if (puntos != null) {
                            txt_puntos.setText(String.format(Locale.getDefault(), "Puntos: %d", puntos.longValue()));
                        } else {
                            txt_puntos.setText(R.string.cero_puntos);
                        }

                        if (puntosUsados != null) {
                            txt_puntosusados.setText(String.format(Locale.getDefault(), "Puntos: %d", puntosUsados.longValue()));
                        } else {
                            txt_puntosusados.setText(R.string.cero_puntos);
                        }


                        if (total != null) {
                            txt_preciototal.setText(String.format(new Locale("es", "MX"), "Total: $%.2f", total));
                        }
                        hideProductosView();

                    } else {
                        // Si no hay carrito activo, mostrar el mensaje de "no hay pedidos"
                        Log.e(LOG_TAG, "No se encontro carrito");
                        showProductosView();
                    }
                });
    }

    private void generateQRCode(String carritoId) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            // Generar el código QR en formato Matrix
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(carritoId, BarcodeFormat.QR_CODE, 512, 512);
            // Convertirlo a un Bitmap
            Bitmap bitmap = toBitmap(bitMatrix);
            // Establecer el Bitmap en el ImageView
            mImageQR.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(LOG_TAG, "Error generando el código QR", e);
        }
    }

    // Método para convertir el BitMatrix a un Bitmap
    private Bitmap toBitmap(com.google.zxing.common.BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Negro o blanco
            }
        }
        return bitmap;
    }

    private void setupProductosCarrito(String carritoId) {
        Query productosCarritoQuery = mAgregarProductosProvider.AllProductosCarrito(carritoId);
        FirestoreRecyclerOptions<ProductosCarritoModel> options = new FirestoreRecyclerOptions.Builder<ProductosCarritoModel>()
                .setQuery(productosCarritoQuery, ProductosCarritoModel.class)
                .build();

        mProductoReciboAdapter = new ProductoReciboAdapter(options, this);
        mRecyclerProductos.setAdapter(mProductoReciboAdapter);
        mProductoReciboAdapter.startListening();
    }

    private void showProductosView() {
        txt_leyenda.setVisibility(View.VISIBLE);
        mImageticket.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    private void hideProductosView() {
        txt_leyenda.setVisibility(View.GONE);  // Cambiar a GONE si no quieres mostrar estos
        mImageticket.setVisibility(View.GONE);  // Cambiar a GONE si no quieres mostrar estos
        scrollView.setVisibility(View.VISIBLE);  // Mostrar ScrollView si no hay productos
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProductoReciboAdapter != null) {
            mProductoReciboAdapter.stopListening();
        }
    }
}