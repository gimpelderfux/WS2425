package de.hka.ws2425.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    /**
     * Kopiert eine Datei aus dem assets-Ordner in den internen App-Speicher.
     *
     * @param context       Der Kontext der Anwendung.
     * @param assetFileName Der Name der Datei im assets-Ordner.
     * @return Ein File-Objekt, das die Datei im internen Speicher repräsentiert.
     */
    public static File copyAssetToInternalStorage(Context context, String assetFileName) {
        File outputFile = new File(context.getFilesDir(), assetFileName);
        if (outputFile.exists()) {
            return outputFile; // Datei existiert bereits
        }

        try (InputStream inputStream = context.getAssets().open(assetFileName);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputFile;
    }
}
