import java.awt.*;
import java.util.ArrayList;
import java.util.List;
public class Stats {
    private List<Integer> preyHistory;
    private List<Integer> predHistory;

    private int graphX = 50;
    private int graphY = 50;
    private int graphWidth = GameConstants.AppWidth-100;
    private int graphHeight = GameConstants.AppHeight-100;
    private int saveEvery = 240;

    public Stats () {
        preyHistory = new ArrayList<>();
        predHistory = new ArrayList<>();
    }

    public boolean updateLogic(List<Organism> worldEntities) {
        if (GameConstants.CurrentFrame % saveEvery == 0) {
            int predatorCount = 0;
            int preyCount = 0;
            for (Organism org : worldEntities) {
                if (org instanceof Predator) {
                    predatorCount++;
                } else {
                    preyCount++;
                }
            }

            if (predatorCount == 0 || preyCount == 0) { //predatorCount == 0 || preyCount == 0
                return true;
            }

            predHistory.add(predatorCount);
            preyHistory.add(preyCount);
        }
        return false;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(graphX, graphY, graphWidth, graphHeight);

        // 2. Find the highest population peak to dynamically scale the Y-axis
        int maxPop = 1; // Default to 1 to prevent division by zero
        for (int p : preyHistory) maxPop = Math.max(maxPop, p);
        for (int p : predHistory) maxPop = Math.max(maxPop, p);

        // 3. Calculate how far apart each point should be on the X-axis
        int xSpacing = graphWidth / Math.max(1, preyHistory.size() - 1);

        // 4. Draw the population lines
        g2d.setStroke(new BasicStroke(2)); // Make the lines slightly thicker

        g2d.setColor(Color.GREEN);
        drawLineGraph(g2d, preyHistory, maxPop, xSpacing);

        g2d.setColor(Color.RED);
        drawLineGraph(g2d, predHistory, maxPop, xSpacing);
    }

    private void drawLineGraph(Graphics2D g2d, List<Integer> history, int maxPop, int xSpacing) {
        if (history.size() < 2) return; // Need at least two points to draw a line

        for (int i = 0; i < history.size() - 1; i++) {
            int x1 = graphX + (i * xSpacing);
            // Invert the Y coordinate because Y=0 is the top of the screen
            int y1 = graphY + graphHeight - (int)(((double)history.get(i) / maxPop) * graphHeight);

            int x2 = graphX + ((i + 1) * xSpacing);
            int y2 = graphY + graphHeight - (int)(((double)history.get(i + 1) / maxPop) * graphHeight);

            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}
