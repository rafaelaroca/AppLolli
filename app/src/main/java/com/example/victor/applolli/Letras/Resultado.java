package com.example.victor.applolli.Letras;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.victor.applolli.Bichos.JogoBichos;
import com.example.victor.applolli.Jogos;
import com.example.victor.applolli.Matematica.JogoMat;
import com.example.victor.applolli.R;
import java.util.ArrayList;
import java.util.Locale;



public class Resultado extends Activity implements TextToSpeech.OnInitListener {

    private ImageView mImageView;
    private TextToSpeech myTTS;

    private int MY_DATA_CHECK_CODE = 0;
    private static final int REQUEST_CODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        mImageView = findViewById(R.id.imageView);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


    }

    private void FuncaoResultado()
    {
        final MediaPlayer mpAplausos = MediaPlayer.create(this, R.raw.aplausos);
        final MediaPlayer mpDecepcao = MediaPlayer.create(this, R.raw.decepcao);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String Jogo = extras.getString("jogo");

            if (Jogo != null)
            {
                if(Jogo.equals("letras"))
                {
                    // get
                    String word = ((SalvaPalavra) this.getApplication()).getSomeVariable();
                    System.out.println("Letra gerada: "+word);

                    String palavra = extras.getString("word");

                    if (word.equals(palavra))
                    {
                        mImageView.setImageResource(R.drawable.robo_ganhou);
                        mpAplausos.start();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                myTTS.speak("Parabéns, você acertou! Quer jogar novamente?", TextToSpeech.QUEUE_FLUSH, null);
                                isTTSSpeaking();
                            }
                        }, 6000);


                    }
                    else
                    {
                        mImageView.setImageResource(R.drawable.robo_triste);
                        mpDecepcao.start();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                myTTS.speak("Que pena, você errou! Quer tentar novamente?", TextToSpeech.QUEUE_FLUSH, null);
                                isTTSSpeaking();
                            }
                        }, 5000);

                    }

                }

                if(Jogo.equals("numeros"))
                {
                    String palavra = extras.getString("word");

                    if (palavra != null)
                    {
                        if (palavra.equals("Acertou"))
                        {
                            mImageView.setImageResource(R.drawable.robo_ganhou);
                            mpAplausos.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myTTS.speak("Parabéns, você acertou! Quer jogar novamente?", TextToSpeech.QUEUE_FLUSH, null);
                                    isTTSSpeaking();

                                }
                            }, 6000);


                        }
                        else
                        {
                            mImageView.setImageResource(R.drawable.robo_triste);
                            mpDecepcao.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    myTTS.speak("Que pena, você errou! Quer tentar novamente?", TextToSpeech.QUEUE_FLUSH, null);
                                    isTTSSpeaking();


                                }
                            }, 5000);

                        }
                    }



                }
                if(Jogo.equals("bichos"))
                {
                    String palavra = extras.getString("word");

                    if (palavra != null)
                    {
                        if (palavra.equals("Acertou"))
                        {
                            mImageView.setImageResource(R.drawable.robo_ganhou);
                            mpAplausos.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myTTS.speak("Parabéns, você acertou! Quer jogar novamente?", TextToSpeech.QUEUE_FLUSH, null);
                                    isTTSSpeaking();
                                }
                            }, 5000);


                        }
                        else
                        {
                            mImageView.setImageResource(R.drawable.robo_triste);
                            mpDecepcao.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myTTS.speak("Que pena, você errou! Quer tentar novamente?", TextToSpeech.QUEUE_FLUSH, null);
                                    isTTSSpeaking();
                                }
                            }, 5000);

                        }

                    }
                }
            }
        }
    }

    private void JogarNovamente()
    {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String AtualJogo = extras.getString("jogo");
            if (AtualJogo != null)
            {
                if(AtualJogo.equals("letras"))
                {
                    Intent intent = new Intent(getApplicationContext(),Jogo.class);
                    startActivity(intent);
                }
                if(AtualJogo.equals("numeros"))
                {
                    Intent intent = new Intent(getApplicationContext(),JogoMat.class);
                    startActivity(intent);
                }
                if(AtualJogo.equals("bichos"))
                {
                    Intent intent = new Intent(getApplicationContext(),JogoBichos.class);
                    startActivity(intent);
                }
            }
        }
    }
    private void VoltarJogos()
    {
        Intent intent = new Intent(getApplicationContext(),Jogos.class);
        startActivity(intent);
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
        ReconhecimentoDeVoz();
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

            for (String result : matches) {
                if (result.equals("sim") || result.equals("Sim")|| result.equals("Quero")|| result.equals("quero"))
                {
                    System.out.println("Veio no sim");
                    JogarNovamente();
                }
                if (result.equals("não") || result.equals("Não")|| result.equals("nao")|| result.equals("Nao"))
                {
                    VoltarJogos();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
            FuncaoResultado();
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
}

