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
import android.widget.Toast;
import android.widget.VideoView;
import com.example.victor.applolli.Bichos.JogoBichos;
import com.example.victor.applolli.Letras.Jogo;
import com.example.victor.applolli.Matematica.JogoMat;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class Jogos extends Activity implements  TextToSpeech.OnInitListener {

    private static final int REQUEST_CODE = 1234;
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    public int Opcao;
    String Op_servidor = "servidor_web";


    @SuppressWarnings("deprecation")


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //   Button btnTerminal = (Button) findViewById(R.id.btnTerminal);
        //   btnTerminal.setOnClickListener(this);

        //  Button btnLetras = (Button) findViewById(R.id.btnLetras);
        // btnLetras.setOnClickListener(this);

        // ListaDeJogos = (ListView) findViewById(R.id.listView);


        // ListaDeJogos.setAdapter(new ArrayAdapter<String>(this, R.layout.mylist, mobileArray));


        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


    }



    private void speakWords() {

        final VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo4;
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

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {

                Opcao =1;
                myTTS.speak("Vamos brincar de que?", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);

            }
        }, 250);




    }
    private void speakWords2() {


        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo5;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {

                Opcao =2;
                myTTS.speak("Que divertido, vamos brincar de encontrar objetos", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);

            }
        }, 250);



    }
    private void speakWords3() {


        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo5;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {

                Opcao =3;
                myTTS.speak("Que divertido, vamos jogar o jogo das letras", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);

            }
        }, 250);



    }
    private void speakWords4() {


        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo5;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {

                Opcao =4;
                myTTS.speak("Que divertido, vamos jogar o jogo dos números", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);

            }
        }, 250);


    }
    private void speakWords5() {

        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo5;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {

                Opcao =5;
                myTTS.speak("Que divertido, vamos jogar o jogo dos bichos", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);

            }
        }, 250);


    }

    private void speakWords6() {

        VideoView VideoRobo = findViewById(R.id.videoViewRelative);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.robo5;
        VideoRobo.setVideoURI(Uri.parse(path));
        VideoRobo.start();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {

                Opcao =6;
                myTTS.speak("Que divertido, vamos para o servidor web", TextToSpeech.QUEUE_FLUSH, null);
                isTTSSpeaking(Opcao);

            }
        }, 250);


    }


    public void isTTSSpeaking(final int opcao) {

        final Handler h = new Handler();

        Runnable r = new Runnable() {

            public void run() {

                if (!myTTS.isSpeaking()) {
                    onTTSSpeechFinished(opcao);
                } else {
                    h.postDelayed(this, 1000);
                }
            }
        };

        h.postDelayed(r, 1000);
    }

    public void onTTSSpeechFinished(int opcao) {
        System.out.println("acabou de falar");
        System.out.println("Shutdown : " + myTTS);

        if(opcao ==1)
        {
            ReconhecimentoDeVoz();
        }
        if(opcao ==2)
        {
            Intent intent = new Intent(getApplicationContext(), TerminalActivity.class);
            startActivity(intent);
        }
        if(opcao ==3)
        {
            Intent intent = new Intent(getApplicationContext(), Jogo.class);
            startActivity(intent);
        }
        if(opcao ==4)
        {
            Intent intent = new Intent(getApplicationContext(), JogoMat.class);
            startActivity(intent);
        }
        if(opcao ==5)
        {
            Intent intent = new Intent(getApplicationContext(), JogoBichos.class);
            startActivity(intent);
        }
        if(opcao ==6)
        {
            Intent intent = new Intent(getApplicationContext(),TerminalActivity.class);
            intent.putExtra("server",Op_servidor);
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

            String text="";
            for (String result : matches)
            {
                if (result.equals("encontrar objeto") || result.equals("encontrar objetos")) {
                    text = "Encontrar objeto";
                }
                if (result.equals("jogo das letras") ||(result.equals("jogo das Letras"))||(result.equals("jogos das Letras")||
                        (result.equals("jogo de letras"))||(result.equals("jogo de letra"))||(result.equals("jogo da Letra"))
                        ||(result.equals("jogo da letras"))||(result.equals("jogo letras")))) {
                    text = "Jogo das letras";
                }
                if (result.equals("jogo dos numeros")||(result.equals("jogo dos números"))||(result.equals("jogos dos números"))||(result.equals("jogos dos número"))
                        ||(result.equals("jogo do numero"))||(result.equals("jogo do número"))
                        ||(result.equals("jogo de números"))||(result.equals("jogo de número"))||(result.equals("jogo de numeros"))
                        ||(result.equals("jogo de numero")))
                {
                    text = "Jogo dos numeros";
                }
                if (result.equals("jogo dos bichos") || result.equals("jogo dos bicho")|| result.equals("jogo do bicho")||
                        result.equals("jogo de bicho")|| result.equals("jogo de bichos")|| result.equals("jogo bicho")||
                        result.equals("jogo bichos")|| result.equals("bicho")|| result.equals("bichos"))
                {
                    text = "Jogo dos bichos";
                }
                if (result.equals("servidor"))
                {
                    text = "Servidor Web";
                }

            }
            if (text.equals("Encontrar objeto"))
            {
                speakWords2();
            }
            if (text.equals("Jogo das letras"))
            {
                speakWords3();
            }
            if (text.equals("Jogo dos numeros"))
            {
                speakWords4();
            }
            if (text.equals("Jogo dos bichos"))
            {
                speakWords5();
            }

            if (text.equals("Servidor Web"))
            {
                speakWords6();
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
            speakWords();
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    public void ReconhecimentoDeVoz()

    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, REQUEST_CODE);

    }

}