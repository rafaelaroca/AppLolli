package com.example.victor.applolli.Letras;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.victor.applolli.R;

import java.util.Locale;
import java.util.Random;


import static android.content.ContentValues.TAG;

public class Jogo extends Activity implements OnInitListener,MediaPlayer.OnPreparedListener  {



    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;

    @SuppressWarnings("deprecation")


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogo);


        View placeholder = (View) findViewById(R.id.placeholder);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {
                Intent checkTTSIntent = new Intent();
                checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            }
        }, 1000);




    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            View placeholder = (View) findViewById(R.id.placeholder);
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onInfo, what = " + what);
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // video started; hide the placeholder.
                    placeholder.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });
    }


    private void speakWords(final String speech) {

        final VideoView VideoRobo = (VideoView)findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.roboletra;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.setBackgroundColor(Color.WHITE);
        VideoRobo.start();
        VideoRobo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


            @Override
            public void onPrepared(MediaPlayer mp) {
                VideoRobo.setBackgroundColor(Color.TRANSPARENT);

                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    View placeholder = (View) findViewById(R.id.placeholder);

                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {

                        Log.d(TAG, "onInfo, what = " + what);
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            // video started; hide the placeholder.
                            placeholder.setVisibility(View.GONE);
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
        myTTS.speak("Mostre a " + speech, TextToSpeech.QUEUE_FLUSH, null);
        isTTSSpeaking();


       /* final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {


            }
        }, 250);
    */
    }

    public void isTTSSpeaking(){

        final Handler h =new Handler();

        Runnable r = new Runnable() {

            public void run() {

                if (!myTTS.isSpeaking()) {
                    onTTSSpeechFinished();
                }
                else
                {
                    h.postDelayed(this, 1000);
                }
            }
        };

        h.postDelayed(r, 1000);
    }

    public void onTTSSpeechFinished()
    {
        Intent intent = new Intent(getApplicationContext(), OcrCaptureActivity.class);
        startActivity(intent);
        finish();
    }




    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(this, this);
            } else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)

    public void FuncaoRandom() {
        Random r = new Random();
        int i1 = r.nextInt(26 - 1 + 1) + 1;
        final String letra = getCharForNumber(i1);
        String palavra = "Letra " + letra;


        //  set
        ((SalvaPalavra) this.getApplication()).setSomeVariable(palavra);


        speakWords(palavra);
    }

    private String getCharForNumber(int i) {
        return i > 0 && i < 27 ? String.valueOf((char) (i + 64)) : null;

    }

    @Override
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
            FuncaoRandom();
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }


}
