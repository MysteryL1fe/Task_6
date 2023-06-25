package khanin.dmitrii.hashMap;

import khanin.dmitrii.hashMap.UI.MainFrame;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MainFrame main = new MainFrame();
        Scanner scanner = new Scanner(System.in);
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        }).run();
    }
}