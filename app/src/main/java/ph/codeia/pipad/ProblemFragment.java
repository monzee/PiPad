package ph.codeia.pipad;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.SimpleChannel;


public class ProblemFragment extends Fragment {

    private Channel<MainActivity.To> nav;

    public ProblemFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_problem, container, false);
        TextView message = (TextView) root.findViewById(R.id.the_message);
        message.setText(getString(R.string.cant_connect, PiPad.config().host()));
        root.findViewById(R.id.do_launch_prefs)
                .setOnClickListener(_v -> nav.send(MainActivity.To.SETTINGS));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        nav = new AndroidLoaderStore(getActivity()).hardGet("navigation", SimpleChannel::new);
    }

}
