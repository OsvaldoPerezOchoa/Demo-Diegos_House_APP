package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.models.User;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SingupFragment extends Fragment {

    // Declaración de variables para los campos de entrada y el botón de registro
    TextInputEditText mCorreo, mPhone, mNombre, mPassword, mConfirmpassword;
    Button mRegister_btn;
    AuthProvider mAuthProvider; // Proveedor de autenticación (Firebase)
    UsersProvider mUsersProvider; // Proveedor para gestionar usuarios en Firestore
    LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla el layout del fragmento de registro
        View root = inflater.inflate(R.layout.fragment_singup, container, false);

        // Inicializa los campos de entrada y el botón de registro
        mCorreo = root.findViewById(R.id.singup_email);
        mPhone = root.findViewById(R.id.singup_phone);
        mNombre = root.findViewById(R.id.singup_name);
        mPassword = root.findViewById(R.id.singup_password);
        loadingDialog = new LoadingDialog(getActivity());
        mConfirmpassword = root.findViewById(R.id.singup_passwordconfir);
        mRegister_btn = root.findViewById(R.id.singup_btnsingup);

        // Inicializa los proveedores de autenticación y Firestore
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        // Establece el evento onClick del botón de registro
        mRegister_btn.setOnClickListener(view -> {
            verificarCampos(); // Verifica los campos de entrada antes de registrar el usuario
        });

        return root; // Retorna la vista inflada
    }

    /**
     * Verifica si todos los campos son válidos antes de registrar al usuario.
     */
    private void verificarCampos() {
        String nombre = Objects.requireNonNull(mNombre.getText()).toString().trim();
        String email = Objects.requireNonNull(mCorreo.getText()).toString().trim();
        String phone = Objects.requireNonNull(mPhone.getText()).toString().trim();
        String password = Objects.requireNonNull(mPassword.getText()).toString().trim();
        String passwordconfirm = Objects.requireNonNull(mConfirmpassword.getText()).toString().trim();

        // Verifica si cada campo es válido y si es correcto procede a registrar el usuario
        if (!validarNombre(nombre) || !validarEmail(email) || !validarPhone(phone) || !validarPassword(password, passwordconfirm)) {
            return; // Si algún campo es inválido, no continúa
        }
        registrarUsuario(email, password, phone, nombre); // Procede con el registro
    }

    /**
     * Valida si el nombre de usuario es válido.
     * @return True si es válido, false si no lo es.
     */
    private boolean validarNombre(String nombre) {
        if (nombre.isEmpty()) {
            mostrarMensaje("Por favor escriba su nombre");
            return false;
        }
        return true; // El nombre es válido si no está vacío
    }

    /**
     * Valida si el número de teléfono es válido.
     * @param phone Número de teléfono ingresado.
     * @return True si es válido, false si no lo es.
     */
    private boolean validarPhone(String phone) {
        if (phone.isEmpty()) {
            mostrarMensaje("Por favor escriba su número de teléfono");
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            mostrarMensaje("Por favor escriba un teléfono válido");
            return false;
        }
        return true; // El teléfono es válido si no está vacío y sigue el patrón adecuado
    }

    /**
     * Valida si el correo electrónico es válido.
     * @param email Correo electrónico ingresado.
     * @return True si es válido, false si no lo es.
     */
    private boolean validarEmail(String email) {
        if (email.isEmpty()) {
            mostrarMensaje("Por favor escriba su correo electrónico");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarMensaje("Por favor escriba un correo electrónico válido");
            return false;
        }
        return true; // El correo es válido si no está vacío y sigue el formato correcto
    }

    /**
     * Valida si las contraseñas ingresadas son válidas.
     * @param password Contraseña ingresada.
     * @param passwordconfirm Confirmación de la contraseña.
     * @return True si ambas contraseñas son válidas y coinciden, false si no.
     */
    private boolean validarPassword(String password, String passwordconfirm) {
        if (password.isEmpty()) {
            mostrarMensaje("Por favor escriba una contraseña");
            return false;
        }
        if (passwordconfirm.isEmpty()) {
            mostrarMensaje("Por favor confirme su contraseña");
            return false;
        }
        if (password.length() < 8) {
            mostrarMensaje("La contraseña debe tener al menos 8 caracteres.");
            return false;
        }
        if (!password.equals(passwordconfirm)) {
            mostrarMensaje("Las contraseñas no coinciden");
            return false;
        }
        if (!validarSeguridadPassword(password)) {
            return false;
        }
        return true; // Las contraseñas son válidas si coinciden y cumplen con los requisitos
    }

    /**
     * Valida si la contraseña cumple con los requisitos de seguridad.
     * @param password Contraseña ingresada.
     * @return True si la contraseña es válida, false si no lo es.
     */
    private boolean validarSeguridadPassword(String password) {
        // Verifica si la contraseña tiene al menos una letra minúscula
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        // Verifica si la contraseña tiene al menos una letra mayúscula
        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        // Verifica si la contraseña tiene al menos un número
        boolean tieneNumero = password.matches(".*\\d.*");
        // Verifica si la contraseña tiene al menos un carácter especial, excepto "/"
        boolean tieneCaracterEspecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?].*");

        // Verifica que la contraseña cumpla con todos los requisitos
        if (!tieneMinuscula || !tieneMayuscula || !tieneNumero || !tieneCaracterEspecial) {
            mostrarMensaje("La contraseña debe contener al menos una letra minúscula, una mayúscula, un número y un carácter especial");
            return false;
        }
        return true;
    }

    /**
     * Registra al usuario en Firebase Authentication con el correo y la contraseña proporcionados.
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @param nombre Nombre del usuario.
     * @param phone Número de teléfono del usuario.
     */
    private void registrarUsuario(String email, String password, String nombre, String phone) {
        // Mostrar el diálogo de carga
        loadingDialog.showLoadingDialog();

        // Registra al usuario en Firebase Authentication
        mAuthProvider.CrearUsuario(email, password).addOnCompleteListener(task -> {
            // Ocultar el diálogo de carga
            loadingDialog.ocultarDialog();

            if (task.isSuccessful()) {
                // Si el registro es exitoso, guarda los datos del usuario en Firestore
                guardarUsuario(email, nombre, phone, mAuthProvider.getUid());
            } else {
                mostrarMensaje("Error al registrar el usuario");
            }
        });
    }

    private void guardarUsuario(String email, String phone, String nombre, String userId) {
        // Crea un nuevo objeto de usuario
        User user = new User();
        user.setId(userId); // Establece el ID del usuario
        user.setEmail(email);
        user.setTelefono(phone);
        user.setNombre(nombre);

        // Guarda los datos del usuario en Firestore
        mUsersProvider.createUser(user).addOnCompleteListener(task -> {
            loadingDialog.ocultarDialog(); // Ocultar el diálogo al finalizar

            if (task.isSuccessful()) {
                // Verifica si el usuario está autenticado antes de enviar el correo de verificación
                FirebaseUser currentUser = mAuthProvider.getCurrentUser();
                if (currentUser != null) {
                    mAuthProvider.enviarCorreo().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            mostrarMensaje("Usuario registrado correctamente. Revisa tu bandeja de entrada.");
                            Intent intent = new Intent(getContext(), HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            mostrarMensaje("Error al enviar el correo de verificación. Intenta de nuevo.");
                        }
                    });
                } else {
                    mostrarMensaje("Error: El usuario no está autenticado. Intente registrarse de nuevo.");
                }
            } else {
                mostrarMensaje("Error al guardar los datos del usuario.");
            }
        });
    }

    /**
     * Muestra un mensaje Snackbar en la pantalla.
     * @param mensaje Mensaje a mostrar.
     */
    private void mostrarMensaje(String mensaje) {
        View rootView = getView(); // Obtén la vista raíz del fragmento
        if (rootView != null) {
            Snackbar snackbar = Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(getResources().getColor(R.color.beige_light)); // Color de fondo
            snackbar.setTextColor(getResources().getColor(R.color.brown_dark)); // Color del texto
            snackbar.show();
        }
    }
}