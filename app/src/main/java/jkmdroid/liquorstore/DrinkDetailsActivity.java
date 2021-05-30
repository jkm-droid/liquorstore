package jkmdroid.liquorstore;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class DrinkDetailsActivity extends AppCompatActivity {

    TextView textName, textPrice, textDescription, textCategory;
    Button addToCart, buyNow;
    ImageView drinkImage;
    String name, category, description, posterurl;
    int price, drink_id;
    SqlLiteHelper sqlLiteHelper;
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

        addToCart = findViewById(R.id.add_to_cart);
        buyNow = findViewById(R.id.buy_now);

        setUp();
    }

    private void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.product_details));
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        drink_id = bundle.getInt("drink_id");
        name = bundle.getString("name");
        category = bundle.getString("category");
        description = bundle.getString("description");
        posterurl = bundle.getString("posterurl");
        price = bundle.getInt("price");

        textName.setText(name);
        textPrice.setText("Kshs "+price);
        textDescription.setText(description);
        textCategory.setText(category);

        if (!posterurl.isEmpty()){
            Picasso.get()
                    .load(posterurl)
                    .fit()
                    .into(drinkImage);
        }else{
            drinkImage.setImageResource(R.drawable.broken_image);
        }

        buyNow.setOnClickListener(v -> {
;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            sqlLiteHelper.insert_drink(drink_id, name, price, category, posterurl,  formatter.format(new Date(System.currentTimeMillis())));
            startActivity(new Intent(getApplicationContext(), CartActivity.class));
        });

        addToCart.setOnClickListener(v ->{
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            boolean isAdded = sqlLiteHelper.insert_drink(drink_id, name, price, category, posterurl,  formatter.format(new Date(System.currentTimeMillis())));
            if (isAdded)
                Toast.makeText(getApplicationContext(), "Drink Added to Cart", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Already in Cart", Toast.LENGTH_SHORT).show();
        });
    }

}