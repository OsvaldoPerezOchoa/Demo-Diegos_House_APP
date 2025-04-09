package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.adapters.ComentariosAdapter;
import com.ayzconsultores.diegoshouse.models.CarritoModel;
import com.ayzconsultores.diegoshouse.models.ComentarioModel;
import com.ayzconsultores.diegoshouse.models.ProductosCarritoModel;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.CarritoProvider;
import com.ayzconsultores.diegoshouse.providers.AgregarProductosProvider;
import com.ayzconsultores.diegoshouse.providers.ComentarioProvider;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DetallesProductoActivity extends AppCompatActivity {

    String mExtraProductoId;
    ProductosProvider mProductosProvider;
    TextView mNombreProducto, mPrecioProducto, mDescripcionProducto, mPrecioTotal, mCont, mtotal_calificacion, mVer_mas, mNumerocalif, mCalificaion_inicial;
    ImageView mImagenProducto;
    RatingBar mCalificacionLugarBar, mBarOpinion;
    RecyclerView mRecyclerComentarios;
    Button mAgregarCarrito;
    ImageButton mImagenPlus;
    TextInputEditText mComentarios;
    ImageButton mImagenMinus;
    ImageButton mImagenBack;
    int contador = 1;
    double precioInicial = 0.0;
    int puntosInicial = 0; // Cambiar de double a int
    CarritoProvider mCarritoProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    AgregarProductosProvider mProductoCarritoProvider;
    ComentarioProvider mComentarioProvider;
    ComentariosAdapter mComentariosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalles_producto);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        // Configura los insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de vistas y datos
        inicializarVistas();
        configurarBotones();
        obtenerProducto();
    }

    private void inicializarVistas() {
        mExtraProductoId = getIntent().getStringExtra("id");
        mProductosProvider = new ProductosProvider();
        mPrecioTotal = findViewById(R.id.txt_preciototal);
        mNombreProducto = findViewById(R.id.txt_titulo);
        mPrecioProducto = findViewById(R.id.txt_precio);
        mDescripcionProducto = findViewById(R.id.txt_descripcion);
        mImagenProducto = findViewById(R.id.img_producto);
        mCont = findViewById(R.id.txt_count);
        mAgregarCarrito = findViewById(R.id.btn_agregarcarrito);
        mImagenMinus = findViewById(R.id.img_minus);
        mImagenPlus = findViewById(R.id.img_plus);
        mVer_mas = findViewById(R.id.ver_mas);
        mCarritoProvider = new CarritoProvider();
        mAuthProvider = new AuthProvider();
        mProductoCarritoProvider = new AgregarProductosProvider();
        mUsersProvider = new UsersProvider();
        mCont.setText(String.valueOf(contador));
        mComentarioProvider = new ComentarioProvider();
        Button mEnviarComentario = findViewById(R.id.btn_enviar_comentario);
        mBarOpinion = findViewById(R.id.ratingBarOpinion);
        mtotal_calificacion = findViewById(R.id.total_calificacion);
        mNumerocalif = findViewById(R.id.numero_calificaciones);
        mComentarios = findViewById(R.id.input_comentario);
        mRecyclerComentarios = findViewById(R.id.recyclerview_opiniones);
        mCalificacionLugarBar = findViewById(R.id.calificacionLugarBar);
        mCalificaion_inicial = findViewById(R.id.calificaion_inicial);
        LinearLayoutManager managerBanner = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerComentarios.setLayoutManager(managerBanner);
        mImagenBack = findViewById(R.id.img_back);


        mVer_mas.setOnClickListener(v -> {
            Intent intent = new Intent(DetallesProductoActivity.this, ComentariosActivity.class);
            intent.putExtra("idProducto", mExtraProductoId); // Pasar el id del producto
            startActivity(intent);
        });

        mImagenBack.setOnClickListener(v -> finish());


        mEnviarComentario.setOnClickListener(v -> ValidarComentario());
    }

    private void ValidarComentario() {
        String comentario = Objects.requireNonNull(mComentarios.getText()).toString();
        float calificacion = mBarOpinion.getRating();

        if (comentario.isEmpty()) {
            mostrarMensaje("Por favor, ingresa un comentario.");
        }
        else if (calificacion == 0) {
            mostrarMensaje("Por favor, califica el producto.");
        }
        else {
            agregarComentario(comentario, calificacion);
        }
    }

    private void agregarComentario(String comentario, float calificacion) {
        ComentarioModel comentarioModel = new ComentarioModel();
        comentarioModel.setComentario(comentario);
        comentarioModel.setCalificacion(calificacion);
        comentarioModel.setId_producto(mExtraProductoId);
        comentarioModel.setId_usuario(mAuthProvider.getUid());
        comentarioModel.setFecha_comentario(new Timestamp(new Date()));

        mComentarioProvider.createComentario(comentarioModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mostrarMensaje("Comentario enviado correctamente.");

                // Limpia los campos después de enviar el comentario
                mComentarios.setText("");
                mBarOpinion.setRating(0);
            } else {
                mostrarMensaje("Error al enviar el comentario.");
            }
        });
    }



    private void configurarBotones() {
        mImagenPlus.setOnClickListener(v -> {
            contador++;
            actualizarContadorYPrecio();
        });

        mImagenMinus.setOnClickListener(v -> {
            if (contador > 1) {
                contador--;
                actualizarContadorYPrecio();
            }
        });

        mAgregarCarrito.setOnClickListener(v -> agregarCarrito());
    }

    private void actualizarContadorYPrecio() {
        mCont.setText(String.valueOf(contador));
        if (precioInicial > 0) {
            actualizarPrecioTotal();
        } else {
            actualizarPuntosTotal();
        }
    }

    private void actualizarContadorYpuntos() {
        mCont.setText(String.valueOf(contador));
        actualizarPuntosTotal();  // Solo actualizar puntos, si el producto es por puntos
    }

    private void actualizarPrecioTotal() {
        if (precioInicial > 0) {
            double precioTotal = precioInicial * contador;
            mPrecioTotal.setText(String.format(new Locale("es", "MX"), "$%.2f", precioTotal));
        } else {
            mPrecioTotal.setText(R.string.precio_no_disponible);
        }
    }

    private void actualizarPuntosTotal() {
        if (puntosInicial > 0) {
            int puntosTotal = puntosInicial * contador; // Mantener el cálculo como int
            mPrecioTotal.setText(String.format(Locale.getDefault(), "%d puntos", puntosTotal));
        } else {
            mPrecioTotal.setText(R.string.precio_no_disponible);
        }
    }


    private void agregarCarrito() {
        String idUsuario = mAuthProvider.getUid();

        if (puntosInicial > 0) {
            // Si el producto usa puntos, verifica si el usuario tiene suficientes puntos
            int puntosTotal = puntosInicial * contador;

            // Obtener los puntos del usuario
            mUsersProvider.getPuntosPorId(idUsuario).addOnSuccessListener(puntosUsuario -> {
                if (puntosUsuario >= puntosTotal) {
                    // El usuario tiene suficientes puntos
                    // Verifica si ya existe un carrito activo para el usuario
                    mCarritoProvider.CarritoActivo(idUsuario).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String carritoId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            verificarYAgregarProductoConPuntos(carritoId, puntosTotal);
                        } else {
                            crearNuevoCarritoYAgregarProductoConPuntos(idUsuario, puntosTotal);
                        }
                    });
                } else {
                    // El usuario no tiene suficientes puntos
                    mostrarMensaje("No tienes suficientes puntos para agregar este producto al carrito.");
                }
            }).addOnFailureListener(e -> mostrarMensaje("Error al obtener los puntos del usuario: " + e.getMessage()));

        } else {
            // Si el producto no usa puntos, se agrega normalmente con el precio
            double precioTotal = precioInicial * contador;

            // Verifica si ya existe un carrito activo para el usuario
            mCarritoProvider.CarritoActivo(idUsuario).get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String carritoId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    verificarYAgregarProducto(carritoId, precioTotal);
                } else {
                    crearNuevoCarritoYAgregarProducto(idUsuario, precioTotal);
                }
            });
        }
    }


    private void crearNuevoCarritoYAgregarProductoConPuntos(String idUsuario, int puntosTotal) {
        mUsersProvider.getNombrePorId(idUsuario).addOnSuccessListener(nombreUsuario -> {
            if (nombreUsuario != null) {
                // Crear el carrito con el nombre del usuario
                CarritoModel carrito = new CarritoModel();
                carrito.setId_usuario(idUsuario);
                carrito.setEstado("activo");
                carrito.setFecha_creacion(new Timestamp(new Date()));
                carrito.setNombre_usuario(nombreUsuario); // Establecer el nombre del usuario

                mCarritoProvider.createCarrito(carrito).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String carritoId = task.getResult().getId();
                        mostrarMensaje("Carrito creado correctamente.");
                        agregarProductoAlCarritoConPuntos(carritoId, puntosTotal);
                    } else {
                        mostrarMensaje("Error al crear el carrito.");
                    }
                });
            } else {
                mostrarMensaje("Error: Nombre del usuario no encontrado.");
            }
        }).addOnFailureListener(e -> mostrarMensaje("Error al obtener el nombre del usuario: " + e.getMessage()));
    }

    private void verificarYAgregarProductoConPuntos(String carritoId, int puntosTotal) {
        mProductoCarritoProvider.getProductoEnCarrito(carritoId, mExtraProductoId)
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        ProductosCarritoModel productoExistente = document.toObject(ProductosCarritoModel.class);

                        if (productoExistente != null) {
                            int nuevaCantidad = productoExistente.getCantidad() + contador;
                            int puntosActualizados = productoExistente.getPuntos_total() + puntosTotal;

                            // Verifica que los puntos actualizados no excedan los puntos del usuario
                            String idUsuario = mAuthProvider.getUid();
                            mUsersProvider.getPuntosPorId(idUsuario).addOnSuccessListener(puntosUsuario -> {
                                if (puntosUsuario >= puntosActualizados) {
                                    // Si el usuario tiene suficientes puntos, actualiza el carrito
                                    document.getReference().update(
                                            "cantidad", nuevaCantidad,
                                            "puntos_total", puntosActualizados
                                    ).addOnSuccessListener(aVoid -> {
                                        mostrarMensaje("Producto actualizado en el carrito con puntos.");
                                        redirigirHome();
                                    }).addOnFailureListener(e -> mostrarMensaje("Error al actualizar el producto con puntos: " + e.getMessage()));
                                } else {
                                    // Si no tiene suficientes puntos, muestra un mensaje
                                    mostrarMensaje("No tienes suficientes puntos para agregar más unidades de este producto.");
                                }
                            }).addOnFailureListener(e -> mostrarMensaje("Error al obtener los puntos del usuario: " + e.getMessage()));
                        }
                    } else {
                        agregarProductoAlCarritoConPuntos(carritoId, puntosTotal);
                    }
                }).addOnFailureListener(e -> mostrarMensaje("Error al verificar el producto con puntos: " + e.getMessage()));
    }


    private void agregarProductoAlCarritoConPuntos(String carritoId, int puntosTotal) {
        ProductosCarritoModel producto = new ProductosCarritoModel();
        producto.setId_producto(mExtraProductoId);
        producto.setId_carrito(carritoId);
        producto.setCantidad(contador);
        producto.setPuntos_total(puntosTotal);

        mProductoCarritoProvider.AgregarProducto(producto).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mostrarMensaje("Producto agregado al carrito con puntos correctamente.");
                redirigirHome();
            } else {
                mostrarMensaje("Error al agregar el producto con puntos al carrito");
            }
        });
    }

    private void verificarYAgregarProducto(String carritoId, double precioTotal) {
        mProductoCarritoProvider.getProductoEnCarrito(carritoId, mExtraProductoId)
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        ProductosCarritoModel productoExistente = document.toObject(ProductosCarritoModel.class);

                        if (productoExistente != null) {
                            int nuevaCantidad = productoExistente.getCantidad() + contador;
                            double nuevoPrecioTotal = productoExistente.getPrecio_total() + precioTotal;

                            document.getReference().update(
                                    "cantidad", nuevaCantidad,
                                    "precio_total", nuevoPrecioTotal
                            ).addOnSuccessListener(aVoid -> {
                                mostrarMensaje("Producto actualizado en el carrito.");
                                redirigirHome();
                            }).addOnFailureListener(e -> mostrarMensaje("Error al actualizar el producto: " + e.getMessage()));
                        }
                    } else {
                        agregarProductoAlCarrito(carritoId, precioTotal);
                    }
                }).addOnFailureListener(e -> mostrarMensaje("Error al verificar el producto: " + e.getMessage()));
    }


    private void agregarProductoAlCarrito(String carritoId, double precioTotal) {
        ProductosCarritoModel producto = new ProductosCarritoModel();
        producto.setId_producto(mExtraProductoId);
        producto.setId_carrito(carritoId);
        producto.setCantidad(contador);
        producto.setPrecio_total(precioTotal);

        mProductoCarritoProvider.AgregarProducto(producto).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mostrarMensaje("Producto agregado al carrito correctamente.");
                redirigirHome();
            } else {
                mostrarMensaje("Error al agregar el producto al carrito");
            }
        });
    }


    private void crearNuevoCarritoYAgregarProducto(String idUsuario, double precioTotal) {
        mUsersProvider.getNombrePorId(idUsuario).addOnSuccessListener(nombreUsuario -> {
            if (nombreUsuario != null) {
                // Crear el carrito con el nombre del usuario
                CarritoModel carrito = new CarritoModel();
                carrito.setId_usuario(idUsuario);
                carrito.setEstado("activo");
                carrito.setFecha_creacion(new Timestamp(new Date()));
                carrito.setNombre_usuario(nombreUsuario); // Establecer el nombre del usuario

                mCarritoProvider.createCarrito(carrito).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String carritoId = task.getResult().getId();
                        mostrarMensaje("Carrito creado correctamente.");
                        agregarProductoAlCarrito(carritoId, precioTotal);
                    } else {
                        mostrarMensaje("Error al crear el carrito.");
                    }
                });
            } else {
                mostrarMensaje("Error: Nombre del usuario no encontrado.");
            }
        }).addOnFailureListener(e -> mostrarMensaje("Error al obtener el nombre del usuario: " + e.getMessage()));
    }




    private void redirigirHome() {
        Intent intent = new Intent(DetallesProductoActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void obtenerProducto() {
        mProductosProvider.getProductoId(mExtraProductoId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Verificar el estado del producto
                String estado = documentSnapshot.getString("estado");
                if (estado != null && !estado.equals("activo")) {
                    // Mostrar un AlertDialog si el producto no está activo
                    mostrarProductoNoDisponible();
                } else {
                    mostrarProducto(documentSnapshot);
                }
            }
        });
    }

    private void mostrarProductoNoDisponible() {
        new android.app.AlertDialog.Builder(DetallesProductoActivity.this)
                .setTitle("Producto No Disponible")
                .setMessage("Este producto no está disponible en este momento.")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    // Redirigir al HomeActivity cuando el usuario acepta
                    redirigirHome();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarProducto(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.contains("imagen")) {
            String imagen = documentSnapshot.getString("imagen");
            if (imagen != null && !imagen.isEmpty()) {
                Picasso.get().load(imagen).into(mImagenProducto);
            } else {
                mImagenProducto.setImageResource(R.drawable.sinimagen); // Cargar la imagen predeterminada
            }
        } else {
            mImagenProducto.setImageResource(R.drawable.sinimagen); // Cargar la imagen predeterminada
        }

        mNombreProducto.setText(documentSnapshot.getString("nombre"));
        mDescripcionProducto.setText(documentSnapshot.getString("descripcion"));

        Double precio = documentSnapshot.getDouble("precio");
        Long puntos = documentSnapshot.getLong("puntos"); // Firebase devuelve los enteros como Long
        if (precio != null && precio > 0) {
            precioInicial = precio;
            mPrecioProducto.setText(String.format(new Locale("es", "MX"), "$%.2f", precioInicial));
            actualizarPrecioTotal();
        } else if (puntos != null && puntos > 0) {
            puntosInicial = puntos.intValue(); // Convertir Long a int
            mPrecioProducto.setText(String.format(Locale.getDefault(), "%d puntos", puntosInicial));
            actualizarContadorYpuntos();
        } else {
            mPrecioProducto.setText(R.string.precio_no_disponible);
        }

    }


    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Consulta limitada a 3 comentarios para mostrar
        Query comentariosQuery = mComentarioProvider.getComentarios(mExtraProductoId).limit(3);
        FirestoreRecyclerOptions<ComentarioModel> options = new FirestoreRecyclerOptions.Builder<ComentarioModel>()
                .setQuery(comentariosQuery, ComentarioModel.class)
                .build();

        mComentariosAdapter = new ComentariosAdapter(options, this);
        mRecyclerComentarios.setAdapter(mComentariosAdapter);

        mComentariosAdapter.startListening();

        // Cargar todas las calificaciones para el promedio
        calcularPromedioCalificaciones();
    }

    private void calcularPromedioCalificaciones() {
        mComentarioProvider.getComentarios(mExtraProductoId).get().addOnSuccessListener(querySnapshot -> {
            int totalComentarios = querySnapshot.size();
            double sumaCalificaciones = 0;

            // Sumar todas las calificaciones
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Double calificacion = document.getDouble("calificacion");
                if (calificacion != null) {
                    sumaCalificaciones += calificacion;
                }
            }

            // Actualizar vistas con los resultados
            mNumerocalif.setText(String.valueOf(totalComentarios));

            if (totalComentarios > 0) {
                float promedio = (float) (sumaCalificaciones / totalComentarios);
                mCalificacionLugarBar.setStepSize(0.1f);
                mCalificacionLugarBar.setRating(promedio);
                mtotal_calificacion.setText(String.format(new Locale("es", "MX"), "%.1f", promedio));
                mCalificaion_inicial.setText(String.format(new Locale("es", "MX"), "%.1f", promedio));
            } else {
                mCalificacionLugarBar.setRating(0.0f);
                mtotal_calificacion.setText("0.0");
                mCalificaion_inicial.setText("0.0");
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error al calcular el promedio: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    @Override
    protected void onStop() {
        super.onStop();
        mComentariosAdapter.stopListening();
    }


}
