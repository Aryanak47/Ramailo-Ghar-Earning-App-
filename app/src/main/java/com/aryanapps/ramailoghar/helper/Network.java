package com.aryanapps.ramailoghar.helper;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.aryanapps.ramailoghar.R;
import com.aryanapps.ramailoghar.SessionManagement;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class Network {
    private Context context;
    private SessionManagement sessionManagement;
    private RequestQueue queue ;
    private KonfettiView konfettiView;
    private MediaPlayer mediaPlayer;

    public Network (Context context , KonfettiView konfettiView ) {
        this.context = context;
        this.sessionManagement = new SessionManagement(this.context);
        this.queue =  Volley.newRequestQueue(context);
        this.konfettiView = konfettiView;
        mediaPlayer = MediaPlayer.create(context, R.raw.music);
    }
    public Network(Context context){
        this(context,null);
    }
    public void rewardUser(float amount){

        final String URL = "https://ramaeloghar.herokuapp.com/reward";
        int id  = sessionManagement.getId();
        JSONObject params = new JSONObject();
        try {
            params.put("id", id);
            params.put("reward",amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.PUT, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    String amount = response.getString("amount");
                    sessionManagement.setAMOUNT(amount);

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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(rq);
    }
        public  void updateAmount(final boolean loadParticle) {
        final String URL = "https://ramaeloghar.herokuapp.com/amount";
        int id  = sessionManagement.getId();
        JSONObject params = new JSONObject();
        try {
            params.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.PUT, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    String amount = response.getString("amount");
                    sessionManagement.setAMOUNT(amount);
                    if(loadParticle){
                        mediaPlayer.start();
                        loadParticles();
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
    public void stopNetworking() {
        queue.stop();

    }

    private void loadParticles() {
        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 5000L);
    }
    public  HashMap<String,String> getUserInfo(int userId) {
        final HashMap<String,String> user = new HashMap<>();
        final  String URL = "https://ramaeloghar.herokuapp.com/user";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            params.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    int sucess = response.getInt("sucess");
                    if( sucess == 1 ) {
                         String name =  response.getString("name");
                         String email =  response.getString("email");
                         String amt =  response.getString("amt");
                         user.put("name",name);
                         user.put("email",email);
                         user.put("amount",amt);

                    }
                    else if(sucess == -1) {
                        Toast.makeText(context,"wrong id",Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context,"fail",Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,"fail"+error.toString(),Toast.LENGTH_LONG).show();
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
        return user;
    }
    public void getAmount() {
        final  String URL = "https://ramaeloghar.herokuapp.com/profile";
        JSONObject params = new JSONObject();
        int id  = sessionManagement.getId();
        try {
            params.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String amount = response.getString("amt");
                    sessionManagement.setAMOUNT(amount);

                } catch (JSONException e) {
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
        rq.setRetryPolicy(new DefaultRetryPolicy(
                1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(rq);
    }
    public  void getRank() {
        final String URL = "https://ramaeloghar.herokuapp.com/rank";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            params.put("userId", sessionManagement.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int sucess = response.getInt("sucess");
                    if (sucess == 1) {
                        String rank = response.getString("rank");
                        sessionManagement.setRank(rank);
                    } else if (sucess == -1) {

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


}








