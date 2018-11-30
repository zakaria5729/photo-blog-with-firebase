package com.hossain.zakaria.simpleblogwithfirebase.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hossain.zakaria.simpleblogwithfirebase.R;
import com.hossain.zakaria.simpleblogwithfirebase.activities.AccountSetupActivity;
import com.hossain.zakaria.simpleblogwithfirebase.adapters.BlogPostRecyclerViewAdapter;
import com.hossain.zakaria.simpleblogwithfirebase.models.BlogPost;
import com.hossain.zakaria.simpleblogwithfirebase.models.User;
import com.hossain.zakaria.simpleblogwithfirebase.utils.CircularProgressBar;
import com.hossain.zakaria.simpleblogwithfirebase.utils.TranslateAnim;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private RecyclerView blogPostRecyclerView;
    private BlogPostRecyclerViewAdapter blogPostRecyclerViewAdapter;
    private DocumentSnapshot lastVisibleSnapshot;

    private List<User> userList;
    private List<BlogPost> blogPostList;
    private boolean isFirstPagePostAlreadyLoaded;

    //private FloatingActionButton floatingActionButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userList = new ArrayList<>();
        blogPostList = new ArrayList<>();
        blogPostRecyclerView = view.findViewById(R.id.blog_post_recycler_view);

        blogPostRecyclerViewAdapter = new BlogPostRecyclerViewAdapter(blogPostList, userList, container.getContext());
        blogPostRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blogPostRecyclerView.setAdapter(blogPostRecyclerViewAdapter);

        if (firebaseAuth.getCurrentUser() != null) {
            blogPostRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean reachedBottom = !recyclerView.canScrollVertically(1); //if can not possible to scroll vertically then it will return false

                    if (reachedBottom) {
                        loadMorePost();
                    }
                }
            });

            loadFirstFewPost();
        }

        return view;
    }

    private void loadFirstFewPost() {
        CircularProgressBar progressBar = new CircularProgressBar(getContext());
        final android.support.v7.app.AlertDialog alertDialog = progressBar.setCircularProgressBar();

        if (firebaseAuth.getCurrentUser() != null) {
            Query firstDataQuery = firebaseFirestore.collection("Posts")
                    .orderBy("postDateAndTime", Query.Direction.DESCENDING)
                    .limit(5);

            alertDialog.dismiss();

            firstDataQuery.addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                        if (!isFirstPagePostAlreadyLoaded) {
                            lastVisibleSnapshot = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() - 1);

                            userList.clear();
                            blogPostList.clear();
                        }

                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = documentChange.getDocument().getId();
                                final BlogPost blogPost = documentChange.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                String blogUserId = documentChange.getDocument().getString("userId");
                                if (blogUserId != null) {
                                    firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                User user = Objects.requireNonNull(task.getResult()).toObject(User.class);

                                                if (!isFirstPagePostAlreadyLoaded) {
                                                    userList.add(user);
                                                    blogPostList.add(blogPost);
                                                } else {
                                                    userList.add(0, user);
                                                    blogPostList.add(0, blogPost);
                                                }
                                                blogPostRecyclerViewAdapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(getContext(), "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }

                        isFirstPagePostAlreadyLoaded = true;
                    }
                }
            });
        }
    }

    private void loadMorePost() {
        CircularProgressBar progressBar = new CircularProgressBar(getContext());
        final android.support.v7.app.AlertDialog alertDialog = progressBar.setCircularProgressBar();

        if (firebaseAuth.getCurrentUser() != null) {
            Query nextDataQuery = firebaseFirestore.collection("Posts")
                    .orderBy("postDateAndTime", Query.Direction.DESCENDING)
                    .startAfter(lastVisibleSnapshot)
                    .limit(5);

            alertDialog.dismiss();

            nextDataQuery.addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        lastVisibleSnapshot = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = documentChange.getDocument().getId();
                                final BlogPost blogPost = documentChange.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                String blogUserId = documentChange.getDocument().getString("userId");
                                if (blogUserId != null) {

                                    firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                User user = Objects.requireNonNull(task.getResult()).toObject(User.class);

                                                userList.add(user);
                                                blogPostList.add(blogPost);

                                                blogPostRecyclerViewAdapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(getContext(), "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    /*@Override
    public void onDetach() {
        // fragment remove from activity
        super.onDetach();

        isFirstPagePostAlreadyLoaded = false;
    }*/
}
