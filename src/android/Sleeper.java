package org.apache.cordova.vibration;

public class Sleeper {

    private Sleeper() {

    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}