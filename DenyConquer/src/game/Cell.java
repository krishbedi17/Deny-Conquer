package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class Cell {
    Color cellColor = Color.WHITE;
    boolean isClaimed = false;
    boolean isBeingClaimed = false;
    Point startPoint;
    BufferedImage drawing = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);

    private final Set<Point> pixelSet = new HashSet<>();

    public Cell(int x, int y) {
        startPoint = new Point(x, y);
    }


    public void draw(Graphics g, int x, int y) {
        if (isClaimed) {
            g.setColor(cellColor);
            g.fillRect(x, y, 50, 50);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, 50, 50);
            g.drawImage(drawing, x, y, null);
        }

        if (isBeingClaimed) {
            g.setColor(Color.RED);
            g.drawRect(x, y, 50, 50);
            g.drawRect(x+1, y+1, 48, 48);
        } else {
            g.setColor(Color.BLACK);
            g.drawRect(x, y, 50, 50);
        }
    }

    public boolean isClaimed() {
        return isClaimed;
    }

    public void setClaimed(boolean claimed, Color claimColor) {
        this.isClaimed = claimed;
        if (claimed) {
            this.cellColor = claimColor;
        }
    }

    public boolean isBeingClaimed() {
        return isBeingClaimed;
    }

    public void setBeingClaimed(boolean flag) {
        isBeingClaimed = flag;
    }

    public void addDrawnPixel(int x, int y, Color playerColor) {
        if (isClaimed) return;

        pixelSet.add(new Point(x, y));
        Graphics2D g2d = drawing.createGraphics();
        g2d.setColor(playerColor);
        g2d.fillRect(x, y, 2, 2);
        g2d.dispose();
    }

    public boolean checkIfValidFill(Color playerColor) {
        System.out.println("Checking fill — pixelSet size: " + pixelSet.size());

        if (pixelSet.size() >= 125) {
            this.cellColor = playerColor;
            isClaimed = true;
            System.out.println("Cell Valid");
            return true;
        } else {
            clearDrawing();
            return false;
        }
    }

    public void clearDrawing() {
        Graphics2D g2d = drawing.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, drawing.getWidth(), drawing.getHeight());
        g2d.dispose();

        pixelSet.clear();
        isBeingClaimed = false;
    }

    public void fillCell(int x,int y,Color color){
        Graphics2D g = drawing.createGraphics();
        g.setColor(color);
        g.fillRect(x, y, 50, 50);
    }


    public Color getColorOfCell(){
        return this.cellColor;
    }
}