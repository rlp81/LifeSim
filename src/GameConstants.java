/**
 * Global simulation tunables shared by the engine, world, and organisms.
 * <p>
 * Holds canvas size, frame timing, calendar length, and runtime flags such as
 * headless mode. Values are read throughout the simulation; {@link #CurrentFrame}
 * and {@link #headless} are mutated by the game loop and entry point.
 * </p>
 */
public class GameConstants {
    /** Width of the simulation window in pixels. */
    public static final int AppWidth = 800;
    /** Height of the simulation window in pixels. */
    public static final int AppHeight = 800;
    /** Monotonic frame counter advanced once per tick of {@link GameLoop}. */
    public static long CurrentFrame = 0;
    /** Target frames per second used to compute the game-loop sleep interval. */
    public static final int fps = 1200;
    /**
     * When {@code true}, entity sprites are skipped and the canvas is redrawn
     * only once per simulated second so the simulation can run faster.
     */
    public static boolean headless = false;
    /** Number of frames that constitute one simulated year. */
    public static final int YEAR = 240;
}
