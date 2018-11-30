package com.hossain.zakaria.simpleblogwithfirebase.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hossain.zakaria.simpleblogwithfirebase.R;
import com.hossain.zakaria.simpleblogwithfirebase.utils.CircularProgressBar;
import com.hossain.zakaria.simpleblogwithfirebase.utils.TranslateAnim;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AccountSetupActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.account_setup_toolbar)
    Toolbar accountSetupToolbar;

    @BindView(R.id.profile_image_view)
    ImageView profileImageView;

    @BindView(R.id.account_user_name)
    EditText accountUserName;

    @BindView(R.id.account_user_designation)
    EditText accountUserDesignation;

    @BindView(R.id.save_account_setting_button)
    Button saveAccountSettingButton;

    @BindView(R.id.account_user_total_post)
    TextView accountUserTotalPost;

    @BindView(R.id.account_setup_card_view)
    CardView accountSetupCardView;

    @BindView(R.id.account_user_follow_button)
    Button accountUserFollowButton;

    private CircularProgressBar progressBar;
    private AlertDialog alertDialog = null;
    private Uri profileImageUri = null;
    private Bitmap compressedImageBitmap;

    private static final int REQUEST_READ_EXTERNAL_PERMISSION_CODE = 4512;
    private boolean isChangedProfileImage = false;
    private String userId;

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        ButterKnife.bind(this);

        setSupportActionBar(accountSetupToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("User Account");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        accountSetupToolbar.setNavigationIcon(R.drawable.ic_navigation_back_icon);

        setAnimatedTranslatedView();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        progressBar = new CircularProgressBar(this);

        userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        getUserAccountSettingsData();
    }

    private void setAnimatedTranslatedView() {
        TranslateAnim.setAnimation(profileImageView, this, "top");
        TranslateAnim.setAnimation(accountUserName, this, "left");
        TranslateAnim.setAnimation(accountUserDesignation, this, "right");
        TranslateAnim.setAnimation(saveAccountSettingButton, this, "left");
        TranslateAnim.setAnimation(accountSetupCardView, this, "right");
        TranslateAnim.setAnimation(accountUserFollowButton, this, "bottom");
    }

    @OnClick(R.id.profile_image_view)
    public void onViewClicked() {
        chooseImageForProfile();
    }

    @OnClick(R.id.save_account_setting_button)
    public void onViewClickedSaveAccountSetting() {
        final String userName = accountUserName.getText().toString();
        final String userDesignation = accountUserDesignation.getText().toString();

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userDesignation) && profileImageUri != null) {
            if (isChangedProfileImage) {
                alertDialog = progressBar.setCircularProgressBar();

                final StorageReference profileImagePath = storageReference.child("userImageUrl").child(userId + ".jpg");

                final File profileImageFile = new File(Objects.requireNonNull(profileImageUri.getPath()));
                try {
                    compressedImageBitmap = new Compressor(this)
                            .setMaxWidth(100)
                            .setMaxHeight(100)
                            .setQuality(1)
                            .compressToBitmap(profileImageFile);
                } catch (IOException e) {
                    Toast.makeText(AccountSetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] profileImageUriByte = baos.toByteArray();

                UploadTask uploadTask = profileImagePath.putBytes(profileImageUriByte);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                storeDataToFireStore(uri, userName, userDesignation);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AccountSetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });

                /*profileImagePath.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                storeDataToFireStore(uri, userName);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                alertDialog.dismiss();
                                Toast.makeText(AccountSetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.dismiss();
                        Toast.makeText(AccountSetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });*/

            } else {
                alertDialog = progressBar.setCircularProgressBar();
                storeDataToFireStore(null, userName, userDesignation);
            }
        } else {
            Toast.makeText(this, "Name, designation and image filled can not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @AfterPermissionGranted(REQUEST_READ_EXTERNAL_PERMISSION_CODE)
    private void chooseImageForProfile() {

        //String[] perms = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
        String perms = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (EasyPermissions.hasPermissions(this, perms)) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(AccountSetupActivity.this);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_read_external_storage_rational), REQUEST_READ_EXTERNAL_PERMISSION_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //need this condition for jump to app permission setting
        }

        //after cropping activity we get the cropped image from here
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                if (result != null) {
                    profileImageUri = result.getUri();

                    Glide.with(this)
                            .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_image_profile))
                            .load(profileImageUri)
                            .into(profileImageView);

                    isChangedProfileImage = true;
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = Objects.requireNonNull(result).getError();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getUserAccountSettingsData() {
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    alertDialog = progressBar.setCircularProgressBar();

                    if (Objects.requireNonNull(task.getResult()).exists()) {
                        String username = task.getResult().getString("userName");
                        String userDesignation = task.getResult().getString("userDesignation");
                        String profileImageUrl = task.getResult().getString("userImageUrl");

                        profileImageUri = Uri.parse(profileImageUrl);

                        if (username != null && userDesignation != null && profileImageUrl != null) {
                            accountUserName.setText(username);
                            accountUserDesignation.setText(userDesignation);

                            Glide.with(AccountSetupActivity.this)
                                    .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_image_profile))
                                    .load(profileImageUri)
                                    .into(profileImageView);
                        }

                        /*String totalPost = getTotalPostNumber();
                        accountUserTotalPost.setText(totalPost);*/

                        alertDialog.dismiss();

                    } else {
                        alertDialog.dismiss();
                        Toast.makeText(AccountSetupActivity.this, "Please Insert your profile image, name and designation", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AccountSetupActivity.this, "FireStore Retrieve Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void storeDataToFireStore(Uri uri, String userName, String userDesignation) {
        Uri downloadUri;

        if (uri != null) {
            downloadUri = uri;
        } else {
            downloadUri = profileImageUri;
        }

        if (downloadUri != null) {
            Map<String, String> userAccountSettingMap = new HashMap<>();
            userAccountSettingMap.put("userName", userName);
            userAccountSettingMap.put("userDesignation", userDesignation);
            userAccountSettingMap.put("userImageUrl", downloadUri.toString());

            firebaseFirestore.collection("Users").document(userId).set(userAccountSettingMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        alertDialog.dismiss();
                        Toast.makeText(AccountSetupActivity.this, "The user settings are updated", Toast.LENGTH_SHORT).show();

                        //send to main activity
                        Intent intent = new Intent(AccountSetupActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    } else {
                        alertDialog.dismiss();
                        Toast.makeText(AccountSetupActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();

        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(0, 0);
        return super.onSupportNavigateUp();
    }

   /* public String getTotalPostNumber() {
        final long[] totalPostNumber = {0};

        firebaseFirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                    for (DocumentSnapshot  documentSnapshot: queryDocumentSnapshots.getDocuments()) {
                        if (Objects.requireNonNull(documentSnapshot.getString("userId")).equals(userId)) {
                            totalPostNumber[0] += 1;
                        }
                    }
                }
            }
        });

        return String.valueOf(totalPostNumber[0]);
    }*/
}
