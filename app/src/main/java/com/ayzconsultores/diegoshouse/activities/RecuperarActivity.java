package com.ayzconsultores.diegoshouse.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarActivity extends AppCompatActivity {

    TextInputEditText mrecuperar_email;
    Button mrecuperar_btn;
    AuthProvider mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.beige_light));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mrecuperar_email = findViewById(R.id.recuperar_email);
        mrecuperar_btn = findViewById(R.id.recuperar_btn);
        mAuth = new AuthProvider();


        mrecuperar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validarUsuario();
            }
        });
    }



    private void validarUsuario(){
        String email = mrecuperar_email.getText().toString().trim();
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mostrarAlerta("Correo invalido");
            return;
        }
        enviarPassword(email);
    }

    private void enviarPassword(String email){
        mAuth.correoRecuperacion(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mostrarAlerta("Revisa tu correo, por favor");
                        }
                        else{
                            mostrarAlerta("correo invalido");
                        }
                    }
                });

    }

    private void mostrarAlerta(String mensajealert){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar Contraseña");
        builder.setMessage(mensajealert);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(RecuperarActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.show();
    }
}