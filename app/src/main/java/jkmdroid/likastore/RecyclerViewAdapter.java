package jkmdroid.likastore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jkmdroid.likastore.models.Drink;

/**
 * Created by jkmdroid on 5/29/21.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    ArrayList<Drink> drinks;
    Context context;
    private ItemClickListener listener;


    RecyclerViewAdapter(Context context1, ArrayList<Drink> movies1) {
        this.context = context1;
        this.drinks = movies1;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView drinkPoster;
        TextView priceView, nameView;
        ViewHolder(View v) {
            super(v);
            drinkPoster = (ImageView)v.findViewById(R.id.drink_poster);
            priceView = (TextView)v.findViewById(R.id.drink_price);
            nameView = (TextView)v.findViewById(R.id.drink_name);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onItemClick(v, getAdapterPosition());
        }
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return drinks.size();
    }

    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }
}
