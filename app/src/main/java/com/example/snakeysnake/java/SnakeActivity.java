package com.example.snakeysnake.java;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SnakeActivity extends Activity {

    private SnakeGame mSnakeGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeSnakeGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSnakeGame.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSnakeGame.pause();
    }

    private void initializeSnakeGame() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mSnakeGame = new SnakeGame(this, size);
        setContentView(mSnakeGame);
    }
}

