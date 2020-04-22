package com.example.intranetdata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PatternMatcher;
import android.service.autofill.FieldClassification;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<String> links = new ArrayList<>();
    ArrayList<String> title = new ArrayList<>();
    ArrayList<Integer> icons = new ArrayList<>();
    ArrayList<String> dateMod = new ArrayList<>();
    ArrayList<String> timeMod = new ArrayList<>();

    String baseUrl = "http://intranet.daiict.ac.in/";
    String currentUrl = "http://intranet.daiict.ac.in/~daiict_nt01/";

    MyListAdapter adapter;

    boolean doubleBackPressed = false;

    HashMap<String, Integer> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new MyListAdapter(this, title, icons, dateMod, timeMod);
        listView.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        putIntoMap();

        String res = "";
        try {
            DownloadData downloadData = new DownloadData();
            res = downloadData.execute(baseUrl+"~daiict_nt01/").get();

            getDataFromHTML(res);
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                goToNewURL(i+1);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, title.get(i), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public class DownloadData extends AsyncTask <String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            StringBuilder result = new StringBuilder();

            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();

                InputStream input = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(input);

                int dataRead = reader.read();

                while (dataRead != -1) {
                    char currentData = (char) dataRead;
                    result.append(currentData);
                    dataRead = reader.read();
                }
                return result.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void goToNewURL (int i) {
        String resNew = "";
        DownloadData downloadData = new DownloadData();
        try {
            String newUrl;

            if (i == 0) {
                newUrl = baseUrl + links.get(i);
                currentUrl = baseUrl + links.get(i);
            }
            else {
                newUrl = currentUrl + links.get(i);
                currentUrl = currentUrl + links.get(i);
            }

            resNew = downloadData.execute(newUrl).get();

            getDataFromHTML(resNew);
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getDataFromHTML (String res) {
        title.clear();
        links.clear();
        icons.clear();

        dateMod.clear();
        timeMod.clear();

        Pattern p = Pattern.compile("<td><a href=\"(.*?)\">");
        Matcher m = p.matcher(res);
        while (m.find()) {
            links.add(m.group(1));
        }

        p = Pattern.compile("\">(.[^\">]*?)</a></td>");
        m = p.matcher(res);
        while (m.find()) {
            String data = m.group(1);
            if (data.charAt(data.length()-1) == '/') {
                title.add(data.substring(0, data.length()-1));
                // It is a folder
                icons.add(map.get("folder"));

            }
            else {
                title.add(data);
                // It is a file

                String type = "";
                if (data.charAt(data.length()-4) == '.') {
                    type = data.substring(data.length()-4);
                }
                else if (data.charAt(data.length()-5) == '.') {
                    type = data.substring(data.length()-5);
                }

                icons.add(map.getOrDefault(type, R.drawable.fileicon));
            }
        }

        // "24-Apr-2019 12:40  "

        p = Pattern.compile("</td><td align=\"right\">(.*?)</td><td align=\"right\">");
        m = p.matcher(res);
        while (m.find()) {
            String data = m.group(1);
            String date = data.substring(0, 11);
            String time = data.substring(12, 17);
            dateMod.add(date);
            timeMod.add(time);
        }

        title.remove(0);
        icons.remove(0);

        Log.i("Titles --------->", title.toString());
        Log.i("Links --------->", links.toString());

        adapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!links.get(0).equals("/")) {
                goToNewURL(0);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackPressed) {
            super.onBackPressed();
            return;
        }
        this.doubleBackPressed = true;
        Toast.makeText(this, "click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackPressed = false;
            }
        }, 2000);
    }

    public void putIntoMap () {
        map.put("folder", R.drawable.foldericon);
        map.put("file", R.drawable.fileicon);
        map.put(".pdf", R.drawable.pdf);
        map.put(".txt", R.drawable.txt);
        map.put(".doc", R.drawable.doc);
        map.put(".docx", R.drawable.docx);
        map.put(".ppt", R.drawable.ppt);
        map.put(".pptx", R.drawable.pptx);
        map.put(".jpg", R.drawable.jpg);
        map.put(".log", R.drawable.log);
        map.put(".rar", R.drawable.rar);
        map.put(".sql", R.drawable.sql);
        map.put(".tex", R.drawable.tex);
        map.put(".zip", R.drawable.zip);
    }
}
