package com.hossain.zakaria.simpleblogwithfirebase.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hossain.zakaria.simpleblogwithfirebase.R;
import com.hossain.zakaria.simpleblogwithfirebase.models.Comment;
import com.hossain.zakaria.simpleblogwithfirebase.models.User;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder> {

    private List<User> commentUserList;
    private List<Comment> commentList;
    private Context context;

    public CommentRecyclerViewAdapter(List<Comment> commentList, List<User> commentUserList, Context context) {
        this.commentUserList = commentUserList;
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, container, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        /*Collections.reverse(commentList);
        Collections.reverse(commentUserList);*/

        String commentUserId = commentList.get(position).getCommentUserId();
        String commentText = commentList.get(position).getCommentText();
        String commentTimeStamp = commentList.get(position).getCommentTimeStamp();

        if (commentUserId != null) {
            String commenterName = commentUserList.get(position).getUserName();
            String commenterImage = commentUserList.get(position).getUserImageUrl();

            holder.setCommentUserNameAndImage(commenterName, commenterImage);
            holder.setCommentTextAndTime(commentText, commentTimeStamp);
        }
    }

    @Override
    public int getItemCount() {
        if (commentList != null) {
            return commentList.size();
        } else {
            return 0;
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_field)
        TextView commentField;

        @BindView(R.id.comment_date_time)
        TextView commentDateTime;

        @BindView(R.id.comment_user_name)
        TextView commentUserName;

        @BindView(R.id.comment_user_image)
        ImageView commentUserImage;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setCommentTextAndTime(String commentText, String commentTimeStamp) {
            commentField.setText(commentText);
            commentDateTime.setText(commentTimeStamp);
        }

        void setCommentUserNameAndImage(String commenterName, String commenterImage) {
            commentUserName.setText(commenterName);

            Glide.with(context)
                    .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.user_image_placeholder))
                    .load(commenterImage)
                    .into(commentUserImage);
        }
    }
}
