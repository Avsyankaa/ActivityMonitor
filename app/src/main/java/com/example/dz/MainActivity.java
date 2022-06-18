package com.example.dz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.example.dz.adapter.AppAdapter;
import com.example.dz.model.App;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static boolean hasUsageStatsPermission(Context context) {
        boolean granted;
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT)
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        else
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        return granted;
    }

    private void AddPermissions()
    {
        if (!hasUsageStatsPermission(getApplicationContext()))
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY).setData(Uri.parse("package:" + getPackageName())));
    }

    private boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(AppMonitor.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AddPermissions();

        if (!foregroundServiceRunning()) {
            Intent serviceIntent = new Intent(this, AppMonitor.class);
            startForegroundService(serviceIntent);
        }
        
        setContentView(R.layout.activity_main);

        PackageManager packageManager = getPackageManager();
        List<App> appList = new ArrayList<>();

        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> applicationList =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for(ApplicationInfo info : applicationList) {
            try {
                if(packageManager.getLaunchIntentForPackage(info.packageName) != null) {
                    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                        continue;
                    }
                    appList.add(new App(
                            info.packageName,
                            info.loadIcon(packageManager),
                            info.loadLabel(packageManager),
                            false, 0
                            )
                    );
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        setAppRecycler(appList);
    }

    private void setAppRecycler(List<App> appList) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        RecyclerView appsRecyclerView = findViewById(R.id.appsRecyclerView);
        appsRecyclerView.setLayoutManager(layoutManager);

        AppAdapter appAdapter = new AppAdapter(this, appList);
        appsRecyclerView.setAdapter(appAdapter);

    }
}