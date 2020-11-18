package com.aryanapps.ramailoghar;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BoostYoutube extends AppCompatActivity {

    static final double COST_PER_LIKE = 0.15;
    TextView max_boost;
    EditText pageUrl, totalBoost;
    Dialog dialog;
    ImageView closeBtn;
    Button okBtn;
    TextView title;
    TextView msg;
    int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boost_youtube);
        max_boost = findViewById(R.id.maxSubs);
        pageUrl = findViewById(R.id.channelUrl);
        totalBoost = findViewById(R.id.totalSubscribe);
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
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscribe Request");
                int userRqst = Integer.parseInt(totalBoost.getText().toString().trim());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("url", pageUrl.getText().toString().trim());
                hashMap.put("amount", userRqst * COST_PER_LIKE);
                hashMap.put("maxBoost", userRqst);
                hashMap.put("currentBoost", 0);
                hashMap.put("userid", String.valueOf(new SessionManagement(getApplicationContext()).getId()));
                databaseReference.push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            emptyFields();
                            Toast.makeText(BoostYoutube.this, "Successfully submitted", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void boostYTBtn(View view) {
        String url = pageUrl.getText().toString().trim();
        String totalBost = totalBoost.getText().toString().trim();
        if (url.isEmpty() || totalBost.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Integer.parseInt(totalBost) > total) {
            Toast.makeText(this, "You are exceeding max boost", Toast.LENGTH_SHORT).show();
            return;
        }
        int userRqst = Integer.parseInt(totalBoost.getText().toString().trim());
        msg.setText("It costs " + userRqst * COST_PER_LIKE + "( Rs. 0.5 per subscibe)");
        dialog.show();

    }

    private void emptyFields() {
        pageUrl.setText("");
        totalBoost.setText("");
    }

    public void ytPasteBtn(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";
        if (clipboard != null && !(clipboard.hasPrimaryClip())) {
            Toast.makeText(this, "You haven't copied anything to paste", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipData.Item item = null;
        if (clipboard != null) {
            item = clipboard.getPrimaryClip().getItemAt(0);
        }
        if (item != null) {
            pasteData = (String) item.getText();
        }
        if (pasteData != null) {
            pageUrl.setText(pasteData);

        } else {
            Toast.makeText(this, "You have copied an invalid data type", Toast.LENGTH_SHORT).show();
        }
    }

    private void readTotalUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                total = getMaxBoost((int) dataSnapshot.getChildrenCount());
                max_boost.setText("Max = " + total);
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
}