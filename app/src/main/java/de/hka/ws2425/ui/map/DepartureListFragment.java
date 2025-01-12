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
import java.util.List;

import de.hka.ws2425.R;
import de.hka.ws2425.utils.GtfsData;

public class DepartureListFragment extends Fragment {

    private static final String ARG_STOP_NAME = "stopName";
    private static final String ARG_DEPARTURES = "departures";
    private static final String ARG_GTFS_DATA = "gtfsData";
    private static final String ARG_CURRENT_LOCATION = "currentLocation";

    private String stopName;
    private List<String> departures;
    private GtfsData gtfsData;
    private GeoPoint currentLocation;

    public static DepartureListFragment newInstance(String stopName, List<String> departures, GtfsData gtfsData, GeoPoint currentLocation) {
        DepartureListFragment fragment = new DepartureListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOP_NAME, stopName);
        args.putStringArrayList(ARG_DEPARTURES, new ArrayList<>(departures));
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
            departures = getArguments().getStringArrayList(ARG_DEPARTURES);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, departures);
        departuresListView.setAdapter(adapter);

        // Listener für Klick auf eine Abfahrt
        departuresListView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedDeparture = departures.get(position);
            Log.d("DepartureListFragment", "Gewählte Abfahrt: " + selectedDeparture);

            // Extrahiere die Trip-ID aus der Abfahrtsinformation
            String tripId = selectedDeparture.split("Trip: ")[1].trim();

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
