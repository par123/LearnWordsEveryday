package words_reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by partho on 3/31/16.
 */
public class broadCastReceiver extends BroadcastReceiver {
    words_reminder.MainActivity mainActivity=new words_reminder.MainActivity();
    //int intent_id=mainActivity.last_received_word_id;
    int intent_id=22;
    Notification notification;
    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent=new Intent(context, words_reminder.MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,intent_id,myIntent,PendingIntent.FLAG_ONE_SHOT);

        notification=new NotificationCompat.Builder(context)
                .setContentTitle("New Word Received")
                .setContentText("Click to see")
                .setTicker("GRE")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        notificationManager=(NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(intent_id,notification);
    }
}
