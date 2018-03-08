package words_reminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

/**
 * Created by partho on 4/5/16.
 */
public class alarmService extends IntentService {

    databaseHelper myDatabaseHelper;

    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";

    private IntentFilter matcher;

    public alarmService(){
        super("TAG");

        matcher = new IntentFilter();
        matcher.addAction(CREATE);
        matcher.addAction(CANCEL);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        String notificationId = intent.getStringExtra("notificationId");

        if (matcher.matchAction(action)) {
            execute(action, notificationId);
        }
    }

    private void execute(String action, String notificationId) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(this, broadCastReceiver.class);
            i.putExtra("id",45);

            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            long time =7*60*1000;
            int words=Integer.parseInt(wordsPerDay());
            int intervalInMilis=(int) ((24.0/words)*3600*1000);
            long T=Long.valueOf("1459498845880");


            if (CREATE.equals(action)) {
                //am.set(AlarmManager.RTC_WAKEUP, time, pi);
                am.setRepeating(AlarmManager.RTC_WAKEUP, T, intervalInMilis, pi);

            } else if (CANCEL.equals(action)) {
                am.cancel(pi);
            }
    }

    private String wordsPerDay(){
        myDatabaseHelper=new databaseHelper(getApplicationContext());
        Cursor result=myDatabaseHelper.getWordsPerDay();
        String w="4";
        if(result!=null && result.moveToFirst()){
            w=result.getString(result.getColumnIndex("words_per_day"));
        }
        return w;
    }


}
