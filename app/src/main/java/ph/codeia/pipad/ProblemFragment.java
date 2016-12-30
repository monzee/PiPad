package ph.codeia.pipad;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import ph.codeia.androidutils.AndroidChannel;
import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.Links;
import ph.codeia.signal.SimpleChannel;


public class ProblemFragment extends Fragment {

    private Channel<MainActivity.To> nav;
    private Channel<IOException> reconnect;
    private Channel.Link links;
    private View doRetry;
    private TextView message;

    public ProblemFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_problem, container, false);
        message = (TextView) root.findViewById(R.id.the_message);
        doRetry = root.findViewById(R.id.do_retry);
        doRetry.setOnClickListener(_v -> reconnect.send(null));
        root.findViewById(R.id.do_launch_prefs)
                .setOnClickListener(_v -> nav.send(MainActivity.To.SETTINGS));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidLoaderStore scope = new AndroidLoaderStore(getActivity());
        Config conf = scope.hardGet("config", PiPad::config);
        nav = scope.hardGet("navigation", SimpleChannel::new);
        reconnect = scope.hardGet("reconnect-on-error", AndroidChannel::new);
        message.setText(getString(R.string.cant_connect, conf.host()));
        Task.Subject<Remote> connection = scope.hardGet("on-connect", Task.Subject::new);
        links = Links.of(
                connection.busy.link(connecting -> {
                    if (connecting) {
                        doRetry.setEnabled(false);
                        message.setVisibility(View.INVISIBLE);
                    } else {
                        doRetry.setEnabled(true);
                        message.setVisibility(View.VISIBLE);
                    }
                }));
    }

    @Override
    public void onPause() {
        super.onPause();
        links.unlink();
        links = null;
    }

}
