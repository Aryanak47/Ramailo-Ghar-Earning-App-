package com.aryanapps.ramailoghar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class WithDraw extends AppCompatActivity {


    final int WITHDRAW_AMOUNT = 200;
    Dialog dialog;
    ImageView closeBtn;
    Button okBtn;
    LottieAnimationView img;
    TextView title;
    Dialog sucess_dialog;
    ImageView sucess_closeBtn;
    Button sucess_okBtn;
    LottieAnimationView sucess_done_animation;
    Dialog try_dialog;
    ImageView try_closeBtn;
    Button try_okBtn;
    TextView try_title;
    TextView try_msg;
    SessionManagement sessionManagement;
    EditText amount;
    EditText paymentID;
    RequestQueue queue;
    View withdrawBtn;
    ProgressBtn progressBtn;
    HorizontalScrollView horizontalScrollView;
    RadioButton radioButton;
    RadioGroup radioGroup;
    TextView label;
    LinearLayout paymentLayout;
    boolean paymentSelected;
    LottieAnimationView moneyAnimation;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_draw);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Withdraw");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WithDraw.this, MainActivity.class));
                finish();
            }
        });

        radioGroup = findViewById(R.id.radiobtns);
        radioButton = findViewById(R.id.radio_esewa);
        horizontalScrollView = findViewById(R.id.scroll);
        scroll();
        onPaymentSelect();
        paymentLayout = findViewById(R.id.parent_layout);
        label = findViewById(R.id.label);
        paymentSelected = false;
        moneyAnimation = findViewById(R.id.money_stack);
        withdrawBtn = findViewById(R.id.withdrawbtn);
        progressBtn = new ProgressBtn(withdrawBtn, getResources().getString(R.string.withdraw_btn));
        sessionManagement = new SessionManagement(getApplicationContext());
        queue = Volley.newRequestQueue(getApplicationContext());

        // transaction failed dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.transaction_failed);
        closeBtn = dialog.findViewById(R.id.failclose_btn);
        img = dialog.findViewById(R.id.animation_view);
        title = dialog.findViewById(R.id.fail_dialog);
        okBtn = dialog.findViewById(R.id.failBtn);

        // try again dialog
        try_dialog = new Dialog(this);
        try_dialog.setContentView(R.layout.try_again_dialog);
        try_closeBtn = try_dialog.findViewById(R.id.transcloseBtn);
        try_title = try_dialog.findViewById(R.id.transTitleDialog);
        try_msg = try_dialog.findViewById(R.id.transMsgDialog);
        try_okBtn = try_dialog.findViewById(R.id.transDialogBtn);

        // transaction sucess dialog
        sucess_dialog = new Dialog(this);
        sucess_dialog.setContentView(R.layout.trans_sucess);
        sucess_closeBtn = sucess_dialog.findViewById(R.id.sucess_close_btn);
        sucess_done_animation = sucess_dialog.findViewById(R.id.animation_view);
        sucess_okBtn = sucess_dialog.findViewById(R.id.sucessBtn);

        sucess_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        try_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        withdrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWithDrawBtnClicked();
            }
        });

    }

    private void onWithDrawBtnClicked() {

        if (!paymentSelected) {
            Toast.makeText(getApplicationContext(), "Please select payment method", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isInValid()) {
            progressBtn.buttonActivated("Processing...");
            String temp = sessionManagement.getAMOUNT();
            double amnt = Double.parseDouble(temp);
            double enteredAmount = Double.parseDouble(amount.getText().toString().trim());
            if (enteredAmount > amnt) {
                Toast.makeText(getApplicationContext(), "You don't have enough money!", Toast.LENGTH_SHORT).show();
                progressBtn.buttonDeactivated(getResources().getString(R.string.withdraw_btn));
                return;
            }
            //check amount is greater than or equals to 100 rs or not for transaction
            if (amnt >= WITHDRAW_AMOUNT && enteredAmount >= WITHDRAW_AMOUNT && enteredAmount <= amnt) {
                doNetworning();
            } else {
                dialog.show();
                progressBtn.buttonDeactivated(getResources().getString(R.string.withdraw_btn));
            }
            emptyInputFields();
        }
    }

    private void doNetworning() {

        final String URL = "https://ramaeloghar.herokuapp.com/deduct";
        final int id = sessionManagement.getId();
        final String gmail = sessionManagement.getEmail();
        final int amnt = Integer.parseInt(amount.getText().toString().trim());
        final String accountId = paymentID.getText().toString().trim();
        final String paymentMethod = label.getText().toString();
        final String currentAmt = sessionManagement.getAMOUNT();

        JSONObject params = new JSONObject();
        try {
            params.put("id", id);
            params.put("amnt", amnt);
            params.put("currentAmount", currentAmt);
            params.put("accountId", accountId);
            params.put("method", paymentMethod);
            params.put("gmail", gmail);

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
                        // transcation done  dialog
                        sucess_dialog.show();
                        progressBtn.buttonDeactivated(getResources().getString(R.string.withdraw_btn));
                        addNotification(id);
                        addtoFirebase(id, amnt, currentAmt, accountId, paymentMethod);
                    } else if (sucess == -1) {
                        //transaction failed
                        progressBtn.buttonDeactivated(getResources().getString(R.string.withdraw_btn));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBtn.buttonDeactivated(getResources().getString(R.string.withdraw_btn));
                    try_dialog.show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBtn.buttonDeactivated(getResources().getString(R.string.withdraw_btn));
                try_dialog.show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(rq);

    }

    private void addtoFirebase(int id, int amnt, String currentAmt, String accountId, String paymentMethod) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("WithDrawRequest");
        String time = "" + System.currentTimeMillis();
        String withdrawId = databaseReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", id);
        hashMap.put("id", withdrawId);
        hashMap.put("requestAmount", amnt);
        hashMap.put("totalAmount", currentAmt);
        hashMap.put("accountId", accountId);
        hashMap.put("time", time);
        hashMap.put("paid", false);
        hashMap.put("visible", true);
        hashMap.put("paymentMethod", paymentMethod);
        databaseReference.child(withdrawId).setValue(hashMap);
    }

    private void addNotification(int id) {
        String message = "Your requested money is on pending,you will be paid within three business day";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Notification").child(String.valueOf(id));
        String time = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", "203");
        hashMap.put("postId", "");
        hashMap.put("ispost", false);
        hashMap.put("text", message);
        hashMap.put("isSeen", false);
        hashMap.put("clicked", false);
        hashMap.put("timeStamp", time);
        databaseReference.push().setValue(hashMap);
    }

    public void dissmissDialog(View view) {
        dialog.dismiss();
        sucess_dialog.dismiss();
        try_dialog.dismiss();
    }

    private boolean isInValid() {
        boolean Invalid = false;
        int len = paymentID.getText().toString().length();
        if (len == 0) {
            paymentID.setError("Enter your ID");
            Invalid = true;
        }

        if (amount.getText().toString().length() == 0) {
            amount.setError("Enter amount");
            Invalid = true;
        }
        return Invalid;

    }

    private void emptyInputFields() {
        paymentID.setText("");
        amount.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        queue.stop();
    }

    private void scroll() {
        horizontalScrollView.postDelayed(new Runnable() {

            @Override
            public void run() {
                horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 2000);
    }

    public void onPaymentSelect() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                label.setVisibility(View.VISIBLE);
                View child = null;
                switch (checkedId) {
                    case R.id.radio_esewa:
                        label.setText(R.string.esewa_id_txt);
                        child = getLayoutInflater().inflate(R.layout.esewa_layout, null);
                        break;
                    case R.id.radio_paypal:
                        label.setText(R.string.paypal_id_txt);
                        child = getLayoutInflater().inflate(R.layout.paypal_layout, null);
                        break;
                    case R.id.radio_paytm:
                        label.setText(R.string.paytm_id_txt);
                        child = getLayoutInflater().inflate(R.layout.paytm_layout, null);
                        break;
                    case R.id.radio_master:
                        label.setText("");
                        moneyAnimation.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "We are working on it try another", Toast.LENGTH_SHORT).show();
                        paymentLayout.removeAllViews();
                        return;

                }
                if (child == null) {
                    child = getLayoutInflater().inflate(R.layout.esewa_layout, null);
                }
                paymentLayout.removeAllViews();
                paymentLayout.addView(child);
                moneyAnimation.setVisibility(View.GONE);
                amount = child.findViewById(R.id.amtinput);
                paymentID = child.findViewById(R.id.input);
                paymentSelected = true;

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(WithDraw.this, MainActivity.class));
        finish();
    }
}
