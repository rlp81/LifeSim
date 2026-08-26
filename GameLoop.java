public class GameLoop implements Runnable {
    private GameWindow game;
    private boolean isRunning;

    public GameLoop(GameWindow game) {
        this.game = game;
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