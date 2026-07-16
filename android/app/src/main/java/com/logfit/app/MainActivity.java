package com.logfit.app;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.app.PictureInPictureParams;
import android.util.Rational;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    
    // JS에서 제어할 타이머 작동 상태 플래그
    public static boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Android 13(Tiramisu, API 33) 이상이고 알림 권한이 승인되지 않은 경우 권한 팝업 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // 안드로이드 OS 레벨에서 TimerService가 실제로 동작 중인지 체크하는 안전 검사 메서드
    private boolean isTimerServiceRunning() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (TimerService.class.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Android 12+ 자동 PIP 상태 업데이트
    public void updatePipParams(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                        builder.setAspectRatio(new Rational(1, 1));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            builder.setAutoEnterEnabled(enable);
                        }
                        setPictureInPictureParams(builder.build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // 메모리 플래그 혹은 실제 타이머 서비스 백그라운드 구동 여부 검사
        boolean shouldEnterPip = isTimerRunning || isTimerServiceRunning();
        if (shouldEnterPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                // PIP 미니뷰용 1:1 화면 비율 지정
                builder.setAspectRatio(new Rational(1, 1));
                enterPictureInPictureMode(builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (getBridge() != null) {
            // 웹뷰(Window 객체)로 pipModeChanged 커스텀 이벤트를 데이터와 함께 발송
            getBridge().triggerWindowJSEvent("pipModeChanged", "{ \"isInPipMode\": " + isInPictureInPictureMode + " }");
        }
    }
}
