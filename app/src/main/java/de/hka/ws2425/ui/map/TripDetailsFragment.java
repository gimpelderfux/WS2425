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

public class TripDetailsFragment extends Fragment {

    private static final String ARG_TRIP_ID = "tripId";
    private static final String ARG_GTFS_DATA = "gtfsData";
    private static final String ARG_CURRENT_LOCATION = "currentLocation";

    private String tripId;
    private GtfsData gtfsData;
    private GeoPoint currentLocation; // Aktuelle Position des Nutzers
    private List<String> tripDetails = new ArrayList<>();

    public static TripDetailsFragment newInstance(String tripId, GtfsData gtfsData, GeoPoint currentLocation) {
        TripDetailsFragment fragment = new TripDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRIP_ID, tripId);
        args.putSerializable(ARG_GTFS_DATA, gtfsData);
        args.putParcelable(ARG_CURRENT_LOCATION, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = getArguments().getString(ARG_TRIP_ID);
            gtfsData = (GtfsData) getArguments().getSerializable(ARG_GTFS_DATA);
            currentLocation = getArguments().getParcelable(ARG_CURRENT_LOCATION);
            Log.d("TripDetailsFragment", "Trip-ID und Position erhalten: " + tripId);

            // Lade die Haltestellen der Fahrt mit Verspätung
            tripDetails = MapUtils.getTripDetailsWithDelays(gtfsData, tripId, currentLocation);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);

        TextView tripIdTextView = view.findViewById(R.id.trip_id_textview);
        ListView stopsListView = view.findViewById(R.id.stops_listview);

        // Setze die Trip-ID
        tripIdTextView.setText("Fahrt-ID: " + tripId);

        // Zeige die Haltestellen mit Fahrzeiten und Verspätungen in der Liste
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, tripDetails);
        stopsListView.setAdapter(adapter);

        return view;
    }
}
