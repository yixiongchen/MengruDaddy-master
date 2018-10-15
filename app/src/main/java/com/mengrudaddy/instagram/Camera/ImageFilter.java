package com.mengrudaddy.instagram.Camera;

/*
ImageFilter.java
This class is to edit image : crop, filter, brightness and contrast
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mengrudaddy.instagram.Adapter.ViewPagerAdapter;
import com.mengrudaddy.instagram.Home.MainActivity;
import com.mengrudaddy.instagram.Interface.EditImageFragmentListener;
import com.mengrudaddy.instagram.Interface.FilterListFragmentListener;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BitmapUtils;
import com.yalantis.ucrop.UCrop;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class ImageFilter extends AppCompatActivity implements FilterListFragmentListener,EditImageFragmentListener{
    public static final  String pic_name = "dad.jpg";
    public static final  String TAG = "ImageFilter";
    public  static final int PERMISSION_PICK_IMAGE = 1000;
    private Context context = ImageFilter.this;
    private String new_path;
    private Uri uri;

    //PhotoEditorView imageView;

    PhotoEditorView photoEditorView;
    PhotoEditor photoEditor;
    CardView func_filters_list,func_edit, func_crop;

    private ImageView  btnCancel, btnNext;
    //private TabLayout tabLayout;
    //private ViewPager viewPager;
    private ConstraintLayout constraintLayout;
    private Bitmap orginal, filtered, finalImg, resize;

    private FilterFragment filterFragment;
    private EditFragment editFragment;

    int brightnessFinal = 0;
    float contrastFinal = 1.0f;


    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_filter);
        //get image from camera
        String path = getIntent().getStringExtra("picture");
        new_path = path;
        /*
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Instagram Filter");
        */

        //View
        Log.d(TAG, "Start filtering");
        //imageView = (ImageView) findViewById(R.id.new_image);

        photoEditorView = (PhotoEditorView) findViewById(R.id.new_image) ;
        photoEditor = new PhotoEditor.Builder(this,photoEditorView)
                .setPinchTextScalable(true)
                .build();


        btnCancel = (ImageView) findViewById(R.id.icon_cancle);
        btnNext = (ImageView) findViewById(R.id.icon_next);
        //tabLayout = (TabLayout) findViewById(R.id.filter_edit_tabs);
        //viewPager = (ViewPager) findViewById(R.id.view_pager);
        constraintLayout = (ConstraintLayout) findViewById(R.id.coordinator);

        func_edit = (CardView) findViewById(R.id.func_edit);
        func_filters_list= (CardView) findViewById(R.id.func_filters_list);
        func_crop= (CardView) findViewById(R.id.func_crop);

        func_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCrop(new_path);

            }
        });

        func_filters_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment filterFragment = FilterFragment.getInstance();
                filterFragment.setListener(ImageFilter.this);
                filterFragment.show(getSupportFragmentManager(),filterFragment.getTag());

            }
        });

        func_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditFragment editFragment = EditFragment.getInstance();
                editFragment.setListener(ImageFilter.this);
                editFragment.show(getSupportFragmentManager(),editFragment.getTag());
            }
        });


        Bitmap bitmap = BitmapFactory.decodeFile(path);
        //resize bitmap
        resize = Bitmap.createScaledBitmap(bitmap, 1080, 1080, true);
        //imageView.setImageBitmap(resize);
        //loadImage(resize);
        loadImage(resize);
        //setUpViewPager(viewPager);
        //tabLayout.setupWithViewPager(viewPager);
        Log.d(TAG, "show the filtered");
        //set view pager and fragments


        //toolbar options
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the filter fragment.");
                finish();
            }
        });
        //forward image to share activity
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Ready to post the photo.");
                //first save the filtered image
                String filepath = StoreFilteredImage();

                Intent intent = new Intent(ImageFilter.this, ShareActivity.class);
                intent.putExtra("PostImage", filepath);
                startActivity(intent);
            }
        });
    }



    // load image on half of the screen
    private void loadImage(Bitmap bitmap){
        //orginal = BitmapUtils.getBitmapFromAssets(this,pic_name,300,30);
        orginal  = bitmap;
        filtered = orginal.copy(Bitmap.Config.ARGB_8888,true);
        finalImg = orginal.copy(Bitmap.Config.ARGB_8888,true);
        ///////
        photoEditorView.getSource().setImageBitmap(orginal);
    }

    // set up fragment of filter and edit
    /*private void setUpViewPager(ViewPager vp){
        ViewPagerAdapter adpter = new ViewPagerAdapter(getSupportFragmentManager());
        filterFragment = new FilterFragment();
        editFragment = new EditFragment();

        filterFragment.setListener(this);
        editFragment.setListener(this);
        adpter.addFragment(filterFragment,"Filter");
        adpter.addFragment(editFragment,"Edit");
        vp.setAdapter(adpter);
    }*/

    // listening for brightness seekbar changing
    public void onBrightnessChanged(int brightness){
        brightnessFinal = brightness;
        Filter flter = new Filter();
        flter.addSubFilter(new BrightnessSubFilter(brightness));
        photoEditorView.getSource().setImageBitmap(flter.processFilter(finalImg.copy(Bitmap.Config.ARGB_8888,true)));
    }

    // listening for contrast seekbar changing
    @Override
    public void onContrastChanged(float contrast) {
        contrastFinal = contrast;
        Filter flter = new Filter();
        flter.addSubFilter(new ContrastSubFilter(contrast));
        photoEditorView.getSource().setImageBitmap(flter.processFilter(finalImg.copy(Bitmap.Config.ARGB_8888,true)));
    }


    @Override
    public void onEditStarted() {

    }


    // on edit completed add filter and brightness and contrast values to the image
    @Override
    public void onEditCompleted() {
        Bitmap bitmap = filtered.copy(Bitmap.Config.ARGB_8888,true);
        Filter filter = new Filter();
        filter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        filter.addSubFilter(new ContrastSubFilter(contrastFinal));
        finalImg =filter.processFilter(bitmap);

    }

    @Override
    public void onFilterSelected(Filter filter) {
        resetControl();
        filtered = orginal.copy(Bitmap.Config.ARGB_8888,true);
        photoEditorView.getSource().setImageBitmap(filter.processFilter(filtered));
        finalImg = filtered.copy(Bitmap.Config.ARGB_8888,true);

    }

    // reset values
    public void resetControl(){
        if(editFragment != null){
            editFragment.resetControls();
        }
        brightnessFinal=0;
        contrastFinal = 1.0f;
    }

    public void goPhoto(View view) {
        finish();
        //Intent upload = new Intent(context, UploadActivity.class);//ACTIVITY_NUM=2
        //context.startActivity(upload);
    }

    public String StoreFilteredImage(){
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "MobileIns"+File.separator+"Filtered";
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filepath = "Filter"+timeStamp+".jpg";

        File file = new File(destDir, filepath);
        try (FileOutputStream out = new FileOutputStream(file)) {
            finalImg.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
        String finalPath = Uri.fromFile(file).getPath();

        return finalPath;

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


    @Override
    protected void onStart(){
        super.onStart();

        Log.d(TAG, "return to");

    }

    private void startCrop(String path) {
        uri = Uri.fromFile(new File(new_path));
        String destinationFileName = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop ucrop = UCrop.of(uri,Uri.fromFile(new File(getCacheDir(),destinationFileName)));
        ucrop.start(ImageFilter.this);


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PERMISSION_PICK_IMAGE){
                Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this,data.getData(),800,800);
                uri = data.getData();
                orginal.recycle();
                finalImg.recycle();
                filtered.recycle();

                orginal = bitmap.copy(Bitmap.Config.ARGB_8888,true);
                finalImg = orginal.copy(Bitmap.Config.ARGB_8888,true);
                filtered = orginal.copy(Bitmap.Config.ARGB_8888,true);
                photoEditorView.getSource().setImageBitmap(orginal);
                bitmap.recycle();

            }
            //else if (requestCode == PERMISSION_INSERT_IMAGE){}

            else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);

            }
        }
        else if(resultCode == UCrop.RESULT_ERROR){
            handleCropError(data);

        }
    }

    private void handleCropResult(Intent data) {
        final Uri resultUri = UCrop.getOutput(data);
        if (resultUri != null){
            photoEditorView.getSource().setImageURI(resultUri);
            Bitmap bitmap = ((BitmapDrawable)photoEditorView.getSource().getDrawable()).getBitmap();
            orginal = bitmap.copy(Bitmap.Config.ARGB_8888,true);
            filtered = orginal;
            finalImg = orginal;
            new_path = resultUri.getPath();

        }
        else {
            Toast.makeText(this,"cannot retrive crop image",Toast.LENGTH_SHORT).show();

        }


    }
    private void handleCropError(Intent data) {
        final Throwable cropError = UCrop.getError((data));
        if (cropError != null){
            Toast.makeText(this,cropError.getMessage(),Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this,"Unexpected Error",Toast.LENGTH_SHORT).show();

        }
    }

}
