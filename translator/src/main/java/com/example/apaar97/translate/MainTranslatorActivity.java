package com.example.apaar97.translate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainTranslatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_translator);
        LinearLayout layout = (LinearLayout) findViewById(R.id.home_container);

        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setFillAfter(true);
        animation.setDuration(1200);
        layout.startAnimation(animation);

        Button bConversation = (Button) findViewById(R.id.start_new_conversation);
        Button bTranslation = (Button) findViewById(R.id.start_new_translation);
        Button bAbout = (Button) findViewById(R.id.about);
        bConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainTranslatorActivity.this, ConversationActivity.class);
                startActivity(intent);
            }
        });
        bTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainTranslatorActivity.this, TranslationActivity.class);
                startActivity(intent);
            }
        });
        bAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainTranslatorActivity.this, AboutTranslatorActivity.class);
                startActivity(intent);
            }
        });
    }
}
