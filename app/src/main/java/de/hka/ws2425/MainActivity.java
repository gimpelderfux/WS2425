package de.hka.ws2425;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsSimpleDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.hka.ws2425.ui.main.MainFragment;
import de.hka.ws2425.ui.map.MapFragment;
import de.hka.ws2425.utils.FileUtils;
import de.hka.ws2425.utils.GtfsData;

public class MainActivity extends AppCompatActivity {

    private Button btnGoToMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }

        // Lade GTFS-Daten
        String assetFileName = "gtfs-hka-j25.zip";
        File destinationFile = new File(getApplication().getFilesDir(), assetFileName);

        if (!destinationFile.exists()) {
            boolean success = FileUtils.copyAssetToInternalStorage(getAssets(), assetFileName, destinationFile);
            if (!success) {
                Log.e("MainActivity", "Fehler beim Kopieren der GTFS-Datei.");
                return;
            }
        }

        // GTFS-Daten laden
        GtfsData gtfsData = FileUtils.loadGtfsData(destinationFile);

        // Übergabe der Stop-Daten an die Map-Ansicht
        Bundle stopsDataBundle = new Bundle();
        stopsDataBundle.putSerializable("stopsList", (ArrayList<?>) gtfsData.getStops());
        getSupportFragmentManager().setFragmentResult("stopsData", stopsDataBundle);

        // Übergabe der gesamten GTFS-Daten
        Bundle gtfsDataBundle = new Bundle();
        gtfsDataBundle.putSerializable("gtfsData", gtfsData);
        getSupportFragmentManager().setFragmentResult("gtfsData", gtfsDataBundle);

        Log.d("MainActivity", "GTFS-Daten erfolgreich geladen und übergeben.");

        // Füge den Navigations-Button hinzu
        setupNavigationButton();

        // Füge den Listener für den Fragment-Wechsel hinzu
        setupFragmentChangeListener();
    }

    private void setupNavigationButton() {
        btnGoToMap = findViewById(R.id.btn_go_to_map);
        if (btnGoToMap != null) {
            btnGoToMap.setOnClickListener(v -> {
                // Ersetze das aktuelle Fragment durch das MapFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new MapFragment())
                        .addToBackStack(null) // Füge zur Backstack hinzu, um zurück navigieren zu können
                        .commit();
            });
        } else {
            Log.e("MainActivity", "Button btn_go_to_map nicht gefunden!");
        }
    }

    private void setupFragmentChangeListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (currentFragment instanceof MapFragment) {
                hideMapButton();
            } else {
                showMapButton();
            }
        });
    }

    private void hideMapButton() {
        if (btnGoToMap != null) {
            btnGoToMap.setVisibility(View.GONE);
        }
    }

    private void showMapButton() {
        if (btnGoToMap != null) {
            btnGoToMap.setVisibility(View.VISIBLE);
        }
    }
}
