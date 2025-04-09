package com.ayzconsultores.diegoshouse.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

// Clase que gestiona la autenticación de usuarios usando Firebase
public class AuthProvider {

    // Instancia de FirebaseAuth que gestiona la autenticación
    private final FirebaseAuth mAuth;

    // Constructor que inicializa la instancia de FirebaseAuth
    public AuthProvider(){
        mAuth = FirebaseAuth.getInstance();
    }

    // Método que permite el inicio de sesión con correo y contraseña
    // Toma como parámetros el correo y la contraseña, y devuelve un Task de tipo AuthResult
    public Task<AuthResult> login(String correo, String password){
        return mAuth.signInWithEmailAndPassword(correo, password);
    }

    // Método que permite el inicio de sesión con Google
    // Toma como parámetro el idToken proporcionado por Google y devuelve un Task de tipo AuthResult
    public Task<AuthResult> googleLogin(String idToken){
        // Se obtiene un AuthCredential con el idToken
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        // Inicia sesión con las credenciales obtenidas
        return mAuth.signInWithCredential(credential);
    }

    // Método que obtiene el correo electrónico del usuario actual autenticado
    public String getEmail() {
        // Si hay un usuario autenticado, devuelve su email, si no, retorna null
        if(mAuth.getCurrentUser()!= null){
            return mAuth.getCurrentUser().getEmail();
        }else{
            return null;
        }
    }

    // Método que obtiene el UID (Identificador único de usuario) del usuario actual
    public String getUid() {
        // Si hay un usuario autenticado, devuelve su UID, si no, retorna null
        if(mAuth.getCurrentUser()!= null){
            return mAuth.getCurrentUser().getUid();
        }else{
            return null;
        }
    }

    // Método para registrar un nuevo usuario con correo y contraseña
    // Devuelve un Task de tipo AuthResult
    public Task<AuthResult> CrearUsuario(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    // Método para enviar un correo de verificación al usuario autenticado actual
    // Devuelve un Task de tipo Void
    public Task<Void> enviarCorreo(){
        return Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification();
    }

    // Método para enviar un correo de recuperación de contraseña
    public Task<Void> correoRecuperacion(String email){
        return mAuth.sendPasswordResetEmail(email);
    }

    // Método que obtiene el usuario actual autenticado
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Método para cerrar sesión
    public void signOut() {
        mAuth.signOut();
    }

}