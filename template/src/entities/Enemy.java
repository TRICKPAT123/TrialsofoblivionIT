package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.*;

import main.Game;
import static utilz.Constants.Directions.*;

public abstract class Enemy extends Entity {
    public int aniIndex, enemyState, enemyType;
    private int aniTick, aniSpeed = 15;
    private boolean firstUpdate = true;
    private boolean inAir;
    private float fallSpeed;
    private float gravity = 0.04f * Game.SCALE;
    private float walkSpeed = 0.35f * Game.SCALE;
    private int walkDir = LEFT;
    
    // Add these variables for state management
    private int idleTime = 0;
    private int maxIdleTime = 120; // 2 seconds at 60 FPS
    private boolean shouldMove = true;

    public Enemy(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height);
        this.enemyType = enemyType;
        this.enemyState = IDLE; // Start with IDLE instead of RUNNING
        initHitbox(x, y, width, height);
        
    }

    public void update(int[][] lvlData) {
        updateMove(lvlData);
        updateAnimationTick();
    }

    public void updateMove(int[][] lvlData) {
        if (firstUpdate) {
            if (!IsEntityOnFloor(hitbox, lvlData))
                inAir = true;
            firstUpdate = false;
        }

        if (inAir) {
            if (CanMoveHere(hitbox.x, hitbox.y + fallSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += fallSpeed;
                fallSpeed += gravity;
            } else {
                inAir = false;
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, fallSpeed);
                fallSpeed = 0;
            }
        } else {
            switch (enemyState) {
                case IDLE:
                    idleTime++;
                    // After idle time, start running
                    if (idleTime >= maxIdleTime) {
                        enemyState = RUNNING;
                        idleTime = 0;
                        shouldMove = true;
                    }
                    break;
                    
                case RUNNING:
                    if (shouldMove) {
                        float xSpeed = (walkDir == LEFT) ? -walkSpeed : walkSpeed;

                        // Check if we can move in the desired direction
                        boolean canMove = CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData);
                        boolean floorAhead = IsFloor(hitbox, xSpeed, lvlData);

                        if (canMove && floorAhead) {
                            // MOVE THE ENEMY
                            hitbox.x += xSpeed;
                            x = hitbox.x;
                        } else {
                            // Cannot move, switch to idle
                            changeWalkDir();
                            enemyState = IDLE;
                            shouldMove = false;
                        }
                    }
                    break;
            }

            // Check if we fell off platform
            if (!IsEntityOnFloor(hitbox, lvlData)) {
                inAir = true;
                fallSpeed = 0;
            }
        }
    }

    private void changeWalkDir() {
        if (walkDir == LEFT)
            walkDir = RIGHT;
        else 
            walkDir = LEFT;
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetSpriteAmount(enemyType, enemyState)) {
                aniIndex = 0;
                // Optional: You can add logic here to switch states after animation completes
            }
        }
    }

    public int getAniIndex() {
        return aniIndex;
    }

    public int getEnemyState() {
        return enemyState;
    }
    
    // Add method to manually set state if needed
    public void setEnemyState(int state) {
        this.enemyState = state;
        if (state == IDLE) {
            idleTime = 0;
        }
    }
}