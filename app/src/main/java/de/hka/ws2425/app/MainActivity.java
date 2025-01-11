package de.hka.ws2425.app;

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

import de.hka.ws2425.R;
import de.hka.ws2425.ui.main.MainFragment;
import de.hka.ws2425.ui.map.MapFragment;

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

        // Lade GTFS-Datei in den internen Speicher und lese sie anschließend aus
        loadAndReadGtfsData();

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

    private void loadAndReadGtfsData() {
        String assetFileName = "gtfs-hka-s24.zip";
        File destinationFile = new File(this.getApplication().getFilesDir(), assetFileName);

        if (!destinationFile.exists()) {
            boolean success = copyAssetToInternalStorage(assetFileName, destinationFile);
            if (!success) {
                Log.e("MainActivity", "Fehler beim Kopieren der GTFS-Datei.");
                return;
            }
        }

        readGtfsData(destinationFile);
    }

    private boolean copyAssetToInternalStorage(String assetFileName, File destinationFile) {
        try (InputStream in = getAssets().open(assetFileName)) {
            try (OutputStream out = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                Log.d("MainActivity", "GTFS-Datei erfolgreich in den internen Speicher kopiert.");
                return true;
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Fehler beim Kopieren der Datei: " + e.getMessage());
            return false;
        }
    }

    private void readGtfsData(File gtfsFile) {
        try {
            GtfsSimpleDao gtfsSimpleDao = new GtfsSimpleDao();
            GtfsReader gtfsReader = new GtfsReader();
            gtfsReader.setDataAccessObject(gtfsSimpleDao);
            gtfsReader.read(gtfsFile.getAbsolutePath());

            gtfsSimpleDao.getAgencies().forEach(agency ->
                    Log.d("GTFS", "Agentur: " + agency.getName())
            );
        } catch (Exception e) {
            Log.e("MainActivity", "Fehler beim Lesen der GTFS-Datei: " + e.getMessage());
        }
    }
}