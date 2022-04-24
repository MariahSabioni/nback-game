package com.example.nback_minet_sabioni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.net.ParseException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingsActivity extends AppCompatActivity {

    //logging
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    //ui
    private Spinner trialTimeSpinner;
    private Spinner numberEventsSpinner;
    private Spinner levelNSpinner;
    private CheckBox stimuliVisualCheck;
    private CheckBox stimuliAudioCheck;

    //settings model
    private NbackSettings nbackSettings;
    private NbackSettings defaultSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //ui
        levelNSpinner = findViewById(R.id.spinner_n_match);
        ArrayAdapter<CharSequence> adapter_n_match = ArrayAdapter.createFromResource(this, R.array.n_match, android.R.layout.simple_spinner_item);
        adapter_n_match.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelNSpinner.setAdapter(adapter_n_match);

        numberEventsSpinner = findViewById(R.id.spinner_nb_events);
        ArrayAdapter<CharSequence> adapter_nb_events = ArrayAdapter.createFromResource(this, R.array.nb_events, android.R.layout.simple_spinner_item);
        adapter_nb_events.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberEventsSpinner.setAdapter(adapter_nb_events);

        trialTimeSpinner = findViewById(R.id.spinner_time_events);
        ArrayAdapter<CharSequence> adapter_time_events = ArrayAdapter.createFromResource(this, R.array.time_events, android.R.layout.simple_spinner_item);
        adapter_time_events.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trialTimeSpinner.setAdapter(adapter_time_events);

        stimuliVisualCheck = findViewById(R.id.visual_stimuli);
        stimuliAudioCheck = findViewById(R.id.audio_stimuli);

        //data
        nbackSettings = NbackSettings.getInstance();
        Log.i(LOG_TAG,"nbackSettings: " + nbackSettings);
        //defaultSettings = NbackSettings.getDefault();
    }

    public void onStart() {
        super.onStart();
        nbackSettings = deserialize();

        Log.i(LOG_TAG,"trialTime: " + nbackSettings.getTrialTime());
        String trialTime = String.valueOf(nbackSettings.getTrialTime());
        ArrayAdapter<CharSequence> adapter_time_events = ArrayAdapter.createFromResource(this, R.array.time_events, android.R.layout.simple_spinner_item);
        adapter_time_events.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trialTimeSpinner.setAdapter(adapter_time_events);
        int trialTimePos = adapter_time_events.getPosition(trialTime);
        trialTimeSpinner.setSelection(trialTimePos);

        String numberEvents = String.valueOf(nbackSettings.getNumberEvents());
        ArrayAdapter<CharSequence> adapter_nb_events = ArrayAdapter.createFromResource(this, R.array.nb_events, android.R.layout.simple_spinner_item);
        adapter_nb_events.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberEventsSpinner.setAdapter(adapter_nb_events);
        int numberEventsPos = adapter_nb_events.getPosition(numberEvents);
        numberEventsSpinner.setSelection(numberEventsPos);

        String levelN = String.valueOf(nbackSettings.getLevelN());
        ArrayAdapter<CharSequence> adapter_n_match = ArrayAdapter.createFromResource(this, R.array.n_match, android.R.layout.simple_spinner_item);
        adapter_n_match.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelNSpinner.setAdapter(adapter_n_match);
        int levelNPos = adapter_n_match.getPosition(levelN);
        levelNSpinner.setSelection(levelNPos);

        stimuliAudioCheck.setChecked(nbackSettings.getStimuliAudio());
        stimuliVisualCheck.setChecked(nbackSettings.getStimuliVisual());

    }

    public void cancelPreferences(View view){
        //returns to main activity without saving
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onStop() {
        super.onStop();
    }

    public void onResume() {
        //set to saved settings ??
        super.onResume();
    }

    public void onPause() {
        //cancel changes ??
        super.onPause();
    }

    public void savePreferences(View view) throws ParseException {
        //save new preferences

        int trial_time = Integer.parseInt(trialTimeSpinner.getSelectedItem().toString());
        int level_n = Integer.parseInt(levelNSpinner.getSelectedItem().toString());
        int number_events = Integer.parseInt(numberEventsSpinner.getSelectedItem().toString());
        boolean stimuli_audio = stimuliAudioCheck.isChecked();
        boolean stimuli_visual = stimuliVisualCheck.isChecked();

        NbackSettings newNbackSettings = new NbackSettings(trial_time, level_n, number_events, stimuli_audio, stimuli_visual);
        Log.i(LOG_TAG, "NbackSettings: " + newNbackSettings);
        Log.i(LOG_TAG, "NbackSettings new level: " + newNbackSettings.getLevelN());

        serialize(newNbackSettings);
        Log.i(LOG_TAG, "Preferences saved!");

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        Toast toast = Toast.makeText(this, "Settings saved!",
                Toast.LENGTH_SHORT);
        toast.show();
    }

    // TODO should this be another thread?
    // serialization - save settings
    private void serialize(NbackSettings nbackSettings) {
        try {
            FileOutputStream fileOut =
                    openFileOutput("nback.ser", Context.MODE_PRIVATE);
            //new FileOutputStream("nback.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(nbackSettings);
            out.close();
            fileOut.close();
            Log.i(LOG_TAG,"Serialized data is saved in /nback.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    // serialization - load settings
    private NbackSettings deserialize(){
        nbackSettings = null;
        try {
            FileInputStream fileIn =
                    //new FileInputStream("weather.ser");
                    openFileInput("nback.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            nbackSettings = (NbackSettings) in.readObject();
            in.close();
            fileIn.close();
            Log.i("deserialize","Deserializing completed");
        } catch (IOException i) {
            //i.printStackTrace();
            nbackSettings = NbackSettings.getInstance();
        } catch (ClassNotFoundException c) {
            Log.i("deserialize","WeatherList class not found");
            c.printStackTrace();
        }
        return nbackSettings;
    }

}