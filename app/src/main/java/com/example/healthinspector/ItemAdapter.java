package com.example.healthinspector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{
    private Context context;
    private List<String> searchItems;
    private SearchFragmentSwitch searchFragmentSwitch;

    public ItemAdapter(Context context, List<String> searchItems, SearchFragmentSwitch searchFragmentSwitch){
        this.context = context;
        this.searchItems = searchItems;
        this.searchFragmentSwitch = searchFragmentSwitch;
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
    public void clear(){
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

        public void bind(String item) {
            //if the user is wanting to search through additives
            if(searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH)){
                searchItemImageView.setImageResource(R.drawable.additives_icon);
            }
            else{
                searchItemImageView.setImageResource(R.drawable.ingredients_icon);
            }
            searchItemTextView.setText(item);
        }
    }

}
