package com.example.androidstudio2dgamedevelopment;

public class Utils {

    /**
     * getDistanceBetweenPoints возвращает расстояние между двумерными точками p1 и p2
     * @параметр p1x
     * @параметр p1y
     * @параметр p2x
     * @параметр p2y
     * @return
     */
    public static double getDistanceBetweenPoints(double p1x, double p1y, double p2x, double p2y) {
        return Math.sqrt(Math.pow(p1x - p2x, 2) + Math.pow(p1y - p2y, 2));
    }
}
