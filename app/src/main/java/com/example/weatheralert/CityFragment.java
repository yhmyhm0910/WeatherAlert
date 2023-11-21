package com.example.weatheralert;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;

public class CityFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_city, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {

        } else {
            Intent intent = getActivity().getIntent();
            String name = intent.getStringExtra("city_name");
            String temp = intent.getStringExtra("temp");
            String feelsLike = intent.getStringExtra("feelsLike");
            String humidity = intent.getStringExtra("humidity");
            if (name != null) {
                MaterialToolbar topAppBar = getActivity().findViewById(R.id.topAppBar);
                topAppBar.setTitle(name);
            }
            if (temp != null) {
                TextView t = getActivity().findViewById(R.id.temperature);
                t.setText(t.getText().toString() + " "  + temp + "°C");
            }
            if (feelsLike != null) {
                TextView f = getActivity().findViewById(R.id.feelsLike);
                f.setText(f.getText().toString() + " " + feelsLike + "°C");
            }
            if (humidity != null) {
                TextView h = getActivity().findViewById(R.id.humidity);
                h.setText(h.getText().toString() + " "  + humidity + "%");
            }
            Button back = getActivity().findViewById(R.id.back);

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent startIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(startIntent);
                }
            });

            ImageView imageView = getActivity().findViewById(R.id.imageView);

            switch (name) {
                case "Guelph":
                    imageView.setImageResource(R.drawable.guelph_dt);
                    break;
                case "Toronto":
                    imageView.setImageResource(R.drawable.toronto_dt);
                    break;
                case "Waterloo":
                    imageView.setImageResource(R.drawable.waterloo_dt);
                    break;
                default: // Windsor
                    imageView.setImageResource(R.drawable.windsor_dt);
            }



            DataHandler dataHandler = new DataHandler(requireContext());
            //dataHandler.deleteDatabase(getContext());


            String[] fetchedData = dataHandler.fetchData(name);
            Log.d("fetchedData", fetchedData[0] + fetchedData[1] + fetchedData[2] + fetchedData[3] + fetchedData[4] + fetchedData[5]);

            TextView lvTime = getActivity().findViewById(R.id.lastVisitTime);
            TextView lvTemp = getActivity().findViewById(R.id.lastVisitTemp);
            TextView lvFeelsLike = getActivity().findViewById(R.id.lastVisitFeelsLike);
            TextView lvHumidity = getActivity().findViewById(R.id.lastVisitHumidity);

            lvTime.setText("Last visited " + fetchedData[2] + "'s weather on " + fetchedData[0] + " " + fetchedData[1]);
            lvTemp.setText("Temperature: " + fetchedData[3] + "°C");
            lvFeelsLike.setText("Feels like: " + fetchedData[4] + "°C");
            lvHumidity.setText("Humidity: " + fetchedData[5] + "%");

            long insertedId = dataHandler.insertData(name, temp, feelsLike, humidity);
        }

    }
}