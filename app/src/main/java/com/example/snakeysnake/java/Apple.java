package com.example.snakeysnake.java;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

import com.example.snakeysnake.R;


class Apple extends GameObject implements Drawable {

    private Point location = new Point();
    private final Point spawnRange;
    private final int size;
    private Bitmap bitmapApple;

    Apple(Context context, Point spawnRange, int size) {
        this.spawnRange = spawnRange;
        this.size = size;
        this.setLocation(-10, -10); // Initialize location off-screen
        this.bitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.moon);
        this.bitmapApple = Bitmap.createScaledBitmap(this.bitmapApple, size, size, false);
    }

    @Override
    public void spawn() {
        Random random = new Random();
        int newX = random.nextInt(spawnRange.x) + 1;
        int newY = random.nextInt(spawnRange.y - 1) + 1;
        this.setLocation(newX, newY);
    }

    @Override
    public Point getLocation() {
        return new Point(location); // Return a copy to maintain encapsulation
    }

    // Setter method for location
    private void setLocation(int x, int y) {
        this.location.set(x, y);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(bitmapApple, location.x * size, location.y * size, paint);
    }

    @Override
    public void draw(){
    }
}
