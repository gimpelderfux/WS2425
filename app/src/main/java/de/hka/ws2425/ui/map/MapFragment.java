package de.hka.ws2425.ui.map;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.hka.ws2425.R;

import androidx.fragment.app.FragmentResultListener;
import de.hka.ws2425.utils.GtfsData;
import de.hka.ws2425.utils.Stops;

public class MapFragment extends Fragment {

    private GeoPoint currentLocation;

    private MapViewModel mViewModel;
    private MapView mapView;

    private List<Stops> stopsList = new ArrayList<>();
    private GtfsData gtfsData; // Enthält alle GTFS-Daten

    private String lastClickedStopId = null; // Speichert die zuletzt angeklickte Stop-ID
    private long lastClickTime = 0; // Zeit des letzten Klicks

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLocationListener();
        mViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        // Empfange die Haltestellen- und GTFS-Daten
        getParentFragmentManager().setFragmentResultListener("stopsData", this, (requestKey, result) -> {
            stopsList = (List<Stops>) result.getSerializable("stopsList");
            if (stopsList != null && mapView != null) {
                Log.d("MapFragment", "Anzahl Haltestellen erhalten: " + stopsList.size());
                MapUtils.addStopsToMap(mapView, stopsList, this::handleMarkerClick);
            }
        });

        getParentFragmentManager().setFragmentResultListener("gtfsData", this, (requestKey, result) -> {
            gtfsData = (GtfsData) result.getSerializable("gtfsData");
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = root.findViewById(R.id.mapView);

        XYTileSource mapServer = new XYTileSource("MapServer",
                8,
                20,
                256,
                ".png",
                new String[]{"https://tileserver.svprod01.app/styles/default/"}
        );

        String authorizationString = getMapServerAuthorizationString(
                "ws2223@hka",
                "LeevwBfDi#2027"
        );

        Configuration.getInstance().getAdditionalHttpRequestProperties().put("Authorization", authorizationString);
        mapView.setTileSource(mapServer);

        GeoPoint startPoint = new GeoPoint(48.910059, 8.728463);
        IMapController mapController = mapView.getController();
        mapController.setZoom(11.0);
        mapController.setCenter(startPoint);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Prüfe, ob die Daten bereits geladen wurden und füge die Marker hinzu
        if (!stopsList.isEmpty() && mapView != null) {
            Log.d("MapFragment", "Marker werden beim erneuten Laden hinzugefügt.");
            MapUtils.addStopsToMap(mapView, stopsList, this::handleMarkerClick);
        }
    }

    @SuppressLint("MissingPermission")
    private void setupLocationListener() {
        LocationListener locationListener = location -> {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            currentLocation = new GeoPoint(latitude, longitude);
            Log.d("MapFragment", "Aktuelle Position: " + currentLocation.toDoubleString());
        };

        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
    }

    private String getMapServerAuthorizationString(String username, String password) {
        String authorizationString = String.format("%s:%s", username, password);
        return "Basic " + Base64.encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    private void handleMarkerClick(Marker marker, Stops stop) {
        long currentTime = System.currentTimeMillis();

        if (lastClickedStopId != null && lastClickedStopId.equals(stop.getId()) && (currentTime - lastClickTime) < 1000) {
            // Zweiter Klick erkannt: Zeige Abfahrtsliste
            List<String> departures = MapUtils.getDeparturesForStop(gtfsData, stop.getId());
            if (!departures.isEmpty()) {
                // Navigation zu DepartureListFragment
                DepartureListFragment fragment = DepartureListFragment.newInstance(stop.getName(), departures, gtfsData, getCurrentLocation());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Keine Abfahrten gefunden.", Toast.LENGTH_LONG).show();
            }
        } else {
            // Erster Klick: Zeige Stop-Name
            marker.showInfoWindow();
            lastClickedStopId = stop.getId();
            lastClickTime = currentTime;
        }
    }

    public GeoPoint getCurrentLocation() {
        return currentLocation;
    }
}
