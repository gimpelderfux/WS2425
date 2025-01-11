package de.hka.ws2425;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hka.ws2425.ui.main.MainFragment;
import de.hka.ws2425.utils.DelayCalculator;
import de.hka.ws2425.utils.FileUtils;
import de.hka.ws2425.utils.GtfsData;
import de.hka.ws2425.utils.LocationHelper;
import de.hka.ws2425.utils.Stops;
import de.hka.ws2425.utils.TimetableEntry;
import de.hka.ws2425.utils.StopTimes;

public class MainActivity extends AppCompatActivity implements LocationHelper.LocationUpdateListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private LocationHelper locationHelper;
    private List<Stops> allStops;
    private List<TimetableEntry> allTimetableEntries;
    private GtfsData gtfsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }

        // Load GTFS data
        String assetFileName = "gtfs-hka-s24.zip";
        File destinationFile = new File(getApplication().getFilesDir(), assetFileName);

        if (!destinationFile.exists()) {
            boolean success = FileUtils.copyAssetToInternalStorage(getAssets(), assetFileName, destinationFile);
            if (!success) {
                Log.e("MainActivity", "Error copying GTFS file.");
                return;
            }
        }

        // Load GTFS data
        gtfsData = FileUtils.loadGtfsData(destinationFile);
        allStops = gtfsData.getStops();
        allTimetableEntries = convertStopTimesToTimetableEntries(gtfsData.getStopTimes());

        // Pass stop data to the map view
        Bundle stopsDataBundle = new Bundle();
        stopsDataBundle.putSerializable("stopsList", (ArrayList<?>) gtfsData.getStops());
        getSupportFragmentManager().setFragmentResult("stopsData", stopsDataBundle);

        // Pass all GTFS data
        Bundle gtfsDataBundle = new Bundle();
        gtfsDataBundle.putSerializable("gtfsData", gtfsData);
        getSupportFragmentManager().setFragmentResult("gtfsData", gtfsDataBundle);

        Log.d("MainActivity", "GTFS data loaded and passed successfully.");

        // Initialize LocationHelper
        locationHelper = new LocationHelper(this, this);

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationHelper.startLocationUpdates();
        }
    }

    @Override
    public void onLocationUpdate(Location location) {
        Log.d("MainActivity", "Location Update: " + location.getLatitude() + ", " + location.getLongitude());

        // Find the closest stop
        Stops currentStop = DelayCalculator.findClosestStop(location, allStops);

        if (currentStop != null) {
            Log.d("MainActivity", "Closest Stop: " + currentStop.getName());

            // Find the current timetable entry (replace with your logic to find the correct entry)
            TimetableEntry currentTimetableEntry = findCurrentTimetableEntry(currentStop.getId(), "yourBusId"); // Replace "yourBusId" with the actual bus ID

            if (currentTimetableEntry != null) {
                // Calculate the delay
                Date scheduledTime = DelayCalculator.convertToDate(currentTimetableEntry.getScheduledArrivalHour(), currentTimetableEntry.getScheduledArrivalMinute());
                Date actualTime = new Date(); // Current time
                long delayInMinutes = DelayCalculator.calculateDelay(scheduledTime, actualTime);

                Log.d("MainActivity", "Delay: " + delayInMinutes + " minutes");
                // ... Update UI with delay information ...
            } else {
                Log.d("MainActivity", "No timetable entry found for this stop and bus.");
            }
        } else {
            Log.d("MainActivity", "No stop found nearby.");
        }
    }

    // Helper method to find the current timetable entry (replace with your actual logic)
    private TimetableEntry findCurrentTimetableEntry(String stopId, String busId) {
        // Replace this with your actual logic to find the correct timetable entry
        for (TimetableEntry entry : allTimetableEntries) {
            if (entry.getStopId().equals(stopId) && entry.getBusId().equals(busId)) {
                return entry;
            }
        }
        return null;
    }

    // Helper method to convert StopTimes to TimetableEntry
    private List<TimetableEntry> convertStopTimesToTimetableEntries(List<StopTimes> stopTimes) {
        List<TimetableEntry> timetableEntries = new ArrayList<>();
        for (StopTimes stopTime : stopTimes) {
            String[] timeParts = stopTime.getArrivalTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            TimetableEntry entry = new TimetableEntry(stopTime.getTripId(), stopTime.getStopId(), hour, minute);
            timetableEntries.add(entry);
        }
        return timetableEntries;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHelper.stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.startLocationUpdates();
            }
        }
    }
}