import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.io.File;

public class Main {
    public static void main(final String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        final File file = args.length > 0 ? new File(args[0]) : null;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    doRun(file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static void doRun(final File file) throws Exception {
        setLookAndFeel("Nimbus");

        final MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (file != null) {
            frame.load(file);
        }
    }

    public static void setLookAndFeel(final String name) throws Exception {
        for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (name.equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    }
}
