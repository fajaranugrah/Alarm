package com.example.fajarramadhan.alarm;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    //Pending intent instance
    private PendingIntent pendingIntent;

    private RadioButton secondsRadioButton, minutesRadioButton, hoursRadioButton;

    //Alarm Request Code
    private static final int ALARM_REQUEST_CODE = 133;

    final private int REQUEST_STORAGE_PERMISSION = 124;
    String[] PERMISSIONS_STO = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String date = sdf.format(new Date());
                    new SaveWallpaperAsync(MainActivity.this, "https://storage.googleapis.com/pb-assets/user-images/full/39b2de8f34.5bd939841f27b.jpg", date).execute();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find id of all radio buttons
        secondsRadioButton = (RadioButton) findViewById(R.id.seconds_radio_button);
        minutesRadioButton = (RadioButton) findViewById(R.id.minutes_radio_button);
        hoursRadioButton = (RadioButton) findViewById(R.id.hours_radio_button);

        /* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, ALARM_REQUEST_CODE, alarmIntent, 0);

        //Find id of Edit Text
        final EditText editText = (EditText) findViewById(R.id.input_interval_time);

        //Set On CLick over start alarm button
        findViewById(R.id.start_alarm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getInterval = editText.getText().toString().trim();//get interval from edittext

                //check interval should not be empty and 0
                if (!getInterval.equals("") && !getInterval.equals("0"))
                    //finally trigger alarm manager
                    triggerAlarmManager(getTimeInterval(getInterval));

            }
        });

        //set on click over stop alarm button
        findViewById(R.id.stop_alarm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop alarm manager
                stopAlarmManager();
            }
        });

        Button button = (Button) findViewById(R.id.save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasPermissions(MainActivity.this, PERMISSIONS_STO)){
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STO, REQUEST_STORAGE_PERMISSION);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String date = sdf.format(new Date());
                    new SaveWallpaperAsync(MainActivity.this, "https://storage.googleapis.com/pb-assets/user-images/full/39b2de8f34.5bd939841f27b.jpg", date).execute();
                }
            }
        });

    }

    private class SaveWallpaperAsync extends AsyncTask<String, String, String> {
        private Context context;
        private ProgressDialog pDialog;
        String image_url;
        String DateNow;
        URL ImageUrl;
        Bitmap bmImg = null;

        public SaveWallpaperAsync(Context context, String url, String date) {
            this.context = context;
            this.image_url = url;
            this.DateNow = date;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            try {
                pDialog = new ProgressDialog(context);
                pDialog.setMessage("Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            InputStream is = null;
            try {
                ImageUrl = new URL(image_url);
                //ImageUrl = new URL(args[0]);
                // myFileUrl1 = args[0];

                HttpURLConnection conn = (HttpURLConnection) ImageUrl
                        .openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bmImg = BitmapFactory.decodeStream(is, null, options);

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                /*String path = ImageUrl.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);*/
                Long tsLong = System.currentTimeMillis();
                String NameImage = "Petbacker-"+ DateNow + "-" + tsLong.toString() + ".jpg";

                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsoluteFile() + "/PetBacker/");
                dir.mkdirs();
                String fileName = NameImage;
                File file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bmImg.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                File imageFile = file;
                MediaScannerConnection.scanFile(context,
                        new String[] { imageFile.getPath() },
                        new String[] { "image/jpeg" }, null);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String args) {
            // TODO Auto-generated method stub
            if (bmImg == null) {
                Toast.makeText(context, "Image still loading...",
                        Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            } else {
                if (pDialog!=null) {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                }
                Toast.makeText(context, "Wallpaper Successfully Saved",
                        Toast.LENGTH_SHORT).show();

            }
        }

    }

    //get time interval to trigger alarm manager
    private int getTimeInterval(String getInterval) {
        int interval = Integer.parseInt(getInterval);//convert string interval into integer

        //Return interval on basis of radio button selection
        if (secondsRadioButton.isChecked())
            return interval;
        if (minutesRadioButton.isChecked())
            return interval * 60;//convert minute into seconds
        if (hoursRadioButton.isChecked()) return interval * 60 * 60;//convert hours into seconds

        //else return 0
        return 0;
    }


    //Trigger alarm manager with entered time interval
    public void triggerAlarmManager(int alarmTriggerTime) {
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add alarmTriggerTime seconds to the calendar object
        cal.add(Calendar.SECOND, alarmTriggerTime);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//get instance of alarm manager
        manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);//set alarm manager with entered timer by converting into milliseconds

        Toast.makeText(this, "Alarm Set for " + alarmTriggerTime + " seconds.", Toast.LENGTH_SHORT).show();
    }

    //Stop/Cancel alarm manager
    public void stopAlarmManager() {

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);//cancel the alarm manager of the pending intent


        //Stop the Media Player Service to stop sound
        stopService(new Intent(MainActivity.this, AlarmSoundService.class));

        //remove the notification from notification tray
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(AlarmNotificationService.NOTIFICATION_ID);

        Toast.makeText(this, "Alarm Canceled/Stop by User.", Toast.LENGTH_SHORT).show();
    }
}
