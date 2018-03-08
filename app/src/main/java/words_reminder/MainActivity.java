package words_reminder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public long curSystemTime(){return (int)System.currentTimeMillis();}

    TextView showing_word_label;
    TextView bottomLabel;
    TextView tvInScrollview;
    databaseHelper myDatabaseHelper;
    public int last_received_word_id;
    boolean isScheduled=false;
    int  words_per_day;
    String WORD_DETAILS[]=new String[4];
    long launch_time,exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        launch_time=System.currentTimeMillis();

        isScheduled=getIssetSchedule();
        if(!isScheduled){
            setSchedule();
            myDatabaseHelper.scheduleCreated();
            myDatabaseHelper.storeDeviceId(getDeviceId());
        }
        setShowingWordLabel();
        showNthWord();

        //showMOnitor(); /* debugging purpose */
        if(!msg_from_server().isEmpty()) {
            bottomLabel.setText(msg_from_server());
        }
    }

    private String msg_from_server(){
        myDatabaseHelper=new databaseHelper(this);
        Cursor result=myDatabaseHelper.getMsgFromServer();
        String s="";
        if(result!=null && result.moveToFirst()){
            s=result.getString(0);
        }
        return s;
    }

    public String getVersionName(){
        String v="1.1";
        try {
            PackageInfo packageInfo=getPackageManager().getPackageInfo(getPackageName(),0);
            v=packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }
        return v;
    }

    public void showMOnitor(){
        myDatabaseHelper=new databaseHelper(words_reminder.MainActivity.this);
        String x="";
        Cursor result=myDatabaseHelper.getMonitoringdata();
        if(result!=null && result.moveToFirst()){

            while (!result.isAfterLast()){
                String lnch=result.getString(result.getColumnIndex("exit_time"));
                String exit=result.getString(result.getColumnIndex("launch_time"));

                x+=lnch+"--"+exit+"\n";
                result.moveToNext();
            }
        }

        Toast.makeText(this,x,Toast.LENGTH_LONG).show();
    }
    /*

    boolean backPressed=false;
    @Override
    public void onBackPressed() {
        exitTime=System.currentTimeMillis();
        if(backPressed){
            myDatabaseHelper=new databaseHelper(MainActivity.this);
            //Toast.makeText(this,launch_time+"--"+exitTime,Toast.LENGTH_SHORT).show();
            myDatabaseHelper.saveMonitoringdata(String.valueOf(launch_time),String.valueOf(exitTime));
            backPressed=false;
            super.onBackPressed();
            return;
        }
        Toast.makeText(this,"Click back again to exit",Toast.LENGTH_SHORT).show();
        backPressed=true;
    }
    */

    public String getDeviceId(){
        return Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
    }

    public void setSchedule(){
        //Toast.makeText(this, ""+"preparing to schedule....", Toast.LENGTH_LONG).show();
        int INTENT_ID=45;
        int intervalInMilis=(int) ((24.0/getWordsPerDay())*3600*1000)+10000;
        //intervalInMilis=getWordsPerDay()*30*1000;
        //Toast.makeText(MainActivity.this,""+intervalInMilis+"--"+getWordsPerDay(),Toast.LENGTH_LONG).show();

        Intent intent=new Intent(this,broadCastReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,INTENT_ID,intent,0);

        AlarmManager alarmManager=(AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalInMilis, pendingIntent);
    }

    private boolean getIssetSchedule(){
        myDatabaseHelper=new databaseHelper(this);
        boolean isset=false;
        Cursor result=myDatabaseHelper.getIssetSchedule();
        if(result!=null && result.moveToFirst()){
            isset=Integer.parseInt(result.getString(result.getColumnIndex("issetSchedule")))==1 ? true:false;
        }
        return isset;
    }


    private void showNthWord() {
        WORD_DETAILS=get_word_details();
        tvInScrollview=(TextView) findViewById(R.id.tvInScrollview);
        if(isWordAvailable()){
            /*
            tvInScrollview.setText(Html.fromHtml("<p><b>Word:</b> "+WORD_DETAILS[0]+
                    "</p> <p><b>Meaning:</b> "+WORD_DETAILS[1]+"<p/> <p><b>Memonics:</b> "+
                    WORD_DETAILS[2]+"</p>"));
            */
            tvInScrollview.setText(WORD_DETAILS[0]);
        }
        else{
            tvInScrollview.setText("More words will be available soon.\n Be with us");
        }
        Typeface customFont=Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/font3.ttf");
        tvInScrollview.setTypeface(customFont);
    }

    long last_word_receive_time(){
        myDatabaseHelper=new databaseHelper(this);
        Cursor result=myDatabaseHelper.get_last_word_receive_time();
        result.moveToFirst();
        //Toast.makeText(this,result.getString(result.getColumnIndex("last_word_receive_time")),Toast.LENGTH_LONG).show();
        return Long.parseLong(result.getString(result.getColumnIndex("last_word_receive_time")));
        //return 387483;
    }

    public boolean InIntervalTime(){
        int intervalInMilis=(int) ((24.0/getWordsPerDay())*3600*1000);
        long curTime=System.currentTimeMillis();
        long last_received=last_word_receive_time();

        //Toast.makeText(MainActivity.this,(curTime-last_received)+"<="+intervalInMilis,Toast.LENGTH_SHORT).show();

        return (curTime-last_received<=intervalInMilis)? true:false;
        //return false;
    }

    private String[] get_word_details(){
        String details[]=new String[4];
        details[0]=details[1]=details[2]=details[3]="";
        myDatabaseHelper=new databaseHelper(this);
        last_received_word_id=get_last_received_word_id();
        Cursor result=myDatabaseHelper.get_word_details(last_received_word_id + 1);
        if(result!=null && result.moveToFirst()){
            details[0]=result.getString(result.getColumnIndex("word"));
            details[1]= String.valueOf(last_received_word_id);
            //details[1]=result.getString(result.getColumnIndex("meaning"));
            //details[2]=result.getString(result.getColumnIndex("memonic"));
            //details[3]=result.getString(result.getColumnIndex("ID"));

            /* update 'last_received_word_id' in 'received_words' table */
            if(!InIntervalTime()){
                myDatabaseHelper.update_last_received_word_info(last_received_word_id + 1, System.currentTimeMillis());
                //Toast.makeText(MainActivity.this,""+System.currentTimeMillis(),Toast.LENGTH_SHORT).show();
            }
        }
        return details;
    }

    public int get_last_received_word_id(){
        int a=0;
        myDatabaseHelper=new databaseHelper(this);
        Cursor result=myDatabaseHelper.get_last_received_word_id();
        if(result!=null && result.moveToFirst()){
            a=Integer.parseInt(result.getString(result.getColumnIndex("last_received_word_id")));
        }
        return a;
    }



    private boolean isWordAvailable(){
        myDatabaseHelper=new databaseHelper(this);
        //Cursor result1=myDatabaseHelper.get_last_received_word_id();
        Cursor result2=myDatabaseHelper.get_max_word_id_in_db();

        int max_word_id_in_db=0;
        boolean available=false;
        if(result2!=null &&  result2.moveToFirst()){
            //result1.moveToFirst();
            //result2.moveToFirst();
            //last_received_word_id=Integer.parseInt(result1.getString(result1.getColumnIndex("last_received_word_id")));
            last_received_word_id=get_last_received_word_id();
            max_word_id_in_db=Integer.parseInt(result2.getString(result2.getColumnIndex("ID")));
            //max_word_id_in_db=22;
            if(last_received_word_id<max_word_id_in_db){
                available=true;
            }
        }
        words_collecttion wordsCollecttion=new words_collecttion();
        try {
            max_word_id_in_db=Integer.parseInt(result2.getString(result2.getColumnIndex("ID")));
        } catch (Exception e){
            tvInScrollview.setText(String.valueOf(e));
        }
        return available;
    }

    int getWordsPerDay(){
        myDatabaseHelper=new databaseHelper(this);
        Cursor result=myDatabaseHelper.getWordsPerDay();
        int n=4;
        if(result!=null && result.moveToFirst()){
            n=Integer.parseInt(result.getString(result.getColumnIndex("words_per_day")));
        }
        return n;
    }

    private void setShowingWordLabel() {
        myDatabaseHelper=new databaseHelper(this);
        //Cursor result1=myDatabaseHelper.getWordsPerDay();
        Cursor result2=myDatabaseHelper.getNumOfWordsReceivedToday();

        //result1.moveToFirst();
        result2.moveToFirst();

        words_per_day= getWordsPerDay();
        int get_num_of_words_received_today= Integer.parseInt(result2.getString(result2.getColumnIndex("num_of_word_shown_today")));
        get_num_of_words_received_today=get_num_of_words_received_today==words_per_day? 0:get_num_of_words_received_today;
        ++get_num_of_words_received_today;

        showing_word_label=(TextView) findViewById(R.id.showing_word_label);
        bottomLabel=(TextView) findViewById(R.id.bottomLabel);
        //showing_word_label.setText("Showing word: "+String.valueOf(get_num_of_words_received_today)+ "/"+String.valueOf(words_per_day));
        String nTh=(get_num_of_words_received_today==1)? "st" : (get_num_of_words_received_today==2)? "nd" :
                (get_num_of_words_received_today==3)? "rd" : "th";
        showing_word_label.setText("Showing word No. "+(get_last_received_word_id()+1));
        bottomLabel.setText("You are learning "+words_per_day+" words everyday");
        Typeface custom_font=Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font2.TTF");
        showing_word_label.setTypeface(custom_font);


        if(!InIntervalTime()){
            myDatabaseHelper.update_num_of_words_received_today(get_num_of_words_received_today);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_words_per_day) {
            //Toast.makeText(this,"action settings",Toast.LENGTH_SHORT).show();
            show_change_words_dialog();
            return true;
        }

        if(id==R.id.revision){
            /*
            Intent intent=new Intent(MainActivity.this,revision.class);
            startActivity(intent);
            */
            showRevisionScreen();
        }

        if(id==R.id.msg_to_developer){
            msgToDeveloperDialog();
        }

        if(id==R.id.jump){
            showJumpDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showJumpDialog(){
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.jump, null);

        final EditText jumpWordField=(EditText) view.findViewById(R.id.editText2);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(view);

        builder.setCancelable(false)
                .setPositiveButton("Jump", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nStr=jumpWordField.getText().toString();
                        if(nStr.isEmpty()){
                            Toast.makeText(words_reminder.MainActivity.this,"No input",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            words_collecttion wordsCollecttion=new words_collecttion();
                            int ttl=wordsCollecttion.WORDS.length;
                            if(isInt(nStr)){
                                int n=Integer.parseInt(nStr);
                                if(n>ttl){
                                    Toast.makeText(words_reminder.MainActivity.this,"Enter a number smaller than "+(ttl+1),Toast.LENGTH_LONG).show();
                                }
                                else if(n<1){
                                    Toast.makeText(words_reminder.MainActivity.this,"Enter a number greater than 0",Toast.LENGTH_LONG).show();
                                }
                                else{
                                    myDatabaseHelper.update_last_received_word_info(n-1,System.currentTimeMillis());
                                    Toast.makeText(words_reminder.MainActivity.this,"Ok. Start learning from word No "+n,Toast.LENGTH_LONG).show();
                                    showing_word_label.setText("Showing word No "+n);
                                    tvInScrollview.setText(get_word_details()[0]);
                                }
                            }
                            else{
                                Toast.makeText(words_reminder.MainActivity.this,"Enter an integer",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setTitle("Skip some words")
                .create().show();
    }

    private boolean isInt(String nStr) {
        return !nStr.contains(".");
    }

    private void msgToDeveloperDialog(){
        data_exchanger data_exchanger=new data_exchanger();
        final boolean isNetwork=data_exchanger.isNetworkAvailable(this);
        if(!isNetwork){
            Toast.makeText(words_reminder.MainActivity.this,"Please enable internet connection",Toast.LENGTH_LONG).show();
        }

        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.msg_to_developer_xml, null);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(view);

        final EditText msg_field=(EditText) view.findViewById(R.id.editText);

        builder.setCancelable(false)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String msg=msg_field.getText().toString();
                        //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                        if(isNetwork){
                            //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                            new requestToServer(words_reminder.MainActivity.this,33).execute(getDeviceId(),msg);
                            Toast.makeText(words_reminder.MainActivity.this,"Thank you for your feedback",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(words_reminder.MainActivity.this,"Opps! Internet not connected",Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setTitle("Contact to developer")
                .create().show();
    }

    private void showRevisionWordsTill(int id){
        myDatabaseHelper=new databaseHelper(this);
        Cursor result=myDatabaseHelper.wordsTill(id);
        String x="";
        if(result!=null && result.moveToFirst()){
            while (!result.isAfterLast()){
                x+=result.getString(0)+"\n------------\n";
            }

        }
    }

    private void show_change_words_dialog() {
        LayoutInflater inflater=LayoutInflater.from(this);
        View wordsPerDayDialog=inflater.inflate(R.layout.change_words_per_day_dialog, null);

        final Spinner wordNumSpinner=(Spinner) wordsPerDayDialog.findViewById(R.id.wordNumListSpinner);
        wordNumSpinner.setSelection(words_per_day-1);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(wordsPerDayDialog);

        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this,"Ok clicked",Toast.LENGTH_SHORT).show();
                        String w=wordNumSpinner.getSelectedItem().toString();
                        bottomLabel=(TextView) findViewById(R.id.bottomLabel);
                        bottomLabel.setText("You are learning "+w+" words everyday");

                        myDatabaseHelper=new databaseHelper(words_reminder.MainActivity.this);
                        myDatabaseHelper.do_change_number_of_words(w);
                        setSchedule();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this,"Cancel clicked",Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                })
                .setTitle("Change word number")
                .create().show();
    }

    private void showRevisionScreen(){
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.revision_screen, null);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(view);

        builder.setCancelable(false)
                .setTitle("Revision")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();


        List<String> li;
        li=new ArrayList<String>();

        myDatabaseHelper=new databaseHelper(this);
        Cursor result=myDatabaseHelper.wordsTill(last_received_word_id);
        //String x="";
        if(result!=null && result.moveToFirst()){
            int i=1;
            while (!result.isAfterLast()){
                //x+=result.getString(0)+"\n------------\n";
                li.add("["+(i++)+"]"+result.getString(0));
                result.moveToNext();
            }
            ListView listView=(ListView) view.findViewById(R.id.listView1);
            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,R.layout.list,li);
            listView.setAdapter(arrayAdapter);
        }
    }

}
