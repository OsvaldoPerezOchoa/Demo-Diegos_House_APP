package com.ayzconsultores.diegoshouse.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.activities.DetallesProductoActivity;
import com.ayzconsultores.diegoshouse.models.ProductosModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

// Adaptador que extiende de FirestoreRecyclerAdapter para mostrar productos en un RecyclerView
public class BannerAdapter extends FirestoreRecyclerAdapter<ProductosModel, BannerAdapter.ViewHolder> {

    // Contexto para realizar la navegación o acceder a recursos de la aplicación
    private final Context contexto;

    // Constructor que recibe las opciones de FirestoreRecyclerAdapter y el contexto de la actividad
    public BannerAdapter(FirestoreRecyclerOptions<ProductosModel> options, Context contexto) {
        super(options);
        this.contexto = contexto;  // Asignamos el contexto proporcionado
    }

    // Método que se llama para enlazar los datos de cada producto con los elementos del ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ProductosModel productosModel) {
        // Obtenemos el documento de la base de datos Firestore para poder acceder a su ID
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String bannerid = document.getId();  // Guardamos el ID del producto

        // Verificamos si el producto tiene una imagen asignada
        if (productosModel.getImagen() != null) {
            // Si la imagen no está vacía, la cargamos usando Picasso
            if (!productosModel.getImagen().isEmpty()) {
                Picasso.get().load(productosModel.getImagen()).into(holder.imageViewPromo);  // Cargamos la imagen en el ImageView
            } else {
                // Si no tiene imagen, mostramos una imagen predeterminada
                holder.imageViewPromo.setImageResource(R.drawable.sinimagen);
            }
        }

        // Establecemos un listener para que cuando el usuario haga clic en el elemento del RecyclerView,
        // se abra la pantalla de detalles del producto
        holder.viewHolder.setOnClickListener(view -> {
            Intent intent = new Intent(contexto, DetallesProductoActivity.class);  // Creamos un intent para abrir la actividad de detalles
            intent.putExtra("id", bannerid);  // Pasamos el ID del producto a la actividad de detalles
            contexto.startActivity(intent);  // Iniciamos la actividad
        });
    }

    // Método que se llama para crear un nuevo ViewHolder cuando es necesario
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout que representa cada item del RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_promocion, parent, false);
        return new ViewHolder(view);  // Devolvemos un nuevo ViewHolder con el layout inflado
    }

    // Clase ViewHolder que representa cada celda del RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPromo;
        View viewHolder;

        // Constructor que inicializa los elementos del ViewHolder
        public ViewHolder(View view) {
            super(view);
            imageViewPromo = view.findViewById(R.id.image_banner);
            viewHolder = view;
        }
    }
}
