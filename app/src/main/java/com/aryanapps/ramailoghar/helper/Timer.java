package com.aryanapps.ramailoghar.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.aryanapps.ramailoghar.R;

import ticker.views.com.ticker.widgets.circular.timer.callbacks.CircularViewCallback;
import ticker.views.com.ticker.widgets.circular.timer.view.CircularView;

public class Timer {
    private Button nextBtn;
    private CircularView circularViewWithTimer;
    private static int secCounter = 0;

    public Timer(Button btn, CircularView circularViewWithTimer) {
        this.nextBtn = btn;
        this.circularViewWithTimer = circularViewWithTimer;
        listenTimerText();
    }

    public void setTimer() {
        secCounter++;
        nextBtn.setEnabled(false);
        CircularView.OptionsBuilder builderWithTimer =
                new CircularView.OptionsBuilder()
                        .shouldDisplayText(true)
                        .setCounterInSeconds(20 + secCounter)
                        .setCircularViewCallback(new CircularViewCallback() {

                            @Override
                            public void onTimerFinish() {
                                nextBtn.setEnabled(true);
                                nextBtn.setText(R.string.next_btn);
                            }

                            @Override
                            public void onTimerCancelled() {
                            }
                        });

        circularViewWithTimer.setOptions(builderWithTimer);
        circularViewWithTimer.startTimer();


    }

    private void listenTimerText() {
        circularViewWithTimer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nextBtn.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void pauseTime() {
        circularViewWithTimer.pauseTimer();
    }

    public void playTime() {
        circularViewWithTimer.resumeTimer();
    }
}
