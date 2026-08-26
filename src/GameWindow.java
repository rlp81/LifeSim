import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameWindow extends JPanel {
    Random random = new Random();
    List<Organism> worldEntities;
    List<Organism> spawnQueue;
    static public GameLoop engine;
    boolean finished = false;
    Stats stats;
    public GameWindow() {
        this.spawnQueue = new LinkedList<>();
        this.worldEntities = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 2; i++) {
            worldEntities.add(new Predator(random.nextInt(GameConstants.AppWidth), random.nextInt(GameConstants.AppHeight), 20, 20));
        }
        for (int i = 0; i < 100; i++) {
            worldEntities.add(new Organism(random.nextInt(GameConstants.AppWidth), random.nextInt(GameConstants.AppHeight), 15, 15));
        }
        stats = new Stats();
    }

    public void updateLogic () {
        if (finished) { return;}
        for (Organism entity: worldEntities) {
            entity.updateLogic(worldEntities, spawnQueue);
        }
        worldEntities.removeIf(Organism::isDead);

        worldEntities.addAll(spawnQueue);
        spawnQueue.clear();
        if (stats.updateLogic(worldEntities)) {
            finished = true;
            repaint();
        }
        if (GameConstants.headless) {
            if (GameConstants.CurrentFrame % GameConstants.fps == 0) {
                this.repaint();
            }

        } else {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
        Font uiFont = new Font("Arial", Font.BOLD, 24);
        g2d.setFont(uiFont);
        if (!finished && !GameConstants.headless) {
            for (Organism entity : worldEntities) {
                entity.draw(g);
            }
            g2d.setColor(Color.BLACK);
            g2d.drawString("Total Organisms: " + worldEntities.size(), 20, 30);
            g2d.drawString("Year: " + GameConstants.CurrentFrame/240, 20, 60);
        } else {
            g2d.setColor(Color.BLACK);
            if (finished) {
                engine.finishGame();
                stats.draw(g2d);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Peak Predators: " + Stats.peakPred, 20, 60);
                g2d.drawString("Peak Prey: " + Stats.peakPrey, 20, 90);
                g2d.drawString("Year: " + GameConstants.CurrentFrame/240, 20, 30);
                g2d.drawString("Prey Eaten: " + Organism.preyEaten, 20, 120);
                g2d.drawString("Prey died by old age: " + Organism.preyDiedByOld, 20, 150);
            } else {
                stats.draw(g2d);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Predators: " + stats.getPredatorCount(), 20, 60);
                g2d.drawString("Prey: " + stats.getPreyCount(), 20, 90);
                g2d.drawString("Year: " + GameConstants.CurrentFrame/240, 20, 30);
            }

        }

    }

    public static void main(String[] args) {
        if (args.length > 0) {
            if (Objects.equals(args[0], "--headless")) {
                GameConstants.headless = true;
            }

        }
        JFrame window = new JFrame("Life Simulation");

        GameWindow gameCanvas = new GameWindow();
        window.add(gameCanvas);

        window.setSize(GameConstants.AppWidth, GameConstants.AppHeight);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);

        engine = new GameLoop(gameCanvas);
        Thread gameThread = new Thread(engine);
        gameThread.start();
    }
}