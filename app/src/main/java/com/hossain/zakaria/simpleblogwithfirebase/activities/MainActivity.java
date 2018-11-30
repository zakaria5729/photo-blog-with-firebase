package com.hossain.zakaria.simpleblogwithfirebase.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hossain.zakaria.simpleblogwithfirebase.R;
import com.hossain.zakaria.simpleblogwithfirebase.fragments.AccountFragment;
import com.hossain.zakaria.simpleblogwithfirebase.fragments.HomeFragment;
import com.hossain.zakaria.simpleblogwithfirebase.fragments.NotificationFragment;
import com.hossain.zakaria.simpleblogwithfirebase.utils.TranslateAnim;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;

    @BindView(R.id.add_post_floating_action_button)
    FloatingActionButton addPostFloatingActionButton;

    @BindView(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;


    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Fragment homeFragment, notificationFragment, accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //fragment instance
        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        setSupportActionBar(mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Photo Blog");

        setAnimatedTranslatedView();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            sentToLoginActivity();
        } else {
            checkAccountSetupCompletedOrNot();
        }

        replaceFragment(homeFragment);
        getBottomNavigationItemSelection();
    }

    private void checkAccountSetupCompletedOrNot() {
        String currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (!Objects.requireNonNull(task.getResult()).exists()) {
                        startActivity(new Intent(MainActivity.this, AccountSetupActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout_menu:
                logout();
                return true;

            case R.id.action_account_setting_menu:
                sendToAccountSetupActivity();
                return true;

            default:
                return false;
        }
    }

    private void sendToAccountSetupActivity() {
        startActivity(new Intent(this, AccountSetupActivity.class));
        overridePendingTransition(0, 0);
    }

    private void logout() {
        firebaseAuth.signOut();
        sentToLoginActivity();
    }

    private void sentToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @OnClick(R.id.add_post_floating_action_button)
    public void onViewClickedAddPost() {
        startActivity(new Intent(this, NewPostActivity.class));
        overridePendingTransition(0, 0);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void getBottomNavigationItemSelection() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home_bottom_nav:
                        replaceFragment(homeFragment);
                        return true;

                    case R.id.notification_bottom_nav:
                        replaceFragment(notificationFragment);
                        return true;

                    case R.id.account_bottom_nav:
                        replaceFragment(accountFragment);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void setAnimatedTranslatedView() {
        TranslateAnim.setAnimation(bottomNavigationView, this, "left");
        TranslateAnim.setAnimation(addPostFloatingActionButton, this, "right");
    }
}
