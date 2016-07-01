package com.udacity.displayjoke;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DisplayJokeActivity extends AppCompatActivity {

    TextView display_joke;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.display_joke);
        display_joke = (TextView) findViewById(R.id.display_joke);

        Intent receivedIntent = getIntent();
        String jokeRecieved = receivedIntent.getStringExtra("JavaLibraryJoke");
        display_joke.setText(jokeRecieved);

    }
}
