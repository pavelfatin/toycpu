import static java.lang.Math.*;

public class TaskRunnerImpl implements TaskRunner {
    private static final int INITIAL_BATCH_SIZE = 1000;
    private static final int MIN_CYCLE_DELAY = 1;
    private static final int MAX_CYCLE_DELAY = 10;
    private static final int MAX_CYCLE_DURATION = 50;

    private final Task myTask;

    private final Object myLock = new Object();
    private volatile boolean myPaused;
    private volatile int mySpeed = 0;

    public TaskRunnerImpl(final Task task) {
        myTask = task;
    }

    public void run() {
        myTask.reset();

        long batchSize = INITIAL_BATCH_SIZE;

        while (true) {
            long previousTime = System.nanoTime();

            myTask.advance(batchSize);

            final long targetSpeed = mySpeed;

            final long processingSpeed = 1000000L * batchSize / (System.nanoTime() - previousTime);

            if (targetSpeed > 0) {
                final long delay = min(MAX_CYCLE_DELAY, (int) round(
                        MAX_CYCLE_DURATION * (1.0D - (double) targetSpeed / processingSpeed)));

                if (delay >= MIN_CYCLE_DELAY) {
                    previousTime = System.nanoTime();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        return;
                    }
                    final long sleepTime = System.nanoTime() - previousTime;

                    batchSize = max(1, (int) (sleepTime / 1000000.0D *
                            targetSpeed * processingSpeed / (processingSpeed - targetSpeed)));
                } else {
                    batchSize = MAX_CYCLE_DURATION * processingSpeed;
                }
            } else {
                batchSize = MAX_CYCLE_DURATION * processingSpeed;
            }

            if (myPaused) {
                synchronized (myLock) {
                    try {
                        myLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } else {
                if (Thread.interrupted()) {
                    return;
                }
            }
        }
    }

    @Override
    public void setSpeed(final int speed) {
        mySpeed = speed;
    }

    @Override
    public void pause() {
        myPaused = true;
    }

    @Override
    public void resume() {
        myPaused = false;
        synchronized (myLock) {
            myLock.notifyAll();
        }
    }
}
