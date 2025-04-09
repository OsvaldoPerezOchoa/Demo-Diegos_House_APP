package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ayzconsultores.diegoshouse.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ContactoActivity extends AppCompatActivity {

    protected TextView phone_text, email_text, social_m_legend;
    protected ImageView facebook_im, x_im, instagram_im;
    protected LinearLayout social_media_list;
    protected int social_media_weight;
    protected String facebook_link, x_twitter_link, instagram_link;
    MaterialToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacto);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mToolbar = findViewById(R.id.topAppBarContactanos);

        phone_text = findViewById(R.id.phone_number);
        email_text = findViewById(R.id.email);
        facebook_im = findViewById(R.id.facebook_ic);
        x_im = findViewById(R.id.x_ic);
        instagram_im = findViewById(R.id.instagram_ic);
        social_m_legend = findViewById(R.id.social_media_legend);
        social_media_list = findViewById(R.id.social_media_list);

        mToolbar.setNavigationOnClickListener(v -> finish());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Soporte").document("contacto");

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extract support contact
                    String phone = document.getString("telefono");
                    String email = document.getString("email");
                    facebook_link = document.getString("facebook");
                    x_twitter_link = document.getString("x");
                    instagram_link = document.getString("instagram");

                    // Show on layout
                    phone_text.setText(phone);
                    email_text.setText(email);


                    // OnClickListener for facebook button
                    facebook_im.setOnClickListener(view -> {
                        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(facebook_link));
                        startActivity(intent);
                    });
                    // OnClickListener for x button
                    x_im.setOnClickListener(view -> {
                        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(x_twitter_link));
                        startActivity(intent);
                    });
                    // OnClickListener for instagram button
                    instagram_im.setOnClickListener(view -> {
                        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(instagram_link));
                        startActivity(intent);
                    });

                    // Social media
                    social_media_weight = 0;
                    if (facebook_link != null && !facebook_link.isEmpty()) {
                        social_media_weight++;
                        facebook_im.setVisibility(View.VISIBLE);
                    }
                    if (x_twitter_link != null && !x_twitter_link.isEmpty()) {
                        social_media_weight++;
                        x_im.setVisibility(View.VISIBLE);
                    }
                    if (instagram_link != null && !instagram_link.isEmpty()) {
                        social_media_weight++;
                        instagram_im.setVisibility(View.VISIBLE);
                    }
                    if(social_media_weight > 0)
                        social_m_legend.setVisibility(View.VISIBLE);


                } else {
                    Log.d("Firestore", "No se han podido cargar los datos de contacto");
                }
            } else {
                Log.d("Firestore", "Error al obtener los datos de contacto", task.getException());
            }
        });
    }

}