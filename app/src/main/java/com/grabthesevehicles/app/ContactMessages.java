package com.grabthesevehicles.app;

/** Verified purchase reminders and heist-ready text. Call/email excerpts are identified in SOURCES.md. */
public final class ContactMessages {
    private ContactMessages() {}
    public static final String[] IDS = {"simeon", "warstock", "paige", "lester", "prix"};
    public static final String[] NAMES = {"Simeon", "Warstock Cache & Carry", "Paige Harris", "Lester", "Prix Luxury Real Estate"};
    public static final String[] DESCRIPTIONS = {"Vehicle requests", "Purchase reminder", "Terrorbyte purchase reminder", "Heist ready", "Mansion offer (excerpt)"};
    private static final String[][] MESSAGES = {
        {},
        {"Become the warzone: purchase a Kosatka, Terrorbyte, Avenger or Mobile Operations Center today from warstock-cache-and-carry.com"},
        {"There's a Terrorbyte truck on Warstock I can turn into our Nerve Center."},
        {"The Planning Screen is ready and waiting. Head on back to the Facility when you're ready for more work."},
        {"Indulge in a not-so-micro microcosm of the entire city with a sprawling deluxe estate equipped with everything you could possibly want or need..."}
    };
    public static int count(int contact) { return contact == 0 ? VehicleMessages.count() : MESSAGES[contact].length; }
    public static String message(int contact, int index) { return contact == 0 ? VehicleMessages.message(index) : MESSAGES[contact][index]; }
}
