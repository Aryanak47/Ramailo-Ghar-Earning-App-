package com.aryanapps.ramailoghar.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aryanapps.ramailoghar.R;
import com.aryanapps.ramailoghar.SessionManagement;
import com.aryanapps.ramailoghar.model.Post;
import com.aryanapps.ramailoghar.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context context;
    public List<Post> posts;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item,parent,false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull ViewHolder holder, int position) {
        final Post post = posts.get(position);
       holder.linearLayout.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_animation));
        holder.relativeLayout.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_animation));
        if(post.getDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        }else{
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
            holder.description.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_animation));
        }
        if(post.getPostImage().equals("")){
            holder.post.setVisibility(View.GONE);
        }else {
            holder.post.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getPostImage()).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.post);
            holder.post.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_scale));
            setListenerOnImage( holder.post,post.getPostId());
        }
        holder.likes.setVisibility(View.VISIBLE);
        holder.like.setVisibility(View.VISIBLE);
        holder.share.setVisibility(View.VISIBLE);

        isLikes(holder.like,post.getPostId());
        NoOfLikes(holder.likes,post.getPostId());
        if(holder.description.getText().toString().length()>= 100)
            addReadMore(holder.description.getText().toString(),holder.description);
        publisherInfo(holder.profile_img,post.getPublisher(),holder.user_name);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userId = String.valueOf(new SessionManagement(context).getId());
                final DatabaseReference userReferenced = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                        .child("LikedPost");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Like");

                if(holder.like.getTag().equals("Like")) {
//                    new Network(context).rewardUser(0.10f);
                    holder.like.setImageResource(R.drawable.ic_liked);
                    reference.child(post.getPostId()).child(userId).setValue(true);
                    userReferenced.child(post.getPostId()).setValue(true);
                    holder.like.setTag("Liked");
                    addNotification(post.getPostId(), String.valueOf(post.getPublisher()));

                    // increment total posts like by 1
                     DatabaseReference totalLikeReference = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(String.valueOf(post.getPublisher())).child("totalPostLike");
                    totalLikeReference.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            Long value = mutableData.getValue(Long.class);
                            if(value == null){
                                mutableData.setValue(1);
                            }else{
                                mutableData.setValue(value+1);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                Log.d("PostAdapter","Transaction complete");
                        }
                    });


                }else{
//                    holder.like.setImageResource(R.drawable.ic_like);
//                    holder.like.setTag("Like");
//                    reference.child(post.getPostId()).child(userId).removeValue();
//                    userReferenced.child(post.getPostId()).removeValue();
//
//                    // decrement totalPostLike by 1
//                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
//                            .getReference("Users")
//                            .child(String.valueOf(post.getPublisher())).child("totalPostLike");
//                    databaseReference.runTransaction(new Transaction.Handler() {
//                        @NonNull
//                        @Override
//                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
//                            Long value = mutableData.getValue(Long.class);
//                            if( value != null ){
//                                mutableData.setValue(value-1);
//
//                            }
//                            return Transaction.success(mutableData);
//                        }
//
//                        @Override
//                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
//
//                        }
//                    });

                }

            }
        });
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(post.getPostImage().equals("")) {
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_TEXT, holder.description.getText().toString());
                    intent.setType("text/plain");
                    context.startActivity(Intent.createChooser(intent, "Share image via"));
                    return;
                }
                Bitmap bitmap = getBitmapFromView(holder.post);
                try {
                    String des = post.getDescription().equals("")?"":post.getDescription();
                    File file = new File(context.getExternalCacheDir(),"logicchip.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, false);
                    final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_TEXT,des);
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    intent.setType("image/*");
                    context.startActivity(Intent.createChooser(intent, "Share image via"));
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView profile_img,post,like,share;
        public TextView user_name,likes,description;
        public LinearLayout linearLayout,root;
        public RelativeLayout relativeLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            linearLayout = itemView.findViewById(R.id.top_part);
            relativeLayout = itemView.findViewById(R.id.bottom_part);
            profile_img = itemView.findViewById(R.id.profile_img);
            post = itemView.findViewById(R.id.user_post);
            like = itemView.findViewById(R.id.like);
            share = itemView.findViewById(R.id.share);
            user_name = itemView.findViewById(R.id.usr_name);
            likes = itemView.findViewById(R.id.likes);
            description = itemView.findViewById(R.id.descpription);

        }
    }

    private void publisherInfo( final ImageView userProfile,int userId,final TextView userName ) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(String.valueOf(userId));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null ){
                    Glide.with(context).load(user.getProfile_pic()).into(userProfile);
                    userName.setText(user.getUserName());
                }else {
                    userName.setText("Anonymous");
                    Glide.with(context).load("https://image.flaticon.com/icons/png/512/64/64572.png").into(userProfile);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setListenerOnImage(ImageView post, final String postId) {

//        post.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
//                editor.putString("postId",postId);
//                editor.apply();
//                context.startActivity(new Intent(context, PostDetail.class));
//            }
//        });


    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void isLikes(final ImageView image, String postId) {

        final SessionManagement sessionManagement = new SessionManagement(context);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Like")
                .child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(String.valueOf(sessionManagement.getId())).exists()) {
                    image.setImageResource(R.drawable.ic_liked);
                    image.setTag("Liked");
                }else{
                    image.setImageResource(R.drawable.ic_like);
                    image.setTag("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void NoOfLikes(final TextView like, String postId) {
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Like")
                .child(postId);
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 like.setText(dataSnapshot.getChildrenCount() +" "+"likes");
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

    }

    private void addReadMore(final String text, final TextView textView) {
        SpannableString ss = new SpannableString(text.substring(0, 100) + "... read more");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                addReadLess(text, textView);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ds.setColor(context.getResources().getColor(R.color.tapPropt, context.getTheme()));
                } else {
                    ds.setColor(context.getResources().getColor(R.color.tapPropt));
                }
            }
        };
        ss.setSpan(clickableSpan, ss.length() - 10, ss.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void addReadLess(final String text, final TextView textView) {
        SpannableString ss = new SpannableString(text + " read less");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                addReadMore(text, textView);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ds.setColor(context.getResources().getColor(R.color.tapPropt, context.getTheme()));
                } else {
                    ds.setColor(context.getResources().getColor(R.color.tapPropt));
                }
            }
        };
        ss.setSpan(clickableSpan, ss.length() - 10, ss.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private void addNotification(String postId,String publisherId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Notification").child(publisherId);
        String time =""+System.currentTimeMillis();
        HashMap <String,Object> hashMap = new HashMap<>();
        hashMap.put("userId",String.valueOf(new SessionManagement(context).getId()));
        hashMap.put("postId",postId);
        hashMap.put("ispost",true);
        hashMap.put("text","liked your post");
        hashMap.put("isSeen",false);
        hashMap.put("clicked",false);
        hashMap.put("timeStamp",time);
        databaseReference.push().setValue(hashMap);

    }





}
