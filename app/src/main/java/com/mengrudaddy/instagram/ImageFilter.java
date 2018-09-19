package com.mengrudaddy.instagram;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.mengrudaddy.instagram.Adapter.ViewPagerAdapter;
import com.mengrudaddy.instagram.Interface.EditImageFragmentListener;
import com.mengrudaddy.instagram.Interface.FilterListFragmentListener;
import com.mengrudaddy.instagram.utils.BitmapUtils;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;

public class ImageFilter extends AppCompatActivity implements FilterListFragmentListener,EditImageFragmentListener{
    public static final  String pic_name = "dad.jpg";
    public static final  String TAG = "ImageFilter";
    public  static final int PERMISSION_PICK_IMAGE = 1000;

    ImageView imageView;
    TabLayout tabLayout;
    ViewPager viewPager;
    ConstraintLayout coordinatorLayout;
    Bitmap orginal, filtered, finalImg;

    FilterFragment filterFragment;
    EditFragment editFragment;

    int brightnessFinal = 0;
    float contrastFinal = 1.0f;


    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_filter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Instagram Filter");


        //View
        imageView = (ImageView) findViewById(R.id.new_image);
        tabLayout = (TabLayout) findViewById(R.id.filter_edit_tabs);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        coordinatorLayout = (ConstraintLayout) findViewById(R.id.coordinator);

        loadImage();
        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void loadImage(){
        orginal = BitmapUtils.getBitmapFromAssets(this,pic_name,300,30);
        filtered = orginal.copy(Bitmap.Config.ARGB_8888,true);
        finalImg = orginal.copy(Bitmap.Config.ARGB_8888,true);
        imageView.setImageBitmap(orginal);


    }
    private void setUpViewPager(ViewPager vp){
        ViewPagerAdapter adpter = new ViewPagerAdapter(getSupportFragmentManager());

        filterFragment = new FilterFragment();
        filterFragment.setListener(this);

        editFragment = new EditFragment();
        editFragment.setListener(this);
        adpter.addFragment(filterFragment,"Filter");
        adpter.addFragment(editFragment,"Edit");
        vp.setAdapter(adpter);
    }
    public void onBrightnessChanged(int brightness){
        brightnessFinal = brightness;
        Filter flter = new Filter();
        flter.addSubFilter(new BrightnessSubFilter(brightness));
        imageView.setImageBitmap(flter.processFilter(finalImg.copy(Bitmap.Config.ARGB_8888,true)));

    }

    @Override
    public void onContrastChanged(float contrast) {
        contrastFinal = contrast;
        Filter flter = new Filter();
        flter.addSubFilter(new ContrastSubFilter(contrast));
        imageView.setImageBitmap(flter.processFilter(finalImg.copy(Bitmap.Config.ARGB_8888,true)));

    }


    @Override
    public void onEditStarted() {

    }

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
        imageView.setImageBitmap(filter.processFilter(filtered));
        finalImg = filtered.copy(Bitmap.Config.ARGB_8888,true);

    }

    public void resetControl(){
        if(editFragment != null){
            editFragment.resetControls();
        }
        brightnessFinal=0;
        contrastFinal = 1.0f;

    }
}
