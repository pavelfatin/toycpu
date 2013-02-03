import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class ScreenImpl extends JComponent implements Screen {
    private static final int SCREEN_WIDTH = 512;
    private static final int SCREEN_HEIGHT = 256;

    private static final int LINE_LENGTH = SCREEN_WIDTH >> 4;

    private static final int FOREGROUND = 0x000000;
    private static final int BACKGROUND = 0xFFFFFF;

    private final Timer myTimer = new Timer(10, new MyTimerListener());
    private BufferedImage myImage;
    private volatile boolean myDirty;

    public ScreenImpl() {
        setFocusable(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
    }

    public void init() {
        if (getTopLevelAncestor() == null) {
            throw new IllegalStateException("Screen must be added to a top-level container before initialization");
        }
        myImage = getGraphicsConfiguration().createCompatibleImage(SCREEN_WIDTH, SCREEN_HEIGHT);
        myTimer.start();
    }

    public void dispose() {
        myTimer.stop();
    }

    public void clear() {
        fill(myImage, BACKGROUND);
        repaint();
    }

    private static void fill(final BufferedImage image, final int color) {
        final Graphics2D g = image.createGraphics();
        g.setColor(new Color(color));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
    }

    public void set(final int address, final int word) {
        final int y = address / LINE_LENGTH;
        final int x = ((address % LINE_LENGTH) << 4);

        int mask = 1;
        for (int i = 0; i < 16; i++) {
            myImage.setRGB(x + i, y, (mask & word) != 0 ? FOREGROUND : BACKGROUND);
            mask = mask << 1;
        }

        myDirty = true;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        g.drawImage(myImage, 0, 0, null);
    }

    private class MyTimerListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {
            if (myDirty) {
                myDirty = false;
                repaint();
            }
        }
    }
}
