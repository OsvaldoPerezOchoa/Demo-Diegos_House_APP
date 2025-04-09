package com.ayzconsultores.diegoshouse.providers;

import com.ayzconsultores.diegoshouse.models.TokenModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import androidx.annotation.NonNull;
import android.util.Log;

public class TokenProvider {

    CollectionReference mCollection;

    public TokenProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("TokensNotificaciones");
    }

    public void createToken(final String user) {
        if (user == null) {
            return;
        }

        // Primero, eliminar el token localmente para forzar la regeneración
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Token FCM eliminado localmente. Solicitando uno nuevo...");
                        requestNewToken(user);
                    } else {
                        Log.e("FCM", "Error al eliminar el token localmente.", task.getException());
                    }
                });
    }

    private void requestNewToken(final String user) {
        // Solicita un nuevo token de FCM
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (token != null) {
                Log.d("FCM", "Nuevo token generado: " + token);

                // Eliminar el token antiguo de Firestore
                mCollection.document(user).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        mCollection.document(user).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FCM", "Token antiguo eliminado en Firestore.");
                                    saveNewToken(user, token);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FCM", "Error al eliminar el token en Firestore.", e);
                                    saveNewToken(user, token);
                                });
                    } else {
                        saveNewToken(user, token);
                    }
                });
            } else {
                Log.e("FCM", "No se pudo obtener un nuevo token.");
            }
        }).addOnFailureListener(e -> Log.e("FCM", "Error al obtener un nuevo token.", e));
    }

    private void saveNewToken(String user, String token) {
        TokenModel tokenModel = new TokenModel(token);
        mCollection.document(user).set(tokenModel)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Nuevo token guardado en Firestore."))
                .addOnFailureListener(e -> Log.e("FCM", "Error al guardar el nuevo token en Firestore.", e));
    }

    public Task<DocumentSnapshot> getToken(String user) {
        return mCollection.document(user).get();
    }
}
