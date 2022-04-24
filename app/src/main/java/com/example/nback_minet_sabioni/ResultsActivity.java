package com.example.nback_minet_sabioni;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ResultsActivity extends AppCompatActivity {

    //logging
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    //data
    private List<DualGame> resultsList;

    //recycler
    RecyclerView.Adapter<ResultsAdapter.ViewHolder> mResultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        //data
        resultsList = deserializeResults();
        //Log.i(LOG_TAG,"Results list has "+resultsList.size()+" elements");
        //recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //adapter
        mResultsAdapter = new ResultsAdapter(resultsList);
        recyclerView.setAdapter(mResultsAdapter);
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

    public void returnToMain(View view) {
        //returns to main activity without saving
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
