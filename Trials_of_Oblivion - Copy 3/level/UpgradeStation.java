package level;

import main.Game;
import entities.player;
import input.keyBoardInput;

public class UpgradeStation {
    private player player;
    private boolean inUpgradeMenu = false;
    private float x, y;
    private int width, height;
    
    public UpgradeStation(float x, float y, int width, int height, player player) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.player = player; // Initialize the player field
    }
    
    public boolean isPlayerAtStation(entities.player p) {
        return p.getX() < x + width &&
               p.getX() + p.getWidth() > x &&
               p.getY() < y + height &&
               p.getY() + p.getHeight() > y;
    }
    
    public void showUpgradeMenu() {
        inUpgradeMenu = true;
        System.out.println("🛠️ === UPGRADE STATION ===");
        System.out.println("Coins: " + player.getCoins() + " | Materials: " + player.getUpgradeMaterials());
        System.out.println("Attack: Level " + player.getAttackLevel() + " (Press 1)");
        System.out.println("Speed: Level " + player.getSpeedLevel() + " (Press 2)");
        System.out.println("Health: Level " + player.getHealthLevel() + " (Press 3)");
        System.out.println("Press ESC to exit");
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isInUpgradeMenu() { return inUpgradeMenu; }
    public void setInUpgradeMenu(boolean inUpgradeMenu) { this.inUpgradeMenu = inUpgradeMenu; }
}