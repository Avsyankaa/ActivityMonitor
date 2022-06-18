package com.example.dz;

import android.graphics.drawable.Drawable;

public class AppInformationBuffer {
    public static final AppInformationBuffer INSTANCE = new AppInformationBuffer();

    public String       packageName;
    public Drawable     logoImage;
    public CharSequence name;
}
