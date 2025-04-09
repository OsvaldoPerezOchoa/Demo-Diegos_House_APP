package com.ayzconsultores.diegoshouse.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.models.User;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.UsersProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;


public class LoginTabFragment extends Fragment {

    TextInputEditText mCorreo, mPassword;
    Button mLogin_btn;
    ImageView mGmail_btn;
    TextView mRecuperar_btn;
    AuthProvider mAuth;
    LoadingDialog loadingDialog;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    UsersProvider mUsers;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_login_tab, container, false);

        // Inicialización de las vistas
        mCorreo = root.findViewById(R.id.login_correo);
        mPassword = root.findViewById(R.id.login_password);
        mLogin_btn = root.findViewById(R.id.login_btn);
        mGmail_btn = root.findViewById(R.id.login_gmail);
        mRecuperar_btn = root.findViewById(R.id.recuperar_btn);
        mUsers = new UsersProvider();
        mAuth    = new AuthProvider();
        loadingDialog = new LoadingDialog(getActivity());

        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mLogin_btn.setOnClickListener(view -> verificarCampos());

        // Acción del botón Recuperar contraseña
        mRecuperar_btn.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), RecuperarActivity.class);
            startActivity(intent);
        });

        // Acción del botón Google Sign-In
        mGmail_btn.setOnClickListener(view -> signIn());

        return root;
    }

    // Método para gestionar el resultado del inicio de sesión con Google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // Obtener la cuenta de Google seleccionada
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    // Autenticar en Firebase con la cuenta de Google
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Log.w(TAG, "GoogleSignInAccount es null");
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed: " + e.getStatusCode());
                mostrarMensaje("Error en el inicio de sesión con Google. Intenta nuevamente.");
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // Método para iniciar el flujo de inicio de sesión con Google
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



    // Método para autenticar en Firebase usando el token de Google
    private void firebaseAuthWithGoogle(String idToken) {
        loadingDialog.showLoadingDialog();
        mAuth.googleLogin(idToken).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicia sesión correctamente, verifica el perfil del usuario
                            String id = mAuth.getUid();
                            verificarUsuario(id);  // Verifica si el perfil está completo y redirige a la actividad correcta
                        } else {
                            // Fallo en la autenticación
                            loadingDialog.ocultarDialog();
                            mostrarMensaje("Error al iniciar sesión con Google");
                            updateUI(null);
                        }
                    }
                });
    }

    private void verificarUsuario(String id) {
        mUsers.getUser(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                loadingDialog.ocultarDialog();
                if (documentSnapshot.exists()) {
                    // Verificar si el campo 'nombre' existe en el documento
                    String nombre = documentSnapshot.getString("nombre");
                    if (nombre != null) {
                        // Si el campo 'nombre' existe, redirigir a HomeActivity
                        Intent intent = new Intent(getContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    } else {
                        // Si el campo 'nombre' no existe, redirigir a CompleteprofileActivity
                        Intent intent = new Intent(getContext(), CompleteprofileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } else {
                    // Si el documento no existe, crear el perfil y redirigir a CompleteprofileActivity
                    String email = mAuth.getEmail();
                    User user = new User();
                    user.setEmail(email);
                    user.setId(id);
                    mUsers.createUser(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getContext(), CompleteprofileActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                mostrarMensaje("Error al crear el usuario");
                            }
                        }
                    });
                }
            }
        });
    }




    // Verificar si los campos de correo y contraseña están llenos
    private void verificarCampos() {
        String correo = mCorreo.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (correo.isEmpty()) {
            mostrarMensaje("Ingrese su correo electrónico");
            return;
        }

        if (password.isEmpty()) {
            mostrarMensaje("Ingrese su contraseña");
            return;
        }
        // Si los campos están correctos, iniciar sesión con correo y contraseña
        LoginUser(correo, password);
    }

    // Método para iniciar sesión con correo y contraseña
    private void LoginUser(String correo, String password) {
        loadingDialog.showLoadingDialog();
        mAuth.login(correo, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Inicio de sesión exitoso, ir a la actividad principal
                if (task.isSuccessful()) {
                    irHome();
                } else {
                    // Fallo en el inicio de sesión
                    loadingDialog.ocultarDialog();
                    mostrarMensaje("Correo o contraseña son incorrectos");
                }
            }
        });
    }
    // Mostrar un mensaje Toast
    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();
    }


    private void updateUI(FirebaseUser user) {
        user = mAuth.getCurrentUser();
        if(user != null){
            irHome();
        }
    }


    private void irHome() {
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
