package org.izv.aad.appwebview;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    String nombre, passa;
    boolean existe;

    WebAppInterface(){
    }

    //public Context getmContext() {
        //return mContext;
    //}

    public String getnombre() {
        return nombre;
    }

    public String getpassa() {
        return passa;
    }

    public boolean getexiste() {
        return existe;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPass(String passa) {
        this.passa = passa;
    }

    public void setexiste(boolean existe) {
        this.existe = existe;
    }

    @JavascriptInterface
    public void exists(boolean existe){
        this.existe = existe;
    }

    @JavascriptInterface
    public void sendnombre(String nombre, String pass, boolean existe) {
        //Get the string value to process
        setNombre(nombre);
        setPass(pass);
    }
}
