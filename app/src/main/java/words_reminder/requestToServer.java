package words_reminder;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by partho on 4/13/16.
 */
public class requestToServer extends AsyncTask<String, Void, String> {

    Context context;
    private int requestCode;
    databaseHelper myDatabaseHelper;

    requestToServer(Context context,int requestCode){
        this.context=context;
        this.requestCode=requestCode;
    }

    @Override
    protected String doInBackground(String... params) {
        String data="";
        String retVal="";
        try {
            if(requestCode==11){
                /* request code 11 for sending monitoring data */
                String device_id=params[0] ,launch_time=params[1], exit_time=params[2];

                data=URLEncoder.encode("requestCode","UTF-8")+"="+URLEncoder.encode("11","UTF-8");
                data+="&"+URLEncoder.encode("device_id","UTF-8")+"="+device_id;
                data+="&"+URLEncoder.encode("launch_time","UTF-8")+"="+ URLEncoder.encode(launch_time,"UTF-8");
                data+="&"+URLEncoder.encode("exit_time","UTF-8")+"="+URLEncoder.encode(exit_time,"UTF-8");
            }
            else if(requestCode==22){
                /* request code 22 for getting server sent message */
                data=URLEncoder.encode("requestCode","UTF-8")+"="+URLEncoder.encode("22","UTF-8");
                data+="&"+URLEncoder.encode("device_id","UTF-8")+"="+URLEncoder.encode(params[0],"UTF-8");
            }
            else if(requestCode==33){
                /* request code 33 for sending msg to developer */
                data=URLEncoder.encode("requestCode","UTF-8")+"="+URLEncoder.encode("33","UTF-8");
                data+="&"+URLEncoder.encode("device_id","UTF-8")+"="+URLEncoder.encode(params[0],"UTF-8");
                data+="&"+URLEncoder.encode("msg","UTF-8")+"="+URLEncoder.encode(params[1],"UTF-8");
            }

            String Link="http://code--projects.com/gre_words/getMonitoringData.php";
            URL url=new URL(Link);
            URLConnection con=url.openConnection();
            con.setDoOutput(true);

            OutputStreamWriter writer=new OutputStreamWriter(con.getOutputStream());
            writer.write(data);
            writer.flush();

            BufferedReader reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder builder=new StringBuilder();

            String line="";
            while ((line=reader.readLine()) !=null){
                builder.append(line);
                break;
            }
            retVal=builder.toString();

        } catch (Exception e){
            retVal=e.toString();
            retVal="error";
        }
        return retVal;
    }

    @Override
    protected void onPostExecute(String s) {
        //Toast.makeText(context,s,Toast.LENGTH_LONG).show();
        myDatabaseHelper=new databaseHelper(context);
        myDatabaseHelper.deleteSentData();

        String x=s.substring(0,2);
        if(x.compareTo("22")==0){
            myDatabaseHelper.saveServerMsg(s.substring(2, s.length()));
            //Toast.makeText(context,"query executed",Toast.LENGTH_LONG).show();
        }
        else{
            //Toast.makeText(context,s,Toast.LENGTH_LONG).show();
        }
    }
}
