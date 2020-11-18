package com.aryanapps.ramailoghar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class YtVideoBoost extends AppCompatActivity {

    final float COST_PER_LIKE = (float) 0.20;
    EditText pageUrl, totalViews;
    Button submit;
    TextView max_view;
    int total = 0;
    Dialog dialog;
    ImageView closeBtn;
    Button okBtn;
    TextView title;
    TextView msg;
    SessionManagement sessionManagement;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yt_video_boost);
        sessionManagement = new SessionManagement(getApplicationContext());
        queue = Volley.newRequestQueue(getApplicationContext());
        pageUrl = findViewById(R.id.videoUrl);
        submit = findViewById(R.id.boostYTVideoBtn);
        max_view = findViewById(R.id.maxViews);
        totalViews = findViewById(R.id.totalView);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boostYTVideoBtn();
            }
        });
        readTotalUser();
        // custom dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        closeBtn = dialog.findViewById(R.id.closeBtn);
        msg = dialog.findViewById(R.id.msgDialog);
        title = dialog.findViewById(R.id.titleDialog);
        title.setText("Do you agree?");
        okBtn = dialog.findViewById(R.id.okDialogBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int userRqst = Integer.parseInt(totalViews.getText().toString().trim());
                final float totalCost = userRqst * COST_PER_LIKE;
                final ProgressDialog progress = new ProgressDialog(YtVideoBoost.this);
                progress.setMessage("Submitting...");
                progress.show();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Video Request");
                String videoId = databaseReference.push().getKey();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("url", pageUrl.getText().toString().trim());
                hashMap.put("id", videoId);
                hashMap.put("amount", totalCost);
                hashMap.put("maxView", userRqst);
                hashMap.put("currentView", 0);
                hashMap.put("userid", String.valueOf(new SessionManagement(getApplicationContext()).getId()));
                databaseReference.child(videoId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            deductAmount(totalCost);
                        }
                    }
                });
                emptyFields();
                progress.dismiss();
                dialog.dismiss();
            }
        });
    }

    private void boostYTVideoBtn() {
        float amount = Float.parseFloat(new SessionManagement(getApplicationContext()).getAMOUNT());
        String totalView = totalViews.getText().toString().trim();
        String url = pageUrl.getText().toString().trim();
        if (url.isEmpty()) {
            pageUrl.setError("empty field");
            return;
        }
        if (totalView.isEmpty()) {
            totalViews.setError("empty field");
            return;
        }
        if (Integer.parseInt(totalView) > total) {
            Toast.makeText(this, "You are exceeding max views", Toast.LENGTH_SHORT).show();
            return;
        }

        int userRqst = Integer.parseInt(totalViews.getText().toString().trim());
        float totalCost = userRqst * COST_PER_LIKE;
        if (totalCost <= amount) {
            msg.setText(new StringBuilder().append("It costs ").append(totalCost).append("( Rs. 0.20 per views)").toString());
            dialog.show();
            return;
        }
        Toast.makeText(this, "You don't have sufficient amount", Toast.LENGTH_SHORT).show();

    }

    private void emptyFields() {
        pageUrl.setText("");
        totalViews.setText("");
    }

    public void ytVideoPasteBtn(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";
        if (!(clipboard.hasPrimaryClip())) {
            Toast.makeText(this, "You haven't copied anything to paste", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        pasteData = (String) item.getText();
        if (pasteData != null) {
            pageUrl.setText(pasteData);

        } else {
            Toast.makeText(this, "You have copied an invalid data", Toast.LENGTH_SHORT).show();
        }
    }

    private void readTotalUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                total = getMaxBoost((int) dataSnapshot.getChildrenCount());
                max_view.setText("Max Views = " + total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int getMaxBoost(int total) {
        int max = (int) (75.0 / 100 * total);
        return max;
    }

    private void deductAmount(float amount) {

        final String URL = "https://ramaeloghar.herokuapp.com/deduct/amount";
        int id = sessionManagement.getId();
        JSONObject params = new JSONObject();
        try {
            params.put("id", id);
            params.put("amnt", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.PUT, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    int sucess = response.getInt("sucess");
                    if (sucess == 1) {
                        String amount = response.getString("amount");
                        sessionManagement.setAMOUNT(amount);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(YtVideoBoost.this, "Failed,try again latter", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(rq);
    }


}