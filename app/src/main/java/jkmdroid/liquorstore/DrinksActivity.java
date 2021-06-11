package jkmdroid.liquorstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jkm-droid on 05/27/2021.
 */

public class DrinksActivity extends AppCompatActivity {
    private ArrayList<Drink> drinks;
    TextView errorView, loadingView;
    ImageView imageError;
    boolean requestSuccessful = false;
    boolean stopThread = false;
    RecyclerView recyclerView;
    private ArrayList<Drink>  categoryDrinks;
    TextView cartView;
    SqlLiteHelper sqlLiteHelper;
    SearchHelper searchHelper;
    String keyword;
    Intent intent;
    RecyclerViewAdapter adapter;
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    ImageView imagePoster;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_drinks);

        invalidateOptionsMenu();

        sqlLiteHelper = new SqlLiteHelper(getApplicationContext());
        searchHelper = new SearchHelper(DrinksActivity.this);
        recyclerView = findViewById(R.id.drinks_recyclerview);
        appBarLayout = findViewById(R.id.app_bar);

        errorView = findViewById(R.id.error);
        loadingView = findViewById(R.id.loading);
        imageError = findViewById(R.id.image_error);
        imagePoster = findViewById(R.id.drink_poster);

        if (MyHelper.isOnline(getApplicationContext())) {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.setText("Loading drinks....");
        }else {
            imageError.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("There is no internet connection!!");
        }
        Bundle extras = getIntent().getExtras();
        keyword = extras.getString("keyword");
        toolbar = findViewById(R.id.toolbar);

        if (keyword.contains("_"))
            toolbar.setTitle(MyHelper.capitalizeCategory(keyword).replace("_", " "));
        else
            toolbar.setTitle(MyHelper.capitalizeCategory(keyword));

        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setImagePoster(keyword);

        getDrinks(keyword);
    }

    private void setImagePoster(String keyword) {
        if (keyword.equalsIgnoreCase("gin")){
            Picasso.get()
                    .load("https://liquorstore.mblog.co.ke/images/gin.jpg")
                    .fit()
                    .into(imagePoster);
        }else if (keyword.equalsIgnoreCase("beer")){
            Picasso.get()
                    .load("https://liquorstore.mblog.co.ke/images/beer.jpg")
                    .fit()
                    .into(imagePoster);
        }else if (keyword.equalsIgnoreCase("vodka")){
            Picasso.get()
                    .load("https://liquorstore.mblog.co.ke/images/vodka1.jpg")
                    .fit()
                    .into(imagePoster);
        }else if (keyword.equalsIgnoreCase("whiskey")){
            Picasso.get()
                    .load("https://liquorstore.mblog.co.ke/images/whiskey.jpeg")
                    .fit()
                    .into(imagePoster);
        }else {
            Picasso.get()
                    .load("https://liquorstore.mblog.co.ke/images/slide2.jpg")
                    .fit()
                    .into(imagePoster);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        View viewCart, viewSearch;
        viewSearch = findViewById(R.id.search);
        viewCart = findViewById(R.id.cart);

        if (viewSearch instanceof TextView)
            ((TextView)viewSearch).setTextColor(Color.WHITE);
        if (viewCart instanceof TextView)
            ((TextView)viewCart).setTextColor(Color.WHITE);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        inflater.inflate(R.menu.cart, menu);

        final MenuItem menuItemCart, menuItemSearch;
        View actionViewCart;

        menuItemSearch = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItemSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Drink> newDrinks = new ArrayList<>();
                for (Drink drink : categoryDrinks){
                    if (drink.getName().toLowerCase().contains(newText.toLowerCase())){
                        newDrinks.add(drink);
                    }
                }
                adapter.setFilter(newDrinks);
                return true;
            }
        });


        menuItemCart = menu.findItem(R.id.cart);
        actionViewCart = menuItemCart.getActionView();
        cartView = actionViewCart.findViewById(R.id.cart_badge);

        setupBadge();

        actionViewCart.setOnClickListener(v -> {
            onOptionsItemSelected(menuItemCart);
            intent = new Intent(getApplicationContext(), CartActivity.class);
            intent.putExtra("activity", "main_activity");
            startActivity(intent);
            finish();
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setupBadge() {
        if (cartView != null) {
            cartView.setText(""+sqlLiteHelper.count_drinks());
            cartView.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                intent.putExtra("activity","drinks_activity");
                intent.putExtra("keyword",keyword);
                startActivity(intent);
                finish();
            });
        }

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
                int id = object.getInt("id"), price = object.getInt("drink_price");
                String name = object.getString("drink_name"), category = object.getString("drink_category"), posterurl = object.getString("posterurl");
                String description = object.getString("drink_description");

                searchHelper.insert_drink(id, name, price, category, description, posterurl);

                drinks.add(drink);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return drinks;
    }

    private void getDrinks(String keyword) {
        @SuppressLint("HandlerLeak") Handler handler = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                String data = "";
                try {
                    data += URLEncoder.encode("get_drinks", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8") + "&";
                    data += URLEncoder.encode("keyword", "UTF-8") + "=" + URLEncoder.encode(keyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String url = "https://liquorstore.mblog.co.ke/drinks/get_drinks.php?" + data;

                PostJson postJson = new PostJson(DrinksActivity.this, url);
                postJson.setOnSuccessListener(response -> {
                    requestSuccessful = true;
                    categoryDrinks = extractDrinks(response);

                    if(categoryDrinks != null){
                        setDrinks(categoryDrinks);
                    }

                });
                postJson.get();
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
    public void setDrinks(ArrayList<Drink> drinks){
        this.drinks = drinks;
        if (drinks == null) {
            imageError.setVisibility(View.VISIBLE);
            errorView.setText("No drinks found");
        }
        if (getApplicationContext() == null)
            return;
        appBarLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new GridLayoutManager(DrinksActivity.this, 3));
        adapter = new RecyclerViewAdapter(DrinksActivity.this, drinks);
        recyclerView.setAdapter(adapter);

        if (drinks.size() > 0){
            loadingView.setVisibility(View.GONE);
            imageError.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        ArrayList<Drink> drinks;
        Context context;

        RecyclerViewAdapter(Context context1, ArrayList<Drink> movies1) {
            this.context = context1;
            this.drinks = movies1;
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView drinkPoster;
            TextView priceView, nameView, addToCart;
            TextView ratingView;
            ViewHolder(View v) {
                super(v);
                drinkPoster = v.findViewById(R.id.drink_poster);
                priceView = v.findViewById(R.id.drink_price);
                nameView = v.findViewById(R.id.drink_name);
                addToCart = v.findViewById(R.id.add_to_cart);
                ratingView = v.findViewById(R.id.rating);
            }

        }

        // inflates the cell layout from xml when needed
        @Override
        @NonNull
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.all_drinks, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
        }

        // binds the data to the xml
        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            if (!drinks.get(position).getPosterurl().equals("")) {
                Picasso.get()
                        .load(drinks.get(position).getPosterurl())
                        .fit()
                        .into(holder.drinkPoster);
            }else{
                holder.drinkPoster.setImageResource(R.drawable.broken_image);
            }

            holder.nameView.setText(drinks.get(position).getName());
            holder.priceView.setText("Ksh:"+drinks.get(position).getPrice());
            holder.ratingView.setText(MyHelper.generateRating());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                    intent.putExtra("keyword", keyword);
                    intent.putExtra("activity", "drinks_activity");
                    intent.putExtra("drink_id", drinks.get(position).getId());
                    intent.putExtra("name", drinks.get(position).getName());
                    intent.putExtra("price", drinks.get(position).getPrice());
                    intent.putExtra("category", drinks.get(position).getCategory());
                    intent.putExtra("description", drinks.get(position).getDescription());
                    intent.putExtra("posterurl", drinks.get(position).getPosterurl());

                    startActivity(intent);
                    finish();
                }
            });

            holder.addToCart.setOnClickListener(v->{
                String name = drinks.get(position).getName(), category = drinks.get(position).getCategory(), posterurl = drinks.get(position).getPosterurl();
                int drink_id = drinks.get(position).getId(), price = drinks.get(position).getPrice();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                int quantity = 1;
                boolean isAdded = sqlLiteHelper.insert_drink(drink_id, name, price, category,quantity, posterurl,  formatter.format(new Date(System.currentTimeMillis())));
                if (isAdded) {
                    setupBadge();
                    Toast.makeText(getApplicationContext(), "Drink Added to Cart", Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getApplicationContext(), "Already in Cart", Toast.LENGTH_SHORT).show();
            });
        }

        // total number of cells
        @Override
        public int getItemCount() {
            return drinks.size();
        }

        public void setFilter(ArrayList<Drink> newDrinks) {
            drinks = new ArrayList<>();
            drinks.addAll(newDrinks);
            notifyDataSetChanged();
        }

    }
}