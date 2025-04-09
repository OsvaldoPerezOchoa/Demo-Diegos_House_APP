package com.ayzconsultores.diegoshouse.providers;

// Importamos las clases necesarias para interactuar con Firebase Firestore y manejar tareas

import com.ayzconsultores.diegoshouse.models.ProductosCarritoModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Tasks;

import java.util.Objects;

// Clase que proporciona métodos para interactuar con la colección "ProductosCarrito" en Firebase Firestore
public class AgregarProductosProvider {

    // Referencia a la colección "ProductosCarrito" en Firestore
    CollectionReference mCollection;

    // Constructor que inicializa la referencia a la colección
    public AgregarProductosProvider() {
        // Obtenemos una instancia de FirebaseFirestore y apuntamos a la colección "ProductosCarrito"
        mCollection = FirebaseFirestore.getInstance().collection("ProductosCarrito");
    }

    // Método para obtener todos los productos de un carrito específico
    public Query AllProductosCarrito(String carritoId) {
        // Retornamos una consulta que filtra por el campo "id_carrito"
        return mCollection.whereEqualTo("id_carrito", carritoId);
    }

    // Método para agregar un producto al carrito
    public Task<DocumentReference> AgregarProducto(ProductosCarritoModel producto) {
        // Añadimos el objeto del modelo ProductoCarrito a la colección y retornamos la tarea
        return mCollection.add(producto);
    }

    // Método para eliminar un producto específico de un carrito
    public Task<Void> BorrarProducto(String productoId, String carritoId) {
        // Realizamos una consulta para encontrar el producto en el carrito correspondiente
        return mCollection
                .whereEqualTo("id_producto", productoId)   // Filtramos por el campo "id_producto"
                .whereEqualTo("id_carrito", carritoId)     // Filtramos por el campo "id_carrito"
                .get()                                    // Ejecutamos la consulta
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        // Obtenemos los resultados de la consulta
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Si encontramos el producto, procedemos a borrarlo
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            return mCollection.document(documentSnapshot.getId()).delete();  // Eliminamos el documento
                        } else {
                            // Si no se encuentra el producto, lanzamos una excepción
                            throw new Exception("Producto no encontrado en el carrito.");
                        }
                    } else {
                        // Si ocurre un error en la consulta, lanzamos la excepción correspondiente
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    // Método para obtener un producto específico en un carrito
    public Task<QuerySnapshot> getProductoEnCarrito(String carritoId, String productoId) {
        // Retornamos una consulta que filtra por "id_carrito" e "id_producto"
        return mCollection
                .whereEqualTo("id_carrito", carritoId)
                .whereEqualTo("id_producto", productoId)
                .get();
    }

    public Task<String> obtenerIdProductoDesdeCarrito(String carritoId) {
        return mCollection.whereEqualTo("id_carrito", carritoId)
                .limit(1)  // Limitamos la consulta a un solo resultado
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot productoCarritoDoc = querySnapshot.getDocuments().get(0);
                            String idProducto = productoCarritoDoc.getString("id_producto");  // Obtener el id_producto
                            return Tasks.forResult(idProducto);
                        } else {
                            throw new Exception("No se encontraron productos en el carrito.");
                        }
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

}
