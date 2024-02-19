package com.example.myapplication;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.*;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    AsteroidView asteroidView;
    private TextView levelTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a LinearLayout to hold both the AsteroidView and the TextView
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL); // Set orientation to vertical

        // Create a TextView and set its text
        levelTextView = new TextView(this);
        levelTextView.setText("Lvl. 1");
        levelTextView.setTextSize(24);
        // Optionally, you can set an ID if you need to reference it later
        // levelTextView.setId(View.generateViewId());
        // Add the TextView to the LinearLayout
        linearLayout.addView(levelTextView);

        // Set the LinearLayout as the content view
        setContentView(linearLayout);

        // Create an instance of AsteroidView and set it as the content view
        asteroidView = new AsteroidView(this);
        linearLayout.addView(asteroidView);

        // Set the LinearLayout as the content view
        setContentView(linearLayout);
    }


    class AsteroidView extends SurfaceView implements Runnable {
        int score = 0;

        bullets.clear();
        private int currentLevel = 1;
        private int maxBullets = 10; // Maximum number of bullets for the first level
        private int currentBullets = 0; // Counter for the current number of bullets
        private int[] scoreThresholds = {3, 6, 9}; // Example score thresholds for advancing to the next level


        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;
        boolean paused = false;
        Canvas canvas;
        Paint paint;
        Plane plane;
        boulder[] b = new boulder[10];
        List<Bullet> bullets = new ArrayList<>();
        boolean[] boulderCollisions = new boolean[b.length];


        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;


        int y;
        int posx, posy;
        int dx, dy;
        int height, width;


        private long thisTimeFrame;
        public AsteroidView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();
        }

        private void updateLevelLabel() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("UIUpdate", "Updating level label to Lvl. " + currentLevel);
                    if (levelTextView != null) {
                        levelTextView.setText("Lvl. " + currentLevel);
                    } else {
                        Log.e("UIUpdate", "levelTextView is null");
                    }
                }
            });
        }

        private void adjustDifficulty() {
            // Adjust game parameters based on the current level
            // For example, increase the number of boulders and their speed
            int boulderSpeedIncrement = 2; // Example speed increment for each level
            int boulderCountIncrement = 2; // Example count increment for each level
            int maxBulletsIncrement = 5; // Example increment in maximum bullets for each level
            currentLevel++;
            score = 0;

            // Increase boulder speed
            for (boulder boulder : b) {
                boulder.dx += boulderSpeedIncrement; // Adjust the horizontal speed
                boulder.dy += boulderSpeedIncrement; // Adjust the vertical speed
            }

            // Increase the number of boulders
            int bouldersToAdd = boulderCountIncrement * currentLevel;
            for (int i = 0; i < bouldersToAdd; i++) {
                // Add new boulders to the array
                if (i < b.length) {
                    b[i] = new boulder();
                    // Set initial position and speed of the new boulder
                    b[i].x = 50 + (i * 150); // Example: Set x position with spacing between boulders
                    b[i].y = 50; // Example: Set y position
                    b[i].dx = 10; // Example: Set horizontal speed
                    b[i].dy = 10; // Example: Set vertical speed
                    b[i].diameter = 20; // Example: Set diameter
                } else {
                    // Expand the array if needed
                    int newSize = b.length + 1; // Increase the size by 1
                    boulder[] newBoulders = new boulder[newSize]; // Create a new array with increased size
                    // Copy existing boulders to the new array
                    System.arraycopy(b, 0, newBoulders, 0, b.length);
                    // Add the new boulder to the end of the array
                    newBoulders[newSize - 1] = new boulder();
                    // Set initial position and speed of the new boulder
                    newBoulders[newSize - 1].x = 50 + (i * 150); // Example: Set x position with spacing between boulders
                    newBoulders[newSize - 1].y = 50; // Example: Set y position
                    newBoulders[newSize - 1].dx = 10; // Example: Set horizontal speed
                    newBoulders[newSize - 1].dy = 10; // Example: Set vertical speed
                    newBoulders[newSize - 1].diameter = 20; // Example: Set diameter
                    // Assign the new array back to the original array variable
                    b = newBoulders;
                }
            }

            // Update the maximum bullets for the current level
            maxBullets += maxBulletsIncrement;

            // Reset the current number of bullets for the new level
            currentBullets = 0;

            // Increment the current level


            // Update the level label to reflect the new level
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (levelTextView != null) {
                        levelTextView.setText("Lvl. " + currentLevel);
                    }
                }
            });
        }






        @Override
        public void run() {
            Random r = new Random();

            plane = new Plane();
            // Set initial plane position, e.g., middle of the screen
            plane.x = (float) screenWidth / 2;
            plane.y = (float) screenHeight * 9 / 10;

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

            while (playing) {
                if (!paused) {
                    try {
                        update();
                    } catch (Exception e) {
                        // Handle the exception (e.g., log the error)
                        e.printStackTrace();
                    }
                }
                try {
                    draw();
                } catch (Exception e) {
                    // Handle the exception (e.g., log the error)
                    e.printStackTrace();
                }
                try {
                    // Check if the player has met the criteria to advance to the next level
                    if (score >= scoreThresholds[currentLevel - 1]) {
                        //currentLevel++; // Advance to the next level
                        // Adjust game parameters for the next level (e.g., increase difficulty)
                        adjustDifficulty();
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Handle the exception (e.g., log the error)
                    e.printStackTrace();
                }
            }
        }

        private boolean bulletCollidesWithBoulder(Bullet bullet, boulder b) {
            // Calculate the distance between the centers of the bullet and the boulder
            float distanceX = b.x - bullet.x;
            float distanceY = b.y - bullet.y;

            // Calculate the combined radii of the bullet and the boulder
            float combinedRadius = bullet.radius + b.diameter / 2; // Assuming b.diameter represents the full diameter of the boulder

            // Use Pythagoras' theorem to calculate the distance
            float distance = (float) Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));

            // If the distance is less than the combined radii, collision detected
            return distance < combinedRadius;
        }

        // Utility method to clamp value between min and max
        private float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        public void update() {
            // Initialize boulder collisions flags
            Arrays.fill(boulderCollisions, false);

            // Update bullet positions
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.update(); // Update bullet position

                // Check if the bullet reaches the top of the screen
                if (bullet.y < 0) {
                    bulletIterator.remove();
                    score++; // Increment the score
                }

                // Check for collision with boulders
                for (int i = 0; i < b.length; i++) {
                    if (b[i] != null && !boulderCollisions[i] && bulletCollidesWithBoulder(bullet, b[i])) {
                        boulderCollisions[i] = true; // Set the collision flag for this boulder
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


            // Check if the score meets or exceeds the threshold for the current level
            if (currentLevel - 1 < scoreThresholds.length && score >= scoreThresholds[currentLevel - 1]) {

                adjustDifficulty(); // Adjust game difficulty for the new level

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

                // Draw the plane
                plane.draw(canvas); // Assuming 'plane' is an instance of Plane

                // Draw the bullets
                for (Bullet bullet : bullets) {
                    bullet.draw(canvas);
                }

                // Draw each boulder
                for (int i = 0; i < b.length; ++i) {
                    if (!boulderCollisions[i]) {
                        b[i].width = width;
                        b[i].height = height;
                        b[i].draw(canvas);
                    }
                }

                // Draw the current score on the screen
                paint.setColor(Color.WHITE);
                paint.setTextSize(48);
                canvas.drawText("Score: " + score, 20, 50, paint);

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
            Log.d("AsteroidView", "Game thread resumed"); // Add this log
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Shoot a bullet from the plane's position if the current number of bullets is less than maxBullets
                    if (currentBullets < maxBullets) {
                        float bulletStartX = plane.x; // The center of the plane
                        float bulletStartY = plane.y; // Adjust as needed, maybe the front of the plane
                        Bullet newBullet = new Bullet(bulletStartX, bulletStartY);
                        bullets.add(newBullet);
                        currentBullets++; // Update currentBullets count
                    }
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
