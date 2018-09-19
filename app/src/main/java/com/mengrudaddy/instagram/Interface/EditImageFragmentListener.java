package com.mengrudaddy.instagram.Interface;

public interface EditImageFragmentListener {
    void onBrightnessChanged(int brightness);
    //void onStaturationChanged(float saturation);
    void onContrastChanged(float contrast);
    void onEditStarted();
    void onEditCompleted();

}
