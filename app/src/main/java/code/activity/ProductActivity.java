package code.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityProductBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class ProductActivity extends BaseActivity implements View.OnClickListener {

    ActivityProductBinding b;

    String type = "1";//InStock, 2=OutStock, 2=BestSeller

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeading.setText(getString(R.string.products));

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        b.tvInStock.setOnClickListener(this);
        b.tvOutStock.setOnClickListener(this);
        b.tvBestSeller.setOnClickListener(this);

        hitGetProductListApi();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvInStock:

                setDefault();
                b.tvInStock.setBackgroundResource(R.drawable.rectangular_border_color);
                b.tvInStock.setTextColor(getResources().getColor(R.color.colorPrimary));
                type = "1";
                hitGetProductListApi();

                break;

            case R.id.tvOutStock:

                setDefault();
                b.tvOutStock.setBackgroundResource(R.drawable.rectangular_border_color);
                b.tvOutStock.setTextColor(getResources().getColor(R.color.colorPrimary));
                type = "2";
                hitGetProductListApi();

                break;

            case R.id.tvBestSeller:

                setDefault();
                b.tvBestSeller.setBackgroundResource(R.drawable.rectangular_border_color);
                b.tvBestSeller.setTextColor(getResources().getColor(R.color.colorPrimary));
                type = "3";
                hitGetProductListApi();

                break;

        }

    }

    private void hitGetProductListApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("type", type);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.productList, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseProductList(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseProductList(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", jsonObject1.getString("productId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("description", jsonObject1.getString("description"));
                    hashMap.put("rating", jsonObject1.getString("rating"));
                    hashMap.put("recommended", jsonObject1.getString("recommended"));
                    hashMap.put("price", jsonObject1.getString("price"));
                    hashMap.put("image",jsonObject1.getString("productImageOne"));
                    hashMap.put("type", jsonObject1.getString("type"));

                    arrayList.add(hashMap);

                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_product_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvProductName.setText(data.get(position).get("name"));
            holder.tvPrice.setText(AppConstants.currency+" "+data.get(position).get("price"));
            holder.tvDescription.setText(data.get(position).get("description"));

            AppUtils.loadPicassoImage(data.get(position).get("image"), holder.ivImage);

            holder.ratingBar.setRating(AppUtils.returnFloat(data.get(position).get("rating")));

            if (data.get(position).get("recommended").equals("1")){

                holder.ivRecommended.setImageResource(R.drawable.ic_thumbs_up_red);
            }
            else
                holder.ivRecommended.setImageResource(R.drawable.ic_thumbs_up);



            if (data.get(position).get("type").equals("1")){

                holder.tvStock.setText(getString(R.string.inStock));
                holder.ivSwitchStock.setImageResource(R.drawable.ic_switch_on);
            }
            else
            {
                holder.tvStock.setText(getString(R.string.itemAUnavailable));
                holder.ivSwitchStock.setImageResource(R.drawable.ic_switch_off);
            }

            holder.ivRecommended.setOnClickListener(view -> hitMarkRecommendApi(data.get(position).get("recommended").equals("1")?"2":"1", data.get(position).get("productId")));
            holder.ivSwitchStock.setOnClickListener(view -> hitChangeStockStatusApi(data.get(position).get("type").equals("1")?"2":"1", data.get(position).get("productId")));


            holder.ivMore.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(mActivity, holder.ivMore);

                popup.getMenuInflater()
                        .inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {

                    if (item.getItemId() == R.id.edit) {
                        startActivity(new Intent(mActivity, AddProductActivity.class)
                                .putExtra("productId", data.get(position).get("productId")));
                    } else if (item.getItemId() == R.id.delete) {
                        hitDeleteProductApi(data.get(position).get("productId"));
                    }

                    return true;
                });

                popup.show(); //showing popup menu
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivImage, ivMore, ivRecommended, ivSwitchStock;

            TextView tvProductName, tvPrice, tvDescription, tvStock;

            RatingBar ratingBar;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);
                ivMore = itemView.findViewById(R.id.ivMore);
                ivRecommended = itemView.findViewById(R.id.ivRecommended);

                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvStock = itemView.findViewById(R.id.tvStock);

                ratingBar = itemView.findViewById(R.id.ratingBar);

                ivSwitchStock = itemView.findViewById(R.id.ivSwitchStock);

            }
        }
    }

    private void hitDeleteProductApi(String productId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("productId", productId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.deleteProduct, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void hitMarkRecommendApi(String recommended, String productId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("recommended", recommended);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.markRecommend+productId, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void hitChangeStockStatusApi(String type, String productId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("type", type);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.putApi(mActivity, AppUrls.changeStock+productId, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJson(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

              hitGetProductListApi();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }


    private void setDefault() {

        b.tvInStock.setBackgroundResource(R.drawable.et_rectangular_border);
        b.tvOutStock.setBackgroundResource(R.drawable.et_rectangular_border);
        b.tvBestSeller.setBackgroundResource(R.drawable.et_rectangular_border);

        b.tvInStock.setTextColor(getResources().getColor(R.color.textGrey));
        b.tvOutStock.setTextColor(getResources().getColor(R.color.textGrey));
        b.tvBestSeller.setTextColor(getResources().getColor(R.color.textGrey));

    }
}