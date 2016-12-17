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

    public static class Event<Out> {
        public final Channel<Boolean> busy = new SimpleChannel<>();
        public final Channel<Out> done = new Replay<>();
        public final Channel<Exception> error = new SimpleChannel<>();
    }

    public interface CheckedRunnable {
        void run() throws Exception;
    }

    private final Do.Map<In, Out> block;
    private final Event<Out> on;

    public Task(Event<Out> on, Do.Map<In, Out> block) {
        this.on = on;
        this.block = block;
    }

    public static Task<Void, Void> of(Event<Void> on, CheckedRunnable block) {
        return new Task<>(on, _void -> {
            block.run();
            return null;
        });
    }

    public static <Out> Task<Void, Out> of(Event<Out> on, Callable<Out> block) {
        return new Task<>(on, _void -> block.call());
    }

    public static Event<Void> now(CheckedRunnable block) {
        Event<Void> event = new Event<>();
        of(event, block).execute();
        return event;
    }

    public static <Out> Event<Out> now(Callable<Out> block) {
        Event<Out> event = new Event<>();
        of(event, block).execute();
        return event;
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
