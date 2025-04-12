package game;

import java.awt.*;

public class GameBoard {
    private final int rows = 8;
    private final int cols = 8;
    private final Cell[][] gameBoard;
    private final int cellSize = 50;

    public GameBoard() {
        gameBoard = new Cell[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                gameBoard[row][col] = new Cell(col * 50, row * 50);
            }
        }
    }

    public void drawBoard(Graphics2D g) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * cellSize;
                int y = row * cellSize;
                gameBoard[row][col].draw(g, x, y);

            }
        }
    }

    public Cell getCellAtPixel(int x, int y) {
        int col = x / cellSize;
        int row = y / cellSize;

        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return gameBoard[row][col];
        }
        return null;
    }

    public Cell getCellByRowAndCol(int row, int col) {
        return gameBoard[row][col];
    }

    public int getBoardSize(){
        return this.rows;
    }

    public void setCellisBeingClaimed(int row,int col){
        gameBoard[row][col].setBeingClaimed(true);
    }

    public void setGameBoardElem(int row, int col, Cell cell){
        gameBoard[row][col] = cell;
    }


//    public boolean allCellsClaimed() {
//        for (Cell[] row : grid) {
//            for (Cell cell : row) {
//                if (!cell.isClaimed()) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    public int countOwnedByColor(Color color) {
//        int count = 0;
//        for (Cell[] row : grid) {
//            for (Cell cell : row) {
//                if (cell.getOwnerColor().equals(color)) {
//                    count++;
//                }
//            }
//        }
//        return count;
//    }
}