package jkmdroid.liquorstore;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

public class DrinkDetailsActivity extends AppCompatActivity {

    TextView textName, textPrice, textDescription, textCategory;
    ImageView drinkImage;
    String name, category, description, posterurl;
    int price;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_details);

        textName = findViewById(R.id.drink_name);
        textPrice = findViewById(R.id.drink_price);
        textCategory = findViewById(R.id.drink_category);
        textDescription = findViewById(R.id.drink_description);
        drinkImage = findViewById(R.id.drink_poster);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.product_details));
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
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
    }

}