package com.ayzconsultores.diegoshouse.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.models.ComentarioModel;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Adaptador para mostrar los comentarios en un RecyclerView
public class ComentariosAdapter extends FirestoreRecyclerAdapter<ComentarioModel, ComentariosAdapter.ViewHolder> {

    Context context;  // Contexto para realizar acciones dentro de la aplicación (como abrir actividades)
    private final UsersProvider usersProvider;  // Proveedor de datos de usuario para obtener los detalles del usuario

    // Constructor del adaptador que recibe las opciones de Firestore y el contexto
    public ComentariosAdapter(FirestoreRecyclerOptions<ComentarioModel> options, Context contexto) {
        super(options);
        this.context = contexto;  // Asignamos el contexto para navegar entre actividades
        this.usersProvider = new UsersProvider();  // Inicializamos el proveedor de usuarios
    }

    // Método que se encarga de vincular los datos del modelo con las vistas del ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ComentarioModel model) {
        try {
            // Obtenemos el ID del usuario que hizo el comentario
            String idUsuario = model.getId_usuario();

            // Consultamos los detalles del usuario mediante su ID
            usersProvider.getUser(idUsuario).addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Si el usuario existe, mostramos su nombre
                    String nombreUsuario = documentSnapshot.getString("nombre");
                    holder.mNombreUsuario.setText(nombreUsuario);
                } else {
                    // Si no existe, mostramos un texto de "Usuario desconocido"
                    holder.mNombreUsuario.setText(R.string.usuario_desconocido);
                }
            }).addOnFailureListener(e -> {
                // Si ocurre un error, también mostramos "Usuario desconocido"
                holder.mNombreUsuario.setText(R.string.usuario_desconocido);
            });

            // Asignamos el comentario al TextView correspondiente
            holder.mComentario.setText(model.getComentario());

            // Mostramos la calificación del comentario en el RatingBar
            holder.mcalificacionRatingBar.setRating(model.getCalificacion());

            // Formateamos y mostramos la fecha del comentario
            holder.mFechaComentario.setText(String.format(Locale.ROOT, "Fecha: %s", formatTimestamp(model.getFecha_comentario())));
        } catch (Exception e) {
            // En caso de error, mostramos el error en el log
            Log.e("ComentariosAdapter", "Error en onBindViewHolder", e);
        }
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comentario, parent, false);
        return new ViewHolder(view);  // Retornamos el ViewHolder con el layout inflado
    }

    // Clase ViewHolder que contiene las vistas que representan cada item en el RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Vistas para mostrar el nombre del usuario, la fecha, el comentario y la calificación
        TextView mNombreUsuario, mFechaComentario, mComentario;
        RatingBar mcalificacionRatingBar;

        // Constructor que inicializa las vistas a partir del layout inflado
        public ViewHolder(View view) {
            super(view);

            // Enlazamos las vistas del layout con los componentes correspondientes
            mNombreUsuario = view.findViewById(R.id.nombreUsuarioTextView);
            mFechaComentario = view.findViewById(R.id.fechaComentario);
            mComentario = view.findViewById(R.id.comentarioTextView);
            mcalificacionRatingBar = view.findViewById(R.id.calificacionRatingBar);
        }
    }
}
