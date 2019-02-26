package com.exercise.oscar.snakeapp;

import android.app.Activity;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

public class SnakeActivity extends Activity {

    // Declaración de SnakeApp
    private SnakeApp grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Conseguimos las dimensiones en pixeles de la pantalla
        Display display = getWindowManager().getDefaultDisplay();

        // Pasar el resultado a un objeto Point
        Point size = new Point();
        display.getSize(size);

        // Creamos la instancia de la clase SnakeEngine
        grid = new SnakeApp(this, size);

        // Hacemos que sea la vista principal de la Activity
        setContentView(grid);
    }

    // Iniciar el hilo con SnakeEngine
    @Override
    protected void onResume() {
        super.onResume();
        grid.resume();
    }

    // Parar el hilo con SnakeEngine
    @Override
    protected void onPause() {
        super.onPause();
        grid.pause();
    }
}
