package com.logfit.app;

import android.content.res.Configuration;
import android.os.Build;
import android.app.PictureInPictureParams;
import android.util.Rational;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    
    // JS에서 제어할 타이머 작동 상태 플래그
    public static boolean isTimerRunning = false;

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isTimerRunning && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
