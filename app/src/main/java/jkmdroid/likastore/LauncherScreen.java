package jkmdroid.likastore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by jkmdroid on 6/25/21.
 */
public class LauncherScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_screen);

        int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //moving to another activity
                Intent registerActivity = new Intent(LauncherScreen.this, MainActivity.class);
                startActivity(registerActivity);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}