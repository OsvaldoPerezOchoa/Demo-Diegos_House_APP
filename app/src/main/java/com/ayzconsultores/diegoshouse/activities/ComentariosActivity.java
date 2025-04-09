package com.ayzconsultores.diegoshouse.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.adapters.ComentariosAdapter;
import com.ayzconsultores.diegoshouse.models.ComentarioModel;
import com.ayzconsultores.diegoshouse.providers.ComentarioProvider;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class ComentariosActivity extends AppCompatActivity {

    RecyclerView recycler;
    ComentarioProvider mComentarioProvider;
    ComentariosAdapter mComentariosAdapter;
    ProductosProvider mProductosProvider;
    TextView mTextViewNombre, mTextViewCalificacion, mTextViewTotal, mTextViewComentario;
    ImageView mImageViewProducto, mImageViewComentario;
    RatingBar mPromedio;
    String idProducto;
    MaterialToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comentarios);

        // Configuración para el color de la barra de navegación
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        idProducto = getIntent().getStringExtra("idProducto");
        mComentarioProvider = new ComentarioProvider();
        mProductosProvider = new ProductosProvider();
        recycler = findViewById(R.id.recyclercomentarios);
        mTextViewNombre = findViewById(R.id.textViewNombre);
        mToolbar = findViewById(R.id.topAppBarComentarios);
        mTextViewCalificacion = findViewById(R.id.textViewcalificacion);
        mImageViewProducto = findViewById(R.id.img_producto);
        mPromedio = findViewById(R.id.calificacionRatingBar);
        mTextViewTotal = findViewById(R.id.textViewTotal);
        mImageViewComentario = findViewById(R.id.imageViewComentario);
        mTextViewComentario = findViewById(R.id.TextViewComentario);
        LinearLayoutManager managerBanner = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(managerBanner);
        CargarDatos(idProducto);

        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void CargarDatos(String idProducto) {
        if (idProducto == null || idProducto.isEmpty()) {
            return; // Evitar errores si el ID es nulo o vacío
        }

        mProductosProvider.getProductoId(idProducto).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Asume que los campos del documento son "nombre", "calificacion" y "imagenUrl"
                String nombre = documentSnapshot.getString("nombre");
                String imagenUrl = documentSnapshot.getString("imagen");

                // Actualizar TextView de nombre
                if (nombre != null) {
                    mTextViewNombre.setText(nombre);
                }

                // Cargar imagen en el ImageView usando Picasso
                if (imagenUrl != null && !imagenUrl.isEmpty()) {
                    Picasso.get().load(imagenUrl).into(mImageViewProducto);
                }

                calcularCalificacionPromedio(idProducto);
            }
        }).addOnFailureListener(e -> {
            // Manejar errores
            Toast.makeText(this, "Error al cargar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void calcularCalificacionPromedio(String idProducto) {
        mComentarioProvider.getComentarios(idProducto).get().addOnSuccessListener(querySnapshot -> {
            double sumaCalificaciones = 0;
            int totalComentarios = querySnapshot.size();

            if (totalComentarios > 0) {
                // Hay comentarios: mostramos el RecyclerView y ocultamos el mensaje vacío
                recycler.setVisibility(View.VISIBLE);
                mImageViewComentario.setVisibility(View.GONE);
                mTextViewComentario.setVisibility(View.GONE);

                // Sumar todas las calificaciones
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Double calificacion = document.getDouble("calificacion");
                    if (calificacion != null) {
                        sumaCalificaciones += calificacion;
                    }
                }

                // Calcular promedio y asignar al TextView
                double promedio = sumaCalificaciones / totalComentarios; // Mantener en escala de 0 a 5
                mTextViewCalificacion.setText(
                        String.format(
                                new Locale("es", "MX"),
                                mTextViewCalificacion.getResources().getString(R.string.calificacion_texto),
                                promedio
                        )
                );
                mPromedio.setRating((float) promedio);
                mTextViewTotal.setText(getString(R.string.total_comments, totalComentarios));
            } else {
                // No hay comentarios: mostramos el mensaje vacío y ocultamos el RecyclerView
                recycler.setVisibility(View.GONE);
                mImageViewComentario.setVisibility(View.VISIBLE);
                mTextViewComentario.setVisibility(View.VISIBLE);

                // Actualizar los textos informativos
                mTextViewCalificacion.setText(R.string.no_hay_comentarios);
                mTextViewTotal.setText(getString(R.string.total_comments, 0));
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error al calcular calificaciones: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    @Override
    protected void onStart() {
        super.onStart();


        if (idProducto != null) {
            Query comentariosQuery = mComentarioProvider.getComentarios(idProducto);
            FirestoreRecyclerOptions<ComentarioModel> options = new FirestoreRecyclerOptions.Builder<ComentarioModel>()
                    .setQuery(comentariosQuery, ComentarioModel.class)
                    .build();

            mComentariosAdapter = new ComentariosAdapter(options, this);
            recycler.setAdapter(mComentariosAdapter);

            // Iniciar escucha de datos
            mComentariosAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mComentariosAdapter != null) {
            mComentariosAdapter.stopListening();
        }
    }

}