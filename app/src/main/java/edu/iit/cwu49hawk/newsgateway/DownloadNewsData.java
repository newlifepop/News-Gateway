package edu.iit.cwu49hawk.newsgateway;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class DownloadNewsData extends AsyncTask<String, Void, String> {
    MainActivity mainActivity;
    String category;
    Context context;
    int statusCode;

    public DownloadNewsData(MainActivity mainActivity, String category) {
        this.mainActivity = mainActivity;
        this.context = mainActivity.getApplicationContext();
        if (category.equals("all") || category.equals("")) {
            this.category = "";
        } else {
            this.category = category;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Toast.makeText(mainActivity, "Fetching news...", Toast.LENGTH_SHORT).show();
        System.out.println("Fetching news...");
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        System.out.println("doInBackground source:-"+this.category+"-");
        String urlString;
        urlString = "https://newsapi.org/v1/sources?language=en&country=us&category="+this.category+"&apiKey=e084dde5de094a26a9613bd8065392ae";
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    // System.out.println("DownloadedCategoryData Response is: " + line);
                }
                return buffer.toString();
            } else {
                System.out.println("Status code is " + statusCode);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    ArrayList<Source> sourceList = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        System.out.println("Return Response: ");
        System.out.println(s);
        if (statusCode == HttpURLConnection.HTTP_OK) {
            JSONObject response;
            JSONArray sources;
            try {
                response = new JSONObject(s);
                sources = response.getJSONArray("sources");
                for (int i=0; i<sources.length(); i++) {
                    JSONObject jObj = sources.getJSONObject(i);
                    String id = jObj.getString("id");
                    String name = jObj.getString("name");
                    String url = jObj.getString("url");
                    String category = jObj.getString("category");
                    Source source = new Source(id,name,url,category);
                    sourceList.add(source);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //done adding to sourceList;

        for (int i=0; i<sourceList.size(); i++) {
            categories.add(sourceList.get(i).getCategory());
        }
        //done
        HashSet<String> uniqueNames = new HashSet<>(categories);
        categories.clear();
        for (String x : uniqueNames) {
            categories.add(x);
        }
        for (int i =0; i<categories.size(); i++) {
            System.out.println("Unique Category: "+i+" "+categories.get(i));
        }
        mainActivity.setSources(sourceList,categories);
    }
}
