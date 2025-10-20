package entities;



import static utilz.Constants.EnemyConstants.*;

import main.Game;

public class bat extends Enemy {
    public bat(float x, float y) {
        super(x, y, WIDTH, HEIGHT,BAT );
         initHitbox(x, y, (int) (64 * Game.SCALE), (int) (19 * Game.SCALE));
    }
    
}
