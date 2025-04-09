package com.ayzconsultores.diegoshouse.providers;

import com.ayzconsultores.diegoshouse.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UsersProvider {

    CollectionReference mCollection;

    public UsersProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("Usuarios");
    }

    public Task<DocumentSnapshot> getUser(String id){
        return mCollection.document(id).get();
    }

    public Task<Void> createUser(User user){
        return mCollection.document(user.getId()).set(user);
    }
    public Task<Void> updateUser(User user){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", user.getNombre());
        map.put("telefono", user.getTelefono());
        return mCollection.document(user.getId()).update(map);
    }

    public Task<Void> actualizarPuntos(String userId, int puntosUsados, int puntosGanados) {
        return mCollection.document(userId).get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot snapshot = task.getResult();
                        // Obtener puntos actuales
                        long puntosActuales = snapshot.contains("puntos") ? snapshot.getLong("puntos") : 0;

                        // Primero restamos los puntos usados
                        long puntosRestados = puntosActuales - puntosUsados;

                        // Luego sumamos los puntos ganados
                        long puntosFinales = puntosRestados + puntosGanados;

                        Map<String, Object> data = new HashMap<>();
                        data.put("puntos", puntosFinales);  // Actualizamos con los puntos finales

                        // Actualizamos los puntos en Firestore
                        return mCollection.document(userId).update(data);
                    } else {
                        // Si no existe el usuario, creamos los puntos directamente
                        Map<String, Object> data = new HashMap<>();
                        data.put("puntos", puntosGanados);
                        return mCollection.document(userId).set(data);
                    }
                });
    }


    public Task<Integer> getPuntosPorId(String id) {
        return mCollection.document(id).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot snapshot = task.getResult();
                // Si el campo "puntos" existe, lo devuelve, sino devuelve 0
                return snapshot.contains("puntos") ? snapshot.getLong("puntos").intValue() : 0;
            } else {
                return 0; // Si no existe el usuario o no tiene puntos, retorna 0
            }
        });
    }

    public Task<String> getNombrePorId(String id) {
        return mCollection.document(id).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot snapshot = task.getResult();
                return snapshot.getString("nombre"); // Retorna el nombre del usuario
            } else {
                return null; // Si no existe el usuario, retorna null
            }
        });
    }

}
