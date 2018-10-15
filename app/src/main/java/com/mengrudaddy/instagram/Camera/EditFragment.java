package com.mengrudaddy.instagram.Camera;


/*
EditFragment.java
This class is activity for showing photo editing choices
 */

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.mengrudaddy.instagram.Interface.EditImageFragmentListener;
import com.mengrudaddy.instagram.R;

public class EditFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {

    private EditImageFragmentListener listener;
    SeekBar seekBar_brighteness, seekbar_contrast;
    static EditFragment instance;

    public static EditFragment getInstance(){
        if (instance == null){
            instance = new EditFragment();
        }
        return instance;
    }


    public EditFragment(){

    }


    public void setListener(EditImageFragmentListener listener1){
        this.listener = listener1;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_edit, container, false);

        // brightness and contrast seekbar set up
        seekBar_brighteness = (SeekBar)itemView.findViewById(R.id.brightness_seekbar);
        seekbar_contrast = (SeekBar)itemView.findViewById(R.id.contrast_seekbar);

        seekBar_brighteness.setMax(200);
        seekBar_brighteness.setProgress(100);

        seekbar_contrast.setMax(20);
        seekbar_contrast.setMin(-20);
        seekbar_contrast.setProgress(0);

        seekBar_brighteness.setOnSeekBarChangeListener(this);
        seekbar_contrast.setOnSeekBarChangeListener(this);

        return itemView;
    }


    // on brightness and contrast change method
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(listener != null){
            if (seekBar.getId() == R.id.brightness_seekbar){
                listener.onBrightnessChanged(i-100);
            }
            if (seekBar.getId() == R.id.contrast_seekbar){
                i+=10;
                float value = .10f*i;
                listener.onContrastChanged(value);
            }
        }
    }

    // on start touching seekbar
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null){
            listener.onEditCompleted();
        }
    }

    // on finish touching seekbar
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(listener!=null){
            listener.onEditCompleted();
        }
    }

    // on brightness and contrast values reset to initial state
    public void resetControls(){
        seekBar_brighteness.setProgress(100);
        seekbar_contrast.setProgress(0);
    }
}
