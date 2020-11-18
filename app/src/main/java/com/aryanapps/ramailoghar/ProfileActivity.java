package com.aryanapps.ramailoghar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.aryanapps.ramailoghar.helper.Network;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    CircularImageView circularImageView;
    ImageView camera;
    TextView userName;
    TextView userEmail;
    EditText newName;
    EditText newEmail;
    TextView userLiked;
    TextView totalUserPost;
    TextView totalUserAmount;
    TextView totalPostLike;

    SessionManagement sessionManagement;
    Uri imageUri;
    StorageReference storageReference;
    String image;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        circularImageView = findViewById(R.id.profile_pic);
        camera = findViewById(R.id.camera);
        userEmail = findViewById(R.id.userEmail);
        userName = findViewById(R.id.userName);
        newName = findViewById(R.id.newName);
        newEmail = findViewById(R.id.newEmail);
        totalUserAmount = findViewById(R.id.user_amount);
        totalPostLike = findViewById(R.id.totalpostLike);
        userLiked = findViewById(R.id.totalLiked);
        totalUserPost = findViewById(R.id.totalPost);
        queue = Volley.newRequestQueue(getApplicationContext());
        storageReference = FirebaseStorage.getInstance().getReference("User Profiles");
        sessionManagement = new SessionManagement(getApplicationContext());
        setUserInfo();
        updateProfilePic();
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(ProfileActivity.this);
            }
        });
        getTotalPostLike();
        getTotalUserAmount();
        getTotalUserPost();
        getUserLiked();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            imageUri = activityResult.getUri();
            updatePhoto();

        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePhoto() {
        if (imageUri != null) {
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + imageUri.getLastPathSegment());
            Toast.makeText(getApplicationContext(), "Uploading", Toast.LENGTH_LONG).show();
            UploadTask task = reference.putFile(imageUri);
            task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        image = uri.toString();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                                .child(String.valueOf(sessionManagement.getId()));
                        HashMap<String, Object> profilePic = new HashMap<>();
                        profilePic.put("profile_pic", image);
                        databaseReference.updateChildren(profilePic).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "sucessfully uploaded", Toast.LENGTH_LONG).show();
                                    Glide.with(getApplicationContext())
                                            .load(new SessionManagement(getApplicationContext()).getProfilePic())
                                            .diskCacheStrategy(DiskCacheStrategy.ALL).into(circularImageView);
                                    setUserInfo();
                                }
                            }
                        });
                    }
                }
            });

        }
    }

    private void setUserInfo() {
        HashMap<String, String> user = sessionManagement.getUser();
        String email = user.get(sessionManagement.EMAIL);
        String name = user.get(sessionManagement.NAME);
        userName.setText(name);
        userEmail.setText(email);
        newEmail.setText(email);
        newName.setText(name);
    }

    private void updateProfilePic() {
        Glide.with(getApplicationContext())
                .load(new SessionManagement(getApplicationContext()).getProfilePic())
                .into(circularImageView);
    }


    public void onUpdateBtnClicked(View view) {
        String name = newName.getText().toString().trim();
        String email = newEmail.getText().toString().trim();
        if (name.equals("") || email.equals("")) {
            Toast.makeText(this, "Please fill the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        updateUserInfo(name, email);

    }

    private void getUserLiked() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()))
                .child("LikedPost");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userLiked.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getTotalUserPost() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(String.valueOf(sessionManagement.getId())).child("totalPost");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    totalUserPost.setText("0");
                    return;
                }
                totalUserPost.setText(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getTotalUserAmount() {
        totalUserAmount.setText(sessionManagement.getAMOUNT());
    }

    private void getTotalPostLike() {
        DatabaseReference totalLikeReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(String.valueOf(sessionManagement.getId())).child("totalPostLike");

        totalLikeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    totalPostLike.setText("0");
                    return;
                }
                totalPostLike.setText(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updatAmount();

    }

    private void updatAmount() {
        new Network(getApplicationContext()).getAmount();
        getTotalUserAmount();
    }

    private void updateUserInfo(String name, String email) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating");
        progressDialog.show();
        final String URL = "https://ramaeloghar.herokuapp.com/update";
        JSONObject params = new JSONObject();
        int id = sessionManagement.getId();
        try {
            params.put("id", id);
            params.put("email", email);
            params.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Toast.makeText(ProfileActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                    int sucess = response.getInt("sucess");
                    if (sucess == 1) {
                        String name = response.getString("name");
                        String email = response.getString("email");
                        sessionManagement.setEmail(email);
                        sessionManagement.setName(name);
                        setUserInfo();
                        progressDialog.dismiss();
                        updateFireBaseDatabase(name, email);

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, " failed try again latter", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Update failed try again latter", Toast.LENGTH_SHORT).show();
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

    private void updateFireBaseDatabase(String name, String email) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()));
        HashMap<String, Object> data = new HashMap<>();
        data.put("userName", name);
        data.put("email", email);
        databaseReference.updateChildren(data);
    }


}