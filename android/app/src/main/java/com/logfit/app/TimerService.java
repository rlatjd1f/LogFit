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
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    private static final String CHANNEL_ID = "LogFitTimerChannel";
    private static final int NOTIFICATION_ID = 2026;
    
    // 타이머 포그라운드 서비스가 실제로 구동 중인지를 절대적으로 나타내는 정적 플래그
    public static boolean isServiceRunning = false;
    
    private NotificationManager notificationManager;
    private Timer timer;
    private int remainingSeconds = 0;
    private long endAtMillis = 0L;
    private String exerciseLabel = "운동 휴식";

    public static int currentRemainingSeconds = 0;
    public static long currentEndAtMillis = 0L;
    public static String currentExerciseLabel = "운동 휴식";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LogFitTimerService", "onCreate: TimerService initialized");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LogFitTimerService", "onStartCommand called: intent=" + (intent != null ? intent.getAction() : "null"));
        if (intent != null) {
            String action = intent.getAction();
            if ("START".equals(action)) {
                remainingSeconds = intent.getIntExtra("seconds", 90);
                endAtMillis = System.currentTimeMillis() + remainingSeconds * 1000L;
                exerciseLabel = intent.getStringExtra("label");
                if (exerciseLabel == null || exerciseLabel.isEmpty()) {
                    exerciseLabel = "운동 휴식";
                }
                Log.d("LogFitTimerService", "onStartCommand START: seconds=" + remainingSeconds + ", label=" + exerciseLabel);
                
                isServiceRunning = true;
                currentRemainingSeconds = remainingSeconds;
                currentEndAtMillis = endAtMillis;
                currentExerciseLabel = exerciseLabel;
                MainActivity.isTimerRunning = true;
                startForegroundServiceWithNotification();
                startCountdownTimer();
            } else if ("STOP".equals(action)) {
                Log.d("LogFitTimerService", "onStartCommand STOP received");
                isServiceRunning = false;
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }
    
    private void startForegroundServiceWithNotification() {
        Log.d("LogFitTimerService", "startForegroundServiceWithNotification: starting foreground");
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
                remainingSeconds = TimerState.remainingSeconds(endAtMillis, System.currentTimeMillis());
                currentRemainingSeconds = remainingSeconds;
                if (remainingSeconds > 0) {
                    updateNotification(remainingSeconds);
                    
                    // Capacitor 플러그인을 통해 웹뷰로 실시간 남은 초 전송
                    TimerPlugin.sendTimerTickEvent(remainingSeconds);
                } else {
                    Log.d("LogFitTimerService", "Timer finished. Stopping service.");
                    isServiceRunning = false;
                    currentRemainingSeconds = 0;
                    currentEndAtMillis = 0L;
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
            .setSubText("LogFit 휴식 타이머")
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text + "\n타이머를 누르면 운동 화면으로 돌아갑니다."))
            // 상태 표시줄에는 투명 배경의 단색 small icon만 안정적으로 표시된다.
            .setSmallIcon(R.drawable.ic_stat_logfit_timer)
            .setColor(0xFF1769E0)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(endAtMillis)
            .setUsesChronometer(seconds > 0)
            .setChronometerCountDown(seconds > 0)
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
        Log.d("LogFitTimerService", "onDestroy: TimerService stopping");
        isServiceRunning = false;
        currentRemainingSeconds = 0;
        currentEndAtMillis = 0L;
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
