package level;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import main.Game;
import utils.LoadSave;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import entities.Slime;

public class LevelManager {
    private Game game;
    private BufferedImage[] levelSprite;
    private int[][] levelOneData;
    private int[][] levelTwoData;
    private int[][] currentLevelData;
    private List<Slime> slimes = new ArrayList<>();
    private boolean showDebugGrid = true;
    private int currentLevel = 1;
    private int currentWave = 1;

private int maxWaves = 5;
private boolean waveInProgress = false;
private int slimesPerWave = 6;
private int slimesKilledThisWave = 0;
private int totalSlimesKilled = 0;



// Wave difficulty scaling
private float damageMultiplier = 1.0f;
private float healthMultiplier = 1.0f;
private float speedMultiplier = 1.0f;
    
    // mark solid tile indices
    private final Set<Integer> solidTiles = new HashSet<>();
    
    // NEW: Collision mask for preventing conflicts between levels
    private boolean[][] collisionMask;

    // animated pixel decorations
    private static class AnimatedDecoration {
        final BufferedImage[] frames;
        final int x, y;            // world pixel position (top-left)
        final int width, height;   // display size
        final int frameDelay;      // ticks per frame
        private int frameTimer = 0;
        private int frameIndex = 0;

        AnimatedDecoration(BufferedImage[] frames, int x, int y, int width, int height, int frameDelay) {
            this.frames = frames;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.frameDelay = Math.max(1, frameDelay);
        }

        void update() {
            frameTimer++;
            if (frameTimer >= frameDelay) {
                frameTimer = 0;
                frameIndex = (frameIndex + 1) % frames.length;
            }
        }

        BufferedImage getFrame() {
            return frames.length > 0 ? frames[frameIndex] : null;
        }
    }
    private final List<AnimatedDecoration> animatedDecorations = new ArrayList<>();

    // Static decorations (non-animated)
    private static class Decoration {
        BufferedImage image;
        int x, y, width, height;
        Decoration(BufferedImage img, int x, int y, int w, int h) {
            this.image = img;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
    }
    private final List<Decoration> decorations = new ArrayList<>();

    public LevelManager(Game game) {
     this.game = game;
    System.out.println("🎮 LevelManager CREATED with game: " + game);
    importOutsideSprites();
    loadLevelOneData();
    loadLevelTwoData();
    setCurrentLevel(1);
    setSolidTiles(2, 7, 8, 9, 10, 11, 12, 33, 166);
    generateCollisionMask();
    loadAnimatedShop();
    loadAllDecorations();
    placeSlimes();
    }
    public void startNextWave() {
    if (currentWave > maxWaves) {
        System.out.println("🎉 All waves completed! Game complete!");
        return;
    }
    
    currentWave++;
    slimesKilledThisWave = 0;
    waveInProgress = true;
    
    // Scale difficulty
    damageMultiplier = 1.0f + (currentWave * 0.3f); // +30% per wave
    healthMultiplier = 1.0f + (currentWave * 0.2f); // +20% per wave
    speedMultiplier = 1.0f + (currentWave * 0.1f);  // +10% per wave
    
    System.out.println("🌊 Starting Wave " + currentWave);
    System.out.println("   Damage: " + damageMultiplier + "x, Health: " + healthMultiplier + "x, Speed: " + speedMultiplier + "x");
    
    // Spawn slimes for this wave
    spawnWaveSlimes();
}
private void spawnWaveSlimes() {
    slimes.clear();
    
    int slimesToSpawn = slimesPerWave + (currentWave * 2); // More slimes each wave
    
    for (int i = 0; i < slimesToSpawn; i++) {
        // Random positions around the level
        int tileX = 10 + (int)(Math.random() * 50);
        int tileY = 12;
        
        Slime slime = addSlimeAtTile(tileX, tileY, 3);
        
        // Apply wave scaling
        slime.setMaxHealth((int)(slime.getMaxHealth() * healthMultiplier));
        slime.setHealth((int)(slime.getMaxHealth() * healthMultiplier));
        slime.setSpeed(slime.getSpeed() * speedMultiplier);
        slime.setDamageMultiplier(damageMultiplier);
    }
    
    System.out.println("🐌 Spawned " + slimesToSpawn + " slimes for wave " + currentWave);
}

private void completeWave() {
    waveInProgress = false;
    dropLoot();
    
}
private void dropLoot() {
    // Random loot drops
    int coins = 10 + (currentWave * 5);
    boolean hasUpgradeMaterial = (Math.random() < 0.3f); // 30% chance
    
    System.out.println("💰 Loot dropped:");
    System.out.println("   Coins: " + coins);
    if (hasUpgradeMaterial) {
        System.out.println("   Upgrade Material: YES");
    }
    
    // Store loot for player to collect
    // You'll need to implement loot collection system
}

    
    // NEW: Generate collision mask that only marks tiles as collidable if they exist and are the same in both levels
    private void generateCollisionMask() {
        if (levelOneData == null || levelTwoData == null) {
            System.out.println("Warning: Level data not loaded properly for collision mask");
            return;
        }
        
        int rows = Math.min(levelOneData.length, levelTwoData.length);
        int cols = Math.min(levelOneData[0].length, levelTwoData[0].length);
        collisionMask = new boolean[rows][cols];
        
        int collidableTiles = 0;
        int nonCollidableTiles = 0;
        
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                boolean level1Solid = isTileSolidInLevel(levelOneData, x, y);
                boolean level2Solid = isTileSolidInLevel(levelTwoData, x, y);
                
                // Only mark as collidable if BOTH levels have the SAME solid tile
                if (level1Solid && level2Solid && levelOneData[y][x] == levelTwoData[y][x]) {
                    collisionMask[y][x] = true;
                    collidableTiles++;
                } else {
                    collisionMask[y][x] = false;
                    nonCollidableTiles++;
                }
            }
        }
      
    }
    
    // NEW: Check if a tile is collidable using the collision mask
    public boolean isTileCollidable(int col, int row) {
        if (collisionMask == null) {
            // Fallback to normal collision if mask not generated
            return isTileSolid(col, row);
        }
        if (row < 0 || row >= collisionMask.length || col < 0 || col >= collisionMask[0].length) {
            return true; // Treat out-of-bounds as solid
        }
        return collisionMask[row][col];
    }
    
    // NEW: Pixel-based collision check using collision mask
    public boolean isCollidableAtPixel(int x, int y) {
        int col = x / Game.TILES_SIZE;
        int row = y / Game.TILES_SIZE;
        return isTileCollidable(col, row);
    }
    
    // UPDATED: Use collision mask for area solid checks
    public boolean isAreaSolid(int x, int y, int width, int height) {
        // Check multiple points for better collision detection using collision mask
        if (isCollidableAtPixel(x, y)) return true;
        if (isCollidableAtPixel(x + width, y)) return true;
        if (isCollidableAtPixel(x, y + height)) return true;
        if (isCollidableAtPixel(x + width, y + height)) return true;
        if (isCollidableAtPixel(x + width/2, y + height/2)) return true;
        return false;
    }

    // UPDATED: Use collision mask for ground detection
    public boolean isGroundBelow(float x, float y, int width) {
        int checkY = (int)(y + 5); // Check slightly below
        int leftX = (int)(x + 5);
        int rightX = (int)(x + width - 5);
        
        return isCollidableAtPixel(leftX, checkY) || isCollidableAtPixel(rightX, checkY);
    }
    
    // Helper method to check if a tile is solid in a specific level data
    private boolean isTileSolidInLevel(int[][] levelData, int col, int row) {
        if (levelData == null) return false;
        if (row < 0 || row >= levelData.length || col < 0 || col >= levelData[0].length) return true;
        int idx = levelData[row][col];
        return solidTiles.contains(idx);
    }
    
public void setCurrentLevel(int level) {
    System.out.println("🔄 ===== SETTING LEVEL FROM " + currentLevel + " TO " + level + " =====");
    
    // Debug before clearing
    System.out.println("BEFORE CLEAR - Slimes count: " + slimes.size());
    for (int i = 0; i < slimes.size(); i++) {
        Slime slime = slimes.get(i);
        System.out.println("  Slime " + i + ": Alive=" + slime.isAlive() + " at " + slime.getX() + "," + slime.getY());
    }
    
    this.currentLevel = level;
    
    // ⚠️ CLEAR ALL LISTS
    slimes.clear();
    animatedDecorations.clear();
    decorations.clear();
    
    System.out.println("AFTER CLEAR - Slimes count: " + slimes.size());
    
    if (level == 1) {
        currentLevelData = levelOneData;
        System.out.println("📁 Loaded Level 1 data");
    } else if (level == 2) {
        currentLevelData = levelTwoData;
        System.out.println("📁 Loaded Level 2 data");
    }
    
    // Place NEW slimes
    placeSlimes();
    loadAnimatedShop();
    loadAllDecorations();
    
    System.out.println("AFTER PLACING - Slimes count: " + slimes.size());
    for (int i = 0; i < slimes.size(); i++) {
        Slime slime = slimes.get(i);
        System.out.println("  New Slime " + i + ": at " + slime.getX() + "," + slime.getY());
    }
    System.out.println("✅ Level switch complete to Level " + currentLevel);
}
// Remove dead slimes that are ready for cleanup
public void cleanupDeadSlimes() {
    // Create a new list with only alive slimes
    List<Slime> aliveSlimes = new ArrayList<>();
    for (Slime slime : slimes) {
        if (slime.isAlive() || slime.getTimeUntilRespawn() > 0) {
            aliveSlimes.add(slime);
        } else {
            System.out.println("Removing permanently dead slime");
        }
    }
    slimes = aliveSlimes;
}
    
    // NEW: Debug method to show collision information
    private void debugCollisionInfo() {
        if (collisionMask == null) return;
        
        int playerX = (int)(game.getPlayer().getX() / Game.TILES_SIZE);
        int playerY = (int)(game.getPlayer().getY() / Game.TILES_SIZE);
        
        System.out.println("Player at tile: (" + playerX + "," + playerY + ")");
        System.out.println("Tile collidable: " + isTileCollidable(playerX, playerY));
        
        // Check surrounding tiles
        for (int y = playerY - 1; y <= playerY + 1; y++) {
            for (int x = playerX - 1; x <= playerX + 1; x++) {
                if (x >= 0 && y >= 0 && y < collisionMask.length && x < collisionMask[0].length) {
                    System.out.println("Tile (" + x + "," + y + "): " + 
                                    (collisionMask[y][x] ? "COLLIDABLE" : "PASSABLE"));
                }
            }
        }
    }
    // Add this method to your LevelManager class
public List<Slime> getAttackableSlimes() {
    List<Slime> attackableSlimes = new ArrayList<>();
    for (Slime slime : slimes) {
        if (slime.isAlive() && !slime.isDying()) {
            attackableSlimes.add(slime);
        }
    }
    return attackableSlimes;
}
// Add to your LevelManager's draw method or Game's render method

 public void onSlimeKilled() {
    slimesKilledThisWave++;
    totalSlimesKilled++;
    
    System.out.println("💀 Slime killed! " + slimesKilledThisWave + "/" + slimes.size() + " this wave");
    
    // GIVE COINS TO PLAYER - ADD THIS
    if (game != null && game.getPlayer() != null) {
        int coinsEarned = 10 + (currentWave * 5);
        boolean hasMaterial = (Math.random() < 0.3f);
        
        game.getPlayer().collectLoot(coinsEarned, hasMaterial);
        System.out.println("💰 Earned: " + coinsEarned + " coins" + (hasMaterial ? " + 1 material" : ""));
    }
    
    // Check if wave is complete
    if (slimesKilledThisWave >= slimes.size() && waveInProgress) {
        completeWave();
    }
}
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public void nextLevel() {
        if (currentLevel < 2) {
            setCurrentLevel(currentLevel + 1);
        } else {
            System.out.println("Already on the highest level!");
        }
    }

    // NEW: Kill a specific slime
    public void killSlime(Slime slime) {
        slime.kill();
    }
    
    // NEW: Kill slime at specific position
    public void killSlimeAt(float x, float y) {
        for (Slime slime : slimes) {
            if (slime.isAlive() && slime.collidesWith(x, y, 1, 1)) {
                slime.kill();
                return;
            }
        }
    }
    
    // NEW: Respawn all dead slimes immediately
    public void respawnAllSlimes() {
        for (Slime slime : slimes) {
            slime.forceRespawn();
        }
        System.out.println("All slimes respawned");
    }
    
    // NEW: Update slime respawns
    public void updateSlimeRespawns() {
        for (Slime slime : slimes) {
            slime.update(); // This handles respawning automatically
        }
    }
    
    // NEW: Get alive slimes only
    public List<Slime> getAliveSlimes() {
        List<Slime> aliveSlimes = new ArrayList<>();
        for (Slime slime : slimes) {
            if (slime.isAlive()) {
                aliveSlimes.add(slime);
            }
        }
        return aliveSlimes;
    }
    
    // NEW: Get dead slimes only
    public List<Slime> getDeadSlimes() {
        List<Slime> deadSlimes = new ArrayList<>();
        for (Slime slime : slimes) {
            if (!slime.isAlive()) {
                deadSlimes.add(slime);
            }
        }
        return deadSlimes;
    }
    
    public List<Slime> getSlimes() {
        return new ArrayList<>(slimes); // Return copy to avoid modification
    }
    
    public Slime getCollidingSlime(float x, float y, int width, int height) {
        for (Slime slime : slimes) {
            if (slime.isAlive() && slime.collidesWith(x, y, width, height)) {
                return slime;
            }
        }
        return null;
    }
    
    public void resolveSlimeCollisions() {
        // Empty - slimes handle their own tile collisions internally
    }
    
    private void placeSlimes() {
        slimes.clear();
        
        if (currentLevel == 1) {
            // Level 1 slimes - gentle introduction
            addSlimeAtTile(60, 12, 10);
            addSlimeAtTile(61, 12, 10);
            addSlimeAtTile(62, 12, 10);
            addSlimeAtTile(63, 12, 10);
            addSlimeAtTile(64, 12, 10);
            addSlimeAtTile(65, 12, 10);
        } else if (currentLevel == 2) {
            // Level 2 slimes - more challenging placement
            addSlimeAtTile(30, 12, 5);
            addSlimeAtTile(40, 12, 8);
            addSlimeAtTile(50, 12, 6);
            addSlimeAtTile(60, 12, 4);
            addSlimeAtTile(70, 12, 7);
            addSlimeAtTile(80, 12, 3);
        }
        
        System.out.println("Placed " + slimes.size() + " slimes on Level " + currentLevel);
    }
    
    private Slime addSlimeAtTile(int tileX, int tileY, int moveRange) {
        float worldX = tileX * Game.TILES_SIZE;
        float worldY = tileY * Game.TILES_SIZE;
        int width = 64;
        int height = 64;
        
        Slime slime = new Slime(worldX, worldY, width, height);
        slime.setMoveRange(moveRange);
        slime.setLevelManager(this);
        
        slimes.add(slime);
        System.out.println("Slime placed at tile (" + tileX + "," + tileY + ") with moveRange: " + moveRange);
        return slime;
    }
    
    private void importOutsideSprites() {
        BufferedImage img = LoadSave.GetSpriteAtlast(LoadSave.LEVEL_ATLAST);
        
        final int TILE_SIZE = (img.getWidth() % 64 == 0 && img.getHeight() % 64 == 0) ? 64
         : (img.getWidth() % 16 == 0 && img.getHeight() % 16 == 0) ? 16
        : 16;

        int tilesX = img.getWidth() / TILE_SIZE;
        int tilesY = img.getHeight() / TILE_SIZE;
        int totalTiles = tilesX * tilesY;

        levelSprite = new BufferedImage[totalTiles];

        int index = 0;
        for (int y = 0; y < tilesY; y++) {
            for (int x = 0; x < tilesX; x++) {
                levelSprite[index++] = img.getSubimage(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        System.out.println("Loaded " + totalTiles + " tile sprites from atlas (" + TILE_SIZE + "px).");
    }

    private void loadAnimatedShop() {
        try {
            BufferedImage shopSheet = ImageIO.read(new File("decorations/shop_anim.png"));
            int frameCount = detectFrameCount(shopSheet);
            BufferedImage[] shopFrames = extractShopFrames(shopSheet, frameCount);

            double scale = 1.7;
            int targetW = (int) (shopFrames[0].getWidth() * scale);
            int targetH = (int) (shopFrames[0].getHeight() * scale);

            BufferedImage[] resizedFrames = new BufferedImage[shopFrames.length];
            for (int i = 0; i < shopFrames.length; i++) {
                resizedFrames[i] = resizeImage(shopFrames[i], targetW, targetH);
            }

            // Position the shop based on level
            int groundRow = Math.min(Game.TILES_IN_HEIGHT - 2, 12);
            int shopCol = (currentLevel == 1) ? 14 : 25; // Different position for level 2
            int shopPx = shopCol * Game.TILES_SIZE - (targetW - Game.TILES_SIZE) / 2;
            int shopPy = groundRow * Game.TILES_SIZE - targetH + Game.TILES_SIZE;

            animatedDecorations.add(new AnimatedDecoration(resizedFrames, shopPx, shopPy, targetW, targetH, 10));

            System.out.println("Animated shop loaded for Level " + currentLevel);
        } catch (IOException e) {
            System.err.println("Failed to load animated shop: " + e.getMessage());
        }
    }

    private int detectFrameCount(BufferedImage sheet) {
        int width = sheet.getWidth();
        int height = sheet.getHeight();
        
        int[] possibleFrameWidths = {32, 48, 64, 96, 128};
        
        for (int frameWidth : possibleFrameWidths) {
            if (width % frameWidth == 0) {
                int detectedFrames = width / frameWidth;
                System.out.println("Detected " + detectedFrames + " frames with width " + frameWidth);
                return detectedFrames;
            }
        }
        
        System.out.println("Could not auto-detect frame count, using default: 4 frames");
        return 6;
    }

    private BufferedImage[] extractShopFrames(BufferedImage sheet, int frameCount) {
        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();
        
        BufferedImage[] frames = new BufferedImage[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        
        return frames;
    }

    private void loadAllDecorations() {
        try {
            // Load lamp decoration
            BufferedImage lamp = ImageIO.read(new File("decorations/lamp.png"));
            int lampScale = 2;
            int lampWidth = lamp.getWidth() * lampScale;
            int lampHeight = lamp.getHeight() * lampScale;
            BufferedImage resizedLamp = resizeImage(lamp, lampWidth, lampHeight);
            
            // Load rock decoration
            BufferedImage rock = ImageIO.read(new File("decorations/rock_1.png"));
            int rockScale = 2;
            int rockWidth = rock.getWidth() * rockScale;
            int rockHeight = rock.getHeight() * rockScale;
            BufferedImage resizedRock = resizeImage(rock, rockWidth, rockHeight);
            
            // Load sign decoration
            BufferedImage sign = ImageIO.read(new File("decorations/sign.png"));
            int signScale = 2;
            int signWidth = sign.getWidth() * signScale;
            int signHeight = sign.getHeight() * signScale;
            BufferedImage resizedSign = resizeImage(sign, signWidth, signHeight);
            
            // Load fence decorations
            BufferedImage fence1 = ImageIO.read(new File("decorations/fence_1.png"));
            BufferedImage fence2 = ImageIO.read(new File("decorations/fence_2.png"));
            
            // Load grass decorations
            BufferedImage grass1 = ImageIO.read(new File("decorations/grass_1.png"));
            BufferedImage grass2 = ImageIO.read(new File("decorations/grass_2.png"));
            BufferedImage grass3 = ImageIO.read(new File("decorations/grass_3.png"));
            
            int scale = 2;
            BufferedImage resizedFence1 = resizeImage(fence1, fence1.getWidth() * scale, fence1.getHeight() * scale);
            BufferedImage resizedFence2 = resizeImage(fence2, fence2.getWidth() * scale, fence2.getHeight() * scale);
            BufferedImage resizedGrass1 = resizeImage(grass1, grass1.getWidth() * scale, grass1.getHeight() * scale);
            BufferedImage resizedGrass2 = resizeImage(grass2, grass2.getWidth() * scale, grass2.getHeight() * scale);
            BufferedImage resizedGrass3 = resizeImage(grass3, grass3.getWidth() * scale, grass3.getHeight() * scale);
            
            int groundRow = Math.min(Game.TILES_IN_HEIGHT - 1, 14);
            
            // Different decoration placements for each level
            if (currentLevel == 1) {
                setupLevelOneDecorations(groundRow, resizedLamp, resizedRock, resizedSign, 
                                      resizedFence1, resizedFence2, resizedGrass1, resizedGrass2, resizedGrass3);
            } else if (currentLevel == 2) {
                setupLevelTwoDecorations(groundRow, resizedLamp, resizedRock, resizedSign, 
                                      resizedFence1, resizedFence2, resizedGrass1, resizedGrass2, resizedGrass3);
            }
            
            System.out.println("Loaded all decorations for Level " + currentLevel + ": " + decorations.size() + " total");
            
        } catch (IOException e) {
            System.err.println("Could not load decoration images: " + e.getMessage());
        }
    }

    private void setupLevelOneDecorations(int groundRow, BufferedImage lamp, BufferedImage rock, BufferedImage sign,
                                       BufferedImage fence1, BufferedImage fence2, 
                                       BufferedImage grass1, BufferedImage grass2, BufferedImage grass3) {
        // Lamp positions for Level 1
        int[][] lampPositions = {
            {22, groundRow}
        };
        
        // Rock positions for Level 1
        int[][] rockPositions = {
            {17, 13},
            {30, 12}
        };
        
        // Fence positions for Level 1
        int[][] fencePositions = {
            {3, groundRow, 2},
            {8, groundRow, 2},
            {5, groundRow, 1},
            {18, groundRow, 2},
        };
        
        // Grass positions for Level 1
        int[][] grassPositions = {
            {3, 13, 1}, {4, 13, 2}, {5, 13, 3}, {6, 13, 1}, {7, 13, 2}, {8, 13, 3}, 
            {9, 13, 1}, {10, 13, 2}, {11, 13, 3}, {12, 13, 1}, {13, 13, 2}, {14, 13, 3},
            {15, 13, 1}, {16, 13, 2}, {17, 13, 3}, {18, 13, 1}, {19, 13, 2}, {21, 13, 3},
            {22, 13, 1}, {23, 13, 2}, {29, 12, 2}, {30, 12, 3},
        };
        
        // Sign position for Level 1
        int signX = 11 * Game.TILES_SIZE;
        int signY = groundRow * Game.TILES_SIZE - sign.getHeight();
        decorations.add(new Decoration(sign, signX, signY, sign.getWidth(), sign.getHeight()));
        
        addDecorations(lampPositions, rockPositions, fencePositions, grassPositions, 
                     lamp, rock, fence1, fence2, grass1, grass2, grass3);
    }

    private void setupLevelTwoDecorations(int groundRow, BufferedImage lamp, BufferedImage rock, BufferedImage sign,
                                       BufferedImage fence1, BufferedImage fence2, 
                                       BufferedImage grass1, BufferedImage grass2, BufferedImage grass3) {
        // Lamp positions for Level 2
        int[][] lampPositions = {
            {15, groundRow},
            {35, groundRow},
            {55, groundRow}
        };
        
        // Rock positions for Level 2
        int[][] rockPositions = {
            {10, 13},
            {25, 13},
            {45, 12},
            {65, 13},
            {75, 12}
        };
        
        // Fence positions for Level 2
        int[][] fencePositions = {
            {5, groundRow, 1},
            {12, groundRow, 2},
            {20, groundRow, 1},
            {28, groundRow, 2},
            {40, groundRow, 1},
            {52, groundRow, 2},
            {60, groundRow, 1},
            {68, groundRow, 2}
        };
        
        // Grass positions for Level 2
        int[][] grassPositions = {
            {2, 13, 1}, {3, 13, 2}, {4, 13, 3}, {6, 13, 1}, {7, 13, 2}, 
            {8, 13, 3}, {9, 13, 1}, {11, 13, 2}, {13, 13, 3}, {14, 13, 1},
            {16, 13, 2}, {17, 13, 3}, {18, 13, 1}, {19, 13, 2}, {21, 13, 3},
            {22, 13, 1}, {23, 13, 2}, {24, 13, 3}, {26, 13, 1}, {27, 13, 2},
            {29, 13, 3}, {30, 13, 1}, {31, 13, 2}, {32, 13, 3}, {33, 13, 1},
            {34, 13, 2}, {36, 13, 3}, {37, 13, 1}, {38, 13, 2}, {39, 13, 3},
            {41, 13, 1}, {42, 13, 2}, {43, 13, 3}, {44, 13, 1}, {46, 13, 2},
            {47, 13, 3}, {48, 13, 1}, {49, 13, 2}, {50, 13, 3}, {51, 13, 1},
            {53, 13, 2}, {54, 13, 3}, {55, 13, 1}, {56, 13, 2}, {57, 13, 3},
            {58, 13, 1}, {59, 13, 2}, {61, 13, 3}, {62, 13, 1}, {63, 13, 2},
            {64, 13, 3}, {66, 13, 1}, {67, 13, 2}, {69, 13, 3}, {70, 13, 1},
            {71, 13, 2}, {72, 13, 3}, {73, 13, 1}, {74, 13, 2}, {76, 13, 3}
        };
        
        // Sign position for Level 2
        int signX = 45 * Game.TILES_SIZE;
        int signY = groundRow * Game.TILES_SIZE - sign.getHeight();
        decorations.add(new Decoration(sign, signX, signY, sign.getWidth(), sign.getHeight()));
        
        addDecorations(lampPositions, rockPositions, fencePositions, grassPositions, 
                     lamp, rock, fence1, fence2, grass1, grass2, grass3);
    }

    private void addDecorations(int[][] lampPositions, int[][] rockPositions, int[][] fencePositions, int[][] grassPositions,
                             BufferedImage lamp, BufferedImage rock, 
                             BufferedImage fence1, BufferedImage fence2,
                             BufferedImage grass1, BufferedImage grass2, BufferedImage grass3) {
        // Add lamps
        for (int[] pos : lampPositions) {
            int col = pos[0];
            int row = pos[1];
            int y = row * Game.TILES_SIZE - lamp.getHeight();
            int x = col * Game.TILES_SIZE;
            decorations.add(new Decoration(lamp, x, y, lamp.getWidth(), lamp.getHeight()));
        }
        
        // Add rocks
        for (int[] pos : rockPositions) {
            int col = pos[0];
            int row = pos[1];
            int y = row * Game.TILES_SIZE - rock.getHeight();
            int x = col * Game.TILES_SIZE;
            decorations.add(new Decoration(rock, x, y, rock.getWidth(), rock.getHeight()));
        }
        
        // Add fences with different types
        for (int[] pos : fencePositions) {
            int col = pos[0];
            int row = pos[1];
            int fenceType = pos[2];
            int y = row * Game.TILES_SIZE;
            int x = col * Game.TILES_SIZE;
            
            BufferedImage fenceImage = (fenceType == 1) ? fence1 : fence2;
            y = y - fenceImage.getHeight();
            decorations.add(new Decoration(fenceImage, x, y, fenceImage.getWidth(), fenceImage.getHeight()));
        }
        
        // Add grass with different types
        for (int[] pos : grassPositions) {
            int col = pos[0];
            int row = pos[1];
            int grassType = pos[2];
            int y = row * Game.TILES_SIZE;
            int x = col * Game.TILES_SIZE;
            
            BufferedImage grassImage;
            switch (grassType) {
                case 1: grassImage = grass1; break;
                case 2: grassImage = grass2; break;
                case 3: grassImage = grass3; break;
                default: grassImage = grass1;
            }
            
            y = y - grassImage.getHeight();
            decorations.add(new Decoration(grassImage, x, y, grassImage.getWidth(), grassImage.getHeight()));
        }
    }

    private void loadLevelOneData() {
        levelOneData = LevelOneMap.createLevelOneCompleteMap();
        System.out.println("Level 1 loaded with " + levelOneData[0].length + " columns");
    }

    private void loadLevelTwoData() {
        levelTwoData = LevelTwoMap.createLevelTwoCompleteMap();
        System.out.println("Level 2 loaded with " + levelTwoData[0].length + " columns");
    }

    // Game Loop Methods
    public void update() {
        // Update animated decorations
        for (AnimatedDecoration anim : animatedDecorations) {
            anim.update();
        }
        
        // Update all slimes - they handle their own respawning
        updateSlimeRespawns();
    }

    public void draw(Graphics g) {
        if (currentLevelData == null || levelSprite == null) return;

        // Draw main level tiles FIRST (background)
        drawLevelTiles(g);
        
        // Draw static decorations SECOND
        for (Decoration d : decorations) {
            if (d.image == null) continue;
            g.drawImage(d.image, d.x, d.y, d.width, d.height, null);
        }
        
        // Draw animated decorations THIRD
        for (AnimatedDecoration anim : animatedDecorations) {
            BufferedImage frame = anim.getFrame();
            if (frame != null) {
                g.drawImage(frame, anim.x, anim.y, anim.width, anim.height, null);
            }
        }
        
        // Draw slimes FOURTH (above decorations but below UI)
        for (Slime slime : slimes) {
            slime.render(g);
        }
        
        // Draw level indicator
        g.setColor(Color.WHITE);
        g.drawString("Level: " + currentLevel, 20, 30);
        
        // Draw collision debug info
        g.drawString("Collision Mask Active", 20, 70);
    }

    private void drawLevelTiles(Graphics g) {
        if (currentLevelData == null || levelSprite == null) {
            System.err.println("Level data or sprites not loaded");
            return;
        }
        
        int rows = currentLevelData.length;
        int cols = currentLevelData[0].length;
        
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                int index = currentLevelData[j][i];
                if (index < 0 || index >= levelSprite.length) {
                    continue;
                }
                
                BufferedImage tile = levelSprite[index];
                if (tile != null) {
                    g.drawImage(tile, i * Game.TILES_SIZE, j * Game.TILES_SIZE, 
                               Game.TILES_SIZE, Game.TILES_SIZE, null);
                }
            }
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        if (original == null) {
            BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = fallback.createGraphics();
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawString("MISSING", 5, height/2);
            g2d.dispose();
            return fallback;
        }
        
        BufferedImage resized = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = resized.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return resized;
    }

    public int[][] getLevelData() {
        return currentLevelData;
    }

    public void setSolidTiles(int... indices) {
        for (int i : indices) solidTiles.add(i);
    }

    // OLD: Keep for backward compatibility, but use collision mask internally
    public boolean isTileSolid(int col, int row) {
        return isTileCollidable(col, row);
    }

    // OLD: Keep for backward compatibility
    public boolean isSolidAtPixel(int x, int y) {
        return isCollidableAtPixel(x, y);
    }
    public Game getGame() {
    return this.game;
}

    
}