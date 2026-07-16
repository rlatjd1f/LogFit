package com.logfit.app;

import android.content.Intent;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "TimerPlugin")
public class TimerPlugin extends Plugin {
    
    private static TimerPlugin instance;

    @Override
    public void load() {
        super.load();
        instance = this;
    }

    @PluginMethod
    public void startTimer(PluginCall call) {
        int seconds = call.getInt("seconds", 90);
        String label = call.getString("label", "운동 휴식");

        try {
            Intent intent = new Intent(getContext(), TimerService.class);
            intent.setAction("START");
            intent.putExtra("seconds", seconds);
            intent.putExtra("label", label);
            
            // Android 8.0 이상에서 Foreground Service 실행 대응
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }
            
            MainActivity.isTimerRunning = true;
            final MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.updatePipParams(true);
                    }
                });
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to start timer service: " + e.getMessage());
        }
    }

    @PluginMethod
    public void stopTimer(PluginCall call) {
        try {
            Intent intent = new Intent(getContext(), TimerService.class);
            intent.setAction("STOP");
            getContext().stopService(intent);
            
            MainActivity.isTimerRunning = false;
            final MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.updatePipParams(false);
                    }
                });
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to stop timer service: " + e.getMessage());
        }
    }

    @PluginMethod
    public void setTimerRunningFlag(PluginCall call) {
        boolean isRunning = call.getBoolean("isRunning", false);
        MainActivity.isTimerRunning = isRunning;
        final MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.updatePipParams(isRunning);
                }
            });
        }
        call.resolve();
    }

    // TimerService에서 매초 틱 신호를 웹뷰로 전송하는 헬퍼 메서드
    public static void sendTimerTickEvent(int secondsRemaining) {
        if (instance != null) {
            JSObject data = new JSObject();
            data.put("seconds", secondsRemaining);
            instance.notifyListeners("timerTick", data);
        }
    }

    // TimerService에서 타이머 완료 신호를 웹뷰로 전송하는 헬퍼 메서드
    public static void sendTimerFinishedEvent() {
        if (instance != null) {
            instance.notifyListeners("timerFinished", new JSObject());
        }
    }

    @Override
    protected void handleOnDestroy() {
        instance = null;
        super.handleOnDestroy();
    }
}
