package com.example.dz;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppBlocker extends Service {

    private static final int SLEEP_TIME = 9999;
    private static int messageId = 1;
    private final String CHANNEL_SERV_ID = "Foreground Service ID";
    private final String CHANNEL_LOGIC_ID = "Logic Service ID";
    static HashMap<String, Integer> overlimited = new HashMap<String, Integer>();;

    private boolean isForeground(Context ctx, String myPackage)
    {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) {
            return true;
        }
        return false;
    }

    private boolean getActiveApp(String packageName)
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

        for (int i = 0; i < runningAppProcessInfo.size(); i++)
        {
            if(runningAppProcessInfo.get(i).processName.equals(packageName))
            {
                sendNotification("HERE",
                        CHANNEL_LOGIC_ID,
                        NotificationCompat.PRIORITY_LOW,
                        "Предупреждение",
                        false
                );
                if (!isForeground(this, packageName))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void checkMap(HashMap<String, Integer> map, Map<String, UsageStats> stats) {
        // Get statistics
        // Iterate through each value in map
        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            UsageStats usageStats = stats.get(entry.getKey());

            if (overlimited.get(entry.getKey()) != null) {
                if (System.currentTimeMillis() - usageStats.getLastTimeVisible() <= SLEEP_TIME) {

                    if (overlimited.get(entry.getKey()) == 5)
                    {
                        sendNotification("Хватит тут сидеть, выходи!",
                                CHANNEL_LOGIC_ID,
                                NotificationCompat.PRIORITY_LOW,
                                "Предупреждение",
                                false
                        );
                        overlimited.replace(entry.getKey(), 0);
                    }
                    else
                    {
                        sendNotification("Новое значение",
                                CHANNEL_LOGIC_ID,
                                NotificationCompat.PRIORITY_LOW,
                                "Предупреждение",
                                false
                        );
                        overlimited.replace(entry.getKey(), overlimited.get(entry.getKey()) + 1);
                    }
                }

                continue;
            }


            long res = 0;
            if(usageStats!=null)
            {
                res = usageStats.getTotalTimeVisible();
                System.out.println(res);

                if (res >= entry.getValue())
                {
                    // Send a notification
                    sendNotification("Вы превысили время пребывания в приложении. Остановитесь!",CHANNEL_LOGIC_ID,
                            NotificationCompat.PRIORITY_LOW,
                            "Предупреждение",
                            false
                    );
                    overlimited.put(entry.getKey(), 0);
                }
            }
        }
    }

    private void createNotificationChannel(String channelName, int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelName,
                    channelName,
                    importance);
            channel.setDescription(channelName);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String notificationText,
                                  String channelId,
                                  int priority,
                                  String contentTitle,
                                  Boolean foreground)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(contentTitle)
                .setContentText(notificationText)
                .setPriority(priority);
        if (!foreground)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(messageId, builder.build());
        }
        else
        {
            startForeground(messageId, builder.build());
        }
        messageId += 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(
                () -> {
                    while (true) {
                        try {
                            Thread.sleep(SLEEP_TIME);

                            PackageManager packageManager = getPackageManager();
                            List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);
                            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DAY_OF_YEAR, -1);
                            long start = calendar.getTimeInMillis();
                            long end = System.currentTimeMillis();
                            Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(start, end);

                            HashMap<String, Integer> testMap = new HashMap<String, Integer>();
                            testMap.put("com.android.vending", 2000);

                            // TO DO: Clear overlimited at 00:00!
                            checkMap(testMap, stats);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();


        // Create notification channel for the current app
        createNotificationChannel(CHANNEL_SERV_ID, NotificationManager.IMPORTANCE_LOW);
        createNotificationChannel(CHANNEL_LOGIC_ID, NotificationManager.IMPORTANCE_HIGH);

        // Send appropriate notification
        sendNotification("Service is running",
                CHANNEL_SERV_ID,
                NotificationCompat.PRIORITY_LOW,
                "Service enabled",
                true);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}