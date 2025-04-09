package com.ayzconsultores.diegoshouse.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ayzconsultores.diegoshouse.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mapa, container, false);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configura el mapa para mejorar el rendimiento
        mMap.getUiSettings().setZoomControlsEnabled(true); // Habilita controles de zoom
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true); // Mejora el desplazamiento
        mMap.setMinZoomPreference(10); // Establece un zoom mínimo
        mMap.setMaxZoomPreference(18); // Establece un zoom máximo

        // Configura la ubicación inicial
        LatLng pizzeria = new LatLng(20.59123096999456, -100.36874499301007);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pizzeria, 15));

        // Agrega el marcador
        mMap.addMarker(new MarkerOptions()
                .position(pizzeria)
                .title("Pizzería Diego's House")
                .snippet("¡Ven y disfruta de las mejores pizzas!"));

        // Desactiva funciones innecesarias
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}
