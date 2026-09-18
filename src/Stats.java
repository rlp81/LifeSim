import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Population counters, peak tracking, and end-of-run line graphs for the simulation.
 * <p>
 * Each simulated year this class recounts predators and prey, records history
 * series, and can signal that the run should end when either population hits
 * zero. {@link #draw(Graphics2D)} plots prey (green), predators (red), deaths
 * by old age (blue), and kills by predators (black).
 * </p>
 */
public class Stats {
    /** Yearly prey population samples plotted in green. */
    private List<Integer> preyHistory;
    /** Yearly predator population samples plotted in red. */
    private List<Integer> predHistory;
    /** Highest prey count observed in a yearly census. */
    public static int peakPrey = 0;
    /** Highest predator count observed in a yearly census. */
    public static int peakPred = 0;

    /** Left edge of the graph rectangle, in pixels. */
    private int graphX = 50;
    /** Top edge of the graph rectangle, in pixels. */
    private int graphY = 50;
    /** Width of the graph rectangle, in pixels. */
    private int graphWidth = GameConstants.AppWidth-100;
    /** Height of the graph rectangle, in pixels. */
    private int graphHeight = GameConstants.AppHeight-100;
    /** Frame interval between population snapshots; equals one simulated year. */
    public int saveEvery = GameConstants.YEAR;
    /** Most recent yearly predator census. */
    private static int predatorCount = 0;
    /** Most recent yearly prey census. */
    private static int preyCount = 0;
    /** Simulated year at which the run ended (extinction or manual quit). */
    private int finalYear = 0;

    /**
     * Returns the predator count from the last yearly census.
     *
     * @return number of living predators at the last snapshot
     */
    static public int getPredatorCount() {
        return predatorCount;
    }

    /**
     * Returns the prey count from the last yearly census.
     *
     * @return number of living prey at the last snapshot
     */
    static public int getPreyCount() {
        return preyCount;
    }

    /**
     * Creates empty predator and prey history series ready for yearly samples.
     */
    public Stats () {
        preyHistory = new ArrayList<>();
        predHistory = new ArrayList<>();
    }

    /**
     * On year boundaries, recounts living predators and prey and records peaks.
     * <p>
     * Returns {@code true} when either population is zero so the simulation can
     * stop. Between year boundaries this method is a no-op and returns {@code false}.
     * </p>
     *
     * @param worldEntities all currently living organisms
     * @return {@code true} if a population has gone extinct and the run should end
     */
    public boolean updateLogic(List<Organism> worldEntities) {
        if (GameConstants.CurrentFrame % saveEvery == 0) {
            predatorCount = 0;
            preyCount = 0;
            for (Organism org : worldEntities) {
                if (org instanceof Predator) {
                    predatorCount++;
                } else {
                    preyCount++;
                }
            }

            if (predatorCount == 0 || preyCount == 0) { //predatorCount == 0 || preyCount == 0
                finalYear = (int) (GameConstants.CurrentFrame / saveEvery);
                return true;
            }
            if (predatorCount > peakPred) {
                peakPred = predatorCount;
            }
            if (preyCount > peakPrey) {
                peakPrey = preyCount;
            }
            predHistory.add(predatorCount);
            preyHistory.add(preyCount);
        }
        return false;
    }

    /**
     * Returns the simulated year at which the run ended.
     *
     * @return final year recorded on extinction or via {@link #setFinalYear(int)}
     */
    public int getFinalYear() {
        return finalYear;
    }

    /**
     * Overrides the recorded end year (used when the player quits early).
     *
     * @param year simulated year to store as the run's end
     */
    public void setFinalYear(int year) {
        finalYear = year;
    }

    /**
     * Draws the translucent graph background and all population/death series.
     *
     * @param g2d the graphics context of the game canvas
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(graphX, graphY, graphWidth, graphHeight);

        int maxPop = 1;
        for (int p : preyHistory) maxPop = Math.max(maxPop, p);
        for (int p : predHistory) maxPop = Math.max(maxPop, p);
        for (int p : Organism.eatenByPred) maxPop = Math.max(maxPop, p);
        for (int p : Organism.diedOfOld) maxPop = Math.max(maxPop, p);

        double xSpacing = (double) graphWidth / Math.max(1, preyHistory.size() - 1);

        g2d.setStroke(new BasicStroke(2));

        g2d.setColor(Color.GREEN);
        drawLineGraph(g2d, preyHistory, maxPop, xSpacing);

        g2d.setColor(Color.RED);
        drawLineGraph(g2d, predHistory, maxPop, xSpacing);

        g2d.setColor(Color.BLUE);
        drawLineGraph(g2d, Organism.diedOfOld, maxPop, xSpacing);

        g2d.setColor(Color.BLACK);
        drawLineGraph(g2d, Organism.eatenByPred, maxPop, xSpacing);
    }

    /**
     * Connects consecutive history samples as a polyline scaled to {@code maxPop}.
     *
     * @param g2d      graphics context to draw into
     * @param history  yearly samples to plot; ignored if fewer than two points
     * @param maxPop   value that maps to the top of the graph
     * @param xSpacing horizontal pixels between consecutive samples
     */
    private void drawLineGraph(Graphics2D g2d, List<Integer> history, int maxPop, double xSpacing) {
        if (history.size() < 2) return;

        for (int i = 0; i < history.size() - 1; i++) {
            int x1 =  (int) Math.round(graphX + (i * xSpacing));

            int y1 = graphY + graphHeight-2 - (int)(((double)history.get(i) / maxPop) * graphHeight);

            int x2 = (int) Math.round(graphX + ((i + 1) * xSpacing));
            int y2 = graphY + graphHeight-2 - (int)(((double)history.get(i + 1) / maxPop) * graphHeight);

            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}
