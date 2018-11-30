package com.hossain.zakaria.simpleblogwithfirebase.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hossain.zakaria.simpleblogwithfirebase.R;
import com.hossain.zakaria.simpleblogwithfirebase.utils.CircularProgressBar;
import com.hossain.zakaria.simpleblogwithfirebase.utils.TranslateAnim;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.welcome_login_icon)
    ImageView welcomeLoginIcon;

    @BindView(R.id.login_email)
    EditText loginEmail;

    @BindView(R.id.login_password)
    EditText loginPassword;

    @BindView(R.id.login_button)
    Button loginButton;

    @BindView(R.id.login_reg_button)
    Button loginRegButton;

    @BindView(R.id.forgot_password)
    TextView forgotPassword;

    @BindView(R.id.login_show_password_checkbox)
    AppCompatCheckBox loginShowPasswordCheckbox;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setAnimatedTranslatedView();
        showOrHidePasswordField();

        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setAnimatedTranslatedView() {
        TranslateAnim.setAnimation(welcomeLoginIcon, this, "top");
        TranslateAnim.setAnimation(loginEmail, this, "left");
        TranslateAnim.setAnimation(loginPassword, this, "right");
        TranslateAnim.setAnimation(loginShowPasswordCheckbox, this, "left");
        TranslateAnim.setAnimation(loginButton, this, "bottom");
        TranslateAnim.setAnimation(forgotPassword, this, "bottom");
        TranslateAnim.setAnimation(loginRegButton, this, "bottom");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    @OnClick(R.id.login_button)
    public void onViewClicked() {
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            CircularProgressBar progressBar = new CircularProgressBar(this);
            final AlertDialog alertDialog = progressBar.setCircularProgressBar();

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    if (alertDialog != null) {
                        if (task.isSuccessful()) {
                            alertDialog.dismiss();
                            sendToMainActivity();
                        } else {
                            alertDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Empty email and password", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @OnClick(R.id.login_reg_button)
    public void onRegViewClicked() {
        startActivity(new Intent(this, RegisterActivity.class));
        overridePendingTransition(0, 0);
    }


    private void showOrHidePasswordField() {
        loginShowPasswordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                }
            }
        });
    }
}
