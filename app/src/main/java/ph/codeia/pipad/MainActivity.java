package ph.codeia.pipad;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import ph.codeia.androidutils.AndroidChannel;
import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.Links;


public class MainActivity extends AppCompatActivity {

    private static final String HOST = "192.168.0.28";
    private static final int PORT = 6000;

    private Channel.Link links;
    private Remote remote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments.findFragmentByTag("trackpad") == null) {
            fragments.beginTransaction()
                    .add(R.id.activity_main, new TrackPadFragment(), "trackpad")
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AndroidLoaderStore scope = new AndroidLoaderStore(this);
        Task.Event<Remote> connection = scope.hardGet("on-connect", Task.Event::new);
        Channel<IOException> reconnect = scope.hardGet("reconnect-on-error", AndroidChannel::new);
        AtomicInteger attempts = new AtomicInteger(0);
        Debounce retryPeriod = new Debounce(1000);
        links = Links.of(
                connection.done.link(it -> remote = it),
                connection.error.link(error -> {
                    tell(error.getMessage());
                    scope.clear("should-connect");
                }),
                reconnect.link(error -> {
                    if (!retryPeriod.check()) {
                        return;
                    }
                    if (error != null) {
                        Log.d("mz", "socket write error: ", error);
                    }
                    Log.d("mz", "connection attempts: " + attempts.incrementAndGet());
                    cleanlyDisconnect();
                    Task.of(connection, () -> new Remote(HOST, PORT, reconnect)).execute();
                    scope.put("should-connect", false);
                }));
        if (scope.get("should-connect", true)) {
            reconnect.send(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        links.unlink();
        links = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            cleanlyDisconnect();
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
        Toast.makeText(this, String.format(msg, fmtArgs), Toast.LENGTH_SHORT).show();
    }

}
