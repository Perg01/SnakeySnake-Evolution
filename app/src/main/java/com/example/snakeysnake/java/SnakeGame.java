package com.example.snakeysnake.java;

//import static com.example.snakeysnake.java.PowerUpDecoder.powerUps;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

public class SnakeGame extends SurfaceView implements Runnable, GameLifecycle, Drawable {

    //    private List<Apple> apples;
    private List<LightningPowerUp> lightningPowerUps;
    private List<SizeUpPowerUp> sizeUpPowerUps;
    private List<BlackHole> mBlackHoles;
    private List<StarSuperNova> mSNovas;
    private static final int DEFAULT_TARGET_FPS = 5;
    private static int TARGET_FPS = DEFAULT_TARGET_FPS;
    private static final long MILLIS_PER_SECOND = 1000;

    private Thread mThread = null;
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    private SoundManager mSoundManager;
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;
    private int mScore = 0;
    private Canvas mCanvas;
    private int mHighScore = 0;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Snake mSnake;
    private Apple mApple;
    private Rect mPauseButtonRect;
    private boolean mPlayerDead = true;
    private Bitmap mBitmapBackground;
    private long mNextFrameTime;
    private int x;
    private int y;
    private double mPowerUpTimer;

    private double mBlackHoleTimer;
    private double mNovaTimer;
    private boolean musicFlag = false;


    public void setTargetFPS(int fps) {
        TARGET_FPS = fps;
    }

    public int getTargetFPS() {
        return TARGET_FPS;
    }

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
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        this.x = size.x;
        this.y = size.y;
        this.setmPowerUpTimer(0);


        // separate list of power ups
        lightningPowerUps = new ArrayList<>();
        sizeUpPowerUps = new ArrayList<>();
        mSNovas = new ArrayList<>();

        // seperate list of obsticles
        mBlackHoles = new ArrayList<>();

        //calls methods to spawn obtainable objects/power-ups in game
        spawnLightningPowerUp(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        spawnSizeUpPowerUp(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);

        // In your game code, where you create and manage powerups
        Powerup lightningPowerup = new Powerup(new LightningPowerupSound(context));
        Powerup speedPowerup = new Powerup(new SpeedPowerupSound(context));

        // When a powerup is applied
        lightningPowerup.applyPowerup(); // This will play the sound for the lightning powerup
        speedPowerup.applyPowerup(); // This will play the sound for the speed powerup

        int buttonSize = blockSize * 3;
        mPauseButtonRect = new Rect(0, size.y - buttonSize, buttonSize, size.y);

        PowerUpDecoder.initializePowerUpDecoder(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
    }

    private void spawnSizeUpPowerUp(Context context, Point spawnRange, int size) {
        SizeUpPowerUp powerup = new SizeUpPowerUp(context, spawnRange, size);
        powerup.spawn();
        sizeUpPowerUps.add(powerup);
    }

    private void spawnLightningPowerUp(Context context, Point spawnRange, int size) {
        LightningPowerUp powerUp = new LightningPowerUp(context, spawnRange, size);
        powerUp.spawn();
        lightningPowerUps.add(powerUp);
    }

    private void spawnBlackHole(Context context, Point spawnRange, int size) {
        BlackHole blackHole = new BlackHole(context, spawnRange, size);
        blackHole.spawn();
        mBlackHoles.add(blackHole);
    }

    private void spawnSNova(Context context, Point spawnRange, int size){
        StarSuperNova sNova = new StarSuperNova(context, spawnRange, size);
        sNova.spawn();
        mSNovas.add(sNova);
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

    private void updateHighScore() {
        if (mScore > mHighScore) {
            mHighScore = mScore;
        }
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
            if (mSnake.isDoubleSize()) {
                mSnake.halfSize();
            }
        } else {
            mPlayerDead = false;
        }

        Iterator<LightningPowerUp> iterator = lightningPowerUps.iterator();
        while (iterator.hasNext()) {
            LightningPowerUp lightningPowerUp = iterator.next();
            if (mSnake.checkCollision(lightningPowerUp.getLocation())) {
                PowerUps powerUp = PowerUpDecoder.decodePowerUp("Lightning");
                powerUp.applyPowerUps(this);
                iterator.remove();
                spawnLightningPowerUp(getContext(), new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), mSnake.getSegmentSize());
                mSoundManager.playEatSound();
                break;
            }
        }

        if(checkTimer(mNovaTimer) >= 10 && mSNovas.isEmpty()){
            spawnSNova(getContext(), new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), mSnake.getSegmentSize());
            mSoundManager.playSNovaCreateSound();
        }

        Iterator<StarSuperNova> sIterator = mSNovas.iterator();
        while(sIterator.hasNext()){
            StarSuperNova sNovaPowerUp = sIterator.next();
            if(mSnake.checkCollision(sNovaPowerUp.getLocation())){
                mBlackHoles.remove(mBlackHoles.size()-1);
                mSoundManager.playBlackHoleRemoveSound();
                sIterator.remove();
                this.setSNovaTimer(System.nanoTime());
            }
        }
        Iterator<SizeUpPowerUp> mIterator = sizeUpPowerUps.iterator();

        //If snake eats mushroom grow *2 for 10 seconds
        if (!mIterator.hasNext() && checkTimer(mPowerUpTimer) >= 10) {
            mSoundManager.playSmallerSound();
            mSnake.halfSize();
            spawnSizeUpPowerUp(getContext(), new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), mSnake.getSegmentSize());

        } else if (mIterator.hasNext()) {
            while (mIterator.hasNext()) {
                SizeUpPowerUp sizeUpPowerUp = mIterator.next();
                if (mSnake.checkCollision(sizeUpPowerUp.getLocation())) {
                    PowerUps mPowerUp = PowerUpDecoder.decodePowerUp("Mushroom");
                    //mPowerUp.applyPowerUps(this);
                    mSnake.doubleSize();
                    mIterator.remove();
                    this.setmPowerUpTimer(System.nanoTime());
                    mSoundManager.playEatSound();
                    break;
                }
            }
        }
//        for (LightningPowerUp lightningPowerUp: lightningPowerUps) {
//            if (mSnake.checkDinner(lightningPowerUp.getLocation())) {
//                PowerUps powerUps = PowerUpDecoder.decodePowerUp("Lightning");
//                powerUps.applyPowerUps(this);
//                lightningPowerUp.spawn();
//                mScore++;
//                mSoundManager.playEatSound();
//            }
//        }

        if(checkTimer(mBlackHoleTimer) > 8){
            spawnBlackHole(getContext(), new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), mSnake.getSegmentSize());
            mSoundManager.playBlackHoleSound();
            this.setBlackHolesTimer(System.nanoTime());
        }

        Iterator<BlackHole> bIterator = mBlackHoles.iterator();
        while (bIterator.hasNext()) {
            BlackHole blackHole = bIterator.next();
            if (mSnake.checkCollision(blackHole.getLocation())) {
                mPlayerDead = true;
                mPaused = true;
                mSoundManager.playCrashSound();
                break;
            }
        }


        if (mPlayerDead) {
            mSoundManager.getBgMusic().pause();
            mSoundManager.getBgMusic().seekTo(0);
            musicFlag = !musicFlag;
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
        mBitmapBackground = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.nightriderbg);
        mBitmapBackground = Bitmap.createScaledBitmap(mBitmapBackground, x, y, false);
        mCanvas.drawBitmap(mBitmapBackground, 0, 0, mPaint);
    }

    private void drawScore() {
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        mPaint.setTextSize(120);
        mPaint.setTypeface(Font.getCustomTypeface(getContext()));
        mCanvas.drawText(String.valueOf(mScore), 20, 120, mPaint);
    }

    private void drawHighScore() {
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        mPaint.setTextSize(120);
        mPaint.setTypeface(Font.getCustomTypeface(getContext()));
        mCanvas.drawText("High Score: " + mHighScore, 20, 220, mPaint);
    }

    private void drawGameObjects() {
        mApple.draw(mCanvas, mPaint);
        mSnake.draw(mCanvas, mPaint);

        for (LightningPowerUp lightningPowerUp : lightningPowerUps) {
            lightningPowerUp.draw(mCanvas, mPaint);
        }
        for (SizeUpPowerUp sizeUpPowerUp : sizeUpPowerUps) {
            sizeUpPowerUp.draw(mCanvas, mPaint);
        }
        for(BlackHole blackHole : mBlackHoles){
            blackHole.draw(mCanvas, mPaint);
        }
        for(StarSuperNova sNova: mSNovas){
            sNova.draw(mCanvas, mPaint);
        }
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
            updateHighScore();
            drawHighScore();
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

        mCanvas.drawText("Steven Ngo", mCanvas.getWidth() - 450, 90, paint);
        mCanvas.drawText("Saboor Malik", mCanvas.getWidth() - 450, 165, paint);
        mCanvas.drawText("Jaspreet Singh", mCanvas.getWidth() - 450, 240, paint);
        mCanvas.drawText("Alexander Fails", mCanvas.getWidth() - 450, 315, paint);


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
                if (!mSoundManager.getBgMusic().isPlaying() && !musicFlag) {
                    mSoundManager.getBgMusic().start();
                    musicFlag = !musicFlag;
                    this.setBlackHolesTimer(System.nanoTime());
                    this.setSNovaTimer(System.nanoTime());
                }
                if (mPaused) {
                    if (mPauseButtonRect.contains(x, y)) {
                        if (!mPlayerDead) { // resume
                            if (!mSoundManager.getBgMusic().isPlaying()) {
                                mSoundManager.getBgMusic().start();
                            }
                            mPaused = false;
                        }
                        return true;
                    }
                } else {
                    if (mPauseButtonRect.contains(x, y)) {
                        if (!mPlayerDead) { //pause
                            if (mSoundManager.getBgMusic().isPlaying()) {
                                mSoundManager.getBgMusic().pause();
                            }

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
        updateHighScore();
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn();
        lightningPowerUps.clear();
        sizeUpPowerUps.clear();
        mBlackHoles.clear();
        mSNovas.clear();

        if(mSnake.isDoubleSize()){
            mSnake.halfSize();
        }

        spawnLightningPowerUp(getContext(), new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), mSnake.getSegmentSize());
        spawnSizeUpPowerUp(getContext(), new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), mSnake.getSegmentSize());
        //spawnBlackHole(getContext(), new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), mSnake.getSegmentSize());
        mScore = 0;
        mNextFrameTime = System.currentTimeMillis();
        mPlayerDead = true;
        setTargetFPS(DEFAULT_TARGET_FPS);

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

    public void setmPowerUpTimer(double mPowerUpTimer) {
        this.mPowerUpTimer = mPowerUpTimer;
    }

    public void setBlackHolesTimer(double mBlackHoleTimer) {
        this.mBlackHoleTimer = mBlackHoleTimer;
    }

    public void setSNovaTimer(double mNovaTimer){
        this.mNovaTimer = mNovaTimer;
    }

    public double checkTimer(double startTime) {
        double endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000_000.0; //Convert to Seconds
    }
}
