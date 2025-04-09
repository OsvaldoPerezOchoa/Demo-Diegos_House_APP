package com.ayzconsultores.diegoshouse.providers;

// Importamos las clases necesarias para interactuar con Firebase Firestore y manejar tareas
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

// Clase que proporciona métodos para interactuar con la colección "Productos" en Firebase Firestore
public class ProductosProvider {

    // Referencia a la colección "Productos" en Firestore
    CollectionReference mCollection;

    // Constructor que inicializa la referencia a la colección
    public ProductosProvider() {
        // Obtenemos una instancia de FirebaseFirestore y apuntamos a la colección "Productos"
        mCollection = FirebaseFirestore.getInstance().collection("Productos");
    }

    // Método para obtener todos los productos ordenados por nombre de manera descendente
    public Query getAll() {
        // Retornamos una consulta que ordena los documentos por el campo "nombre" en orden descendente
        return mCollection.whereEqualTo("estado", "activo").orderBy("nombre", Query.Direction.DESCENDING);
    }

    // Método para filtrar productos por categoría específica
    public Query getFiltro(String Categoria) {
        // Retornamos una consulta que filtra los productos por el campo "categoria"
        return mCollection.whereEqualTo("estado", "activo").whereEqualTo("categoria", Categoria);
    }

    // Método para obtener productos que pertenecen a la categoría "Promociones"
    public Query getPromo() {
        // Filtramos los productos por el campo "categoria" con el valor "Promociones"
        return mCollection.whereEqualTo("categoria", "Promociones");
    }

    // Método para obtener un producto específico por su ID
    public Task<DocumentSnapshot> getProductoId(String id) {
        // Accedemos al documento en la colección que tiene el ID proporcionado y lo retornamos
        return mCollection.document(id).get();
    }

    // Método para obtener un producto específico por su ID y escuchar cambios en tiempo real
    public DocumentReference getProductoTiemporeal(String id) {
        // Accedemos al documento en la colección que tiene el ID proporcionado
        return mCollection.document(id);
    }

}
