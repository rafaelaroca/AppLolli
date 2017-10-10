package com.example.victor.applolli;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import java.io.IOException;
import java.util.Map;
import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import fi.iki.elonen.NanoHTTPD;
import android.os.Environment;
import android.widget.Toast;

import java.io.*;
import java.util.*;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Looper.prepare;

public class ServidorAndroid extends Activity implements TextToSpeech.OnInitListener{
    Context context = this;
    private WebServer server;
    int QteArquivos = 0;
    int opcao_fala;
    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;

    String texto_programa = "";
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
    String []VetorFalar;
    String Teste = "Testando";
    String nome_salvar = "";
    int ind_falar = 0;

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    //  @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servidor_android);
        server = new WebServer();
        TextView EnderecoIp = (TextView) findViewById(R.id.TxtIp);
        System.out.println("Estado do local externo " + Environment.getExternalStorageState());
        //   System.out.println("SD =" + getExternalFilesDirs(null));

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


        try {

            server.start();
            WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            int ipAddress = wifiInf.getIpAddress();
            String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            System.out.println(ip);

            EnderecoIp.setText("Endereço de IP: " + ip + ":8080");

        } catch (IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
    }


    // DON'T FORGET to stop the server
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

    private class WebServer extends NanoHTTPD {

        //   @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void generateNoteOnSD(String sBody) {

            sFileName =   Nome_SalvarComo + ".txt";

            try {

                File root = new File(getExternalStorageDirectory(), "ArquivosProjeto");
                if (!root.exists()) {
                    root.mkdirs();
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


        public WebServer() {
            super(8080);
        }


        //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public Response serve(IHTTPSession session) {

            QteArquivos = 0;
            File root = new File(getExternalStorageDirectory(), "ArquivosProjeto");
            if (!root.exists()) {
                root.mkdirs();
            }

            File DiretorioArquivo = new File(getExternalStorageDirectory() + "/ArquivosProjeto");


            // Map<String, String> parms = session.getParms();
            Map<String, String> files = new HashMap<String, String>();
            Method method = session.getMethod();

            param_abrir = session.getParms().toString();
            boolean b = param_abrir.contains("abrir=");


            if (b == true ) {
                texto_abrir = session.getParms().get("abrir");
                String[] sep_text = texto_abrir.split("\\/");
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

            } else {
                //texto_programa = "Digite o código desejado";
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
                Salvar_Como = session.getParms().get("BotaoSalvar");
                Salvar = session.getParms().get("Salvar");


                if (BotaoExecutar != null) {

                    CodigoTexto = session.getParms().get("TextoCodigo");
                    texto_programa = CodigoTexto;
                    System.out.println("Veio no executar");
                    ExecutarPrograma(CodigoTexto);


                }

                if (Salvar_Como != null) {

                    Nome_SalvarComo = session.getParms().get("nomePrograma");
                    CodigoTexto = session.getParms().get("TextoCodigo");
                    texto_programa = CodigoTexto;

                    if (Nome_SalvarComo.equals(""))
                    {
                    }
                    else
                    {
                        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                        } else {
                            generateNoteOnSD(CodigoTexto);
                        }
                    }

                }

                if (Salvar != null) {

                    Nome_SalvarComo = session.getParms().get("nomePrograma");
                    CodigoTexto = session.getParms().get("TextoCodigo");
                    texto_programa = CodigoTexto;

                    if (Nome_SalvarComo.equals(""))
                    {
                    }
                    else
                    {
                        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                        } else {
                            generateNoteOnSD(CodigoTexto);
                        }
                    }

                }

            }

            msg1 =

                    "<!DOCTYPE html>\n" +
                            "\n" +
                            "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">        \n" +
                            "<title>titulo 1</title></head><body><h1> Servidor Web </h1><p>Digite os comandos desejados </p>  \n" +
                            "  <!-- Conteúdo --><form method=\"post\" id = \"myForm\">    <textarea cols=\"50\" rows=\"8\" name=\"TextoCodigo\">\n" + texto_programa + "</textarea>  \n" +
                            "\n" +
                            "<br>\n" +
                            "\n" +
                            " <input type=\"submit\" name=executar value=\"Executar programa\">\n" +
                            "\n" +
                            "<br>\n" +
                            "<br>\n" +
                            "\n";

            String Ref_Prog = "";
            File[] list = DiretorioArquivo.listFiles();
            for (File f : list) {
                QteArquivos++;
                String name = f.getName();

                String[] SepararTexto = name.split("\\.");
                String name_split = SepararTexto[0];

                String Prog = "<a href=programas.html?abrir=file://" + DiretorioArquivo + "/" + name_split + ">" + name_split + "</a>|\n";
                Ref_Prog += Prog;
            }



            msg2 = Ref_Prog +
                    "<br>\n" +
                    "<br>\n" +
                    "<input type=text value = \"" + nome_salvar +"\" id = \"IDNome\" name=\"nomePrograma\" onchange=\"handleChange(this)\"/>\n" +
                    "<input type=\"submit\" value=Salvar name = \"Salvar\">\n" +
                    "<input type=\"submit\" name=\"BotaoSalvar\" value=\"Salvar Como...\" onclick = \"handleChange()\" />\n" +
                    "\n" +
                    "<script>\n" +
                    "  function handleChange(input)\n" +
                    "  {\n" +
                    "\t\tvar x = document.getElementById(\"IDNome\").value;\n" +
                    "\t\tif (x === \"\")\n" +
                    "\t\t{\n" +
                    "\t\t\talert(\"Insira o nome do programa\");\n" +
                    "\t\t}\n" +
                    "  }\n" +
                    "\t\n" +
                    "\t\n" +
                    "</script>\n" +
                    "</form>\n" +
                    "</body></html>";

            msg = msg1 + msg2;
            return new NanoHTTPD.Response(msg);

        }

        void ExecutarPrograma(String Codigo) {


            if(Codigo.contains("<INICIO>") & Codigo.contains("<FIM>"))
            {

                String[] sep_codigo = Codigo.split("\\\n");
                int tam_codigo = sep_codigo.length - 1;

                for (int i=0 ; i<=tam_codigo ; i++)
                {
                    String Codigo_tratado = sep_codigo[i].replace(" ", "");
                    System.out.println("Linha do codigo =" + sep_codigo[i]);
                    System.out.println("Codigo tratado =" + Codigo_tratado);

                    if (sep_codigo[i].contains("<INICIO>"))
                    {
                        System.out.println("Inicio do programa");
                    }

                    else if (sep_codigo[i].contains("<FIM>"))
                    {
                        System.out.println("Fim do programa");
                        i = tam_codigo;
                    }
                    else if (sep_codigo[i].contains("Andar") || sep_codigo[i].contains("andar") )
                    {
                        System.out.println("comando de andar");


                        int startIndex_por = Codigo_tratado.indexOf("por");
                        String Tempo_Codigo = Codigo_tratado.substring(startIndex_por + 3,startIndex_por + 4);

                        int startIndex_para = Codigo_tratado.indexOf("para");
                        String Dir_Codigo = Codigo_tratado.substring(startIndex_para + 4,startIndex_por);

                        int tempo_andar = 0;
                        try{
                            tempo_andar = Integer.parseInt(Tempo_Codigo);
                        } catch(NumberFormatException nfe){
                            System.out.println("Nao foi possivel converter para inteiro " + nfe);
                        }

                        // Ir para o terminal, enviando os dados de Tempo e direçao

                        System.out.println("tempo andar = " + tempo_andar);
                        System.out.println("direcao andar = " + Dir_Codigo);


                        TerminalActivity.FuncaoAndar(tempo_andar,Dir_Codigo);
                    }

                    else if (sep_codigo[i].contains("Falar") || sep_codigo[i].contains("falar"))
                    {
                        System.out.println("Veio em falar");
                        int startIndex_falar = sep_codigo[i].indexOf("[");
                        int endIndex_falar = sep_codigo[i].indexOf("]");

                        falar_codigo = sep_codigo[i].substring(startIndex_falar + 1,endIndex_falar);
                        System.out.println("codigo fala = " + falar_codigo);
                        myTTS.speak(falar_codigo, TextToSpeech.QUEUE_FLUSH, null);

                        try {

                            if (sep_codigo[i].length() - 8 <= 5)
                            {
                                Thread.sleep(180*(sep_codigo[i].length() - 8));
                                System.out.println("Tempo =" + 150* (sep_codigo[i].length()- 8));
                            }

                            if (sep_codigo[i].length() - 8 > 5 &  sep_codigo[i].length() - 8 <= 10)
                            {
                                Thread.sleep(150*(sep_codigo[i].length() - 8));
                                System.out.println("Tempo =" + 150* (sep_codigo[i].length()- 8));
                            }

                            if (sep_codigo[i].length() - 8 > 10 & sep_codigo[i].length() - 8 < 30)
                            {
                                Thread.sleep(100*(sep_codigo[i].length() - 8));
                                System.out.println("Tempo =" + 100* (sep_codigo[i].length()- 8));
                            }

                            if (sep_codigo[i].length() - 8 > 30)
                            {
                                Thread.sleep(80*(sep_codigo[i].length() - 8));
                                System.out.println("Tempo =" + 80* (sep_codigo[i].length()- 8));
                            }



                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        //speakWords0(falar_codigo);

                        //  myTTS.speak(falar_codigo, TextToSpeech.QUEUE_FLUSH, null);
                        opcao_fala = 0;

                        //new ExecuteAsyncTask().execute();

                       /* final Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable = new Runnable(){
                            @Override
                            public void run() {
                                Looper.prepare();
                                System.out.println("veio no runnable");
                            }};
                        handler.postDelayed(runnable,1000);*/

                        //isTTSSpeaking();

                       /* final Handler h = new Handler(Looper.getMainLooper());

                        Runnable r = new Runnable() {

                            public void run() {

                                if (!myTTS.isSpeaking()) {
                                    System.out.println("Acabou de falar");
                                } else {
                                    h.postDelayed(this, 100);
                                }
                            }
                        };

                        h.postDelayed(r, 1000);*/




                        /*final Runnable runnable = new Runnable() {
                            public void run() {

                                // need to do tasks on the UI thread
                             //   Log.d(TAG, "runn test");

                                    handler.postDelayed(this, 1000);
                                System.out.println("veio no handler");

                            }
                        };*/

                        // trigger first time
                        // handler.post(runnable);


                    }

                }


                System.out.println("FIM e INICIO");

            }

        }


        class ExecuteAsyncTask extends AsyncTask<Object, Void, Void> {


            protected Void doInBackground(Object... task_idx) {


                try {
                    myTTS.speak(falar_codigo, TextToSpeech.QUEUE_FLUSH, null);
                    System.out.println("veio no asynk");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



                //

                return null;
            }


            protected void onPostExecute(Void result) {

            }

        }

    }



    @Override
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
            //speakWords0();
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    private void speakWords0(String falar_cod) {
        myTTS.speak(falar_cod, TextToSpeech.QUEUE_FLUSH, null);
        opcao_fala = 0;
        isTTSSpeaking();

    }
    private void speakWords1() {
        myTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
        opcao_fala = 1;
        isTTSSpeaking();

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
    }

    public void isTTSSpeaking() {

        final Handler h = new Handler();

        Runnable r = new Runnable() {

            public void run() {

                if (!myTTS.isSpeaking()) {
                    onTTSSpeechFinished();
                } else {
                    h.postDelayed(this, 100);
                }
            }
        };

        h.postDelayed(r, 1000);
    }

    public void onTTSSpeechFinished() {
        System.out.println("acabou de falar");
        System.out.println("Shutdown : " + myTTS);

        if(opcao_fala ==0)
        {

        }
        if(opcao_fala ==1)
        {

        }
        if(opcao_fala ==2)
        {


        }

    }
}





