package com.exercise.oscar.snakeapp;

public class Node {

    // Valores X e Y del Nodo en la cuadrícula
    private  int x;
    private  int y;

    //Contructores
    public Node(){}
    public Node(int x,int y){
        this.x=x;
        this.y=y;
    }

    //GET & SET
    public int getX(){
        return x;
    }
    public int getY() {
        return y;
    }
    public void setX(int x){
         this.x=x;
    }
    public void setY(int y){
        this.y=y;
    }

}
