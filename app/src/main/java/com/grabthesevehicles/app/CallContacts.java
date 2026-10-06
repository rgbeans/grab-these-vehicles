package com.grabthesevehicles.app;

/** Phone callers have their own catalog; a text sender is not automatically a voice caller. */
public final class CallContacts {
    private CallContacts() {}
    // Keep the original twenty indices stable across upgrades and imported recordings.
    public static final String[] IDS = {"simeon", "warstock", "pavel", "lester", "prix", "tony", "kdj", "agent14", "bryony", "ron", "mazebank", "miguel", "maude", "mors", "pegasus", "securoserv", "franklin", "lamar", "gerald", "junkenergy", "paige", "dom", "brucie", "englishdave", "martin", "merryweather", "mechanic", "assistant_male", "assistant_female", "raf"};
    public static final String[] NAMES = {"Simeon", "Warstock Cache & Carry", "Pavel", "Lester", "Prix Luxury Real Estate", "Tony", "KDJ", "Agent 14", "Bryony", "Ron", "Maze Bank Foreclosures", "Miguel Madrazo", "Maude", "Mors Mutual Insurance", "Pegasus", "SecuroServ", "Franklin", "Lamar", "Gerald", "Junk Energy", "Paige", "Dom", "Brucie", "English Dave", "Martin Madrazo", "Merryweather Security", "Mechanic", "Assistant (male)", "Assistant (female)", "Raf"};
    static int icon(int contact) {
        return contact < ContactMessages.IDS.length ? ContactIcons.resource(contact) : 0;
    }
}
