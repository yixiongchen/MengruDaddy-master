package com.mengrudaddy.instagram.Camera;

/*
UploadActivity.java
This class is activity for upload images
with fragment of choose photo from local album
or choose photo by taking a new one
 */
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mengrudaddy.instagram.Adapter.ViewPagerAdapter;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.Permission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//import static com.mengrudaddy.instagram.Camera.ImageFilter.PERMISSION_PICK_IMAGE;

public class UploadActivity extends AppCompatActivity{
    private static final String TAG = "UploadActivity";
    private Context context=UploadActivity.this;

    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager viewPager;
    ConstraintLayout constraintLayout;
    AlbumFragment albumFragment;
    PhotoFragment photoFragment;
    TabLayout tabLayout;

    private Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int ALBUM_REQUEST_CODE = 1;


    private String mCurrentPhotoPath;
    private String dir;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        if(!checkPermissionsArray(Permission.PERMISSIONS)){
            verifyPermissions(Permission.PERMISSIONS);
        }
        Log.d(TAG, "onCreatView: started.");

        Button btnLaunchCamera = (Button)findViewById(R.id.take);
        //imageView = (ImageView)view.findViewById(R.id.new_capture);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick:launching camera.");
                //dispatchTakePictureIntent();
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                takePicture();

            }
        });
        final ImageView btnClose = (ImageView) findViewById(R.id.icon_cancle);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the upload activity.");
                finish();
            }
        });


        Button btnOpenAlbum = (Button)findViewById(R.id.choose_from_album);
        //imageView = (ImageView)view.findViewById(R.id.new_capture);
        btnOpenAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageFromGallery();
                //return true;

            }
        });



//        tabLayout = (TabLayout) findViewById(R.id.album_photo_tabs);
//        viewPager = (ViewPager) findViewById(R.id.view_pager);
//
//        constraintLayout = (ConstraintLayout) findViewById(R.id.coordinator);
//
//        setUpViewPager(viewPager);
//        tabLayout.setupWithViewPager(viewPager);
//        tabLayout.getTabAt(0).setText("AlBUM");
//        tabLayout.getTabAt(1).setText("PHOTO");

    }
    private void openImage(String path){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path),"image/*");
        startActivity(intent);


    }

    private void openImageFromGallery() {
        Dexter.withActivity(this)
                .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()){
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, ALBUM_REQUEST_CODE);
                        }
                        else{
                            Toast.makeText(UploadActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
        .check();
    }

    public void takePicture(){
        //access external storage
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "MobileIns";
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        //create image file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String file = "Ins"+timeStamp+".jpg";
        File photoFile =new File(destDir,file);


        if (photoFile != null) {
            imageUri = Uri.fromFile(photoFile);
            //imageUri = outputUri;
            Log.d(TAG, imageUri.getPath());

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "show result");
        super.onActivityResult(requestCode, resultCode, data);
        //camera
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            if(imageUri!=null){
                String path = imageUri.getPath();
                Log.d(TAG, "Image Url is"+path);
                //File imagefile = new File(path);
                Intent intent = new Intent(this, ImageFilter.class);
                intent.putExtra("picture", path);
                startActivity(intent);
                finish();

            }
        }
        //gallery
        if (requestCode ==ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = getPath( this.getApplicationContext(), uri);
                //Log.d(TAG, "Image Url is"+path);
                Intent intent = new Intent(this, ImageFilter.class);
                intent.putExtra("picture", path);
                startActivity(intent);
                finish();

            }
    }


    private void setUpViewPager(ViewPager vp){
        ViewPagerAdapter adpter = new ViewPagerAdapter(getSupportFragmentManager());
        albumFragment = new AlbumFragment();
        photoFragment = new PhotoFragment();
        adpter.addFragment(albumFragment,"Library");
        adpter.addFragment(photoFragment,"Photo");
        vp.setAdapter(adpter);
    }


    /**
     * verifiy all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                UploadActivity.this,
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

        int permissionRequest = ActivityCompat.checkSelfPermission(UploadActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    /*
        Get the true physical path of uri (Gallery)
     */
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

}
