package com.example.myapplication;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.*;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    AsteroidView asteroidView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        asteroidView = new AsteroidView(this);
        setContentView(asteroidView);
    }

    class AsteroidView extends SurfaceView implements Runnable {
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;
        boolean paused = false;
        Canvas canvas;
        Paint paint;
        Plane plane;
        List<Bullet> bullets = new ArrayList<>();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;


        int y;
        int posx, posy;
        int dx, dy;
        int height, width;
        boulder[] b;

        private long thisTimeFrame;
        public AsteroidView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();
        }

        @Override
        public void run() {
            Random r = new Random();
            b = new boulder[5];
            plane = new Plane();
            // Set initial plane position, e.g., middle of the screen
            plane.x = screenWidth / 2;
            plane.y = screenHeight / 2;

            posx = 50;
            posy = 50;
            dx = 20;
            dy = 45;
            for (int i = 0; i < b.length; ++i) {
                b[i] = new boulder();
                b[i].x = r.nextInt(1080);
                b[i].y = r.nextInt(2000);
                b[i].dx = r.nextInt(100) - 50;
                b[i].dy = r.nextInt(100) - 50;
                b[i].diameter = 20;
            }


            while (playing)
            {
                if (!paused) {
                    update();
                }
                draw();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {

                }
            }
        }

        private boolean bulletCollidesWithBoulder(Bullet bullet, boulder b) {
            // Find the closest point on the bullet rectangle to the center of the boulder
            float closestX = clamp(b.x, bullet.x - 5, bullet.x + 5); // Assuming bullet's width is 10
            float closestY = clamp(b.y, bullet.y - 10, bullet.y); // Assuming bullet's height is 10

            // Calculate the distance between the boulder's center and this closest point
            float distanceX = b.x - closestX;
            float distanceY = b.y - closestY;

            // Distance formula
            float distance = (float) Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));

            // If the distance is less than the radius, collision detected
            return distance < b.diameter / 2; // Assuming b.diameter represents the full diameter of the boulder
        }

        // Utility method to clamp value between min and max
        private float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        public void update() {
            // Update bullet positions
            Iterator<Bullet> bulletIterator = bullets.iterator(); // Assuming 'bullets' is a List<Bullet>
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.update(); // Update bullet position

                // Check for collision with boulders
                for (int i = 0; i < b.length; i++) {
                    if (b[i] != null && bulletCollidesWithBoulder(bullet, b[i])) {
                        b[i] = null; // Remove the boulder
                        bulletIterator.remove(); // Remove the bullet
                        break; // Stop checking this bullet against other boulders
                    }
                }

                // Remove bullets that are off the screen
                if (bullet.y < 0) {
                    bulletIterator.remove();
                }
            }

            // Existing boulder updates
            for (int i = 0; i < b.length; ++i) {
                if (b[i] != null) {
                    b[i].update();
                }
            }
        }


        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                width = canvas.getWidth();
                height = canvas.getHeight();

                // Draw the background color
                canvas.drawColor(Color.argb(255, 26, 128, 182));


                canvas.drawCircle(plane.x, plane.y, 50, paint);

                // Draw the plane
                plane.draw(canvas); // Assuming 'plane' is an instance of Plane

                // Draw the bullets
                for (Bullet bullet : bullets) { // Assuming 'bullets' is a List<Bullet>
                    bullet.draw(canvas);
                }



                // Draw each boulder
                for (int i = 0; i < b.length; ++i) {
                    b[i].width = width;
                    b[i].height = height;
                    b[i].draw(canvas); // Updated to pass only the canvas
                }

                // canvas.drawCircle(b.x, b.y, 50, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // This can still toggle pause, or you could use a separate button or area of the screen for pausing
                    paused = !paused;

                    // Shoot a bullet from the plane's position
                    // Ensure this doesn't conflict with your pause functionality
                    float bulletStartX = plane.x; // The center of the plane
                    float bulletStartY = plane.y; // Adjust as needed, maybe the front of the plane
                    Bullet newBullet = new Bullet(bulletStartX, bulletStartY);
                    bullets.add(newBullet);
                    return true;
            }
            return super.onTouchEvent(motionEvent);
        }

    }


    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        asteroidView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        asteroidView.pause();
    }

}
