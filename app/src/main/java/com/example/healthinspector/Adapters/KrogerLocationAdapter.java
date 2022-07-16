package com.example.healthinspector.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.LocationService;
import com.example.healthinspector.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class KrogerLocationAdapter extends RecyclerView.Adapter<KrogerLocationAdapter.ViewHolder>{

    private ArrayList<JSONObject> locations;
    private Context context;
    private static final String MAP_REDIRECT_URL = "http://maps.google.com/maps?saddr=";
    private FragmentSwitch fragmentSwitch;


    public KrogerLocationAdapter(Context context, ArrayList<JSONObject> locations){
        this.context = context;
        this.locations = locations;
    }
    public KrogerLocationAdapter(Context context, ArrayList<JSONObject> locations, FragmentSwitch fragmentSwitch){
        this.context = context;
        this.locations = locations;
        this.fragmentSwitch = fragmentSwitch;
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
        private TextView storeNameTextView, locationAddressTextView, stockIndicatorTextView;
        private ImageView stockIndicatorImageView;
        private RelativeLayout locationContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
            locationAddressTextView = itemView.findViewById(R.id.locationAddressTextView);
            stockIndicatorImageView = itemView.findViewById(R.id.stockIndicatorImageView);
            locationContainer = itemView.findViewById(R.id.locationRelativeLayout);
            stockIndicatorTextView = itemView.findViewById(R.id.productInStockTextView);
        }

        public void bind(JSONObject location) throws JSONException {
            storeNameTextView.setText(location.getString(Constants.STORE_NAME));
            locationAddressTextView.setText(location.getString(Constants.ADDRESS));
            if(location.has(Constants.IN_STOCK)){
                Glide.with(context).load(context.getDrawable(R.drawable.in_stock_icon)).into(stockIndicatorImageView);
            }
            else{
                Glide.with(context).load(context.getDrawable(R.drawable.not_in_stock_icon)).into(stockIndicatorImageView);
            }
            if(fragmentSwitch != null && fragmentSwitch.equals(FragmentSwitch.HOME_FRAGMENT)){
                stockIndicatorImageView.setVisibility(View.GONE);
                stockIndicatorTextView.setVisibility(View.GONE);
            }
            locationContainer.setOnClickListener(view -> {
                String startAddress = (LocationService.getLastLocation().getLatitude()) + "," + (LocationService.getLastLocation().getLongitude());
                String destinationAddress = null;
                try {
                    destinationAddress = location.getDouble(Constants.LATITUDE) + "," + location.getDouble(Constants.LONGITUDE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MAP_REDIRECT_URL + startAddress + "&daddr=" + destinationAddress));
                context.startActivity(intent);
            });
        }
    }
}


