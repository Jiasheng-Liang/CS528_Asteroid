package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class Bullet {
    float x, y, speed;
    Paint paint;

    public Bullet(float startX, float startY) {
        x = startX;
        y = startY;
        speed = 20; // Adjust speed as necessary
        paint = new Paint();
        paint.setColor(Color.argb(255, 255, 0, 0)); // Bullet color
    }

    public void update() {
        y -= speed; // Move bullet up
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x - 5, y - 10, x + 5, y, paint); // Draw as a small rectangle
    }
}
