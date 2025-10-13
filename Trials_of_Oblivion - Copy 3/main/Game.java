package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import entities.Enemy;
import entities.Slime;
import entities.player;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import level.LevelManager;
import level.UpgradeStation;
import utils.LoadSave;
import utils.SoundManager;

public class Game implements Runnable {
    
    private GameWindow gameWindow;
    private Gamepanel gamePanel;
    private Thread gameThread;
    private final int FPS = 120;
    private final int UPS = 200;

    private int frames = 0;
    private long lastCheck = System.currentTimeMillis();
    private int updates = 0;

    private player player;
    private LevelManager levelManager;
    private boolean inUpgradeMenu = false;
    private UpgradeStation upgradeStation;

    public final static int TILES_DEFAULT_SIZE = 20;
    public final static float SCALE = 2.0f;
    public final static int TILES_IN_WIDTH = 26;
    public final static int TILES_IN_HEIGHT = 14;
    public final static int TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
    public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

    private int cameraX = 0;
    private int cameraY = 0;
    private int viewWidth = GAME_WIDTH;
    private int viewHeight = GAME_HEIGHT;
    private List<Enemy> enemies = new ArrayList<>();
    private SoundManager soundManager;

    public Game(){
        initClasses();
        gamePanel = new Gamepanel(this);
        gameWindow = new GameWindow(gamePanel);
        gamePanel.requestFocus();
        startGameThread();
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public void initClasses() {
        levelManager = new LevelManager(this);
        soundManager = new SoundManager();

        int spawnTileCol = 6;
        int spawnTileRow = 12;
        int spawnX = spawnTileCol * TILES_SIZE;
        int spawnY = spawnTileRow * TILES_SIZE - TILES_SIZE;
        player = new player((float) spawnX, (float) spawnY, 120, 120);
        
        player.setGame(this);
        player.setLevelManager(levelManager);
        
        // Create upgrade station near spawn with player reference
        upgradeStation = new UpgradeStation(spawnX - 100, spawnY, 80, 80, player);
        
        int[][] data = levelManager.getLevelData();
        if (data != null) player.loadLevelData(data);

        updateCamera();
    }

    public void showUpgradeMenu() {
        upgradeStation.showUpgradeMenu();
        inUpgradeMenu = true;
    }

    public void exitUpgradeMenu() {
        inUpgradeMenu = false;
    }
    
    public boolean isInUpgradeMenu() {
        return inUpgradeMenu;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public Gamepanel getGamePanel() {
        return gamePanel;
    }

    private void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void update(){
        player.update();
        levelManager.update();
        
        // Check if player is at upgrade station
        if (upgradeStation.isPlayerAtStation(player)) {
            player.setAtUpgradeStation(true);
        } else {
            player.setAtUpgradeStation(false);
        }
        
        // Check collisions between player and slimes
        for (Slime slime : levelManager.getAliveSlimes()) {
            player.checkEnemyCollision(slime);
        }
        
        updateCamera();
        updates++;
    }
    
    public UpgradeStation getUpgradeStation() {
        return upgradeStation;
    }
    
    private void updateCamera() {
        int targetX = (int)(player.getX() + player.getWidth() / 2f) - GAME_WIDTH / 2;
        int targetY = (int)(player.getY() + player.getHeight() / 2f) - GAME_HEIGHT / 2;

        int mapPixelW = GAME_WIDTH;
        int mapPixelH = GAME_HEIGHT;
        int[][] ld = levelManager.getLevelData();
        if (ld != null && ld.length > 0) {
            mapPixelH = ld.length * TILES_SIZE;
            mapPixelW = ld[0].length * TILES_SIZE;
        }

        cameraX = Math.max(0, Math.min(targetX, Math.max(0, mapPixelW - GAME_WIDTH)));
        cameraY = Math.max(0, Math.min(targetY, Math.max(0, mapPixelH - GAME_HEIGHT)));
    }

    public void render(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform old = g2.getTransform();
        g2.translate(-cameraX, -cameraY);

        levelManager.draw(g2);
        
        // Draw upgrade station
        drawUpgradeStation(g2);
        
        for (Enemy enemy : enemies) {
            enemy.render(g2);
        }
        
        player.render(g2);

        g2.setTransform(old);
        
        // Draw UI and menu
        drawUI(g);
        
        if (inUpgradeMenu) {
            renderUpgradeMenu(g);
        }
    }
    
    private void drawUI(Graphics g) {
        g.drawString("Coins: " + player.getCoins(), 20, 70);
        g.drawString("Materials: " + player.getUpgradeMaterials(), 20, 90);
        
        if (player.isAtUpgradeStation() && !inUpgradeMenu) {
            g.drawString("Press U to upgrade", GAME_WIDTH/2 - 60, GAME_HEIGHT - 50);
        }
    }
    
    private void renderUpgradeMenu(Graphics g) {
        // Semi-transparent background
        g.setColor(new java.awt.Color(0, 0, 0, 200));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // Menu box
        g.setColor(new java.awt.Color(50, 50, 80));
        g.fillRect(GAME_WIDTH/2 - 200, GAME_HEIGHT/2 - 150, 400, 300);
        g.setColor(java.awt.Color.WHITE);
        g.drawRect(GAME_WIDTH/2 - 200, GAME_HEIGHT/2 - 150, 400, 300);
        
        // Title
        g.setColor(java.awt.Color.YELLOW);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        g.drawString("🛠️ UPGRADE STATION", GAME_WIDTH/2 - 100, GAME_HEIGHT/2 - 100);
        
        // Resources
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));
        g.drawString("Coins: " + player.getCoins(), GAME_WIDTH/2 - 180, GAME_HEIGHT/2 - 60);
        g.drawString("Materials: " + player.getUpgradeMaterials(), GAME_WIDTH/2 - 180, GAME_HEIGHT/2 - 30);
        
        // Upgrade options
        g.setColor(java.awt.Color.CYAN);
        g.drawString("1. Upgrade Attack (Level " + player.getAttackLevel() + ")", GAME_WIDTH/2 - 180, GAME_HEIGHT/2 + 10);
        g.drawString("2. Upgrade Speed (Level " + player.getSpeedLevel() + ")", GAME_WIDTH/2 - 180, GAME_HEIGHT/2 + 40);
        g.drawString("3. Upgrade Health (Level " + player.getHealthLevel() + ")", GAME_WIDTH/2 - 180, GAME_HEIGHT/2 + 70);
        
        // Costs
        g.setColor(java.awt.Color.ORANGE);
        g.drawString("Cost: " + player.getAttackUpgradeCost() + " coins + 1 material", GAME_WIDTH/2 - 180, GAME_HEIGHT/2 + 100);
        g.drawString("Cost: " + player.getSpeedUpgradeCost() + " coins + 1 material", GAME_WIDTH/2 - 180, GAME_HEIGHT/2 + 130);
        g.drawString("Cost: " + player.getHealthUpgradeCost() + " coins + 1 material", GAME_WIDTH/2 - 180, GAME_HEIGHT/2 + 160);
        
        // Exit instruction
        g.setColor(java.awt.Color.RED);
        g.drawString("Press ESC to exit", GAME_WIDTH/2 - 80, GAME_HEIGHT/2 + 200);
    }

    public void drawUpgradeStation(Graphics g) {
        if (upgradeStation != null) {
            // Draw upgrade station on map
            g.setColor(new java.awt.Color(0, 255, 0, 100));
            g.fillRect((int)upgradeStation.getX(), (int)upgradeStation.getY(), 
                      (int)upgradeStation.getWidth(), (int)upgradeStation.getHeight());
            
            // Draw "UPGRADE" text above it
            g.setColor(java.awt.Color.YELLOW);
            g.drawString("UPGRADE", (int)upgradeStation.getX(), (int)upgradeStation.getY() - 10);
        }
    }

    @Override
    public void run() {
        double timePerFrame = 1000000000.0 / FPS;
        double timePerUpdate = 1000000000.0 / UPS;

        long previous = System.nanoTime();

        double deltaU = 0;
        double deltaF = 0;

        int loopFrames = 0;
        int loopUpdates = 0;
        long loopLastCheck = System.currentTimeMillis();

        while(true){
            long currentTime = System.nanoTime();

            deltaU += (currentTime - previous) / timePerUpdate;
            deltaF += (currentTime - previous) / timePerFrame;
            previous = currentTime;

            if (deltaU >= 1) {
                update();
                loopUpdates++;
                deltaU--;
            }

            if (deltaF >= 1) {
                gamePanel.repaint();
                loopFrames++;
                deltaF--;
            }

            if (System.currentTimeMillis() - loopLastCheck >= 1000) {
                loopLastCheck = System.currentTimeMillis();
                this.frames = loopFrames;
                this.updates = loopUpdates;
                System.out.println("FPS: " + loopFrames + " | UPS: " + loopUpdates);
                loopFrames = 0;
                loopUpdates = 0;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void WindowrequestFocus(){
        player.resetDirBooleans();
    }

    public player getPlayer(){
        return player;
    }

    public void setViewSize(int w, int h) {
        if (w > 0) viewWidth = w;
        if (h > 0) viewHeight = h;
        updateCamera();
    }

    public int getCameraX() { return cameraX; }
    public int getCameraY() { return cameraY; }
}