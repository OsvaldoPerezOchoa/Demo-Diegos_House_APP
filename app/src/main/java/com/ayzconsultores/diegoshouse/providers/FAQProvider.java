package com.ayzconsultores.diegoshouse.providers;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FAQProvider {

    CollectionReference mCollection;

    public FAQProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("FAQ");
    }

    public Query getAllFAQ() {
        // Retornamos una consulta que ordena los documentos por el campo "nombre" en orden descendente
        return mCollection.orderBy("titulo", Query.Direction.DESCENDING);
    }
}
