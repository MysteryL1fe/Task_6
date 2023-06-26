package khanin.dmitrii.hashMap;

import khanin.dmitrii.hashMap.UI.MainFrame;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        }).run();
    }
}