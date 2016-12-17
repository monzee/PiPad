package ph.codeia.pipad;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import ph.codeia.signal.Channel;

/**
 * This file is a part of the PiPad project.
 */
public class Remote {

    private final Socket socket;
    private final DataOutputStream out;
    private final Channel<? super IOException> errors;

    public enum Press { TAP, HOLD, RELEASE }

    public Remote(String host, int port, Channel<? super IOException> errors) throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        this.errors = errors;
    }

    public void moveBy(int x, int y) {
        if (x == 0 && y == 0) {
            return;
        }
        try {
            out.writeByte('m');
            out.writeShort(x);
            out.writeShort(y);
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void click() {
        click(Press.TAP);
    }

    public void click(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte('c');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void rightClick() {
        try {
            out.writeByte('r');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void escape() {
        try {
            out.writeByte('X');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void enter() {
        try {
            out.writeByte('E');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void up() {
        up(Press.TAP);
    }

    public void up(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte('n');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void down() {
        down(Press.TAP);
    }

    public void down(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte('s');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void left() {
        left(Press.TAP);
    }

    public void left(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte('w');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void right() {
        right(Press.TAP);
    }

    public void right(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte('e');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void pageUp() {
        pageUp(Press.TAP);
    }

    public void pageUp(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte('[');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void pageDown() {
        pageDown(Press.TAP);
    }

    public void pageDown(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte(']');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void backspace() {
        backspace(Press.TAP);
    }

    public void backspace(Press state) {
        try {
            holdOrRelease(state);
            out.writeByte('~');
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void type(String text) {
        try {
            out.writeByte('$');
            out.writeShort(text.length());
            out.writeBytes(text);
        } catch (IOException e) {
            errors.send(e);
        }
    }

    public void dispose() {
        try {
            out.writeByte(4);
        } catch (IOException ignored) {}
        try {
            out.close();
            socket.close();
        } catch (IOException ignored) {}
    }

    private void holdOrRelease(Press state) throws IOException {
        switch (state) {
            case HOLD:
                out.writeByte('+');
                break;
            case RELEASE:
                out.writeByte('-');
                break;
            case TAP:
                break;
        }
    }

}
