package code.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.FragmentProductBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.activity.AddProductActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseFragment;

public class ProductFragment extends BaseFragment implements View.OnClickListener {

    FragmentProductBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        b = FragmentProductBinding.inflate(inflater, container, false);

        inits();

        return b.getRoot();
    }

    private void inits() {

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        b.fabAdd.setOnClickListener(this);

    }

    private void hitGetProductListApi() {

        WebServices.getApi(mActivity, AppUrls.productList, true, true, new WebServicesCallback() {

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
                    hashMap.put("reviewCount", jsonObject1.getString("reviewCount"));
                    hashMap.put("recommended", jsonObject1.getString("recommended"));
                    hashMap.put("price", jsonObject1.getString("price"));
                    hashMap.put("image", jsonObject1.getString("imageOne"));
                    hashMap.put("unit", jsonObject1.getString("unit"));
                    hashMap.put("quantity", jsonObject1.getString("quantity"));
                    hashMap.put("inStock", jsonObject1.getString("inStock"));
                    hashMap.put("sellingPrice", jsonObject1.getString("sellingPrice"));
                    hashMap.put("unitType", jsonObject1.getString("unitType"));
                    hashMap.put("isProductApproved", jsonObject1.getString("isProductApproved"));

                    arrayList.add(hashMap);

                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products), jsonObject.getString(AppConstants.resMsg), 2);

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

            holder.tvPrice.setText(AppConstants.currency + " " + data.get(position).get("price"));

            if (!data.get(position).get("sellingPrice").isEmpty()) {

                holder.tvPrice.setPaintFlags(holder.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvSellingPrice.setText(AppConstants.currency + " " + data.get(position).get("sellingPrice"));
            }


            holder.tvDescription.setText(data.get(position).get("description"));

            if (data.get(position).get("unit").isEmpty()) {
                holder.tvUnit.setVisibility(View.GONE);
            } else {
                holder.tvUnit.setText(data.get(position).get("unit") + " " + data.get(position).get("unitType"));
                holder.tvUnit.setVisibility(View.VISIBLE);
            }


            AppUtils.loadPicassoImage(data.get(position).get("image"), holder.ivImage);

            holder.ratingBar.setRating(AppUtils.returnFloat(data.get(position).get("rating")));
            holder.tvRatingCount.setText("" + AppUtils.roundOff2Digit(data.get(position).get("reviewCount")));
            if (data.get(position).get("recommended").equals("1")) {

                holder.ivRecommended.setImageResource(R.drawable.ic_thumbs_up_red);
            } else holder.ivRecommended.setImageResource(R.drawable.ic_thumbs_up);


            if (data.get(position).get("inStock").equals("1")) {

                holder.tvStock.setText(getString(R.string.inStock));
                holder.ivSwitchStock.setImageResource(R.drawable.ic_switch_on);
            } else {
                holder.tvStock.setText(getString(R.string.itemAUnavailable));
                holder.ivSwitchStock.setImageResource(R.drawable.ic_switch_off);
            }

            holder.ivRecommended.setOnClickListener(view -> hitMarkRecommendApi(data.get(position).get("recommended").equals("1") ? "2" : "1", data.get(position).get("productId")));
            holder.ivSwitchStock.setOnClickListener(view -> hitChangeStockStatusApi(data.get(position).get("inStock").equals("1") ? "2" : "1", data.get(position).get("productId")));

            if (data.get(position).get("isProductApproved").equals("0")) {
                holder.tvStatus.setText(getString(R.string.pending));
                holder.tvStatus.setTextColor(mActivity.getResources().getColor(R.color.colorPrimary));

            } else if (data.get(position).get("isProductApproved").equals("1")) {
                holder.tvStatus.setText(getString(R.string.approved));
                holder.tvStatus.setTextColor(mActivity.getResources().getColor(R.color.green));
            } else {
                holder.tvStatus.setText(getString(R.string.rejected));
                holder.tvStatus.setTextColor(mActivity.getResources().getColor(R.color.red));
            }

            holder.ivMore.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(mActivity, holder.ivMore);

                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {

                    if (item.getItemId() == R.id.edit) {
                        AppSettings.putString(AppSettings.isFrom, "Update");
                        startActivity(new Intent(mActivity, AddProductActivity.class).putExtra("productId", data.get(position).get("productId")));
                    } else if (item.getItemId() == R.id.delete) {
                        removeAlert(data.get(position).get("productId"));
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

            TextView tvProductName, tvPrice, tvDescription, tvStock, tvUnit, tvSellingPrice, tvRatingCount, tvStatus;

            RatingBar ratingBar;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);
                ivMore = itemView.findViewById(R.id.ivMore);
                ivRecommended = itemView.findViewById(R.id.ivRecommended);
                tvRatingCount = itemView.findViewById(R.id.tvRatingCount);

                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvStock = itemView.findViewById(R.id.tvStock);
                tvUnit = itemView.findViewById(R.id.tvUnit);
                tvSellingPrice = itemView.findViewById(R.id.tvSellingPrice);
                tvStatus = itemView.findViewById(R.id.tvStatus);

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

        WebServices.postApi(mActivity, AppUrls.markRecommend + productId, json, true, true, new WebServicesCallback() {

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

        WebServices.putApi(mActivity, AppUrls.changeStock + productId, json, true, true, new WebServicesCallback() {

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
                AppUtils.showMessageDialog(mActivity, getString(R.string.products), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fabAdd:
                AppSettings.putString(AppSettings.isFrom, "Add");
                startActivity(new Intent(mActivity, AddProductActivity.class));

                break;

        }

    }

    private void removeAlert(final String removeId) {
        new AlertDialog.Builder(mActivity)
                .setTitle(getString(R.string.deleteProduct))
                .setMessage(getString(R.string.areYouSureDeleteProduct) + "?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    hitDeleteProductApi(removeId);
                })

                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.alert)
                .show();

    }

    @Override
    public void onResume() {
        super.onResume();
        hitGetProductListApi();
    }
}
