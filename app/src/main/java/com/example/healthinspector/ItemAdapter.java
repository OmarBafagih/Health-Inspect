package com.example.healthinspector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{
    private Context context;
    private List<String> searchItems;
    private SearchFragmentSwitch searchFragmentSwitch;
    private ArrayList<String> addedItems;

    public ItemAdapter(){
        this.context = null;
        this.searchItems = null;
        this.searchFragmentSwitch = null;
        this.addedItems = new ArrayList<>();
    }

    public ItemAdapter(Context context, List<String> searchItems, SearchFragmentSwitch searchFragmentSwitch){
        this.context = context;
        this.searchItems = searchItems;
        this.searchFragmentSwitch = searchFragmentSwitch;
        this.addedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = searchItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return searchItems.size();
    }
    public ArrayList<String> getAddedItems(){return this.addedItems;}

    public void clear(){
        addedItems.clear();
        searchItems.clear();
        notifyDataSetChanged();
    }
    public void addAll(List<String> newPosts) {
        searchItems.addAll(newPosts);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView searchItemTextView;
        private ImageView searchItemImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            searchItemTextView = itemView.findViewById(R.id.itemTextView);
            searchItemImageView = itemView.findViewById(R.id.itemImageView);
        }
        //nice
        public void bind(String item) {
            searchItemTextView.setText(item);
            //if the user is wanting to search through additives
            if(searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH) || searchFragmentSwitch.equals(SearchFragmentSwitch.USER_WARNINGS)){
                searchItemImageView.setImageResource(R.drawable.additives_icon);
            }
            else{
                searchItemImageView.setImageResource(R.drawable.ingredients_icon);
            }
            if(searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH) || searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH)){
                searchItemTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addedItems.add(item);
                    }
                });
            }

        }
    }

}