package com.dj.travelmantics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class Splash extends AppCompatActivity {
    TextView logo;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        ImageView imageView = findViewById(R.id.ImageView);

        GlideApp.with(this)
                .load(R.drawable.logo_one)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .circleCrop()
                .into(imageView);


        final Thread timmer = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(new Intent(Splash.this,Login.class));
                }
            }
        };
        timmer.start();
    }


    //when splash activity pauses
    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

}
