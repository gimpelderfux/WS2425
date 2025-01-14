package de.hka.ws2425.ui.map;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hka.ws2425.R;
import de.hka.ws2425.utils.GtfsData;

public class DepartureListFragment extends Fragment {

    private static final String ARG_STOP_NAME = "stopName";
    private static final String ARG_DISPLAY_DEPARTURES = "displayDepartures";
    private static final String ARG_TRIP_ID_MAPPING = "tripIdMapping";
    private static final String ARG_GTFS_DATA = "gtfsData";
    private static final String ARG_CURRENT_LOCATION = "currentLocation";

    private String stopName;
    private List<String> displayDepartures; // Nur die Anzeige
    private Map<String, String> tripIdMapping; // Zuordnung Anzeige -> TripId
    private GtfsData gtfsData;
    private GeoPoint currentLocation;

    public static DepartureListFragment newInstance(String stopName, List<String> displayDepartures, Map<String, String> tripIdMapping, GtfsData gtfsData, GeoPoint currentLocation) {
        DepartureListFragment fragment = new DepartureListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOP_NAME, stopName);
        args.putStringArrayList(ARG_DISPLAY_DEPARTURES, new ArrayList<>(displayDepartures));
        args.putSerializable(ARG_TRIP_ID_MAPPING, (HashMap<String, String>) tripIdMapping);
        args.putSerializable(ARG_GTFS_DATA, gtfsData);
        args.putParcelable(ARG_CURRENT_LOCATION, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stopName = getArguments().getString(ARG_STOP_NAME);
            displayDepartures = getArguments().getStringArrayList(ARG_DISPLAY_DEPARTURES);
            tripIdMapping = (Map<String, String>) getArguments().getSerializable(ARG_TRIP_ID_MAPPING);
            gtfsData = (GtfsData) getArguments().getSerializable(ARG_GTFS_DATA);
            currentLocation = getArguments().getParcelable(ARG_CURRENT_LOCATION);
            Log.d("DepartureListFragment", "GTFS-Daten und Position erfolgreich erhalten.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_departure_list, container, false);

        TextView stopNameTextView = view.findViewById(R.id.stop_name_textview);
        ListView departuresListView = view.findViewById(R.id.departures_listview);

        // Setze den Namen der Haltestelle
        stopNameTextView.setText(stopName);

        // Zeige die Abfahrten in der Liste
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayDepartures);
        departuresListView.setAdapter(adapter);

        // Listener für Klick auf eine Abfahrt
        departuresListView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedDeparture = displayDepartures.get(position);
            Log.d("DepartureListFragment", "Gewählte Abfahrt: " + selectedDeparture);

            // Ermittle die zugehörige TripId
            String tripId = tripIdMapping.get(selectedDeparture);
            if (tripId == null) {
                Log.e("DepartureListFragment", "Keine TripId gefunden für: " + selectedDeparture);
                return;
            }

            // Navigation zu TripDetailsFragment mit Übergabe der aktuellen Position
            TripDetailsFragment fragment = TripDetailsFragment.newInstance(tripId, gtfsData, currentLocation);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}

