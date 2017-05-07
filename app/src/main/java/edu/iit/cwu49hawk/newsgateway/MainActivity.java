package edu.iit.cwu49hawk.newsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NewsReceiver newsReceiver;
    private MyPageAdapter pageAdapter;
    private List<Fragment> fragments;
    private ViewPager pager;
    private Menu menu;
    private ImageView background;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ArrayAdapter listAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<String> sourceNames = new ArrayList<>();
    HashMap<String,Source> sourceMap = new HashMap<>();
    ArrayList<String> categories;
    ArrayList<Article> articles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, NewsService.class);
        startService(intent);

        newsReceiver = new NewsReceiver();
        //background = (ImageView)findViewById(R.id.background);
        IntentFilter filter1 = new IntentFilter("ACTION_NEWS_STORY");
        registerReceiver(newsReceiver, filter1);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        listAdapter = new ArrayAdapter<>(this,R.layout.drawer_list_item,sourceNames);
        mDrawerList.setAdapter(listAdapter);
        mDrawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectItem(position);
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragments = getFragments();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        new DownloadNewsData(this,"").execute();

    }

    public void setSources(ArrayList<Source> sources, ArrayList<String> uniqueNames) {
        sourceNames.clear();
        sourceMap.clear();

        for (int i=0; i<sources.size(); i++) {
            //populate sourceNames list
            sourceNames.add(sources.get(i).getName());
        }
        for (int i=0; i<sources.size(); i++) {
            sourceMap.put(sources.get(i).getName(),sources.get(i));
        }
        if (categories == null || categories.size() == 0) {
            categories = uniqueNames;
            categories.add(0,"all");
        }
        for (int i=0; i<categories.size(); i++) {
            menu.add(categories.get(i));
            System.out.println("Category in menu "+categories.get(i));
        }
        listAdapter.notifyDataSetChanged();
    }

    class NewsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACTION_NEWS_STORY")) {
                articles = intent.getParcelableArrayListExtra("ARTICLES");
                reDoFragments(articles);
            }
        }
    }

    private void selectItem(int position) {
//        Toast.makeText(this, sourceNames.get(position), Toast.LENGTH_SHORT).show();
//        setTitle(sourceNames.get(position));
//        reDoFragments(position);
        //background.setVisibility(View.INVISIBLE);
        mDrawerLayout.setBackgroundResource(0);
        setTitle(sourceNames.get(position));
        Intent intent  = new Intent();
        intent.setAction(NewsService.ACTION_MSG_TO_SERVICE);
        intent.putExtra("CURRENTSOURCE",sourceMap.get(sourceNames.get(position)));
        sendBroadcast(intent);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void reDoFragments(ArrayList<Article> articles) {

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        fragments.clear();
        for (int i=0; i<articles.size(); i++) {
            fragments.add(MyFragment.newInstance(articles.get(i)));
        }
        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        System.out.println("item clicked is "+item.toString());
        //download new data
        new DownloadNewsData(this,item.toString()).execute();
        return true;
        //return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(newsReceiver);
        super.onDestroy();
    }

    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<>();
        return fList;
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
            //return super.getItemPosition(object);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
            //return null;
        }

        @Override
        public int getCount() {
            return fragments.size();
            //return 0;
        }

        @Override
        public long getItemId(int position) {
            return baseId + position;
            //return super.getItemId(position);
        }
        public void notifyChangeInPosition(int n) {
            baseId += getCount() + n;
        }
    }
}
