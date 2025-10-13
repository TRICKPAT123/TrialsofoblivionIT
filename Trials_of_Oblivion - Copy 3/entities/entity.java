package entities;

import java.awt.geom.Rectangle2D;

public class entity {
    protected float x, y;
    protected int width, height;
    protected Rectangle2D.Float hitbox;
    protected int hbOffsetX, hbOffsetY, hbWidth, hbHeight;

    public entity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setHitbox(int offsetX, int offsetY, int w, int h) {
        this.hbOffsetX = offsetX;
        this.hbOffsetY = offsetY;
        this.hbWidth = w;
        this.hbHeight = h;
        this.hitbox = new Rectangle2D.Float(x + hbOffsetX, y + hbOffsetY, hbWidth, hbHeight);
    }

    public void updateHitbox() {
        if (hitbox == null) {
            setHitbox(0, 0, width, height);
        } else {
            hitbox.x = x + hbOffsetX;
            hitbox.y = y + hbOffsetY;
            hitbox.width = hbWidth;
            hitbox.height = hbHeight;
        }
    }

    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }
    
    protected void initHitbox(float x, float y, int width, int height) {
        hitbox = new Rectangle2D.Float(x, y, width, height);
    }
}