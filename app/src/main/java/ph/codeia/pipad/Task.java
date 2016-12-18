package ph.codeia.pipad;

import android.os.AsyncTask;

import java.util.concurrent.Callable;

import ph.codeia.signal.Channel;
import ph.codeia.signal.Replay;
import ph.codeia.signal.SimpleChannel;
import ph.codeia.values.Do;

/**
 * This file is a part of the PiPad project.
 */
public class Task<In, Out> extends AsyncTask<In, Out, Exception> {

    public static class Subject<Out> {
        public final Channel<Boolean> busy = new Replay<>();
        public final Channel<Out> done = new Replay<>();
        public final Channel<Exception> error = new SimpleChannel<>();
    }

    public interface CheckedRunnable {
        void run() throws Exception;
    }

    private final Do.Map<In, Out> block;
    private final Subject<Out> on;

    public Task(Subject<Out> on, Do.Map<In, Out> block) {
        this.on = on;
        this.block = block;
    }

    public static Task<Void, Void> of(Subject<Void> on, CheckedRunnable block) {
        return new Task<>(on, _void -> {
            block.run();
            return null;
        });
    }

    public static <Out> Task<Void, Out> of(Subject<Out> on, Callable<Out> block) {
        return new Task<>(on, _void -> block.call());
    }

    public static Subject<Void> now(CheckedRunnable block) {
        Subject<Void> subject = new Subject<>();
        of(subject, block).execute();
        return subject;
    }

    public static <Out> Subject<Out> now(Callable<Out> block) {
        Subject<Out> subject = new Subject<>();
        of(subject, block).execute();
        return subject;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Exception doInBackground(In... ins) {
        try {
            if (ins.length > 0) {
                for (In in : ins) {
                    publishProgress(block.from(in));
                }
            } else {
                publishProgress(block.from(null));
            }
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    @Override
    protected void onPreExecute() {
        on.busy.send(true);
    }

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(Out... values) {
        for (Out value : values) {
            on.done.send(value);
        }
    }

    @Override
    protected void onPostExecute(Exception e) {
        on.busy.send(false);
        if (e != null) {
            on.error.send(e);
        }
    }

}
