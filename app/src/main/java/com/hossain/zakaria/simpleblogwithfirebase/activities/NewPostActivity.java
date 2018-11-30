package com.hossain.zakaria.simpleblogwithfirebase.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    @BindView(R.id.new_post_toolbar)
    Toolbar newPostToolbar;

    @BindView(R.id.blog_post_image_view)
    ImageView blogPostImageView;

    @BindView(R.id.blog_new_post_description)
    EditText blogNewPostDescription;

    @BindView(R.id.publish_post_button)
    Button publishPostButton;

    private Uri postImageUri;
    private CircularProgressBar progressBar;
    private AlertDialog alertDialog;
    private Bitmap compressedImageBitmap;

    private String currentUserId;
    private String randomName;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        ButterKnife.bind(this);

        setSupportActionBar(newPostToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setAnimatedTranslatedView();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        progressBar = new CircularProgressBar(this);
    }

    private void setAnimatedTranslatedView() {
        TranslateAnim.setAnimation(blogPostImageView, this, "top");
        TranslateAnim.setAnimation(blogNewPostDescription, this, "left");
        TranslateAnim.setAnimation(publishPostButton, this, "right");
    }

    @OnClick(R.id.blog_post_image_view)
    public void onBlogPostImageViewClicked() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .start(NewPostActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //after cropping activity we get the cropped image from here
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    postImageUri = result.getUri();

                    Glide.with(this)
                            .load(postImageUri)
                            .into(blogPostImageView);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = Objects.requireNonNull(result).getError();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.publish_post_button)
    public void onPublishPostButtonClicked() {
        final String postDescription = blogNewPostDescription.getText().toString();

        if (!TextUtils.isEmpty(postDescription) && postImageUri != null) {
            alertDialog = progressBar.setCircularProgressBar();
            randomName = getRandomName();

            final StorageReference postImagePath = storageReference.child("post_images").child(randomName + ".jpg");

            postImagePath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    postImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri imageUri) {
                            String downloadPostImageUri = imageUri.toString();

                            File newImageFile = new File(Objects.requireNonNull(postImageUri.getPath()));
                            try {
                                compressedImageBitmap = new Compressor(NewPostActivity.this)
                                        .setMaxWidth(100)
                                        .setMaxHeight(100)
                                        .setQuality(2)
                                        .compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                Toast.makeText(NewPostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumbnailDataByte = baos.toByteArray();

                            storePostDataAndThumbnailToFireStore(thumbnailDataByte, downloadPostImageUri, postDescription);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            alertDialog.dismiss();
                            Toast.makeText(NewPostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    alertDialog.dismiss();
                    Toast.makeText(NewPostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Image and description fields can not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void storePostDataAndThumbnailToFireStore(byte[] thumbnailData, final String downloadPostImageUri, final String postDescription) {
        final StorageReference thumbnailImagePath = storageReference.child("post_images/thumbnail").child(randomName + ".jpg");

        UploadTask uploadTask = thumbnailImagePath.putBytes(thumbnailData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                thumbnailImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri thumbnailUri) {
                        String downloadThumbnailUri = thumbnailUri.toString();

                        //map tag name should be the same as model class (BlogPost) name
                        Map<String, Object> postMap = new HashMap<>();
                        postMap.put("userId", currentUserId);
                        postMap.put("postImageUrl", downloadPostImageUri);
                        postMap.put("postThumbnailUrl", downloadThumbnailUri);
                        postMap.put("postDescription", postDescription);
                        postMap.put("postDateAndTime", getCurrentDateAndTime());

                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    alertDialog.dismiss();
                                    Toast.makeText(NewPostActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(NewPostActivity.this, MainActivity.class));
                                    finish();
                                    overridePendingTransition(0, 0);
                                } else {
                                    alertDialog.dismiss();
                                    Toast.makeText(NewPostActivity.this, "Error " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.dismiss();
                Toast.makeText(NewPostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    private String getRandomName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
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
