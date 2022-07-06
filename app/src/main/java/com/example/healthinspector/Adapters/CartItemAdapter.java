package com.example.healthinspector.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.R;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder>{
    private Context context;
    private List<RecommendedProduct> recommendedProducts;
    private static final String TAG = "ProductRecommendationsAdapter";


    public CartItemAdapter(Context context, List<RecommendedProduct> recommendedProducts){
        this.context = context;
        this.recommendedProducts = recommendedProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recommended_product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedProduct recommendedProduct = recommendedProducts.get(position);
        holder.bind(recommendedProduct);
    }

    @Override
    public int getItemCount() {
        return recommendedProducts.size();
    }
    
    public void clear() {
        recommendedProducts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<RecommendedProduct> newPosts) {
        recommendedProducts.addAll(newPosts);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImageView;
        TextView cartItemFactsTextView, cartItemNameTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImageView = itemView.findViewById(R.id.cartItemImageView);
            cartItemFactsTextView = itemView.findViewById(R.id.cartItemFactsTextView);
            cartItemNameTextView = itemView.findViewById(R.id.cartItemNameTextView);
        }

        public void bind(RecommendedProduct recommendedProduct) {
            if(!recommendedProduct.getProductImageUrl().trim().isEmpty() || recommendedProduct.getProductImageUrl() != null) {
                Glide.with(context).load(recommendedProduct.getProductImageUrl()).into(cartItemImageView);
            }
            cartItemNameTextView.setText(recommendedProduct.getProductName());
            String cartItemFacts = "";
            for(int i = 0; i < recommendedProduct.getNutrientLevels().size(); i++){
                if(i != recommendedProduct.getNutrientLevels().size()-1){
                    cartItemFacts += recommendedProduct.getNutrientLevels().get(i) + "\n";
                    continue;
                }
                cartItemFacts += recommendedProduct.getNutrientLevels().get(i);
            }
            cartItemFactsTextView.setText(cartItemFacts);
        }
    }

}
