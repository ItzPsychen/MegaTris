package megatris.ui;

import javax.sound.sampled.*;
import java.io.File;
import java.util.prefs.Preferences;

public class SoundManager {
    // Gestore del salvataggio automatico nativo di Java
    private static final Preferences prefs = Preferences.userNodeForPackage(SoundManager.class);
    
    private static Clip musicClip;
    
    // Legge il volume salvato all'avvio. Se è la prima volta, imposta 1.0f (100%)
    private static float musicVolume = prefs.getFloat("musicVolume", 1.0f);
    private static float sfxVolume = prefs.getFloat("sfxVolume", 1.0f);

    public static void playMusic(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            applyMusicVolume();
        } catch (Exception e) {
            System.out.println("Errore musica: " + e.getMessage());
        }
    }

    public static void playSound(String filePath) {
        if (sfxVolume <= 0.0f) return;
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log10(sfxVolume) * 20.0);
            gainControl.setValue(dB);
            
            clip.start();
        } catch (Exception e) {
            System.out.println("Errore SFX: " + e.getMessage());
        }
    }

    public static void setMusicVolume(float volume) {
        musicVolume = volume;
        prefs.putFloat("musicVolume", volume); // Salva istantaneamente
        applyMusicVolume();
    }

    public static void setSfxVolume(float volume) {
        sfxVolume = volume;
        prefs.putFloat("sfxVolume", volume); // Salva istantaneamente
    }

    // Metodi utili per leggere il volume (da 0 a 100) e settare gli slider all'avvio
    public static int getMusicVolumePercent() { return Math.round(musicVolume * 100); }
    public static int getSfxVolumePercent() { return Math.round(sfxVolume * 100); }

    private static void applyMusicVolume() {
        if (musicClip != null && musicClip.isOpen()) {
            FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            if (musicVolume <= 0.0f) {
                gainControl.setValue(gainControl.getMinimum()); 
            } else {
                float dB = (float) (Math.log10(musicVolume) * 20.0);
                gainControl.setValue(dB);
            }
        }
    }
}