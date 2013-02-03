public class EmulationTask implements Task {
    private static final int C_COMMAND = 1 << 15;
    private static final int ALU_SOURCE = 1 << 12;
    private static final int ZX = 1 << 11;
    private static final int NX = 1 << 10;
    private static final int ZY = 1 << 9;
    private static final int NY = 1 << 8;
    private static final int F = 1 << 7;
    private static final int NO = 1 << 6;
    private static final int LOAD_A = 1 << 5;
    private static final int LOAD_D = 1 << 4;
    private static final int WRITE_M = 1 << 3;
    private static final int JLT = 1 << 2;
    private static final int JEQ = 1 << 1;
    private static final int JGT = 1;

    private static final int SCREEN_ADDRESS = 16384;
    private static final int KEYBOARD_ADDRESS = 24576;

    private final int[] myROM;
    private final int[] myRAM;
    private final Screen myScreen;
    private final Keyboard myKeyboard;

    private int myPC;
    private int myA;
    private int myD;

    public EmulationTask(final int[] rom, final int[] ram,
                         final Screen screen, final Keyboard keyboard) {
        myROM = rom;
        myRAM = ram;
        myScreen = screen;
        myKeyboard = keyboard;
    }

    public void advance(final long steps) {
        for (long i = 0; i < steps; i++) {
            final int op = myROM[myPC];

            if ((op & C_COMMAND) != 0) {
                int x = ((op & ZX) != 0) ? 0 : myD;

                int y = ((op & ZY) != 0) ? 0
                        : (op & ALU_SOURCE) != 0 ? myA == KEYBOARD_ADDRESS ? myKeyboard.getCode() : myRAM[myA] : myA;

                if ((op & NX) != 0) x = ~x;
                if ((op & NY) != 0) y = ~y;

                int out = ((op & F) != 0 ? x + y : x & y);
                if ((op & NO) != 0) out = ~out;

                final boolean jump = (op & JLT) != 0 && out < 0 ||
                        (op & JEQ) != 0 && out == 0 ||
                        (op & JGT) != 0 && out > 0;

                if ((op & WRITE_M) != 0) {
                    myRAM[myA] = out;
                    if (myA >= SCREEN_ADDRESS && myA < KEYBOARD_ADDRESS) {
                        myScreen.set(myA - SCREEN_ADDRESS, out);
                    }
                }

                if ((op & LOAD_A) != 0) myA = out;

                if ((op & LOAD_D) != 0) myD = out;

                myPC = jump ? myA : myPC + 1;
            } else {
                myA = op;
                myPC++;
            }
        }
    }

    @Override
    public void reset() {
        myPC = 0;
        myA = 0;
        myD = 0;
    }
}
