package jkmdroid.liquorstore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
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

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by jkm-droid on 09/06/2021.
 */

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    RecyclerView recyclerView;
    TextView cartView, errorView;
    SearchView searchView;
    Toolbar toolbar;
    String keyword;
    Intent intent;
    SearchHelper searchHelper;
    SqlLiteHelper sqlLiteHelper;
    String drink_id, name, price, category,posterurl, description;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void start() {
        recyclerView = findViewById(R.id.drinks_recyclerview);
        searchView = findViewById(R.id.search);
        toolbar = findViewById(R.id.toolbar);
        sqlLiteHelper = new SqlLiteHelper(SearchActivity.this);
        searchHelper = new SearchHelper(SearchActivity.this);
        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getString("category");
        errorView = findViewById(R.id.error_search);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(MyHelper.capitalizeCategory(keyword));
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //search
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            back();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void back() {
        intent = new Intent(SearchActivity.this, DrinksActivity.class);
        intent.putExtra("keyword", keyword);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cart, menu);

        final MenuItem menuItemCart= menu.findItem(R.id.cart);
        View actionViewCart= menuItemCart.getActionView();
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
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        recyclerView.setAdapter(new RecyclerViewAdapter(this, filterText(newText)));
        return true;
    }

    private ArrayList<Drink> filterText(String text) {
        Cursor cursor = searchHelper.get_drinks();
        ArrayList<Drink> drinks = new ArrayList<>();
        Drink drink;
        if (cursor != null && cursor.getCount() > 0){
            while (cursor.moveToNext()){
                drink = new Drink();
                drink_id = cursor.getString(1);
                name = cursor.getString(2);
                price = cursor.getString(3);
                category = cursor.getString(4);
                description = cursor.getString(5);
                posterurl = cursor.getString(6);

                if (name.toLowerCase().contains(text.toLowerCase())) {
                    drink.setId(Integer.parseInt(drink_id));
                    drink.setName(name);
                    drink.setPrice(Integer.parseInt(price));
                    drink.setCategory(category);
                    drink.setCategory(description);
                    drink.setPosterurl(posterurl);

                    drinks.add(drink);
                }
            }
            cursor.close();
        }

        return drinks;
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        ArrayList<Drink> drinks;
        Context context;

        RecyclerViewAdapter(Context context1, ArrayList<Drink> drinks1) {
            this.context = context1;
            this.drinks = drinks1;
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView drinkPoster;
            TextView priceView, nameView, addToCart;
            ViewHolder(View v) {
                super(v);
                drinkPoster = v.findViewById(R.id.drink_poster);
                priceView = v.findViewById(R.id.drink_price);
                nameView = v.findViewById(R.id.drink_name);
                addToCart = v.findViewById(R.id.add_to_cart);
            }

        }

        // inflates the cell layout from xml when needed
        @Override
        @NonNull
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.search, parent, false);
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

    }

}