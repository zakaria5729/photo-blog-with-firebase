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

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.welcome_reg_icon)
    ImageView welcomeRegIcon;

    @BindView(R.id.reg_email)
    EditText regEmail;

    @BindView(R.id.reg_password)
    EditText regPassword;

    @BindView(R.id.confirm_password)
    EditText confirmPassword;

    @BindView(R.id.reg_button)
    Button regButton;

    @BindView(R.id.already_account_button)
    Button alreadyAccountButton;

    @BindView(R.id.reg_show_password_checkbox)
    AppCompatCheckBox regShowPasswordCheckbox;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        setAnimatedTranslatedView();
        showOrHidePasswordField();

        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setAnimatedTranslatedView() {
        TranslateAnim.setAnimation(welcomeRegIcon, this, "top");
        TranslateAnim.setAnimation(regEmail, this, "left");
        TranslateAnim.setAnimation(regPassword, this, "right");
        TranslateAnim.setAnimation(confirmPassword, this, "left");
        TranslateAnim.setAnimation(regShowPasswordCheckbox, this, "right");
        TranslateAnim.setAnimation(regButton, this, "bottom");
        TranslateAnim.setAnimation(alreadyAccountButton, this, "bottom");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    @OnClick(R.id.reg_button)
    public void onViewClicked() {
        String email = regEmail.getText().toString();
        String password = regPassword.getText().toString();
        String confirm_password = confirmPassword.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirm_password)) {
            if (password.equals(confirm_password)) {
                CircularProgressBar progressBar = new CircularProgressBar(this);
                final AlertDialog alertDialog = progressBar.setCircularProgressBar();

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (alertDialog != null) {
                            if (task.isSuccessful()) {
                                alertDialog.dismiss();
                                Intent intent = new Intent(RegisterActivity.this, AccountSetupActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(0, 0);
                            } else {
                                alertDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Password and confirm password mismatched", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "All filled must be filled up", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @OnClick(R.id.already_account_button)
    public void onViewClickedHaveAnAccount() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void showOrHidePasswordField() {
        regShowPasswordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    regPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    regPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
