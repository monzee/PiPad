package ph.codeia.pipad;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * This file is a part of the PiPad project.
 */

public class PiPad extends Application {

    public interface Config {
        String host();
        int port();
        boolean leftHanded();
        float pointerSpeed();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        //LeakCanary.install(this);
    }

}
