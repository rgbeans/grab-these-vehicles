package com.grabthesevehicles.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import java.io.IOException;

public final class SimeonNotifications {
    public static final String CHANNEL = "simeon_requests_gta_file_v1";
    private static final String RESOURCE_SOUND_CHANNEL = "simeon_requests_gta";
    private static final String LEGACY_CHANNEL = "simeon_requests";
    private SimeonNotifications() {}
    static Uri soundUri(Context c) {
        return SoundProvider.SOUND_URI;
    }
    public static void createChannel(Context c) {
        try { SoundProvider.prepareAudio(c); }
        catch (IOException error) { throw new IllegalStateException("Cannot prepare the GTA notification sound", error); }
        NotificationManager manager = c.getSystemService(NotificationManager.class);
        if (manager.getNotificationChannel(CHANNEL) != null) {
            NotificationChannel existing = manager.getNotificationChannel(CHANNEL);
            existing.setName("GTA Online contacts");
            existing.setDescription("Scheduled texts and emails from your selected GTA Online contacts");
            manager.createNotificationChannel(existing);
            return;
        }
        // Android channel sounds are immutable; a new channel also updates existing installations.
        NotificationChannel old = manager.getNotificationChannel(RESOURCE_SOUND_CHANNEL);
        if (old == null) old = manager.getNotificationChannel(LEGACY_CHANNEL);
        int importance = old == null ? NotificationManager.IMPORTANCE_HIGH : old.getImportance();
        NotificationChannel channel = new NotificationChannel(CHANNEL, "GTA Online contacts", importance);
        channel.setDescription("Scheduled texts and emails from your selected GTA Online contacts");
        channel.setSound(soundUri(c),new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
        if (old != null && old.getVibrationPattern() != null) channel.setVibrationPattern(old.getVibrationPattern());
        channel.enableVibration(old == null || old.shouldVibrate());
        channel.setShowBadge(old == null || old.canShowBadge());
        manager.createNotificationChannel(channel);
    }
    public static boolean allowed(Context c) {
        if (Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false;
        NotificationManager manager = c.getSystemService(NotificationManager.class);
        NotificationChannel channel = manager.getNotificationChannel(CHANNEL);
        return manager.areNotificationsEnabled() && (channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE);
    }
    public static boolean deliver(Context c) {
        return deliver(c, -1);
    }
    public static boolean deliver(Context c, int contact) {
        createChannel(c);
        if (!allowed(c)) return false;
        String message = RequestScheduler.nextMessage(c, contact);
        if (message == null) return false;
        String sender = RequestScheduler.prefs(c).getString("last_sender", "Simeon");
        int senderIndex = RequestScheduler.prefs(c).getInt("last_contact", 0);
        PendingIntent open = PendingIntent.getActivity(c, 11 + senderIndex,
            new Intent(c, CopyMessageActivity.class).putExtra(CopyMessageActivity.MESSAGE,message)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = new Notification.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(sender)
            .setContentText(message)
            .setStyle(new Notification.BigTextStyle().setBigContentTitle(sender).bigText(message))
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setColor(Color.rgb(255, 176, 71))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setVisibility(Notification.VISIBILITY_PUBLIC);
        int icon = ContactIcons.resource(senderIndex);
        if (icon != 0) builder.setLargeIcon(android.graphics.drawable.Icon.createWithResource(c, icon));
        Notification notification = builder.build();
        try { c.getSystemService(NotificationManager.class).notify(100 + senderIndex, notification); return true; }
        catch (SecurityException denied) { return false; }
    }
}
