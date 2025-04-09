package com.ayzconsultores.diegoshouse.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.activities.CarritoHistorialActivity;
import com.ayzconsultores.diegoshouse.models.CarritoModel;
import com.ayzconsultores.diegoshouse.providers.AgregarProductosProvider;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Adaptador para mostrar los carritos en un RecyclerView
public class CarritoAdapter extends FirestoreRecyclerAdapter<CarritoModel, CarritoAdapter.ViewHolder> {

    // Contexto para realizar acciones dentro de la aplicación (como abrir actividades)
    private final Context context;
    AgregarProductosProvider mAgregarProductosProvider;
    ProductosProvider mProductosProvider;

    // Constructor del adaptador que recibe las opciones de Firestore y el contexto
    public CarritoAdapter(FirestoreRecyclerOptions<CarritoModel> options, Context context) {
        super(options);
        this.context = context;  // Asignamos el contexto para navegar entre actividades
        mAgregarProductosProvider = new AgregarProductosProvider();  // Inicializamos el proveedor de productos
        mProductosProvider = new ProductosProvider();  // Inicializamos el proveedor de productos
    }

    // Método que se encarga de vincular los datos del modelo con las vistas del ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull CarritoModel carritoModel) {
        // Mostrar el estado del carrito
        holder.textViewEstatus.setText(String.format(Locale.ROOT, "Estatus: %s", carritoModel.getEstado()));

        // Mostrar el precio total del carrito con formato de moneda
        double precio = carritoModel.getTotal();  // Obtener el total del carrito
        holder.textViewPrecio.setText(String.format(Locale.getDefault(), "Total: $%.2f", precio));  // Formato de precio

        // Mostrar la fecha de creación del carrito, usando el método auxiliar para formatear la fecha
        holder.textViewFecha.setText(String.format(Locale.ROOT, "Fecha: %s", formatTimestamp(carritoModel.getFecha_compra())));

        // Obtener el ID del carrito de Firestore para identificarlo de manera única
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String carritoId = document.getId();
        holder.textViewid.setText(String.format(Locale.ROOT, "No. Pedido: %s", carritoId));

        // Obtener el ID del producto asociado al carrito
        // Obtener el ID del producto asociado al carrito
        mAgregarProductosProvider.obtenerIdProductoDesdeCarrito(carritoId).continueWithTask(task -> {
            if (task.isSuccessful()) {
                String idProducto = task.getResult();  // Obtener el id_producto

                // Obtener los detalles del producto
                return mProductosProvider.getProductoId(idProducto);
            } else {
                throw new Exception("No se pudo obtener el ID del producto.");
            }
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot productoSnapshot = task.getResult();
                String imagenUrl = productoSnapshot.getString("imagen");  // Obtener la URL de la imagen

                // Cargar la imagen con Picasso de manera optimizada
                Picasso.get()
                        .load(imagenUrl)  // Cargar la imagen desde la URL
                        .resize(200, 200)  // Redimensionar la imagen para no ocupar demasiados recursos
                        .centerCrop()  // Asegurarse de que la imagen se recorte correctamente si es más grande
                        .placeholder(R.drawable.sinimagen)  // Imagen de carga (opcional)
                        .error(R.drawable.sinimagen)  // Imagen de error (opcional)
                        .into(holder.img_producto);  // Colocar la imagen en el ImageView
            } else {
                // Manejar el error si no se pudo obtener la información del producto
                Picasso.get().load(R.drawable.sinimagen).into(holder.img_producto);
            }
        });


        // Manejar el clic en cada elemento del RecyclerView para abrir la actividad de historial del carrito
        holder.viewHolder.setOnClickListener(view -> {
            Intent intent = new Intent(context, CarritoHistorialActivity.class);  // Creamos el Intent para abrir la actividad
            intent.putExtra("id", carritoId);  // Pasamos el ID del carrito a la nueva actividad
            context.startActivity(intent);  // Iniciamos la actividad
        });
    }

    // Método auxiliar para formatear un objeto Timestamp en una cadena de texto con formato de fecha
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();  // Convertimos el Timestamp a un objeto Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());  // Establecemos el formato de fecha
            return dateFormat.format(date);  // Formateamos la fecha y la retornamos como una cadena
        }
        return "";  // Si no hay fecha, retornamos una cadena vacía
    }

    // Método para crear un nuevo ViewHolder para cada item del RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout para cada item del RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_recibos, parent, false);
        return new ViewHolder(view);  // Retornamos el ViewHolder con el layout inflado
    }

    // Clase ViewHolder que contiene las vistas que representan cada item en el RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewid, textViewPrecio, textViewFecha, textViewEstatus;
        ImageView img_producto;
        View viewHolder;

        // Constructor que inicializa las vistas a partir del layout inflado
        public ViewHolder(View view) {
            super(view);
            // Enlazamos las vistas del layout con los componentes correspondientes
            textViewid = view.findViewById(R.id.txt_id);
            textViewPrecio = view.findViewById(R.id.txt_precio);
            textViewFecha = view.findViewById(R.id.txt_Fecha);
            img_producto = view.findViewById(R.id.img_carrito);
            textViewEstatus = view.findViewById(R.id.txt_estatus);
            viewHolder = view;
        }
    }

}
