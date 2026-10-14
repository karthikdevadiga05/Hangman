package com.example.hang;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DailyChallenge {
    private DailyChallenge() { }

    public static String todayId() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}