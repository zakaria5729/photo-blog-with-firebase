package com.hossain.zakaria.simpleblogwithfirebase.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hossain.zakaria.simpleblogwithfirebase.R;
import com.hossain.zakaria.simpleblogwithfirebase.activities.AccountSetupActivity;
import com.hossain.zakaria.simpleblogwithfirebase.activities.CommentActivity;
import com.hossain.zakaria.simpleblogwithfirebase.models.BlogPost;
import com.hossain.zakaria.simpleblogwithfirebase.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BlogPostRecyclerViewAdapter extends RecyclerView.Adapter<BlogPostRecyclerViewAdapter.BlogPostViewHolder> {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private List<BlogPost> blogPostList;
    private List<User> userList;
    private Context context;

    public BlogPostRecyclerViewAdapter(List<BlogPost> blogPostList, List<User> userList, Context context) {
        this.blogPostList = blogPostList;
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public BlogPostViewHolder onCreateViewHolder(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_blog_post, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new BlogPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlogPostViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        final String blogPostId = blogPostList.get(position).blogPostId;
        String userId = blogPostList.get(position).getUserId();
        String postImageUrl = blogPostList.get(position).getPostImageUrl();
        String postThumbnailUrl = blogPostList.get(position).getPostThumbnailUrl();
        String postDescription = blogPostList.get(position).getPostDescription();
        String postDateAndTime = blogPostList.get(position).getPostDateAndTime();

        if (userId != null) {
            String userName = userList.get(position).getUserName();
            String userImageUrl = userList.get(position).getUserImageUrl();
            holder.setBlogUserDataAndImage(userName, userImageUrl);

            holder.setBlogPostData(postDateAndTime, postDescription);
            holder.setBlogPostImage(postImageUrl, postThumbnailUrl);

            /*Start set like feature*/
            holder.postLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postLikeOrDelete(blogPostId, currentUserId);
                }
            });

            holder.postLikeCounter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postLikeOrDelete(blogPostId, currentUserId);
                }
            });
            /*End set like feature*/

            //Get like status and change the icon
            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        holder.setLikeButtonBlue(true);
                    } else {
                        holder.setLikeButtonBlue(false);
                    }
                }
            });

            //Get like count and set the counter
            firebaseFirestore.collection("Posts").document(blogPostId).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        holder.setLikeCount(queryDocumentSnapshots.size() + " Likes");
                    } else {
                        holder.setLikeCount(0 + " Likes");
                    }
                }
            });

            //Get comment count and set the counter
            firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        holder.setCommentCount(queryDocumentSnapshots.size() + " Comments");
                    } else {
                        holder.setCommentCount(0 + " Comments");
                    }
                }
            });

            /*Start set comment feature*/
            holder.postCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToCommentActivity(blogPostId, "focusCommentEditText");
                }
            });

            holder.postCommentCounter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToCommentActivity(blogPostId, "");
                }
            });
            /*End set comment feature*/

            if (currentUserId.equals(userId)) { // checking for delete button showing
                holder.postMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(context, holder.postMoreButton);
                        popupMenu.getMenuInflater().inflate(R.menu.menu_more_button_popup, popupMenu.getMenu());

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.action_delete_post_menu:
                                        Toast.makeText(context, "delete toast", Toast.LENGTH_SHORT).show();
                                        return true;

                                    case R.id.action_edit_post_menu:
                                        Toast.makeText(context, "edit toast", Toast.LENGTH_SHORT).show();
                                        return true;

                                    default:
                                        return false;
                                }
                            }
                        });
                        popupMenu.show();
                    }
                });
            } else {
                holder.postMoreButton.setVisibility(View.GONE);
            }

        } else {
            Toast.makeText(context, "Error: your not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void postLikeOrDelete(final String blogPostId, final String currentUserId) {
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (!Objects.requireNonNull(task.getResult()).exists()) {
                    Map<String, Object> likesMap = new HashMap<>();
                    likesMap.put("likeTimeStamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);
                } else {
                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                }
            }
        });
    }

    private void goToCommentActivity(String blogPostId, String editTextFocus) {
        Intent commentIntent = new Intent(context, CommentActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        commentIntent.putExtra("focus_comment_edit_text", editTextFocus);
        context.startActivity(commentIntent);
    }

    @Override
    public int getItemCount() {
        if (blogPostList != null) {
            return blogPostList.size();
        } else {
            return 0;
        }
    }

    class BlogPostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.post_like_button)
        TextView postLikeButton;

        @BindView(R.id.post_comment_button)
        TextView postCommentButton;

        @BindView(R.id.blog_post_image)
        ImageView blogPostImage;

        @BindView(R.id.blog_user_image)
        CircleImageView blogUserImage;

        @BindView(R.id.post_more_button)
        ImageView postMoreButton;

        @BindView(R.id.post_like_counter)
        TextView postLikeCounter;

        @BindView(R.id.post_comment_counter)
        TextView postCommentCounter;

        @BindView(R.id.blog_user_name)
        TextView blogUserName;

        @BindView(R.id.blog_post_description)
        TextView blogPostDescription;

        @BindView(R.id.blog_post_date)
        TextView blogPostDate;

        BlogPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            blogUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, AccountSetupActivity.class));
                }
            });
        }

        void setBlogUserDataAndImage(String userName, String userImageUrl) {
            blogUserName.setText(userName);
            Glide.with(context)
                    .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.user_image_placeholder))
                    .load(userImageUrl)
                    .into(blogUserImage);
        }

        void setBlogPostData(String postDateAndTime, String postDescription) {
            blogPostDate.setText(postDateAndTime);
            blogPostDescription.setText(postDescription);
        }

        /*if the main url (blogPostImageUrl) is loaded with delay then glide library loaded thumbnail first then loaded the main url otherwise only loaded main url only*/
        void setBlogPostImage(String postImageUrl, String postThumbnailUrl) {
            Glide.with(context).
                    applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.post_image_placeholder))
                    .load(postImageUrl)
                    .thumbnail(Glide.with(context).load(postThumbnailUrl)) //loading thumbnail
                    .into(blogPostImage);
        }

        void setLikeButtonBlue(boolean isLiked) {
            if (isLiked) {
                postLikeButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_like_blue_icon), null, null, null);
            } else {
                postLikeButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_like_gray_icon), null, null, null);
            }
        }

        void setLikeCount(String likeCount) {
            postLikeCounter.setText(likeCount);
        }

        void setCommentCount(String commentCount) {
            postCommentCounter.setText(commentCount);
        }
    }
}
