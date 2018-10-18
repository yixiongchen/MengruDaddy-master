package com.mengrudaddy.instagram.Profile;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    // about layout elements
    private CircleImageView profile_pic;
    private EditText changed_name, changed_description;
    private Button btn_changePic, btn_cancle, btn_ok;
    private Context context = EditProfileActivity.this;
    private static final String TAG = "EditProfileActivity";
    private Bitmap original_new_pic, resize;

    // about activity result code
    private Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int ALBUM_REQUEST_CODE = 1;

    // about database and storage connection
    private FirebaseStorage storage;
    private DatabaseReference UserDatabaseRef;
    private DatabaseReference DatabaseRef;
    private FirebaseUser authUser;
    private StorageReference imageReference, thumbReference;
    private FirebaseDatabase database;
    private ProgressBar progressBar;
    private ValueEventListener mPostListener;
    private InputStream thumbStream;
    private Bitmap thumbBitmap;
    private byte[] thumbData;
    private String imagePath;
    private String new_username, new_description, new_profile_pic, username, description;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //View initialization
        changed_name = (EditText) findViewById(R.id.change_name);
        profile_pic = (CircleImageView) findViewById(R.id.profile_image);
        changed_description = (EditText) findViewById(R.id.change_description);
        btn_changePic = (Button) findViewById(R.id.change_image);
        btn_cancle = (Button) findViewById(R.id.cancle);
        btn_ok = (Button) findViewById(R.id.ok);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // For fire base connection
        database = FirebaseDatabase.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        UserDatabaseRef = database.getReference("users/" + authUser.getUid());
        final String uID = authUser.getUid();
        changed_name.setText(authUser.getDisplayName());

        DatabaseRef = database.getReference();
        String profilePicRef = "profile_pic/";

        storage = FirebaseStorage.getInstance();
        imageReference = storage.getReference(profilePicRef).child(uID);

        btn_changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int activity_alum = 0;
                final int activity_camera = 1;

                // a dialog ask user the way of changing profile pic
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new CharSequence[]
                                {"Choose from album", "Take a new photo"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                switch (which) {
                                        // on click 'choose form album'
                                    case activity_alum:
                                        Log.d(TAG, "onClick: choose from album");
                                        changeImage();
                                        break;
                                        // on click take a new photo by opening camera
                                    case activity_camera:
                                        Log.d(TAG, "onClick: take a new photo");
                                        takePicture();
                                        break;
                                }
                            }
                        });

                builder.show();
            }
        });

        // finish activity
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // upload changes
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //write profile info to database
                if (imagePath != null) {
                    uploadImage();
                }
                new_description = changed_description.getText().toString();
                new_username = changed_name.getText().toString();
                new_profile_pic = uID;
                updateUser(authUser.getUid());

            }
        });

    }

    public void updateUser(String userid) {
        DatabaseReference ref = DatabaseRef.child("users/").child(userid);
        Map<String, Object> updates = new HashMap<>();
        if (new_description.length() > 0) {
            updates.put("description", new_description);
        }
        if (new_username.length() > 0) {
            updates.put("username", new_username);
        }
        if (imagePath != null) {
            updates.put("image", new_profile_pic);
        }

        // if updates size > 0 then we start update user info on database
        if (updates.size() > 0) {
            ref.updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Your user information updated", Toast.LENGTH_SHORT).show();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Intent i = new Intent(context, ProfileActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                    });
        }
    }

    private void changeImage() {
        // check permission
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // if permission granted then we can pick a pic from album
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, ALBUM_REQUEST_CODE);
                        } else {
                            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //camera
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (imageUri != null) {
                Log.d(TAG, "Image Url is" + imageUri);
                try {
                    // resolve file provider from input stream
                    InputStream ims = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    Log.d(TAG, "onActivityResult: " + bitmap);
                    // load image on circle view
                    resize = getResizedBitmap(bitmap, 300, 300);
                    loadImage(resize);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // if pic is chosen from gallery
        if (requestCode == ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imagePath = getPath(this.getApplicationContext(), imageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            resize = getResizedBitmap(bitmap, 300, 300);
            loadImage(resize);
        }
    }

    // get path for pics chosen from gallery
    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    public void takePicture() {
        //access external storage
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "MobileIns";
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        //create image file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String file = "Ins" + timeStamp + ".jpg";
        File photoFile = new File(destDir, file);

        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile);
            //imageUri = Uri.fromFile(photoFile);
            imagePath = Uri.fromFile(photoFile).getPath();
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }

    }

    //resize bitmap
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private void loadImage(Bitmap bitmap) {
        //orginal = BitmapUtils.getBitmapFromAssets(this,pic_name,300,30);
        original_new_pic = bitmap;
        profile_pic.setImageBitmap(original_new_pic);
    }

    private void uploadImage() {
        if (imageReference != null) {
            progressBar.setVisibility(View.VISIBLE);
            //disable user interaction
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //InputStream stream = new FileInputStream(new File(imagePath));
            Uri filepath = Uri.fromFile(new File(imagePath));
            imageReference.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Your profile image updated", Toast.LENGTH_SHORT).show();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Intent i = new Intent(context, ProfileActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(EditProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        /*

         */


    }

    @Override
    public void onStart() {
        super.onStart();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User newUser = dataSnapshot.getValue(User.class);
                username = newUser.username;
                description = newUser.description;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        UserDatabaseRef.addValueEventListener(userListener);
        mPostListener = userListener;
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove post value event listener
        if (mPostListener != null) {
            UserDatabaseRef.removeEventListener(mPostListener);
        }
    }

}
