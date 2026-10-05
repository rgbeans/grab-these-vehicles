package com.grabthesevehicles.app;

/** Original GTA phone textures. Zero means no verified sender icon exists in this catalog. */
public final class ContactIcons {
    private ContactIcons() {}
    private static final int[] RESOURCES = {
        R.drawable.contact_simeon,
        R.drawable.contact_warstock,
        R.drawable.contact_pavel,
        R.drawable.contact_lester,
        0,
        R.drawable.contact_tony,
        R.drawable.contact_kdj,
        R.drawable.contact_agent14,
        R.drawable.contact_bryony,
        R.drawable.contact_ron,
        R.drawable.contact_mazebank,
        R.drawable.contact_miguel,
        R.drawable.contact_maude,
        R.drawable.contact_mors,
        R.drawable.contact_pegasus,
        R.drawable.contact_securoserv,
        R.drawable.contact_franklin,
        R.drawable.contact_lamar,
        R.drawable.contact_gerald,
        R.drawable.contact_junkenergy
    };
    public static int resource(int contact) {
        return contact >= 0 && contact < RESOURCES.length ? RESOURCES[contact] : 0;
    }
}
