package ph.codeia.pipad;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.SimpleChannel;


public class TextEntryDialog extends DialogFragment {

    public static void showOnce(FragmentManager fm, String tag) {
        if (fm.findFragmentByTag(tag) == null) {
            new TextEntryDialog().show(fm, tag);
        }
    }

    private Channel<CharSequence> text;
    private Channel<Boolean> backspace;

    public TextEntryDialog() {}

    @Override
    public void onResume() {
        super.onResume();
        AndroidLoaderStore scope = new AndroidLoaderStore(getActivity());
        text = scope.hardGet("text-to-send", SimpleChannel::new);
        backspace = scope.hardGet("hold-backspace", SimpleChannel::new);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        View root = LayoutInflater.from(context).inflate(R.layout.fragment_text_entry, null);
        EditText textToSend = (EditText) root.findViewById(R.id.take_text_to_send);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(root)
                .setPositiveButton("Send", (_d, _i) -> text.send(textToSend.getText()))
                .setNeutralButton("BKSP", null)
                .create();
        dialog.setOnShowListener(_d -> {
            ((InputMethodManager) getActivity()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .showSoftInput(textToSend, InputMethodManager.SHOW_IMPLICIT);
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setOnTouchListener((view, motionEvent) -> {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                view.setPressed(true);
                                backspace.send(true);
                                return true;
                            case MotionEvent.ACTION_CANCEL:  // fallthrough
                            case MotionEvent.ACTION_UP:
                                view.setPressed(false);
                                backspace.send(false);
                                return true;
                            default:
                                view.setPressed(false);
                                return false;
                        }
                    });
        });
        return dialog;
    }

}
