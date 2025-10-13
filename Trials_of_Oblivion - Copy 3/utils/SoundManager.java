package utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class SoundManager {
    private Clip backgroundMusic;
    private float volume = 0.3f; // Start with lower volume for background music
    private boolean isMuted = false;
    private String currentMusic;
    
    public SoundManager() {
        // Initialize sound system
    }
    
    public void playBackgroundMusic(String musicFile) {
        try {
            // Stop current music if playing
            stopBackgroundMusic();
            
            currentMusic = musicFile;
            
            // Get the audio file as InputStream (works with files in JAR)
            InputStream audioStream = getClass().getResourceAsStream("/" + musicFile);
            if (audioStream == null) {
                System.err.println("Music file not found in resources: " + musicFile);
                // Try as file path
                try {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(
                        new java.io.File(musicFile));
                    setupMusicClip(audioInput);
                    return;
                } catch (Exception e) {
                    System.err.println("Music file not found as file either: " + musicFile);
                    return;
                }
            }
            
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(audioStream);
            setupMusicClip(audioInput);
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing background music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupMusicClip(AudioInputStream audioInput) 
            throws LineUnavailableException, IOException {
        backgroundMusic = AudioSystem.getClip();
        backgroundMusic.open(audioInput);
        
        // Set volume
        setVolume(volume);
        
        // Loop continuously - THIS IS THE KEY FOR LOOPING
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        
        System.out.println("Background music started (looping): " + currentMusic);
    }
    
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            if (backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }
            backgroundMusic.close();
            backgroundMusic = null;
        }
    }
    
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        
        if (backgroundMusic != null && backgroundMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Convert linear volume to decibels
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            
            // Clamp dB value to valid range
            dB = Math.max(min, Math.min(max, dB));
            gainControl.setValue(dB);
        }
    }
    
    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            // Store current volume and set to 0
            setVolume(0.0f);
        } else {
            // Restore volume
            setVolume(volume);
        }
    }
    
    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
    
    public void resumeBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isRunning() && !isMuted) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
    
    public boolean isPlaying() {
        return backgroundMusic != null && backgroundMusic.isRunning();
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public float getVolume() {
        return volume;
    }
}