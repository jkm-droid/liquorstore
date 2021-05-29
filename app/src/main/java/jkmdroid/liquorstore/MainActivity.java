package jkmdroid.liquorstore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by jkm-droid on 27/05/2021.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    TabLayout tabLayout;
    ViewPager viewPager;
    int selectedTab = 0;
    DrawerLayout drawer;
    boolean stopThread = false;
    FragmentHome fragmentHome;
    FragmentAllDrinks fragmentAllDrinks;
    private ArrayList<Drink>  homeDetails, allDrinks;
    boolean requestSuccessful = false;
    TextView textCartItemCount;
    int mCartItemCount = 10;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar) ;
        drawer = findViewById(R.id.drawer_layout) ;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer , toolbar , R.string.navigation_drawer_open ,
                R.string.navigation_drawer_close) ;
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view ) ;
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        start_drinks();
    }
    void start_drinks(){

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            selectedTab = extras.getInt("tab", 0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 3);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 88);
        }

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        tabLayout.addTab(tabLayout.newTab().setText(""));
        tabLayout.getTabAt(0).setIcon(R.drawable.home);

        tabLayout.addTab(tabLayout.newTab().setText(""));
        tabLayout.getTabAt(1).setIcon(R.drawable.alldrinks);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        final MyAdapter adapter = new MyAdapter(MainActivity.this, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab){

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.getTabAt(selectedTab).select();
        getdrinks();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopThread = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopThread = false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat. START )) {
            drawer.closeDrawer(GravityCompat. START ) ;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you really want to exit?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            finish();
                        }
                    }).create().show();
        }
    }

    String string = "https://play.google.com/store/apps/details?id=jkmdroid.liquorstore";

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem){
        if (drawer.isDrawerOpen(GravityCompat. START )){
            drawer.closeDrawer(GravityCompat. START ) ;
        }

        switch (menuItem.getItemId()) {
            case R.id.privacy:
                Intent privacy = new Intent(Intent.ACTION_VIEW);
                privacy.setData(Uri.parse("https://infinity.mblog.co.ke/info/privacy.html"));
                startActivity(privacy);
                break;
            case R.id.terms:
                Intent terms = new Intent(Intent.ACTION_VIEW);
                terms.setData(Uri.parse("https://infinity.mblog.co.ke/info/terms.html"));
                startActivity(terms);
                break;
            case R.id.developers:
                Intent dev = new Intent(Intent.ACTION_VIEW);
                dev.setData(Uri.parse("https://jpdevelopers.mblog.co.ke"));
                startActivity(dev);
                break;
            case R.id.whatsapp:
                PackageManager packageManager = getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);

                try {
                    String url = "https://api.whatsapp.com/send?phone="+
                            URLEncoder.encode("+254738801655", "UTF-8") +"&text=" + URLEncoder.encode("Hello Infinity Movies", "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.email:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "infinitymovies23@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "");
                email.putExtra(Intent.EXTRA_TEXT, "Hello Infinity Movies");

                //need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));
                break;
            case R.id.telegram:
                String url = "http://t.me/toptierodds";
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse(url));
                startActivity(Intent.createChooser(intent1, "Choose browser"));
                break;
            case R.id.share_button:
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Hello Liquor Store");
                        intent.putExtra(Intent.EXTRA_TEXT, string);
                        startActivity(Intent.createChooser(intent, "Share with"));
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(MainActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
                    }

                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart, menu);
        final MenuItem menuItem = menu.findItem(R.id.cart);

        View actionView = menuItem.getActionView();
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

    private void setupBadge() {
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.cart) {
            try {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Drink> extractDrinks(JSONObject response) {
        JSONArray array;
        JSONObject object;
        ArrayList<Drink> drinks = new ArrayList<>();
        try {
            array = response.getJSONArray("drinks");
            Drink drink;
            int s = array.length();
            for (int i = 0; i < s; i++){
                drink = new Drink();
                object = array.getJSONObject(i);
                drink.setId(object.getInt("id"));
                drink.setName(object.getString("drink_name"));
                drink.setPrice(object.getInt("drink_price"));
                drink.setDescription(object.getString("drink_description"));
                drink.setPosterurl(object.getString("posterurl"));
                drink.setCategory(object.getString("drink_category"));

                drinks.add(drink);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return drinks;
    }

    private void getdrinks() {
        @SuppressLint("HandlerLeak") Handler handler = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                String data = "";
                try {
                    data += URLEncoder.encode("get_drinks", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8") + "&";
                    data += URLEncoder.encode("keyword", "UTF-8") + "=" + URLEncoder.encode("recommended", "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String url = "https://liquorstore.mblog.co.ke/drinks/get_drinks.php?" + data;

                PostJson postJson = new PostJson(MainActivity.this, url), postJson1;
                postJson.setOnSuccessListener(response -> {
                    requestSuccessful = true;
                    homeDetails = extractDrinks(response);

                    if (fragmentHome == null){
                        fragmentHome = new FragmentHome();
                        fragmentHome.setOnFragmentRestart(() -> {
                            if (homeDetails != null)
                                fragmentHome.setDrinks(homeDetails);
                            else fragmentHome.setDrinks(new ArrayList<>());
                        });
                    }
                    fragmentHome.setDrinks(homeDetails);

                });
                postJson.get();

                //get home details
                data  = "";
                try {
                    data += URLEncoder.encode("get_drinks", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8") + "&";
                    data += URLEncoder.encode("keyword", "UTF-8") + "=" + URLEncoder.encode("all_drinks", "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                url = "https://liquorstore.mblog.co.ke/drinks/get_drinks.php?" + data;
                postJson1 = new PostJson(MainActivity.this, url);
                postJson1.setOnSuccessListener(new PostJson.OnSuccessListener() {
                    @Override
                    public void onSuccess(JSONObject response) throws JSONException {
                        allDrinks = extractDrinks(response);

                        requestSuccessful = true;

                        if (fragmentAllDrinks == null){
                            fragmentAllDrinks = new FragmentAllDrinks();
                            fragmentAllDrinks.setOnFragmentRestart( () -> {
                                if (allDrinks != null)
                                    fragmentAllDrinks.setDrinks(allDrinks);
                                else fragmentAllDrinks.setDrinks(new ArrayList<>());
                            });
                        }
                        fragmentAllDrinks.setDrinks(allDrinks);
                    }
                });
                postJson1.get();
            }
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                if (stopThread)
                    return;
                handler.sendEmptyMessage(1);

                try {
                    if (requestSuccessful)
                        sleep(120000);
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                run();
            }
        };
        thread.start();
    }

    public class MyAdapter extends FragmentPagerAdapter {
        Context context;
        int totalTabs;

        public MyAdapter(Context context, FragmentManager fm, int totalTabs) {
            super(fm);
            this.context = context;
            this.totalTabs = totalTabs;
        }

        // this is for fragment tabs
        @Override
        public Fragment getItem(int position){
            switch (position){
                case 0:
                    if (fragmentHome == null){
                        fragmentHome = new FragmentHome();
                        fragmentHome.setOnFragmentRestart(() -> {
                            if (homeDetails != null)
                                fragmentHome.setDrinks(homeDetails);
                            else fragmentHome.setDrinks(new ArrayList<>());
                        });
                    }
                    if (homeDetails != null)
                        fragmentHome.setDrinks(homeDetails);
                    return fragmentHome;

                case 1:
                    if (fragmentAllDrinks == null){
                        fragmentAllDrinks = new FragmentAllDrinks();
                        fragmentAllDrinks.setOnFragmentRestart(() -> {
                            if (allDrinks != null)
                                fragmentAllDrinks.setDrinks(allDrinks);
                            else fragmentAllDrinks.setDrinks(new ArrayList<>());
                        });
                    }
                    if (allDrinks != null)
                        fragmentAllDrinks.setDrinks(allDrinks);
                    return fragmentAllDrinks;

                default:
                    return null;
            }
        }
        @Override
        public int getCount() {
            return totalTabs;
        }
    }
}