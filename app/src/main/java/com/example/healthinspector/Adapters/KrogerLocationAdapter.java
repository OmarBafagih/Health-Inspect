package com.example.healthinspector.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthinspector.Constants;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class KrogerLocationAdapter extends RecyclerView.Adapter<KrogerLocationAdapter.ViewHolder>{

    private ArrayList<JSONObject> locations;
    private Context context;

    public KrogerLocationAdapter(Context context, ArrayList<JSONObject> locations){
        this.context = context;
        this.locations = locations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.location_item, parent, false);
        return new KrogerLocationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject location = locations.get(position);
        try {
            holder.bind(location);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView storeNameTextView, locationAddressTextView;
        private ImageView stockIndicatorImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
            locationAddressTextView = itemView.findViewById(R.id.locationAddressTextView);
            stockIndicatorImageView = itemView.findViewById(R.id.stockIndicatorImageView);
        }

        public void bind(JSONObject location) throws JSONException {
            storeNameTextView.setText(location.getString(Constants.STORE_NAME));
            locationAddressTextView.setText(location.getString(Constants.ADDRESS));
            if(location.getBoolean(Constants.IN_STOCK)){
                stockIndicatorImageView.setImageResource(R.drawable.in_stock_icon);
            }
            else{
                stockIndicatorImageView.setImageResource(R.drawable.not_in_stock_icon);
            }
        }
    }
}


