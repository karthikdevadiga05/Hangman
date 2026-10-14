package com.example.hang;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class WeeklyScore {

    private WeeklyScore() { }

    /** Id of the current week = the date of this week's Monday, e.g. "2026-10-12". */
    public static String currentWeekId() {
        Calendar c = Calendar.getInstance();
        int diff = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Monday = 0 ... Sunday = 6
        c.add(Calendar.DAY_OF_YEAR, -diff);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    /** Adds points to the user's weekly score, resetting it first if a new week has started. */
    public static void addPoints(String uid, int earned) {
        if (uid == null || earned <= 0) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("users").document(uid);
        String week = currentWeekId();

        db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref);
            Long stored = snap.getLong("weeklyScore");
            long current = (stored != null) ? stored : 0;

            if (!week.equals(snap.getString("weekId"))) {
                current = 0; // new week, start fresh
            }

            Map<String, Object> data = new HashMap<>();
            data.put("weekId", week);
            data.put("weeklyScore", current + earned);
            tx.set(ref, data, SetOptions.merge());
            return null;
        });
    }
}