package com.example.healthinspector;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{
    private Context context;
    private List<String> searchItems;
    private SearchFragmentSwitch searchFragmentSwitch;
    private String addedItem;
    private int selected_position = -1;

    public ItemAdapter(Context context, List<String> searchItems, SearchFragmentSwitch searchFragmentSwitch){
        this.context = context;
        this.searchItems = searchItems;
        this.searchFragmentSwitch = searchFragmentSwitch;
        this.addedItem = "";
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
        holder.itemView.setBackground(selected_position == position ? context.getDrawable(R.drawable.item_selected_background) : context.getDrawable(R.drawable.item_background));
    }

    @Override
    public int getItemCount() {
        return searchItems.size();
    }
    public String getAddedItem(){return this.addedItem;}

    public void clear(){
        searchItems.clear();
        notifyDataSetChanged();
    }

    // method for filtering our recyclerview items.
    public void filterList(ArrayList<String> filteredList) {
        this.searchItems = filteredList;
        notifyDataSetChanged();
    }

    public void addAll(List<String> newPosts) {
        searchItems.addAll(newPosts);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView searchItemTextView;
        private ImageView searchItemImageView;
        private RelativeLayout searchItemRelativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            searchItemTextView = itemView.findViewById(R.id.itemTextView);
            searchItemImageView = itemView.findViewById(R.id.itemImageView);
            searchItemRelativeLayout = itemView.findViewById(R.id.searchItemRelativeLayout);
        }

        public void bind(String item) {
            searchItemTextView.setText(item);
            //if the user is wanting to search through additives
            if(searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH) || searchFragmentSwitch.equals(SearchFragmentSwitch.USER_WARNINGS)){
                searchItemImageView.setImageResource(R.drawable.additives_icon);
            }
            else{
                searchItemImageView.setImageResource(R.drawable.ingredients_icon);
            }
            if(searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH) || searchFragmentSwitch.equals(SearchFragmentSwitch.ALLERGEN_SEARCH)){
                searchItemRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                        notifyItemChanged(selected_position);
                        selected_position = getAdapterPosition();
                        notifyItemChanged(selected_position);
                        addedItem = item;
                    }
                });
            }
        }
    }
}