package main;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import input.keyBoardInput;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import input.MouseInputs;
import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

// import utils.Constants;
import static utils.Constants.PlayerConstants.*;
import static utils.Constants.Directions.*;
public class Gamepanel extends JPanel{

private MouseInputs mouseInputs;
private Game game;

// new: background images
private BufferedImage[] backgrounds;
private int backgroundIndex = 0;

    public Gamepanel(Game game){
        mouseInputs = new MouseInputs(this);
        this.game = game;
        setPanelSize();

        // load backgrounds from resources
        loadBackgrounds();

        // optional: keep a fallback background color
        setBackground(Color.GRAY);

        // ensure panel can receive keys if needed
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        requestFocusInWindow();

        addKeyListener(new keyBoardInput(this));
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);

        // ensure game knows the real view size when the panel appears or is resized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                game.setViewSize(getWidth(), getHeight());
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                game.setViewSize(getWidth(), getHeight());
            }
        });

        // ensure keyboard focus so input reaches the player
        setFocusable(true);
        addHierarchyListener(e -> {
            if (isShowing()) requestFocusInWindow();
        });
    }

    public void setPanelSize(){
        Dimension   panelSize = new Dimension(GAME_WIDTH,GAME_HEIGHT);

        setMinimumSize(panelSize);
        System .out.println("Width: " + GAME_WIDTH + " Height: " + GAME_HEIGHT);
        setPreferredSize(panelSize);
        // setMaximumSize(panelSize);
    }

    private void loadBackgrounds() {
        // place your images under resources/backgrounds and reference them here
        String[] names = {
            "/background/background_layer_3.png",
            "/background/background_layer_2.png",
            "/background/background_layer_1.png"
        };
        backgrounds = new BufferedImage[names.length];
        for (int i = 0; i < names.length; i++) {
            try (InputStream is = getClass().getResourceAsStream(names[i])) {
                if (is == null) {
                    System.err.println("Background resource not found: " + names[i]);
                    continue;
                }
                backgrounds[i] = ImageIO.read(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // allow switching background at runtime
    public void setBackgroundIndex(int idx){
        if (backgrounds == null) return;
        if (idx < 0 || idx >= backgrounds.length) return;
        backgroundIndex = idx;
        repaint();
    }
    public int getBackgroundIndex(){ return backgroundIndex; }

    public void updateGame(){
   
    }

    public void paintComponent(Graphics g){
    super.paintComponent(g);

    // draw selected background scaled to panel
    if (backgrounds != null && backgrounds[backgroundIndex] != null) {
        g.drawImage(backgrounds[backgroundIndex], 0, 0, getWidth(), getHeight(), null);
    }

    game.render(g);
  
    
    }

    public Game getGame(){
        return game;
    }

}









