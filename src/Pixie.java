import javax.swing.*;
import java.awt.*;
import java.util.List;
public abstract class Pixie extends Sprite {
    public Pixie(int startX, int startY) {
        super(startX, startY, 10, 10, Color.GREEN);
    }

    public Pixie(int startX, int startY, int sizeX, int sizeY) {
        super(startX, startY, sizeX, sizeY, Color.GREEN);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void changeColor(Color newColor) {
        this.color = newColor;
    }

    public void move(int x, int y) {
        this.x += x;
        this.y += y;
    }
}
