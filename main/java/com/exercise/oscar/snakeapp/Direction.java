package com.exercise.oscar.snakeapp;

public enum Direction {
    UP(0),
    RIGHT(1),
    DOWN(2),
    LEFT(3);

    private  final int directionCode;

    public int directionCode(){
        return directionCode;
    }
    Direction(int directioncode){
        this.directionCode=directioncode;
    }
    public boolean compatibleWith(Direction newdirection){
        if(this.directionCode!=newdirection.directionCode){
            return true;
        }else{
            return false;
        }
    }
}
