package org.izv.aad.appwebview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

/*RECOGER DATOS DE USUARIO Y ENTRAR, SI YA LOS HA REGISTRADO ANTES ENTRA DIRECTAMENTE*/
//USUARIO: aad passA: 1234

public class ActividadPrincipal extends AppCompatActivity {
    private WebView webView;
    private static final String TAG = "MITAG";
    private String url = "http://www.juntadeandalucia.es/averroes/centros-tic/18700098/moodle2/login/index.php";
    private String URL = url;   //URL CONSTANTE
    private String javaScript = "";
    private EditText username, pass;
    private Button login;
    private static String user = "",password = "";
    private boolean verified = false, visitada = false, visitada2 = false;
    private SharedPreferences shPref;
    private WebAppInterface webAppInterface;

    /*Añade los valores de la clase 'user' y 'password' a las ShPref, si comprobados=true, las guarda en las checkeadas
    * si no, las guarda en unas 'temporales' (esperando a que se verifiquen)*/
    public void addPref(String username, String password){
        SharedPreferences shPref = this.getSharedPreferences(getString(R.string.archivoSharedPreferences),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shPref.edit();
        editor.putString(getString(R.string.username), username);
        editor.putString(getString(R.string.password), password);
        editor.commit();
    }

    public void checkIfExists(boolean exists){
        SharedPreferences shPref = this.getSharedPreferences(getString(R.string.archivoSharedPreferences),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shPref.edit();
        editor.putBoolean(getString(R.string.exists), exists);
        editor.commit();
    }

    public String getUser(){
        SharedPreferences shPref = getSharedPreferences(getString(R.string.archivoSharedPreferences),Context.MODE_PRIVATE);
        String user = shPref.getString(getString(R.string.username),"");
        return user;
    }

    public String getPassword(){
        SharedPreferences shPref = getSharedPreferences(getString(R.string.archivoSharedPreferences),Context.MODE_PRIVATE);
        String password = shPref.getString(getString(R.string.password),"");
        return password;
    }

    //INICIALIZA LOS ELEMENTOS
    public void init(){
        webView = findViewById(R.id.wvMoodle);  //init()
        username = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        login = findViewById(R.id.login);
        webView.getSettings().setJavaScriptEnabled(true);   //enableJava()
        webView.loadUrl(url);
        webAppInterface = new WebAppInterface();
        webView.addJavascriptInterface(webAppInterface, "android");
        shPref = getPreferences(Context.MODE_PRIVATE);
        user = shPref.getString("user",null);
        password = shPref.getString("password",null);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_principal);

        init();
        addPref(username.toString(),pass.toString());
        //CLIENTE WEB
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //PAGINA PRINCIPAL + NO VERIFICADO
                if(paginaPrincipal(url) && !verified) {//CREAMOS UNA ALERTA
                    if(!visitada){
                        /*LayoutInflater inflater = LayoutInflater.from(ActividadPrincipal.this);
                        //Inflamos el layout del alertdialog
                        final View customView = inflater.inflate(R.layout.dialog_signin, null);
                        final TextView username = (EditText) customView.findViewById(R.id.username);
                        final TextView pass = (EditText) customView.findViewById(R.id.password);
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ActividadPrincipal.this)
                                .setTitle(R.string.datos)
                                .setView(customView)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    //Capturamos los valores de los campos de texto en las variables de la clase
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        user = username.getText().toString();
                                        password = pass.getText().toString();
                                        addPref(false); //NO COMPROBADO
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null);

                        dialog.show();*/
                        //CREAMOS SENTENCIA JAVASCRIPT QUE ESCRIBA EN LOS CAMPOS DEL LOGIN

                        login.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                user = username.getText().toString();
                                password = pass.getText().toString();
                                javaScript = " var bt = document.getElementById('loginbtn');\n" +
                                        " var name = document.getElementById('username');\n" +
                                        " var password = document.getElementById('password');\n" +
                                        //Utilizo e.prevent default para modificar el comportamiento por defecto del formulario.
                                        //En caso de no utilizarlo, la página se logueará antes de llegar a enviar los datos a la interfaz.
                                        "       e.preventDefault();\n"+
                                        "       var usuario = name.value;\n" +
                                        "       var clave = password.value;\n" +
                                        "       android.sendData(usuario,clave);\n" +
                                        "       document.getElementById('login').submit();\n" +
                                        "   });" +
                                        " }\n"+
                                        "   boton.addEventListener('click', function(e){\n " +
                                        "       e.preventDefault();\n"+
                                        "       name.value = '"+getUser()+"';\n"+
                                        "       password.value = '"+getPassword()+"';\n" +
                                        "       var usuario = name.value;\n" +
                                        "       var clave = password.value;\n" +
                                        "       android.sendData(usuario,clave);\n" +
                                        "       document.getElementById('login').submit();\n" +
                                        "   });" +
                                        "   bt.click();"+
                                        " }";
                                Log.v(TAG, javaScript + "");
                            }
                        });
                    }
                    visitada = true;
                }

                //PAGINA PRINCIPAL + VERIFICADO
                if(!paginaPrincipal(url)) {
                    checkIfExists(true);
                }else{
                    checkIfExists(false);
                }

                webView.loadUrl("javascript: " + javaScript);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

        });
    }

    //INDICA SI NOS ENCONTRAMOS EN LA PAGINA PRINCIPAL
    public boolean paginaPrincipal(String url){
        boolean esta = false;

        if(URL.compareTo(url) == 0)
            esta = true;

        return esta;
    }

    //DEVUELVE CODIGO JAVASCRIPT: INSERTA VALORES EN LOS CAMPOS
    public String putFields(String nombre, String pass){
        return "document.getElementById('username').value = "+nombre+";"+
                "document.getElementById('password').value = "+pass+";"+
                "document.getElementById('loginbtn').click()";
    }

    //DEVUELVE CODIGO JAVASCRIPT: RECUPERA VALORES DEL HTML MEDIANTE JAVASCRIPTINTERFACE
    public String saveFromJavaScript(){
        return "var boton = document.getElementById('loginbtn')" +
                "boton.addEventListener('click', function(){" +
                "var usuario = document.getElementById('usuario').value" +
                "var password = document.getElementById('clave').value" +
                "android.sendnombre(usuario,password,true)" +
                "})";
    }
}
