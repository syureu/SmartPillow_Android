package com.example.dblab_d.seung_ossleeper;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import flexjson.JSONSerializer;

import static com.example.dblab_d.seung_ossleeper.MainActivity.datachart;

public class DataActivity extends Activity {
    String[] tocken;
    static String graphdata;
    static String graphdrawdata;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_main);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);


        String datachart = getIntent().getStringExtra("datachart");
        Button returnToMainButton = (Button) findViewById(R.id.returnToMainButton);

        StringTokenizer st = new StringTokenizer(datachart,"<");
        tocken = new String[st.countTokens()];
        for(int i=0; i<st.countTokens(); i++) {
            tocken[i] = st.nextToken();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tocken);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String select = String.valueOf(tocken[position]);
                SeungOProtocol sop = new SeungOProtocol();
                sop.isGetData = true;
                sop.dataID = select;
                JSONSerializer serializer = new JSONSerializer();
                String class_to_json = serializer.serialize(sop);

                FileRequest fr = new FileRequest("113,198,236,243", class_to_json);
                fr.start();
                try {
                    fr.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(graphdata.equals("Fail")) {
                    Toast.makeText(getApplicationContext(), "값을 얻어오는데 실패 했습니다.", Toast.LENGTH_LONG);
                    return;
                }
                byte[] bytes = Base64.decode(graphdata, 0);
                try {
                    graphdrawdata = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        returnToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

class FileRequest extends Thread {
    String hostname;
    String data;

    public FileRequest(String addr, String json) {
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
                        DataActivity.graphdata = element.getAsJsonObject().get("base64data").getAsString();
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
