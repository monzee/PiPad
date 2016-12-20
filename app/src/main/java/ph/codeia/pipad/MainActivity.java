package ph.codeia.pipad;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ph.codeia.androidutils.AndroidChannel;
import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.Links;
import ph.codeia.signal.SimpleChannel;


public class MainActivity extends AppCompatActivity {

    public enum To { PAD, SETTINGS, PROBLEM, NOWHERE }

    private TextView status;
    private Channel.Link links;
    private Remote remote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.the_toolbar));
        status = (TextView) findViewById(R.id.the_status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("mz", "activity#resume");
        AndroidLoaderStore scope = new AndroidLoaderStore(this);
        Config conf = scope.hardGet("config", PiPad::config);
        Channel<To> nav = scope.hardGet("navigation", SimpleChannel::new);
        Task.Subject<Remote> connection = scope.hardGet("on-connect", Task.Subject::new);
        Channel<IOException> reconnect = scope.hardGet("reconnect-on-error", AndroidChannel::new);
        Channel<Void> settings = scope.hardGet("settings-changed", SimpleChannel::new);
        AtomicInteger attempts = new AtomicInteger(0);
        Debounce retryPeriod = new Debounce(1000);
        links = Links.of(
                nav.link(this::go),
                connection.busy.link(connecting -> {
                    if (connecting) {
                        status.setVisibility(View.VISIBLE);
                        status.setText(getString(R.string.connecting, conf.host(), conf.port()));
                    } else {
                        status.setVisibility(View.GONE);
                    }
                }),
                connection.done.link(it -> {
                    remote = it;
                    go(To.PAD);
                }),
                connection.error.link(error -> {
                    scope.clear("should-connect");
                    tell(error.getMessage());
                    go(To.PROBLEM);
                }),
                settings.link(_v -> reconnect.send(null)),
                reconnect.link(error -> {
                    if (!retryPeriod.check()) {
                        return;
                    }
                    if (error != null) {
                        Log.d("mz", "socket write error: ", error);
                    }
                    Log.d("mz", "connection attempts: " + attempts.incrementAndGet());
                    cleanlyDisconnect();
                    Task.of(connection, () -> new Remote(conf.host(), conf.port(), reconnect))
                            .execute();
                    scope.put("should-connect", false);
                }));
        if (scope.get("should-connect", true)) {
            reconnect.send(null);
        } else {
            Log.d("mz", "not reconnecting");
        }
    }

    @Override
    protected void onPause() {
        Log.d("mz", "activity#pause");
        super.onPause();
        links.unlink();
        links = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            Log.d("mz", "activity#destroy");
            cleanlyDisconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.do_launch_prefs:
                go(To.SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void go(To where) {
        FragmentManager fragments = getSupportFragmentManager();
        int container = R.id.fragment_container;
        switch (where) {
            case PAD:
                if (fragments.findFragmentByTag("trackpad") == null) {
                    fragments.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(container, new TrackPadFragment(), "trackpad")
                            .commit();
                }
                break;
            case PROBLEM:
                if (fragments.findFragmentByTag("problem") == null) {
                    fragments.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(container, new ProblemFragment(), "problem")
                            .commit();
                }
                break;
            case SETTINGS:
                if (fragments.findFragmentByTag("settings") == null) {
                    fragments.beginTransaction()
                            .setCustomAnimations(
                                    R.anim.fade_slide_in_right, android.R.anim.fade_out,
                                    android.R.anim.fade_in, R.anim.fade_slide_out_right)
                            .replace(container, new SettingsFragment(), "settings")
                            .addToBackStack("settings")
                            .commit();
                }
                break;
            case NOWHERE:
                List<Fragment> fs = fragments.getFragments();
                if (fs != null) {
                    for (Fragment fragment : fs) {
                        fragments.beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                                .remove(fragment)
                                .commit();
                    }
                }
                break;
        }
    }

    private void cleanlyDisconnect() {
       if (remote != null) {
           Remote old = remote;
           remote = null;
           Task.now(old::dispose);
       }
    }

    private void tell(String msg, Object... fmtArgs) {
        Toast.makeText(this, String.format(msg, fmtArgs), Toast.LENGTH_LONG).show();
    }

}
