import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainFrame extends JFrame {
    private static final int NORMAL_SPEED = 2500;
    private static final int SPEED_DELTA = 800;

    private final int[] myROM = new int[32 * 1024];
    private final ScreenImpl myScreen = new ScreenImpl();
    private final TaskRunner myEmulationRunner;
    private final ExecutorService myExecutor;

    private final ButtonModel myPauseButtonModel;
    private final BoundedRangeModel mySpeedSliderModel;

    private Future<?> myFuture;
    private boolean myPaused;

    public MainFrame() {
        final OpenAction openAction = new OpenAction();
        final PauseAction pauseAction = new PauseAction();
        final RestartAction restartAction = new RestartAction();

        register(openAction);
        register(pauseAction);
        register(restartAction);

        final JButton openButton = new JButton(openAction);
        final JSlider speedSlider = createSpeedSlider();
        final JToggleButton myPauseButton = new JToggleButton(pauseAction);
        final JButton restartButton = new JButton(restartAction);

        openButton.setFocusable(false);
        speedSlider.setFocusable(false);
        myPauseButton.setFocusable(false);
        restartButton.setFocusable(false);

        myPauseButtonModel = myPauseButton.getModel();
        mySpeedSliderModel = speedSlider.getModel();

        speedSlider.setToolTipText("Emulation speed");

        final JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.add(openButton);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(new JLabel("Speed:"));
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(speedSlider);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(myPauseButton);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(restartButton);

        final JPanel screenPanel = new JPanel(new BorderLayout());
        screenPanel.setBorder(new LineBorder(Color.GRAY, 1));
        screenPanel.add(myScreen, BorderLayout.CENTER);

        final JPanel content = new JPanel(new BorderLayout(0, 5));
        content.setBorder(new EmptyBorder(5, 5, 5, 5));
        content.add(toolbar, BorderLayout.NORTH);
        content.add(screenPanel, BorderLayout.CENTER);

        getContentPane().add(content);

        myScreen.init();
        myScreen.clear();

        final KeyboardImpl keyboard = new KeyboardImpl();
        myScreen.addKeyListener(keyboard);

        speedSlider.addChangeListener(new MySliderListener());

        addWindowListener(new MyWindowListener());

        final EmulationTask task = new EmulationTask(myROM, new int[32 * 1024], myScreen, keyboard);
        myEmulationRunner = new TaskRunnerImpl(task);

        myExecutor = Executors.newSingleThreadExecutor();

        setTitle(formatTitle(null));

        updateSpeed();
    }

    private void register(final Action action) {
        final JRootPane root = getRootPane();
        final KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        root.registerKeyboardAction(action, keyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private static JSlider createSpeedSlider() {
        final JSlider slider = new JSlider(new DefaultBoundedRangeModel(3, 0, 0, 7));

        slider.setLabelTable(createSpeedLabelsTable());
        slider.setMinorTickSpacing(1);
        slider.setMajorTickSpacing(1);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);

        return slider;
    }

    private static Hashtable<Integer, JComponent> createSpeedLabelsTable() {
        final Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();

        labels.put(0, new JLabel("-3"));
        labels.put(1, new JLabel("-2"));
        labels.put(2, new JLabel("-1"));
        labels.put(3, new JLabel("0"));
        labels.put(4, new JLabel("+1"));
        labels.put(5, new JLabel("+2"));
        labels.put(6, new JLabel("+3"));
        labels.put(7, new JLabel("Max"));

        return labels;
    }

    private void open() {
        final boolean wasPaused = myPaused;

        if (!myPaused) {
            pause();
        }

        final JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File file = chooser.getSelectedFile();
            load(file);
        } else {
            if (!wasPaused) {
                resume();
            }
        }
    }

    public void load(final File file) {
        final int[] buffer = new int[myROM.length];

        try {
            Utilities.load(file, buffer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Error loading file", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.arraycopy(buffer, 0, myROM, 0, myROM.length);

        setTitle(formatTitle(file));

        restart();
    }

    private static String formatTitle(final File file) {
        final String name = file == null ? "Empty" : file.getName();
        return "Emulator - " + name;
    }

    public void restart() {
        stop();
        start();
        resume();
    }

    private void start() {
        myScreen.clear();
        myFuture = myExecutor.submit(myEmulationRunner);
    }

    private void stop() {
        if (myFuture != null) {
            myFuture.cancel(true);
        }
    }

    public void togglePause() {
        if (myPaused) {
            resume();
        } else {
            pause();
        }
    }

    private void pause() {
        myPauseButtonModel.setSelected(true);
        myEmulationRunner.pause();
        myPaused = true;
    }

    private void resume() {
        myPauseButtonModel.setSelected(false);
        myEmulationRunner.resume();
        myPaused = false;
    }

    private void updateSpeed() {
        final int value = mySpeedSliderModel.getValue();
        final int speed = value == 7 ? 0 : NORMAL_SPEED + SPEED_DELTA * (value - 3);
        myEmulationRunner.setSpeed(speed);
    }

    private class MySliderListener implements ChangeListener {
        @Override
        public void stateChanged(final ChangeEvent e) {
            updateSpeed();
        }
    }

    private class OpenAction extends AbstractAction {
        private OpenAction() {
            super("Open");
            putValue(AbstractAction.SHORT_DESCRIPTION, "Open a file (Ctrl + O)");
            putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(AbstractAction.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            open();
        }
    }

    private class PauseAction extends AbstractAction {
        private PauseAction() {
            super("Pause");
            putValue(AbstractAction.SHORT_DESCRIPTION, "Pause emulation (P)");
            putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            togglePause();
        }
    }

    private class RestartAction extends AbstractAction {
        private RestartAction() {
            super("Restart");
            putValue(AbstractAction.SHORT_DESCRIPTION, "Restart emulation (R)");
            putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            restart();
        }
    }

    private class MyWindowListener extends WindowAdapter {
        @Override
        public void windowClosed(final WindowEvent e) {
            myScreen.dispose();
            myExecutor.shutdownNow();
        }
    }
}
