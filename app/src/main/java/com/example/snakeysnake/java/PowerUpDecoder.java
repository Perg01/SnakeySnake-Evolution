package com.example.snakeysnake.java;

import android.content.Context;
import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

public class PowerUpDecoder {
    private static final Map<String, PowerUps> powerUps = new HashMap<>();

    // add different power ups here
    public static void initializePowerUpDecoder(Context context, Point spawnRange, int size)  {
        powerUps.put("Lightning", new LightningPowerUp(context, spawnRange, size));
        powerUps.put("Mushroom", new SizeUpPowerUp(context, spawnRange, size));
    }

    public static PowerUps decodePowerUp(String powerUpType) {
        return powerUps.get(powerUpType);
    }
}
