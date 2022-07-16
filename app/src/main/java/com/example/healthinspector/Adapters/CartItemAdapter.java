package com.example.healthinspector.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Cart;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Fragments.ProductFinderFragment;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder>{
    private Context context;
    private List<RecommendedProduct> recommendedProducts;
    private FragmentSwitch fragmentSwitch;
    private ScannedProduct scannedProduct;
    private static final String TAG = "ProductRecommendationsAdapter";
    private static final float CARD_ELEVATION = 50;

    public CartItemAdapter(Context context, List<RecommendedProduct> recommendedProducts, FragmentSwitch fragmentSwitch){
        this.context = context;
        this.recommendedProducts = recommendedProducts;
        this.fragmentSwitch = fragmentSwitch;
    }

    public CartItemAdapter(Context context, List<RecommendedProduct> recommendedProducts, ScannedProduct scannedProduct, FragmentSwitch fragmentSwitch){
        this.context = context;
        this.recommendedProducts = recommendedProducts;
        this.fragmentSwitch = fragmentSwitch;
        this.scannedProduct = scannedProduct;
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

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView cartItemImageView, addToCartImageView;
        TextView cartItemFactsTextView, cartItemNameTextView;
        CardView cartItemContainer;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImageView = itemView.findViewById(R.id.cartItemImageView);
            cartItemFactsTextView = itemView.findViewById(R.id.cartItemFactsTextView);
            cartItemNameTextView = itemView.findViewById(R.id.cartItemNameTextView);
            addToCartImageView = itemView.findViewById(R.id.addtoCartImageView);
            cartItemContainer = itemView.findViewById(R.id.cartItemCardView);
        }

        public void bind(RecommendedProduct recommendedProduct) {
            Glide.with(context).load(recommendedProduct.getProductImageUrl()).placeholder(R.drawable.ingredients_icon).into(cartItemImageView);
            cartItemNameTextView.setText(recommendedProduct.getProductName());
            String cartItemFacts = String.join("\n", recommendedProduct.getNutrientLevels());
            cartItemFactsTextView.setText(cartItemFacts);
            if(fragmentSwitch.equals(FragmentSwitch.RECOMMENDATIONS) || fragmentSwitch.equals(FragmentSwitch.HOME_FRAGMENT)){
                addToCartImageView.setImageResource(R.drawable.add_icon_2);
                if(fragmentSwitch.equals(FragmentSwitch.HOME_FRAGMENT)){
                    cartItemContainer.setCardElevation(CARD_ELEVATION);
                }
            }
            else{
                addToCartImageView.setImageResource(R.drawable.delete_icon);
            }
            addToCartImageView.setOnClickListener(v -> addOrRemoveFromCart(recommendedProduct));
            cartItemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //send to product finder fragment with product
                    FragmentManager fragmentManager = ((FragmentActivity) v.getContext()).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    ProductFinderFragment productFinderFragment = new ProductFinderFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.RECOMMENDED_PRODUCT, Parcels.wrap(recommendedProduct));
                    bundle.putParcelable(Constants.SCANNED_PRODUCT, Parcels.wrap(scannedProduct));
                    productFinderFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.fragment_container, productFinderFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        public void addOrRemoveFromCart(RecommendedProduct recommendedProduct){
            //creating new json object to save to cart JsonArray
            JSONObject newCartItem = new JSONObject();
            try {
                newCartItem.put(Constants.KEYWORDS, recommendedProduct.getKeyWords());
                newCartItem.put(Constants.BRAND, recommendedProduct.getBrand());
                newCartItem.put(Constants.PRODUCT_IMAGE_URL, recommendedProduct.getProductImageUrl());
                newCartItem.put(Constants.PRODUCT_NAME, recommendedProduct.getProductName());
                newCartItem.put(Constants.NUTRIENT_LEVELS, recommendedProduct.getNutrientLevels());

            } catch (JSONException err) {
                Toast.makeText(context, context.getString(R.string.error_saving_cart), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error creating user's cart " + err);
            }
            //if the recommended product is not in the cart
            if(fragmentSwitch.equals(FragmentSwitch.RECOMMENDATIONS) || fragmentSwitch.equals(FragmentSwitch.HOME_FRAGMENT)){
                //if the user's cart has already been initialized
                if(!ParseUser.getCurrentUser().getParseObject(Constants.CART).equals(null)){
                    ParseQuery<Cart> parseQuery = ParseQuery.getQuery(Cart.class);
                    try {
                        Cart userCart = parseQuery.get(ParseUser.getCurrentUser().getParseObject(Constants.CART).getObjectId());
                        userCart.setCartItems(userCart.getCartItems().put(newCartItem));
                        userCart.saveInBackground(e -> Toast.makeText(context, context.getString(R.string.saved_item), Toast.LENGTH_SHORT).show());
                    } catch (ParseException e) {
                        Toast.makeText(context, context.getString(R.string.error_saving_cart), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error saving user's cart " + e);
                    }
                }
                else{
                    Cart newCart = new Cart();
                    JSONArray newCartItems = new JSONArray();
                    newCartItems.put(newCartItem);
                    newCart.setCartItems(newCartItems);
                    newCart.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Toast.makeText(context, context.getString(R.string.saved_item), Toast.LENGTH_SHORT).show();
                                ParseUser.getCurrentUser().put(Constants.CART, newCart);
                                ParseUser.getCurrentUser().saveInBackground();
                            }
                        }
                    });
                }
            }
            //user wants the item to be removed from cart
            else{
                ParseQuery<Cart> parseQuery = ParseQuery.getQuery(Cart.class);
                try {
                    Cart userCart = parseQuery.get(ParseUser.getCurrentUser().getParseObject(Constants.CART).getObjectId());
                    for(int i = 0; i < userCart.getCartItems().length(); i++){
                        if(userCart.getCartItems().getJSONObject(i).getString(Constants.PRODUCT_NAME).equals(recommendedProduct.getProductName())){
                            JSONArray updatedCart = userCart.getCartItems();
                            updatedCart.remove(i);
                            userCart.setCartItems(updatedCart);
                        }
                    }
                    Toast.makeText(context, context.getString(R.string.removed_item), Toast.LENGTH_SHORT).show();
                    userCart.saveInBackground();
                } catch (ParseException | JSONException e) {
                    Toast.makeText(context, context.getString(R.string.error_removing_item), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error removing item from user's cart " + e);
                }
                recommendedProducts.remove(recommendedProduct);
                notifyDataSetChanged();
            }
        }
    }
}
