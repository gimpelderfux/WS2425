package de.hka.ws2425.ui.main;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.hka.ws2425.api.RandomJoke;
import de.hka.ws2425.ui.map.MapFragment;
import de.hka.ws2425.R;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    private TextView delayTextView;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        delayTextView = view.findViewById(R.id.delayTextView); // Get a reference to the TextView
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("delayUpdate", getViewLifecycleOwner(), (requestKey, bundle) -> {
            long delay = bundle.getLong("delay");
            delayTextView.setText("Delay: " + delay + " minutes");
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        /*TextView txtMessage = this.getView().findViewById(R.id.txt_message);
        txtMessage.setText("This is a stupid way to set a text ....");*/

        Button btnDoSomething = this.getView().findViewById(R.id.btn_do_something);
        btnDoSomething.setOnClickListener((view) -> {
            FragmentTransaction fragmentTransaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack("MainFragment");
            fragmentTransaction.replace(R.id.container, new MapFragment());
            fragmentTransaction.commit();
        });

        TextView txtMessage = this.getView().findViewById(R.id.txt_message);

        mViewModel.getResult().observe(this.getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                txtMessage.setText(s);
            }
        });

        // WebService
        Button btnGetRandomJoke = this.getView().findViewById(R.id.btn_get_random_joke);
        btnGetRandomJoke.setOnClickListener(view -> this.mViewModel.requestRandomJoke());

        TextView txtJokeSetup = this.getView().findViewById(R.id.txt_joke_setup);
        TextView txtJokePunchline = this.getView().findViewById(R.id.txt_joke_punchline);

        mViewModel.getRandomJokeResponse().observe(this.getViewLifecycleOwner(), new Observer<RandomJoke>() {
            @Override
            public void onChanged(RandomJoke randomJoke) {
                txtJokeSetup.setText(randomJoke.getSetup());
                txtJokePunchline.setText(randomJoke.getPunchline());
            }
        });

        // MQTT-Broker
        Button btnSendMqttMessage = this.getView().findViewById(R.id.btn_send_mqtt_message);
        EditText edtMqttMessage = this.getView().findViewById(R.id.edt_mqtt_message);

        btnSendMqttMessage.setOnClickListener(view -> {
            this.mViewModel.sendMqttMessage("hka/test/app/message", edtMqttMessage.getText().toString());
        });
    }
}