package jkmdroid.likastore.orders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

import jkmdroid.likastore.DetailsActivity;
import jkmdroid.likastore.helpers.MyHelper;
import jkmdroid.likastore.models.Drink;
import jkmdroid.likastore.DrinksActivity;
import jkmdroid.likastore.MainActivity;
import jkmdroid.likastore.R;
import jkmdroid.likastore.helpers.SqlLiteHelper;

/**
 * Created by jkm-droid on 29/05/2021.
 */
public class CartActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView textView;
    SqlLiteHelper sqlLiteHelper;
    String drink_id, name, price, category,quantity, posterurl;
    TextView emptyMessage;
    ImageView emptyCart;
    LinearLayout linearLayout;
    TextView subTotalView, finalTotalView, packView;
    Button checkoutButton;
    String activity, activity2, keyword, description;
    int prices, drink_ids;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        recyclerView = findViewById(R.id.drinks_recyclerview);
        textView = findViewById(R.id.loading);
        emptyMessage = findViewById(R.id.empty_message);
        emptyCart = findViewById(R.id.empty_cart);
        linearLayout = findViewById(R.id.layout_prices);

        subTotalView = findViewById(R.id.sub_total);
        finalTotalView = findViewById(R.id.final_total);
        packView = findViewById(R.id.package_fee);

        checkoutButton = findViewById(R.id.check_out);
        checkoutButton.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, CheckOutActivity.class));
            finish();
        });

        sqlLiteHelper = new SqlLiteHelper(getApplicationContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));

        if (writeDrinks().isEmpty()) {
            emptyCart.setVisibility(View.VISIBLE);
            emptyMessage.setText("No drinks!");
            emptyMessage.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setAdapter(new RecyclerViewAdapter(CartActivity.this, writeDrinks()));
            linearLayout.setVisibility(View.VISIBLE);
            calculatePrices();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Cart");
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        get_bundle();

    }

    private void get_bundle() {
        Bundle bundle = getIntent().getExtras();
        activity = bundle.getString("activity");
        activity2 = bundle.getString("activity2");
        keyword = bundle.getString("keyword");
        drink_ids = bundle.getInt("drink_id");
        name = bundle.getString("name");
        category = bundle.getString("category");
        description = bundle.getString("description");
        posterurl = bundle.getString("posterurl");
        prices = bundle.getInt("price");
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
        if (activity.equalsIgnoreCase("drinks_activity")) {
            intent = new Intent(getApplicationContext(), DrinksActivity.class);
//            intent.putExtra("activity", activity);
            intent.putExtra("keyword", keyword);
            startActivity(intent);
            finish();
        }else if(activity.equalsIgnoreCase("drinks_details_activity")){
            intent = new Intent(getApplicationContext(), DetailsActivity.class);
            intent.putExtra("activity", activity2);
            intent.putExtra("keyword", keyword);
            intent.putExtra("drink_id",drink_ids);
            intent.putExtra("name",name);
            intent.putExtra("category",category);
            intent.putExtra("description",description);
            intent.putExtra("posterurl",posterurl);
            intent.putExtra("price",prices);
            startActivity(intent);
            finish();
        }else{
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("activity", "trigger_token");
            startActivity(intent);
            finish();
        }

    }

    private void calculatePrices(){
        Cursor cursor = sqlLiteHelper.get_drinks();
        int totalPrice = 0;
        if (cursor != null && cursor.getCount() > 0){

            while (cursor.moveToNext()){
                price = cursor.getString(6);//contains the total prices based on drink quantity
                totalPrice += Integer.parseInt(price);
            }
            cursor.close();
        }
        DecimalFormat format = new DecimalFormat("#,###,###");
        int finalCost = totalPrice + 50 + (10 * cursor.getCount());

        subTotalView.setText("Kshs "+format.format(totalPrice));
        packView.setText("Kshs "+(10 * cursor.getCount()));
        finalTotalView.setText("Kshs "+format.format(finalCost));
    }

    private ArrayList<Drink> writeDrinks() {
        Cursor cursor = sqlLiteHelper.get_drinks();
        ArrayList<Drink> drinks = new ArrayList<>();
        Drink drink;
        if (cursor != null && cursor.getCount() > 0){
            while (cursor.moveToNext()){
                drink = new Drink();
                drink_id = cursor.getString(1);
                name = cursor.getString(2);
                price = cursor.getString(3);
                category = cursor.getString(4);
                quantity = cursor.getString(5);
                posterurl = cursor.getString(7);

                drink.setId(Integer.parseInt(drink_id));
                drink.setName(name);
                drink.setPrice(Integer.parseInt(price));
                drink.setCategory(category);
                drink.setQuantity(Integer.parseInt(quantity));
                drink.setPosterurl(posterurl);

                drinks.add(drink);
            }
            cursor.close();
        }

        return drinks;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
        ArrayList<Drink> drinks;
        Context context;

        RecyclerViewAdapter(Context context1, ArrayList<Drink> movies1) {
            this.context = context1;
            this.drinks = movies1;
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView drinkPoster, cancelDrink;
            TextView priceView, nameView, quantityView, minus, add;
            RatingBar ratingBar;
            ViewHolder(View v) {
                super(v);
                drinkPoster = (ImageView)v.findViewById(R.id.drink_poster);
                cancelDrink = (ImageView)v.findViewById(R.id.cancel_drink);
                priceView = (TextView)v.findViewById(R.id.drink_price);
                nameView = (TextView)v.findViewById(R.id.drink_name);
                quantityView = (TextView)v.findViewById(R.id.quantity);
                minus = (TextView)v.findViewById(R.id.minus);
                add = (TextView)v.findViewById(R.id.add);
                ratingBar = (RatingBar)v.findViewById(R.id.rating_bar);
            }

        }

        // inflates the cell layout from xml when needed
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.cart_drink, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the xml
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
            holder.quantityView.setText(""+drinks.get(position).getQuantity()+"Qty");
            holder.ratingBar.setRating((float)Float.parseFloat(MyHelper.generateRating()));
            holder.cancelDrink.setOnClickListener(v -> {
               boolean isRemoved =  sqlLiteHelper.delete_drink(drinks.get(position).getId());
               if (isRemoved) {
                   Toast.makeText(getApplicationContext(), "Drink Removed", Toast.LENGTH_SHORT).show();
                   notifyItemRemoved(position);
                   drinks.remove(position);
                   notifyDataSetChanged();
                   calculatePrices();
                   if(sqlLiteHelper.count_drinks() <= 0){
                       emptyCart.setVisibility(View.VISIBLE);
                       emptyMessage.setText("No drinks!");
                       emptyMessage.setVisibility(View.VISIBLE);
                       linearLayout.setVisibility(View.GONE);
                   }
               }
            });
            //deduct quantity
            holder.minus.setOnClickListener(v ->{
                String keyword = "minus";
                sqlLiteHelper.change_quantity(drinks.get(position).getId(), keyword);
                int new_quantity = sqlLiteHelper.get_drink_quantity(drinks.get(position).getId());
                holder.quantityView.setText(""+new_quantity+"Qty");
                calculatePrices();
            });

            //add quantity
            holder.add.setOnClickListener(v ->{
                String keyword = "add";
                sqlLiteHelper.change_quantity(drinks.get(position).getId(), keyword);
                int new_quantity = sqlLiteHelper.get_drink_quantity(drinks.get(position).getId());
                holder.quantityView.setText(""+new_quantity+"Qty");
                calculatePrices();
            });
        }

        // total number of cells
        @Override
        public int getItemCount() {
            return drinks.size();
        }

    }

}