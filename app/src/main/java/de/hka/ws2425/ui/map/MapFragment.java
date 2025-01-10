/*
 * Icon Attributierung:
 * <a href="https://www.flaticon.com/free-icons/bus-stop" title="bus stop icons">Bus stop icons created by mavadee - Flaticon</a>
 */

package de.hka.ws2425.ui.map;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsDaoBase;
import org.gtfs.reader.GtfsSimpleDao;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.hka.ws2425.R;

import androidx.fragment.app.FragmentResultListener;
import de.hka.ws2425.utils.Stops;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {

    private MapViewModel mViewModel;

    private MapView mapView;

    private List<Stops> stopsList = new ArrayList<>();

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        // TODO: Use the ViewModel
    }


    // Ergänze die Methode `onCreateView`:
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        this.mapView = root.findViewById(R.id.mapView);

        XYTileSource mapServer = new XYTileSource("MapServer",
                8,
                20,
                256,
                ".png",
                new String[]{"https://tileserver.svprod01.app/styles/default/"}
        );

        String authorizationString = this.getMapServerAuthorizationString(
                "ws2223@hka",
                "LeevwBfDi#2027"
        );

        Configuration
                .getInstance()
                .getAdditionalHttpRequestProperties()
                .put("Authorization", authorizationString);

        this.mapView.setTileSource(mapServer);

        GeoPoint startPoint = new GeoPoint(48.99958, 8.80337);

        IMapController mapController = this.mapView.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(startPoint);

        // Empfange Haltestellen-Daten von MainActivity:
        getParentFragmentManager().setFragmentResultListener("stopsData", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                stopsList = (List<Stops>) result.getSerializable("stopsList");
                if (stopsList != null) {
                    addStopsToMap();
                }
            }
        });

        return root;
    }

    // Neue Methode hinzufügen:
    private void addStopsToMap() {
        for (Stops stop : stopsList) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(stop.getLatitude(), stop.getLongitude()));
            marker.setTitle(stop.getName());
            // Icon laden
            Drawable icon = getResources().getDrawable(R.drawable.ic_bus_stop, null);

            // Icon skalieren
            int iconWidth = 30; // Gewünschte Breite in Pixeln
            int iconHeight = 30; // Gewünschte Höhe in Pixeln
            Bitmap bitmap;
            if (icon instanceof BitmapDrawable) {
                // Wenn es ein BitmapDrawable ist, extrahieren wir das Bitmap
                bitmap = ((BitmapDrawable) icon).getBitmap();
            } else {
                // Wenn es kein BitmapDrawable ist, erstellen wir ein Bitmap daraus
                bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
            }

            Drawable scaledIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, true));

            // Icon setzen
            marker.setIcon(scaledIcon);

            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();  // Karte aktualisieren
    }


    @Override
    public void onResume() {
        super.onResume();

        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
        };

        Permissions.check(this.getContext(), permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                setupLocationListener();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void setupLocationListener()
    {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                GeoPoint startPoint = new GeoPoint(latitude, longitude);

                IMapController mapController = mapView.getController();
                mapController.setCenter(startPoint);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        LocationManager locationManager = (LocationManager) this.getContext().getSystemService(
                Context.LOCATION_SERVICE
        );

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                10,
                locationListener
        );
    }

    private String getMapServerAuthorizationString(String username, String password)
    {
        String authorizationString = String.format("%s:%s", username, password);
        return "Basic " + Base64.encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }
}

