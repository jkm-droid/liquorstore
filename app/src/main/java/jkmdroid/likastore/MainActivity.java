package jkmdroid.likastore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jkmdroid.likastore.helpers.SearchHelper;
import jkmdroid.likastore.models.Drink;
import jkmdroid.likastore.helpers.FlipperHelper;
import jkmdroid.likastore.helpers.SqlLiteHelper;
import jkmdroid.likastore.orders.CartActivity;
import jkmdroid.likastore.orders.OrderActivity;

/**
 * Created by jkm-droid on 27/05/2021.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddDrinkToCart {
    TabLayout tabLayout;
    ViewPager viewPager;
    int selectedTab = 0;
    DrawerLayout drawer;
    boolean stopThread = false;
    FragmentHome fragmentHome;
    FragmentAllDrinks fragmentAllDrinks;
    private ArrayList<Drink> homeDetails, allDrinks;
    boolean requestSuccessful = false;
    TextView cartView;
    SqlLiteHelper sqlLiteHelper;
    FlipperHelper flipperHelper;
    SearchHelper searchHelper;
    Intent intent;
    MenuItem menuItemCart;
    ViewFlipper viewFlipper;
    ArrayList<String> arrayListPoster;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqlLiteHelper = new SqlLiteHelper(getApplicationContext());
        flipperHelper = new FlipperHelper(getApplicationContext());
        searchHelper = new SearchHelper(getApplicationContext());
        fragmentAllDrinks = new FragmentAllDrinks(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.blue_for_buttons));
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout ctl = findViewById(R.id.collapsing_toolbar);
        ctl.setCollapsedTitleTextColor(getResources().getColor(R.color.colorIcons));
        ctl.setExpandedTitleColor(getResources().getColor(R.color.colorIcons));

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        viewFlipper = findViewById(R.id.view_flipper);

        start_drinks();
    }

    void start_drinks() {

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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.getTabAt(selectedTab).select();

//        flipperHelper.delete_posters();
        getDrinks();

        for (String poster : loadPosters()) {
            initFlipper(poster);
        }
    }

    private void initFlipper(String poster) {
        ImageView imageView = new ImageView(getApplicationContext());
        Picasso.get()
                .load(poster)
                .fit()
                .into(imageView);
        imageView.setImageAlpha(200);
        System.out.println(poster);
        viewFlipper.addView(imageView);
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);

        viewFlipper.setInAnimation(getApplicationContext(), android.R.anim.slide_in_left);
        viewFlipper.setOutAnimation(getApplicationContext(), android.R.anim.slide_out_right);
    }
    private ArrayList<String> loadPosters(){
        Cursor cursor = flipperHelper.get_posters();
        arrayListPoster = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0){
            while (cursor.moveToNext()) {
                String poster = cursor.getString(3);
                arrayListPoster.add(poster);
            }
            cursor.close();
        }
        return arrayListPoster;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you really want to exit?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("OK", (dialogInterface, i) -> finish()).create().show();
        }
    }

    String string = "https://play.google.com/store/apps/details?id=jkmdroid.likastore";

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        switch (menuItem.getItemId()) {
            case R.id.order_status:
                startActivity(new Intent(MainActivity.this, OrderActivity.class));
                break;
            case R.id.cart:
                intent = new Intent(MainActivity.this, CartActivity.class);
                intent.putExtra("activity", "main_activity");
                startActivity(intent);
                finish();
                break;
            case R.id.search:
                intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.privacy:
                Intent privacy = new Intent(Intent.ACTION_VIEW);
                privacy.setData(Uri.parse("https://liquorstore.mblog.co.ke/info/privacy.html"));
                startActivity(privacy);
                break;
            case R.id.terms:
                Intent terms = new Intent(Intent.ACTION_VIEW);
                terms.setData(Uri.parse("https://liquorstore.mblog.co.ke/info/terms.html"));
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
                    String url = "https://api.whatsapp.com/send?phone=" +
                            URLEncoder.encode("+254799518610", "UTF-8") + "&text=" + URLEncoder.encode("Hello Lika Store", "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.email:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"stowlika@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "");
                email.putExtra(Intent.EXTRA_TEXT, "Hello Lika Store");

                //need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));
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
        menuItemCart = menu.findItem(R.id.cart);

        View actionView = menuItemCart.getActionView();
        cartView = actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(v -> {
            onOptionsItemSelected(menuItemCart);
            intent = new Intent(getApplicationContext(), CartActivity.class);
            intent.putExtra("activity", "main_activity");
            startActivity(intent);
            finish();
        });

        return true;
    }

    private void setupBadge() {
        cartView.setText(""+sqlLiteHelper.count_drinks());
        cartView.setOnClickListener(v -> {
            intent = new Intent(getApplicationContext(), CartActivity.class);
            intent.putExtra("activity", "main_activity");
            startActivity(intent);
            finish();
        });
    }

    private ArrayList<Drink> extractDrinks(JSONObject response, String word) {
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
                drink.setPosterurl(object.getString("poster_url"));
                drink.setCategory(object.getString("drink_category"));

                String name = object.getString("drink_name"), category = object.getString("drink_category"), url = object.getString("poster_url");
                String desc = object.getString("drink_description");
                int id = object.getInt("id"), price = object.getInt("drink_price");

                if (word.equalsIgnoreCase("all"))
                    searchHelper.insert_drink(id, name, price, category, desc,url);
                drinks.add(drink);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return drinks;
    }

    private void getDrinks() {
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

                PostJson postJson = new PostJson(MainActivity.this, url), postJson1, postJson2;
                postJson.setOnSuccessListener(response -> {
                    requestSuccessful = true;
                    homeDetails = extractDrinks(response, "home");

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
                        allDrinks = extractDrinks(response, "all");

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

                data = "";
                try {
                    data += URLEncoder.encode("get_images", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                url = "https://liquorstore.mblog.co.ke/flipper/get_flipper.php?" + data;
                postJson2 = new PostJson(MainActivity.this, url);
                postJson2.setOnSuccessListener(object -> {
                    requestSuccessful = true;
                    JSONObject jsonObject;
                    JSONArray jsonArray = object.getJSONArray("flippers");
                    for (int i = 0; i < jsonArray.length(); i++){
                        jsonObject = jsonArray.getJSONObject(i);
                        int id = jsonObject.getInt("id");
                        String name = jsonObject.getString("poster_name");
                        String posterUrl = jsonObject.getString("poster_url");
                        flipperHelper.insert_poster(id, name, posterUrl);
                    }
                });
                postJson2.get();
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

    @Override
    public void callBack() {
        setupBadge();
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