package com.example.dblab_d.seung_ossleeper;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;

import flexjson.JSONSerializer;


class SeungOProtocol {
    boolean isSleep;
    boolean isWakeup;
    boolean isGetData;
    boolean isReturnData;
    boolean okSign;
    String dataID;
    byte[] data;
    String base64data;
}

public class MainActivity extends AppCompatActivity {

    TimePicker timePicker;
    int hour, min;
    static String datachart = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        Button sleepButton = (Button) findViewById(R.id.sleepButton);
        final TextView textView = (TextView) findViewById(R.id.textView);

        //Calendar calendar = Calendar.getInstance();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeungOProtocol sop = new SeungOProtocol();
                sop.isGetData = true;
                JSONSerializer serializer = new JSONSerializer();
                String class_to_json = serializer.serialize(sop);

                DataRequest dr = new DataRequest("113.198.236.243", class_to_json);
                dr.start();
                try {
                    dr.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getApplicationContext(), DataActivity.class);
                intent.putExtra("datachart", datachart);
                startActivity(intent);
            }
        });
        sleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hour = MainActivity.this.timePicker.getHour();
                    min = MainActivity.this.timePicker.getMinute();
                } else {
                    hour = MainActivity.this.timePicker.getCurrentHour();
                    min = MainActivity.this.timePicker.getCurrentMinute();
                }
                SeungOProtocol sop = new SeungOProtocol();
                sop.isSleep = true;
                sop.data = new byte[2];
                sop.data[0] = (byte) hour;
                sop.data[1] = (byte) min;
                sop.base64data = Base64.encodeToString(sop.data, Base64.DEFAULT);
                JSONSerializer serializer = new JSONSerializer();
                String class_to_json = serializer.serialize(sop);

                textView.setText(class_to_json);
                //Toast.makeText(getApplicationContext(), class_to_json, Toast.LENGTH_LONG).show();

                SleepRequest sr = new SleepRequest("113.198.235.99", class_to_json);
                sr.start();
                try {
                    sr.join();
                } catch (InterruptedException e) {

                } catch (Exception e) {

                }

                Intent intent = new Intent(getApplicationContext(), SleepActivity.class);
                intent.putExtra("ahour", hour);
                intent.putExtra("amin", min);
                startActivity(intent);
            }
        });
    }
}

class SleepRequest extends Thread {
    String hostname;
    String data;

    public SleepRequest(String addr, String json) {
        hostname = addr;
        data = json;
    }

    public void run() {
        try {
            Socket socket = new Socket("113.198.236.243", 6528);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(data);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String str = "";
            while (true) {
                try {
                    str += in.readLine();
                    if (str.contains("<EOF>")) {
                        str = str.replace("<EOF>", "");
                        JsonParser parser = new JsonParser();
                        JsonElement element = parser.parse(str);
                        if (element.getAsJsonObject().get("okSign").getAsBoolean())
                            break;
                    }
                } catch (IOException e) {

                } catch (Exception e) {

                }
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class DataRequest extends Thread {
    String hostname;
    String data;

    public DataRequest(String addr, String json) {
        hostname = addr;
        data = json;
    }

    public void run() {
        try {
            Socket socket = new Socket("113.198.236.243", 6528);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(data);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String str = "";
            while (true) {
                try {
                    str += in.readLine();
                    if (str.contains("<EOF>")) {
                        str = str.replace("<EOF>", "");
                        JsonParser parser = new JsonParser();
                        JsonElement element = parser.parse(str);
                        if (element.getAsJsonObject().get("okSign").getAsBoolean()) {
                            MainActivity.datachart = element.getAsJsonObject().get("dataID").getAsString();

                        }
                        break;
                    }
                } catch (IOException e) {
                    break;
                } catch (Exception e) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

/*

package graphtest;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class tf extends ApplicationFrame {

   public tf( String applicationTitle , String chartTitle ) {
      super(applicationTitle);
      JFreeChart lineChart = ChartFactory.createLineChart(
         chartTitle,
         "Time","Sleep Data",
         createDataset(),
         PlotOrientation.VERTICAL,
         true,true,false);

      ChartPanel chartPanel = new ChartPanel( lineChart );
      chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
      setContentPane( chartPanel );
   }
   int []time_data= {99,98,70,44,23,18,0,55,30,65};

   private DefaultCategoryDataset createDataset( ) {
      DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
      for(int i=0;i<10;i++) {
       dataset.addValue( time_data[i] , "time" , ""+i );

      }
      return dataset;
   }

   public static void main( String[ ] args ) {
      tf chart = new tf(
         "수면상태" ,
         "Sleeping State");

      chart.pack( );
      RefineryUtilities.centerFrameOnScreen( chart );
      chart.setVisible( true );
   }
}

 */