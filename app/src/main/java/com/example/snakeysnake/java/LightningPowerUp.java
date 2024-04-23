package com.example.snakeysnake.java;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.example.snakeysnake.R;

import java.util.Random;

public class LightningPowerUp implements PowerUps {

//    private static final int SPEED_UP_FPS = 10;
    private Context context;
    private final Point spawnRange;
    private final int size;
    private Bitmap lightningBitmap;
    private final Point location = new Point();


    public LightningPowerUp(Context context, Point spawnRange, int size) {
        this.context = context;
        this.spawnRange = spawnRange;
        this.size = size;
        lightningBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.lightning);
        lightningBitmap = Bitmap.createScaledBitmap(lightningBitmap, size,size,false);
    }

    @Override
    public void applyPowerUps(SnakeGame sg) {
        int currentFPS = sg.getTargetFPS();
        sg.setTargetFPS(currentFPS + 3);
    }

    public Point getLocation() {
        return new Point(location);
    }

    public void spawn() {
        Random random = new Random();
        int newX = random.nextInt(spawnRange.x) + 1;
        int newY = random.nextInt(spawnRange.y - 1) + 1;
        setLocation(newX, newY);
    }

    private void setLocation(int x, int y) {
        this.location.set(x, y);
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(lightningBitmap, getLocation().x * size, getLocation().y * size, paint);
    }
}
