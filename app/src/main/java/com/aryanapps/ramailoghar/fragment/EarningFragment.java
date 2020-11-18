package com.aryanapps.ramailoghar.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aryanapps.ramailoghar.BoostFacebook;
import com.aryanapps.ramailoghar.Comedy_Videos;
import com.aryanapps.ramailoghar.Feelings;
import com.aryanapps.ramailoghar.R;
import com.aryanapps.ramailoghar.SessionManagement;
import com.aryanapps.ramailoghar.WithDraw;
import com.aryanapps.ramailoghar.YtVideoBoost;
import com.aryanapps.ramailoghar.helper.Network;




public class EarningFragment extends Fragment {

    TextView amount,rank;
    Button boost_youtube;
    Button boost_fb,withDrawBtn;
    RelativeLayout watch_video,facebook_like,subscribe_youtube,watch_ads;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_earning, container, false);
        amount = view.findViewById(R.id.amount);
        rank = view.findViewById(R.id.rank_num);
        boost_youtube = view.findViewById(R.id.boost_youtube);
        boost_fb = view.findViewById(R.id.boost_fb);
        watch_video = view.findViewById(R.id.watch_video);
        withDrawBtn = view.findViewById(R.id.withdrawbuttn);
        facebook_like = view.findViewById(R.id.facebook_like);
        subscribe_youtube = view.findViewById(R.id.subscribe_youtube);
        watch_ads = view.findViewById(R.id.watch_ads);
        facebook_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Not available now", Toast.LENGTH_SHORT).show();
            }
        });
        subscribe_youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Not available now", Toast.LENGTH_SHORT).show();
            }
        });
        withDrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), WithDraw.class));
            }
        });
        watch_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Feelings.class));
            }
        });
        boostYoutubeVideo();
        boostFb();
        watchVideoOnClick();
        getTotalUserAmount();
        updateRank();
        setRankToUi();
        return view;
    }

    private void setRankToUi() {
        rank.setText(new SessionManagement(getContext()).getRank());
    }

    private void boostYoutubeVideo() {
        boost_youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             getActivity().startActivity(new Intent(getContext(), YtVideoBoost.class));
            }
        });
    }
    private void boostFb() {
        boost_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             getActivity().startActivity(new Intent(getContext(), BoostFacebook.class));
            }
        });
    }
    private void watchVideoOnClick() {
        watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Comedy_Videos.class));
            }
        });
    }
    private void getTotalUserAmount() {
        amount.setText(new SessionManagement(getContext()).getAMOUNT());
    }

    @Override
    public void onStart() {
        super.onStart();
        new Network(getContext()).getAmount();
    }

    private void updateRank() {
        new Network(getContext()).getRank();
    }

}