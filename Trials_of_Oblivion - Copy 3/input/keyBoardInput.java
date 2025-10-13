package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import main.Gamepanel;

public class keyBoardInput implements KeyListener{
    private Gamepanel gamePanel;
    
    public keyBoardInput(Gamepanel gamePanel){
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_T:
                    // Teleport to slimes
                    gamePanel.getGame().getPlayer().setPosition(2450, 400);
                    System.out.println("Teleported to slimes area!");
                    break;
                    
                case KeyEvent.VK_A:
                    gamePanel.getGame().getPlayer().setLeft(true);
                    break;
                    
                case KeyEvent.VK_D:
                    gamePanel.getGame().getPlayer().setRight(true);
                    break;
                    
                case KeyEvent.VK_W:
                    gamePanel.getGame().getPlayer().setJumping(true);
                    break;
                    
                case KeyEvent.VK_SPACE:
                    gamePanel.getGame().getPlayer().setAttacking(true);
                    break;

                // WAVE SYSTEM CONTROLS
                case KeyEvent.VK_N:
                    // Start next wave
                    if (gamePanel.getGame().getLevelManager() != null) {
                        gamePanel.getGame().getLevelManager().startNextWave();
                        System.out.println("Starting next wave!");
                    }
                    break;
                    
                case KeyEvent.VK_U:
                    // Upgrade menu (when at upgrade station)
                    if (gamePanel.getGame().getPlayer().isAtUpgradeStation()) {
                        gamePanel.getGame().showUpgradeMenu();
                        System.out.println("Upgrade menu opened!");
                    } else {
                        System.out.println("Go to upgrade station first!");
                    }
                    break;
                    
                case KeyEvent.VK_1:
                    // Upgrade Attack OR Switch to Level 1
                    if (gamePanel.getGame().isInUpgradeMenu()) {
                        // In upgrade menu - upgrade attack
                        boolean success = gamePanel.getGame().getPlayer().upgradeAttack();
                        if (success) {
                            System.out.println("Attack upgraded!");
                        }
                    } else {
                        // Not in upgrade menu - switch level
                        if (gamePanel.getGame().getLevelManager() != null) {
                            gamePanel.getGame().getLevelManager().setCurrentLevel(1);
                            System.out.println("Switched to Level 1");
                        }
                    }
                    break;
                    
                case KeyEvent.VK_2:
                    // Upgrade Speed OR Switch to Level 2
                    if (gamePanel.getGame().isInUpgradeMenu()) {
                        // In upgrade menu - upgrade speed
                        boolean success = gamePanel.getGame().getPlayer().upgradeSpeed();
                        if (success) {
                            System.out.println("Speed upgraded!");
                        }
                    } else {
                        // Not in upgrade menu - switch level
                        if (gamePanel.getGame().getLevelManager() != null) {
                            gamePanel.getGame().getLevelManager().setCurrentLevel(2);
                            System.out.println("Switched to Level 2");
                        }
                    }
                    break;
                    
                case KeyEvent.VK_3:
                    // Upgrade Health
                    if (gamePanel.getGame().isInUpgradeMenu()) {
                        boolean success = gamePanel.getGame().getPlayer().upgradeHealth();
                        if (success) {
                            System.out.println("Health upgraded!");
                        }
                    }
                    break;
                    
                case KeyEvent.VK_ESCAPE:
                    // Exit upgrade menu
                    if (gamePanel.getGame().isInUpgradeMenu()) {
                        gamePanel.getGame().exitUpgradeMenu();
                        System.out.println("Upgrade menu closed");
                    }
                    break;

                case KeyEvent.VK_R:
                    // Respawn all slimes
                    if (gamePanel.getGame().getLevelManager() != null) {
                        gamePanel.getGame().getLevelManager().respawnAllSlimes();
                        System.out.println("All slimes respawned");
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Exception ex) {
            System.err.println("Error in keyPressed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    gamePanel.getGame().getPlayer().setLeft(false);
                    break;
                case KeyEvent.VK_D:
                    gamePanel.getGame().getPlayer().setRight(false);
                    break;
                case KeyEvent.VK_W:
                    gamePanel.getGame().getPlayer().setJumping(false);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            System.err.println("Error in keyReleased: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}