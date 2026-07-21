package com.logfit.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.reflect.Method;

public class TimerService extends Service {
    private static final String ACTION_START = "START";
    private static final String ACTION_STOP = "STOP";
    // LOW 채널은 상태 표시줄에서 숨겨지므로 새 DEFAULT 채널을 사용한다.
    // Android 알림 채널 중요도는 최초 생성 후 앱에서 변경할 수 없어 ID도 갱신해야 한다.
    private static final String CHANNEL_ID = "LogFitTimerChannelV2";
    private static final int NOTIFICATION_ID = 2026;
    // Notification.EXTRA_REQUEST_PROMOTED_ONGOING (API 36.1).
    // 문자열 extra를 사용하면 compileSdk 34를 유지하면서 구버전 fallback도 가능하다.
    private static final String EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing";
    
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
            if (ACTION_START.equals(action)) {
                remainingSeconds = Math.max(1, Math.min(intent.getIntExtra("seconds", 90), 3600));
                endAtMillis = System.currentTimeMillis() + remainingSeconds * 1000L;
                exerciseLabel = intent.getStringExtra("label");
                if (exerciseLabel == null || exerciseLabel.isEmpty()) {
                    exerciseLabel = "운동 휴식";
                } else if (exerciseLabel.length() > 60) {
                    exerciseLabel = exerciseLabel.substring(0, 60);
                }
                Log.d("LogFitTimerService", "onStartCommand START: seconds=" + remainingSeconds + ", label=" + exerciseLabel);
                
                isServiceRunning = true;
                currentRemainingSeconds = remainingSeconds;
                currentEndAtMillis = endAtMillis;
                currentExerciseLabel = exerciseLabel;
                MainActivity.isTimerRunning = true;
                startForegroundServiceWithNotification();
                startCountdownTimer();
            } else if (ACTION_STOP.equals(action)) {
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

        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String timeString = String.format("%02d:%02d", seconds / 60, seconds % 60);
        String title = exerciseLabel + " - 휴식 중";
        String text = "남은 휴식 시간: " + timeString;
        if (seconds <= 0) {
            text = "휴식 완료! 다음 세트를 준비하세요.";
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText("LogFit 휴식 타이머")
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text + "\n타이머를 누르면 운동 화면으로 돌아갑니다."))
            // 상태 표시줄에는 투명 배경의 단색 small icon만 안정적으로 표시된다.
            .setSmallIcon(R.drawable.ic_stat_logfit_timer)
            .setColor(0xFF1769E0)
            .setContentIntent(pendingIntent)
            .addAction(0, "타이머 종료", stopPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setWhen(endAtMillis)
            .setUsesChronometer(seconds > 0)
            .setChronometerCountDown(seconds > 0)
            .setOnlyAlertOnce(true)
            .setOngoing(true);

        // Android 16.1+에서는 아이콘과 남은 시간이 포함된 시스템 Live Update
        // 상태 칩으로 승격을 요청한다. 지원하지 않는 OS에서는 이 extra를 무시한다.
        builder.getExtras().putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true);
        Notification notification = builder.build();

        // Android 16.1+ 플랫폼 API가 있으면 상태 칩에 들어갈 짧은 MM:SS 값을
        // 명시한다. reflection을 사용해 compileSdk 34와 구버전 호환성을 유지한다.
        try {
            Builder platformBuilder = Builder.recoverBuilder(this, notification);
            Method setShortCriticalText = Builder.class.getMethod("setShortCriticalText", String.class);
            Method setRequestPromotedOngoing = Builder.class.getMethod("setRequestPromotedOngoing", boolean.class);
            setShortCriticalText.invoke(platformBuilder, timeString);
            setRequestPromotedOngoing.invoke(platformBuilder, true);
            notification = platformBuilder.build();
            Log.d("LogFitTimerService", "Live Update status chip requested: " + timeString);
        } catch (NoSuchMethodException ignored) {
            // Android 16.0 이하에서는 기존 상태 표시줄 아이콘과 알림 카드 사용.
        } catch (Exception e) {
            Log.w("LogFitTimerService", "Failed to apply Live Update status chip", e);
        }

        return notification;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "LogFit 운동 휴식 타이머",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setDescription("운동 후 휴식 카운트다운 알림 채널입니다.");
            serviceChannel.setSound(null, null);
            serviceChannel.enableVibration(false);
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
