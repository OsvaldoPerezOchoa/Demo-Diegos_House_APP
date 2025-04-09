package com.ayzconsultores.diegoshouse.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ayzconsultores.diegoshouse.activities.PagoActivity;
import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.activities.AvisoActivity;
import com.ayzconsultores.diegoshouse.activities.AyudaActivity;
import com.ayzconsultores.diegoshouse.activities.ContactoActivity;
import com.ayzconsultores.diegoshouse.activities.HistorialActivity;
import com.ayzconsultores.diegoshouse.activities.LoginActivity;
import com.ayzconsultores.diegoshouse.activities.PuntosActivity;
import com.ayzconsultores.diegoshouse.activities.TerminosActivity;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseUser;

public class PerfilFragment extends Fragment {

    // Proveedor de autenticación y datos del usuario
    AuthProvider mAuth;
    FirebaseUser user;

    // Botón de cerrar sesión
    Button btnCerrar;

    // TextViews para mostrar nombre y correo del usuario
    TextView txtNombre, txtCorreo;

    // Componentes para las opciones de navegación
    ConstraintLayout mHistorial, mAuyuda, mPrivacidad, mTerminos, mContacto, mPuntos;

    // Cliente de inicio de sesión de Google
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View root = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Inicializar los componentes de la interfaz
        inicializarComponentes(root);

        // Obtener los datos del usuario
        obtenerDatosUsuario();

        // Configurar los clics de las opciones del menú
        configurarMenuOpciones();

        // Configuración del inicio de sesión con Google
        configurarGoogleSignIn();

        // Acción del botón de cerrar sesión
        btnCerrar.setOnClickListener(view -> logout()); // Cerrar sesión

        return root;
    }

    // Inicializa los componentes de la interfaz de usuario
    private void inicializarComponentes(View root) {
        mAuth = new AuthProvider();
        user = mAuth.getCurrentUser();

        // Asociar los elementos de la interfaz con sus respectivos IDs
        btnCerrar = root.findViewById(R.id.cerrar_sesion);
        txtNombre = root.findViewById(R.id.txt_nombre);
        txtCorreo = root.findViewById(R.id.txt_correo);
        mHistorial = root.findViewById(R.id.layout_historial);
        mAuyuda = root.findViewById(R.id.layoutauyuda);
        mPrivacidad = root.findViewById(R.id.layout_privacidad);
        mTerminos = root.findViewById(R.id.layout_terminos);
        mContacto = root.findViewById(R.id.layout_contacto);
        mPuntos = root.findViewById(R.id.layout_puntos);
    }

    // Obtiene los datos del usuario actual desde Firebase
    private void obtenerDatosUsuario() {
        if (user != null) {
            String userId = user.getUid();

            // Crear instancia del proveedor de usuarios y obtener los datos
            UsersProvider usersProvider = new UsersProvider();
            usersProvider.getUser(userId)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Extraer nombre y correo del documento de Firebase
                            String nombre = documentSnapshot.getString("nombre");
                            String correo = documentSnapshot.getString("email");

                            // Asignar los datos extraídos a los TextViews
                            txtNombre.setText(nombre != null ? nombre : "Nombre no disponible");
                            txtCorreo.setText(correo != null ? correo : "Correo no disponible");
                        } else {
                            mostrarMensaje("No se encontraron datos del usuario.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        mostrarMensaje("Error al obtener datos del usuario.");
                        e.printStackTrace();
                    });
        } else {
            mostrarMensaje("Usuario no autenticado.");
        }
    }

    // Configura el menú de opciones para navegar a las actividades correspondientes
    private void configurarMenuOpciones() {
        // Navegar a HistorialActivity
        mHistorial.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), HistorialActivity.class);
            startActivity(intent);
        });

        // Navegar a AyudaActivity
        mAuyuda.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AyudaActivity.class);
            startActivity(intent);
        });

        // Navegar a PuntosActivity
        mPuntos.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PuntosActivity.class);
            startActivity(intent);
        });

        // Navegar a AvisoActivity (Aviso de privacidad)
        mPrivacidad.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AvisoActivity.class);
            startActivity(intent);
        });

        // Navegar a TerminosActivity
        mTerminos.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), TerminosActivity.class);
            startActivity(intent);
        });
        mContacto.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ContactoActivity.class);
            startActivity(intent);
        });
    }

    // Configuración de Google Sign-In para autenticación
    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    // Método para cerrar sesión
    private void logout() {
        // Cerrar sesión de Firebase
        mAuth.signOut();

        // Cerrar sesión de Google
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                irLogin(); // Redirigir al login si la sesión se cerró correctamente
            } else {
                mostrarMensaje("No se logró cerrar sesión");
            }
        });
    }

    // Redirige al LoginActivity
    private void irLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Muestra un mensaje de error o éxito en forma de Toast
    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}
