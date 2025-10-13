package utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


import javax.imageio.ImageIO;



public class LoadSave {
    public static final String PLAYER_ATLAST = "main/src/DarkSamurai (64x64).png";
    public static final String LEVEL_ATLAST = "main/src/oak_woods_tileset.png";
    public static final String LEVEL_ONE_DATA =  "main/src/oak_woods_tileset.png";
     public static final String SLIME_IDLE =  "main/src/slime_idle.png";
       public static final String SLIME_MOVING =  "main/src/slime_run.png";
       public static final String SLIME_DEAD =  "main/src/slime_die.png";
     

    public static  BufferedImage GetSpriteAtlast(String fileName){
        BufferedImage img = null;
        InputStream is =LoadSave.class.getResourceAsStream("/"+fileName);
        try {
          img = ImageIO.read(is);

          }catch (IOException e) {
          e.printStackTrace();
        }finally{
            try{
                is.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return img;
    }

    public static int[][] GetLevelData(){
        BufferedImage img = GetSpriteAtlast(LEVEL_ONE_DATA);
        int[][] lvlData = new int[img.getHeight()][img.getWidth()];
       
        for(int j = 0; j < img.getHeight(); j++)
            for(int i = 0; i < img.getWidth(); i++){
                Color color = new Color(img.getRGB(i, j));
                int value = color.getRed();
                if(value >= 260)
                    value = 0;
                lvlData [j][i]= value;
            }
        return lvlData;
    }

}
