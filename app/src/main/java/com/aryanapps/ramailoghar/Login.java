package com.aryanapps.ramailoghar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    Dialog dialog;
    ImageView closeBtn;
    Button okBtn;
    TextView title;
    TextView msg;

    Dialog net_dialog;
    Button net_okBtn;
    TextView net_title;
    TextView net_msg;

    EditText emailText;
    EditText pass;
    LinearLayout createAccount;
    TextInputLayout inputpw;
    TextInputLayout inputEmail;
    SessionManagement sessionManagement;
    View logInBtn;
    ProgressBtn progressBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logInBtn = findViewById(R.id.progress_login);
        progressBtn = new ProgressBtn(logInBtn, getResources().getString(R.string.login_txt));
        emailText = findViewById(R.id.lemail);
        pass = findViewById(R.id.lpw);
        createAccount = findViewById(R.id.createAcc);
        inputpw = findViewById(R.id.inputpw);
        inputEmail = findViewById(R.id.inputEma);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.login_dialog);
        closeBtn = dialog.findViewById(R.id.logcloseBtn);
        msg = dialog.findViewById(R.id.logMsgDialog);
        title = dialog.findViewById(R.id.logTitleDialog);
        okBtn = dialog.findViewById(R.id.logDialogBtn);

        net_dialog = new Dialog(this);
        net_dialog.setContentView(R.layout.internet_dialog);
        net_msg = net_dialog.findViewById(R.id.netMsgDialog);
        net_title = net_dialog.findViewById(R.id.netTitleDialog);
        net_okBtn = net_dialog.findViewById(R.id.netDialogBtn);
        net_dialog.setCancelable(false);
        net_dialog.setCanceledOnTouchOutside(false);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        sessionManagement = new SessionManagement(getApplicationContext());

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Login.this.isEmpyty()) {
                    progressBtn.buttonActivated(getResources().getString(R.string.log_in_progress));
                    Login.this.doNetworking();
                }
            }
        });
        if (!isConnected(getApplicationContext())) {
            net_dialog.show();
        }
    }

    private void doNetworking() {
        final String email = emailText.getText().toString().trim();
        final String password = pass.getText().toString().trim();
        final String URL = "https://ramaeloghar.herokuapp.com/signin";
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int sucess = response.getInt("sucess");
                    if (sucess == 1) {

                        String name = response.getString("name");
                        String email = response.getString("email");
                        String amount = response.getString("amt");
                        int id = response.getInt("id");
                        sessionManagement.saveSession(email, name, amount, id);
                        sessionManagement.setProfilePic();
                        startActivity(new Intent(Login.this, MainActivity.class));
                        progressBtn.buttonDeactivated(getResources().getString(R.string.log_in_deactivate));
                        finish();
                    } else if (sucess == -1) {
                        dialog.show();
                        progressBtn.buttonDeactivated(getResources().getString(R.string.log_in_deactivate));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

        };
        queue.add(rq);
    }

    private boolean isEmpyty() {
        boolean empty = false;
        if (pass.getText().toString().length() == 0) {
            inputpw.setError("Enter your password");
            empty = true;
        }
        if (emailText.getText().toString().length() == 0) {
            inputEmail.setError("Enter your email");
            empty = true;
        }
        return empty;

    }

    @Override
    protected void onStart() {
        super.onStart();

        // if user is already loged in take user to main activity
        if (sessionManagement.isLogin()) {
            startActivity(new Intent(Login.this, Splash.class));
            finish();
        }
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        } else
            return false;
    }

    public void close(View v) {
        finish();
    }
}
