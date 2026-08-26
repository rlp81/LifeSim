public class GameLoop implements Runnable {
    private GameWindow game;
    static private boolean isRunning;
    Stats stats;
    public GameLoop(GameWindow game) {
        Stats stats = new Stats();
        this.game = game;
    }

    public void finishGame() {
        GameLoop.isRunning = false;
    }

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
            game.repaint();

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