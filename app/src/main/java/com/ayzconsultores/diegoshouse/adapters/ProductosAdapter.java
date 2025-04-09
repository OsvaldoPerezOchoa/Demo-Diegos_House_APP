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
import com.ayzconsultores.diegoshouse.activities.DetallesProductoActivity;
import com.ayzconsultores.diegoshouse.models.ProductosModel;
import com.ayzconsultores.diegoshouse.providers.ComentarioProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.Locale;

// Adaptador para mostrar los productos en un RecyclerView
public class ProductosAdapter extends FirestoreRecyclerAdapter<ProductosModel, ProductosAdapter.ViewHolder> {

    private final Context context;  // Contexto de la actividad o fragmento que usa el adaptador
    private final ComentarioProvider comentarioProvider;  // Proveedor para obtener los comentarios del producto

    // Constructor del adaptador, recibe las opciones para Firestore y el contexto
    public ProductosAdapter(FirestoreRecyclerOptions<ProductosModel> options, Context context) {
        super(options);  // Pasa las opciones al constructor de FirestoreRecyclerAdapter
        this.context = context;  // Asigna el contexto
        this.comentarioProvider = new ComentarioProvider();  // Inicializa el proveedor de comentarios
    }

    // Método para vincular los datos del modelo a las vistas del ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ProductosModel productosModel) {
        // Establece el nombre y el precio del producto en sus respectivos TextView
        holder.textViewNombre.setText(productosModel.getNombre());
        // Verifica si el precio es 0 para usar los puntos

        if (productosModel.getPrecio() == 0) {
            // Muestra los puntos en formato entero y sin el símbolo de moneda
            holder.textViewPrecio.setText(String.format(Locale.getDefault(), "%d puntos", productosModel.getPuntos().intValue()));
        } else {
            // Si el precio no es 0, muestra el precio en formato moneda
            holder.textViewPrecio.setText(String.format(new Locale("es", "MX"), "$%.2f", productosModel.getPrecio()));
        }


        // Obtiene el ID del documento del producto
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String bannerId = document.getId();

        // Carga la imagen del producto usando Picasso
        if (productosModel.getImagen() != null && !productosModel.getImagen().isEmpty()) {
            // Si la URL de la imagen es válida, carga la imagen desde la URL
            Picasso.get()
                    .load(productosModel.getImagen())
                    .error(R.drawable.sinimagen)  // Imagen predeterminada en caso de error
                    .into(holder.imageViewProducto);
        } else {
            // Si no hay URL, se muestra una imagen predeterminada
            holder.imageViewProducto.setImageResource(R.drawable.sinimagen);
        }

        // Establece el comportamiento al hacer clic en un producto (lleva a la actividad de detalles)
        holder.viewHolder.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetallesProductoActivity.class);  // Crea la intención para abrir los detalles del producto
            intent.putExtra("id", bannerId);  // Pasa el ID del producto a la actividad
            context.startActivity(intent);  // Inicia la actividad de detalles
        });

        // Obtiene los comentarios del producto para calcular su calificación promedio
        comentarioProvider.getComentarios(bannerId).addSnapshotListener((querySnapshot, error) -> {
            if (error != null || querySnapshot == null) {
                holder.textViewCalificacion.setText("0.0");  // Si ocurre un error o no hay comentarios, se establece la calificación a 0
                return;
            }

            double totalCalificacion = 0;  // Variable para sumar las calificaciones
            int totalComentarios = 0;  // Variable para contar los comentarios

            // Itera sobre todos los comentarios del producto
            for (DocumentSnapshot comentarioDocument : querySnapshot.getDocuments()) {
                if (comentarioDocument.contains("calificacion")) {  // Verifica si el comentario tiene calificación
                    Double calificacion = comentarioDocument.getDouble("calificacion");
                    if (calificacion != null) {
                        totalCalificacion += calificacion;  // Suma la calificación
                        totalComentarios++;  // Aumenta el contador de comentarios
                    }
                }
            }

            // Calcula el promedio de calificación
            double promedio = totalComentarios > 0 ? totalCalificacion / totalComentarios : 0.0;
            // Muestra el promedio de calificación en el TextView
            holder.textViewCalificacion.setText(String.format(Locale.getDefault(), "%.1f", promedio));
        });
    }


    // Método para crear un nuevo ViewHolder inflando el layout correspondiente
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout 'cardview_productos' para cada item en el RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_productos, parent, false);
        return new ViewHolder(view);  // Devuelve un nuevo ViewHolder con la vista inflada
    }

    // ViewHolder para almacenar las vistas de cada ítem de la lista
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Componentes de las vistas
        TextView textViewNombre, textViewPrecio, textViewCalificacion;
        ImageView imageViewProducto;
        View viewHolder;

        // Constructor del ViewHolder, se enlazan las vistas del layout
        public ViewHolder(View view) {
            super(view);
            // Enlazamos las vistas del layout con los componentes correspondientes
            textViewNombre = view.findViewById(R.id.txt_titulo);
            textViewPrecio = view.findViewById(R.id.txt_precio);
            imageViewProducto = view.findViewById(R.id.img_producto);
            textViewCalificacion = view.findViewById(R.id.txt_calification);
            viewHolder = view;
        }
    }
}
