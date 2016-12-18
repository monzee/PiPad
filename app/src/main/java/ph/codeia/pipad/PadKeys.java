package ph.codeia.pipad;

import android.support.annotation.IdRes;
import android.view.MotionEvent;
import android.view.View;

/**
 * This file is a part of the PiPad project.
 */
public class PadKeys implements View.OnClickListener, View.OnTouchListener {

    private final Remote remote;

    public PadKeys(Remote remote) {
        this.remote = remote;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.do_right_click:
                Task.now(remote::rightClick);
                break;
            case R.id.do_enter:
                Task.now(remote::enter);
                break;
            case R.id.do_escape:
                Task.now(remote::escape);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setPressed(true);
                Task.now(() -> didTouch(view.getId(), true));
                return true;
            case MotionEvent.ACTION_UP:  // fallthrough
            case MotionEvent.ACTION_CANCEL:
                view.setPressed(false);
                Task.now(() -> didTouch(view.getId(), false));
                return true;
            default:
                return false;
        }
    }

    private void didTouch(@IdRes int id, boolean down) {
        switch (id) {
            case R.id.do_up:
                remote.up(down ? Remote.Press.HOLD : Remote.Press.RELEASE);
                break;
            case R.id.do_down:
                remote.down(down ? Remote.Press.HOLD : Remote.Press.RELEASE);
                break;
            case R.id.do_left:
                remote.left(down ? Remote.Press.HOLD : Remote.Press.RELEASE);
                break;
            case R.id.do_right:
                remote.right(down ? Remote.Press.HOLD : Remote.Press.RELEASE);
                break;
            case R.id.do_page_up:
                remote.pageUp(down ? Remote.Press.HOLD : Remote.Press.RELEASE);
                break;
            case R.id.do_page_down:
                remote.pageDown(down ? Remote.Press.HOLD : Remote.Press.RELEASE);
                break;
            default:
                break;
        }
    }

}
