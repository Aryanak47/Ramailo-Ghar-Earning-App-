package com.aryanapps.ramailoghar;


import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ProgressBtn {
    private CardView cardView;
    private TextView btnText;
    private ProgressBar progressBar;
    private ConstraintLayout layout;

    public ProgressBtn(View view, String text) {
        cardView = view.findViewById(R.id.card);
        btnText = view.findViewById(R.id.btnTxt);
        btnText.setText(text);
        progressBar = view.findViewById(R.id.progress);
        layout = view.findViewById(R.id.constraint);

    }

    void buttonActivated(String text) {
        progressBar.setVisibility(View.VISIBLE);
        btnText.setText(text);
    }

    void buttonDeactivated(String text) {
        progressBar.setVisibility(View.GONE);
        btnText.setText(text);
    }

}
