package com.grabthesevehicles.app;

/** Verified phone texts and emails only. Exact excerpts are identified in SOURCES.md. */
public final class ContactMessages {
    private ContactMessages() {}
    public static final String[] IDS = {"simeon", "warstock", "pavel", "lester", "prix", "tony", "kdj", "agent14", "bryony", "ron", "mazebank", "miguel", "maude"};
    public static final String[] NAMES = {"Simeon", "Warstock Cache & Carry", "Pavel", "Lester", "Prix Luxury Real Estate", "Tony", "KDJ", "Agent 14", "Bryony", "Ron", "Maze Bank Foreclosures", "Miguel Madrazo", "Maude"};
    public static final String[] DESCRIPTIONS = {"Text · vehicle requests", "Email · Kosatka / Terrorbyte / Avenger / MOC", "Text · buy a Kosatka (excerpt)", "Texts · Doomsday / Casino / arcade offer", "Email · mansion offer (excerpt)", "Text · buy a nightclub", "Text · buy an Auto Shop", "Text · buy a MOC (excerpt)", "Text · buy an Arena Workshop (excerpt)", "Text · buy a hangar", "Email · property offer (excerpt)", "Text · Cayo Perico introduction (excerpt)", "Text · bounty-hunting introduction (excerpt)"};
    private static final String[][] MESSAGES = {
        {},
        {"Become the warzone: purchase a Kosatka, Terrorbyte, Avenger or Mobile Operations Center today from warstock-cache-and-carry.com"},
        {"And my Kosatka. Come my friend! It is listed on Warstock Cache and Carry. The captain's chair is waiting!"},
        {"The Planning Screen is ready and waiting. Head on back to the Facility when you're ready for more work.",
         "If you do get yourself on Maze Bank Foreclosures and purchase an Arcade property!!",
         "You ready to do this thing? We're all set downstairs. Head back to the arcade when you can."},
        {"Indulge in a not-so-micro microcosm of the entire city with a sprawling deluxe estate equipped with everything you could possibly want or need..."},
        {"You want to be a club owner, don't you... buy the space - you'll not regret it... Txx"},
        {"Yo it's K. Moodymann. Remember, if you serious about goin' into business with me and Sess, get yourself an auto shop."},
        {"You ready to take this up a gear? There's a truck and trailer Mobile Operations Center for sale on Warstock."},
        {"If you want to hit the big time head to our site and purchase a workshop."},
        {"So, those hangars are still available on the foreclosures site. Just like in case you wanted to get one."},
        {"Check out our exclusive properties at foreclosures.maze-bank.com."},
        {"There's a club at The Diamond called The Music Locker. Meet me there."},
        {"Hello stranger! I interest you in some bounty hunting?"}
    };
    public static int count(int contact) { return contact == 0 ? VehicleMessages.count() : MESSAGES[contact].length; }
    public static String message(int contact, int index) { return contact == 0 ? VehicleMessages.message(index) : MESSAGES[contact][index]; }
}
