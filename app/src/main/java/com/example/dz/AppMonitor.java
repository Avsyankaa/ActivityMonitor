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

import com.example.dz.model.App;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppMonitor extends Service {

    private static final int SLEEP_TIME = 9999;
    private static int messageId = 1;
    private static int SLEEP_FORB_TIMES = 6;
    private static final int MINS_TO_MILLISEC = 60000; // 1000 * 60
    private final String CHANNEL_LOGIC_ID = "Logic Service ID";

    private void checkMap(Map<String, UsageStats> stats) {
        // Get statistics
        // Iterate through each value in map
        for (Map.Entry<String, App> entry : AppsBuffer.INSTANCE.appsMap.entrySet())
        {
            UsageStats usageStats = stats.get(entry.getKey());
            App currApp = entry.getValue();

            assert usageStats != null;

            if (currApp.isForbidden())
            {
                if (System.currentTimeMillis() - usageStats.getLastTimeVisible() <= SLEEP_TIME) {
                    if (currApp.getOverTimeLimited() == SLEEP_FORB_TIMES)
                    {
                        sendNotification("Хватит тут сидеть, выходи!",
                                CHANNEL_LOGIC_ID,
                                "Предупреждение",
                                false
                        );
                        currApp.resetOverTimeLimited();
                    }
                    else
                    {
                        currApp.incOverTimeLimited();
                    }
                }

                continue;
            }

            if ((usageStats.getTotalTimeVisible() - currApp.getStartTimeMeasured())>= (long) (currApp.getTimeLimit()) * MINS_TO_MILLISEC)
            {
                // Send a notification
                sendNotification("Для " + currApp.getName() + " время использования вышло. Остановитесь!",
                        CHANNEL_LOGIC_ID,
                        "Предупреждение",
                        false
                );
                currApp.setIsForbidden(true);
            }
        }
    }

    private void createNotificationChannel(String channelName, int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        NotificationChannel channel = new NotificationChannel(channelName,
                channelName,
                importance);
        channel.setDescription(channelName);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotification(String notificationText,
                                  String channelId,
                                  String contentTitle,
                                  Boolean foreground)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(contentTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_LOW);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(
                () -> {
                    while (true) {
                        try {
                            Thread.sleep(SLEEP_TIME);
                            checkApplicationsActivity();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();

        // Create notification channel for the current app
        String CHANNEL_SERV_ID = "Foreground Service ID";
        createNotificationChannel(CHANNEL_SERV_ID, NotificationManager.IMPORTANCE_LOW);
        createNotificationChannel(CHANNEL_LOGIC_ID, NotificationManager.IMPORTANCE_HIGH);

        // Send appropriate notification
        sendNotification("Ваше время - наш приоритет",
                CHANNEL_SERV_ID,
                "ActivityMonitor",
                true);

        return super.onStartCommand(intent, flags, startId);
    }

    private void checkApplicationsActivity(){
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);

        // TO DO: Clear overlimited at 00:00!
        // Update StartTimeMeasured at 00:00!!
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(start, end);

        checkMap(stats);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}