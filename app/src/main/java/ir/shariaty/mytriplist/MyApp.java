package ir.shariaty.mytriplist;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks {
    private static int started = 0;
    private static int stopped = 0;

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    public static boolean isAppInForeground() {
        return started > stopped;
    }

    @Override public void onActivityStarted(Activity activity) { started++; }
    @Override public void onActivityStopped(Activity activity)  { stopped++; }

    @Override public void onActivityCreated(Activity a, Bundle b) {}
    @Override public void onActivityResumed(Activity a) {}
    @Override public void onActivityPaused(Activity a) {}
    @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
    @Override public void onActivityDestroyed(Activity a) {}
}