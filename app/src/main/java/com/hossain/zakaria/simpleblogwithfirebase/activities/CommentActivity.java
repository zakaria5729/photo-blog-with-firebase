package com.hossain.zakaria.simpleblogwithfirebase.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hossain.zakaria.simpleblogwithfirebase.R;
import com.hossain.zakaria.simpleblogwithfirebase.adapters.CommentRecyclerViewAdapter;
import com.hossain.zakaria.simpleblogwithfirebase.models.Comment;
import com.hossain.zakaria.simpleblogwithfirebase.models.User;
import com.hossain.zakaria.simpleblogwithfirebase.utils.TranslateAnim;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentActivity extends AppCompatActivity {

    @BindView(R.id.comment_toolbar)
    Toolbar commentToolbar;

    @BindView(R.id.comment_field_edit_text)
    EditText commentFieldEditText;

    @BindView(R.id.comment_send_button)
    ImageView commentSendButton;

    @BindView(R.id.comment_list_recycler_view)
    RecyclerView commentListRecyclerView;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private CommentRecyclerViewAdapter commentRecyclerViewAdapter;
    private List<Comment> commentList;
    private List<User> commentUserList;

    private String currentUserId;
    private String blogPostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);

        setSupportActionBar(commentToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        commentToolbar.setNavigationIcon(R.drawable.ic_navigation_back_icon);

        setAnimatedTranslatedView();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        blogPostId = getIntent().getStringExtra("blog_post_id");

        if (getIntent().getStringExtra("focus_comment_edit_text").equals("focusCommentEditText")) {
            commentFieldEditText.requestFocus();

            if (commentFieldEditText.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }

        getCommentList();
    }

    private void setAnimatedTranslatedView() {
        TranslateAnim.setAnimation(commentListRecyclerView, this, "bottom");
        TranslateAnim.setAnimation(commentFieldEditText, this, "right");
        TranslateAnim.setAnimation(commentSendButton, this, "left");
    }

    /*Activity name added to snapshot because to ensure that when close this activity then stop the snapshot listener*/
    private void getCommentList() {
        commentList = new ArrayList<>();
        commentUserList = new ArrayList<>();
        commentListRecyclerView.setHasFixedSize(true);

        commentRecyclerViewAdapter = new CommentRecyclerViewAdapter(commentList, commentUserList, this);
        commentListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        commentListRecyclerView.setAdapter(commentRecyclerViewAdapter);

        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    for (final DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            final Comment comment = documentChange.getDocument().toObject(Comment.class);

                            String commentUserId = documentChange.getDocument().getString("commentUserId");

                            if (commentUserId != null) {
                                firebaseFirestore.collection("Users").document(commentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                                            commentUserList.add(user);
                                            commentList.add(comment);

                                            commentRecyclerViewAdapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(CommentActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

    @OnClick(R.id.comment_send_button)
    public void commentSendOnViewClicked() {
        String commentText = commentFieldEditText.getText().toString();

        if (!commentText.isEmpty()) {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("commentText", commentText);
            commentMap.put("commentUserId", currentUserId);
            commentMap.put("commentTimeStamp", getCurrentDateAndTime());

            firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        commentFieldEditText.setText("");
                    } else {
                        Toast.makeText(CommentActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please write a comment first", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDateAndTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mma", Locale.ENGLISH);
        return simpleDateFormat.format(new Date()).toLowerCase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        overridePendingTransition(0, 0);
        return super.onSupportNavigateUp();
    }
}
