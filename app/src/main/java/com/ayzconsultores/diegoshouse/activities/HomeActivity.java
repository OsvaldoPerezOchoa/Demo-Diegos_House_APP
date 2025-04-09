package com.ayzconsultores.diegoshouse.activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.fragments.CarritoFragment;
import com.ayzconsultores.diegoshouse.fragments.HomeFragment;
import com.ayzconsultores.diegoshouse.fragments.MapaFragment;
import com.ayzconsultores.diegoshouse.fragments.PedidosFragment;
import com.ayzconsultores.diegoshouse.fragments.PerfilFragment;
import com.ayzconsultores.diegoshouse.providers.AuthProvider;
import com.ayzconsultores.diegoshouse.providers.TokenProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nafis.bottomnavigation.NafisBottomNavigation;


public class HomeActivity extends AppCompatActivity {


    private GoogleSignInClient mGoogleSignInClient;
    private final static int Home = 1;
    private final static int Perfil = 2;
    private final static int Pedidos = 3;
    private final static int Map = 4;
    private final static int Carrito = 5;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;
    AuthProvider mAuth;
    FirebaseUser user;
    TokenProvider mTokenProvider;

    // ActivityResultLauncher para solicitar permisos
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permiso concedido
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
                } else {
                    // Permiso denegado
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = new AuthProvider();
        user = mAuth.getCurrentUser();
        // Verifica si el usuario está autenticado
        if (user == null) {
            irLogin();
            return;
        }


        db = FirebaseFirestore.getInstance(); // Inicializa Firestore
        setupUserDeletionListener(); // Configura el listener
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);


        // Configuración para el color de la barra de navegación
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brown_dark));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NafisBottomNavigation mMeowBottomNavigation = findViewById(R.id.meowbottomnavigation);
        mTokenProvider = new TokenProvider();

        registrarDispositivo();
        verificarPermisos();
        verificarUsuario();

        //-------------Servicios Google----------------
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //Configuracion para el uso de inicio de sesion con google

        mMeowBottomNavigation.add(new NafisBottomNavigation.Model(Perfil, R.drawable.ic_person));
        mMeowBottomNavigation.add(new NafisBottomNavigation.Model(Pedidos, R.drawable.ic_receipt));
        mMeowBottomNavigation.add(new NafisBottomNavigation.Model(Home, R.drawable.ic_home));
        mMeowBottomNavigation.add(new NafisBottomNavigation.Model(Carrito, R.drawable.ic_shopping_cart));
        mMeowBottomNavigation.add(new NafisBottomNavigation.Model(Map, R.drawable.ic_location_pin));

        mMeowBottomNavigation.show(Home, true);

        mMeowBottomNavigation.setOnShowListener(model -> {

            Fragment fragment = null;

            if (model.getId() == Home) {
                fragment = new HomeFragment();

            } else if (model.getId() == Pedidos) {
                fragment = new PedidosFragment();

            } else if (model.getId() == Map) {
                fragment = new MapaFragment();

            } else if (model.getId() == Perfil) {
                fragment = new PerfilFragment();

            } else if (model.getId() == Carrito) {
                fragment = new CarritoFragment();

            }


            // Eliminar el fragmento actual antes de cargar uno nuevo
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flFragment);
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
            }

            // Cargar el nuevo fragmento
            CargarFragment(fragment);
            return null;
        });
    }

    private void verificarUsuario() {
        String userId = user.getUid();
        db.collection("Usuarios").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String nombreUsuario = document.getString("nombre"); // Cambia "nombre" por el campo que almacena el nombre del usuario en Firestore
                            if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                                // Si el nombre del usuario es null o vacío, mostrar un mensaje y cerrar sesión
                                mostrarMensaje("El nombre del usuario no está configurado. Cerrando sesión...");
                                logout(); // Cierra la sesión
                            }
                        } else {
                            // Si el documento no existe, mostrar un mensaje y cerrar sesión
                            mostrarMensaje("El usuario no existe en la base de datos. Cerrando sesión...");
                            logout(); // Cierra la sesión
                        }
                    } else {
                        // Si hay un error al obtener el documento, mostrar un mensaje y cerrar sesión
                        mostrarMensaje("Error al verificar el usuario. Cerrando sesión...");
                        logout(); // Cierra la sesión
                    }
                });
    }

    private void verificarPermisos() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupUserDeletionListener() {
        String userId = user.getUid();
        userListener = db.collection("Usuarios").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al verificar el usuario", error);
                        return;
                    }
                    if (documentSnapshot == null || !documentSnapshot.exists()) {
                        mostrarMensaje("Tu cuenta ha sido eliminada");
                        irLogin(); // Redirige al login
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove(); // Elimina el listener para evitar fugas de memoria
        }
    }

    private void registrarDispositivo() {
        mTokenProvider.createToken(mAuth.getUid());
    }

    private void CargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment) // Usar el fragmento correcto
                .commit();
    }

    private void verifyUser() {
        user.reload();
        if (!user.isEmailVerified()) {
            // Ubicación desactivada, mostrar un diálogo para permitir al usuario activarla
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Para continuar con la aplicación es necesario verificar tu correo.\n\nPor favor, revisa tu correo incluso tu spam.")
                    .setCancelable(false)
                    .setNegativeButton("Enviar correo", (dialog, id) -> {
                        user.sendEmailVerification();
                        dialog.cancel();

                        AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(HomeActivity.this);
                        confirmationBuilder.setMessage("Busca en tu correo electrónico el mensaje de verificación, da clic al enlace y vuelve a iniciar sesión.")
                                .setCancelable(false)
                                .setPositiveButton("Continuar", (dialog1, id1) -> logout());
                        confirmationBuilder.create().show();
                    })
                    .setPositiveButton("Aceptar", (dialog, id) -> logout());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // --> Método para cerrar sesión
    private void logout() {
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                irLogin();
            } else {
                mostrarMensaje("No se logro cerrar sesion");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = mAuth.getCurrentUser();
        if (user == null) {
            irLogin();
        } else {
            verifyUser();
            checkAndSubscribeToTopic();

            // Forzar la generación de un nuevo token
            mTokenProvider.createToken(user.getUid());
        }
    }

    private void checkAndSubscribeToTopic() {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (preferences.getBoolean("isSubscribed", false)) return;

        FirebaseMessaging.getInstance().subscribeToTopic("productos")
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Suscripción exitosa" : "Suscripción fallida";
                    Log.d("FCM", msg);
                    preferences.edit().putBoolean("isSubscribed", task.isSuccessful()).apply();
                });
    }

    private void irLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Evita que el usuario regrese a HomeActivity
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(HomeActivity.this, mensaje, Toast.LENGTH_SHORT).show();
    }
}