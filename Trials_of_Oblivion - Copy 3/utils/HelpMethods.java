package utils;

import main.Game;

public class HelpMethods {
    // returns true if rectangle (x,y,width,height) does not overlap any solid tile in levelData
    public static boolean CanMoveHere(float x, float y, int width, int height, int[][] levelData) {
        if (levelData == null) return true;
        int tileSize = Game.TILES_SIZE;

        int leftTile = (int) (x) / tileSize;
        int rightTile = (int) (x + width - 1) / tileSize;
        int topTile = (int) (y) / tileSize;
        int bottomTile = (int) (y + height - 1) / tileSize;

        // out of bounds -> treat as blocked (or change to false if you want wrap)
        if (topTile < 0 || leftTile < 0 || bottomTile >= levelData.length || rightTile >= levelData[0].length)
            return false;

        for (int r = topTile; r <= bottomTile; r++) {
            for (int c = leftTile; c <= rightTile; c++) {
                // assume 0 = empty, non-zero = solid
                if (levelData[r][c] != 0) return false;
            }
        }
        return true;
    }
}
