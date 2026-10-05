package com.grabthesevehicles.app;

/** Eight original and eight post-Summer-Special lists, in SMS order. */
public final class VehicleMessages {
    private VehicleMessages() {}
    private static final String[][] LISTS = {
        {"Ubermacht Sentinel XS", "Vapid Dominator", "Benefactor Schafter", "Cheval Surge", "Ocelot Jackal"},
        {"Cheval Surge", "Ocelot Jackal", "Obey Tailgater", "Dundreary Landstalker", "Maibatsu Penumbra"},
        {"Dundreary Landstalker", "Maibatsu Penumbra", "Ocelot F620", "Fathom FQ 2", "Bollokan Prairie"},
        {"Dundreary Landstalker", "Maibatsu Penumbra", "Ocelot F620", "Fathom FQ 2", "Mammoth Patriot"},
        {"Fathom FQ 2", "Mammoth Patriot", "Emperor Habanero", "Schyster Fusilade", "Bravado Gresley"},
        {"Benefactor Serrano", "Mammoth Patriot", "Emperor Habanero", "Schyster Fusilade", "Bravado Gresley"},
        {"Schyster Fusilade", "Bravado Gresley", "Albany Buccaneer", "Western Daemon", "Western Bagger"},
        {"Karin BeeJay XL", "Bravado Gresley", "Albany Buccaneer", "Western Daemon", "Western Bagger"},
        {"Benefactor Schafter", "Vapid Bullet", "Ocelot F620", "Grotti Carbonizzare", "Pfister Comet"},
        {"Bravado Banshee", "Invetero Coquette", "Ubermacht Sentinel", "Benefactor Dubsta", "Pegassi Infernus"},
        {"Benefactor Feltzer", "Ocelot Jackal", "Ocelot F620", "Enus Super Diamond", "Obey Rocoto"},
        {"Pegassi Infernus", "Invetero Coquette", "Bravado Banshee", "Benefactor Dubsta", "Ubermacht Sentinel"},
        {"Lampadati Felon GT", "Benefactor Serrano", "Vapid Bullet", "Pegassi Infernus", "Invetero Coquette"},
        {"Ubermacht Zion", "Bravado Banshee", "Pfister Comet", "Benefactor Surano", "Dewbauchee Exemplar"},
        {"Ubermacht Sentinel", "Benefactor Schwartzer", "Enus Super Diamond", "Ocelot Jackal", "Benefactor Feltzer"},
        {"Obey Rocoto", "Lampadati Felon GT", "Benefactor Schafter", "Grotti Carbonizzare", "Dewbauchee Exemplar"}
    };
    public static int count() { return LISTS.length; }
    public static String message(int index) {
        return "Grab these vehicles: " + String.join(", ", LISTS[index]) + ".";
    }
    public static String preview() { return message(0); }
}
