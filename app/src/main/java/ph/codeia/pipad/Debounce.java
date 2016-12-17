package ph.codeia.pipad;

/**
 * This file is a part of the PiPad project.
 */

public class Debounce {

    private final long period;
    private long previous = -1;

    public Debounce(long period) {
        this.period = period;
    }

    public boolean check() {
        long now = System.currentTimeMillis();
        if (previous != -1 && now - previous < period) {
            return false;
        }
        previous = now;
        return true;
    }

}
