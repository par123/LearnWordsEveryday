package words_reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

/**
 * Created by partho on 4/13/16.
 */
public class data_exchanger extends BroadcastReceiver {
    private Context context;
    databaseHelper myDatabaseHelper;
    String DATA[];

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        if(isNetworkAvailable(context)){
            // send data to server
            DATA=dataToSend();

            /* request code 11 for sending monitoring data */
            new requestToServer(context,11).execute(getDeviceId(),DATA[0],DATA[1]);

            /* request code 22 for getting server sent message */
            new requestToServer(context,22).execute(getDeviceId());
        }
    }

    public boolean isNetworkAvailable(Context context2){
        ConnectivityManager connectivityManager=(ConnectivityManager) context2.getSystemService(context2.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnectedOrConnecting();
    }

    private String[] dataToSend(){
        myDatabaseHelper=new databaseHelper(context);
        Cursor result=myDatabaseHelper.getMonitoringdata();
        String launch_time="",exit_time="";
        String mString[]=new String[3];

        if(result!=null && result.moveToFirst()){
            while (!result.isAfterLast()){
                launch_time+=result.getString(1)+"@*@";
                exit_time+=result.getString(2)+"@*@";
                result.moveToNext();
            }
            mString[0]=launch_time;
            mString[1]=exit_time;
        }
        return mString;
    }

    public String getDeviceId(){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
