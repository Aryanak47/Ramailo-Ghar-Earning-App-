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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl;
    StorageReference storageReference;
    TextView post;
    ImageView close, image;
    EditText postDescription;
    SessionManagement sessionManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        post = findViewById(R.id.post);
        close = findViewById(R.id.close);
        image = findViewById(R.id.post_img);
        postDescription = findViewById(R.id.post_descrip);

        sessionManagement = new SessionManagement(getApplicationContext());
        storageReference = FirebaseStorage.getInstance().getReference("Post");
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setFixAspectRatio(false)
                        .start(PostActivity.this);
            }
        });

    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();
        final String des = postDescription.getText().toString().trim();

        if (imageUri != null) {
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()
                    + imageUri.getLastPathSegment());
            UploadTask uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = (Uri) task.getResult();
                        myUrl = downloadUrl.toString();
                        addToFireBaseDataBase(sessionManagement.getId(), des, progressDialog);
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed" + e.toString(), Toast.LENGTH_LONG).show();

                }
            });
        } else {
            if (des.length() == 0) {
                Toast.makeText(getApplicationContext(), "You have nothing to post", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                return;
            }
            addToFireBaseDataBase(sessionManagement.getId(), des, progressDialog);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            imageUri = activityResult.getUri();
            image.setImageURI(imageUri);
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }

    private void addToFireBaseDataBase(int publisher, String description, ProgressDialog progressDialog) {
        final ProgressDialog progressDialog1 = progressDialog;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requested post");
        String postId = reference.push().getKey();
        HashMap<String, Object> data = new HashMap<>();
        data.put("postId", postId);
        data.put("description", description.length() == 0 ? "" : description);
        data.put("publisher", publisher);
        data.put("publisherName", new SessionManagement(getApplicationContext()).getName());
        if (myUrl != null) {
            data.put("postImage", myUrl);
        } else {
            data.put("postImage", "");
        }
        reference.child(postId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog1.dismiss();
                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                    finish();
                }
            }
        });

    }
}