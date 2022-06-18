package com.example.dz;

import com.example.dz.model.App;

import java.util.HashMap;

public class AppsBuffer {
    public static final AppsBuffer INSTANCE = new AppsBuffer();

    public HashMap<String, App> appsMap = new HashMap<>();
}
