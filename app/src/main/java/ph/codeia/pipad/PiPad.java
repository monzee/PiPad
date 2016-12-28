package ph.codeia.pipad;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;

/**
 * This file is a part of the PiPad project.
 */

public class PiPad extends Application {

    private static Config CONFIG = Config.DEFAULT;

    public static Config config() {
        return CONFIG;
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
        CONFIG = load(PreferenceManager.getDefaultSharedPreferences(this));
    }

    private Config load(final SharedPreferences prefs) {
        return new Config() {
            @Override
            public String host() {
                return prefs.getString(Config.HOST, Config.DEFAULT.host());
            }

            @Override
            public int port() {
                String defaultValue = String.valueOf(Config.DEFAULT.port());
                return Integer.valueOf(prefs.getString(Config.PORT, defaultValue));
            }

            @Override
            public boolean rightHanded() {
                return prefs.getBoolean(Config.RIGHT_HANDED, Config.DEFAULT.rightHanded());
            }

            @Override
            public float speed() {
                int defaultValue = (int) (Config.DEFAULT.speed() * 100) - 100;
                return 1 + prefs.getInt(Config.SPEED, defaultValue) / 100f;
            }
        };
    }
}
