package com.symbolstar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.symbolstar.template.games.GamesActivity;
import com.symbolstar.template.R;
import com.symbolstar.template.screenshot.ScreenshotActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchGames(View view) {
        startActivity(new Intent(this, GamesActivity.class));
    }

    public void launchScreenshot(View view) {
        startActivity(new Intent(this, ScreenshotActivity.class));
    }

}