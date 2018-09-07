package com.example.victor.applolli;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.library.BluetoothSPP;
import com.example.library.BluetoothState;
import com.example.library.DeviceList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import fi.iki.elonen.NanoHTTPD;

import static android.os.Environment.getExternalStorageDirectory;

public class ServidorAndroid extends AppCompatActivity implements TextToSpeech.OnInitListener,TextToSpeech.OnUtteranceCompletedListener{

    Context context = this;

    private SpeechRecognizer sr;
    private WebServer server;
    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;
    private static final int REQUEST_CODE = 1234;
    private static final String TAG = "MyStt3Activity";

    String texto_programa = "";
    String texto_check = "";
    String param_abrir;
    String texto_abrir;
    String CodigoTexto;
    String Nome_SalvarComo;
    String sFileName;
    String BotaoExecutar;
    String Salvar_Como;
    String Salvar;
    String msg1;
    String msg2;
    String msg;
    String falar_codigo = "";
    String nome_salvar = "";
    String correct_result = "";
    ArrayList<String> result_recog;
    StringBuilder under_sol = new StringBuilder();
    StringBuilder palavra_sol = new StringBuilder();
    StringBuilder texto_check_SB;
    String EndIP;

    int var_loop = 0;
    int enter_func = 0;
    int ver_result = 0;
    int QteArquivos = 0;
    int jogo_forma = 0;
    int num_jogo = 0;
    int jogo_soletrar = 0;
    int tam_palavra = 0;

    boolean ent_senao = false;
    boolean conn_serv = false;
    boolean conn_bluetooth = false;

    BluetoothSPP bt;

    TextView EnderecoIp,textStatus,textStatusServidor, textRead, txtObjeto;
    EditText etMessage;
    Menu menu;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servidor_android);

        EnderecoIp = findViewById(R.id.TxtIp);

        ImageView imageLolli;

        imageLolli = findViewById(R.id.ImageViewLolli);
        imageLolli.setImageResource(android.R.color.transparent);

        AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int value_sound = 9;//range(0-15)
        mgr.setStreamVolume(AudioManager.STREAM_MUSIC, value_sound, 0);

        bt = new BluetoothSPP(this);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        /*Intent intent = new Intent(getApplicationContext(),TerminalActivity.class);
                        startActivity(intent);*/
                        IniciarBluetooth();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        IniciarServidor();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Voce deseja conectar o sistema ao robô?").setNegativeButton("Não", dialogClickListener).setPositiveButton("Sim", dialogClickListener).show();

    }

    public void IniciarBluetooth()
    {

        textStatusServidor = findViewById(R.id.textStatusServidor);

        textStatus = findViewById(R.id.textStatus);
        etMessage = findViewById(R.id.etMessage);
        txtObjeto = findViewById(R.id.txtObjeto);


        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {

            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);


        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                textRead.append(message + "\n");
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            String strConnect = "";
            public void onDeviceDisconnected() {
                strConnect = "Status : Sem conexão";
                textStatusServidor.setText(strConnect);
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
                conn_bluetooth = false;
            }

            public void onDeviceConnectionFailed() {
                strConnect = "Status : Conexão falhou";
                textStatusServidor.setText(strConnect);
                conn_bluetooth = false;
            }

            public void onDeviceConnected(String name, String address) {
                strConnect = "Status : Conectado ao dispositivo " + name;
                textStatusServidor.setText(strConnect);
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                conn_bluetooth = true;

                if (!conn_serv)
                {
                    IniciarServidor();
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
        /*if (id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);

			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

        No menu_connection:

        <item
        android:id="@+id/menu_android_connect"
        android:orderInCategory="100"
        android:title="Conectar com dispositivos Android"  />

        }*/

        if (id == R.id.menu_device_connect) {
            System.out.println("VEIO NO DEVICE CONNECT");
            IniciarBluetooth();



            /*bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);

			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);*/
        }

        else if (id == R.id.menu_disconnect) {
            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
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
            }
        //The key argument here must match that used in the other activity*/
    }

    public void setup() {
    }

    public void IniciarServidor()
    {



        server = new WebServer();
        System.out.println("Estado do local externo " + Environment.getExternalStorageState());

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        ImageView imageLolli;

        imageLolli = findViewById(R.id.ImageViewLolli);
        imageLolli.setImageResource(R.drawable.robo_feliz);


        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

            try {

                server.start();
                WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiMan != null) {
                    WifiInfo wifiInf = wifiMan.getConnectionInfo();
                    int ipAddress = wifiInf.getIpAddress();
                    String ip = String.format(Locale.getDefault(), "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                    System.out.println(ip);

                    EndIP = "Endereço de IP: " + ip + ":8080";
                    EnderecoIp.setText(EndIP);
                    conn_serv = true;

                }

            } catch (IOException ioe) {
                Log.w("Httpd", "The server could not start.");
            }
            Log.w("Httpd", "Web server initialized.");




    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    // DON'T FORGET to stop the server
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

    private class WebServer extends NanoHTTPD {

        private WebServer() {
            super(8080);
        }

        private void generateNoteOnSD(String sBody) {

            sFileName =   Nome_SalvarComo + ".txt";

            try {

                File root = new File(getExternalStorageDirectory(), "ArquivosProjeto");
                if (!root.exists()) {
                    boolean wasSuccessful = root.mkdirs();
                    if (wasSuccessful)
                    {
                        Log.d(TAG,"Direitorio criado");
                    }
                }
                File gpxfile = new File(root, sFileName);
                FileWriter writer = new FileWriter(gpxfile);
                writer.append(sBody);
                writer.flush();
                writer.close();

                refreshGallery(gpxfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void refreshGallery(File file) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(file));
            sendBroadcast(mediaScanIntent);
        }

        //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public Response serve(IHTTPSession session) {

            QteArquivos = 0;
            File root = new File(getExternalStorageDirectory(), "ArquivosProjeto");
            if (!root.exists()) {
                boolean wasSuccessful = root.mkdirs();
                if (wasSuccessful)
                {
                    Log.d(TAG,"Direitorio criado");
                }
            }

            File DiretorioArquivo = new File(getExternalStorageDirectory() + "/ArquivosProjeto");
            Map<String, String> files = new HashMap<>();
            Method method = session.getMethod();

            param_abrir = session.getParms().toString();
            boolean b = param_abrir.contains("abrir=");

            if (b) {
                texto_abrir = session.getParms().get("abrir");
                String[] sep_text = texto_abrir.split("/");
                String nome_arq = sep_text[sep_text.length - 1] + ".txt";
                nome_salvar = sep_text[sep_text.length - 1];

                File file = new File(DiretorioArquivo, nome_arq);
                StringBuilder prog_SB = new StringBuilder();



                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        prog_SB.append(line);
                        prog_SB.append('\n');
                    }
                    br.close();
                } catch (IOException e) {
                    //You'll need to add proper error handling here
                }
                texto_programa = prog_SB.toString();

            }

            if (Method.PUT.equals(method) || Method.POST.equals(method)) {

                try {
                    session.parseBody(files);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ResponseException e) {
                    e.printStackTrace();
                }

                BotaoExecutar = session.getParms().get("executar");
                Salvar = session.getParms().get("Salvar");




                if (BotaoExecutar != null) {

                    CodigoTexto = session.getParms().get("TextoCodigo");
                    texto_programa = CodigoTexto;
                    System.out.println("Veio no executar");
                    var_loop = 0;
                    num_jogo = 0;
                    jogo_forma = 0;

                    boolean var_executar;


                    var_executar = CheckCode();
                    System.out.println("VAR EXECUTAR");
                    System.out.println(var_executar);

                    //todo executar programa
                    //ExecutarPrograma();

                    if (var_executar)
                    {
                        ExecutarPrograma();
                    }
                }

                if (Salvar_Como != null) {

                    Nome_SalvarComo = session.getParms().get("nomePrograma");
                    CodigoTexto = session.getParms().get("TextoCodigo");
                    texto_programa = CodigoTexto;

                    if (Nome_SalvarComo.contains(" "))
                    {
                        Nome_SalvarComo = Nome_SalvarComo.replace(" ","_");
                    }



                    if (!Nome_SalvarComo.equals(""))
                    {
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                        {
                            generateNoteOnSD(CodigoTexto);
                        }
                    }

                }

                if (Salvar != null) {

                    Nome_SalvarComo = session.getParms().get("nomePrograma");
                    CodigoTexto = session.getParms().get("TextoCodigo");
                    texto_programa = CodigoTexto;

                    if (Nome_SalvarComo.contains(" "))
                    {
                        Nome_SalvarComo = Nome_SalvarComo.replace(" ","_");
                    }

                    if (!Nome_SalvarComo.equals(""))
                    {
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                        {
                            generateNoteOnSD(CodigoTexto);
                        }
                    }
                }
            }

            msg1 =

                    "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "<meta charset=\"UTF-8\">\n" +
                            "\t<title>JQuery LinedTextArea Demo</title>\n" +
                            "\t<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js\"></script>\n" +
                            "\t<script src=\"jquery-linedtextarea.js\"></script>\n" +
                            "\t<link href=\"jquery-linedtextarea.css\" type=\"text/css\" rel=\"stylesheet\" />\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "\n" +
                            "<style>\n" +
                            "\n" +
                            "\n" +
                            "body{height:100%;\n" +
                            "   width:100%;\n" +
                            "   background-image:url(background_lolli10.jpg);/*your background image*/  \n" +
                            "   background-repeat:no-repeat;/*we want to have one single image not a repeated one*/  \n" +
                            "   background-size:cover;/*this sets the image to fullscreen covering the whole screen*/  \n" +
                            "   /*css hack for ie*/     \n" +
                            "   filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='.image.jpg',sizingMethod='scale');\n" +
                            "   -ms-filter:\"progid:DXImageTransform.Microsoft.AlphaImageLoader(src='image.jpg',sizingMethod='scale')\";\n" +
                            "}\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            ".inline-div {\n" +
                            "    display:inline-block;\n" +
                            "\tmargin-left:30px;\n" +
                            "\n" +
                            "\t\n" +
                            "}\n" +
                            "\n" +
                            ".inline-div2 {\n" +
                            "    display:inline-block;\n" +
                            "\tmargin-left:50px;\n" +
                            "\t\n" +
                            "\tbackground-attachment: local;\n" +
                            "\tbackground-image:\n" +
                            "\tlinear-gradient(to right, white 10px, transparent 10px),\n" +
                            "\tlinear-gradient(to left, white 10px, transparent 10px),\n" +
                            "\trepeating-linear-gradient(white, white 30px, #ccc 30px, #ccc 31.2px, white 31.2px);\n" +
                            "\tline-height: 31.2px;\n" +
                            "\tpadding: 2px 10px;\n" +
                            "}\n" +
                            "\n" +
                            ".inline-div3 {\n" +
                            "    display:inline-block;\n" +
                            "\tmargin-left:0px;\n" +
                            "\n" +
                            "\t\n" +
                            "}\n" +
                            "\n" +
                            "p {\n" +
                            "\n" +
                            "    margin-top: 0px;\n" +
                            "    margin-bottom: 0px;\n" +
                            "    margin-right: 0px;\n" +
                            "    margin-left: 30px;\n" +
                            "}\n" +
                            "\n" +
                            ".p2 {\n" +
                            "\n" +
                            "    margin-top: 0px;\n" +
                            "    margin-bottom: 0px;\n" +
                            "    margin-right: 0px;\n" +
                            "    margin-left: 10px;\n" +
                            "}\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "</style>\n" +
                            "\n" +
                            "<img src=\"lolli4.png\" class = \"p2\">\n" +
                            "\n" +
                            "\n" +
                            "<p>Digite os comandos desejados.</p>\n" +
                            "\n" +
                            "\n" +
                            "<form method=\"post\" id = \"myForm\">\n" +
                            "\n" +
                            "<div style=\"background-color:white\" class=\"inline-div\">\n" +
                            "\n" +
                            "\n" +
                            "\t<textarea class=\"lined\" rows=\"20\" cols=\"70\" name=\"TextoCodigo\">\n" + texto_programa + "</textarea>\n" +
                            "</div>\n" +
                            "\n" +
                            "<div style=\"background-color:white\" class=\"inline-div3\">\n" +
                            " \n" +
                            "\n" +
                            "\t<textarea class=\"lined\" rows=\"20\" cols=\"15\" name=\"TextoCheck\">\n" + texto_check + "</textarea>\n" +
                            "\n" +
                            "</div>\n" +
                            "\n" +
                            "<div style=\"background-color:white; width: 400px; height: 330px; overflow-y: scroll;\" class=\"inline-div2\" contenteditable=\"false\">\n" +
                            "\n" +
                            "<strong>Funções da Lolli</strong>\n" +
                            "\n" +
                            "<br><strong>mover frente(tempo de movimento em segundos):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para o robô se mover para frente por um tempo especificado em segundos.\n" +
                            "<br><strong>Exemplo:</strong> mover frente(2) → O robô se move para frente por 2 segundos.\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>mover tras(tempo de movimento em segundos):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para o robô se mover para trás por um tempo especificado em segundos.\n" +
                            "<br><strong>Exemplo:</strong> mover tras(1.5) → O robô se move para trás por 1,5 segundos.\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>mover esquerda(tempo de movimento em segundos):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para o robô se mover para a esquerda por um tempo especificado em segundos.\n" +
                            "<br><strong>Exemplo:</strong> mover esquerda(3) → O robô se move para esquerda por 3 segundos.\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>mover direita(tempo de movimento em segundos):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para o robô se mover para a direita por um tempo especificado em segundos.\n" +
                            "<br><strong>Exemplo:</strong> mover para frente(10) → O robô se move para direita por 10 segundos.\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>rodar:</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para o robô rodar em torno se si mesmo para fazer comemorações.\n" +
                            "<br><strong>Exemplo:</strong> rodar\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>jogo circulo:</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para ilustrar a imagem de um círculo na tela do smartphone e fazer três perguntas sobre um círculo\n" +
                            "<br><strong>Perguntas:</strong> \n" +
                            "<br> 1. Qual é o nome desta forma?\n" +
                            "<br> 2. O círculo é redondo?\n" +
                            "<br> 3. O círculo tem linhas retas?\n" +
                            "<br><strong>Exemplo:</strong> jogo circulo → É ilustrado um círculo na tela e feito três perguntas\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>jogo triangulo:</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para ilustrar a imagem de um triângulo na tela do smartphone e fazer três perguntas sobre um triângulo\n" +
                            "<br><strong>Perguntas:</strong> \n" +
                            "<br> 1. Qual é o nome desta forma?\n" +
                            "<br> 2. Quantas linhas possui um triângulo?\n" +
                            "<br> 3. O triângulo é redondo?\n" +
                            "<br><strong>Exemplo:</strong> jogo triangulo → É ilustrado um triângulo na tela e feito três perguntas\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>jogo quadrado:</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para ilustrar a imagem de um quadrado na tela do smartphone e fazer três perguntas sobre um quadrado\n" +
                            "<br><strong>Perguntas:</strong> \n" +
                            "<br> 1. Qual é o nome desta forma?\n" +
                            "<br> 2. Quantos lados têm um quadrado?\n" +
                            "<br> 3. Quantos lados iguais têm um quadrado?\n" +
                            "<br><strong>Exemplo:</strong> jogo quadrado → É ilustrado um quadrado na tela e feito três perguntas\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>jogo retangulo:</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para ilustrar a imagem de um retângulo na tela do smartphone e fazer três perguntas sobre um retângulo\t\n" +
                            "<br><strong>Perguntas:</strong> \n" +
                            "<br> 1. Qual é o nome desta forma?\n" +
                            "<br> 2. Quantas linhas possui um retângulo?\n" +
                            "<br> 3. O retângulo é igual ao quadrado?\n" +
                            "<br><strong>Exemplo:</strong> jogo retangulo → É ilustrado um retangulo na tela e feito três perguntas\n" +
                            "</p>\n" +
                            "\n" +
                            "\n" +
                            "<br><strong>soma(número 1 + número 2):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para a Lolli perguntar qual é a resposta da soma de dois números especificados pelo programador. A resposta deve ser dada via comando de voz.\n" +
                            "<br><strong>Exemplo:</strong> soma(2+5) → Lolli pergunta quanto que é 2 mais 5\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>sub(número 1 - número 2):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para a Lolli perguntar qual é a resposta da subtração de dois números especificados pelo programador. A resposta deve ser dada via comando de voz.\n" +
                            "<br><strong>Exemplo:</strong> sub(3-1) → Lolli pergunta quanto que é 3 menos 1\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>mult(número 1 * número 2):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para a Lolli perguntar qual é a resposta da multiplicação de dois números especificados pelo programador. A resposta deve ser dada via comando de voz.\n" +
                            "<br><strong>Exemplo:</strong> mult(1*3) → Lolli pergunta quanto que é 1 vezes 3\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>div(número 1 / número 2):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para a Lolli perguntar qual é a resposta da divisão de dois números especificados pelo programador. A resposta deve ser dada via comando de voz.\n" +
                            "<br><strong>Exemplo:</strong> div(8/2) → Lolli pergunta quanto que é 8 dividido por 2\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>soletrar(palavra a ser soletrada):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para a Lolli pedir para uma palavra desejada ser soletrada.\n" +
                            "<br><strong>Exemplo:</strong> soletrar(gato) → Lolli fala: Soletre a palavra gato. Então, a pessoa deve soletrar a palavra gato.\n" +
                            "<br><strong>Observação:</strong> A pessoa deve falar a palavra \"letra\" antes de falar a letra desejada. Se a palavra possui a letra \"A\", deve-se dizer \"letra A\".    \n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>falar(palavras a serem faladas):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para a Lolli falar as palavras escritas.\n" +
                            "<br><strong>Exemplo:</strong> falar(Oi, meu nome é Lolli) → Lolli fala: Oi, meu nome é Lolli\n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>animacao(tipo de animação):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para executar um áudio de animações\n" +
                            "<br><strong>Tipos de animaçoes:</strong>\n" +
                            "<br>• feliz\n" +
                            "<br>• triste\n" +
                            "<br><strong>Exemplo:</strong> animacao(feliz) → Lolli fica feliz \n" +
                            "</p>\n" +
                            "\n" +
                            "<br><strong>mostrar video(Link do video do YouTube):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para rodar um video do YouTube na tela do smartphone.\n" +
                            "<br><strong>Exemplo:</strong> \n" +
                            "<br>mostrar video(https://www.youtube.com/watch?v=g3uHKlXy5Yw) → Executa o video do link especificado do YouTube na tela do smartphone. \n" +
                            "</p>\n" +
                            "\n" +
                            "\n" +
                            "<br><strong>perguntar(pergunta desejada):</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para a Lolli fazer uma pergunta desejada para o usuário. A pergunta pode ser sobre qualquer tema, incluindo problemas matemáticos. Em conjunto com esta função, pode-se utilizar 2 outras funções que possibilitam um maior controle do sistema. \n" +
                            "<br><strong>Funções Utilizadas em Conjunto:</strong>\n" +
                            "<br>1. se resposta = (resposta correta)\n" +
                            "<br>2. senao\n" +
                            "<br>Para o entendimento de tais funções, procure as definições a seguir.\n" +
                            "<br><strong>Exemplo 1:</strong> \n" +
                            "<br>perguntar(quantas letras existem no alfabeto latino?)\n" +
                            "<br>se resposta = 26\n" +
                            "<br>falar(parabéns, voce acertou)\n" +
                            "<br>animacao(feliz)\n" +
                            "<br>fim se\n" +
                            "<br>\n" +
                            "<br><strong>Exemplo 2: </strong>\n" +
                            "<br>perguntar(quanto que é 5+5+10?)\n" +
                            "<br>se resposta = 20\n" +
                            "<br>falar(voce acertou!)\n" +
                            "<br>mover frente(0.5)\n" +
                            "<br>mover tras (0.5)\n" +
                            "<br>rodar\n" +
                            "<br>fim se\n" +
                            "<br>senao\n" +
                            "<br>mover tras(1)\n" +
                            "<br>animacao(triste)\n" +
                            "<br>falar(Esta não é a resposta correta. A resposta correta é 20. Assista um video na tela do celular para entender melhor sobre somar números)\n" +
                            "<br>mostrar video(https://www.youtube.com/watch?v=kq0kh0XvT9c)\n" +
                            "<br>fim senao\n" +
                            "</p>\n" +
                            "\n" +
                            "\n" +
                            "<br><strong>se resposta = resposta correta</strong>\n" +
                            "<br>Escrever outras funções aqui\n" +
                            "<br><strong>fim se</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para comparar se a resposta dada pelo usuário é a igual a resposta correta, que deve ser escrita pelo programador. \n" +
                            "No caso das respostas serem iguais, será executado todo o código do programa escrito entre as funções \"se resposta = resposta correta\" e \"fim se\". \n" +
                            "Caso contrário, será executado o código entre as funções \"senao\" e \"fim senao\".\n" +
                            "\n" +
                            "\n" +
                            "<br><strong>senao</strong>\n" +
                            "<br>Escrever outras funções aqui\n" +
                            "<br><strong>fim senao</strong>\n" +
                            "<br><p><strong>Definição:</strong> Esta função é utilizada para implementar códigos quando a resposta dada pelo usuário não é igual a resposta correta, que deve ser escrita pelo programador.\n" +
                            "Caso as respostas não forem iguais, será executado todo o código do programa implementado entre as funções \"senao\" e \"fim senao\"\n" +
                            "</p>\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "</div>\n"+
                            "<br>\n" +
                            "\n" +
                            "<p>\n" +
                            "<input type=\"submit\" name=\"executar\" value=\"Executar programa\">\n" +
                            "<br>\n" +
                            "<br>";

            //String Ref_Prog = "";
            StringBuilder Ref_Prog = new StringBuilder();

            File[] list = DiretorioArquivo.listFiles();

            for (File f : list) {
                QteArquivos++;
                String name = f.getName();

                String[] SepararTexto = name.split("\\.");
                String name_split = SepararTexto[0];

                String Prog = "<a href=programas.html?abrir=file://" + DiretorioArquivo + "/" + name_split + ">" + name_split + "</a>|\n";
                //Ref_Prog += Prog;
                Ref_Prog.append(Prog);


            }

            msg2 = Ref_Prog +
                    "<br>\n" +
                    "<input type=\"text\" value = \"" + nome_salvar + "\" id = \"IDNome\" name=\"nomePrograma\" />\n" +
                    "<input type=\"submit\" value=\"Salvar\" name = \"Salvar\">\n" +
                    "\n" +
                    "<script>\n" +
                    "\n" +
                    "function checkTextField(field) {\n" +
                    "    if (document.getElementById(\"IDNome\").value == '') {\n" +
                    "        alert(\"Insira o nome do programa\");\n" +
                    "    }\n" +
                    "}\n"+
                    "$(function() {\n" +
                    "\t$(\".lined\").linedtextarea(\n" +
                    "\n" +
                    "\t);\n" +
                    "});\n" +
                    "</script>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>";

            msg = msg1 + msg2;
            String uri = session.getUri();
            InputStream mbuffer;

            try {
                if(uri!=null){

                    if(uri.contains(".js")){
                        mbuffer = context.getAssets().open(uri.substring(1));
                        return new NanoHTTPD.Response(Response.Status.OK, "application/javascript", mbuffer);
                    }else if(uri.contains(".css")){
                        mbuffer = context.getAssets().open(uri.substring(1));
                        return new NanoHTTPD.Response(Response.Status.OK, "text/css", mbuffer);

                    }else if(uri.contains(".jpg")) {
                        mbuffer = context.getAssets().open(uri.substring(1));
                        // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                        return new NanoHTTPD.Response(Response.Status.OK, "image/jpeg", mbuffer);

                    }else if(uri.contains(".png")) {
                        mbuffer = context.getAssets().open(uri.substring(1));
                        // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                        return new NanoHTTPD.Response(Response.Status.OK, "image/png", mbuffer);

                    }else if (uri.contains("/mnt/sdcard")){
                        Log.d(TAG,"request for media on sdCard "+uri);
                        File request = new File(uri);
                        mbuffer = new FileInputStream(request);
                        FileNameMap fileNameMap = URLConnection.getFileNameMap();
                        String mimeType = fileNameMap.getContentTypeFor(uri);

                        Response streamResponse = new Response(Response.Status.OK, mimeType, mbuffer);
                        Random rnd = new Random();
                        String etag = Integer.toHexString( rnd.nextInt() );
                        streamResponse.addHeader( "ETag", etag);
                        streamResponse.addHeader( "Connection", "Keep-alive");

                        return streamResponse;
                    }else{
                        //mbuffer = mContext.getAssets().open("index.html");
                        return new NanoHTTPD.Response(msg);
                    }
                }

            } catch (IOException e) {
                Log.d(TAG,"Error opening file"+uri.substring(1));
                e.printStackTrace();
            }

            return null;
        }



        boolean CheckCode()
        {

            String[] sep_codigo = CodigoTexto.split("\n");
            texto_check_SB = new StringBuilder();
            String Codigo_tratado;

            int tam_codigo = sep_codigo.length - 1;

            boolean call_executar;

            //Todo: FUNCAO CHECK CODE

            for (int var_check = 0; var_check<=tam_codigo; var_check++)
            {

                Codigo_tratado = sep_codigo[var_check].toUpperCase().trim();
                if(Codigo_tratado.trim().isEmpty()) {
                    texto_check_SB.append("\n");
                }

                else if(Codigo_tratado.replace(" ", "").equals("INICIO"))
                {
                    texto_check_SB.append("OK\n");
                }
                else if(Codigo_tratado.replace(" ", "").equals("FIM"))
                {
                    texto_check_SB.append("OK\n");
                }

                else if (Codigo_tratado.replace(" ","").contains("JOGOCIRCULO"))
                {
                    texto_check_SB.append("OK\n");
                }
                else if (Codigo_tratado.replace(" ","").contains("JOGOQUADRADO"))
                {
                    texto_check_SB.append("OK\n");
                }
                else if (Codigo_tratado.replace(" ","").contains("JOGOTRIANGULO"))
                {
                    texto_check_SB.append("OK\n");
                }
                else if (Codigo_tratado.replace(" ","").contains("JOGORETANGULO"))
                {
                    texto_check_SB.append("OK\n");
                }

                else if (Codigo_tratado.contains("PERGUNTAR"))
                {
                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");

                    if (Codigo_tratado.contains("PERGUNTAR(") & Codigo_tratado.substring(0, 3).equals("PER") & Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")")) {
                        texto_check_SB.append("OK\n");
                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.contains("SE RESPOSTA") && Codigo_tratado.contains("="))
                {
                    texto_check_SB.append("OK\n");
                }


                else if(Codigo_tratado.replace(" ", "").equals("FIMSE"))
                {
                    texto_check_SB.append("OK\n");
                }

                else if(Codigo_tratado.replace(" ", "").equals("SENAO"))
                {
                    texto_check_SB.append("OK\n");
                }

                else if(Codigo_tratado.replace(" ", "").equals("FIMSENAO"))
                {
                    texto_check_SB.append("OK\n");
                }


                else if (Codigo_tratado.replace(" ", "").contains("MOSTRARVIDEO")) {
                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");

                    if (Codigo_tratado.contains("(") & Codigo_tratado.substring(0, 3).equals("MOS") & Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")")) {
                        texto_check_SB.append("OK\n");
                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.replace(" ", "").contains("ANIMACAO")) {
                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");

                    if (Codigo_tratado.contains("(") & Codigo_tratado.substring(0, 3).equals("ANI") & Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")")) {
                        if (Codigo_tratado.contains("FELIZ") || Codigo_tratado.contains("TRISTE") )
                        {
                            texto_check_SB.append("OK\n");
                        }
                        else
                        {
                            texto_check_SB.append("ERRO\n");
                        }

                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }


                else if (Codigo_tratado.contains("FALAR")) {
                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");

                    if (Codigo_tratado.contains("FALAR(") & Codigo_tratado.substring(0, 3).equals("FAL") & Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")")) {
                        texto_check_SB.append("OK\n");
                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.contains("SOLETRAR")) {
                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");

                    if (Codigo_tratado.contains("SOLETRAR(") & Codigo_tratado.substring(0, 3).equals("SOL") & Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")")) {
                        texto_check_SB.append("OK\n");
                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.contains("SOMA"))
                {

                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");
                    if (Codigo_tratado.contains("SOMA(") && Codigo_tratado.contains("+") && Codigo_tratado.substring(0, 3).equals("SOM") && Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")"))
                    {

                        if(Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1,Codigo_tratado.indexOf("+")).matches("\\d+(?:\\.\\d+)?"))
                        {
                            if(Codigo_tratado.substring(Codigo_tratado.indexOf("+") + 1,Codigo_tratado.indexOf(")")).matches("\\d+(?:\\.\\d+)?")) {
                                texto_check_SB.append("OK\n");
                            }
                        }

                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.contains("SUB"))
                {

                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");
                    if (Codigo_tratado.contains("SUB(") && Codigo_tratado.contains("-") && Codigo_tratado.substring(0, 3).equals("SUB") && Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")"))
                    {

                        if(Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1,Codigo_tratado.indexOf("-")).matches("\\d+(?:\\.\\d+)?"))
                        {
                            if(Codigo_tratado.substring(Codigo_tratado.indexOf("-") + 1,Codigo_tratado.indexOf(")")).matches("\\d+(?:\\.\\d+)?")) {
                                texto_check_SB.append("OK\n");
                            }
                        }

                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.contains("MULT"))
                {

                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");
                    if (Codigo_tratado.contains("MULT(") && Codigo_tratado.contains("*") && Codigo_tratado.substring(0, 3).equals("MUL") && Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")"))
                    {

                        if(Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1,Codigo_tratado.indexOf("*")).matches("\\d+(?:\\.\\d+)?"))
                        {
                            if(Codigo_tratado.substring(Codigo_tratado.indexOf("*") + 1,Codigo_tratado.indexOf(")")).matches("\\d+(?:\\.\\d+)?")) {
                                texto_check_SB.append("OK\n");
                            }
                        }

                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.contains("DIV"))
                {

                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");
                    if (Codigo_tratado.contains("DIV(") && Codigo_tratado.contains("/") && Codigo_tratado.substring(0, 3).equals("DIV") && Codigo_tratado.substring(Codigo_tratado.length() - 1).equals(")"))
                    {

                        if(Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1,Codigo_tratado.indexOf("/")).matches("\\d+(?:\\.\\d+)?"))
                        {
                            if(Codigo_tratado.substring(Codigo_tratado.indexOf("/") + 1,Codigo_tratado.indexOf(")")).matches("\\d+(?:\\.\\d+)?")) {
                                texto_check_SB.append("OK\n");
                            }
                        }

                    }
                    else
                    {
                        texto_check_SB.append("ERRO\n");
                    }
                }

                else if (Codigo_tratado.contains("MOVER") && Codigo_tratado.contains("(") && Codigo_tratado.contains(")") )
                {
                    Codigo_tratado = Codigo_tratado.replaceAll(" ", "");
                    if (Codigo_tratado.contains("FRENTE") || Codigo_tratado.contains("TRAS") || Codigo_tratado.contains("ESQUERDA") || Codigo_tratado.contains("DIREITA") )
                    {

                        if(Codigo_tratado.substring(Codigo_tratado.indexOf("(")+1,Codigo_tratado.indexOf(")")).matches("\\d+(?:\\.\\d+)?"))
                        {
                            texto_check_SB.append("OK\n");

                        }
                        else
                        {
                            texto_check_SB.append("ERRO\n");
                        }

                    }

                }
                else if (Codigo_tratado.replace(" ","").contains("RODAR"))
                {
                    texto_check_SB.append("OK\n");
                }

                else
                {
                    texto_check_SB.append("ERRO\n");
                }


            }

            texto_check = texto_check_SB.toString();

            call_executar = !texto_check.contains("ERRO");
            return call_executar;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        void ExecutarPrograma() {

            System.out.println("ENTROU NO EXECUTAR");

            enter_func = 0;

            String[] sep_codigo = CodigoTexto.split("\n");
            String Codigo_tratado;
            int tam_codigo = sep_codigo.length - 1;
            String sep_codigo_up;

            final ImageView imageLolli;
            imageLolli = findViewById(R.id.ImageViewLolli);


            if (var_loop <= tam_codigo) {

                final HashMap<String, String> myHashAlarm = new HashMap<>();
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
                sep_codigo_up = sep_codigo[var_loop].toUpperCase();


                if (sep_codigo_up.contains("MOVER") && conn_bluetooth ) {
                    enter_func = 1;
                    //Todo: Consertar a funcao ANDAR. Colocar FRENTE, TRAS, ESQUERDA, DIREITA
                    System.out.println("comando de andar");

                    System.out.println(bt.getConnectedDeviceAddress());

                    if (sep_codigo_up.contains("FRENTE"))
                    {


                        String tempo_mover = sep_codigo_up.replace(" ","").substring(sep_codigo_up.indexOf("("), sep_codigo_up.indexOf(")") - 1);
                        float tempo_mover_float = Float.valueOf(tempo_mover);
                        tempo_mover_float = tempo_mover_float*1000;

                        bt.send("f",true);
                        try {
                            Thread.sleep((long) tempo_mover_float);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bt.send("0",true);

                        System.out.println(tempo_mover_float);
                        System.out.println(bt.getConnectedDeviceName());
                        System.out.println(bt.getConnectedDeviceAddress());

                    }

                    else if (sep_codigo_up.contains("TRAS"))
                    {


                        String tempo_mover = sep_codigo_up.replace(" ","").substring(sep_codigo_up.indexOf("(") + 1, sep_codigo_up.indexOf(")"));
                        float tempo_mover_float = Float.valueOf(tempo_mover);
                        tempo_mover_float = tempo_mover_float*1000;

                        bt.send("r",true);
                        try {
                            Thread.sleep((long) tempo_mover_float);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bt.send("0",true);

                    }

                    else if (sep_codigo_up.contains("ESQUERDA"))
                    {


                        String tempo_mover = sep_codigo_up.replace(" ","").substring(sep_codigo_up.indexOf("(") + 1, sep_codigo_up.indexOf(")"));
                        float tempo_mover_float = Float.valueOf(tempo_mover);
                        tempo_mover_float = tempo_mover_float*1000;

                        bt.send("e",true);
                        try {
                            Thread.sleep((long) tempo_mover_float);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bt.send("0",true);

                    }

                    else if (sep_codigo_up.contains("DIREITA"))
                    {


                        String tempo_mover = sep_codigo_up.replace(" ","").substring(sep_codigo_up.indexOf("(") + 1, sep_codigo_up.indexOf(")"));
                        float tempo_mover_float = Float.valueOf(tempo_mover);
                        tempo_mover_float = tempo_mover_float*1000;

                        bt.send("d",true);
                        try {
                            Thread.sleep((long) tempo_mover_float);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bt.send("0",true);

                    }

                }
                else if (sep_codigo_up.replace(" ", "").contains("RODAR") && conn_serv)
                {

                    bt.send("2",true);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bt.send("0",true);

                }
                else if (sep_codigo_up.replace(" ", "").contains("MOSTRARVIDEO"))
                {
                    enter_func = 0;
                    String YouTube_video;

                    YouTube_video = sep_codigo[var_loop].replace(" ", "").trim().substring(sep_codigo[var_loop].indexOf("("),sep_codigo[var_loop].indexOf(")") - 1);
                    /*if (!YouTube_video.startsWith("http://") && !YouTube_video.startsWith("https://"))
                        YouTube_video = "http://" + YouTube_video;*/
                    System.out.println(YouTube_video);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YouTube_video));
                    startActivity(browserIntent);
                }
                else if (sep_codigo_up.contains("FALAR")) {
                    enter_func = 1;

                    falar_codigo = sep_codigo_up.substring(sep_codigo_up.indexOf("(") + 1, sep_codigo_up.indexOf(")"));
                    System.out.println("codigo fala = " + falar_codigo);

                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FALAR");
                    myTTS.speak(falar_codigo, TextToSpeech.QUEUE_ADD, myHashAlarm);
                }

                else if (sep_codigo_up.replace(" ", "").contains("SERESPOSTA"))
                {
                    enter_func = 0;

                    Codigo_tratado = sep_codigo_up.replace(" ", "");
                    correct_result = Codigo_tratado.substring(Codigo_tratado.indexOf("=") + 1, Codigo_tratado.length() - 1);

                    String trat_Resp;
                    String trat_Result;

                    for (String result : result_recog) {
                        trat_Resp = result.replaceAll(" ","").toUpperCase();
                        trat_Result = correct_result.replaceAll(" ","").toUpperCase();

                        if (trat_Resp.equals(trat_Result))
                        {
                            ent_senao = false;
                            System.out.println("VEIO NO RESP IGUAL");
                            break;
                        }
                        else
                        {
                            ent_senao = true;
                        }
                    }

                    if (ent_senao)
                    {
                        boolean sair_se = true;
                        while (sair_se)
                        {
                            var_loop++;
                            if (sep_codigo[var_loop].replace(" ", "").toUpperCase().trim().equals("FIMSE"))
                            {
                                sair_se = false;
                            }
                        }

                    }

                }

                else if (sep_codigo_up.replace(" ", "").contains("SENAO"))
                {
                    if (!ent_senao)
                    {
                        boolean sair_senao = true;
                        while (sair_senao)
                        {
                            var_loop++;
                            if (sep_codigo[var_loop].replace(" ", "").toUpperCase().trim().equals("FIMSENAO"))
                            {
                                sair_senao = false;
                            }
                        }
                    }
                }

                else if (sep_codigo_up.contains("PERGUNTAR")) {
                    enter_func = 1;

                    falar_codigo = sep_codigo_up.substring(sep_codigo_up.indexOf("(") + 1, sep_codigo_up.indexOf(")"));
                    System.out.println("codigo fala = " + falar_codigo);

                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "PERGUNTAR");
                    myTTS.speak(falar_codigo, TextToSpeech.QUEUE_ADD, myHashAlarm);



                }

                else if (sep_codigo_up.contains("ESPERAR RESPOSTA")) {
                    enter_func = 1;
                    num_jogo = 0;
                    ReconhecimentoDeVoz();
                }

                else if (sep_codigo_up.contains("SE RESPOSTA")) {
                    enter_func = 1;
                    correct_result = sep_codigo_up.substring(sep_codigo_up.lastIndexOf("=") + 1);
                    num_jogo = 4;
                    VerifyResult();


                }




                else if (sep_codigo_up.contains("SOMA")) {
                    enter_func = 1;
                    Codigo_tratado = sep_codigo_up.replace(" ", "");
                    String x = Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1, Codigo_tratado.indexOf("+"));
                    String y = Codigo_tratado.substring(Codigo_tratado.indexOf("+") + 1, Codigo_tratado.indexOf(")"));

                    falar_codigo = "Quanto que é " + x + "mais" + y + "?";

                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MAT");
                    myTTS.speak(falar_codigo, TextToSpeech.QUEUE_ADD, myHashAlarm);

                    float x_f = Float.parseFloat(x);
                    float y_f = Float.parseFloat(y);
                    float resp_soma = x_f + y_f;

                    correct_result = Float.valueOf(resp_soma).toString().replaceAll("\\.?0*$", "");
                }

                else if (sep_codigo_up.contains("SUB")) {
                    enter_func = 1;
                    Codigo_tratado = sep_codigo_up.replace(" ", "");
                    String x = Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1, Codigo_tratado.indexOf("-"));
                    String y = Codigo_tratado.substring(Codigo_tratado.indexOf("-") + 1, Codigo_tratado.indexOf(")"));

                    falar_codigo = "Quanto que é " + x + "menos" + y + "?";

                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MAT");
                    myTTS.speak(falar_codigo, TextToSpeech.QUEUE_ADD, myHashAlarm);

                    float x_f = Float.parseFloat(x);
                    float y_f = Float.parseFloat(y);
                    float resp_soma = x_f - y_f;

                    correct_result = Float.valueOf(resp_soma).toString().replaceAll("\\.?0*$", "");
                }

                else if (sep_codigo_up.contains("MULT")) {
                    enter_func = 1;
                    Codigo_tratado = sep_codigo_up.replace(" ", "");
                    String x = Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1, Codigo_tratado.indexOf("*"));
                    String y = Codigo_tratado.substring(Codigo_tratado.indexOf("*") + 1, Codigo_tratado.indexOf(")"));

                    falar_codigo = "Quanto que é " + x + "vezes" + y + "?";

                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MAT");
                    myTTS.speak(falar_codigo, TextToSpeech.QUEUE_ADD, myHashAlarm);

                    float x_f = Float.parseFloat(x);
                    float y_f = Float.parseFloat(y);
                    float resp_soma = x_f * y_f;

                    correct_result = Float.valueOf(resp_soma).toString().replaceAll("\\.?0*$", "");
                }

                else if (sep_codigo_up.contains("DIV")) {
                    enter_func = 1;
                    Codigo_tratado = sep_codigo_up.replace(" ", "");
                    String x = Codigo_tratado.substring(Codigo_tratado.indexOf("(") + 1, Codigo_tratado.indexOf("/"));
                    String y = Codigo_tratado.substring(Codigo_tratado.indexOf("/") + 1, Codigo_tratado.indexOf(")"));

                    falar_codigo = "Quanto que é " + x + "dividido por" + y + "?";

                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MAT");
                    myTTS.speak(falar_codigo, TextToSpeech.QUEUE_ADD, myHashAlarm);

                    float x_f = Float.parseFloat(x);
                    float y_f = Float.parseFloat(y);

                    float resp_soma = x_f / y_f;

                    correct_result = Float.valueOf(resp_soma).toString().replaceAll("\\.?0*$", "");
                }

                else if(sep_codigo_up.contains("JOGO QUADRADO")) {

                    enter_func = 1;


                    if (jogo_forma == 0) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.square);

                            }
                        });


                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        correct_result = "quadrado";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Qual é o nome desta forma?", TextToSpeech.QUEUE_ADD, myHashAlarm);
                    }

                    else if (jogo_forma == 1) {

                        correct_result = "quatro";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Quantos lados têm um quadrado?" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                     }

                    else if (jogo_forma == 2) {

                        correct_result = "quatro";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Quantos lados iguais têm um quadrado?" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                    }


                    else if (jogo_forma == 3) {
                        enter_func = 0;

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.robo_feliz);

                            }
                        });

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }


                }




                else if(sep_codigo_up.contains("JOGO CIRCULO")) {

                    enter_func = 1;


                    if (jogo_forma == 0) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.circle);

                            }
                        });


                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        correct_result = "círculo";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Qual é o nome desta forma?", TextToSpeech.QUEUE_ADD, myHashAlarm);
                    }

                    else if (jogo_forma == 1) {

                        correct_result = "sim";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("O círculo é redondo?????" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                    }

                    else if (jogo_forma == 2) {

                        correct_result = "não";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("O círculo têm linhas retas????" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                    }


                    else if (jogo_forma == 3) {
                        enter_func = 0;

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.robo_feliz);

                            }
                        });

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }


                }



                else if(sep_codigo_up.contains("JOGO TRIANGULO")) {

                    enter_func = 1;

                    if (jogo_forma == 0) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.triangle);

                            }
                        });


                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        correct_result = "triângulo";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Qual é o nome desta forma?", TextToSpeech.QUEUE_ADD, myHashAlarm);
                    }

                    else if (jogo_forma == 1) {

                        correct_result = "três";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Quantas linhas possui um triângulo?" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                    }

                    else if (jogo_forma == 2) {

                        correct_result = "não";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("O triângulo é redondo?" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                    }


                    else if (jogo_forma == 3) {
                        enter_func = 0;

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.robo_feliz);

                            }
                        });

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }


                }

                else if(sep_codigo_up.contains("JOGO RETANGULO")) {

                    enter_func = 1;


                    if (jogo_forma == 0) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.rectangle);

                            }
                        });


                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        correct_result = "retângulo";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Qual é o nome desta forma?", TextToSpeech.QUEUE_ADD, myHashAlarm);
                    }

                    else if (jogo_forma == 1) {

                        correct_result = "quatro";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("Quantas linhas possui um retângulo?" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                    }

                    else if (jogo_forma == 2) {

                        correct_result = "não";
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FORMA");
                        myTTS.speak("O retângulo é igual ao quadrado?" , TextToSpeech.QUEUE_ADD, myHashAlarm);

                    }


                    else if (jogo_forma == 3) {
                        enter_func = 0;

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                imageLolli.setImageResource(R.drawable.robo_feliz);

                            }
                        });

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }


                }

                else if(sep_codigo_up.contains("SOLETRAR")) {

                    enter_func = 1;

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            imageLolli.setImageResource(android.R.color.transparent);

                        }
                    });


                    falar_codigo = sep_codigo_up.substring(sep_codigo_up.indexOf("(") + 1, sep_codigo_up.indexOf(")"));
                    correct_result = falar_codigo;
                    System.out.println("Soletre a palavra " + falar_codigo);




                    for (int var_sol=0;var_sol<falar_codigo.length();var_sol++)
                    {
                        under_sol.append("_ ");
                    }
                    System.out.println(under_sol);
                    tam_palavra = under_sol.length();

                    TextView txtSoletrar = findViewById(R.id.textSoletrar);
                    txtSoletrar.setText(under_sol);

                    String TextoSoletrar = "Soletre a palavra " + falar_codigo;

                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOLETRAR");
                    myTTS.speak(TextoSoletrar, TextToSpeech.QUEUE_ADD, myHashAlarm);

                }


                else if (sep_codigo_up.contains("ANIMACAO")) {
                    enter_func = 1;

                    String tipo_anim;

                    tipo_anim = sep_codigo_up.substring(sep_codigo_up.indexOf("(") + 1, sep_codigo_up.indexOf(")"));
                    tipo_anim = tipo_anim.replace(" ", "").toUpperCase().trim();
                    System.out.println("ANIMACAO");
                    System.out.println(tipo_anim);
                    if (tipo_anim.equals("FELIZ"))
                    {

                        imageLolli.setImageResource(R.drawable.robo_ganhou);
                        final MediaPlayer mpAplausos = MediaPlayer.create(ServidorAndroid.this, R.raw.aplausos);
                        mpAplausos.start();
                        mpAplausos.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                imageLolli.setImageResource(R.drawable.robo_feliz);
                                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FALAR");
                                myTTS.speak("", TextToSpeech.QUEUE_ADD, myHashAlarm);
                            }
                        });

                    }

                    else if (tipo_anim.equals("TRISTE"))
                    {
                        imageLolli.setImageResource(R.drawable.robo_triste);
                        final MediaPlayer mpDecepcao = MediaPlayer.create(ServidorAndroid.this, R.raw.decepcao);
                        mpDecepcao.start();
                        mpDecepcao.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                imageLolli.setImageResource(R.drawable.robo_feliz);
                                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FALAR");
                                myTTS.speak("", TextToSpeech.QUEUE_ADD, myHashAlarm);
                            }
                        });

                    }

                    //Feliz
                    //Trsite
                    ///etc

                } else if (sep_codigo_up.contains("IFIFNOT")) {
                    enter_func = 1;
                }

                if (ver_result == 1) {
                    enter_func = 1;
                    System.out.println("Rec 1");
                    ReconhecimentoDeVoz();
                    System.out.println("Rec 2");
                }

                if (enter_func == 0 && var_loop < tam_codigo) {

                    var_loop = var_loop + 1;
                    ExecutarPrograma();

                }
            }
        }

        public void VerifyResult()
        {

            //todo verify result

            boolean right_answer = false;
            final MediaPlayer mpAplausos = MediaPlayer.create(ServidorAndroid.this, R.raw.aplausos);
            final MediaPlayer mpDecepcao = MediaPlayer.create(ServidorAndroid.this, R.raw.decepcao);
            final HashMap<String, String> myHashAlarm = new HashMap<>();
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));

            final ImageView imageLolli;
            imageLolli = findViewById(R.id.ImageViewLolli);


            if (num_jogo == 0)
            {
                var_loop = var_loop + 1;
                ExecutarPrograma();
            }

            else if (num_jogo == 1)
            {

                for (String result : result_recog)
                {

                    if(result.matches("\\d+(?:\\.\\d+)?"))
                    {

                        if (Float.valueOf(result).equals(Float.valueOf(correct_result))) {
                            right_answer = true;
                            System.out.println("ACERTOU");

                            imageLolli.setImageResource(R.drawable.robo_ganhou);
                            mpAplausos.start();
                            mpAplausos.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                public void onCompletion(MediaPlayer mp) {
                                    imageLolli.setImageResource(R.drawable.robo_feliz);
                                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FALAR");
                                    myTTS.speak("Parabéns", TextToSpeech.QUEUE_ADD, myHashAlarm);
                                }
                            });


                        }

                    }

                }
                if (!right_answer)
                {
                    jogo_forma = 0;
                    imageLolli.setImageResource(R.drawable.robo_triste);
                    mpDecepcao.start();
                    mpDecepcao.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            imageLolli.setImageResource(R.drawable.robo_feliz);
                            System.out.println("ERROU");
                            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FALAR");
                            myTTS.speak("A resposta correta é " + correct_result, TextToSpeech.QUEUE_ADD, myHashAlarm);
                        }
                    });
                }
            }

            else if (num_jogo == 2)
            {

                for (String result : result_recog)
                {

                    if (result.toUpperCase().equals(correct_result.toUpperCase())) {
                        right_answer = true;
                        System.out.println("ACERTOU");

                        imageLolli.setImageResource(R.drawable.robo_ganhou);
                        mpAplausos.start();
                        mpAplausos.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {


                                if(jogo_forma == 3)
                                {
                                    imageLolli.setImageResource(R.drawable.robo_feliz);
                                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ACERTOU");
                                    myTTS.speak("Parabéns. Você acertou todas as perguntas.", TextToSpeech.QUEUE_ADD, myHashAlarm);
                                }

                                else
                                {
                                    imageLolli.setImageResource(R.drawable.robo_feliz);
                                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ACERTOU");
                                    myTTS.speak("Parabéns.", TextToSpeech.QUEUE_ADD, myHashAlarm);
                                }
                            }
                        });
                    }
                }
                if (!right_answer)
                {
                    imageLolli.setImageResource(R.drawable.robo_triste);
                    mpDecepcao.start();
                    mpDecepcao.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            System.out.println("ERROU");
                            imageLolli.setImageResource(R.drawable.robo_feliz);
                            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ACERTOU");
                            myTTS.speak("A resposta correta é " + correct_result, TextToSpeech.QUEUE_ADD, myHashAlarm);
                            jogo_forma = 3;
                        }
                    });
                }
            }

            else if (num_jogo == 3)
            {
                System.out.println("SOLETRAR");
                System.out.println(jogo_soletrar);
                System.out.println(tam_palavra);
                if (jogo_soletrar<tam_palavra)
                {
                    String result = result_recog.get(0);
                    result = result.toUpperCase();
                    if (result.contains("LETRA") && result.length() == 7 && result.contains(" ")) {

                        String[] split_letra = result.split("\\s+");
                        under_sol.replace(jogo_soletrar, jogo_soletrar + 1, split_letra[1]);

                        TextView txtSoletrar = findViewById(R.id.textSoletrar);
                        txtSoletrar.setText(under_sol);

                        jogo_soletrar = jogo_soletrar + 2;
                        palavra_sol.append(split_letra[1]);

                        System.out.println("AQUI!");
                        System.out.println(palavra_sol);
                        System.out.println(correct_result);

                        if (jogo_soletrar<tam_palavra)
                        {
                            server.ReconhecimentoDeVoz();
                        }
                        else
                        {
                            if (palavra_sol.toString().equals(correct_result))
                            {
                                System.out.println("ACERTOU_SOL");

                                imageLolli.setImageResource(R.drawable.robo_ganhou);
                                mpAplausos.start();
                                mpAplausos.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    public void onCompletion(MediaPlayer mp) {
                                        imageLolli.setImageResource(R.drawable.robo_feliz);
                                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ACERTOU_SOL");
                                        myTTS.speak("Parabéns", TextToSpeech.QUEUE_ADD, myHashAlarm);
                                    }
                                });
                            }
                            else
                            {
                                imageLolli.setImageResource(R.drawable.robo_triste);
                                mpDecepcao.start();
                                mpDecepcao.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    public void onCompletion(MediaPlayer mp) {
                                        imageLolli.setImageResource(R.drawable.robo_feliz);
                                        System.out.println("ERROU");
                                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ERROU_SOLET");
                                        myTTS.speak("Infelizmente você não acertou. Veja na minha tela a forma correta de soletrar a palavra", TextToSpeech.QUEUE_ADD, myHashAlarm);

                                    }
                                });
                                palavra_sol = new StringBuilder();

                            }
                        }

                    }
                    else
                    {
                        server.ReconhecimentoDeVoz();
                    }

                }

            }

            else if (num_jogo == 4) {
                String trat_Resp;
                String trat_Result;

                for (String result : result_recog) {
                    trat_Resp = result.replaceAll(" ","").toUpperCase();
                    trat_Result = correct_result.replaceAll(" ","").toUpperCase();
                    if (trat_Result.contains(trat_Resp))
                    {
                        System.out.println("RESPOSTA CERTA");

                    }

                }
            }

        }

        private void ReconhecimentoDeVoz()
        {
            /*Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
            startActivityForResult(intent, REQUEST_CODE);*/
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    sr.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));

                }
            });
        }
    }

    @Override
    public void onInit(int initStatus) {

        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setOnUtteranceCompletedListener(this);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {

        //todo Aqui é o CALLBACK
        Log.i("CALLBACK", utteranceId); //utteranceId == "SOME MESSAGE"
        TextView txtSoletrar = findViewById(R.id.textSoletrar);

        final ImageView imageLolli;
        imageLolli = findViewById(R.id.ImageViewLolli);

        switch (utteranceId) {
            case "MAT":
                num_jogo = 1;
                server.ReconhecimentoDeVoz();
                break;

            case "FALAR":
                var_loop = var_loop + 1;
                server.ExecutarPrograma();

                break;

            case "FORMA":

                jogo_forma = jogo_forma + 1;
                num_jogo = 2;
                server.ReconhecimentoDeVoz();
                break;

            case "ACERTOU":
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server.ExecutarPrograma();

                break;

            case "SOLETRAR":
                num_jogo = 3;
                server.ReconhecimentoDeVoz();
                break;

            case "PERGUNTAR":
                num_jogo = 0;
                server.ReconhecimentoDeVoz();
                break;

            case "ERROU_SOLET":

                txtSoletrar.setText(correct_result);

                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                txtSoletrar.setText("");

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        imageLolli.setImageResource(R.drawable.robo_feliz);

                    }
                });

                var_loop = var_loop + 1;

                server.ExecutarPrograma();
                break;

            case "ACERTOU_SOL":

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                txtSoletrar.setText("");

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        imageLolli.setImageResource(R.drawable.robo_feliz);

                    }
                });

                var_loop = var_loop + 1;
                server.ExecutarPrograma();
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

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            result_recog = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            System.out.println("Resultados :"+result_recog);
            server.VerifyResult();

        }


        if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
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

        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        }
    }


    class listener implements RecognitionListener
    {

        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB)
        {
            //Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error)
        {

            Log.d(TAG,  "error " +  error);
            if (error == 7)
            {
                sr.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
            }

        }
        public void onResults(Bundle results)
        {

            Log.d(TAG, "onResults " + results);
            result_recog = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            System.out.println("Resultados :"+result_recog);
            server.VerifyResult();

        }

        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

}