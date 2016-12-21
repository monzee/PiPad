package ph.codeia.pipad;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ph.codeia.androidutils.AndroidChannel;
import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.Links;
import ph.codeia.signal.Replay;
import ph.codeia.signal.SimpleChannel;


public class TrackPadFragment extends Fragment {

    private View pad;
    private TextView pos;
    private Button keyboard;
    private final View[] clickables = new View[3];
    private final View[] holdables = new View[6];
    private Channel.Link links;
    private Remote remote;

    public TrackPadFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trackpad, container, false);
        pad = root.findViewById(R.id.track_pad);
        pos = (TextView) root.findViewById(R.id.the_pos);
        keyboard = (Button) root.findViewById(R.id.do_type);
        int i = 0;
        for (int id : new int[] { R.id.do_right_click, R.id.do_enter, R.id.do_escape, }) {
            clickables[i++] = root.findViewById(id);
        }
        i = 0;
        for (int id : new int[] {
                R.id.do_up, R.id.do_down, R.id.do_left, R.id.do_right,
                R.id.do_page_up, R.id.do_page_down,
        }) {
            holdables[i++] = root.findViewById(id);
        }
        ((AppCompatActivity) getActivity())
                .setSupportActionBar((Toolbar) root.findViewById(R.id.the_toolbar));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidLoaderStore scope = new AndroidLoaderStore(getActivity());
        Task.Subject<Remote> connection = scope.hardGet("on-connect", Task.Subject::new);
        Channel<Pair<Float, Float>> movement = scope.hardGet("movement", Replay::new);
        Channel<Integer> haptic = scope.hardGet("haptic-feedback", AndroidChannel::new);
        Channel<CharSequence> text = scope.hardGet("text-to-send", SimpleChannel::new);
        Channel<Boolean> backspace = scope.hardGet("hold-backspace", SimpleChannel::new);
        links = Links.of(
                haptic.link(pad::performHapticFeedback),
                movement.link(pair -> {
                    float dx = pair.first;
                    float dy = pair.second;
                    tell("x: %f y: %f%nx: %d y: %d", dx, dy, Math.round(dx), Math.round(dy));
                }),
                text.link(chars -> Task.now(() -> remote.type(chars.toString()))),
                backspace.link(hold -> Task.now(() -> remote
                        .backspace(hold ? Remote.Press.HOLD : Remote.Press.RELEASE))),
                connection.done.link(remote -> {
                    this.remote = remote;

                    PadKeys onKeyEvent = new PadKeys(remote);
                    for (View view : clickables) {
                        view.setOnClickListener(onKeyEvent);
                    }
                    for (View view : holdables) {
                        view.setOnTouchListener(onKeyEvent);
                    }

                    PadMotions onPadEvent = new PadMotions(remote, movement, haptic);
                    GestureDetector gestures = new GestureDetector(getContext(), onPadEvent);
                    pad.setOnTouchListener((_v, motionEvent) -> {
                        gestures.onTouchEvent(motionEvent);
                        return true;
                    });

                    Debounce delay = new Debounce(500);
                    keyboard.setOnClickListener(_v -> {
                        if (delay.check()) {
                            new TextEntryDialog().show(getChildFragmentManager(), "text-entry");
                        }
                    });
                }));
    }

    @Override
    public void onPause() {
        super.onPause();
        links.unlink();
        links = null;
        remote = null;
    }

    private void tell(String msg, Object... fmtArgs) {
        pos.setText(String.format(msg, fmtArgs));
    }

}
