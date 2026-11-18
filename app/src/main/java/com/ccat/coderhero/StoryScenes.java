package com.ccat.coderhero;

/** Story text shown before each level. Each slide = { top white panel, red panel }. HTML allowed. */
public final class StoryScenes {
    private StoryScenes() {}

    public static String[][] get(int level) {
        switch (level) {
            case 1: return new String[][]{
                {"<b>LEVEL 1</b><br>CODE RED \u2013 THE HOSPITAL",
                 "Boris sprints through the night streets. The Corruptor's first strike: Cavite Central Hospital."},
                {"<b>Inside the ICU</b>",
                 "Doctors pound on the locked doors. Life-support screens freeze, waiting for one thing: a patient status."}};
            case 2: return new String[][]{
                {"<b>LEVEL 2</b><br>BLACKOUT \u2013 THE POWER GRID",
                 "With the hospital saved, the lights across the city begin to die."},
                {"<b>North Power Station</b>",
                 "The Corruptor scrambled every number. Only exact calculations can bring the grid back."}};
            case 3: return new String[][]{
                {"<b>LEVEL 3</b><br>GRIDLOCK \u2013 THE AIRPORT",
                 "Power returns, but the airport tower is stuck in a loop of bad decisions."},
                {"<b>Metro Airport</b>",
                 "Planes circle overhead and fuel is running low. Boris must write the right logic."}};
            case 4: return new String[][]{
                {"<b>LEVEL 4</b><br>VAULT LOOP \u2013 THE BANK",
                 "A distress call: City Bank's vault door is cycling endlessly."},
                {"<b>Trapped Inside</b>",
                 "Customers press against the glass. Something is repeating forever, and Boris must break it."}};
            case 5: return new String[][]{
                {"<b>LEVEL 5</b><br>SCRAMBLED \u2013 THE SCHOOL",
                 "The Corruptor is getting angrier. This time it hits Central High School."},
                {"<b>Sealed Gates</b>",
                 "Attendance records are scrambled. Every student must be found in the data."}};
            case 6: return new String[][]{
                {"<b>LEVEL 6</b><br>FLOOD WARNING \u2013 THE DAM",
                 "Rain pounds the city. The river rises behind the dam."},
                {"<b>River Dam Control</b>",
                 "The floodgates ignore every command. Each routine must be called exactly right."}};
            default: return new String[][]{
                {"<b>FINAL LEVEL</b><br>THE CORE",
                 "Every trail leads to the heart of the city's systems. Boris steps into a room of red static."},
                {"<b>The Corruptor Appears</b>",
                 "\"You have come far, hero. But can you speak the language perfectly?\""},
                {"<b>One Last Test</b>",
                 "The doors seal behind Boris. One wrong symbol, and the whole city goes dark forever."}};
        }
    }

    public static String[][] epilogue() {
        return new String[][]{
            {"<b>EPILOGUE:</b><br>THE CORRUPTOR FALLS",
             "The last line of code locks into place. The red glow shatters across every screen, then fades to calm blue."},
            {"<b>The City Wakes</b>",
             "Hospitals hum. Planes land. Vault doors open. Students run home, and nobody knows who saved them."},
            {"<b>Back at CCAT Lab</b>",
             "Boris pulls off the mask and smiles. Every monitor shows one message: SYSTEM STABLE."},
            {"<b>A New Message</b>",
             "A single line flickers on the main screen: \"// the Corruptor was only a bug... who wrote it?\""},
            {"<b>THE END</b>",
             "Congratulations, Syntax Savior! You spoke the language perfectly. Try Endless Mode to beat your best score."}};
    }
}
