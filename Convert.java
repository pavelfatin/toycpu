import java.io.*;

public class Convert {
    public static void main(final String[] args) throws IOException {
        if (args.length == 2) {
            final File input = new File(args[0]);
            final File output = new File(args[1]);
            convert(input, output);
        } else {
            System.out.println("Usage: Convert <input file> <output file>");
        }
    }

    private static void convert(final File input, final File output) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(input));
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            try {
                writer.write("v2.0 raw");
                writer.newLine();

                while (true) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    writer.write(Integer.toHexString(Integer.parseInt(line, 2)));
                    writer.write(' ');
                }

                writer.flush();
            } finally {
                writer.close();
            }
        } finally {
            reader.close();
        }
    }
}
