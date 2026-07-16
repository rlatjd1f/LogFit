package com.logfit.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    private static final String CHANNEL_ID = "LogFitTimerChannel";
    private static final int NOTIFICATION_ID = 2026;
    
    private NotificationManager notificationManager;
    private Timer timer;
    private int remainingSeconds = 0;
    private String exerciseLabel = "운동 휴식";
    
    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("START".equals(action)) {
                remainingSeconds = intent.getIntExtra("seconds", 90);
                exerciseLabel = intent.getStringExtra("label");
                if (exerciseLabel == null || exerciseLabel.isEmpty()) {
                    exerciseLabel = "운동 휴식";
                }
                
                MainActivity.isTimerRunning = true;
                startForegroundServiceWithNotification();
                startCountdownTimer();
            } else if ("STOP".equals(action)) {
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }
    
    private void startForegroundServiceWithNotification() {
        Notification notification = buildNotification(remainingSeconds);
        startForeground(NOTIFICATION_ID, notification);
    }
    
    private void startCountdownTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    updateNotification(remainingSeconds);
                    
                    // Capacitor 플러그인을 통해 웹뷰로 실시간 남은 초 전송
                    TimerPlugin.sendTimerTickEvent(remainingSeconds);
                } else {
                    TimerPlugin.sendTimerFinishedEvent();
                    stopSelf();
                }
            }
        }, 1000, 1000);
    }
    
    private void updateNotification(int seconds) {
        Notification notification = buildNotification(seconds);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    private Notification buildNotification(int seconds) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        String timeString = String.format("%02d:%02d", seconds / 60, seconds % 60);
        String title = exerciseLabel + " - 휴식 중";
        String text = "남은 휴식 시간: " + timeString;
        if (seconds <= 0) {
            text = "휴식 완료! 다음 세트를 준비하세요.";
        }
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "LogFit 운동 휴식 타이머",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("운동 후 휴식 카운트다운 알림 채널입니다.");
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }
    
    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        MainActivity.isTimerRunning = false;
        notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
