package com.aryanapps.ramailoghar.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aryanapps.ramailoghar.PostDetail;
import com.aryanapps.ramailoghar.R;
import com.aryanapps.ramailoghar.SessionManagement;
import com.aryanapps.ramailoghar.model.Notification;
import com.aryanapps.ramailoghar.model.Post;
import com.aryanapps.ramailoghar.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class NotificatonAdapter extends RecyclerView.Adapter<NotificatonAdapter.ViewHolder> {

    private Context context;
    private List<Notification> notifications;

    public NotificatonAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.notificaton_items,parent,false);
        return new NotificatonAdapter.ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Notification notification = notifications.get(position);
        holder.text.setText(notification.getText());
        getUserInfo(holder.userName,holder.profile_pic, Integer.parseInt(notification.getUserId()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(notification.getTimeStamp()));
        String date = new SimpleDateFormat("yyyy//MM//dd").format(calendar.getTime());
        holder.date.setText(date);

        if(notification.isIspost()) {
            holder.post.setVisibility(View.VISIBLE);
            getPostInfo(holder.post,notification.getPostId());
        }else{
            holder.post.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkClicked(notification.getTimeStamp());

                if(notification.isIspost()){
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                    editor.putString("postId",notification.getPostId());
                    editor.apply();
                    context.startActivity(new Intent(context, PostDetail.class));
                }

            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Do you want to delete this notification?");
                builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //delete
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notification")
                                .child(String.valueOf(new SessionManagement(context).getId()));
                                databaseReference.orderByChild("timeStamp").equalTo(notification.getTimeStamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String temp = dataSnapshot.getValue().toString();
                                        String key = temp.substring(1,temp.indexOf("="));
                                        databaseReference.child(key).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
               final AlertDialog dialog = builder.create();
               dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                   @Override
                   public void onShow(DialogInterface dialogInterface) {
                       dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                       dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                   }
               });
                dialog.show();

                return true;
            }
        });
        updateNotification(holder.root,notification.isClicked());

    }

    private void updateNotification(View layout,boolean isClicked) {
        if(isClicked) {
            layout.setBackgroundColor(Color.parseColor("#ffffff"));
        }
    }

    private void checkClicked(String time) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notification")
                .child(String.valueOf(new SessionManagement(context).getId()));
        databaseReference.orderByChild("timeStamp").equalTo(time).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String temp = dataSnapshot.getValue().toString();
                String key = temp.substring(1,temp.indexOf("="));
                HashMap<String,Object> data = new HashMap<>();
                data.put("clicked",true);
                databaseReference.child(key).updateChildren(data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostInfo(final ImageView postImage, final String postId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post")
                .child(postId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    Glide.with(context).load(post.getPostImage()).into(postImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView userName,text,date;
        public ImageView profile_pic,post;
        public RelativeLayout root ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            text = itemView.findViewById(R.id.comment);
            profile_pic = itemView.findViewById(R.id.profilePic);
            post = itemView.findViewById(R.id.notificationpost);
            date = itemView.findViewById(R.id.date);
            root = itemView.findViewById(R.id.rooto);
        }
    }
    private void  getUserInfo(final TextView userName,final ImageView userPhoto,final int publisherId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(publisherId));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null ){
                    Glide.with(context).load(user.getProfile_pic()).into(userPhoto);
                    userName.setText(user.getUserName());
                }else {
                    userName.setText("Anonymous");
                    Glide.with(context).load("https://image.flaticon.com/icons/png/512/64/64572.png").into(userPhoto);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
