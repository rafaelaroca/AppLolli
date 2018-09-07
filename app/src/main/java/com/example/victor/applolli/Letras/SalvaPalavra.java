package com.example.victor.applolli.Letras;

import android.app.Application;

public class SalvaPalavra extends Application {

    private String someVariable;
    public String getSomeVariable() {
        return someVariable;
    }
    public void setSomeVariable(String someVariable) {
        this.someVariable = someVariable;
    }
}
