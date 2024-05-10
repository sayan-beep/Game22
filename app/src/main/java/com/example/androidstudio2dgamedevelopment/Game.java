package com.example.androidstudio2dgamedevelopment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.androidstudio2dgamedevelopment.gameobject.Circle;
import com.example.androidstudio2dgamedevelopment.gameobject.Enemy;
import com.example.androidstudio2dgamedevelopment.gameobject.Player;
import com.example.androidstudio2dgamedevelopment.gameobject.Spell;
import com.example.androidstudio2dgamedevelopment.gamepanel.GameOver;
import com.example.androidstudio2dgamedevelopment.gamepanel.Joystick;
import com.example.androidstudio2dgamedevelopment.gamepanel.Performance;
import com.example.androidstudio2dgamedevelopment.graphics.Animator;
import com.example.androidstudio2dgamedevelopment.graphics.SpriteSheet;
import com.example.androidstudio2dgamedevelopment.map.Tilemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class Game extends SurfaceView implements SurfaceHolder.Callback {

    private final Tilemap tilemap;
    private int joystickPointerId = 0;
    private final Joystick joystick;
    private final Player player;
    private GameLoop gameLoop;
    private List<Enemy> enemyList = new ArrayList<Enemy>();
    private List<Spell> spellList = new ArrayList<Spell>();
    private int numberOfSpellsToCast = 0;
    private GameOver gameOver;
    private Performance performance;
    private GameDisplay gameDisplay;

    public Game(Context context) {
        super(context);

        // Получить surface holder и добавить обратный вызов
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        // Инициализировать игровые панели
        performance = new Performance(context, gameLoop);
        gameOver = new GameOver(context);
        joystick = new Joystick(275, 700, 70, 40);

        // Инициализировать игровые объекты
        SpriteSheet spriteSheet = new SpriteSheet(context);
        Animator animator = new Animator(spriteSheet.getPlayerSpriteArray());
        player = new Player(context, joystick, 2*500, 500, 32, animator);

        // Инициализируйте отображение и отцентрируйте его вокруг проигрывателя
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        gameDisplay = new GameDisplay(displayMetrics.widthPixels, displayMetrics.heightPixels, player);

        // Инициализировать мозаичную карту (Tilemap)
        tilemap = new Tilemap(spriteSheet);

        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Обрабатывать действия по событиям касания, вводимые пользователем
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (joystick.getIsPressed()) {
                    // Джойстик был нажат до этого события -> произнесение заклинания
                    numberOfSpellsToCast ++;
                } else if (joystick.isPressed((double) event.getX(), (double) event.getY())) {
                    // В этом случае нажат джойстик -> setIsPressed(значение true) и сохранен идентификатор указателя
                    joystickPointerId = event.getPointerId(event.getActionIndex());
                    joystick.setIsPressed(true);
                } else {
                    // Джойстик не был нажат ранее и не будет нажат в этом событии -> произнесение заклинания
                    numberOfSpellsToCast ++;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (joystick.getIsPressed()) {
                    // Ранее джойстик был нажат, а теперь перемещен
                    joystick.setActuator((double) event.getX(), (double) event.getY());
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (joystickPointerId == event.getPointerId(event.getActionIndex())) {
                    // указатель джойстика был отпущен -> setIsPressed(ложь) и сброс настроек()
                    joystick.setIsPressed(false);
                    joystick.resetActuator();
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("Game.java", "surfaceCreated()");
        if (gameLoop.getState().equals(Thread.State.TERMINATED)) {
            SurfaceHolder surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);
            gameLoop = new GameLoop(this, surfaceHolder);
        }
        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("Game.java", "surfaceChanged()");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("Game.java", "surfaceDestroyed()");
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Draw Tilemap
        tilemap.draw(canvas, gameDisplay);

        // Draw game objects
        player.draw(canvas, gameDisplay);

        for (Enemy enemy : enemyList) {
            enemy.draw(canvas, gameDisplay);
        }

        for (Spell spell : spellList) {
            spell.draw(canvas, gameDisplay);
        }

        // Рисование игровых панелей
        joystick.draw(canvas);
        performance.draw(canvas);

        // Ничья, игра заканчивается, если игрок мертв
        if (player.getHealthPoint() <= 0) {
            gameOver.draw(canvas);
        }
    }

    public void update() {
        // Прекратить обновление игры, если игрок мертв
        if (player.getHealthPoint() <= 0) {
            return;
        }

        // Обновить состояние игры
        joystick.update();
        player.update();

        // Спавн enemy
        if(Enemy.readyToSpawn()) {
            enemyList.add(new Enemy(getContext(), player));
        }

        // Обновите состояния всех врагов
        for (Enemy enemy : enemyList) {
            enemy.update();
        }

        // Обновлять состояния всех заклинаний
        while (numberOfSpellsToCast > 0) {
            spellList.add(new Spell(getContext(), player));
            numberOfSpellsToCast --;
        }
        for (Spell spell : spellList) {
            spell.update();
        }

        // Пройдитесь по списку врагов и проверьте, нет ли столкновений между каждым врагом и игроком
        // и заклинаниями в списке заклинаний.
        Iterator<Enemy> iteratorEnemy = enemyList.iterator();
        while (iteratorEnemy.hasNext()) {
            Circle enemy = iteratorEnemy.next();
            if (Circle.isColliding(enemy, player)) {
                // Убрать противника, если он столкнется с игроком
                iteratorEnemy.remove();
                player.setHealthPoint(player.getHealthPoint() - 1);
                continue;
            }

            Iterator<Spell> iteratorSpell = spellList.iterator();
            while (iteratorSpell.hasNext()) {
                Circle spell = iteratorSpell.next();
                // Удалить врага, если он столкнется с заклинанием
                if (Circle.isColliding(spell, enemy)) {
                    iteratorSpell.remove();
                    iteratorEnemy.remove();
                    break;
                }
            }
        }

        // Обновите gameDisplay, чтобы его центр был установлен на новый центр игрока
        // игровые координаты
        gameDisplay.update();
    }

    public void pause() {
        gameLoop.stopLoop();
    }
}
