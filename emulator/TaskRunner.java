public interface TaskRunner extends Runnable {
    void setSpeed(int speed);

    void pause();

    void resume();
}
