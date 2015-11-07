package com.jj.mysimpleplayer;

public class Helpers {

    public static String getFormattedTime(int progress) {
        int seconds = 1000;
        int minutes = seconds * 60;
        int hours = minutes * 60;

        int elapsedHours = progress / hours;
        progress = progress % hours;

        int elapsedMinutes = progress / minutes;
        progress = progress % minutes;

        int elapsedSeconds = progress / seconds;

        return elapsedHours != 0 ? String.format("%d:%d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds)
                : String.format("%d:%02d", elapsedMinutes, elapsedSeconds);
    }
}
