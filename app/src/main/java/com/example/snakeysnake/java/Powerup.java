package com.example.snakeysnake.java;

public class Powerup {
    private final PowerupSoundStrategy soundStrategy;

    public Powerup(PowerupSoundStrategy soundStrategy) {
        this.soundStrategy = soundStrategy;
    }

    public void applyPowerup() {
        // Apply powerup logic
        soundStrategy.playSound(); // Play the sound
    }
}