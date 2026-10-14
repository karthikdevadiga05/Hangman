package com.example.hang.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WordBank {

    public static class WordHint {
        public final String word;
        public final String hint;

        public WordHint(String word, String hint) {
            this.word = word;
            this.hint = hint;
        }
    }

    private static final Map<String, WordHint[]> WORD_CATEGORIES = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        WORD_CATEGORIES.put("ANIMALS", new WordHint[]{
                new WordHint("GIRAFFE", "Long neck tall animal"),
                new WordHint("PELICAN", "Large water bird with a pouch beak"),
                new WordHint("PANTHER", "Fierce dark wild big cat"),
                new WordHint("DOLPHIN", "Intelligent ocean mammal that leaps"),
                new WordHint("LEOPARD", "Spotted wild predator cat"),
                new WordHint("CHEETAH", "Fastest land animal on earth"),
                new WordHint("FALCON", "Swift predatory bird with sharp talons"),
                new WordHint("HAMSTER", "Small rodent pet with cheek pouches"),
                new WordHint("BUFFALO", "Large horned wild bovine mammal"),
                new WordHint("PENGUIN", "Flightless aquatic bird in cold ice")
        });

        WORD_CATEGORIES.put("OBJECTS", new WordHint[]{
                new WordHint("LANTERN", "Portable light source with a flame"),
                new WordHint("MIRROR", "Reflective glass surface"),
                new WordHint("COMPASS", "Navigation tool that points north"),
                new WordHint("PENDULUM", "Hanging swinging weight"),
                new WordHint("SCISSORS", "Cutting tool with two sharp blades"),
                new WordHint("WHISTLE", "Small instrument making high sound"),
                new WordHint("TRUMPET", "Brass musical wind instrument"),
                new WordHint("PACKAGE", "Wrapped box or parcel"),
                new WordHint("PADLOCK", "Detachable lock with a shackle"),
                new WordHint("CRYSTAL", "Clear shiny mineral stone")
        });

        WORD_CATEGORIES.put("CELEBRITIES", new WordHint[]{
                new WordHint("EINSTEIN", "Physicist famous for theory of relativity"),
                new WordHint("PICASSO", "Famous cubist painter"),
                new WordHint("BEETHOVEN", "Deaf classical music composer"),
                new WordHint("CHAPLIN", "Silent comedy movie icon"),
                new WordHint("MOZART", "Child prodigy classical composer"),
                new WordHint("DARWIN", "Scientist behind theory of evolution"),
                new WordHint("NEWTON", "Scientist who discovered gravity"),
                new WordHint("TESLA", "Inventor of alternating electric current"),
                new WordHint("DISNEY", "Creator of Mickey Mouse and animations"),
                new WordHint("SHAKESPEARE", "Famous English playwright and poet")
        });

        WORD_CATEGORIES.put("PROFESSIONS", new WordHint[]{
                new WordHint("DOCTOR", "Medical professional who heals sick patients"),
                new WordHint("TEACHER", "Educator who guides students in school"),
                new WordHint("ASTRONAUT", "Space traveler exploring the stars"),
                new WordHint("ENGINEER", "Creator and builder of machines and bridges"),
                new WordHint("LAWYER", "Legal expert who defends in court"),
                new WordHint("PILOT", "Aviator who flies airplanes in the sky"),
                new WordHint("POLICE", "Law enforcement protector of peace"),
                new WordHint("SOLDIER", "Brave defender serving in military"),
                new WordHint("NURSE", "Healthcare caregiver tending to patients"),
                new WordHint("ARTIST", "Creative painter or sculptor of art")
        });
    }

    public static WordHint getRandomWordHint(String category) {
        String key = getCategoryKey(category);
        WordHint[] bank = WORD_CATEGORIES.get(key);
        if (bank != null && bank.length > 0) {
            return bank[RANDOM.nextInt(bank.length)];
        }
        return new WordHint("GIRAFFE", "Long neck tall animal");
    }

    private static String getCategoryKey(String cat) {
        if (cat != null) {
            String upper = cat.toUpperCase();
            if (upper.contains("OBJECT")) {
                return "OBJECTS";
            }
            if (upper.contains("CELEBRITY") || upper.contains("CELEBRITIES")) {
                return "CELEBRITIES";
            }
            if (upper.contains("DOCTOR") || upper.contains("ADULT") || upper.contains("PROFESSION")) {
                return "PROFESSIONS";
            }
        }
        return "ANIMALS";
    }
}