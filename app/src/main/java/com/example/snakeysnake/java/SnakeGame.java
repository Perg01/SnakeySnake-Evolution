package com.example.snakeysnake.java;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Typeface;
import android.graphics.Rect;


import com.example.snakeysnake.R;
public class SnakeGame extends SurfaceView implements Runnable, GameLifecycle, Drawable {

    private static final int TARGET_FPS = 5;
    private static final long MILLIS_PER_SECOND = 1000;

    private Thread mThread = null;
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    private SoundManager mSoundManager;
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;
    private int mScore;
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Snake mSnake;
    private Apple mApple;
    private Rect mPauseButtonRect;
    private boolean mPlayerDead = true;
    private Bitmap mBitmapBackground;
    private long mNextFrameTime;


    public SnakeGame(Context context, Point size) {
        super(context);
        initGame(context, size);
    }

    private void initGame(Context context, Point size) {
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        mSoundManager = new SoundManager(context);
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        int buttonSize = blockSize * 3;
        mPauseButtonRect = new Rect(0, size.y - buttonSize, buttonSize, size.y);
    }

    @Override
    public void run() {
        while (mPlaying) {
            if (!mPaused && updateRequired()) {
                update();
            }
            draw();
        }
    }

    private boolean updateRequired() {
        return mNextFrameTime <= System.currentTimeMillis();
    }

    private void update() {
        mSnake.move();
        if (mSnake.checkDinner(mApple.getLocation())) {
            mApple.spawn();
            mScore++;
            mSoundManager.playEatSound();
        }
        if (mSnake.detectDeath()) {
            mSoundManager.playCrashSound();
            mPaused = true;
            mPlayerDead = true;
        } else {
            mPlayerDead = false;
        }
        mNextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / TARGET_FPS;
    }

    @Override
    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();
            drawBackground();
            drawScore();
            drawGameObjects();
            drawPauseButton();
            drawTapToPlayMessage();
            drawGroupMembers(); // Call drawGroupMembers before unlocking and posting
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void drawBackground() {
        mCanvas.drawColor(Color.argb(205, 11, 8, 102));
        mBitmapBackground = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hxh);
        mBitmapBackground = Bitmap.createScaledBitmap(mBitmapBackground, 2300, 1280, false);
        mCanvas.drawBitmap(mBitmapBackground, 0, 0, mPaint);
    }

    private void drawScore() {
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        mPaint.setTextSize(120);
        mPaint.setTypeface(Font.getCustomTypeface(getContext()));
        mCanvas.drawText(String.valueOf(mScore), 20, 120, mPaint);
    }

    private void drawGameObjects() {
        mApple.draw(mCanvas, mPaint);
        mSnake.draw(mCanvas, mPaint);
    }

    private void drawPauseButton() {
        mPaint.setColor(Color.argb(128, 255, 255, 255));
        int buttonSize = mPauseButtonRect.width() / 2;
        mCanvas.drawRect(mPauseButtonRect, mPaint);
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(40);
        mPaint.setTypeface(Font.getCustomTypeface(getContext()));
        String buttonText = mPaused ? "Resume" : "Pause";
        float textWidth = mPaint.measureText(buttonText);
        float textHeight = mPaint.ascent() + mPaint.descent();
        mCanvas.drawText(buttonText, mPauseButtonRect.centerX() - textWidth / 2, mPauseButtonRect.centerY() - textHeight / 2, mPaint);
    }


    private void drawTapToPlayMessage() {
        if (mPaused && mPlayerDead) {

            mPaint.setColor(Color.argb(255, 255, 255, 255));
            mPaint.setTextSize(250);
            mPaint.setTypeface(Font.getCustomTypeface(getContext()));
            mCanvas.drawText(getResources().getString(R.string.tap_to_play), 450, 500, mPaint);
        }
    }


    private void drawGroupMembers() {
        // Set the transparency level (alpha value)
        int alphaValue = 150; // Adjust this value to set the desired transparency level

        // Load custom font from assets
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "ChrustyRock-ORLA.ttf");

        // Draw the group members' names with transparency and custom font
        Paint paint = new Paint();
        paint.setColor(Color.argb(alphaValue, 255, 255, 255)); // Set color to white with alpha
        paint.setTextSize(60);
        paint.setTypeface(typeface); // Set custom font

        // Draw "Tony Tran"
        mCanvas.drawText("Tony Tran", mCanvas.getWidth() - 400, 90, paint);
        // Draw "Saboor Malik"
        mCanvas.drawText("Saboor Malik", mCanvas.getWidth() - 400, 165, paint);
    }





    @Override
    public void draw(Canvas canvas, Paint paint) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mPaused) {
                    if (mPauseButtonRect.contains(x, y)) {
                        if (!mPlayerDead) {
                            mPaused = false;
                        }
                        return true;
                    }
                } else {
                    if (mPauseButtonRect.contains(x, y)) {
                        if (!mPlayerDead) {
                            mPaused = true;
                        }
                        return true;
                    } else {
                        mSnake.switchHeading(motionEvent);
                    }
                }
                if (mPlayerDead) {
                    newGame();
                    mPaused = false;
                    return true;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void newGame() {
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn();
        mScore = 0;
        mNextFrameTime = System.currentTimeMillis();
        mPlayerDead = true;
    }

    @Override
    public void pause() {
        mPlaying = false;
        mSoundManager.release();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
        mSoundManager = new SoundManager(getContext());
    }
}
