package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Plane {
    float x, y; // Plane's position
    float size; // Size of the plane
    Paint paint; // Paint object for drawing

    public Plane() {
        paint = new Paint();
        paint.setColor(Color.argb(255, 0, 0, 0)); // Set plane color
        size = 20; // Set default size
    }

    public void draw(Canvas canvas) {
        Path path = new Path();
        path.moveTo(x, y - size); // Top
        path.lineTo(x - size / 2, y + size / 2); // Bottom left
        path.lineTo(x + size / 2, y + size / 2); // Bottom right
        path.close();
        canvas.drawPath(path, paint);
    }

    // Method to shoot a bullet
    public Bullet shoot() {
        return new Bullet(x, y - size); // Create a new bullet at the top of the plane
    }
}

