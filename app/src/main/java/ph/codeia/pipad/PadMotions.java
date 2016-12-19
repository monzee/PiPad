package ph.codeia.pipad;

import android.support.v4.util.Pair;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;

import ph.codeia.signal.Channel;

/**
 * This file is a part of the PiPad project.
 */
public class PadMotions extends GestureDetector.SimpleOnGestureListener {

    private final Debounce rateLimit = new Debounce(16);  // ~60Hz
    private final Remote remote;
    private final Channel<Pair<Float, Float>> movement;
    private final Channel<Integer> hapticFeedback;
    private final float speed = PiPad.CONFIG.speed();

    public PadMotions(
            Remote remote,
            Channel<Pair<Float, Float>> movement,
            Channel<Integer> hapticFeedback) {
        this.remote = remote;
        this.movement = movement;
        this.hapticFeedback = hapticFeedback;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
        if (!rateLimit.check()) {
            return false;
        }
        remote.moveBy(Math.round(dx * speed * -1), Math.round(dy * speed * -1));
        movement.send(new Pair<>(dx, dy));
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        remote.click();
        hapticFeedback.send(HapticFeedbackConstants.VIRTUAL_KEY);
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        remote.click();
        remote.click();
        hapticFeedback.send(HapticFeedbackConstants.VIRTUAL_KEY);
        return true;
    }

}
