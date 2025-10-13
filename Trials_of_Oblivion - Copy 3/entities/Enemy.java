package entities;

import utils.Constants;

public abstract class Enemy extends entity {
    private int aniIndex, enemyState, enemyType;
    private int aniTick, aniSpeed = 25;
    
    public Enemy(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height);
        this.enemyType = enemyType;
        this.enemyState = Constants.EnemyConstants.IDLE;
        initHitbox(x, y, width, height);
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            int frameCount = Constants.EnemyConstants.GetSpriteAmount(enemyType, enemyState);
            if (aniIndex >= frameCount) {
                aniIndex = 0;
            }
        }
    }
    
    public void update() {
        updateAnimationTick();
        updateHitbox();
        move();
    }
    
    public void setEnemyState(int state) {
        this.enemyState = state;
        resetAnimation();
    }
    
    public int getAniIndex() {
        return aniIndex;
    }
    
    public int getEnemyState() {
        return enemyState;
    }
    
    public int getEnemyType() {
        return enemyType;
    }
    
    private void resetAnimation() {
        aniIndex = 0;
        aniTick = 0;
    }
    
    protected abstract void move();
    public abstract void render(java.awt.Graphics g);
}