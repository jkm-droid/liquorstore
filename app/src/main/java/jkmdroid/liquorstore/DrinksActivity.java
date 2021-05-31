package jkmdroid.liquorstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_drinks);

        sqlLiteHelper = new SqlLiteHelper(getApplicationContext());
        recyclerView = findViewById(R.id.drinks_recyclerview);

        errorView = findViewById(R.id.error);
        loadingView = findViewById(R.id.loading);
        imageError = findViewById(R.id.image_error);

        if (MyHelper.isOnline(getApplicationContext())) {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.setText("Loading details....");
        }else {
            imageError.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("There is no internet connection!!");
        }
        Bundle extras = getIntent().getExtras();
        String keyword = extras.getString("keyword");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (keyword.contains("_"))
            toolbar.setTitle(keyword.replace("_", " ").toUpperCase());
        else
            toolbar.setTitle(keyword.toUpperCase());
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getDrinks(keyword);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart, menu);
        final MenuItem menuItem = menu.findItem(R.id.cart);

        View actionView = menuItem.getActionView();
        cartView = actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        return true;
    }

    private void setupBadge() {
        if (cartView != null) {
            cartView.setText(""+sqlLiteHelper.count_drinks());
            cartView.setOnClickListener(v -> {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
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
        recyclerView.setLayoutManager(new GridLayoutManager(DrinksActivity.this, 3));
        recyclerView.setAdapter(new RecyclerViewAdapter(DrinksActivity.this, drinks));

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
            TextView priceView, nameView;
            ViewHolder(View v) {
                super(v);
                drinkPoster = (ImageView)v.findViewById(R.id.drink_poster);
                priceView = (TextView)v.findViewById(R.id.drink_price);
                nameView = (TextView)v.findViewById(R.id.drink_name);
            }

        }

        // inflates the cell layout from xml when needed
        @Override
        @NonNull
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.recommended, parent, false);
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
            holder.priceView.setText("Kshs:"+drinks.get(position).getPrice());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), DrinkDetailsActivity.class);
                    intent.putExtra("drink_id", drinks.get(position).getId());
                    intent.putExtra("name", drinks.get(position).getName());
                    intent.putExtra("price", drinks.get(position).getPrice());
                    intent.putExtra("category", drinks.get(position).getCategory());
                    intent.putExtra("description", drinks.get(position).getDescription());
                    intent.putExtra("posterurl", drinks.get(position).getPosterurl());

                    startActivity(intent);
                }
            });
        }

        // total number of cells
        @Override
        public int getItemCount() {
            return drinks.size();
        }

    }
}