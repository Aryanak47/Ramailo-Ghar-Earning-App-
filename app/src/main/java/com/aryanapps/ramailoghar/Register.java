package com.aryanapps.ramailoghar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    static int i = 1;
    Button register;
    TextView login;
    TextInputEditText user;
    TextInputEditText email;
    TextInputEditText pw;
    TextInputEditText cPw;
    ProgressBar pb;
    boolean validEmail;
    boolean validPassword;
    TextView progressLabel;
    TextView cPwError;
    Dialog dialog;
    ImageView closeBtn;
    Button okBtn;
    TextView title;
    TextView msg;
    SessionManagement sessionManagement;
    int Total;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register = findViewById(R.id.registerBtn);
        login = findViewById(R.id.loginTxt);
        user = findViewById(R.id.user);
        pw = findViewById(R.id.pass);
        email = findViewById(R.id.lemail);
        cPw = findViewById(R.id.confpass);
        pb = findViewById(R.id.progressBar);
        validEmail = false;
        validPassword = false;
        progressLabel = findViewById(R.id.progress_label);
        cPwError = findViewById(R.id.cPWLable);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.register_dialog);
        closeBtn = dialog.findViewById(R.id.regcloseBtn);
        msg = dialog.findViewById(R.id.regMsgDialog);
        title = dialog.findViewById(R.id.regTitleDialog);
        okBtn = dialog.findViewById(R.id.regDialogBtn);

        sessionManagement = new SessionManagement(getApplicationContext());
        Total = 0;
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
        email.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String em = email.getText().toString().trim();
                if (!emailValidator(em)) {
                    email.setError("Invalid email");
                    validEmail = false;
                    return;
                }
                validEmail = true;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // other stuffs
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // other stuffs
            }
        });

        cPw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        pw.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                caculation();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean notEmpty = true;
                if (user.getText().toString().length() == 0) {
                    notEmpty = false;
                    user.setError("Enter user name");
                }
                if (email.getText().toString().length() == 0) {
                    notEmpty = false;
                    email.setError("Enter email");
                }
                if (pw.getText().toString().length() == 0) {
                    notEmpty = false;
                    pw.setError("Enter Password");
                }
                if (cPw.getText().toString().length() == 0) {
                    notEmpty = false;
                    cPw.setError("Enter Password");
                }
                checkPassword();
                if (notEmpty && validEmail && validPassword) {
                    doNetworking();
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

    private void checkPassword() {
        String password = pw.getText().toString().trim();
        if (!password.equals(cPw.getText().toString())) {
            cPwError.setVisibility(View.VISIBLE);
            validPassword = false;
        } else {
            cPwError.setVisibility(View.INVISIBLE);
            validPassword = true;
        }
    }

    public boolean emailValidator(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    protected void caculation() {
        String temp = pw.getText().toString();
        i = i + 1;
        int length = 0, uppercase = 0, lowercase = 0, digits = 0, symbols = 0, bonus = 0, requirements = 0;
        int lettersonly = 0, numbersonly = 0, cuc = 0, clc = 0;
        length = temp.length();
        for (int i = 0; i < temp.length(); i++) {
            if (Character.isUpperCase(temp.charAt(i)))
                uppercase++;
            else if (Character.isLowerCase(temp.charAt(i)))
                lowercase++;
            else if (Character.isDigit(temp.charAt(i)))
                digits++;
            symbols = length - uppercase - lowercase - digits;

        }
        if (length > 7) {
            requirements++;
        }

        if (uppercase > 0) {
            requirements++;
        }

        if (lowercase > 0) {
            requirements++;
        }

        if (digits > 0) {
            requirements++;
        }

        if (symbols > 0) {
            requirements++;
        }

        if (bonus > 0) {
            requirements++;
        }

        if (digits == 0 && symbols == 0) {
            lettersonly = 1;
        }

        if (lowercase == 0 && uppercase == 0 && symbols == 0) {
            numbersonly = 1;
        }

        Total = (length * 4) + ((length - uppercase) * 2)
                + ((length - lowercase) * 2) + (digits * 4) + (symbols * 6)
                + (bonus * 2) + (requirements * 2) - (lettersonly * length * 2)
                - (numbersonly * length * 3) - (cuc * 2) - (clc * 2);


        if (Total < 30) {
            setProgress(15, getResources().getColor(R.color.weakPw), "Weak");
        } else if (Total >= 40 && Total < 55) {
            setProgress(20, getResources().getColor(R.color.fairPw), "Fair");
        } else if (Total >= 56 && Total < 70) {
            setProgress(25, getResources().getColor(R.color.goodPW), "Good");
        } else if (Total >= 71) {
            setProgress(20, getResources().getColor(R.color.strongPw), "Strong");
        }


    }

    private void doNetworking() {

        final String URL = "https://ramaeloghar.herokuapp.com/register";
        final String em = email.getText().toString().trim();
        final String passW = pw.getText().toString().trim();
        final String userr = user.getText().toString().trim();
        JSONObject data = new JSONObject();
        try {
            data.put("name", userr);
            data.put("email", em);
            data.put("password", passW);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST, URL, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Register", response.toString());
                try {
                    int sucess = response.getInt("sucess");
                    if (sucess == 1) {
                        String userName = response.getString("name");
                        String amount = response.getString("amt");
                        String email = response.getString("email");
                        int id = response.getInt("id");
                        saveDateInFirebase(id, userName, email);
                        sessionManagement.saveSession(email, userName, amount, id);
                    } else if (sucess == -1) {
                        dialog.show();

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

    private void setProgress(int value, int color, String label) {
        pb.setProgress(Total - 20);
        progressLabel.setTextColor(color);
        progressLabel.setText(label);
    }

    private void saveDateInFirebase(int id, String userName, String email) {
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(id));
        HashMap<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("email", email);
        data.put("videoAdsTarget", 13);
        data.put("totalPostLikeTarget", 100);
        data.put("totalPostILikedTarget", 100);
        data.put("totalPostILiked", 0);
        data.put("profile_pic", "https://image.flaticon.com/icons/png/512/64/64572.png");
        reference.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(Register.this, MainActivity.class));
                    finish();
                }
            }
        });
    }

}
