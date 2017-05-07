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

public class DownloadArticleData extends AsyncTask<String, Void, String> {
    NewsService newsService;
    String article;
    Context context;
    int statusCode;

    public DownloadArticleData(NewsService newsService, String article) {
        this.newsService = newsService;
        this.context = newsService.getApplicationContext();
        if (article.equals("all") || article.equals("")) {
            this.article = "";
        } else {
            this.article = article;
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
        System.out.println("doInBackground article:-"+this.article+"-");
        String urlString = "https://newsapi.org/v1/articles?source="+this.article +"&apiKey=e084dde5de094a26a9613bd8065392ae";
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
    ArrayList<Article> articleList = new ArrayList<>();

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("Return Response: ");
        System.out.println(s);
        if (statusCode == HttpURLConnection.HTTP_OK) {
            JSONObject response;
            JSONArray articles;
            try {
                response = new JSONObject(s);
                articles = response.getJSONArray("articles");
                for (int i=0; i<articles.length(); i++) {
                    JSONObject jObj = articles.getJSONObject(i);
                    String author = jObj.getString("author");
                    String title = jObj.getString("title");
                    String description = jObj.getString("description");
                    String urlToImage = jObj.getString("urlToImage");
                    String publishedAt = jObj.getString("publishedAt");
                    Article article = new Article(author,title,description,urlToImage,publishedAt);
                    articleList.add(article);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        newsService.setArticles(articleList);
    }
}
