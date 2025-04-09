package com.ayzconsultores.diegoshouse.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.adapters.FAQAdapter;
import com.ayzconsultores.diegoshouse.models.FAQModel;
import com.ayzconsultores.diegoshouse.providers.FAQProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;

public class AyudaActivity extends AppCompatActivity {

    MaterialToolbar mToolbar;
    RecyclerView mRecyclerViewFAQ;
    FAQAdapter mFaqAdapter;
    FAQProvider mFAQProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ayuda);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mFAQProvider = new FAQProvider();
        mToolbar = findViewById(R.id.topAppBarHistorial);
        mToolbar.setNavigationOnClickListener(v -> finish());
        mRecyclerViewFAQ = findViewById(R.id.recyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Configuración del adaptador para los productos
        Query faqQuery = mFAQProvider.getAllFAQ();
        FirestoreRecyclerOptions<FAQModel> options = new
                FirestoreRecyclerOptions.Builder<FAQModel>()
                .setQuery(faqQuery, FAQModel.class)
                .build();
        mFaqAdapter = new FAQAdapter(options, this) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }
        };
        mRecyclerViewFAQ.setAdapter(mFaqAdapter);
        mFaqAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mFaqAdapter.stopListening();
    }
}