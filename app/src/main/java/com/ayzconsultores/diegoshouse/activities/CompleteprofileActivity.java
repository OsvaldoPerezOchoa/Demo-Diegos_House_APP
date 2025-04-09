package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.models.User;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class CompleteprofileActivity extends AppCompatActivity {

    // Declaración de variables para los campos de entrada y botones
    TextInputEditText mCorreo, mPhone, mNombre;
    Button mRegister_btn;
    AuthProvider mAuthProvider; // Instancia de FirebaseAuth para autenticar usuarios
    UsersProvider mUsersProvider;// Instancia de FirebaseFirestore para almacenar datos del usuario
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completeprofile);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa los elementos del layout
        mCorreo = findViewById(R.id.complete_email);
        mPhone = findViewById(R.id.complete_phone);
        mNombre = findViewById(R.id.complete_name);
        mRegister_btn = findViewById(R.id.singup_confirm);
        mAuthProvider = new AuthProvider(); // Obtiene una instancia de FirebaseAuth
        mUsersProvider = new UsersProvider(); // Obtiene una instancia de Firestore
        String email = mAuthProvider.getEmail();

        mCorreo.setText(email);
        mCorreo.setEnabled(false);

        mRegister_btn.setOnClickListener(view -> {
            verificarCampos(); // Verifica los campos antes de registrar el usuario
        });

    }

    private void verificarCampos() {
        String username = Objects.requireNonNull(mNombre.getText()).toString().trim();
        String phone = Objects.requireNonNull(mPhone.getText()).toString();

        // Valida cada campo y si son correctos procede a registrar el usuario
        if(!validarNombre(username) || !validarPhone(phone) ){
            return;
        }
        updateUser(phone, username);
    }

    private boolean validarPhone(String phone) {
        if(phone.isEmpty()){
            mostrarMensaje("Por favor escriba su número de teléfono");
            return false;
        }
        if(!Patterns.PHONE.matcher(phone).matches()){
            mostrarMensaje("Por favor escriba un teléfono válido");
            return false;
        }
        return true;
    }

    private boolean validarNombre(String username) {
        if(username.isEmpty()){
            mostrarMensaje("Por favor escriba su nombre");
            return false;
        }
        return true;
    }

    private void updateUser(String phone, String nombre) {
        String userId = mAuthProvider.getUid();
        User user = new User();
        user.setNombre(nombre);
        user.setTelefono(phone);
        user.setId(userId);

        mUsersProvider.updateUser(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(CompleteprofileActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                mostrarMensaje("Error al guardar los datos del usuario");
            }
        });
    }


    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}