package com.example.apaar97.translate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutTranslatorActivity extends AppCompatActivity {

    private static String YANDEX_URL = "https://translate.yandex.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_translator);
        ImageView imageView = (ImageView) findViewById(R.id.about_yandex);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webUri = Uri.parse(YANDEX_URL);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        });
    }
}
