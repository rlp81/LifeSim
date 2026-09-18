import java.awt.*;

/**
 * Movable sprite with convenience constructors for life-simulation agents.
 * <p>
 * Extends {@link Sprite} with default green coloring and helpers to teleport,
 * recolor, and offset position. {@link Organism} builds on this class to add
 * hunger, lifespan, and AI.
 * </p>
 */
public abstract class Pixie extends Sprite {
    /**
     * Creates a 10-by-10 green pixie at the given top-left position.
     *
     * @param startX initial x-coordinate in pixels
     * @param startY initial y-coordinate in pixels
     */
    public Pixie(int startX, int startY) {
        super(startX, startY, 10, 10, Color.GREEN);
    }

    /**
     * Creates a green pixie at the given top-left position with a custom size.
     *
     * @param startX initial x-coordinate in pixels
     * @param startY initial y-coordinate in pixels
     * @param sizeX  width in pixels
     * @param sizeY  height in pixels
     */
    public Pixie(int startX, int startY, int sizeX, int sizeY) {
        super(startX, startY, sizeX, sizeY, Color.GREEN);
    }

    /**
     * Instantly sets the pixie's top-left position.
     *
     * @param x new x-coordinate in pixels
     * @param y new y-coordinate in pixels
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Replaces the fill color used when this pixie is drawn.
     *
     * @param newColor the color to apply
     */
    public void changeColor(Color newColor) {
        this.color = newColor;
    }

    /**
     * Offsets the current position by the given pixel deltas.
     *
     * @param x amount to add to the x-coordinate
     * @param y amount to add to the y-coordinate
     */
    public void move(int x, int y) {
        this.x += x;
        this.y += y;
    }
}
