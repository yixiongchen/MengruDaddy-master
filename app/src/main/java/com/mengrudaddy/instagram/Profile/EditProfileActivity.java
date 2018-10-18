package com.mengrudaddy.instagram.Profile;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
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
import com.mengrudaddy.instagram.Camera.UploadActivity;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.Permission;

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
    private CircleImageView profile_pic;
    private EditText changed_name,changed_description;
    private Button btn_changePic,btn_cancle,btn_ok;
    private Context context = EditProfileActivity.this;
    private static final String TAG = "EditProfileActivity";
    private Bitmap original_new_pic,resize;
    private String new_username, new_description, new_profile_pic,username,description;

    private static final int VERIFY_PERMISSIONS_REQUEST = 1;


    private Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int ALBUM_REQUEST_CODE = 1;

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


        if(!checkPermissionsArray(Permission.PERMISSIONS)){
            verifyPermissions(Permission.PERMISSIONS);
        }

        // For fire base connection
        database = FirebaseDatabase.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        UserDatabaseRef = database.getReference("users/"+ authUser.getUid());
        final String uID = authUser.getUid();
        changed_name.setText(authUser.getDisplayName());

        //database location: posts/userId/postid/
        DatabaseRef = database.getReference();
        //database: create a postId into the firebase database
        //storage location: profile_pic/uid/
        String profilePicRef = "profile_pic/";

        storage = FirebaseStorage.getInstance();
        imageReference = storage.getReference(profilePicRef).child(uID);

        btn_changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new CharSequence[]
                                {"Choose from album", "Take a new photo"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                switch (which) {
                                    case 0:
                                        Log.d(TAG, "onClick: choose from album");
                                        changeImage();
                                        break;
                                    case 1:
                                        Log.d(TAG, "onClick: take a new photo");

                                        takePicture();
                                        break;

                                }
                            }
                        });

                builder.show();
            }
        });
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //write profile info to database

                new_description= changed_description.getText().toString();
                new_username = changed_name.getText().toString();
                new_profile_pic = uID;
                updateUser(authUser.getUid());



                //uploadNewProfile();

            }
        });
    }

    public void updateUser(String userid){
        DatabaseReference ref = DatabaseRef.child("users/").child(userid);
        Map<String, Object> updates = new HashMap<>();
        if (new_description.length()>0){
            updates.put("description",new_description);
        }
        if(new_username.length()>0) {
            updates.put("username", new_username);
        }
        if (imagePath != null){
            updates.put("image", new_profile_pic);

        }
        if (updates.size()>0) {
            ref.updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    if (imagePath != null) {
                        uploadImage();
                    }
                }
            });
        }
    }

    private void changeImage() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()){
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, ALBUM_REQUEST_CODE);
                        }
                        else{
                            Toast.makeText(context,"Permission denied",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        //camera
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            if(imageUri!=null){
                //String path = imageUri.getPath();
                Log.d(TAG, "Image Url is"+imageUri);
                try {

                    InputStream ims = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    Log.d(TAG, "onActivityResult: "+bitmap);
                    //resize bitmap
                    resize = getResizedBitmap(bitmap, 300, 300);
                    //imageView.setImageBitmap(resize);
                    //loadImage(resize);
                    loadImage(resize);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        //gallery
        if (requestCode ==ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imagePath = getPath( this.getApplicationContext(), imageUri);
            //Log.d(TAG, "Image Url is"+path);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            //resize bitmap
            resize = getResizedBitmap(bitmap, 300, 300);
            //imageView.setImageBitmap(resize);
            //loadImage(resize);
            loadImage(resize);
        }
    }
    public static String getPath( Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }
    public void takePicture(){
        //access external storage
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "MobileIns" + File.separator +"profile";
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        //create image file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String file = "Ins"+timeStamp+".jpg";
        File photoFile =new File(destDir,file);

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
    private void loadImage(Bitmap bitmap){
        //orginal = BitmapUtils.getBitmapFromAssets(this,pic_name,300,30);
        original_new_pic  = bitmap;
        profile_pic.setImageBitmap(original_new_pic);
    }

    private void uploadImage() {

        //Log.d(TAG, imagePath);

        if(imageReference != null )
        {
            progressBar.setVisibility(View.VISIBLE);
            //disable user interaction
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //InputStream stream = new FileInputStream(new File(imagePath));
            Uri filepath = Uri.fromFile(new File(imagePath));
            Log.d(TAG, "uploadImage: @@@@@@@@@@@@@@@@@@@@@@@@@@@"+filepath);
            imageReference.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Intent i = new Intent(context, ProfileActivity.class);
                            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(EditProfileActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        /*

         */



    }
    @Override
    public void onStart(){
        super.onStart();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                User newUser = dataSnapshot.getValue(User.class);
                username = newUser.username;
                description = newUser.description;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
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
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { //checking
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    /**
     * verifiy all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                EditProfileActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }


    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for(int i = 0; i< permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(EditProfileActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

}
