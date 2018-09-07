package com.example.victor.applolli;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.Locale;
import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener,MediaPlayer.OnPreparedListener {

    private static final int REQUEST_CODE = 1234;
    private static final int REQUEST_CODE2 = 1;
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    public int Opcao;
    String Op_servidor = "servidor_web";


    @SuppressWarnings("deprecation")


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_inicial);

        /*Intent intent = new Intent(getApplicationContext(),TerminalActivity.class);
        intent.putExtra("server",Op_servidor);
        startActivityForResult(intent,1);*/


        Intent intent = new Intent(getApplicationContext(),ServidorAndroid.class);
        startActivity(intent);


        /*Intent intent = new Intent(getApplicationContext(), Jogo.class);
        startActivity(intent);*/



        Button btnComecar = findViewById(R.id.btnComecar);
        Button btnLista = findViewById(R.id.btnLista);

        btnComecar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setContentView(R.layout.activity_main);
                Intent checkTTSIntent = new Intent();
                checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


            }
        });

        btnLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setContentView(R.layout.lista_de_jogos);

            }
        });


    }

    public void ReiniciarAvticity(View v) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
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


    private void speakWords0() {

        Opcao =0;
        myTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
        isTTSSpeaking(Opcao);

    }

    private void speakWords() {

        Opcao =1;
        myTTS.speak("Olá amiguinho, meu nome é Lolli. Qual o seu nome?", TextToSpeech.QUEUE_FLUSH, null);
        final VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo;
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
        isTTSSpeaking(Opcao);

    }
    private void speakWords2(final String NomePessoa) {

        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo2;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {
                Opcao =2;
                myTTS.speak("Oi " +NomePessoa , TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);
            }
        }, 250);

    }
    private void speakWords3() {

        Opcao =3;
        myTTS.speak("Voce quer brincar de aprender comigo?", TextToSpeech.QUEUE_FLUSH, null);
        isTTSSpeaking(Opcao);
    }
    private void speakWords4() {

        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo3;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {
                Opcao =4;
                myTTS.speak("Que legal. Tenho certeza que vamos nos divertir bastante", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);

            }
        }, 250);
    }
    private void speakWords5() {

        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo_nao;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                Opcao =5;
                myTTS.speak("Tudo bem. Quem sabe uma outra hora", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);
            }
        }, 100);

    }

    public void isTTSSpeaking(final int opcao) {

        final Handler h = new Handler();

        Runnable r = new Runnable() {

            public void run() {

                if (!myTTS.isSpeaking()) {
                    onTTSSpeechFinished(opcao);
                } else {
                    h.postDelayed(this, 100);
                }
            }
        };

        h.postDelayed(r, 1000);
    }

    public void onTTSSpeechFinished(int opcao) {
        System.out.println("acabou de falar");
        System.out.println("Shutdown : " + myTTS);

        if(opcao ==0)
        {
            speakWords();
        }
        if(opcao ==1)
        {
            ReconhecimentoDeVoz();
        }
        if(opcao ==2)
        {
            speakWords3();
        }
        if(opcao ==3)
        {
            ReconhecimentoDeVoz2();
        }
        if(opcao ==4)
        {
            Intent intent = new Intent(getApplicationContext(), Jogos.class);
            startActivity(intent);
        }
        if(opcao ==5)
        {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }


    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
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

            String NomePessoa =  matches.get(0);
            speakWords2(NomePessoa);

        }

        if (requestCode == REQUEST_CODE2 && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition
            // engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            System.out.println("Resultados :"+matches);

            for (String result : matches) {
                if (result.equals("sim") || result.equals("Sim")|| result.equals("Quero")|| result.equals("quero"))
                {
                    speakWords4();
                }
                if (result.equals("não") || result.equals("Não")|| result.equals("nao")|| result.equals("Nao"))
                {
                    speakWords5();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
            speakWords0();
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
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
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        startActivityForResult(intent, REQUEST_CODE);

    }
    public void ReconhecimentoDeVoz2()

    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        // String defaultLanguage = Locale.getDefault().toString();
        // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //  intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        // intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        // intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        startActivityForResult(intent,REQUEST_CODE2);

    }


}
