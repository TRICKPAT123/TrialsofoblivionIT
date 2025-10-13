package entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import utils.LoadSave;
import main.Game;
import level.LevelManager;

public class Slime extends Enemy {
    private BufferedImage[] idleFrames;
    private int aniTick, aniIndex, aniSpeed = 21;
    private boolean framesLoaded = false;
    private float speed = 1.0f;
    private boolean movingRight = true;
    private int moveRange;
    private float startX, startY;
    private LevelManager levelManager;
    private boolean canMove = true;
    private int groundCheckOffset = 10;
    // AI and attack system
    private boolean seesPlayer = false;
    private float attackRange = 150f; // How far the slime can see/attack
    private int attackCooldown = 0;
    private final int ATTACK_COOLDOWN_TIME = 90; // 1.5 seconds at 60 UPS
    private boolean isAttacking = false;
    private float attackSpeed = 1.0f; // Faster when attacking
    private float damageMultiplier = 1.0f;

    //health and damage 
    private int health = 30;
    private int maxHealth = 30;
    private boolean isDying = false;
    private int deathAnimationTimer = 0;
    private final int DEATH_ANIMATION_TIME = 60;
    private int damageFlashTimer = 0;
    private  int DAMAGE_FLASH_TIME = 10;

    private BufferedImage[] deathFrames; // Death animation frames
    private int deathAniTick = 0;
    private int deathAniIndex = 0;
    private final int DEATH_ANI_SPEED = 8; // Slower for death animation
        
        // Respawning variables
    private boolean isAlive = true;
    private long deathTime = 0;
    private final long RESPAWN_TIME = 30000; // 30 seconds in milliseconds
    
    public Slime(float x, float y, int width, int height) {
        this(x, y, width, height, 3, null);
    }
    
    public Slime(float x, float y, int width, int height, int moveRange, LevelManager levelManager) {
        super(x, y, width, height, utils.Constants.EnemyConstants.SLIME);
        this.startX = x;
        this.startY = y;
        this.moveRange = Math.min(6, Math.max(1, moveRange));
        this.levelManager = levelManager;
        extractFramesFromImage();
        loadDeathAnimation();
        System.out.println("Slime created at position: " + x + ", " + y + " with moveRange: " + moveRange + " blocks");
    }
    
    private void extractFramesFromImage() {
      
            BufferedImage fullImage = LoadSave.GetSpriteAtlast(LoadSave.SLIME_MOVING);
            
          
            
           
            
            idleFrames = new BufferedImage[6];
            int frameWidth = fullImage.getWidth() / 6;
            int frameHeight = fullImage.getHeight();
         
            
            for (int i = 0; i < 6; i++) {
                int startX = i * frameWidth;
                idleFrames[i] = fullImage.getSubimage(startX, 0, frameWidth, frameHeight);
            }
            
            framesLoaded = true;
         
            
        
    }
    private boolean canSeePlayer(player player) {
    if (player == null || !player.isAlive()) return false;
    
    // Calculate distance to player
    float playerX = player.getX() + player.getWidth() / 2;
    float playerY = player.getY() + player.getHeight() / 2;
    float slimeX = this.x + this.width / 2;
    float slimeY = this.y + this.height / 2;
    
    float distance = (float) Math.sqrt(
        Math.pow(playerX - slimeX, 2) + Math.pow(playerY - slimeY, 2)
    );
    
    // Check if player is within attack range
    return distance <= attackRange;
}
    private void moveTowardsPlayer(player player) {
    if (player == null) return;
    
    float playerX = player.getX();
    float slimeX = this.x;
    
    // Determine direction to move
    if (playerX > slimeX) {
        // Player is to the right
        movingRight = true;
        x += attackSpeed; // Move faster when attacking
    } else if (playerX < slimeX) {
        // Player is to the left
        movingRight = false;
        x -= attackSpeed; // Move faster when attacking
    }
    
    // Optional: Add small random movement when close to player
    if (Math.abs(playerX - slimeX) < 50) {
        // Add some random movement to make it harder to predict
        x += (Math.random() - 0.5) * 2f;
    }
}
private void attackPlayer(player player) {
    if (player == null || attackCooldown > 0) return;
    
    if (collidesWith(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
        int damage = (int)(10 * damageMultiplier); // Scaled damage
        player.takeDamage(damage);
        attackCooldown = ATTACK_COOLDOWN_TIME;
        isAttacking = true;
    }
}
private void updateAnimationTick() {
    if (isDying) {
        // UPDATE DEATH ANIMATION - ADDED
        updateDeathAnimationTick();
    } else {
        // Normal movement animationtttdw
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= 6) {
                aniIndex = 0;
            }
        }
    }
}
@Override
public void update() {
    if (isAlive) {
        if (isDying) {
            updateDeathAnimation();
        } else {
            updateAnimationTick();
            updateAttackCooldown();
            
            // Get player reference (you'll need to pass this to slime)
            player player = levelManager.getGame().getPlayer();
            
            if (player != null && player.isAlive()) {
                // Check if slime can see player
                seesPlayer = canSeePlayer(player);
                
                if (seesPlayer) {
                    // Attack behavior: move towards player and attack
                    moveTowardsPlayer(player);
                    attackPlayer(player);
                } else {
                    // Normal patrol behavior
                    move();
                }
            } else {
                // No player or player dead, just patrol
                move();
            }
            
            updateDamageFlash();
        }
    } else {
        // Check if it's time to respawn
        if (System.currentTimeMillis() - deathTime >= RESPAWN_TIME) {
            respawn();
        }
    }
}


    private void loadDeathAnimation() {
   
        BufferedImage deathImage = LoadSave.GetSpriteAtlast(LoadSave.SLIME_DEAD);
        if (deathImage == null) {  
            createFallbackDeathAnimation();
            return;
        }
     
        // Assuming death animation has 4 frames
        deathFrames = new BufferedImage[4];
        int frameWidth = deathImage.getWidth() / 4;
        int frameHeight = deathImage.getHeight();
        
        for (int i = 0; i < 4; i++) {
            int startX = i * frameWidth;
            deathFrames[i] = deathImage.getSubimage(startX, 0, frameWidth, frameHeight);
        }
    
    
}
private void createFallbackDeathAnimation() {
    deathFrames = new BufferedImage[6];
    for (int i = 0; i < 6; i++) {
        deathFrames[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = deathFrames[i].createGraphics();
        
        // Shrinking and fading effect
        float scale = 1.0f - (i * 0.15f);
        int currentWidth = (int)(width * scale);
        int currentHeight = (int)(height * scale);
        int offsetX = (width - currentWidth) / 2;
        int offsetY = (height - currentHeight) / 2;
        
        // Semi-transparent green with increasing transparency
        java.awt.Color color = new java.awt.Color(0, 200, 100, 200 - (i * 30));
        g2d.setColor(color);
        g2d.fillOval(offsetX, offsetY, currentWidth, currentHeight);
        
        g2d.dispose();
    }
    
}
private void updateDeathAnimationTick() {
    deathAniTick++;
    if (deathAniTick >= DEATH_ANI_SPEED) {
        deathAniTick = 0;
        deathAniIndex++;
        
        // Check if death animation is complete
        if (deathAniIndex >= (deathFrames != null ? deathFrames.length : 6)) {
            deathAniIndex = (deathFrames != null ? deathFrames.length : 6) - 1; // Stay on last frame
            isAlive = false;
            deathTime = System.currentTimeMillis();
            System.out.println("Slime death animation complete!");
        }
    }
}


    
    // COLLISION DETECTION METHODS
    
    private boolean isCollidingWithWall(float newX, float newY) {
        if (levelManager == null) return false;
        
        // Check front collision based on direction
        if (movingRight) {
            // Check right side
            int checkX = (int)(newX + width);
            int checkY = (int)(newY + height / 2); // Middle of slime
            return levelManager.isSolidAtPixel(checkX, checkY);
        } else {
            // Check left side
            int checkX = (int)(newX - 1);
            int checkY = (int)(newY + height / 2); // Middle of slime
            return levelManager.isSolidAtPixel(checkX, checkY);
        }
    }
    
    private boolean isOnGround() {
        if (levelManager == null) return true; // Assume on ground if no level manager
        
        // Check if there's ground directly below the slime
        int checkY = (int)(y + height + 1);
        int leftX = (int)(x + 5); // Slightly in from left edge
        int rightX = (int)(x + width - 5); // Slightly in from right edge
        
        boolean leftGround = levelManager.isSolidAtPixel(leftX, checkY);
        boolean rightGround = levelManager.isSolidAtPixel(rightX, checkY);
        
        return leftGround || rightGround;
    }
    
    private boolean isAtEdge() {
        if (levelManager == null) return false;
        
        if (movingRight) {
            // Check if there's no ground ahead to the right
            int checkX = (int)(x + width + 5);
            int checkY = (int)(y + height + 1);
            return !levelManager.isSolidAtPixel(checkX, checkY);
        } else {
            // Check if there's no ground ahead to the left
            int checkX = (int)(x - 5);
            int checkY = (int)(y + height + 1);
            return !levelManager.isSolidAtPixel(checkX, checkY);
        }
    }
    
    private boolean isBlockedByCeiling(float newX, float newY) {
        if (levelManager == null) return false;
        
        // Check if there's a ceiling above the slime
        int checkY = (int)(newY - 1);
        int leftX = (int)(newX + 5);
        int rightX = (int)(newX + width - 5);
        
        return levelManager.isSolidAtPixel(leftX, checkY) || 
               levelManager.isSolidAtPixel(rightX, checkY);
    }
    // Add this method to check if player's attack hits the slime
public boolean isHitByAttack(float attackX, float attackY, int attackWidth, int attackHeight) {
    if (!isAlive || isDying) return false;
    
    // Check collision with attack hitbox
    boolean hit = attackX < x + width && attackX + attackWidth > x && attackY < y + height && attackY + attackHeight > y;

    return hit;
}
private void updateAttackCooldown() {
    if (attackCooldown > 0) {
        attackCooldown--;
        if (attackCooldown <= 0) {
            isAttacking = false;
        }
    }
}


    
    @Override
    protected void move() {
         if (!canMove || !isAlive || isDying) return; // ADDED: Don't move while dying
        
        // Check if we're on ground - if not, don't move horizontally
        if (!isOnGround()) {
            return;
        }
        
        float newX = x;
        
        // Check if we hit a wall or are at an edge
        boolean hitWall = isCollidingWithWall(x, y);
        boolean atEdge = isAtEdge();
        
        if (hitWall || atEdge) {
            // Turn around if hitting wall or at edge
            movingRight = !movingRight;
            return;
        }
        
        // Normal movement
        if (movingRight) {
            newX = x + speed;
            // Check if moved beyond movement range
            if (newX >= startX + (moveRange * Game.TILES_SIZE)) {
                movingRight = false;
                return;
            }
        } else {
            newX = x - speed;
            // Check if returned to start position
            if (newX <= startX) {
                movingRight = true;
                return;
            }
        }
        
        // Final collision check before moving
        if (!isCollidingWithWall(newX, y) && !isBlockedByCeiling(newX, y)) {
            x = newX;
        } else {
            // Turn around if final check fails
            movingRight = !movingRight;
        }
    }
    

private void updateDeathAnimation() {
    deathAnimationTimer++;
    
    if (deathAnimationTimer >= DEATH_ANIMATION_TIME) {
        isAlive = false;
        deathTime = System.currentTimeMillis();
    }
}
private void updateDamageFlash() {
    if (damageFlashTimer > 0) {
        damageFlashTimer--;
    }
}
public void takeDamage(int damage) {
    if (!isAlive || isDying) return;
    
    health = Math.max(0, health - damage);
    damageFlashTimer = DAMAGE_FLASH_TIME;
    
   
    
    if (health <= 0) {
        // GIVE COINS BEFORE DYING
        if (levelManager != null) {
            levelManager.onSlimeKilled(); // ⚠️ ADD THIS LINE
        }
        startDeathAnimation();
    }
}
    
    @Override
    public void render(Graphics g) {
    if (!isAlive) {
        // Show respawn timer above dead slime position
        g.setColor(java.awt.Color.RED);
        int respawnTime = getTimeUntilRespawn();
        if (respawnTime > 0) {
            g.drawString("Respawning in: " + respawnTime + "s", (int)startX, (int)startY - 20);
        }
        return;
    }
    
    if (framesLoaded && idleFrames != null && idleFrames[aniIndex] != null) {
        if (movingRight) {
            g.drawImage(idleFrames[aniIndex], (int)x, (int)y, width, height, null);
        } else {
            g.drawImage(idleFrames[aniIndex], (int)x + width, (int)y, -width, height, null);
        }
        
        drawHealthBar(g);
        
    } else {
      
        drawHealthBar(g);
    }
}
private void drawHealthBar(Graphics g) {
    if (!isAlive || isDying) return;
    
    int barWidth = 40;
    int barHeight = 6;
    int barX = (int)x + (width - barWidth) / 2;
    int barY = (int)y - 10;
    
    // Background (red)
    g.setColor(java.awt.Color.RED);
    g.fillRect(barX, barY, barWidth, barHeight);
    
    // Health (green)
    g.setColor(java.awt.Color.GREEN);
    int healthWidth = (int)((health / (float)maxHealth) * barWidth);
    g.fillRect(barX, barY, healthWidth, barHeight);
    
    // Border
    g.setColor(java.awt.Color.BLACK);
    g.drawRect(barX, barY, barWidth, barHeight);
}
    
    // COLLISION WITH OTHER ENTITIES (like player)
    public boolean collidesWith(float otherX, float otherY, int otherWidth, int otherHeight) {
        if (!isAlive) return false; // Dead slimes don't collide
        
        return x < otherX + otherWidth &&
               x + width > otherX &&
               y < otherY + otherHeight &&
               y + height > otherY;
    }
    
    public boolean collidesWith(Slime other) {
        if (!isAlive) return false; // Dead slimes don't collide
        return collidesWith(other.x, other.y, other.width, other.height);
    }
    
    // NEW: Method to kill the slime
    public void kill() {
     takeDamage(health); // Deal remaining health as damage
    }
    
    // NEW: Method to respawn the slime
public void respawn() {
    isAlive = true;
    isDying = false;
    health = maxHealth;
    deathAnimationTimer = 0;
    damageFlashTimer = 0;
    x = startX;
    y = startY;
    movingRight = true;
    aniIndex = 0;
    aniTick = 0;
    System.out.println("Slime respawned at position: " + x + ", " + y);
}
// Add this method to your Slime class
public void setHealth(int health) {
    this.health = health;
    // Ensure health doesn't exceed max health
    if (this.health > maxHealth) {
        this.health = maxHealth;
    }
    // Ensure health doesn't go below 0
    if (this.health < 0) {
        this.health = 0;
    }
}

    // Health system method
    
public void setDamageMultiplier(float multiplier) {
    this.damageMultiplier = multiplier;
}
public void setMaxHealth(int maxHealth) {
    this.maxHealth = maxHealth;
}



private void startDeathAnimation() {
    isDying = true;
    deathAnimationTimer = 0;
}
public boolean isDying() {
    return isDying;
}

    // NEW: Check if slime is alive
public boolean isAlive() {
    return isAlive;
 }
    
    // NEW: Get time until respawn (in seconds)
public int getTimeUntilRespawn() {
        if (isAlive) return 0;
        long timePassed = System.currentTimeMillis() - deathTime;
        long timeLeft = RESPAWN_TIME - timePassed;
        return (int) Math.max(0, timeLeft / 1000);
}
    
    // NEW: Force immediate respawn (for level changes, etc.)
    public void forceRespawn() {
        respawn();
    }
    
    public void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
    }
    
    // Getters and setters
    public void setMoveRange(int moveRange) {
        this.moveRange = Math.min(6, Math.max(1, moveRange));
    }
    
    public void setSpeed(float newSpeed) {
        this.speed = newSpeed;
    }
    
    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
    
    public int getCurrentFrame() {
        return aniIndex;
    }
    
    public boolean isFramesLoaded() {
        return framesLoaded;
    }
    
    public int getMoveRange() {
        return moveRange;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public boolean isMovingRight() {
        return movingRight;
    }
    
    public float getStartX() {
        return startX;
    }
    
    public float getStartY() {
        return startY;
    }
    
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }
    public float getX() { 
    return this.x; 
}

public float getY() { 
    return this.y; 
}

public int getWidth() { 
    return this.width; 
}

public int getHeight() { 
    return this.height; 
}

public int getHealth() {
    return health;
}

public int getMaxHealth() {
    return maxHealth;
}

}