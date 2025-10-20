package utilz;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import entities.bat;
import entities.mushroom;
import main.Game;


import static utilz.Constants.EnemyConstants.BAT;
import static utilz.Constants.EnemyConstants.mushroom;

public class LoadSave {

	// public static final String Mushroom_sprite= "res/mush.png";

	// add below your existing constants
// public static final String MUSHROOM_ATTACK = "res/Enemy3-Movement-In-Animation/Mushroom-Attack.png";
public static final String MUSHROOM_IDLE   = "res/Enemy3-Movement-In-Animation/Enemy3-Idle.png";
public static final String MUSHROOM_RUN    = "res/Enemy3-Movement-In-Animation/Enemy3-Fly.png";
// public static final String MUSHROOM_HIT    = "res/Enemy3-Movement-In-Animation/Mushroom-Hit.png";
// public static final String MUSHROOM_DEAD   = "res/Enemy3-Movement-In-Animation/Mushroom-Die.png";
//for bats
public static final String BAT_IDLE   = "res/Bat with VFX/Bat-IdleFly.png";

public static final String BAT_RUN   = "res/Bat with VFX/Bat-Run.png";

    // public static final String SKELETON_ATTACK = "res/skeleton_attack.png";
    // public static final String SKELETON_RUNNING = "res/skeleton_running.png";
	
	// public static final String SKELETON_ATLAS = "res/Mushroom with VFX/player_death.wav";
	public static final String PLAYER_ATLAS = "res/player_sprites.png";
	public static final String LEVEL_ATLAS = "res/outside_sprites.png";
	public static final String LEVEL_ONE_DATA = "res/level_one_data_long.png";
	public static final String MENU_BUTTONS = "res/button_atlas.png";
	public static final String MENU_BACKGROUND = "res/menu_background.png";
	public static final String PAUSE_BACKGROUND = "res/pause_menu.png";
	public static final String SOUND_BUTTONS = "res/sound_button.png";
	public static final String URM_BUTTONS = "res/urm_buttons.png";
	public static final String VOLUME_BUTTONS = "res/volume_buttons.png";
	// public static final String MENU_BACKGROUND_IMG = "res/background_menu.png";
	public static final String PLAYING_BG_IMG = "res/playing_bg_img.png";
	public static final String BIG_CLOUDS = "res/big_clouds.png";
	public static final String SMALL_CLOUDS = "res/small_clouds.png";

	public static BufferedImage GetSpriteAtlas(String fileName) {
		BufferedImage img = null;
		InputStream is = LoadSave.class.getResourceAsStream("/" + fileName);
		try {
			img = ImageIO.read(is);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return img;
	}

	public static ArrayList<mushroom> GetMush(){
	BufferedImage img = GetSpriteAtlas(LEVEL_ONE_DATA);
	ArrayList<mushroom> list = new ArrayList<>();

	for (int j = 0; j < img.getHeight(); j++)
		for (int i = 0; i < img.getWidth(); i++) {
			Color color = new Color(img.getRGB(i, j));
			int value = color.getGreen();
			if (value == mushroom) {
				list.add(new mushroom(i *  Game.TILES_SIZE, j *  Game.TILES_SIZE));
			}
		}
		return list;
	}
	public static ArrayList<bat> GetBat(){
	BufferedImage img = GetSpriteAtlas(LEVEL_ONE_DATA);
	ArrayList<bat> listBat = new ArrayList<>();

	for (int j = 0; j < img.getHeight(); j++)
		for (int i = 0; i < img.getWidth(); i++) {
			Color color = new Color(img.getRGB(i, j));
			int value = color.getGreen();
			if (value == BAT) {
				listBat.add(new bat(i *  Game.TILES_SIZE, j *  Game.TILES_SIZE));
			}
		}
		return listBat;
	}

	public static int[][] GetLevelData() {
		BufferedImage img = GetSpriteAtlas(LEVEL_ONE_DATA);
		int[][] lvlData = new int[img.getHeight()][img.getWidth()];

		for (int j = 0; j < img.getHeight(); j++)
			for (int i = 0; i < img.getWidth(); i++) {
				Color color = new Color(img.getRGB(i, j));
				int value = color.getRed();
				if (value >= 48)
					value = 0;
				lvlData[j][i] = value;
			}
		return lvlData;

	}
}