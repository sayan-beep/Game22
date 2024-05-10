package com.example.androidstudio2dgamedevelopment;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameLoop extends Thread{
    public static final double MAX_UPS = 30.0;
    private static final double UPS_PERIOD = 1E+3/MAX_UPS;

    private Game game;
    private SurfaceHolder surfaceHolder;

    private boolean isRunning = false;
    private double averageUPS;
    private double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public void startLoop() {
        Log.d("GameLoop.java", "startLoop()");
        isRunning = true;
        start();
    }

    @Override
    public void run() {
        Log.d("GameLoop.java", "run()");
        super.run();

        // Объявлять переменные времени и количества циклов
        int updateCount = 0;
        int frameCount = 0;

        long startTime;
        long elapsedTime;
        long sleepTime;

        // Игровой цикл
        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        while(isRunning) {

            // Попробуйте обновить и отрендерить игру
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    game.update();
                    updateCount++;

                    game.draw(canvas);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                if(canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
					} catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Приостановите цикл игры, чтобы не превысить заданные значения.
            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long) (updateCount*UPS_PERIOD - elapsedTime);
            if(sleepTime > 0) {
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Пропустить кадры, чтобы не отставать от целевых показателей
            while(sleepTime < 0 && updateCount < MAX_UPS-1) {
                game.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long) (updateCount*UPS_PERIOD - elapsedTime);
            }

            // вычислить среднее значение UPS and FPS
            elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime >= 1000) {
                averageUPS = updateCount / (1E-3 * elapsedTime);
                averageFPS = frameCount / (1E-3 * elapsedTime);
                updateCount = 0;
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
    }

    public void stopLoop() {
        Log.d("GameLoop.java", "stopLoop()");
        isRunning = false;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
