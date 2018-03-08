package words_reminder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by partho on 3/30/16.
 */
public class databaseHelper extends SQLiteOpenHelper {

    Context context;

    public databaseHelper(Context context){
        super(context,"settings",null,1);
        this.context=context;
        //Toast.makeText(context,"DB created",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            /* create settings table */
            db.execSQL("CREATE TABLE settings(ID INTEGER PRIMARY KEY NOT NULL, words_per_day INT NOT NULL, last_update_date TEXT NOT NULL," +
                    " is_notification_stopped INT NOT NULL, issetSchedule INT NOT NULL, device_id TEXT NOT NULL)");
            /* initialize settings table */
            db.execSQL("INSERT INTO settings(words_per_day,last_update_date,is_notification_stopped,issetSchedule,device_id) " +
                    "VALUES(4,datetime(),0,0,"+"'deviceid'"+")");
        } catch (Exception e){

        }

        try {
            /* create monitor table */
            db.execSQL("CREATE TABLE monitor(ID INTEGER PRIMARY KEY NOT NULL, launch_time TEXT NOT NULL, exit_time TEXT NOT NULL)");
            //db.execSQL("INSERT INTO monitor(device_id,launch_time,exit_time) VALUES('xxx','yy','zzz')");
        } catch (Exception e){

        }

        try {
            /* create msg_from_server table */
            db.execSQL("CREATE TABLE msg_from_server(ID INTEGER PRIMARY KEY NOT NULL, msg TEXT NOT NULL)");
            db.execSQL("INSERT INTO msg_from_server(msg) VALUES('"+""+"')");
        } catch (Exception e){

        }

        try {
            /* create received_words table*/
            db.execSQL("CREATE TABLE received_words(ID INTEGER PRIMARY KEY NOT NULL, last_received_word_id INT NOT NULL, num_of_word_shown_today INT NOT NULL, " +
                    " last_word_receive_time TEXT NOT NULL, total_received_word INT NOT NULL)");
            /* initialize received_words table*/
            db.execSQL("INSERT INTO received_words(last_received_word_id,num_of_word_shown_today,last_word_receive_time,total_received_word) " +
                    " VALUES(0,0,'1459498845880',0) ");
        } catch (Exception e){
            //Toast.makeText(context,String.valueOf(e),Toast.LENGTH_SHORT).show();
        }

        try {
            /* create words table */
            //db.execSQL("CREATE TABLE words(ID INTEGER PRIMARY KEY, word TEXT NOT NULL, meaning TEXT NOT NULL, memonic TEXT NOT NULL) ");
            db.execSQL("CREATE TABLE words(ID INTEGER PRIMARY KEY NOT NULL, word TEXT NOT NULL)");
            words_collecttion wordsCollecttion=new words_collecttion();
            int words=wordsCollecttion.WORDS.length;

            for (int i=0;i<words;i++){
                //String query="INSERT INTO words(word,meaning,memonic) " +
                        //" VALUES('"+wordsCollecttion.words[i]+"','"+wordsCollecttion.meanings[i]+"','"+wordsCollecttion.memonics[i]+"')";

                String query2="INSERT INTO words(word) VALUES('"+wordsCollecttion.WORDS[i]+"')";
                //db.execSQL(query);
                db.execSQL(query2);
            }

            //db.execSQL("INSERT INTO words(word,meaning,memonic) VALUES('abse','lower; degrade; humiliate; make humble; make (oneself) lose self respect','abase-abe(a slang used to degrade a person)+shhh(se)usually an attempt to degrade a persons opinion..overall goes to humiliate a person....')");
        } catch (Exception e){
            //Toast.makeText(context,String.valueOf(e),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    SQLiteDatabase db=this.getReadableDatabase();
    public Cursor getWordsPerDay(){
        Cursor result=db.rawQuery("SELECT * FROM settings",null);
        return result;
    }

    public Cursor getNumOfWordsReceivedToday(){
        Cursor result=db.rawQuery("SELECT * FROM received_words",null);
        return result;
    }

    public Cursor get_last_received_word_id(){
        Cursor result=db.rawQuery("SELECT last_received_word_id FROM received_words",null);
        return result;
    }

    public Cursor get_max_word_id_in_db(){
        Cursor result=db.rawQuery(" SELECT * FROM words WHERE ID=(SELECT MAX(ID) FROM words) ",null);
        return result;
    }

    public Cursor get_word_details(int word_id){
        Cursor result=db.rawQuery("SELECT * FROM words WHERE ID="+word_id,null);
        return result;
    }

    public Cursor getIssetSchedule(){
        Cursor result=db.rawQuery("SELECT issetSchedule FROM settings",null);
        return result;
    }

    public void scheduleCreated(){
        String query="UPDATE settings SET issetSchedule='"+1+"'";
        db.execSQL(query);
    }

    public void update_last_received_word_info(int ID,long curSystemTime){
        String query1="UPDATE received_words SET last_received_word_id="+(ID);
        String query2="UPDATE received_words SET last_word_receive_time="+curSystemTime;
        db.execSQL(query1);
        db.execSQL(query2);
    }

    public Cursor get_last_word_receive_time(){
        Cursor result=db.rawQuery("SELECT last_word_receive_time FROM received_words",null);
        return result;
    }

    public void update_num_of_words_received_today(int id){
        String query="UPDATE received_words SET num_of_word_shown_today="+id;
        db.execSQL(query);
    }

    public void do_change_number_of_words(String n){
        String query="UPDATE settings SET words_per_day="+n;
        db.execSQL(query);
    }


    public void saveMonitoringdata(String launch_time, String exit_time){
        String query="INSERT INTO monitor(launch_time,exit_time) VALUES('"+launch_time+"','"+exit_time+"')";
        db.execSQL(query);
    }

    public void storeDeviceId(String deviceId){
        String query="UPDATE settings SET device_id='"+deviceId+"'";
        db.execSQL(query);
    }

    public Cursor getMonitoringdata(){
        //Cursor result=db.rawQuery("SELECT monitor.launch_time, monitor.exit_time, settings.device_id FROM monitor INNER JOIN settings",null);
        //Cursor result=db.rawQuery("SELECT device_id FROM settings",null);
        Cursor result=db.rawQuery("SELECT * FROM monitor",null);
        return result;
    }

    public void deleteSentData(){
        db.execSQL("DELETE FROM monitor WHERE 1");

    }

    public void saveServerMsg(String serverMsg){
        String query="UPDATE msg_from_server SET msg='"+serverMsg+"'";
        db.execSQL(query);
    }

    public Cursor getMsgFromServer(){
        Cursor result=db.rawQuery("SELECT msg FROM msg_from_server",null);
        return result;
    }

    public Cursor wordsTill(int id){
        Cursor result=db.rawQuery("SELECT word FROM words WHERE ID<="+id,null);
        return result;
    }

}
