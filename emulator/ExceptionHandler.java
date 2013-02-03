import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JScrollPane component = new JScrollPane(createTextArea(format(e)));
                JOptionPane.showMessageDialog(findVisibleWindow(), component, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static String format(final Throwable e) {
        final StringWriter writer = new StringWriter();

        e.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }

    private static JTextArea createTextArea(final String s) {
        final JTextArea area = new JTextArea(s);

        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        area.setColumns(80);
        area.setRows(20);

        return area;
    }

    private static Window findVisibleWindow() {
        for (final Window window : Window.getWindows()) {
            if (window.isShowing()) {
                return window;
            }
        }
        return null;
    }
}
