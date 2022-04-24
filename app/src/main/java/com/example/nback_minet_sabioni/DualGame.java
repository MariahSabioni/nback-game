package com.example.nback_minet_sabioni;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DualGame implements Serializable {

    //Settings
    private NbackSettings nbackSettings;
    private int currentScore;
    private int currentStep;
    private int currentErrors;
    private long gameDate;

    //List to be filled
    public List<Integer> integerList;
    public List<String> lettersList;

    public DualGame(NbackSettings nbackSettings){
        this.nbackSettings = nbackSettings;
        this.currentErrors = 0;
        this.currentStep = 1;
        this.currentScore = 0;
        this.integerList = new ArrayList<>();
        this.lettersList = new ArrayList<>();
        this.gameDate = System.currentTimeMillis();
    }

    public void reset(){
        currentScore = 0;
        currentErrors = 0;
        currentStep = 1;
        integerList.clear();
        lettersList.clear();
        gameDate = System.currentTimeMillis();
    }

    public int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public String getRandomLetter() {
        //Todo maybe change this set of letters (sometimes it's difficult to distinguish)
        String[] lettersArray = {"C", "Q", "L", "R", "K", "A", "B", "O", "S"};
        int max = lettersArray.length-1;
        int min = 0;
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        int ind = r.nextInt((max - min) + 1) + min;

        return lettersArray[ind];
    }

    public void markScore(){
        currentScore++;
    }

    public void markError(){
        currentErrors++;
    }

    public void countStep(){
        currentStep++;
    }

    public boolean isFinished(){
        return nbackSettings.getNumberEvents() == currentStep-1;
    }

    public int getCurrentScore(){
        return currentScore;
    }

    public int getCurrentErrors() {
        return currentErrors;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getLevelN(){
        return nbackSettings.getLevelN();
    }

    public int getNumberEvents(){
        return nbackSettings.getNumberEvents();
    }

    public int getTrialTime(){
        return nbackSettings.getTrialTime();
    }

    public boolean getVisualStimuli(){
        return nbackSettings.getStimuliVisual();
    }

    public boolean getAudioStimuli(){
        return nbackSettings.getStimuliAudio();
    }

    public boolean isMatchVisual(){
        int length = integerList.size();
        int numClicked = integerList.get(length - 1);
        int numTarget = integerList.get(length - nbackSettings.getLevelN() - 1);
        if (numClicked == numTarget && (length>nbackSettings.getLevelN())){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isMatchAudio(){
        int length = lettersList.size();
        String numClicked = lettersList.get(length - 1);
        String numTarget = lettersList.get(length - nbackSettings.getLevelN() - 1);
        if (numClicked.equals(numTarget) && (length>nbackSettings.getLevelN())){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isMissVisual(boolean button_pressed){
        return dualGame.isMatchVisual() && !button_pressed;
    }

    public boolean isMissAudio(boolean button_pressed){
        return dualGame.isMatchAudio() && !button_pressed;
    }

    public boolean countPlayVisual (){
        return dualGame.integerList.size() > dualGame.getLevelN();
    }

    public boolean countPlayAudio (){
        return dualGame.lettersList.size() > dualGame.getLevelN();
    }

    public static DualGame getInstance(){
        if (dualGame == null) {
            dualGame = new DualGame();
        }
        return dualGame;
    }

    private static DualGame dualGame = null;

    private DualGame() { // NB! Must be private - Singleton implementation
        reset();
    }

    public void resetDate() {
        gameDate = System.currentTimeMillis();
    }

    public String getPrettyDate() {
        long mDate = gameDate;
        Date prettyDate = new Date (mDate);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM, HH:mm");
        return format.format(prettyDate);
    }
}
