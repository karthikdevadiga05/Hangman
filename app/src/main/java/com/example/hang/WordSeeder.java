//package com.example.hang;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.widget.Toast;
//
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.WriteBatch;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * One-time helper that adds 300 new words (10 categories x 3 difficulties x 10)
// * to the "words" collection. Every document gets an auto ID and the same fields
// * as your existing words: word, description, category, difficulty, length.
// * Words already in the collection are skipped, and it only runs once per install.
// */
//public final class WordSeeder {
//
//    private static final String PREFS = "HangmanPrefs";
//    private static final String KEY_DONE = "words_seeded_v1";
//
//    private WordSeeder() { }
//
//    // {word, description, category, difficulty}
//    private static final String[][] DATA = {
//            {"zebra", "Striped African animal that looks like a horse", "Animals", "Easy"},
//            {"panda", "Black and white bear that eats bamboo", "Animals", "Easy"},
//            {"camel", "Desert animal with humps", "Animals", "Easy"},
//            {"horse", "Animal you can ride and race", "Animals", "Easy"},
//            {"sheep", "Farm animal that gives wool", "Animals", "Easy"},
//            {"eagle", "Large bird of prey with sharp eyes", "Animals", "Easy"},
//            {"rabbit", "Long-eared animal that hops", "Animals", "Easy"},
//            {"monkey", "Playful animal that climbs trees", "Animals", "Easy"},
//            {"shark", "Fierce fish with sharp teeth", "Animals", "Easy"},
//            {"whale", "Largest animal in the ocean", "Animals", "Easy"},
//            {"giraffe", "Tallest land animal with a long neck", "Animals", "Medium"},
//            {"dolphin", "Intelligent sea mammal that clicks and whistles", "Animals", "Medium"},
//            {"penguin", "Flightless bird that waddles on ice", "Animals", "Medium"},
//            {"leopard", "Spotted big cat that climbs trees", "Animals", "Medium"},
//            {"gorilla", "Largest ape of the African forests", "Animals", "Medium"},
//            {"hamster", "Small pet rodent that stuffs its cheeks", "Animals", "Medium"},
//            {"squirrel", "Bushy-tailed animal that stores nuts", "Animals", "Medium"},
//            {"buffalo", "Large horned animal seen on Indian farms", "Animals", "Medium"},
//            {"octopus", "Sea creature with eight arms", "Animals", "Medium"},
//            {"peacock", "Bird famous for its colorful fan of feathers", "Animals", "Medium"},
//            {"rhinoceros", "Thick-skinned animal with a horn on its nose", "Animals", "Hard"},
//            {"hippopotamus", "Huge river animal that spends the day in water", "Animals", "Hard"},
//            {"chimpanzee", "Intelligent ape closely related to humans", "Animals", "Hard"},
//            {"grasshopper", "Green insect that jumps and chirps", "Animals", "Hard"},
//            {"woodpecker", "Bird that drills into tree trunks", "Animals", "Hard"},
//            {"hummingbird", "Tiny bird that hovers while feeding", "Animals", "Hard"},
//            {"salamander", "Amphibian that looks like a lizard", "Animals", "Hard"},
//            {"rattlesnake", "Venomous snake with a rattling tail", "Animals", "Hard"},
//            {"caterpillar", "Larva that turns into a butterfly", "Animals", "Hard"},
//            {"wildebeest", "African antelope that migrates in huge herds", "Animals", "Hard"},
//            {"chair", "You sit on it", "Objects", "Easy"},
//            {"table", "Furniture with a flat top for meals", "Objects", "Easy"},
//            {"clock", "Shows the time on a wall", "Objects", "Easy"},
//            {"spoon", "Used to eat soup", "Objects", "Easy"},
//            {"brush", "Used to clean teeth or paint", "Objects", "Easy"},
//            {"lamp", "Gives light on a desk", "Objects", "Easy"},
//            {"mirror", "Shows your reflection", "Objects", "Easy"},
//            {"pencil", "Writing tool made of graphite", "Objects", "Easy"},
//            {"bottle", "Container for water or milk", "Objects", "Easy"},
//            {"basket", "Woven container for carrying things", "Objects", "Easy"},
//            {"scissors", "Cuts paper with two blades", "Objects", "Medium"},
//            {"backpack", "Bag carried on your shoulders", "Objects", "Medium"},
//            {"blanket", "Warm cover for the bed", "Objects", "Medium"},
//            {"envelope", "Holds a letter for the post", "Objects", "Medium"},
//            {"notebook", "Pages bound together for writing", "Objects", "Medium"},
//            {"suitcase", "Bag packed for travelling", "Objects", "Medium"},
//            {"calendar", "Shows days and months", "Objects", "Medium"},
//            {"wardrobe", "Large cupboard for clothes", "Objects", "Medium"},
//            {"mattress", "Soft bed you sleep on", "Objects", "Medium"},
//            {"curtain", "Cloth hung over a window", "Objects", "Medium"},
//            {"flashlight", "Handheld battery torch", "Objects", "Hard"},
//            {"screwdriver", "Tool that turns screws", "Objects", "Hard"},
//            {"toothbrush", "Cleans teeth every morning", "Objects", "Hard"},
//            {"wheelbarrow", "Single-wheeled cart for garden loads", "Objects", "Hard"},
//            {"thermometer", "Measures temperature", "Objects", "Hard"},
//            {"paintbrush", "Used by an artist to apply color", "Objects", "Hard"},
//            {"sunglasses", "Protect your eyes from bright light", "Objects", "Hard"},
//            {"microscope", "Makes tiny things look larger", "Objects", "Hard"},
//            {"typewriter", "Old machine that prints letters when keys are pressed", "Objects", "Hard"},
//            {"dishwasher", "Machine that cleans plates", "Objects", "Hard"},
//            {"dhoni", "Former Indian cricket captain nicknamed Captain Cool", "Celebrities", "Easy"},
//            {"kohli", "Indian batter known as the Run Machine", "Celebrities", "Easy"},
//            {"salman", "Bollywood star famous for Dabangg", "Celebrities", "Easy"},
//            {"ajith", "Tamil film star known as Thala", "Celebrities", "Easy"},
//            {"messi", "Argentine footballer who won the 2022 World Cup", "Celebrities", "Easy"},
//            {"mozart", "Child prodigy composer from Austria", "Celebrities", "Easy"},
//            {"eminem", "American rapper known as Slim Shady", "Celebrities", "Easy"},
//            {"pele", "Brazilian football legend with three World Cups", "Celebrities", "Easy"},
//            {"neymar", "Brazilian forward famous for dribbling", "Celebrities", "Easy"},
//            {"sachin", "Cricket's Master Blaster", "Celebrities", "Easy"},
//            {"tendulkar", "Indian batting legend with 100 international centuries", "Celebrities", "Medium"},
//            {"einstein", "Physicist who developed the theory of relativity", "Celebrities", "Medium"},
//            {"beethoven", "German composer who kept composing despite deafness", "Celebrities", "Medium"},
//            {"mohanlal", "Malayalam superstar known as the Complete Actor", "Celebrities", "Medium"},
//            {"aishwarya", "Former Miss World and Indian actress", "Celebrities", "Medium"},
//            {"priyanka", "Indian actress who became a global star", "Celebrities", "Medium"},
//            {"deepika", "Bollywood actress, star of Padmaavat", "Celebrities", "Medium"},
//            {"shahrukh", "Bollywood's King Khan", "Celebrities", "Medium"},
//            {"hrithik", "Bollywood actor known for dancing and Krrish", "Celebrities", "Medium"},
//            {"ranveer", "Energetic Bollywood actor from Bajirao Mastani", "Celebrities", "Medium"},
//            {"kamalhaasan", "Versatile Tamil actor nicknamed Ulaganayagan", "Celebrities", "Hard"},
//            {"chiranjeevi", "Telugu film megastar", "Celebrities", "Hard"},
//            {"nawazuddin", "Bollywood actor known for Gangs of Wasseypur", "Celebrities", "Hard"},
//            {"schwarzenegger", "Bodybuilder turned Terminator actor", "Celebrities", "Hard"},
//            {"shakespeare", "English playwright who wrote Hamlet", "Celebrities", "Hard"},
//            {"michelangelo", "Italian artist who painted the Sistine Chapel ceiling", "Celebrities", "Hard"},
//            {"muralitharan", "Sri Lankan spinner with 800 Test wickets", "Celebrities", "Hard"},
//            {"vivekananda", "Indian monk famed for his 1893 Chicago speech", "Celebrities", "Hard"},
//            {"dharmendra", "Veteran Bollywood actor of Sholay", "Celebrities", "Hard"},
//            {"jacqueline", "Sri Lankan-born Bollywood actress", "Celebrities", "Hard"},
//            {"bread", "Baked from flour and eaten with butter", "Food", "Easy"},
//            {"pizza", "Round baked dish with cheese on top", "Food", "Easy"},
//            {"pasta", "Italian dish of noodles", "Food", "Easy"},
//            {"rice", "Staple grain eaten with curry", "Food", "Easy"},
//            {"honey", "Sweet food made by bees", "Food", "Easy"},
//            {"cheese", "Dairy food made from milk", "Food", "Easy"},
//            {"mango", "King of fruits", "Food", "Easy"},
//            {"grape", "Small fruit that grows in bunches", "Food", "Easy"},
//            {"lemon", "Sour yellow citrus fruit", "Food", "Easy"},
//            {"banana", "Yellow fruit with a peel", "Food", "Easy"},
//            {"biryani", "Spiced rice dish layered with meat", "Food", "Medium"},
//            {"spaghetti", "Long thin strands of Italian pasta", "Food", "Medium"},
//            {"pancake", "Flat round cake flipped in a pan", "Food", "Medium"},
//            {"popcorn", "Corn that pops when heated", "Food", "Medium"},
//            {"coconut", "Tropical fruit with white flesh and water", "Food", "Medium"},
//            {"omelette", "Beaten eggs cooked in a pan", "Food", "Medium"},
//            {"broccoli", "Green vegetable shaped like a tiny tree", "Food", "Medium"},
//            {"spinach", "Leafy green vegetable", "Food", "Medium"},
//            {"dumpling", "Dough parcel filled and steamed", "Food", "Medium"},
//            {"cucumber", "Long green cooling salad vegetable", "Food", "Medium"},
//            {"strawberry", "Red heart-shaped berry with seeds outside", "Food", "Hard"},
//            {"watermelon", "Large green fruit with red juicy flesh", "Food", "Hard"},
//            {"cauliflower", "White vegetable that looks like a flower", "Food", "Hard"},
//            {"cheesecake", "Creamy dessert on a biscuit base", "Food", "Hard"},
//            {"gingerbread", "Spiced biscuit shaped like a little man", "Food", "Hard"},
//            {"marshmallow", "Soft fluffy sweet toasted over fire", "Food", "Hard"},
//            {"pomegranate", "Fruit full of red juicy seeds", "Food", "Hard"},
//            {"cappuccino", "Italian coffee topped with milk foam", "Food", "Hard"},
//            {"mozzarella", "Soft Italian cheese used on pizza", "Food", "Hard"},
//            {"blackberry", "Dark purple berry that grows on thorny bushes", "Food", "Hard"},
//            {"chess", "Board game with kings and queens", "Sports", "Easy"},
//            {"tennis", "Racket sport played over a net", "Sports", "Easy"},
//            {"hockey", "Team sport played with curved sticks", "Sports", "Easy"},
//            {"boxing", "Fighters wear gloves in a ring", "Sports", "Easy"},
//            {"rugby", "Oval ball contact sport", "Sports", "Easy"},
//            {"golf", "Tiny ball hit into distant holes", "Sports", "Easy"},
//            {"squash", "Racket sport played against four walls", "Sports", "Easy"},
//            {"karate", "Japanese martial art", "Sports", "Easy"},
//            {"skiing", "Sliding down snow slopes", "Sports", "Easy"},
//            {"diving", "Jumping headfirst into a pool", "Sports", "Easy"},
//            {"archery", "Shooting arrows at a target", "Sports", "Medium"},
//            {"kabaddi", "Indian team game of tagging and raiding", "Sports", "Medium"},
//            {"cycling", "Racing on two wheels", "Sports", "Medium"},
//            {"wrestling", "Grappling sport on a mat", "Sports", "Medium"},
//            {"baseball", "Bat and ball game with four bases", "Sports", "Medium"},
//            {"swimming", "Racing through water", "Sports", "Medium"},
//            {"shooting", "Aiming at targets with a rifle", "Sports", "Medium"},
//            {"fencing", "Sword sport with masks", "Sports", "Medium"},
//            {"netball", "Team game with a hoop and no dribbling", "Sports", "Medium"},
//            {"bowling", "Rolling a ball to knock down pins", "Sports", "Medium"},
//            {"volleyball", "Spike a ball over a high net", "Sports", "Hard"},
//            {"basketball", "Shoot a ball through a hoop", "Sports", "Hard"},
//            {"gymnastics", "Flips and routines on beams and bars", "Sports", "Hard"},
//            {"weightlifting", "Lifting heavy barbells overhead", "Sports", "Hard"},
//            {"skateboarding", "Riding a board with wheels", "Sports", "Hard"},
//            {"snowboarding", "Gliding down a mountain on a board", "Sports", "Hard"},
//            {"mountaineering", "Climbing tall peaks", "Sports", "Hard"},
//            {"powerlifting", "Squat, bench press and deadlift", "Sports", "Hard"},
//            {"paragliding", "Gliding through the sky under a canopy", "Sports", "Hard"},
//            {"bodybuilding", "Sculpting muscles for competition", "Sports", "Hard"},
//            {"taxi", "Car you hire for a ride", "Vehicles", "Easy"},
//            {"train", "Runs on rails with many coaches", "Vehicles", "Easy"},
//            {"truck", "Large vehicle that carries goods", "Vehicles", "Easy"},
//            {"plane", "Flies passengers through the sky", "Vehicles", "Easy"},
//            {"jeep", "Rugged off-road vehicle", "Vehicles", "Easy"},
//            {"boat", "Floats on water", "Vehicles", "Easy"},
//            {"tram", "City vehicle running on rails along streets", "Vehicles", "Easy"},
//            {"ship", "Large vessel at sea", "Vehicles", "Easy"},
//            {"cycle", "Two-wheeler you pedal", "Vehicles", "Easy"},
//            {"bus", "Carries many passengers along a route", "Vehicles", "Easy"},
//            {"scooter", "Small two-wheeler with a step-through frame", "Vehicles", "Medium"},
//            {"tractor", "Farm vehicle that pulls ploughs", "Vehicles", "Medium"},
//            {"ambulance", "Rushes patients to hospital", "Vehicles", "Medium"},
//            {"bulldozer", "Pushes earth at construction sites", "Vehicles", "Medium"},
//            {"rickshaw", "Three-wheeler hired for short trips", "Vehicles", "Medium"},
//            {"airplane", "Flying machine with wings", "Vehicles", "Medium"},
//            {"sailboat", "Boat powered by wind", "Vehicles", "Medium"},
//            {"limousine", "Long luxury car", "Vehicles", "Medium"},
//            {"forklift", "Lifts pallets in warehouses", "Vehicles", "Medium"},
//            {"minivan", "Family vehicle with sliding doors", "Vehicles", "Medium"},
//            {"locomotive", "Engine that pulls a train", "Vehicles", "Hard"},
//            {"spacecraft", "Vehicle that travels beyond Earth", "Vehicles", "Hard"},
//            {"hovercraft", "Rides on a cushion of air", "Vehicles", "Hard"},
//            {"rollercoaster", "Thrilling ride on twisting tracks", "Vehicles", "Hard"},
//            {"steamroller", "Heavy machine that flattens roads", "Vehicles", "Hard"},
//            {"snowmobile", "Machine that glides over snow", "Vehicles", "Hard"},
//            {"automobile", "Another name for a car", "Vehicles", "Hard"},
//            {"trolleybus", "Bus powered by overhead wires", "Vehicles", "Hard"},
//            {"convertible", "Car with a folding roof", "Vehicles", "Hard"},
//            {"wheelchair", "Seat on wheels for people who cannot walk far", "Vehicles", "Hard"},
//            {"mouse", "Pointing device moved by hand", "Technology", "Easy"},
//            {"email", "Letters sent over the internet", "Technology", "Easy"},
//            {"robot", "Machine programmed to act like a person", "Technology", "Easy"},
//            {"cable", "Wire that connects devices", "Technology", "Easy"},
//            {"wifi", "Wireless internet signal", "Technology", "Easy"},
//            {"pixel", "Tiny dot that makes up a screen image", "Technology", "Easy"},
//            {"drone", "Flying device with propellers", "Technology", "Easy"},
//            {"cloud", "Online storage in the sky", "Technology", "Easy"},
//            {"laser", "Narrow beam of intense light", "Technology", "Easy"},
//            {"modem", "Device that connects you to the internet", "Technology", "Easy"},
//            {"printer", "Puts documents on paper", "Technology", "Medium"},
//            {"monitor", "Screen of a desktop computer", "Technology", "Medium"},
//            {"browser", "Software for visiting websites", "Technology", "Medium"},
//            {"battery", "Stores power inside gadgets", "Technology", "Medium"},
//            {"internet", "Global network of computers", "Technology", "Medium"},
//            {"software", "Programs that run on a computer", "Technology", "Medium"},
//            {"password", "Secret word that unlocks accounts", "Technology", "Medium"},
//            {"website", "Collection of linked web pages", "Technology", "Medium"},
//            {"android", "Google's mobile operating system", "Technology", "Medium"},
//            {"firewall", "Security barrier for a network", "Technology", "Medium"},
//            {"motherboard", "Main circuit board of a computer", "Technology", "Hard"},
//            {"programming", "Writing instructions for computers", "Technology", "Hard"},
//            {"cybersecurity", "Protecting systems from online attacks", "Technology", "Hard"},
//            {"touchscreen", "Display you control with your fingers", "Technology", "Hard"},
//            {"microphone", "Records and amplifies sound", "Technology", "Hard"},
//            {"smartwatch", "Computer worn on the wrist", "Technology", "Hard"},
//            {"headphones", "Worn over ears to listen privately", "Technology", "Hard"},
//            {"spreadsheet", "Grid software for calculations", "Technology", "Hard"},
//            {"blockchain", "Chain of linked digital records behind cryptocurrency", "Technology", "Hard"},
//            {"workstation", "Powerful computer for professionals", "Technology", "Hard"},
//            {"river", "Water flowing towards the sea", "Nature", "Easy"},
//            {"ocean", "Vast body of salt water", "Nature", "Easy"},
//            {"forest", "Large area full of trees", "Nature", "Easy"},
//            {"flower", "Colorful part of a plant", "Nature", "Easy"},
//            {"storm", "Heavy rain with thunder", "Nature", "Easy"},
//            {"beach", "Sandy shore by the sea", "Nature", "Easy"},
//            {"grass", "Green carpet of the field", "Nature", "Easy"},
//            {"lake", "Still water surrounded by land", "Nature", "Easy"},
//            {"moon", "Lights the night sky", "Nature", "Easy"},
//            {"island", "Land surrounded by water", "Nature", "Easy"},
//            {"mountain", "Very high natural peak", "Nature", "Medium"},
//            {"thunder", "Loud rumble after lightning", "Nature", "Medium"},
//            {"glacier", "Slow river of ice", "Nature", "Medium"},
//            {"tornado", "Spinning funnel of wind", "Nature", "Medium"},
//            {"blizzard", "Violent snowstorm", "Nature", "Medium"},
//            {"monsoon", "Seasonal rains of South Asia", "Nature", "Medium"},
//            {"wildfire", "Fire spreading through a forest", "Nature", "Medium"},
//            {"avalanche", "Mass of snow sliding down a slope", "Nature", "Medium"},
//            {"mushroom", "Umbrella-shaped fungus", "Nature", "Medium"},
//            {"seashell", "Hard home left by a sea creature", "Nature", "Medium"},
//            {"earthquake", "Sudden shaking of the ground", "Nature", "Hard"},
//            {"thunderstorm", "Storm with lightning and thunder", "Nature", "Hard"},
//            {"rainforest", "Dense tropical woodland with heavy rain", "Nature", "Hard"},
//            {"waterspout", "Tornado over water", "Nature", "Hard"},
//            {"photosynthesis", "How plants make food from sunlight", "Nature", "Hard"},
//            {"constellation", "Pattern of stars", "Nature", "Hard"},
//            {"wilderness", "Wild land untouched by people", "Nature", "Hard"},
//            {"archipelago", "Chain of islands", "Nature", "Hard"},
//            {"stalactite", "Cave formation hanging from the ceiling", "Nature", "Hard"},
//            {"thunderbolt", "Flash of lightning with a thunderclap", "Nature", "Hard"},
//            {"india", "Country with the Taj Mahal", "Places", "Easy"},
//            {"paris", "French capital with the Eiffel Tower", "Places", "Easy"},
//            {"china", "Home of the Great Wall", "Places", "Easy"},
//            {"japan", "Land of the rising sun", "Places", "Easy"},
//            {"egypt", "Country of pharaohs and pyramids", "Places", "Easy"},
//            {"delhi", "Capital city of India", "Places", "Easy"},
//            {"tokyo", "Capital of Japan", "Places", "Easy"},
//            {"dubai", "City with the Burj Khalifa", "Places", "Easy"},
//            {"italy", "Boot-shaped country in Europe", "Places", "Easy"},
//            {"nepal", "Himalayan country with Mount Everest", "Places", "Easy"},
//            {"bangalore", "India's Silicon Valley", "Places", "Medium"},
//            {"kolkata", "City of Joy on the Hooghly", "Places", "Medium"},
//            {"scotland", "Part of the UK famous for kilts", "Places", "Medium"},
//            {"thailand", "Southeast Asian land of elephants and temples", "Places", "Medium"},
//            {"australia", "Country with kangaroos", "Places", "Medium"},
//            {"malaysia", "Home of the Petronas Towers", "Places", "Medium"},
//            {"hyderabad", "City of pearls and biryani", "Places", "Medium"},
//            {"singapore", "Island city-state with Marina Bay", "Places", "Medium"},
//            {"germany", "European country known for cars and Oktoberfest", "Places", "Medium"},
//            {"kashmir", "Paradise on Earth in the Himalayas", "Places", "Medium"},
//            {"switzerland", "Alpine country known for watches and chocolate", "Places", "Hard"},
//            {"afghanistan", "Mountainous country west of Pakistan", "Places", "Hard"},
//            {"bangladesh", "Country east of India with Dhaka as capital", "Places", "Hard"},
//            {"netherlands", "Land of tulips and windmills", "Places", "Hard"},
//            {"philippines", "Island nation in Southeast Asia", "Places", "Hard"},
//            {"madagascar", "Island home of lemurs", "Places", "Hard"},
//            {"luxembourg", "Tiny European country", "Places", "Hard"},
//            {"azerbaijan", "Caspian Sea country with Baku", "Places", "Hard"},
//            {"uzbekistan", "Central Asian country with Samarkand", "Places", "Hard"},
//            {"kazakhstan", "World's largest landlocked country", "Places", "Hard"},
//            {"nurse", "Cares for patients in hospital", "Professions", "Easy"},
//            {"chef", "Cooks in a restaurant kitchen", "Professions", "Easy"},
//            {"pilot", "Flies an aircraft", "Professions", "Easy"},
//            {"judge", "Decides cases in court", "Professions", "Easy"},
//            {"actor", "Performs in films and plays", "Professions", "Easy"},
//            {"baker", "Makes bread and cakes", "Professions", "Easy"},
//            {"tailor", "Stitches clothes", "Professions", "Easy"},
//            {"farmer", "Grows crops and raises animals", "Professions", "Easy"},
//            {"singer", "Performs songs", "Professions", "Easy"},
//            {"driver", "Operates a vehicle for work", "Professions", "Easy"},
//            {"teacher", "Educates students in class", "Professions", "Medium"},
//            {"dentist", "Fixes teeth", "Professions", "Medium"},
//            {"engineer", "Designs and builds machines", "Professions", "Medium"},
//            {"surgeon", "Operates on patients", "Professions", "Medium"},
//            {"cashier", "Takes payments at a shop", "Professions", "Medium"},
//            {"soldier", "Defends the country in uniform", "Professions", "Medium"},
//            {"mechanic", "Repairs engines", "Professions", "Medium"},
//            {"painter", "Creates pictures with brushes", "Professions", "Medium"},
//            {"carpenter", "Works with wood", "Professions", "Medium"},
//            {"librarian", "Looks after books", "Professions", "Medium"},
//            {"electrician", "Installs and repairs wiring", "Professions", "Hard"},
//            {"photographer", "Takes pictures for a living", "Professions", "Hard"},
//            {"veterinarian", "Doctor for animals", "Professions", "Hard"},
//            {"programmer", "Writes code for software", "Professions", "Hard"},
//            {"journalist", "Reports the news", "Professions", "Hard"},
//            {"accountant", "Keeps financial records", "Professions", "Hard"},
//            {"pharmacist", "Prepares and sells medicines", "Professions", "Hard"},
//            {"psychologist", "Studies and treats the mind", "Professions", "Hard"},
//            {"mathematician", "Expert in numbers and proofs", "Professions", "Hard"},
//            {"choreographer", "Designs dance routines", "Professions", "Hard"},
//    };
//
//    public static void seedOnce(Context context) {
//        final Context app = context.getApplicationContext();
//        final SharedPreferences prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
//        if (prefs.getBoolean(KEY_DONE, false)) {
//            return;
//        }
//
//        final FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // 1) read the words that already exist so nothing is added twice
//        db.collection("words").get()
//                .addOnSuccessListener(snapshot -> {
//                    Set<String> existing = new HashSet<>();
//                    for (com.google.firebase.firestore.DocumentSnapshot d : snapshot.getDocuments()) {
//                        String w = d.getString("word");
//                        if (w != null) {
//                            existing.add(w.trim().toLowerCase());
//                        }
//                    }
//
//                    // 2) add the missing ones with auto-generated IDs
//                    WriteBatch batch = db.batch();
//                    int added = 0;
//                    for (String[] row : DATA) {
//                        if (existing.contains(row[0])) {
//                            continue;
//                        }
//                        Map<String, Object> m = new HashMap<>();
//                        m.put("word", row[0]);
//                        m.put("description", row[1]);
//                        m.put("category", row[2]);
//                        m.put("difficulty", row[3]);
//                        m.put("length", row[0].length());
//
//                        DocumentReference ref = db.collection("words").document(); // auto ID
//                        batch.set(ref, m);
//                        added++;
//                    }
//
//                    final int total = added;
//                    if (total == 0) {
//                        prefs.edit().putBoolean(KEY_DONE, true).apply();
//                        return;
//                    }
//
//                    batch.commit()
//                            .addOnSuccessListener(u -> {
//                                prefs.edit().putBoolean(KEY_DONE, true).apply();
//                                Toast.makeText(app, total + " words added",
//                                        Toast.LENGTH_LONG).show();
//                            })
//                            .addOnFailureListener(e ->
//                                    Toast.makeText(app, "Word seed failed: " + e.getMessage(),
//                                            Toast.LENGTH_LONG).show());
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(app, "Word seed failed: " + e.getMessage(),
//                                Toast.LENGTH_LONG).show());
//    }
//}