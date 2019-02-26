package com.exercise.oscar.snakeapp;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;


class SnakeApp extends SurfaceView implements Runnable {


    // Utilizamos un solo thread para la aplicación
    private Thread thread = null;

    // Context es una referencia al MainActivity
    private Context context;

    // Para usar efectos de sonido
    private SoundPool soundPool;
    private int sound_comer = -1;
    private int sound_crash = -1;


    //Direccion de la cabeza, inicia el movimiento hacia la derecha
    private Direction direction = Direction.RIGHT;


    // Controla el tiempo de juego
    private long timestart;
    private long time;

    // Usamos un Nodo como comida
    private Node food;


    // Longitud X e Y de la pantalla actual
    private int pantallaX;
    private int pantallaY;

    // Número de bloques que se divide el ancho de la pantalla
    private final int numBloquesX = 70;
    // Número de bloques que se divide el largo de la pantalla
    private int numBloquesY;

    // Tamaño de los bloques en los que se dividirá la pantalla
    private int tamBloque;

    // Limites X e Y de la zona jugable
    private int limiteX_izq;
    private int limiteX_dch;
    private int limiteY_sup;
    private int limiteY_inf;


    // Controla el tiempo de pausa entre updates
    private long nextFrameTime;

    // Actualiza el juego 15 veces por segundo
    private final long FPS = 15;

    // 1 segundo = 1000millis
    private final long MILLIS_PER_SECOND = 1000;


    // Puntuacion del jugador
    private int puntuacion;

    // Localización del snake en la cuadrícula en todas sus partes, cada Nodo es una parte de la serpiente
    private LinkedList<Node> snake;


    // Si se esta jugando o no
    private volatile boolean estaJugando;


    // Lienzo para pintar el fondo, snake y la comida
    private Canvas canvas;

    // Necesario para usar el lienzo
    private SurfaceHolder surfaceHolder;

    // Para pintar sobre el lienzo
    private Paint paint;



    public SnakeApp(Context context, Point size) {
        super(context);

        this.context = context;

        //Se insertan los tamaños del dispositivo
        pantallaX = size.x;
        pantallaY = size.y;


        // Se calcula los pixeles que tendrá cada bloque basandonos en los bloques que queremos que tenga de ancho
        tamBloque = pantallaX / numBloquesX;
        // Se calcula en base al tamaño en pixeles de cada bloque, cuantos bloques hay en el largo de la pantalla
        numBloquesY = pantallaY / tamBloque;


        // Limites de la zona jugable
        limiteX_izq = numBloquesX - ((int)(numBloquesX*0.93));
        limiteX_dch = numBloquesX - ((int)(numBloquesX*0.05));
        limiteY_sup = numBloquesY - ((int)(numBloquesY*0.9));
        limiteY_inf = numBloquesY - ((int)(numBloquesY*0.1));


        // Preparacion de efectos de sonido
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            // Se crean las clases necesarias para manejarlos

            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Se preparan los sonidos en memoria
            descriptor = assetManager.openFd("EatSound.wav");
            sound_comer = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("Death_sound.wav");
            sound_crash = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Error
        }



        // Se inician los objetos para pintar
        surfaceHolder = getHolder();
        paint = new Paint();


        // Inicia el juego
        newGame();
    }
    @Override
    public void run() {

        while (estaJugando) {

            // Actualiza 15 veces por segundo
            if(updateRequired()) {
                update();
                draw();
            }

        }
    }


    //Pausa el juego
    public void pause() {
        estaJugando = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }


    //Reanuda el juego
    public void resume() {
        estaJugando = true;
        thread = new Thread(this);
        thread.start();
    }


    public void newGame() {


        snake=new LinkedList<>();
        //Iniciamos el tiempo de juego
        timestart=System.currentTimeMillis();

        direction = Direction.RIGHT;

        // Se empieza con una serpiente de longitud 3 (No queremos que sea una cabeza flotante la pobre)
        for (int i=0;i<3;i++) {
            int x = (numBloquesX / 2)- i;
            int y = (numBloquesY / 2);
            snake.add(new Node(x,y));
        }

        // Se añade comida
        spawnFood();

        // Resetea la puntuación
        puntuacion = 0;

        // Se prepara el siguiente tiempo para que salte el update
        nextFrameTime = System.currentTimeMillis();
    }
    public void spawnFood() {

        // La comida aparece en una zona aleatoria dentro de la zona jugable y además lejos del limite de la zona (para jugar con más facilidad)

        food=new Node();
        Random random = new Random();
        int X = random.nextInt((limiteX_dch - 3 ) - limiteX_izq ) + limiteX_izq + 1 ;
        int Y = random.nextInt((limiteY_inf - 3 ) - limiteY_sup ) + limiteY_sup + 1 ;
        food.setX(X);
        food.setY(Y);

    }
    private void eatFood(Node Last){

        // Se come la comida y crece uno de longitud
        snake.addLast(Last);

        // Se añade comida
        spawnFood();
        // Se aumenta en uno la puntación
        puntuacion = puntuacion + 1;
        // Se utiliza el efecto de sonido para comer
        soundPool.play(sound_comer, 1, 1, 0, 0, 1);
    }
    private Node moveSnake(){
        // Se mueve el cuerpo
        Node first=snake.getFirst();

        // Se mueve la cabeza hacia la direccion elegida
        switch (direction) {
            case UP:
                snake.addFirst(new Node(first.getX(),first.getY()+1));
                break;

            case RIGHT:
                snake.addFirst(new Node(first.getX()+1,first.getY()));
                break;

            case DOWN:
                snake.addFirst(new Node(first.getX(),first.getY()-1));
                break;

            case LEFT:
                snake.addFirst(new Node(first.getX()-1,first.getY()));
                break;
        }
        Node Last=snake.getLast();
        snake.removeLast();
        return Last;
    }
    public void update() {

        // Se mueve la serpiente
        Node Last = moveSnake();
        // Revisa si la cabeza toca comida y si lo es, se la come
        if (snake.getFirst().getX() == food.getX() && snake.getFirst().getY() == food.getY()) {
            eatFood(Last);
        }

        //Se actualiza el tiempo de juego
        time=System.currentTimeMillis()-timestart;
        time= TimeUnit.MILLISECONDS.toSeconds(time);

        //Si se muere vuelve a empezar una nueva partida
        if (detectarMuerte()) {
            // Se utiliza el efecto de sonido de muerte

            soundPool.play(sound_crash, 1, 1, 0, 0, 1);

            newGame();
        }
    }
    private boolean detectarMuerte(){

        // Si muere la serpiente
        boolean dead = false;

        // Muere si toca alguna pared
        if (snake.getFirst().getX() == (limiteX_izq-1)) dead = true;
        if (snake.getFirst().getX() >= (limiteX_dch-1)) dead = true;
        if (snake.getFirst().getY() == (limiteY_sup-1)) dead = true;
        if (snake.getFirst().getY() == (limiteY_inf-1)) dead = true;

        // Muere si se come a si mismo
        Node head=snake.pollFirst();
        for(Node point:snake){
            if (point.getX()==head.getX() && point.getY()==head.getY()){
                dead=true;
            }
        }
        snake.addFirst(head);

        return dead;
    }

    public void draw() {

        // Se preparan los elementos que se van a pintar

        //Si la superficie es valida
        if (surfaceHolder.getSurface().isValid()) {

            // Se bloquea el lienzo hasta preparar lo necesario
            canvas = surfaceHolder.lockCanvas();

            // Se crea un rect que servirá como molde para pintar fondos, comida y serpiente
            Rect rect = new Rect(0,0, pantallaX,pantallaY);

            // Prepara fondo exterior
            paint.setColor(Color.argb(255, 123, 255, 255));
            Bitmap bitmapJugable,bitmapEntorno;
            bitmapEntorno = BitmapFactory.decodeResource(this.getResources(), R.drawable.color_entorno);

            // Prepara fondo jugable
            bitmapJugable = BitmapFactory.decodeResource(this.getResources(), R.drawable.pantalla_gameboy);


            // Pinta fondo exterior
            canvas.drawBitmap(bitmapEntorno,null,rect,paint);

            // Cambio de molde y pinta fondo jugable
            rect.set((limiteX_izq *tamBloque), (limiteY_sup*tamBloque), ((limiteX_dch -1)*tamBloque), ((limiteY_inf-1)*tamBloque));
            canvas.drawBitmap(bitmapJugable,null,rect,paint);

            // Poner color de marcador a blanco
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Se pone la escala del texto y se pinta
            paint.setTextSize(70);
            canvas.drawText("Puntuacion : " + puntuacion, 450, tamBloque*3, paint);
            canvas.drawText("Tiempo : " + time,1050,tamBloque*3,paint);

            // Contador para pintar la cabeza y el cuerpo de manera distinta
            int i=0;

            // Se prepara la creacion de la serpiente y su color
            for (Node point:snake){
                // Se va pintando un bloque de la serpiente cada vez
                if(i==0){
                    // Pinta solo la cabeza
                    paint.setColor(Color.argb(255, 0, 100, 0));
                    canvas.drawRect(point.getX() * tamBloque,
                           (point.getY() * tamBloque),
                           (point.getX() * tamBloque) + tamBloque,
                           (point.getY() * tamBloque) + tamBloque,
                           paint);
                   i++;
                }else {
                    // Pinta el cuerpo
                    paint.setColor(Color.argb(255, 0, 0, 0));
                    canvas.drawRect(point.getX() * tamBloque,
                           (point.getY() * tamBloque),
                           (point.getX() * tamBloque) + tamBloque,
                           (point.getY() * tamBloque) + tamBloque,
                           paint);
                }
             }

            // Pinta la comida con el color deseado
            paint.setColor(Color.RED);
            canvas.drawRect(food.getX() * tamBloque,
                    (food.getY() * tamBloque),
                    (food.getX() * tamBloque) + tamBloque,
                    (food.getY() * tamBloque) + tamBloque,
                    paint);

            // Desbloquea el lienzo y revela lo que se ha pintado en este frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
    public boolean updateRequired() {

        // Actualiza el proximo frame si es necesario
        if(nextFrameTime <= System.currentTimeMillis()){

            // Se prepara el siguiente tiempo en el que se debe actualizar
            nextFrameTime =System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;

            // Devuelve true para que se pueda ejecutar el update y el draw
            return true;
        }

        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        // Se prepara el evento cuando se toca la pantalla
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            // Cuando se levanta el dedo
            case MotionEvent.ACTION_UP:
                // Si se ha pulsado en la mitad derecha  de la pantalla, se va cambiando la posición hacia la dcha.
                if (motionEvent.getX() >= pantallaX / 2) {

                    switch(direction){
                        case UP:
                            direction = Direction.LEFT;
                            break;
                        case RIGHT:
                            direction = Direction.UP;
                            break;
                        case DOWN:
                            direction = Direction.RIGHT;
                            break;
                        case LEFT:
                            direction = Direction.DOWN;
                            break;


                    }
                    // Si se ha pulsado en la mited izquierda de la pantalla, se va cambiando la posición hacia la izq .

                } else {
                    switch(direction){
                        case UP:
                            direction = Direction.RIGHT;
                            break;
                        case LEFT:
                            direction = Direction.UP;
                            break;
                        case DOWN:
                            direction = Direction.LEFT;
                            break;
                        case RIGHT:
                            direction = Direction.DOWN;
                            break;
                    }
                }
        }
        return true;
    }
}
