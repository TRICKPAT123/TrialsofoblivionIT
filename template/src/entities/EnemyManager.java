package entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gamestates.Playing;
import utilz.LoadSave;

import static utilz.Constants.EnemyConstants.*; // expects IDLE, RUNNING, WIDTH, HEIGHT, WIDTH_DEFAULT, HEIGHT_DEFAULT

public class EnemyManager {

    private static final int NUM_STATES = 6; // 0..5 (matches your comment about HIT at index 5)

    private final Playing playing;
    private BufferedImage[][] mushAnim; // [state][frame]
    private BufferedImage[][] batAnim;  // [state][frame]

    private ArrayList<mushroom> mushies = new ArrayList<>();
    private ArrayList<bat> bats = new ArrayList<>();

    public EnemyManager(Playing playing) {
        this.playing = playing;
        loadEnemyImgs();
        addEnemies();
    }

    /* -----------------------
       Lifecycle / game loop
    ------------------------*/
    private void addEnemies() {
        mushies = LoadSave.GetMush();
        System.out.println("Size of mush " + mushies.size());

        bats = LoadSave.GetBat();
        System.out.println("Size of bats " + bats.size());
    }

    public void update(int[][] lvlData) {
        for (mushroom m : mushies) m.update(lvlData);
        for (bat b : bats) b.update(lvlData);
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawMush(g, xLvlOffset);
        drawBat(g, xLvlOffset);
    }

    private void drawMush(Graphics g, int xLvlOffset) {
        for (mushroom m : mushies) {
            BufferedImage img = getFrame(mushAnim, m.getEnemyState(), m.getAniIndex());
            if (img == null) continue; // no frames loaded for this state

            int drawX = (int) (m.getHitbox().x - xLvlOffset);
            int drawY = (int) (m.getHitbox().y + m.getHitbox().height - HEIGHT);
            g.drawImage(img, drawX, drawY, WIDTH, HEIGHT, null);
             m.drawHitbox(g, xLvlOffset);
        }
    }

    private void drawBat(Graphics g, int xLvlOffset) {
        for (bat b : bats) {
            BufferedImage img = getFrame(batAnim, b.getEnemyState(), b.getAniIndex());
            if (img == null) continue; // no frames loaded for this state

            int drawX = (int) (b.getHitbox().x - xLvlOffset);
            int drawY = (int) (b.getHitbox().y + b.getHitbox().height - HEIGHT);
            g.drawImage(img, drawX, drawY, WIDTH, HEIGHT, null);
             b.drawHitbox(g, xLvlOffset);
        }
    }

    /* -----------------------
       Asset loading
    ------------------------*/
    private void loadEnemyImgs() {
        final int fw = WIDTH_DEFAULT;   // e.g., 64
        final int fh = HEIGHT_DEFAULT;  // e.g., 64

        mushAnim = newEmptyAnimTable();
        batAnim  = newEmptyAnimTable();

        // Load separate strips
        // Mushrooms
        BufferedImage mushIdle = utilz.LoadSave.GetSpriteAtlas(LoadSave.MUSHROOM_IDLE);
        BufferedImage mushRun  = utilz.LoadSave.GetSpriteAtlas(LoadSave.MUSHROOM_RUN);
        // Optional strips (uncomment + add constants when you have assets)
        // BufferedImage mushHit  = utilz.LoadSave.GetSpriteAtlas(LoadSave.MUSHROOM_HIT);
        // BufferedImage mushDead = utilz.LoadSave.GetSpriteAtlas(LoadSave.MUSHROOM_DEAD);
        // BufferedImage mushAtk  = utilz.LoadSave.GetSpriteAtlas(LoadSave.MUSHROOM_ATTACK);

        // Bats
        BufferedImage batIdle = utilz.LoadSave.GetSpriteAtlas(LoadSave.BAT_IDLE);
        BufferedImage batRun  = utilz.LoadSave.GetSpriteAtlas(LoadSave.BAT_RUN);

        // Slice strips safely (returns empty arrays if null)
        mushAnim[IDLE]    = sliceStripSafe(mushIdle, fw, fh);
        mushAnim[RUNNING] = sliceStripSafe(mushRun,  fw, fh);
        // If you later enable these states, just assign:
        // mushAnim[HIT]     = sliceStripSafe(mushHit,  fw, fh);
        // mushAnim[DEAD]    = sliceStripSafe(mushDead, fw, fh);
        // mushAnim[ATTACK_1]= sliceStripSafe(mushAtk,  fw, fh);

        batAnim[IDLE]     = sliceStripSafe(batIdle, fw, fh);
        batAnim[RUNNING]  = sliceStripSafe(batRun,  fw, fh); // <-- fixed: was using `run` (mushroom)
    }

    /* -----------------------
       Helpers
    ------------------------*/
    private static BufferedImage[][] newEmptyAnimTable() {
        // Create the table with null rows. We’ll fill only the states we have.
        return new BufferedImage[NUM_STATES][];
    }

    /**
     * Slices a single-row sprite strip into frames of (fw x fh).
     * Returns an empty array (length 0) if the sheet is null or width < fw.
     */
    private static BufferedImage[] sliceStripSafe(BufferedImage sheet, int fw, int fh) {
        if (sheet == null || sheet.getWidth() < fw || sheet.getHeight() < 1) {
            return new BufferedImage[0];
        }
        int frames = Math.max(1, sheet.getWidth() / fw);
        BufferedImage[] row = new BufferedImage[frames];
        int usableH = Math.min(fh, sheet.getHeight());
        for (int i = 0; i < frames; i++) {
            int x = i * fw;
            if (x + fw <= sheet.getWidth()) {
                row[i] = sheet.getSubimage(x, 0, fw, usableH);
            } else {
                // Guard against overflow; leave any trailing frames null
                row[i] = null;
            }
        }
        // Trim out any nulls at the end (optional)
        int last = frames - 1;
        while (last >= 0 && row[last] == null) last--;
        if (last < frames - 1) {
            BufferedImage[] trimmed = new BufferedImage[last + 1];
            System.arraycopy(row, 0, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return row;
    }

    /**
     * Returns a safe frame for (state, index), or null if not available.
     * Wraps the animation index so callers can increment freely.
     */
    private static BufferedImage getFrame(BufferedImage[][] table, int state, int aniIndex) {
        if (table == null) return null;
        if (state < 0 || state >= table.length) return null;
        BufferedImage[] row = table[state];
        if (row == null || row.length == 0) return null;
        int idx = Math.floorMod(aniIndex, row.length);
        return row[idx];
    }
}
