package com.matt.firebasestorage;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    EditText etTitle, etDesc;
    CardView cardView;
    MaterialButton btnupload;
    Uri FilePathUri;
    int Image_Request_Code = 7;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userID;
    private byte[] compressed_byte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Post Your Work");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference("Images").child(userID);
        databaseReference = FirebaseDatabase.getInstance().getReference("Images");
        progressDialog = new ProgressDialog(MainActivity.this);
        imageView = findViewById(R.id.imageview);
        btnupload = findViewById(R.id.upload);
        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);
            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });
    }

    public void UploadImage() {

        final String titleT = etTitle.getText().toString().trim();

        if (FilePathUri != null && !titleT.matches("")) {
            btnupload.setEnabled(false); // disable button
            progressDialog.setMessage("Uploading. Please wait...");
            progressDialog.show();

            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));
            // supposed to be putFile but using putBytes for compressed data
            fileReference.putBytes(compressed_byte)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // this method captures the download link for the uploaded image
                            Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    /*String image download link*/

                                    String photoStringLink = uri.toString();
                                    String currentDate = new SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault()).format(new Date());
                                    String desc = etDesc.getText().toString().trim();

                                    // send upload info to realtime database mapped with image link
                                    Blog upload = new Blog(titleT, desc, photoStringLink, userID, currentDate);
                                    String uploadId = databaseReference.push().getKey();
                                    databaseReference.child(uploadId).setValue(upload);
                                }
                            });


                            progressDialog.dismiss();
                            btnupload.setEnabled(true); // re-enable button
                            finish();
                            Toast.makeText(getApplicationContext(), "All Done!", Toast.LENGTH_LONG).show();

                           /* Snackbar snackbar = Snackbar
                                    .make(findViewById(android.R.id.content), "Confirm delete?", Snackbar.LENGTH_LONG)
                                    .setAction("YES", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Snackbar mSnackbar = Snackbar.make(findViewById(android.R.id.content), "Message successfully deleted.", Snackbar.LENGTH_SHORT);
                                            mSnackbar.show();
                                        }
                                    });

                            snackbar.show();*/

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                }
            });
        } else {

            Toast.makeText(MainActivity.this, "Please Select Image and Add Post Name", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();

            Compression(FilePathUri);

            // puts file into image view
            Picasso.with(this).load(FilePathUri)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }
    }

    private void Compression(Uri uri) {
        // compress file uri to bytes
        File actualImage;
        Bitmap compressedImageBitmap = null;
        try {
            actualImage = FileUtil.from(this, uri);
            compressedImageBitmap = new Compressor(this)
                    .setMaxWidth(400)
                    .setMaxWidth(400)
                    .setQuality(50)
                    .compressToBitmap(actualImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
        compressed_byte = byteArrayOutputStream.toByteArray();
    }

    public String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }
}