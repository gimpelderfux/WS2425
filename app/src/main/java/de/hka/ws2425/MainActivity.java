package de.hka.ws2425;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsDaoBase;
import org.gtfs.reader.GtfsSimpleDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.hka.ws2425.ui.main.MainFragment; // Sicherstellen, dass dies korrekt importiert ist

public class MainActivity extends AppCompatActivity {

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
    }

    private void loadAndReadGtfsData() {
        String assetFileName = "gtfs-hka-s24.zip";  // Name der Datei in den Assets
        File destinationFile = new File(this.getApplication().getFilesDir(), assetFileName);

        // Kopiere die Datei aus den Assets in den internen Speicher
        if (!destinationFile.exists()) {
            boolean success = copyAssetToInternalStorage(assetFileName, destinationFile);
            if (!success) {
                Log.e("MainActivity", "Fehler beim Kopieren der GTFS-Datei.");
                return;
            }
        }

        // Lese die GTFS-Datei mit der Bibliothek
        readGtfsData(destinationFile);
    }

    private boolean copyAssetToInternalStorage(String assetFileName, File destinationFile) {
        try (InputStream in = getAssets().open(assetFileName);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            Log.d("MainActivity", "GTFS-Datei erfolgreich in den internen Speicher kopiert.");
            return true;
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

            // Beispiel: Agenturen aus der GTFS-Datei auslesen und loggen
            gtfsSimpleDao.getAgencies().forEach(agency ->
                    Log.d("GTFS", "Agentur: " + agency.getName())
            );
        } catch (Exception e) {
            Log.e("MainActivity", "Fehler beim Lesen der GTFS-Datei: " + e.getMessage());
        }
    }
}
