package com.ccat.coderhero;

/** Story-mode level definitions. Add a new Level here + questions with the same level id in questions.json. */
public final class Level {
    public final int id;
    public final String title, victim, topic, alertTitle, alert, system, success, fail;
    public final int seconds, count, background;

    private Level(int id, String title, String victim, String topic, int seconds, int count, int background,
                  String alertTitle, String alert, String system, String success, String fail) {
        this.id = id; this.title = title; this.victim = victim; this.topic = topic;
        this.seconds = seconds; this.count = count; this.background = background;
        this.alertTitle = alertTitle; this.alert = alert; this.system = system;
        this.success = success; this.fail = fail;
    }

    public static final Level[] ALL = {
        new Level(1, "CODE RED \u2013 THE HOSPITAL", "\uD83E\uDE7A Medical Team & Patients in ICU Time",
            "Basic Variable Declaration", 60, 5, R.drawable.bg_hospital,
            "FIRST ALERT: CAVITE CENTRAL HOSPITAL.",
            "The life-support systems are freezing. Doctors and patients are locked out. The display shows the system is waiting for a status update.",
            "Define patient status to restore monitoring. Syntax must be exact.",
            "System online. Monitoring restored. Doctors rush in. Lives saved!",
            "Syntax error. Connection lost. The screens go dark..."),
        new Level(2, "BLACKOUT \u2013 THE POWER GRID", "\u26A1 Residents & Hospitals on Backup Power",
            "Data Types & Operators", 60, 5, R.drawable.bg_nightcity,
            "SECOND ALERT: NORTH POWER STATION.",
            "Voltage readings are corrupted and the city is going dark. The grid needs exact calculations to reboot.",
            "Compute every value correctly to stabilize the grid.",
            "Grid stabilized. Lights return across the city!",
            "Calculation error. Overload. The city goes dark..."),
        new Level(3, "GRIDLOCK \u2013 THE AIRPORT", "\u2708 Passengers & Flight Crews",
            "Conditions (if / else / switch)", 60, 5, R.drawable.bg_airport,
            "THIRD ALERT: METRO AIRPORT.",
            "Control-tower logic is stuck in a loop. Planes circle overhead with no clearance and fuel is running low.",
            "Write the right decision logic before the fuel runs out.",
            "Clearance granted. Every plane lands safely!",
            "Logic error. Runway lights fail..."),
        new Level(4, "VAULT LOOP \u2013 THE BANK", "\uD83C\uDFE6 Clerks & Customers Trapped Inside",
            "Loops (for / while / do-while)", 75, 6, R.drawable.bg_bank,
            "FOURTH ALERT: CITY BANK.",
            "The vault door cycles endlessly, trapping everyone inside. Something is repeating forever.",
            "Control the loop with exact syntax to open the doors.",
            "Loop broken. Vault unlocked. Everyone walks out safe!",
            "Infinite loop. The doors seal shut..."),
        new Level(5, "SCRAMBLED \u2013 THE SCHOOL", "\uD83C\uDF92 Students & Teachers",
            "Arrays & Strings", 75, 6, R.drawable.bg_school2,
            "FIFTH ALERT: CENTRAL HIGH SCHOOL.",
            "Attendance records are scrambled and the gates stay sealed until every student is accounted for.",
            "Read the data exactly: one wrong index and the gates stay shut.",
            "Records restored. Gates open. Students run home safe!",
            "Index out of bounds. The gates stay shut..."),
        new Level(6, "FLOOD WARNING \u2013 THE DAM", "\uD83C\uDF0A Riverside Families",
            "Methods", 90, 6, R.drawable.bg_dam,
            "SIXTH ALERT: RIVER DAM CONTROL.",
            "The floodgates will not respond. Every control routine must be called exactly right.",
            "Call and define the methods correctly to release the pressure.",
            "Floodgates open. The river calms. Families are safe!",
            "Method not found. The water keeps rising..."),
        new Level(7, "FINAL BOSS \u2013 THE CORE", "\uD83C\uDFD9 The Entire City",
            "Classes & Objects (OOP)", 90, 7, R.drawable.bg_glitch,
            "FINAL ALERT: THE CORE.",
            "The Corruptor reveals itself inside the city's core. Every system hangs on one last test of the language.",
            "Speak the language perfectly. One wrong symbol, and the door closes forever.",
            "The Corruptor is purged. The city is free. Boris, Syntax Savior!",
            "Compile error. The Corruptor laughs..."),
    };

    public static Level get(int id) {
        for (Level l : ALL) if (l.id == id) return l;
        return ALL[0];
    }

    public boolean isLast() { return id == ALL.length; }
}
