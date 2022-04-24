package com.example.nback_minet_sabioni;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Array;

public class NbackSettings implements Serializable {

    private static final String LOG_TAG = NbackSettings.class.getSimpleName();
    private final int trialTime;
    private final int levelN;
    private final int numberEvents;
    private boolean stimuliAudio;
    private boolean stimuliVisual;

    private static NbackSettings nbackSettings;

    //constructor
    public NbackSettings(int trialTime, int levelN, int numberEvents, boolean stimuliAudio, boolean stimuliVisual){
        this.trialTime = trialTime;
        this.levelN = levelN;
        this.stimuliAudio = stimuliAudio;
        this.stimuliVisual = stimuliVisual;
        this.numberEvents = numberEvents;
    }

    // default settings
    public static NbackSettings getDefault() {
        Log.i(LOG_TAG,"getDefault called");
        int defaultTrialTime = 3;
        int defaultLevelN = 1;
        int defaultNumberEvents = 10;
        boolean defaultStimuliAudio = false;
        boolean defaultStimuliVisual = true;
        NbackSettings defaultNbackSettings = new NbackSettings(defaultTrialTime, defaultLevelN,defaultNumberEvents,defaultStimuliAudio,defaultStimuliVisual);
        return defaultNbackSettings;
    }

    // get values
    public int getTrialTime(){return trialTime;}
    public int getLevelN(){return levelN;}
    public boolean getStimuliAudio(){return stimuliAudio;}
    public boolean getStimuliVisual(){return stimuliVisual;}
    public int getNumberEvents(){return numberEvents;}

    //singleton
    public static NbackSettings getInstance() {
        if (nbackSettings == null) {
            nbackSettings = NbackSettings.getDefault();
        }
        return nbackSettings;
    }

}
