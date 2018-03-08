package words_reminder;

import android.content.Context;
import android.content.Intent;

/**
 * Created by partho on 4/5/16.
 */
public class alarmSetter extends broadCastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, alarmService.class);
        service.setAction(alarmService.CREATE);
        context.startService(service);
    }


}
