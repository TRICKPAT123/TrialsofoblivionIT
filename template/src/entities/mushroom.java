package entities;

import static utilz.Constants.EnemyConstants.*;
import main.Game;

public class mushroom extends Enemy {
    public mushroom(float x, float y) {
        super(x, y, WIDTH, HEIGHT, mushroom);
        initHitbox(x, y, (int) (64 * Game.SCALE), (int) (19 * Game.SCALE));
    }
}