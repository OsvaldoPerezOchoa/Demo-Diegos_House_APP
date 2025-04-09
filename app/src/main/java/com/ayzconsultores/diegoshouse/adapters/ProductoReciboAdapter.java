package com.ayzconsultores.diegoshouse.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.models.ProductosCarritoModel;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

import java.util.Locale;

// Adaptador para mostrar los productos dentro de un recibo o carrito
public class ProductoReciboAdapter extends FirestoreRecyclerAdapter<ProductosCarritoModel, ProductoReciboAdapter.ViewHolder> {

    Context context;  // Contexto de la aplicación (usado para interactuar con vistas, actividades, etc.)
    ProductosProvider productosProvider;  // Proveedor que permite obtener productos desde Firestore

    // Constructor que recibe las opciones de Firestore y el contexto
    public ProductoReciboAdapter(FirestoreRecyclerOptions<ProductosCarritoModel> options, Context context) {
        super(options);
        this.context = context;
        this.productosProvider = new ProductosProvider();  // Inicializamos el proveedor de productos
    }

    // Este método se encarga de enlazar los datos del modelo con las vistas del ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ProductosCarritoModel model) {
        String cantidadTexto = "Cantidad: " + model.getCantidad();
        String idProducto = model.getId_producto();
        holder.textViewCantidad.setText(cantidadTexto);

        // Consultamos Firestore para obtener los detalles del producto usando su ID
        productosProvider.getProductoId(idProducto)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String imagenUrl = documentSnapshot.getString("imagen");
                        holder.textViewNombre.setText(nombre);
                        holder.textViewTotal.setText(String.format(new Locale("es", "MX"), "Precio total: $%.2f", model.getPrecio_total()));

                        if (imagenUrl != null && !imagenUrl.isEmpty()) {
                            Picasso.get().load(imagenUrl).into(holder.imageViewProducto);
                        } else {
                            Log.e("Picasso", "La URL de la imagen es nula o vacía.");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener producto", e));
    }

    // Este método se llama para crear un nuevo ViewHolder con el layout especificado
    @NonNull
    @Override
    public ProductoReciboAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_producto_recibo, parent, false);
        return new ProductoReciboAdapter.ViewHolder(view);
    }

    // Clase ViewHolder que contiene las vistas que representan cada item en el RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewNombre, textViewCantidad, textViewTotal, textViewPrecio;
        ImageView imageViewProducto;

        public ViewHolder(View view) {
            super(view);
            textViewNombre = view.findViewById(R.id.txt_nombre);
            textViewCantidad = view.findViewById(R.id.txt_count);
            textViewTotal = view.findViewById(R.id.txt_preciototal);
            imageViewProducto = view.findViewById(R.id.img_producto);
        }
    }
}
