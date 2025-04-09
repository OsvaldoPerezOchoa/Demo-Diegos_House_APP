package com.ayzconsultores.diegoshouse.providers;

// Importamos las clases necesarias para interactuar con Firebase Firestore y manejar tareas
import com.ayzconsultores.diegoshouse.models.ComentarioModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

// Clase que proporciona métodos para interactuar con la colección "Comentarios" en Firebase Firestore
public class ComentarioProvider {

    // Referencia a la colección "Comentarios" en Firestore
    CollectionReference mCollection;

    // Constructor que inicializa la referencia a la colección
    public ComentarioProvider() {
        // Obtenemos una instancia de FirebaseFirestore y apuntamos a la colección "Comentarios"
        mCollection = FirebaseFirestore.getInstance().collection("Comentarios");
    }

    // Método para crear un nuevo comentario
    public Task<Void> createComentario(ComentarioModel comentario) {
        // Creamos un nuevo documento en la colección "Comentarios" y establecemos su contenido basado en el modelo
        return mCollection.document().set(comentario);
    }

    // Método para obtener los comentarios asociados a un producto específico
    public Query getComentarios(String id_producto) {
        // Retornamos una consulta que filtra por el campo "id_producto"
        return mCollection.whereEqualTo("id_producto", id_producto);
    }
}
