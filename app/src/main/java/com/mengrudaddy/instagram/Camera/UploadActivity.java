package com.mengrudaddy.instagram.Camera;

/*
UploadActivity.java
This class is activity for upload images
with fragment of choose photo from local album
or choose photo by taking a new one
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.mengrudaddy.instagram.Adapter.ViewPagerAdapter;
import com.mengrudaddy.instagram.R;

import com.mengrudaddy.instagram.utils.Permission;

public class UploadActivity extends AppCompatActivity{
    private static final String TAG = "UploadActivity";
    private Context context=UploadActivity.this;

    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager viewPager;
    ConstraintLayout constraintLayout;
    AlbumFragment albumFragment;
    PhotoFragment photoFragment;
    TabLayout tabLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        if(!checkPermissionsArray(Permission.PERMISSIONS)){
            verifyPermissions(Permission.PERMISSIONS);
        }

        tabLayout = (TabLayout) findViewById(R.id.album_photo_tabs);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        constraintLayout = (ConstraintLayout) findViewById(R.id.coordinator);

        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("AlBUM");
        tabLayout.getTabAt(1).setText("PHOTO");

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











}
