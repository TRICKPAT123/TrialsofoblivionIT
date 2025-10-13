package utils;

public class Constants {

  public static class EnemyConstants {
    // Enemy Types
    public static final int SLIME = 0;
    public static final int SLIME_DEAD = 0;
    
    // Enemy States
    public static final int IDLE = 0;      // Uses idle_0.png, idle_1.png, etc.
    // public static final int RUNNING = 1;   // Uses run_0.png, run_1.png, etc.
    // public static final int ATTACK = 2;    // Uses attack_0.png, attack_1.png, etc.
    // public static final int HIT = 3;       // Uses hit_0.png, hit_1.png, etc.
    // public static final int DEAD = 4;      // Uses dead_0.png, dead_1.png, etc.

    public static int GetSpriteAmount(int enemy_type, int enemy_state) {
        // All states have 4 frames (0,1,2,3)
        return 4;
    }
}

    public static class Directions {
        public static final int LEFT = 0;
        public static final int UP = 1;
        public static final int RIGHT = 2;
        public static final int DOWN = 3;
    }

    public static class PlayerConstants {
        public static final int IDLE = 0;
        public static final int RUNNING = 1;
        public static final int JUMP = 4;
        public static final int ATTACK_1 = 2;
        public static final int ATTACK_2 = 3;
        public static final int FALLING = 5;
        public static final int HIT = 6;
        public static final int DEAD = 7;

        public static int GetSpriteAmount(int player_action) {
            switch(player_action) {
                case IDLE:
                    return 8;
                case RUNNING:
                    return 8;
                case JUMP:
                    return 4;
                case FALLING:
                    return 4;
                case ATTACK_1:
                    return 4;
                case ATTACK_2:
                    return 4;
                case HIT:
                    return 2;
                case DEAD:
                    return 12;
                default:
                    return 1;
            }
        }
    }
}