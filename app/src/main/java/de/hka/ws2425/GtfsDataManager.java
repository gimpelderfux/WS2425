package de.hka.ws2425;

import android.content.Context;
import android.util.Log;

import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsSimpleDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GtfsDataManager {
    private static final String GTFS_ASSET_FILE_NAME = "gtfs-hka-s24.zip";
    private static final String TAG = "GtfsDataManager"; // Tag for log messages
    private Context context;
    private boolean loadingSuccessful = false; // Flag to track loading success

    public GtfsDataManager(Context context) {
        this.context = context;
    }

    public void loadAndReadGtfsData() {
        Log.d(TAG, "loadAndReadGtfsData() gestartet");

        String assetFileName = GTFS_ASSET_FILE_NAME;
        File destinationFile = new File(context.getFilesDir(), assetFileName);

        Log.d(TAG, "Destination file path: " + destinationFile.getAbsolutePath());

        boolean success = false;
        if (!destinationFile.exists()) {
            Log.d(TAG, "Destination file does not exist. Copying from assets...");
            success = copyAssetToInternalStorage(assetFileName, destinationFile);
            if (!success) {
                Log.e(TAG, "Error copying GTFS file to internal storage.");
                return;
            }
        } else {
            Log.d(TAG, "Destination file already exists. Skipping copy.");
            success = true; // File already exists, consider it a success
        }

        if (success) {
            Log.d(TAG, "Starting to read GTFS data...");
            readGtfsData(destinationFile);
            loadingSuccessful = true; // Set to true if loading and reading were successful
        } else {
            loadingSuccessful = false; // Set to false if there was an error
        }

        Log.d(TAG, "loadAndReadGtfsData() completed. loadingSuccessful: " + loadingSuccessful);
    }

    private boolean copyAssetToInternalStorage(String assetFileName, File destinationFile) {
        Log.d(TAG, "copyAssetToInternalStorage() started");

        try (InputStream in = context.getAssets().open(assetFileName);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            Log.d(TAG, "GTFS file copied to internal storage successfully.");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file: " + e.getMessage(), e);
            return false;
        } finally {
            Log.d(TAG, "copyAssetToInternalStorage() completed");
        }
    }

    private void readGtfsData(File gtfsFile) {
        Log.d(TAG, "readGtfsData() started");

        GtfsSimpleDao gtfsSimpleDao = null;
        try {
            gtfsSimpleDao = new GtfsSimpleDao();
            GtfsReader gtfsReader = new GtfsReader();
            gtfsReader.setDataAccessObject(gtfsSimpleDao);
            gtfsReader.read(gtfsFile.getAbsolutePath());

            // Example: Accessing and logging agency names
            if (gtfsSimpleDao != null) {
                gtfsSimpleDao.getAgencies().forEach(agency ->
                        Log.d(TAG, "Agency: " + agency.getName())
                );
                loadingSuccessful = true; // Set loadingSuccessful to true here
            } else {
                Log.e(TAG, "gtfsSimpleDao is null!");
                // Handle the error appropriately
                loadingSuccessful = false; // Set loadingSuccessful to false in case of error
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading GTFS file: " + e.getMessage(), e);
            // Error handling
            loadingSuccessful = false; // Set loadingSuccessful to false in case of error
        } finally {
            Log.d(TAG, "readGtfsData() completed");
        }
    }

    public boolean wasLoadingSuccessful() {
        return loadingSuccessful;
    }
}