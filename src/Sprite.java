import javax.swing.*;
import java.awt.*;

public class Sprite {
    protected int x, y;
    protected int width, height;
    protected Color color;
    public Sprite(int startX, int startY, int w, int h, Color c) {
        x = startX;
        y = startY;
        width = w;
        height = h;
        color = c;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }
}
