package com.example.androidstudio2dgamedevelopment.gameobject;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.example.androidstudio2dgamedevelopment.GameLoop;
import com.example.androidstudio2dgamedevelopment.R;

/**
 * enemy - это персонаж, который всегда движется в направлении игрока.
 * Класс enemy является продолжением Круга, который является продолжением игрового объекта
 */
public class Enemy extends Circle {

    private static final double SPEED_PIXELS_PER_SECOND = Player.SPEED_PIXELS_PER_SECOND*0.6;
    private static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / GameLoop.MAX_UPS;
    private static final double SPAWNS_PER_MINUTE = 20;
    private static final double SPAWNS_PER_SECOND = SPAWNS_PER_MINUTE/60.0;
    private static final double UPDATES_PER_SPAWN = GameLoop.MAX_UPS/SPAWNS_PER_SECOND;
    private static double updatesUntilNextSpawn = UPDATES_PER_SPAWN;
    private Player player;

    public Enemy(Context context, Player player, double positionX, double positionY, double radius) {
        super(context, ContextCompat.getColor(context, R.color.enemy), positionX, positionY, radius);
        this.player = player;
    }

    /**
     * Enemy - это перегруженный конструктор, используемый для создания врагов в случайных местах
     * @param context
     * @param player
     */
    public Enemy(Context context, Player player) {
        super(
            context,
            ContextCompat.getColor(context, R.color.enemy),
   Math.random()*1000,
   Math.random()*1000,
     30
        );
        this.player = player;
    }

    /**
     * Функция readyToSpawn проверяет, должен ли появиться новый враг, в соответствии с установленным количеством появлений
     * в минуту (см.  "SPAWN_PER_MINUTE" вверху)
     * @return
     */
    public static boolean readyToSpawn() {
        if (updatesUntilNextSpawn <= 0) {
            updatesUntilNextSpawn += UPDATES_PER_SPAWN;
            return true;
        } else {
            updatesUntilNextSpawn --;
            return false;
        }
    }

    public void update() {
        // Измените скорость противника так, чтобы она была направлена в сторону игрока
        //
        // Вычислить вектор от противника к игроку (в x и y)
        double distanceToPlayerX = player.getPositionX() - positionX;
        double distanceToPlayerY = player.getPositionY() - positionY;

        // Вычилсить (absolute) дистанцию между enemy (this) and player
        double distanceToPlayer = GameObject.getDistanceBetweenObjects(this, player);

        // Вычислить направление от enemy до player
        double directionX = distanceToPlayerX/distanceToPlayer;
        double directionY = distanceToPlayerY/distanceToPlayer;

        // Установить ускорение в направление к player
        if(distanceToPlayer > 0) { // Avoid division by zero
            velocityX = directionX*MAX_SPEED;
            velocityY = directionY*MAX_SPEED;
        } else {
            velocityX = 0;
            velocityY = 0;
        }

        // =========================================================================================
        //  изменить позицию enemy
        // =========================================================================================
        positionX += velocityX;
        positionY += velocityY;
    }
}

