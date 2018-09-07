package com.example.victor.applolli.Matematica;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;
import com.example.victor.applolli.Letras.Resultado;
import com.example.victor.applolli.R;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static android.content.ContentValues.TAG;


public class JogoMat extends Activity implements TextToSpeech.OnInitListener {


    private static final int REQUEST_CODE = 1234;

    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    public int i4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }




    private void speakWords(String speech) {

        final VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robomat;
        VideoRobo.setVideoURI(Uri.parse(path));
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


        myTTS.speak("Quanto que é: " + speech, TextToSpeech.QUEUE_FLUSH, null);
        isTTSSpeaking();

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


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                ReconhecimentoDeVoz();
            }
        }, 4000);


    }

    public void ReconhecimentoDeVoz()

    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        // String defaultLanguage = Locale.getDefault().toString();
        // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //  intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        // intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        // intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        // intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        startActivityForResult(intent, REQUEST_CODE);

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

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition
            // engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            System.out.println("Resultados :"+matches);

            String text="Errou";
            for (String result : matches)
            {
                if (result.equals(String.valueOf(i4))) {
                    text = "Acertou";
                }

            }
          /*  if (text.equals("Acertou"))
            {
                myTTS.speak("Acertou", TextToSpeech.QUEUE_FLUSH, null);
                //speakWords2();
            }
            if (text.equals("Errou"))
            {
                myTTS.speak("Errou", TextToSpeech.QUEUE_FLUSH, null);
                //speakWords3();
            }
            else
            {
                //  Esperar();
            }*/

            Intent intent = new Intent(getApplicationContext(),Resultado.class);
            intent.putExtra("word",text);
            intent.putExtra("jogo","numeros");
            startActivityForResult(intent,1);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)

    public void FuncaoRandom() {
        Random r = new Random();
        int i1 = r.nextInt(10 - 1 + 1) + 1;
        int i2 = r.nextInt(10 - 1 + 1) + 1;
        int i3 = r.nextInt(2 - 1 + 1) + 1;


        if (i1<i2)
        {
            int iref = i1;
            i1 = i2;
            i2 = iref;
        }
        String palavra = null;
        if (i3 ==1)
        {
            i4 = i1+i2;
            palavra = String.valueOf(i1) + " mais " + String.valueOf(i2);
        }
        if (i3 ==2)
        {
            i4 = i1-i2;
            palavra = String.valueOf(i1) + " menos " + String.valueOf(i2);
        }


        speakWords(palavra);
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

