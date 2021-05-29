package jkmdroid.liquorstore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 * Created by jkm-droid on 27/05/2021.
 */

public class FragmentHome extends Fragment {
    private ArrayList<Drink> drinks;
    TextView errorView, loadingView, seeAll;
    ImageView imageError;
    FragmentAllDrinks.OnFragmentRestart onFragmentRestart;
    RecyclerView recyclerView;
    ImageView whiskey, beer, vodka, gin;
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

        errorView = view.findViewById(R.id.error);
        loadingView = view.findViewById(R.id.loading);
        imageError = view.findViewById(R.id.image_error);

        if (MyHelper.isOnline(getActivity())) {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.setText("Loading details....");
        }else {
            imageError.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("There is no internet connection!!");
        }

        GridLayout gridLayout = new GridLayout(getActivity());
        gridLayout.addView(view);

        if (onFragmentRestart != null)
            onFragmentRestart.onMoviesReceived();

        return gridLayout;
    }

    public void setDrinks(ArrayList<Drink> drinks){
        this.drinks = drinks;
        if (drinks == null) {
            imageError.setVisibility(View.VISIBLE);
            errorView.setText("No drinks found");
        }
        if (getContext() == null)
            return;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setAdapter(new RecyclerViewAdapter(getContext(), drinks));

        if (drinks.size() > 0){
            loadingView.setVisibility(View.GONE);
            imageError.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }
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

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), DrinkDetailsActivity.class);
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