package com.logfit.app;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.app.PictureInPictureParams;
import android.util.Log;
import android.util.Rational;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    
    // JS에서 제어할 타이머 작동 상태 플래그
    public static boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // 커스텀 타이머 플러그인을 브릿지에 명시적으로 수동 등록
            registerPlugin(TimerPlugin.class);
            Log.d("LogFitMainActivity", "onCreate: TimerPlugin registered successfully");
            Toast.makeText(this, "[LogFit Native] MainActivity 초기화 & TimerPlugin 수동 등록 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("LogFitMainActivity", "onCreate: Failed to register TimerPlugin", e);
            Toast.makeText(this, "[LogFit Native 오류] TimerPlugin 등록 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        
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
        Log.d("LogFitMainActivity", "updatePipParams called: enable=" + enable);
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
                        Log.d("LogFitMainActivity", "updatePipParams: setPictureInPictureParams applied, autoEnter=" + enable);
                        Toast.makeText(MainActivity.this, "[LogFit Native] PIP 파라미터 갱신! autoEnter=" + enable, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("LogFitMainActivity", "updatePipParams error: ", e);
                        Toast.makeText(MainActivity.this, "[LogFit Native 오류] PIP 파라미터 갱신 실패", Toast.LENGTH_SHORT).show();
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
        Log.d("LogFitMainActivity", "onUserLeaveHint: isTimerRunning=" + isTimerRunning + ", serviceRunning=" + isTimerServiceRunning() + " -> shouldEnterPip=" + shouldEnterPip);
        Toast.makeText(this, "[LogFit Native] 홈이탈 감지! enterPipTarget=" + shouldEnterPip, Toast.LENGTH_LONG).show();
        
        if (shouldEnterPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                builder.setAspectRatio(new Rational(1, 1));
                boolean success = enterPictureInPictureMode(builder.build());
                Log.d("LogFitMainActivity", "onUserLeaveHint: enterPictureInPictureMode returned " + success);
                Toast.makeText(this, "[LogFit Native] PIP 수동 진입 결과: " + success, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("LogFitMainActivity", "onUserLeaveHint error: ", e);
                Toast.makeText(this, "[LogFit Native 오류] PIP 수동 진입 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        Log.d("LogFitMainActivity", "onPictureInPictureModeChanged: isInPipMode=" + isInPictureInPictureMode);
        Toast.makeText(this, "[LogFit Native] PIP 상태 변경: isInPip=" + isInPictureInPictureMode, Toast.LENGTH_SHORT).show();
        if (getBridge() != null) {
            // 웹뷰(Window 객체)로 pipModeChanged 커스텀 이벤트를 데이터와 함께 발송
            getBridge().triggerWindowJSEvent("pipModeChanged", "{ \"isInPipMode\": " + isInPictureInPictureMode + " }");
        }
    }
}
