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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hka.ws2425.R;
import de.hka.ws2425.utils.GtfsData;
import de.hka.ws2425.utils.StopTimes;
import de.hka.ws2425.utils.Stops;

public class TripDetailsFragment extends Fragment {

    private static final String ARG_TRIP_ID = "tripId";
    private static final String ARG_GTFS_DATA = "gtfsData";

    private String tripId;
    private GtfsData gtfsData;
    private List<String> tripDetails = new ArrayList<>();

    public static TripDetailsFragment newInstance(String tripId, GtfsData gtfsData) {
        TripDetailsFragment fragment = new TripDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRIP_ID, tripId);
        args.putSerializable(ARG_GTFS_DATA, gtfsData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = getArguments().getString(ARG_TRIP_ID);
            gtfsData = (GtfsData) getArguments().getSerializable(ARG_GTFS_DATA);
            Log.d("TripDetailsFragment", "Trip-ID erhalten: " + tripId);

            // Lade die Haltestellen der Fahrt mit Namen
            tripDetails = getTripDetailsWithStopNames(gtfsData, tripId);
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

        // Zeige die Haltestellen mit Fahrzeiten in der Liste
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, tripDetails);
        stopsListView.setAdapter(adapter);

        return view;
    }

    private List<String> getTripDetailsWithStopNames(GtfsData gtfsData, String tripId) {
        List<String> tripDetails = new ArrayList<>();

        // Erstelle eine Map für schnellen Zugriff auf Stop-Namen
        Map<String, String> stopIdToNameMap = new HashMap<>();
        for (Stops stop : gtfsData.getStops()) {
            stopIdToNameMap.put(stop.getId(), stop.getName());
        }

        // Füge Details mit Namen der Haltestellen hinzu
        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getTripId().equals(tripId)) {
                String stopName = stopIdToNameMap.getOrDefault(stopTime.getStopId(), "Unbekannte Haltestelle");
                String detail = "Haltestelle: " + stopName +
                        "\nAnkunft: " + stopTime.getArrivalTime() +
                        "\nAbfahrt: " + stopTime.getDepartureTime();
                tripDetails.add(detail);
            }
        }

        if (tripDetails.isEmpty()) {
            tripDetails.add("Keine Daten für diese Fahrt verfügbar.");
        }

        return tripDetails;
    }
}
