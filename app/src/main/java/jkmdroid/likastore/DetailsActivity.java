package jkmdroid.likastore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import jkmdroid.likastore.helpers.MyHelper;
import jkmdroid.likastore.helpers.SqlLiteHelper;
import jkmdroid.likastore.orders.CartActivity;

/**
 * Created by jkmdroid on 5/28/21.
 */
public class DetailsActivity extends AppCompatActivity {

    TextView textName, textPrice, textDescription, textCategory;
    Button addToCart, buyNow;
    ImageView drinkImage;
    String name, category, description, posterurl;
    int price, drink_id;
    SqlLiteHelper sqlLiteHelper;
    TextView cartView;
    String keyword, activity, activity2;
    Intent intent;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_details);

        sqlLiteHelper = new SqlLiteHelper(getApplicationContext());

        textName = findViewById(R.id.drink_name);
        textPrice = findViewById(R.id.drink_price);
        textCategory = findViewById(R.id.drink_category);
        textDescription = findViewById(R.id.drink_description);
        drinkImage = findViewById(R.id.drink_poster);
        ratingBar = findViewById(R.id.rating_bar);

        addToCart = findViewById(R.id.add_to_cart);
        buyNow = findViewById(R.id.buy_now);

        setUp();
    }

    private void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.product_details));
        setSupportActionBar(toolbar) ;

        startCollapsingToolBar();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getBundle();

        textName.setText(name);
        textPrice.setText("Kshs "+price);
        textDescription.setText(description);
        if (category.contains("_"))
            textCategory.setText(category.replaceAll("_", " "));
        else
            textCategory.setText(category);
        ratingBar.setRating((float)Float.parseFloat(MyHelper.generateRating()));

        if (!posterurl.isEmpty()){
            Picasso.get()
                    .load(posterurl)
                    .fit()
                    .into(drinkImage);
        }else{
            drinkImage.setImageResource(R.drawable.broken_image);
        }
        int quantity = 1;
        buyNow.setOnClickListener(v -> {
            ;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            sqlLiteHelper.insert_drink(drink_id, name, price, category, quantity, posterurl,  formatter.format(new Date(System.currentTimeMillis())));
            goToCartActivity();
        });

        addToCart.setOnClickListener(v ->{
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            boolean isAdded = sqlLiteHelper.insert_drink(drink_id, name, price, category,quantity, posterurl,  formatter.format(new Date(System.currentTimeMillis())));
            if (isAdded) {
                setupBadge();
                Toast.makeText(getApplicationContext(), "Drink Added to Cart", Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(getApplicationContext(), "Already in Cart", Toast.LENGTH_SHORT).show();
        });
    }

    private void startCollapsingToolBar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();

        keyword = bundle.getString("keyword");
        activity = bundle.getString("activity");
        activity2 = bundle.getString("activity2");
        System.out.println(activity+"------------------------------");
        drink_id = bundle.getInt("drink_id");
        name = bundle.getString("name");
        category = bundle.getString("category");
        description = bundle.getString("description");
        posterurl = bundle.getString("posterurl");
        price = bundle.getInt("price");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart, menu);
        final MenuItem menuItem = menu.findItem(R.id.cart);

        View actionView = menuItem.getActionView();
        cartView = (TextView) actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(v -> {
            onOptionsItemSelected(menuItem);
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
            goToCartActivity();
        });
    }

    private void goToCartActivity() {
        Intent intent = new Intent(getApplicationContext(), CartActivity.class);
        intent.putExtra("activity","drinks_details_activity");
        intent.putExtra("activity2","drinks_activity");
        intent.putExtra("keyword",keyword);
        intent.putExtra("drink_id",drink_id);
        intent.putExtra("name",name);
        intent.putExtra("category",category);
        intent.putExtra("description",description);
        intent.putExtra("posterurl",posterurl);
        intent.putExtra("price",price);

        startActivity(intent);
        finish();
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
            intent.putExtra("keyword", keyword);
            startActivity(intent);
            finish();
        }else if (activity.equalsIgnoreCase("main_activity")){
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("activity", "trigger_token");
            startActivity(intent);
            finish();
        }else{
            intent = new Intent(getApplicationContext(), DrinksActivity.class);
            intent.putExtra("keyword", keyword);
            startActivity(intent);
            finish();
        }
    }

}