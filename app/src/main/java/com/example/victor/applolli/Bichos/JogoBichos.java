package com.example.victor.applolli.Bichos;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.victor.applolli.Letras.Resultado;
import com.example.victor.applolli.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

//import app.akexorcist.bluetoothspp.Letras.Resultado;
//import app.akexorcist.bluetoothspp.R;

public class JogoBichos extends Activity implements TextToSpeech.OnInitListener {

    private ImageView mImageView;
    public String animal;

    private static final int REQUEST_CODE = 1234;
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    public int i4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogo_bichos);

        mImageView = (ImageView)findViewById(R.id.imageViewBichos);


        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        //  FuncaoRandom();

    }

    private void speakWords(int figura) {

        if(figura == 1) {
            animal = "cobra";
            mImageView.setImageResource(R.drawable.cobra);
        }
        if(figura == 2) {
            animal = "elefante";
            mImageView.setImageResource(R.drawable.elefante);
        }
        if(figura == 3) {
            animal = "cavalo";
            mImageView.setImageResource(R.drawable.cavalo);
        }
        if(figura == 4) {
            animal = "cachorro";
            mImageView.setImageResource(R.drawable.cachorro);
        }
        if(figura == 5) {
            animal = "jacaré";
            mImageView.setImageResource(R.drawable.jacare);
        }
        if(figura == 6) {
            animal = "gato";
            mImageView.setImageResource(R.drawable.gato);
        }
        if(figura == 7) {
            animal = "leão";
            mImageView.setImageResource(R.drawable.leao);
        }
        if(figura == 8) {
            animal = "macaco";
            mImageView.setImageResource(R.drawable.macaco);
        }
        if(figura == 9) {
            animal = "peixe";
            mImageView.setImageResource(R.drawable.peixe);
        }
        if(figura == 10) {
            animal = "girafa";
            mImageView.setImageResource(R.drawable.girafa);
        }

        myTTS.speak("Qual animal é esse?", TextToSpeech.QUEUE_FLUSH, null);
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
        }, 2000);

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
            System.out.println("O animal é : "+animal);

            String text="Errou";
            for (String result : matches)
            {
                System.out.println("RESULTADO : "+result);

                if (result.equals(animal)){
                    text = "Acertou";
                }

            }

            Intent intent = new Intent(getApplicationContext(),Resultado.class);
            intent.putExtra("word",text);
            intent.putExtra("jogo","bichos");
            startActivityForResult(intent,1);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)

    public void FuncaoRandom() {
        Random r = new Random();
        int i3 = r.nextInt(10 - 1 + 1) + 1;

        speakWords(i3);
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

