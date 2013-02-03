import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardImpl extends KeyAdapter implements Keyboard {
    private volatile int myCode;

    public int getCode() {
        return myCode;
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        myCode = map(e.getKeyCode());
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        myCode = 0;
    }

    private static int map(final int code) {
        switch (code) {
            case KeyEvent.VK_ENTER:
                return 128;
            case KeyEvent.VK_BACK_SPACE:
                return 129;
            case KeyEvent.VK_LEFT:
                return 130;
            case KeyEvent.VK_UP:
                return 131;
            case KeyEvent.VK_RIGHT:
                return 132;
            case KeyEvent.VK_DOWN:
                return 133;
            case KeyEvent.VK_HOME:
                return 134;
            default:
                return code;
        }
    }
}
