package com.ayzconsultores.diegoshouse.providers;

// Importamos las clases necesarias para interactuar con Firebase Firestore y manejar tareas
import com.ayzconsultores.diegoshouse.models.CarritoModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Clase que proporciona métodos para interactuar con la colección "Carrito" en Firebase Firestore
public class CarritoProvider {

    // Referencia a la colección "Carrito" en Firestore
    CollectionReference mCollection;

    // Constructor que inicializa la referencia a la colección
    public CarritoProvider() {
        // Obtenemos una instancia de FirebaseFirestore y apuntamos a la colección "Carrito"
        mCollection = FirebaseFirestore.getInstance().collection("Carrito");
    }

    // Método para obtener el carrito activo de un usuario
    public Query CarritoActivo(String usuarioId) {
        // Retornamos una consulta que filtra por el campo "id_usuario" y el estado "activo"
        return mCollection.whereEqualTo("id_usuario", usuarioId).whereEqualTo("estado", "activo");
    }

    // Método para obtener los carritos finalizados de un usuario
    public Query CarritoFinalizado(String usuarioId) {
        // Retornamos una consulta que filtra por el campo "id_usuario" y el estado "finalizado"
        return mCollection.whereEqualTo("id_usuario", usuarioId).whereEqualTo("estado", "finalizado");
    }

    // Método para obtener los carritos en producción de un usuario
    public Query CarritoProduccionORecoleccion(String usuarioId) {
        // Retornamos una consulta que filtra por el campo "id_usuario" y donde el estado sea "produccion" o "recoleccion"
        return mCollection.whereEqualTo("id_usuario", usuarioId)
                .whereIn("estado", Arrays.asList("produccion", "recoleccion", "aceptado"));
    }


    // Método para obtener un carrito específico por su ID
    public Task<DocumentSnapshot> getCarritoId(String id) {
        // Obtenemos el documento de la colección que tiene el ID proporcionado
        return mCollection.document(id).get();
    }

    // Método para actualizar el total de un carrito y cambiar su estado a "produccion"
    public Task<Void> updateCarritoTotal(String carritoId, double total) {
        // Creamos un mapa con los campos a actualizar
        Map<String, Object> updates = new HashMap<>();
        updates.put("total", total);          // Actualizamos el campo "total"
        updates.put("estado", "aceptado");  // Cambiamos el estado a "produccion"

        // Actualizamos el documento del carrito con el ID proporcionado
        return mCollection.document(carritoId).update(updates);
    }

    public Task<Void> preciototalCarrito(String carritoId, double total) {
        // Creamos un mapa con los campos a actualizar
        Map<String, Object> updates = new HashMap<>();
        updates.put("total", total);          // Actualizamos el campo "total"
        // Actualizamos el documento del carrito con el ID proporcionado
        return mCollection.document(carritoId).update(updates);
    }

    // Método para crear un nuevo carrito en la colección
    public Task<DocumentReference> createCarrito(CarritoModel carrito) {
        // Añadimos el objeto del modelo CarritoModel a la colección y retornamos la tarea
        return mCollection.add(carrito);
    }

    // Método para actualizar los puntos de un carrito
    public Task<Void> updateCarritoPuntosYFecha(String carritoId, int puntos, Timestamp fechaCompra, int puntosUsados) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("puntos", puntos);
        updates.put("fecha_compra", fechaCompra);
        updates.put("puntos_usados", puntosUsados);

        return mCollection.document(carritoId).update(updates);
    }


}
