package com.example.nback_minet_sabioni;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //logging
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //Serialized settings
    private NbackSettings nbackSettings;

    //Results
    private List<DualGame> resultsList;

    //Timer
    private Timer visualTimer;
    private Timer audioTimer;
    private Timer dualTimer;
    private Handler handler;

    //data
    private int levelN;
    private int eventsNb;
    private int trialTime;

    //ui
    private TextView typeStimuli;
    private TextView Level;
    private TextView TrialTime;
    private ImageView audio_image;
    private Button audio_match_button;
    private Button visual_match_button;
    private Button match_button;
    private Button start_button;
    private TextView Current_score;
    private TextView Step;
    private ImageView errorAnimation;

    //Visual
    private ImageView[] imageViews;
    private Drawable visualBlueCircle;
    private Drawable errorCross;
    private Drawable correctCheck;

    //Audio
    private TextToSpeech textToSpeech;
    private static final int utteranceId = 42;

    //Dual
    private DualGame dualGame;
    private boolean buttonPressed;
    private boolean audioButtonPressed;
    private boolean visualButtonPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageViews = loadReferencesToImageViews();

        // load drawables (images)
        Resources resources = getResources();
        visualBlueCircle = ResourcesCompat.getDrawable(resources, R.drawable.blue_circle, null);
        //Todo change errorCross into Jonas meme
        errorCross = ResourcesCompat.getDrawable(resources, R.drawable.jonas_upset, null);
        correctCheck = ResourcesCompat.getDrawable(resources,R.drawable.green_check,null);

        //UI
        typeStimuli = findViewById(R.id.stimuli_type);
        Level = findViewById(R.id.level_n);
        TrialTime = findViewById(R.id.trial_time);
        Current_score = findViewById(R.id.correct_ongoing);
        Step = findViewById(R.id.step_ongoing);
        errorAnimation = findViewById(R.id.errorImageView);

        //UI audio
        audio_image = findViewById(R.id.audio_image);
        audio_match_button = findViewById(R.id.match_audio_button);

        //UI visual (grid is behind audio_image)
        visual_match_button = findViewById(R.id.match_visual_button);

        //UI both
        match_button = findViewById(R.id.match_button);
        start_button = findViewById(R.id.restart_button);

        //Timer
        visualTimer = null;
        handler = new Handler();

        //load settings
        nbackSettings = deserialize(); //created default when no previous data exists
        levelN = nbackSettings.getLevelN();
        eventsNb = nbackSettings.getNumberEvents();
        trialTime = nbackSettings.getTrialTime();

        //load results
        resultsList = deserializeResults(); //creates new empty array when no previous data exists
        //Log.i(LOG_TAG,"Results list has "+resultsList.size()+" results on create");

        //Set textviews
        Level.setText("Level: "+ levelN);
        TrialTime.setText("Trial time: "+ trialTime);

        //Hide error animation Image view
        errorAnimation.setImageDrawable(null);

        if (nbackSettings.getStimuliAudio() && nbackSettings.getStimuliVisual()){
            //Hide single button and show two sides buttons
            //Hide audio_image
            audio_image.setVisibility(View.INVISIBLE);
            match_button.setVisibility(View.GONE);
            audio_match_button.setVisibility(View.VISIBLE);
            visual_match_button.setVisibility(View.VISIBLE);
            typeStimuli.setText(R.string.both_stimuli);
        }
        else if (nbackSettings.getStimuliAudio() && !nbackSettings.getStimuliVisual()){
            //Hide two sides buttons and show single button
            //Show audio image
            //Change button text
            audio_image.setVisibility(View.VISIBLE);
            match_button.setVisibility(View.VISIBLE);
            audio_match_button.setVisibility(View.GONE);
            visual_match_button.setVisibility(View.GONE);
            match_button.setText(R.string.match_audio_button);
            typeStimuli.setText(R.string.audio_stimuli);
        }
        else if (nbackSettings.getStimuliVisual() && !nbackSettings.getStimuliAudio()){
            //Hide two sides buttons and show single button
            //Hide audio image
            //Change button text
            audio_image.setVisibility(View.INVISIBLE);
            match_button.setVisibility(View.VISIBLE);
            audio_match_button.setVisibility(View.GONE);
            visual_match_button.setVisibility(View.GONE);
            match_button.setText(R.string.match_visual_button);
            typeStimuli.setText(R.string.visual_stimuli);
        }
    }

    // cancel timer and pending tasks when user leaves this activity
    @Override
    protected void onPause() {
        super.onPause();
        resetTimers();
        handler.removeCallbacks(createMsgDialogRunnable);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void resetTimers(){
        if (visualTimer != null) {
            visualTimer.cancel();
            visualTimer = null;
        }
        if (audioTimer != null) {
            audioTimer.cancel();
            audioTimer = null;
        }
        if (dualTimer != null) {
            dualTimer.cancel();
            dualTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialize the text-to-speech service - we do this initialization
        // in onResume because we shutdown the service in onPause
        textToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            textToSpeech.setLanguage(Locale.UK);
                        }
                    }
                });
    }

    public void launchPreferences(View view) {
        //This method is called when the user presses the preferences button
        Log.d(LOG_TAG, "Preferences Button clicked!");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // deserialization - load settings
    public void launchResults(View view) {
        //This method is called when the user presses the preferences button
        Log.d(LOG_TAG, "Results Button clicked!");
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }

    // serialization - load settings
    public NbackSettings deserialize(){
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
        } catch (Exception e) {
            //i.printStackTrace();
            nbackSettings = NbackSettings.getInstance();
            Log.i("deserialize","WeatherList class not found");
            e.printStackTrace();
        }
        return nbackSettings;
    }

    public void launchGame(View view) {
        if (visualTimer != null) {
            //there is visual game ongoing, reset it
            visualTimer.cancel();
            visualTimer = null;
            //Reset UI
            int image_view = dualGame.integerList.get(dualGame.getCurrentStep()-2);
            imageViews[image_view].setImageDrawable(null);
            Current_score.setText(R.string.correct_ongoing);
            Step.setText(R.string.step_ongoing);
            start_button.setText(R.string.restart_button);
            dualGame.reset();
        } else if (audioTimer != null){
            // there is auDIO game ongoing, reset it
            audioTimer.cancel();
            audioTimer = null;
            //Reset UI
            Current_score.setText(R.string.correct_ongoing);
            Step.setText(R.string.step_ongoing);
            start_button.setText(R.string.restart_button);
            dualGame.reset();
        } else if (dualTimer != null) {
            // there is Dual game ongoing, reset it
            dualTimer.cancel();
            dualTimer = null;
            //Reset UI
            int image_view = dualGame.integerList.get(dualGame.getCurrentStep()-2);
            imageViews[image_view].setImageDrawable(null);
            Current_score.setText(R.string.correct_ongoing);
            Step.setText(R.string.step_ongoing);
            start_button.setText(R.string.restart_button);
            dualGame.reset();
        } else {
            //there is no game ongoing, start new visual game
            if (nbackSettings.getStimuliVisual() && !nbackSettings.getStimuliAudio()) {
                dualGame = new DualGame(nbackSettings);
                Log.d(LOG_TAG, String.valueOf(dualGame));
                visualTimer = new Timer();
                visualTimer.schedule(new VisualTimerTask(), 0, 1000* trialTime);
                start_button.setText(R.string.reset_button);
            }
            //there is no game ongoing, start new audio game
            else if (nbackSettings.getStimuliAudio() && !nbackSettings.getStimuliVisual()){
                dualGame = new DualGame(nbackSettings);
                Log.d(LOG_TAG, String.valueOf(dualGame));
                audioTimer = new Timer();
                audioTimer.schedule(new AudioTimerTask(), 0, 1000* trialTime);
                start_button.setText(R.string.reset_button);
            }
            //there is no game ongoing, start new dual game
            else if (nbackSettings.getStimuliAudio() && nbackSettings.getStimuliVisual()){
                Log.i(LOG_TAG, "new dual game");
                dualGame = new DualGame(nbackSettings);
                Log.d(LOG_TAG, String.valueOf(dualGame));
                dualTimer = new Timer();
                dualTimer.schedule(new DualTimerTask(), 0, 1000* trialTime);
                start_button.setText(R.string.reset_button);
            }
        }
    }

    // the task to execute periodically
    public class VisualTimerTask extends TimerTask {
        public void run() {
            if (dualGame.isFinished()){
                //finish game and show results
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Correct: " + String.valueOf(dualGame.getCurrentScore()) + "/ Errors: "
                                + String.valueOf(dualGame.getCurrentErrors());
                        Current_score.setText(msg);
                    }
                });
                // save results for visual game
                dualGame.resetDate();
                //Log.i(LOG_TAG,"Old list has "+resultsList.size()+" elements");
                resultsList.add(dualGame);
                serializeResults(resultsList);
                //Log.i(LOG_TAG,"Serializing completed. List has "+resultsList.size()+" elements");
                //Alert with results
                handler.post(createMsgDialogRunnable);
                //cancel timer but it doesn't make it null
                visualTimer.cancel();
                //Log.i(LOG_TAG,"Serializing completed. New list has "+resultsList.size()+" elements");
            } else {
                //If the user didn't press the button where as it was a match
                if (dualGame.integerList.size()> dualGame.getLevelN() && dualGame.isMatchVisual() && !buttonPressed){
                    dualGame.markError();
                    handler.post(animationErrorRunnable);
                    handler.postDelayed(animationCancelRunnable,1000);
                }
                int random_int = dualGame.getRandomNumberInRange(0,8);
                dualGame.integerList.add(random_int);

                //We need to be on the main thread to modify UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // run game logic
                        if (dualGame.integerList.size()>1){
                            //clean the view that was modified by the previous execution
                            int image_view = dualGame.integerList.get(dualGame.getCurrentStep()-2);
                            imageViews[image_view].setImageDrawable(null);
                        }

                        //Show one blue circle in the picked imageview.
                        updateImageViews(random_int);

                        String Current_scoreS = "Correct: "+String.valueOf(dualGame.getCurrentScore())
                                +" / Errors: "+String.valueOf(dualGame.getCurrentErrors());
                        Current_score.setText(Current_scoreS);
                        String StepS = "Step: "+String.valueOf(dualGame.getCurrentStep())
                                +"/"+String.valueOf(dualGame.getNumberEvents());
                        Step.setText(StepS);

                        dualGame.countStep();
                    }
                });

                if (dualGame.integerList.size()> dualGame.getLevelN()){

                    buttonPressed = false;
                    match_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dualGame.integerList.size()> dualGame.getLevelN() && dualGame.isMatchVisual() && !buttonPressed){
                                dualGame.markScore();
                                handler.post(animationCorrectRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            else if (dualGame.integerList.size()> dualGame.getLevelN() && !dualGame.isMatchVisual() && !buttonPressed){
                                dualGame.markError();
                                handler.post(animationErrorRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            buttonPressed = true;
                        }
                    });
                }
            }
        }
    }

    // the task to execute periodically
    public class AudioTimerTask extends TimerTask {
        public void run() {
            if (dualGame.isFinished()){
                //finish game and show results
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Correct: " + String.valueOf(dualGame.getCurrentScore()) + "/ Errors: "
                                + String.valueOf(dualGame.getCurrentErrors());
                        Current_score.setText(msg);
                    }
                });
                //save results for audioGame
                dualGame.resetDate();
                //Log.i(LOG_TAG,"Old list has "+resultsList.size()+" elements");
                resultsList.add(dualGame);
                serializeResults(resultsList);
                //Log.i(LOG_TAG,"Serializing completed. List has "+resultsList.size()+" elements");
                //Alert with results
                handler.post(createMsgDialogRunnable);
                //cancel timer but it doesn't make it null
                audioTimer.cancel();
                //Log.i(LOG_TAG,"Serializing completed. New list has "+resultsList.size()+" elements");
            } else {
                //If the didn't press the button where as it was a match
                if (dualGame.lettersList.size()> dualGame.getLevelN() && dualGame.isMatchAudio() && !buttonPressed){
                    dualGame.markError();
                    handler.post(animationErrorRunnable);
                    handler.postDelayed(animationCancelRunnable,1000);
                }
                String random_letter = dualGame.getRandomLetter();
                sayIt(random_letter);
                dualGame.lettersList.add(random_letter);

                //We need to be on the main thread to modify UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String Current_scoreS = "Correct: "+String.valueOf(dualGame.getCurrentScore())
                                +" / Errors: "+String.valueOf(dualGame.getCurrentErrors());
                        Current_score.setText(Current_scoreS);
                        String StepS = "Step: "+String.valueOf(dualGame.getCurrentStep())
                                +"/"+String.valueOf(dualGame.getNumberEvents());
                        Step.setText(StepS);

                        dualGame.countStep();
                    }
                });

                if (dualGame.lettersList.size()> dualGame.getLevelN()){

                    buttonPressed = false;
                    match_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dualGame.lettersList.size()> dualGame.getLevelN() && dualGame.isMatchAudio() && !buttonPressed){
                                dualGame.markScore();
                                handler.post(animationCorrectRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            else if (dualGame.lettersList.size()> dualGame.getLevelN() && !dualGame.isMatchAudio() && !buttonPressed){
                                dualGame.markError();
                                handler.post(animationErrorRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            buttonPressed = true;
                        }
                    });
                }
            }
        }
    }

    // the task to execute periodically
    public class DualTimerTask extends TimerTask {
        public void run() {
            if (dualGame.isFinished()){
                //finish game and show results
                Log.i(LOG_TAG, "game finished: "+dualGame.getCurrentStep());
                                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Correct: " + String.valueOf(dualGame.getCurrentScore()) + "/ Errors: "
                                + String.valueOf(dualGame.getCurrentErrors());
                        Current_score.setText(msg);
                    }
                });
                //Save results for dualgame
                dualGame.resetDate();
                //Log.i(LOG_TAG,"Old list has "+resultsList.size()+" elements");
                resultsList.add(dualGame);
                serializeResults(resultsList);
                //cancel timer but it doesn't make it null
                handler.post(createMsgDialogRunnable);
                dualTimer.cancel();
                //Log.i(LOG_TAG,"Serializing completed. New list has "+resultsList.size()+" elements");
            } else {

                //If the didn't press the button where as it was a match, mark error
                //Errors in visual and audio count separately
                if (dualGame.integerList.size()> dualGame.getLevelN() && dualGame.isMatchVisual() && !visualButtonPressed){
                    dualGame.markError();
                    handler.post(animationErrorRunnable);
                    handler.postDelayed(animationCancelRunnable,1000);
                }
                else if (dualGame.lettersList.size()> dualGame.getLevelN() && dualGame.isMatchAudio() && !audioButtonPressed){
                    dualGame.markError();
                    handler.post(animationErrorRunnable);
                    handler.postDelayed(animationCancelRunnable,1000);
                }
                //new audio play
                String random_letter = dualGame.getRandomLetter();
                sayIt(random_letter);
                dualGame.lettersList.add(random_letter);

                //new visual play
                int random_int = dualGame.getRandomNumberInRange(0,8);
                dualGame.integerList.add(random_int);

                //We need to be on the main thread to modify UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dualGame.integerList.size()>1){
                            //clean the view that was modified by the previous execution
                            int image_view = dualGame.integerList.get(dualGame.getCurrentStep()-2);
                            imageViews[image_view].setImageDrawable(null);
                        }
                        //Show one blue circle in the picked imageview.
                        updateImageViews(random_int);

                        //Update scores and steps
                        String Current_scoreS = "Correct: "+String.valueOf(dualGame.getCurrentScore())
                                +" / Errors: "+String.valueOf(dualGame.getCurrentErrors());
                        Current_score.setText(Current_scoreS);
                        String StepS = "Step: "+String.valueOf(dualGame.getCurrentStep())
                                +"/"+String.valueOf(dualGame.getNumberEvents());
                        Step.setText(StepS);
                        dualGame.countStep();
                    }
                });

                // if he pressed the button
                if (dualGame.lettersList.size()> dualGame.getLevelN()){
                    audioButtonPressed = false;
                    audio_match_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dualGame.lettersList.size()> dualGame.getLevelN() && dualGame.isMatchAudio() && !audioButtonPressed){
                                dualGame.markScore();
                                handler.post(animationCorrectRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            else if (dualGame.lettersList.size()> dualGame.getLevelN() && !dualGame.isMatchAudio() && !audioButtonPressed){
                                dualGame.markError();
                                handler.post(animationErrorRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            audioButtonPressed = true;
                        }
                    });
                }
                // if he pressed the button
                if (dualGame.integerList.size()> dualGame.getLevelN()){
                    visualButtonPressed = false;
                    visual_match_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dualGame.integerList.size()> dualGame.getLevelN() && dualGame.isMatchVisual() && !visualButtonPressed){
                                dualGame.markScore();
                                handler.post(animationCorrectRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            else if (dualGame.integerList.size()> dualGame.getLevelN() && !dualGame.isMatchVisual() && !visualButtonPressed){
                                dualGame.markError();
                                handler.post(animationErrorRunnable);
                                handler.postDelayed(animationCancelRunnable,1000);
                            }
                            visualButtonPressed = true;
                        }
                    });
                }
            }
        }
    }

    // serialization - save results
    private void serializeResults(List<DualGame> resList) {
        try {
            FileOutputStream fileOut =
                    openFileOutput("dual_results.ser", Context.MODE_PRIVATE);
            //new FileOutputStream("nback.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(resList);
            out.close();
            fileOut.close();
            Log.i(LOG_TAG,"Serialized data is saved in /dual_results.ser");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // serialization - load results
    private List<DualGame> deserializeResults(){
        resultsList = null;
        try {
            FileInputStream fileIn =
                    //new FileInputStream("weather.ser");
                    openFileInput("dual_results.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            resultsList = (List<DualGame>) in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception e) {
            e.printStackTrace();
            resultsList = new ArrayList<>();
        }
        return resultsList;
    }

    private void sayIt(String utterance) {
        textToSpeech.speak(utterance, TextToSpeech.QUEUE_FLUSH,
                null, new String("" + utteranceId));
    }

    private final Runnable createMsgDialogRunnable = new Runnable() {
        @Override
        public void run() {
            String results_msg = "Correct: " + String.valueOf(dualGame.getCurrentScore()) + "/ Errors: "
                    + String.valueOf(dualGame.getCurrentErrors());
            createMsgDialog("Final results", results_msg).show();
        }
    };



    // AlertDialogs
    private AlertDialog createMsgDialog(String title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);

        builder.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        return builder.create();
    }

    public void updateImageViews(int view_nb) {
        imageViews[view_nb].setImageDrawable(visualBlueCircle);
        AnimationUtils.fadeInImageView(imageViews[view_nb]);
    }

    private final Runnable animationErrorRunnable = new Runnable() {
        @Override
        public void run() {
            errorAnimation.setImageDrawable(errorCross);
            AnimationUtils.fadeInImageView(errorAnimation);
        }
    };

    private final Runnable animationCorrectRunnable = new Runnable() {
        @Override
        public void run() {
            errorAnimation.setImageDrawable(correctCheck);
            AnimationUtils.fadeInImageView(errorAnimation);
        }
    };

    private final Runnable animationCancelRunnable = new Runnable() {
        @Override
        public void run() {
            errorAnimation.setImageDrawable(null);
            AnimationUtils.fadeInImageView(errorAnimation);
        }
    };



    // load references to, and add listener on, all image views
    private ImageView[] loadReferencesToImageViews() {
        // well, it would probably be easier (for a larger matrix) to create
        // the views in Java code and then add them to the appropriate layout
        ImageView[] imgViews = new ImageView[9];
        imgViews[0] = findViewById(R.id.imageView0);
        imgViews[1] = findViewById(R.id.imageView1);
        imgViews[2] = findViewById(R.id.imageView2);
        imgViews[3] = findViewById(R.id.imageView3);
        imgViews[4] = findViewById(R.id.imageView4);
        imgViews[5] = findViewById(R.id.imageView5);
        imgViews[6] = findViewById(R.id.imageView6);
        imgViews[7] = findViewById(R.id.imageView7);
        imgViews[8] = findViewById(R.id.imageView8);

        return imgViews;
    }
}