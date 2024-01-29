package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class boulder {
    float x, y, dx, dy, diameter;
    float width, height;
    Paint paint; // Paint object for the boulder

    public boulder() {
        paint = new Paint();
        paint.setColor(Color.argb(255, 255, 255, 255)); // Default white color

    }

    // Method to randomly change the color of the boulder
    private void changeColor() {
        Random random = new Random();
        paint.setColor(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256)));
    }

    public void update() {
        x += dx;
        y += dy;

        // Check for boundary collisions
        boolean bounced = false;
        if (x < 0 || x > width) {
            dx = -dx;
            bounced = true;
        }
        if (y < 0 || y > height) {
            dy = -dy;
            bounced = true;
        }

        // Change color if a bounce occurred
        if (bounced) {
            changeColor();
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, diameter, paint);
    }
}
