package khanin.dmitrii.hashMap;

import khanin.dmitrii.hashMap.UI.MainFrame;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int main = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        }).run();
    }
}