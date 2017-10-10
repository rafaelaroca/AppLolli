package com.example.victor.applolli;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.example.library.BluetoothSPP;
import com.example.library.BluetoothState;
import com.example.library.DeviceList;
import com.example.library.BluetoothSPP.BluetoothConnectionListener;
import com.example.library.BluetoothSPP.OnDataReceivedListener;


import java.util.ArrayList;
import java.util.Locale;

public class TerminalActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    static BluetoothSPP bt;

    TextView textStatus, textRead, txtObjeto;
    EditText etMessage;
    private int MY_DATA_CHECK_CODE = 0;
    private static final int REQUEST_CODE = 1234;
    public int Opcao;
    public String ObjetoDesejado;
    private TextToSpeech myTTS;
    Uri imageUri;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    boolean Serv = false;

    Menu menu;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        Log.i("Check", "onCreate");

        textRead = (TextView) findViewById(R.id.textRead);
        textStatus = (TextView) findViewById(R.id.textStatus);
        etMessage = (EditText) findViewById(R.id.etMessage);
        txtObjeto = (TextView) findViewById(R.id.txtObjeto);

        // Bundle b = getIntent().getExtras();
        //  long value = b.getLong("startTime", 0);

        System.out.println("Voltou para a terminal");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String Opcao_Servidor = extras.getString("server");

            if (Opcao_Servidor.equals("servidor_web")) {

                Serv = true;
            }
        }

        bt = new BluetoothSPP(this);




        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                textRead.append(message + "\n");
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                textStatus.setText("Status : Not connect");
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                textStatus.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {

                textStatus.setText("Status : Connected to " + name);
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);



                if(Serv==true)
                {
                    Intent intent = new Intent(getApplicationContext(),ServidorAndroid.class);
                    startActivityForResult(intent,1);
                }

                else {
                    Intent checkTTSIntent = new Intent();
                    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                    startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
                }



            }
        });

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if (id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if (id == R.id.menu_disconnect) {
            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
        System.out.println("Veio no onStart");
      /*  String ren = getIntent().getStringExtra("methodName");
            if (ren.equals("myMethod"))
            {
                Reiniciar();
            }*/
        //The key argument here must match that used in the other activity
    }
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null)
            setIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String value = extras.getString("methodName");
            if(value.equals("myMethod"));
            {
                Reiniciar();
            }
            //The key argument here must match that used in the other activity
        }
    }
    /* @Override
     public void onResume(){
         System.out.println("Veio no onResumo");
         super.onResume();
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         if (extras != null) {
             String value = extras.getString("methodName");
             if(value.equals("myMethod"));
             {
                 Reiniciar();
             }
             //The key argument here must match that used in the other activity
         }

     }*/
    public void Reiniciar() {

        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        intent.putExtra("word",ObjetoDesejado);
        startActivityForResult(intent,1);
    }

    public void setup() {
    }

    public static void MoverFrente() {

        bt.send("f",true);
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);

        // bt.send("b", true);


        // Intent intent = new Intent(getApplicationContext(),CameraActivity2.class);
        // startActivityForResult(intent,1);
    }
    /*public static void GirarEsquerda() {

        bt.send("e",true);
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);
       /* Intent intent = new Intent(getApplicationContext(),CameraActivity2.class);
        startActivityForResult(intent,1);

    }*/
   /* public static void GirarDireita() {


        bt.send("d",true);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bt.send("0",true);

    }*/

    public static void GirarEsquerdaFrente() {

        bt.send("e",true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);
        bt.send("f",true);
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);

    }
    public static void GirarDireitaFrente() {

        bt.send("d",true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);
        bt.send("f",true);
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);

    }


    public void FotoFrente()
    {
        bt.send("e",true);
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        intent.putExtra("word",ObjetoDesejado);
        startActivityForResult(intent,1);
    }

    public void FotoEsquerda()
    {
        bt.send("d",true);
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        intent.putExtra("word",ObjetoDesejado);
        startActivityForResult(intent,1);
    }
    public void FotoDireita()
    {
        bt.send("e",true);
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bt.send("0",true);
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        intent.putExtra("word",ObjetoDesejado);
        startActivityForResult(intent,1);
    }

    public static void FuncaoAndar(int tempo, String direcao)
    {

        if (direcao.equals("frente"))
        {
            bt.send("f", true);
            System.out.println("Veio no funçao andar");
            try {
                Thread.sleep(tempo * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bt.send("0", true);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (direcao.equals("direita"))
        {
            bt.send("d", true);
            System.out.println("Veio no funçao andar");
            try {
                Thread.sleep(tempo * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bt.send("0", true);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        if (direcao.equals("tras"))
        {
            bt.send("r", true);
            System.out.println("Veio no funçao andar");
            try {
                Thread.sleep(tempo * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bt.send("0", true);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        if (direcao.equals("esquerda"))
        {
            bt.send("e", true);
            System.out.println("Veio no funçao andar");
            try {
                Thread.sleep(tempo * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bt.send("0", true);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {


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

        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        }


        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition
            // engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            System.out.println("Resultados :"+matches);

            ObjetoDesejado =  matches.get(0);
            System.out.println("O objeto desejado é: " + ObjetoDesejado);
            txtObjeto.setText("Objeto desejado : " + ObjetoDesejado);

            speakWords2();

        }


        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){

                String ResultadoObjeto=data.getStringExtra("result");
                // String Movimentar=data.getStringExtra("movimentacao");
                System.out.println("Veio no result");


              /*  if(ResultadoObjeto.equals("ObjetoIdentificado"))
                {
                    if (Movimentar.equals("1"));
                    {
                        setup4();
                    }
                    if (Movimentar.equals("2"));
                    {
                        setup5();
                    }
                    if (Movimentar.equals("3"));
                    {
                        setup6();
                    }
                    System.out.println("Entrou no OBJETOIDENTIFICADO");

                }*/
                if(ResultadoObjeto.equals("Reiniciar"))
                {
                    Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                    startActivityForResult(intent,1);
                }
                if(ResultadoObjeto.equals("ObjetoIdentificado"))
                {
                    MoverFrente();
                }
                if(ResultadoObjeto.equals("ObjetoNaoIdentificado"))
                {
                    System.out.println("Entrou no OBJETO NAO IDENTIFICADO");
                    MoverFrente();
                }

                if(ResultadoObjeto.equals("FotoFrente"))
                {
                    FotoFrente();
                }
                if(ResultadoObjeto.equals("FotoEsquerda"))
                {
                    FotoEsquerda();
                }
                if(ResultadoObjeto.equals("FotoDireita"))
                {
                    FotoDireita();
                }

               /* else
                {
                    System.out.println("Entrou EM NENHUM");
                    Intent intent = new Intent(getApplicationContext(),CameraActivity2.class);
                    startActivityForResult(intent,1);
                }*/
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

        else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

    private void speakWords0() {

        Opcao =0;
        myTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
        isTTSSpeaking(Opcao);

    }
    private void speakWords() {

        Opcao =1;
        myTTS.speak("O que deseja que eu encontre?", TextToSpeech.QUEUE_FLUSH, null);
        isTTSSpeaking(Opcao);

    }
    private void speakWords2() {

        Opcao =2;
        myTTS.speak("Ok. Vou procurar o objeto desejado" , TextToSpeech.QUEUE_FLUSH, null);
        isTTSSpeaking(Opcao);

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
            Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
            intent.putExtra("word",ObjetoDesejado);
            startActivityForResult(intent,1);
        }

    }

    public void ReconhecimentoDeVoz()

    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        // intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        // String defaultLanguage = Locale.getDefault().toString();
        // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //  intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        // intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        // intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        startActivityForResult(intent, REQUEST_CODE);

    }
}