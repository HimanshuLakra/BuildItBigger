package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.test.AndroidTestCase;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.udacity.Tell_A_Joke;
import com.udacity.gradle.builditbigger.backend.myApi.MyApi;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class JokeTest extends AndroidTestCase {

    final CountDownLatch signal = new CountDownLatch(1);

    private Context context;
    private String resultJoke;

    AsyncTask<Pair<Context, String>, Void, String> asyncTask = new AsyncTask<Pair<Context, String>, Void, String>() {

        private MyApi myApiService = null;

        @Override
        protected String doInBackground(Pair<Context, String>... params) {
            if (myApiService == null) {  // Only do this once
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://udacitycloudbackend.appspot.com/_ah/api/");

                myApiService = builder.build();
            }

            context = params[0].first;
            String joke = params[0].second;

            try {
                return myApiService.supplyJokes(joke).execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }


        @Override
        protected void onPostExecute(String result) {
            resultJoke = result;
            signal.countDown();// notify the count down latch

        }
    };

    public void jokeValidator(){

        Tell_A_Joke joke = new Tell_A_Joke();

        asyncTask.execute(new Pair<>(context,joke.getJoke()));

        try {
            signal.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull(resultJoke);
    }
}
