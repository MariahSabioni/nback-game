package com.example.nback_minet_sabioni;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder>  {
    //Adapter for the Recycler view of results

    private final List<DualGame> resultsList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView levelNTextView;
        public TextView trialTimeTextView;
        public TextView nbEventsTextView;
        public TextView correctsTextView;
        public TextView errorsTextView;
        public ImageView visualImageView;
        public ImageView audioImageView;
        public TextView dateTextView;

        public ViewHolder(View v) {
            super(v);
        }
    }

    // initialize dataset of the adapter
    public ResultsAdapter(List<DualGame> loadedResultsList){
        if (loadedResultsList != null){
            Collections.reverse(loadedResultsList);
            resultsList = loadedResultsList;
        }else{
            resultsList = null;
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new item view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.result_item, parent, false);
        final ViewHolder vh = new ViewHolder(itemView);

        vh.levelNTextView = itemView.findViewById(R.id.level_n_res);
        vh.trialTimeTextView = itemView.findViewById(R.id.trial_time_res);
        vh.nbEventsTextView = itemView.findViewById(R.id.nb_events_res);
        vh.correctsTextView = itemView.findViewById(R.id.corrects_res);
        vh.errorsTextView = itemView.findViewById(R.id.errors_res);
        vh.visualImageView = itemView.findViewById(R.id.type_visual);
        vh.audioImageView = itemView.findViewById(R.id.type_audio);
        vh.dateTextView = itemView.findViewById(R.id.date);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        DualGame dualGame = resultsList.get(position);

        vh.levelNTextView.setText("Level: "+String.valueOf(dualGame.getLevelN()));
        vh.trialTimeTextView.setText("Trial time: "+String.valueOf(dualGame.getTrialTime()));
        vh.nbEventsTextView.setText("Nº steps: "+String.valueOf(dualGame.getNumberEvents()));
        vh.correctsTextView.setText(String.valueOf(dualGame.getCurrentScore()));
        vh.errorsTextView.setText(String.valueOf(dualGame.getCurrentErrors()));
        vh.dateTextView.setText("Date: "+dualGame.getPrettyDate());

        if (!dualGame.getVisualStimuli()){
            vh.visualImageView.setVisibility(View.INVISIBLE);
        } else{
            vh.visualImageView.setVisibility(View.VISIBLE);
        }

        if (!dualGame.getAudioStimuli()){
            vh.audioImageView.setVisibility(View.INVISIBLE);
        } else{
            vh.audioImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (resultsList != null) {
            return resultsList.size();
        }else{
            return 0;
        }
    }

}