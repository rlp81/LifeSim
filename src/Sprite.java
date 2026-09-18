import java.awt.*;

/**
 * Axis-aligned colored rectangle used as the visual base for all world entities.
 * <p>
 * Stores screen position, size, and fill color, and can paint itself onto a
 * {@link Graphics} context. {@link Pixie} and its subclasses extend this class
 * to add movement and simulation behavior.
 * </p>
 */
public class Sprite {
    /** Pixel coordinates of the rectangle's top-left corner ({@code x} horizontal, {@code y} vertical). */
    protected int x, y;
    /** Pixel dimensions of the rectangle ({@code width} horizontal, {@code height} vertical). */
    protected int width, height;
    /** Fill color used when {@link #draw(Graphics)} is called. */
    protected Color color;

    /**
     * Creates a sprite at the given top-left position with the specified size and color.
     *
     * @param startX initial x-coordinate in pixels
     * @param startY initial y-coordinate in pixels
     * @param w      width in pixels
     * @param h      height in pixels
     * @param c      fill color
     */
    public Sprite(int startX, int startY, int w, int h, Color c) {
        x = startX;
        y = startY;
        width = w;
        height = h;
        color = c;
    }

    /**
     * Fills this sprite's rectangle on the supplied graphics context.
     *
     * @param g the AWT graphics context to draw into
     */
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }
}
