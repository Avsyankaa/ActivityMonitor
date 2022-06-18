package com.example.dz.model;

import android.graphics.drawable.Drawable;

public class App{

    private final String       packageName;
    private final Drawable     logo;
    private final CharSequence name;

    private       boolean      isEnable;
    private       int          timeLimit;

    private       boolean      isForbidden;
    private       int          overTimeLimited;

    public App(String packageName, Drawable logo, CharSequence name,
               boolean isEnable, int timeLimit) {
        this.packageName = packageName;
        this.logo        = logo;
        this.name        = name;
        this.isEnable    = isEnable;
        this.timeLimit   = timeLimit;

        this.isForbidden = false;
        this.overTimeLimited = 0;
    }

    public String getPackageName() { return packageName; }

    public Drawable getLogo() { return logo; }

    public CharSequence getName() { return name; }

    public boolean getIsEnable() { return isEnable; }

    public void setIsEnable(boolean isEnable) { this.isEnable = isEnable; }

    public int getTimeLimit() { return timeLimit; }

    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }

    public int getOverTimeLimited() { return overTimeLimited; }

    public void incOverTimeLimited() { this.overTimeLimited += 1; }

    public void resetOverTimeLimited() { this.overTimeLimited = 0; }

    public boolean isForbidden() { return isForbidden; }

    public void setIsForbidden(boolean isForbidden) { this.isForbidden = isForbidden; }
}
