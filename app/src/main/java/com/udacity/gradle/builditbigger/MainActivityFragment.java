package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.udacity.Tell_A_Joke;
import com.udacity.displayjoke.DisplayJokeActivity;
import com.udacity.gradle.builditbigger.backend.myApi.MyApi;

import java.io.IOException;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    AdView mAdView;
    InterstitialAd mInterstitialAd;
    Button tellJoke;
    ProgressBar progressBar;
    TextView message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_activity, container, false);
        mAdView = (AdView) root.findViewById(R.id.adView);
        tellJoke = (Button) root.findViewById(R.id.btnTellJoke);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        message = (TextView) root.findViewById(R.id.instructions_text_view);
        tellJoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tellJoke();
            }
        });

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("552F9665C709A142AFC6998216C347DF")
                .build();

        mAdView.loadAd(adRequest);
        return root;
    }


    public void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        tellJoke.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        message.setText(show ? "Loading ..." : getString(R.string.instructions));
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    public void tellJoke() {

        mInterstitialAd = new InterstitialAd(getActivity());

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen_unit_id));

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("552F9665C709A142AFC6998216C347DF")
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }

            @Override
            public void onAdClosed() {
                showProgressBar(true);
                Toast.makeText(getContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
                Tell_A_Joke retreiveJoke = new Tell_A_Joke();
                new EndpointsAsyncTask().execute(new Pair<Context, String>(getActivity(), retreiveJoke.getJoke()));
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(getContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                Toast.makeText(getContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                Toast.makeText(getContext(), "Ad is opened!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    class EndpointsAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {
        private MyApi myApiService = null;
        private Context context;

        @Override
        protected String doInBackground(Pair<Context, String>... params) {
            if (myApiService == null) {  // Only do this once
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("https://udacitycloudbackend.appspot.com/_ah/api/");
                        /*.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });*/
                // end options for devappserver

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
            sendJoke(result);
        }
    }

    public void sendJoke(String joke) {

        showProgressBar(false);
        Intent intentForLibrary = new Intent(getActivity(), DisplayJokeActivity.class);
        intentForLibrary.putExtra("JavaLibraryJoke", joke);
        startActivity(intentForLibrary);
    }

}
