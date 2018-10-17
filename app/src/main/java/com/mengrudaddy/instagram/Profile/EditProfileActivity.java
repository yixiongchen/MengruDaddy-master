package com.mengrudaddy.instagram.Profile;

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
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mengrudaddy.instagram.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView profile_pic;
    private EditText username,user_description;
    private Button btn_changePic,btn_cancle,btn_ok;
    private Context context = EditProfileActivity.this;
    private static final String TAG = "EditProfileActivity";
    private Bitmap original_new_pic,resize;


    private Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int ALBUM_REQUEST_CODE = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //View initialization
        username = (EditText) findViewById(R.id.change_name);
        user_description = (EditText) findViewById(R.id.change_description);
        btn_changePic = (Button) findViewById(R.id.change_image);
        btn_cancle = (Button) findViewById(R.id.cancle);
        btn_ok = (Button) findViewById(R.id.ok);

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
                                        Toast.makeText(context, "clicked 1", Toast.LENGTH_SHORT).show();
                                        changeImage();
                                    case 1:
                                        Toast.makeText(context, "clicked 2", Toast.LENGTH_SHORT).show();
                                        takePicture();
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
                //uploadNewProfile();
            }
        });

    }

    private void changeImage() {
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
        Log.d(TAG, "show result");
        super.onActivityResult(requestCode, resultCode, data);

        //camera
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            if(imageUri!=null){

                String path = imageUri.getPath();
                Log.d(TAG, "Image Url is"+path);
                //File imagefile = new File(path);
                //Intent intent = new Intent(this, ImageFilter.class);
                //intent.putExtra("picture", path);
                //startActivity(intent);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                //resize bitmap
                resize = getResizedBitmap(bitmap, 1080, 1080);
                //imageView.setImageBitmap(resize);
                //loadImage(resize);
                loadImage(resize);
            }
        }
        //gallery
        if (requestCode ==ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = getPath( this.getApplicationContext(), uri);
            //Log.d(TAG, "Image Url is"+path);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            //resize bitmap
            resize = getResizedBitmap(bitmap, 1080, 1080);
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
}
