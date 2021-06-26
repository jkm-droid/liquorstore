package jkmdroid.likastore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jkmdroid.likastore.models.Drink;
import jkmdroid.likastore.helpers.MyHelper;

/**
 * Created by jkm-droid on 27/05/2021.
 */

public class FragmentHome extends Fragment{
    private ArrayList<Drink> drinks;
    TextView errorView, loadingView, seeAll;
    ImageView imageError;
    FragmentAllDrinks.OnFragmentRestart onFragmentRestart;
    RecyclerView recyclerView;
    ImageView whiskey, beer, vodka, gin, soft_drinks;
    LinearLayout linearLayout;
    ViewFlipper viewFlipper;
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    TextView searchView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.drinks_recyclerview);
        seeAll = view.findViewById(R.id.see_all);
        whiskey = view.findViewById(R.id.whisky);
        beer = view.findViewById(R.id.beer);
        vodka = view.findViewById(R.id.vodka);
        gin = view.findViewById(R.id.gin);
        soft_drinks = view.findViewById(R.id.soft_drinks);
        searchView = view.findViewById(R.id.search_view);
        searchView.setOnClickListener(v-> startActivity(new Intent(getActivity(), SearchActivity.class)));

        linearLayout = view.findViewById(R.id.layout_content);

//        viewFlipper = view.findViewById(R.id.view_flipper);
        errorView = view.findViewById(R.id.error);
        loadingView = view.findViewById(R.id.loading);
        imageError = view.findViewById(R.id.image_error);

        if (MyHelper.isOnline(getActivity())) {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.setText("Loading drinks....");
        }else {
            imageError.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("There is no internet connection!!");
        }

//        int[] images = {
//                R.drawable.slide0, R.drawable.slide1, R.drawable.slide2,
//                R.drawable.slide3, R.drawable.slide4, R.drawable.slide5,
//                R.drawable.slide6, R.drawable.slide7, R.drawable.slide8,
//                R.drawable.slide9
//        };
//
//        for (int image : images)
//            initFlipper(image);

        moveToDrinksActivity();

        GridLayout gridLayout = new GridLayout(getActivity());
        gridLayout.addView(view);

        if (onFragmentRestart != null)
            onFragmentRestart.onMoviesReceived();

        return gridLayout;
    }

//    private void initFlipper(int image) {
//        ImageView imageView = new ImageView(getActivity());
//        imageView.setBackgroundResource(image);
//        viewFlipper.addView(imageView);
//        viewFlipper.setFlipInterval(3000);
//        viewFlipper.setAutoStart(true);
//
//        viewFlipper.setInAnimation(getActivity(), android.R.anim.slide_in_left);
//        viewFlipper.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
//    }

    private void moveToDrinksActivity() {
        whiskey.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DrinksActivity.class);
            intent.putExtra("keyword", "whiskey");
            startActivity(intent);
        });
        beer.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DrinksActivity.class);
            intent.putExtra("keyword", "beer");
            startActivity(intent);
        });
        vodka.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DrinksActivity.class);
            intent.putExtra("keyword", "vodka");
            startActivity(intent);
        });
        gin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DrinksActivity.class);
            intent.putExtra("keyword", "gin");
            startActivity(intent);
        });
        seeAll.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DrinksActivity.class);
            intent.putExtra("keyword", "all_drinks");
            startActivity(intent);
        });
     soft_drinks.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DrinksActivity.class);
            intent.putExtra("keyword", "soft_drinks");
            startActivity(intent);
        });
    }

    public void setDrinks(ArrayList<Drink> drinks){
        this.drinks = drinks;

        if (getContext() == null)
            return;
        if (drinks.size() > 0){
            loadingView.setVisibility(View.GONE);
            imageError.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(),drinks);
        recyclerView.setAdapter(adapter);

    }

    public void setOnFragmentRestart(FragmentAllDrinks.OnFragmentRestart onFragmentRestart) {
        this.onFragmentRestart = onFragmentRestart;
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
            TextView priceView, nameView, ratingView;
            ViewHolder(View v) {
                super(v);
                drinkPoster = (ImageView)v.findViewById(R.id.drink_poster);
                priceView = (TextView)v.findViewById(R.id.drink_price);
                nameView = (TextView)v.findViewById(R.id.drink_name);
                ratingView = (TextView)v.findViewById(R.id.rating);
            }

        }

        // inflates the cell layout from xml when needed
        @Override
        @NonNull
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.recommended, parent, false);
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
            holder.priceView.setText("Ksh:"+drinks.get(position).getPrice());
            holder.ratingView.setText(MyHelper.generateRating());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra("activity", "main_activity");
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