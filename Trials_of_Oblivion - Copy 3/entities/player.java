package entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.Color;
import utils.LoadSave;
import main.Game;
import static utils.Constants.PlayerConstants.*;
import static utils.Constants.Directions.*;
import level.LevelManager;

public class player extends entity{
    // Attack system
    private int attackDamage = 15;
    private int attackRange = 100;
    private boolean canAttack = true;
    private int attackCooldown = 0;
    private final int ATTACK_COOLDOWN_TIME = 30; // 0.5 seconds at 60 UPS
    private Game game; // Reference to game for accessing level manager

    private BufferedImage[][] Animation;
    private int AnimationTic, aniIndex, aniSpeed = 30;
    private LevelManager LevelManager;
    private int player_action = IDLE;
    private int playerDir = -1;
    private boolean moving = false, attacking= false;
    private boolean left, up, right, down;
    private float playerSpeed = 1.2f;
    private int [][] levelData;

    private boolean jumping = false;
    private float yVelocity = 0f;
    private float gravity = 0.6f;
    private float jumpStrength = -13f;
    private boolean onGround = true;

    // horizontal air impulse applied when jumping
    private float xVelocity = 0f;
    private final float JUMP_HORZ_BOOST = 3.0f;
    private final float AIR_DRAG = 0.92f;
    private final float GROUND_FRICTION = 0.6f;

    // hitbox config for this player
    private final int HB_OFFSET_X = 32;
    private final int HB_OFFSET_Y = 32;
    private final int HB_W = 64;
    private final int HB_H = 64;
    private boolean facingRight = true;

    // Health system
    private int health = 100;
    private int maxHealth = 100;
    private float spawnX, spawnY;
    private boolean isAlive = true;
    private int invincibilityTimer = 0;
    private final int INVINCIBILITY_TIME = 60;

    // Add to Player class
    private int coins = 0;
    private int upgradeMaterials = 0;
    private int attackLevel = 1;
    private int speedLevel = 1;
    private int healthLevel = 1;

    // Upgrade costs
    private final int[] ATTACK_UPGRADE_COST = {0, 50, 100, 200, 400};
    private final int[] SPEED_UPGRADE_COST = {0, 30, 60, 120, 240};
    private final int[] HEALTH_UPGRADE_COST = {0, 40, 80, 160, 320};

    public player(float x, float y, int width, int height) {
        super(x, y, width, height);
        this.spawnX = x;
        this.spawnY = y;
        loadAnimation();
        setHitbox(HB_OFFSET_X, HB_OFFSET_Y, HB_W, HB_H);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
public void update(){
    updatePos();
    updateHitbox();
    updateAnimationRun();
    setAnimation();
    updateInvincibility();
    updateAttackCooldown();
    
    
 
      if (attacking && canAttack && attackCooldown <= 0) {
        attack();
      }
}

public void attack() {
  
    
    attacking = true;
    attackCooldown = ATTACK_COOLDOWN_TIME;
    
    float attackX, attackY;
    int attackWidth, attackHeight;
    
    // TEMPORARY: LARGE attack range for testing
    attackWidth = 10;  // Increased to 100 pixels
    attackHeight = 10;  // Increased height
    
    if (facingRight) {
        attackX = hitbox.x + hitbox.width;
        attackY = hitbox.y + (hitbox.height / 4);
    } else {
        attackX = hitbox.x - attackWidth;
        attackY = hitbox.y + (hitbox.height / 4);
    }
    
    checkAttackCollision(attackX, attackY, attackWidth, attackHeight);
}

public void collectLoot(int coins, boolean hasMaterial) {
    this.coins += coins;
    if (hasMaterial) {
        this.upgradeMaterials++;
    }
  
}

public boolean upgradeAttack() {
    if (attackLevel >= 5) {
     
        return false;
    }
    
    int cost = ATTACK_UPGRADE_COST[attackLevel];
    if (coins >= cost && upgradeMaterials >= 1) {
        coins -= cost;
        upgradeMaterials--;
        attackLevel++;
        attackDamage += 5; // +5 damage per level
      
        return true;
    } else {
        return false;
    }
}
public boolean upgradeSpeed() {
    if (speedLevel >= 5) {
       
        return false;
    }
    
    int cost = SPEED_UPGRADE_COST[speedLevel];
    if (coins >= cost && upgradeMaterials >= 1) {
        coins -= cost;
        upgradeMaterials--;
        speedLevel++;
        playerSpeed += 0.2f; // +0.2 speed per level
      
        return true;
    } else {
       
        return false;
    }
}

public boolean upgradeHealth() {
    if (healthLevel >= 5) {
       
        return false;
    }
    
    int cost = HEALTH_UPGRADE_COST[healthLevel];
    if (coins >= cost && upgradeMaterials >= 1) {
        coins -= cost;
        upgradeMaterials--;
        healthLevel++;
        maxHealth += 20; // +20 health per level
        health = maxHealth; // Heal to full
      
        return true;
    } else {
        
        return false;
    }
}
private void checkAttackCollision(float attackX, float attackY, int attackWidth, int attackHeight) {
   
    
    java.util.List<Slime> slimes = LevelManager.getSlimes();
    
    boolean hitAny = false;
    for (int i = 0; i < slimes.size(); i++) {
        Slime slime = slimes.get(i);
        if (slime == null) continue;
        
        if (!slime.isAlive() || slime.isDying()) continue;
        
  
        
        // Manual collision check
        boolean manualCheck = attackX < slime.getX() + slime.getWidth() &&  attackX + attackWidth > slime.getX() &&   attackY < slime.getY() + slime.getHeight() && attackY + attackHeight > slime.getY();
        
        boolean slimeCheck = slime.isHitByAttack(attackX, attackY, attackWidth, attackHeight);
       
        
        if (manualCheck || slimeCheck) {
            slime.takeDamage(attackDamage);
            hitAny = true;
        } 
    }
    
  
}
    public void checkEnemyCollision(Slime slime) {
        if (!isAlive || isInvincible()) return;
        
        // Direct collision check using entity fields
        if (this.x < slime.x + slime.width && 
            this.x + this.width > slime.x &&
            this.y < slime.y + slime.height && 
            this.y + this.height > slime.y) {
            
            takeDamage(1); // 10 damage per slime hit
            // Add knockback
            float knockbackDirection = (slime.x < this.x) ? 1 : -1;
            xVelocity = knockbackDirection * 5f;
            yVelocity = -5f; // Small upward knockback
        }
    }



private void updateAttackCooldown() {
    if (attackCooldown > 0) {
        attackCooldown--;
        if (attackCooldown <= 0) {
            canAttack = true;
            attackCooldown = 0;
        }
    }
}

    public void render(Graphics g){
        if (Animation == null) return;
        if (player_action < 0 || player_action >= Animation.length) return;
        int frames = GetSpriteAmount(player_action);
        if (frames <= 0) return;
        int idx = aniIndex % frames;
        BufferedImage frame = Animation[player_action][idx];
        if (frame != null) {
            // Simple flashing effect for invincibility
            if (isInvincible() && (invincibilityTimer / 10) % 2 == 0) {
                // Skip drawing every other frame for flashing effect
            } else {
                g.drawImage(frame, (int)x, (int)y, 120, 120, null);
            }
        }
        
        drawHealthBar(g);
        
      
    }

    private void drawHealthBar(Graphics g) {
        int barWidth = 80;
        int barHeight = 8;
        int barX = (int)x + 20;
        int barY = (int)y - 15;
        
        // Background (red)
        g.setColor(Color.RED);
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // Health (green)
        g.setColor(Color.GREEN);
        int healthWidth = (int)((health / (float)maxHealth) * barWidth);
        g.fillRect(barX, barY, healthWidth, barHeight);
        
        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    // Optional: Debug method to visualize attack range


    // Health system methods
    public void takeDamage(int damage) {
        if (isInvincible() || !isAlive) return;
        
        health = Math.max(0, health - damage);
        invincibilityTimer = INVINCIBILITY_TIME;
        
        if (health <= 0) {
            die();
        }
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void die() {
        isAlive = false;
        health = 0;
        respawn();
    }

    private void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.health = maxHealth;
        this.isAlive = true;
        this.invincibilityTimer = INVINCIBILITY_TIME * 2;
        this.yVelocity = 0;
        this.onGround = true;
        this.jumping = false;
        resetDirBooleans();
    }

    private void updateInvincibility() {
        if (invincibilityTimer > 0) {
            invincibilityTimer--;
        }
    }
    // Add this field to Player class
private boolean atUpgradeStation = false;

// Add these methods to Player class
public void setAtUpgradeStation(boolean atUpgradeStation) {
    this.atUpgradeStation = atUpgradeStation;
}



    // Fixed collision check with enemies


    private void updateAnimationRun() {
        AnimationTic++;
        if(AnimationTic >= aniSpeed){
            AnimationTic = 0;
            aniIndex++;
            if(aniIndex >= GetSpriteAmount(player_action)){
                aniIndex = 0;
                attacking = false;
            }
        }
    }

    private void setAnimation() {
        int startAni = player_action;

        if (jumping)
            player_action = JUMP;
        else if (attacking)
            player_action = ATTACK_1;
        else if (moving)
            player_action = RUNNING;
        else
            player_action = IDLE;
        
        if (startAni != player_action) {
            aniIndex = 0;
            resetAnimationTic();
        }
    }

    public void resetAnimationTic(){
        AnimationTic = 0;
        aniIndex = 0;
    }

    private boolean isSolidTileIndex(int idx) {
        return idx == 11 || idx == 12 || idx == 62 || idx == 5 || idx == 67 || idx == 0 || idx == 2 || idx == 7 || idx == 8 || idx == 9 || idx == 10;
    }

    private void updatePos() {
        if (!isAlive) return;
    
    moving = false;
    float xSpeed = 0;

    if (left && !right) {
        xSpeed = -playerSpeed;
        facingRight = false; // ⚠️ ADD THIS
    }
    if (right && !left) {
        xSpeed = playerSpeed;
        facingRight = true; // ⚠️ ADD THIS
    }

        float totalX = xSpeed + xVelocity;

        if (totalX != 0) {
            x += totalX;
            moving = true;

            if (levelData != null) {
                int hbX = (int)x + HB_OFFSET_X;
                int hbY = (int)y + HB_OFFSET_Y;
                int hbW = HB_W;
                int hbH = HB_H;

                int leftCol = hbX / Game.TILES_SIZE;
                int rightCol = (hbX + hbW - 1) / Game.TILES_SIZE;
                int topRow = hbY / Game.TILES_SIZE;
                int bottomRow = (hbY + hbH - 1) / Game.TILES_SIZE;

                boolean collided = false;
                for (int r = topRow; r <= bottomRow; r++) {
                    for (int c = leftCol; c <= rightCol; c++) {
                        if (r < 0 || r >= levelData.length || c < 0 || c >= levelData[0].length) continue;
                        if (isSolidTileIndex(levelData[r][c])) {
                            collided = true;
                            break;
                        }
                    }
                    if (collided) break;
                }
                if (collided) {
                    x -= totalX;
                    xVelocity = 0f;
                }
            }
        }

        if (Math.abs(xVelocity) > 0.01f) {
            if (onGround) xVelocity *= GROUND_FRICTION;
            else xVelocity *= AIR_DRAG;
            if (Math.abs(xVelocity) < 0.02f) xVelocity = 0f;
        } else {
            xVelocity = 0f;
        }
        
        if (!onGround) {
            yVelocity += gravity;
            y += yVelocity;
        } else {
            yVelocity = 0;
        }

        if (levelData != null) {
            int hbX = (int)x + HB_OFFSET_X;
            int hbY = (int)y + HB_OFFSET_Y;
            int hbW = HB_W;
            int hbH = HB_H;

            int leftCol = hbX / Game.TILES_SIZE;
            int rightCol = (hbX + hbW - 1) / Game.TILES_SIZE;
            int topRow = hbY / Game.TILES_SIZE;
            int bottomRow = (hbY + hbH - 1) / Game.TILES_SIZE;

            int checkRow = (hbY + hbH) / Game.TILES_SIZE;
            boolean landed = false;
            if (checkRow >= 0 && checkRow < levelData.length) {
                for (int c = leftCol; c <= rightCol; c++) {
                    if (c < 0 || c >= levelData[0].length) continue;
                    if (isSolidTileIndex(levelData[checkRow][c])) {
                        int tileTop = checkRow * Game.TILES_SIZE;
                        y = tileTop - HB_OFFSET_Y - HB_H;
                        yVelocity = 0;
                        onGround = true;
                        jumping = false;
                        landed = true;
                        break;
                    }
                }
            }
            if (!landed) {
                if (yVelocity < 0) {
                    int headRow = hbY / Game.TILES_SIZE;
                    boolean hitHead = false;
                    if (headRow >= 0 && headRow < levelData.length) {
                        for (int c = leftCol; c <= rightCol; c++) {
                            if (c < 0 || c >= levelData[0].length) continue;
                            if (isSolidTileIndex(levelData[headRow][c])) {
                                int tileBottom = (headRow + 1) * Game.TILES_SIZE;
                                y = tileBottom - HB_OFFSET_Y;
                                yVelocity = 0;
                                hitHead = true;
                                break;
                            }
                        }
                    }
                    if (!hitHead) {
                        onGround = false;
                    }
                } else {
                    onGround = false;
                }
            }
        } else {
            float groundY = (10 * Game.TILES_SIZE);
            if (y + height >= groundY) {
                y = groundY - height;
                yVelocity = 0;
                onGround = true;
                jumping = false;
            }
        }

        if (y > Game.GAME_HEIGHT) {
            // Falling off the map counts as death
            takeDamage(100);
        }
    }


    public void loadAnimation(){
        BufferedImage img = LoadSave.GetSpriteAtlast(LoadSave.PLAYER_ATLAST);
      

        Animation = new BufferedImage[8][14];
        for(int j = 0; j < Animation.length; j++) 
            for(int i = 0; i < Animation[j].length; i++) 
                Animation[j][i] = img.getSubimage(i*64, j*64, 64, 64);
    }

    public void setJumping(boolean jumping) {
        if (jumping && onGround && isAlive) {
            this.jumping = true;
            this.onGround = false;
            this.yVelocity = jumpStrength;
            if (left && !right) xVelocity = -JUMP_HORZ_BOOST;
            else if (right && !left) xVelocity = JUMP_HORZ_BOOST;
            player_action = JUMP;
            resetAnimationTic();
        }
    }
    
    

    public void loadLevelData(int[][] levelData){
        this.levelData = levelData;
    }

    public void resetDirBooleans(){
        left = false;
        up = false;
        right = false;
        down = false;
    }

    public void setAttacking(boolean attacking){
        this.attacking = attacking;
    }
    public boolean isAtUpgradeStation() {
    return atUpgradeStation;
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setSpawnPoint(float x, float y) {
        this.spawnX = x;
        this.spawnY = y;
    }

    public boolean isLeft(){ return left; }
    public boolean isUp(){ return up; }
    public boolean isRight(){ return right; }
    public boolean isDown(){ return down; }

    public void setLeft(boolean left){ this.left = left; }
    public void setUp(boolean up){ this.up = up; }
    public void setRight(boolean right){ this.right = right; }
    public void setDown(boolean down){ this.down = down; }

    public float getX() { return this.x; }
    public float getY() { return this.y; }
    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }
    // Add these methods to Player class
    public int getAttackUpgradeCost() {
        return attackLevel < 5 ? ATTACK_UPGRADE_COST[attackLevel] : 0;
    }

    public int getSpeedUpgradeCost() {
        return speedLevel < 5 ? SPEED_UPGRADE_COST[speedLevel] : 0;
    }

    public int getHealthUpgradeCost() {
        return healthLevel < 5 ? HEALTH_UPGRADE_COST[healthLevel] : 0;
    }
     public void setLevelManager(LevelManager levelManager) {
    this.LevelManager = levelManager;
 
}
    
    // Attack system getters
    public boolean canAttack() { return canAttack; }
    public int getAttackDamage() { return attackDamage; }
    public int getAttackRange() { return attackRange; }
    public int getCoins() { return coins; }
    public int getUpgradeMaterials() { return upgradeMaterials; }
    public int getAttackLevel() { return attackLevel; }
    public int getSpeedLevel() { return speedLevel; }
    public int getHealthLevel() { return healthLevel; }
}