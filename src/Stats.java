import java.awt.*;
import java.util.ArrayList;
import java.util.List;
public class Stats {
    private List<Integer> preyHistory;
    private List<Integer> predHistory;
    public static int peakPrey = 0;
    public static int peakPred = 0;

    private int graphX = 50;
    private int graphY = 50;
    private int graphWidth = GameConstants.AppWidth-100;
    private int graphHeight = GameConstants.AppHeight-100;
    public int saveEvery = 240;
    private static int predatorCount = 0;
    private static int preyCount = 0;
    private int finalYear = 0;

    static public int getPredatorCount() {
        return predatorCount;
    }

    static public int getPreyCount() {
        return preyCount;
    }

    public Stats () {
        preyHistory = new ArrayList<>();
        predHistory = new ArrayList<>();
    }

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

    public int getFinalYear() {
        return finalYear;
    }

    public void setFinalYear(int year) {
        finalYear = year;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(graphX, graphY, graphWidth, graphHeight);

        int maxPop = 1;
        for (int p : preyHistory) maxPop = Math.max(maxPop, p);
        for (int p : predHistory) maxPop = Math.max(maxPop, p);

        double xSpacing = (double) graphWidth / Math.max(1, preyHistory.size() - 1);

        g2d.setStroke(new BasicStroke(2));

        g2d.setColor(Color.GREEN);
        drawLineGraph(g2d, preyHistory, maxPop, xSpacing);

        g2d.setColor(Color.RED);
        drawLineGraph(g2d, predHistory, maxPop, xSpacing);
    }

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
