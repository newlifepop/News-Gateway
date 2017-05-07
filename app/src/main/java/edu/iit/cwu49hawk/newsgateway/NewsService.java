package edu.iit.cwu49hawk.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.util.ArrayList;

public class NewsService extends Service {
    private ServiceReceiver serviceReceiver;
    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    private boolean running = true;
    public ArrayList articles;

    public NewsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("service started");
        serviceReceiver = new ServiceReceiver();
        IntentFilter filter1 = new IntentFilter(ACTION_MSG_TO_SERVICE);
        registerReceiver(serviceReceiver,filter1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (articles == null || articles.size() == 0) {
                        try {
                            Thread.sleep(250);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Intent intent = new Intent();
                        intent.setAction("ACTION_NEWS_STORY");
                        intent.putExtra("ARTICLES",articles);
                        sendBroadcast(intent);
                        articles.clear();
                    }
                }
            }
        }).start();

        return Service.START_STICKY;
    }
    public void setArticles(ArrayList<Article> articles) {
        if (this.articles != null) {
            this.articles.clear();
            this.articles = articles;
        } else {
            this.articles = articles;
        }
    }

    @Override
    public void onDestroy() {
        System.out.println("Service destroyed");
        running = false;
        super.onDestroy();
    }

    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("In onRecieve");
            if (intent.getAction().equals(ACTION_MSG_TO_SERVICE)) {
                Source currentSource = intent.getParcelableExtra("CURRENTSOURCE");
                String id = currentSource.getId();
                System.out.println("id is "+id);
                //fetch data
                new DownloadArticleData(NewsService.this,id).execute();
            }
        }
    }
}
