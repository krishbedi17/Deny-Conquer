package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Cell {
    Color colorOfCell = Color.WHITE;
    boolean isClaimed = false;
    boolean isBeingClaimed = false;
    private final ReentrantLock lock = new ReentrantLock();

    Point locOnCanvas;
    BufferedImage drawing = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);

    private final Set<Point> pixelSet = new HashSet<>();


    public Cell(int x, int y) {
        locOnCanvas = new Point(x, y);
    }

    public void draw(Graphics g, int x, int y) {
        if (isClaimed) {
            g.setColor(colorOfCell);
            g.fillRect(x, y, 50, 50);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, 50, 50);
            g.drawImage(drawing, x, y, null);
        }
        g.setColor(Color.BLACK);
        g.drawRect(x, y, 50, 50);
    }

    public boolean tryLock() {
        return lock.tryLock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isClaimed() {
        return isClaimed;
    }

    public boolean isBeingClaimed() {
        return isBeingClaimed;
    }

    public void setBeingClaimed(boolean flag) {
        isBeingClaimed = flag;
    }

    public void addDrawnPixel(int x, int y, Color playerColor) {
        if (!isClaimed && !isBeingClaimed) return;

        if (!pixelSet.contains(new Point(x, y))) {
            pixelSet.add(new Point(x, y));
            Graphics2D g2d = drawing.createGraphics();
            g2d.setColor(playerColor); // semi-transparent red
            g2d.fillRect(x, y, 2, 2);
            g2d.dispose();
        }
        System.out.println("Pixel Added: (" + x + ", " + y + ")");
    }

    public boolean checkIfValidFill(Color playerColor) {
        boolean flag = false;
        System.out.println("Checking fill — pixelSet size: " + pixelSet.size());

        if (pixelSet.size() >= 125) {
            colorOfCell = playerColor;
            isClaimed = true;
            System.out.println("Cell Valid");
            flag = true;
        } else {
            Graphics2D g2d = drawing.createGraphics();
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, drawing.getWidth(), drawing.getHeight());
            g2d.dispose();
        }

        pixelSet.clear();
        isBeingClaimed = false;
        return flag;
    }
}
