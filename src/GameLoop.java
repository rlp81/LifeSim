/**
 * Dedicated simulation thread that advances {@link GameWindow} at a fixed target rate.
 * <p>
 * Each loop iteration increments {@link GameConstants#CurrentFrame}, calls
 * {@link GameWindow#updateLogic()}, then sleeps so that ticks approach
 * {@link GameConstants#fps}. {@link #finishGame()} clears {@link #isRunning}
 * so the loop exits after the stats screen is shown.
 * </p>
 */
public class GameLoop implements Runnable {
    /** Canvas whose world is stepped once per loop iteration. */
    private GameWindow game;
    /** Shared flag that keeps {@link #run()} iterating until the game finishes. */
    static private boolean isRunning;
    /** Unused stats instance field; a local {@link Stats} is constructed instead. */
    Stats stats;

    /**
     * Binds this loop to the given window. A local {@link Stats} is created and discarded.
     *
     * @param game the panel that owns world entities and rendering
     */
    public GameLoop(GameWindow game) {
        Stats stats = new Stats();
        this.game = game;
    }

    /**
     * Signals the loop to stop after the current iteration.
     */
    public void finishGame() {
        GameLoop.isRunning = false;
    }

    /**
     * Runs the frame loop until {@link #finishGame()} clears {@link #isRunning}.
     * <p>
     * Sleeps for the remainder of the target frame duration when logic finishes early.
     * Interrupted sleep is logged; the loop continues while {@link #isRunning} is true.
     * </p>
     */
    @Override
    public void run() {
        isRunning = true;
        final long targetTime = 1000/GameConstants.fps;
        long startTime;
        long elapsedTime;
        long waitTime;

        while (isRunning) {
            GameConstants.CurrentFrame++;
            startTime = System.currentTimeMillis();
            game.updateLogic();

            java.awt.Toolkit.getDefaultToolkit().sync();

            elapsedTime = System.currentTimeMillis() - startTime;
            waitTime = targetTime - elapsedTime;

            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
