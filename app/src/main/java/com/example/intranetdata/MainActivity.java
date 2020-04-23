package com.example.intranetdata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<String> links = new ArrayList<>();    // links to the pages
    ArrayList<String> title = new ArrayList<>();    // title of the pages
    ArrayList<Integer> icons = new ArrayList<>();   // icons based upon the file extension
    ArrayList<String> dateMod = new ArrayList<>();  // modify date
    ArrayList<String> timeMod = new ArrayList<>();  // modify time

    // base url is used when going to the parent directory
    String baseUrl = "http://intranet.daiict.ac.in/";
    // current url is used to go into a directory or open a file
    String currentUrl = "http://intranet.daiict.ac.in/~daiict_nt01/";

    // custom array list adapter
    MyListAdapter adapter;

    // to check back button pressed twice
    boolean doubleBackPressed = false;

    // map containing the extensions and corresponding icon value (R.drawble. )
    HashMap<String, Integer> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // list view showing directories and files
        listView = (ListView) findViewById(R.id.listView);
        // custom array adapter
        adapter = new MyListAdapter(this, title, icons, dateMod, timeMod);
        listView.setAdapter(adapter);

        // show back icon on action
        // used to go back to the parent directory
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        putIntoMap();   // put the icons values corresponding to the extension

        String res = "";
        try {
            DownloadData downloadData = new DownloadData();
            res = downloadData.execute(baseUrl+"~daiict_nt01/").get();

            // get the required titles, links, date and time from the parsed HTML
            getDataFromHTML(res);
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // go to new url
                // links array list contains link to parent directory at 0th position
                // so i+1 to get the correct link
                goToNewURL(i+1);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // toast the title
                Toast.makeText(MainActivity.this, title.get(i), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public class DownloadData extends AsyncTask <String, Void, String> {    // parse the html file from URL
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

            // go to parent directory
            if (i == 0) {
                newUrl = baseUrl + links.get(i);
                currentUrl = baseUrl + links.get(i);
            }
            // go to a directory or open a file
            else {
                newUrl = currentUrl + links.get(i);
                currentUrl = currentUrl + links.get(i);
            }

            resNew = downloadData.execute(newUrl).get();

            // get data from the parsed HTML
            getDataFromHTML(resNew);
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getDataFromHTML (String res) {
        // clear all array list so as to add new data
        title.clear();
        links.clear();
        icons.clear();
        dateMod.clear();
        timeMod.clear();

        // get all the links to the directory and files
        Pattern p = Pattern.compile("<td><a href=\"(.*?)\">");
        Matcher m = p.matcher(res);
        while (m.find()) {
            links.add(m.group(1));
        }

        // get the name of all directories and the files
        p = Pattern.compile("\">(.[^\">]*?)</a></td>");
        m = p.matcher(res);
        while (m.find()) {
            String data = m.group(1);
            // if it is a directory
            if (data.charAt(data.length()-1) == '/') {
                title.add(data.substring(0, data.length()-1));
                // It is a folder
                // add icon to array list
                icons.add(map.get("folder"));

            }
            // if it is a file
            else {
                // if it is a temporary file
                if (data.charAt(0) != '~') {
                    title.add(data);
                }
                // It is a file
                // get the extension of the file and set corresponding icon matching the extension
                String type = "";
                if (data.charAt(data.length()-4) == '.') {
                    type = data.substring(data.length()-4);
                }
                else if (data.charAt(data.length()-5) == '.') {
                    type = data.substring(data.length()-5);
                }
                // add icon to array list
                icons.add(map.getOrDefault(type, R.drawable.fileicon));
            }
        }

        // "24-Apr-2019 12:40  "
        // get the modified date and time
        p = Pattern.compile("</td><td align=\"right\">(.*?)</td><td align=\"right\">");
        m = p.matcher(res);
        while (m.find()) {
            String data = m.group(1);
            String date = data.substring(0, 11);    // extract the date
            String time = data.substring(12, 17);   // extract the time
            dateMod.add(date);
            timeMod.add(time);
        }

        // remove the parent directory fro the list
        title.remove(0);
        icons.remove(0);

        Log.i("Titles --------->", title.toString());
        Log.i("Links --------->", links.toString());

        // notify change in the list
        adapter.notifyDataSetChanged();
        // scroll to the top of the list
        listView.smoothScrollToPosition(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // back button on action bar clicked
        if (item.getItemId() == android.R.id.home) {
            // do nothing if we are already on the home page
            if (!links.get(0).equals("/")) {
                goToNewURL(0);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // double tap back button to exit the app
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

    // put the extensions nad the corresponding icon value
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
        map.put(".png", R.drawable.png);
        map.put(".log", R.drawable.log);
        map.put(".rar", R.drawable.rar);
        map.put(".sql", R.drawable.sql);
        map.put(".tex", R.drawable.tex);
        map.put(".zip", R.drawable.zip);
    }
}
