package jkmdroid.liquorstore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Created by jkm-droid on 27/05/2021.
 */

public class FragmentAllDrinks extends Fragment{
    private ListView listView;
    private OnFragmentRestart onFragmentRestart;
    private ArrayList<Drink> drinks;
    TextView errorView, loadingView;
    ImageView imageError, cartView;
    SqlLiteHelper sqlLiteHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_drinks, container,false);
        listView = view.findViewById(R.id.drinks_listview);

        errorView = view.findViewById(R.id.error);
        loadingView = view.findViewById(R.id.loading);
        imageError = view.findViewById(R.id.image_error);
        sqlLiteHelper = new SqlLiteHelper(getContext());

        if (MyHelper.isOnline(getActivity())) {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.setText("Loading Drinks....");
        }else {
            imageError.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("There is no internet connection!!");
        }

        FrameLayout layout = new FrameLayout(getActivity());
        layout.addView(view);

        if (onFragmentRestart != null)
            onFragmentRestart.onMoviesReceived();
        return layout;
    }

    public void setOnFragmentRestart(OnFragmentRestart onFragmentRestart){
        this.onFragmentRestart = onFragmentRestart;
    }

    public void setDrinks(ArrayList<Drink> drinks){
        this.drinks = drinks;
        if (drinks == null) {
            imageError.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("No drinks found");
        }
        if (getContext() == null)
            return;
        listView.setAdapter(new Adapter(getContext(), drinks));
        if (drinks.size() > 0){
            imageError.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }
    }
    interface  OnFragmentRestart{
        void onMoviesReceived();
    }
    public ArrayList<Drink> getDrinks() {
        return drinks;
    }

    class Adapter extends ArrayAdapter {
        public Adapter(@NonNull Context context, @NonNull List objects){
            super(context, R.layout.drink, objects);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            View view;
            if (convertView == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.drink, parent,false);
            else view = convertView;


            ((TextView)view.findViewById(R.id.drink_name)).setText(drinks.get(position).getName());
            ((TextView)view.findViewById(R.id.drink_price)).setText("Kshs "+drinks.get(position).getPrice());
            ImageView imageView = (ImageView)view.findViewById(R.id.drink_poster);

            if (!drinks.get(position).getPosterurl().isEmpty()) {
                Picasso.get()
                        .load(drinks.get(position).getPosterurl())
                        .resize(150, 130)
                        .into(imageView);
            }else{
                imageView.setImageResource(R.drawable.broken_image);
            }

            cartView = view.findViewById(R.id.cart);
            cartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = drinks.get(position).getName(), category = drinks.get(position).getCategory(), posterurl = drinks.get(position).getPosterurl();
                    int drink_id = drinks.get(position).getId(), price = drinks.get(position).getPrice();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    boolean isAdded = sqlLiteHelper.insert_drink(drink_id, name, price, category, posterurl,  formatter.format(new Date(System.currentTimeMillis())));
                    if (isAdded)
                        Toast.makeText(getActivity(), "Drink Added to Cart", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Already in Cart", Toast.LENGTH_SHORT).show();
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), DrinkDetailsActivity.class);
                    intent.putExtra("drink_id", drinks.get(position).getId());
                    intent.putExtra("name", drinks.get(position).getName());
                    intent.putExtra("price", drinks.get(position).getPrice());
                    intent.putExtra("category", drinks.get(position).getCategory());
                    intent.putExtra("description", drinks.get(position).getDescription());
                    intent.putExtra("posterurl", drinks.get(position).getPosterurl());

                    startActivity(intent);
                }
            });

            return view;
        }
    }
}