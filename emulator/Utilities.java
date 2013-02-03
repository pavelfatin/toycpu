import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utilities {
    private Utilities() {
    }

    public static void load(final File file, final int[] buffer) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            int i = 0;
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                buffer[i] = Integer.parseInt(line, 2);
                i++;
            }
        } catch (NumberFormatException e) {
            throw new DataFormatException(e);
        } finally {
            reader.close();
        }
    }
}
