package com.example.androidstudio2dgamedevelopment.gameobject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.androidstudio2dgamedevelopment.GameDisplay;

/**
 В данном коде представлена абстрактный класс Circle, который наследуется
 от класса GameObject и реализует метод draw для отрисовки объекта в виде круга.
 */
public abstract class Circle extends GameObject {
    protected double radius;
    protected Paint paint;

    public Circle(Context context, int color, double positionX, double positionY, double radius) {
        super(positionX, positionY);
        this.radius = radius;

        // Set colors of circle
        paint = new Paint();
        paint.setColor(color);
    }

    /**
     * isColliding проверяет, сталкиваются ли два объекта circle на основе их положений и радиусов.
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean isColliding(Circle obj1, Circle obj2) {
        double distance = getDistanceBetweenObjects(obj1, obj2);
        double distanceToCollision = obj1.getRadius() + obj2.getRadius();
        if (distance < distanceToCollision)
            return true;
        else
            return false;
    }

    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        canvas.drawCircle(
                (float) gameDisplay.gameToDisplayCoordinatesX(positionX),
                (float) gameDisplay.gameToDisplayCoordinatesY(positionY),
                (float) radius,
                paint
        );
    }

    public double getRadius() {
        return radius;
    }
}