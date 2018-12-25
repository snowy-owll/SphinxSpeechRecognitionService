package ru.snowy_owl.testservicesphinx;

import java.util.ArrayList;

public class LastMessagesList extends ArrayList<String> {
    private int _maxSize;

    public LastMessagesList(int size) {
        _maxSize = size;
    }

    public boolean add(String str) {
        boolean r = super.add(str);
        if(size()>_maxSize){
            removeRange(0, size()-_maxSize-1);
        }
        return r;
    }

    public String toString(){
        StringBuilder rez= new StringBuilder();
        for(String str : this){
            rez.append(str).append("\n");
        }
        rez.deleteCharAt(rez.length()-1);
        return rez.toString();
    }
}
