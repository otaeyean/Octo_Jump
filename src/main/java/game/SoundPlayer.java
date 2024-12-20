package game;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isPlaying = false;

    public SoundPlayer(String soundFilePath, float volume) {
        try {
            File soundFile = new File(soundFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(volume); // 초기 볼륨 설정
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("사운드 파일을 로드할 수 없습니다: " + soundFilePath);
            e.printStackTrace();
        }
    }

    public void setVolume(float volume) {
        if (volumeControl != null) {
            // volume 범위: -80.0f (최소) ~ 6.0f (최대)
            float min = volumeControl.getMinimum(); // -80.0
            float max = volumeControl.getMaximum(); // 6.0
            float gain = Math.min(max, Math.max(min, volume)); // 범위 제한
            volumeControl.setValue(gain);
        }
    }

    // 사운드 재생 (반복 재생 가능)
    public void play(boolean loop) {
        if (clip != null) {
            clip.setFramePosition(0); // 시작 위치를 0으로 설정
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY); // 무한 반복
            } else {
                clip.start(); // 한 번만 재생
            }
            isPlaying = true;
        }
    }

    // 사운드 정지
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
            isPlaying = false;
        }
    }

    // 사운드가 재생 중인지 확인
    public boolean isPlaying() {
        return isPlaying;
    }
}