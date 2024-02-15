package code.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityPhotoOrderBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.model.Product;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class PhotoOrderActivity extends BaseActivity implements View.OnClickListener {

    private ActivityPhotoOrderBinding b;
    String Type = "1";
    private String id = "";

    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private ArrayList<Product> productList = new ArrayList<>();
    private Adapter adapter;
    private AdapterProduct productAdapter;

    private boolean isAlreadyAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPhotoOrderBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(v -> onBackPressed());
        b.header.tvHeading.setText(getString(R.string.order));

        b.llPicture.setOnClickListener(this);
        b.tvSearchProduct.setOnClickListener(this);
        b.tvSubmit.setOnClickListener(this);
        b.tvAddProductManual.setOnClickListener(this);
        if (Type.equalsIgnoreCase("1")) {
            adapter = new Adapter(arrayList);
            b.rvList.setAdapter(adapter);
        } else {
            productAdapter = new AdapterProduct(productList);
            b.rvList.setAdapter(productAdapter);
        }

        Intent intent = getIntent();

        id = getIntent().getStringExtra("id");

        b.tvCustomerName.setText(intent.getStringExtra("name"));
        b.tvDateTime.setText(AppUtils.changeDateFormat3(intent.getStringExtra("createdAt")));
        AppUtils.loadPicassoImage(intent.getStringExtra("document"), b.ivPicture);

        hitCheckBucketApi();

    }

    private void hitCheckBucketApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {

            jsonObject.put("requestOrderId", id);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.bucketProductList, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCheckBucketJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseCheckBucketJson(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                isAlreadyAdded = true;
                b.tvSubmit.setVisibility(View.GONE);
                b.tvAddProductManual.setVisibility(View.GONE);
                b.tvAddItem.setText(getString(R.string.addedProducts));
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("productName"));
                    hashMap.put("productAmount", jsonObject1.getString("productAmount"));
//                    hashMap.put("unitType", jsonObject1.getString("unitType"));
//                    if (jsonObject1.getString("sellingPrice").isEmpty())
//                        hashMap.put("price", jsonObject1.getString("price"));
//                    else {
//                        hashMap.put("price", jsonObject1.getString("sellingPrice"));
//                    }
//                    hashMap.put("quantity", jsonObject1.getString("productQuantity"));

                    arrayList.add(hashMap);

                }

                calculateBill();

            } else {
                b.tvSubmit.setVisibility(View.VISIBLE);
                b.tvAddProductManual.setVisibility(View.VISIBLE);
                isAlreadyAdded = false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.llPicture:

                showImageInFull();

                break;

            case R.id.tvSearchProduct:
                Type = "1";
                if (!productList.isEmpty()) {
                    productList.clear();
                    productAdapter.notifyDataSetChanged();
                }
                adapter = new Adapter(arrayList);
                b.rvList.setAdapter(adapter);
                showSearchBox();
                calculateBill();
                break;
            case R.id.tvAddProductManual:
                if (!arrayList.isEmpty()) {
                    arrayList.clear();
                    adapter.notifyDataSetChanged();
                }
                calculateItemBill();
                productAdapter = new AdapterProduct(productList);
                b.rvList.setAdapter(productAdapter);
                Type = "2";
                showAddItemBox();

                break;

            case R.id.tvSubmit:

                validate();

                break;

        }

    }

    private void validate() {
        if (productList.size() == 0) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseAddProduct));
        } else {
            hitSubmitProductApi();
            //AppUtils.showToastSort(mActivity, productList.toString());
        }

    }

    private void hitSubmitProductApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
//            for (int i = 0; i < arrayList.size(); i++) {
//
//                JSONObject jsonItem = new JSONObject();
//                jsonItem.put("productId", arrayList.get(i).get("productId"));
//
//                jsonItem.put("quantity", arrayList.get(i).get("quantity"));
//
//                jsonArray.put(jsonItem);
//            }
            for (int i = 0; i < productList.size(); i++) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("productName", productList.get(i).getProductName());
                jsonItem.put("productPrice", productList.get(i).getPrice());
                jsonArray.put(jsonItem);
            }
            jsonObject.put("requestOrderId", id);
            jsonObject.put("products", jsonArray);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.addProductInBucket, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSubmitJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseSubmitJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                hitCheckBucketApi();
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 1);
                // AppUtils.showResMsgToastSort(mActivity, jsonObject);
            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showSearchBox() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity);
        bottomSheetDialog.setContentView(R.layout.dialog_product_search);
        bottomSheetDialog.show();

        RecyclerView rvList = bottomSheetDialog.findViewById(R.id.rvList);
        EditText etSearch = bottomSheetDialog.findViewById(R.id.etSearch);

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

        AdapterSearch adapter = new AdapterSearch(arrayList, bottomSheetDialog);
        rvList.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().isEmpty()) {
                    arrayList.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    hitSearchProductApi(s.toString(), adapter, arrayList, bottomSheetDialog);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void hitSearchProductApi(String string, AdapterSearch adapter, ArrayList<HashMap<String, String>> arrayList, BottomSheetDialog bottomSheetDialog) {

        WebServices.getApi(mActivity, AppUrls.productList + "?search=" + string, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSearchJson(response, adapter, arrayList, bottomSheetDialog);
            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseSearchJson(JSONObject response, AdapterSearch adapter, ArrayList<HashMap<String, String>> arrayList, BottomSheetDialog bottomSheetDialog) {

        if (bottomSheetDialog.isShowing()) {

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
                        hashMap.put("unit", jsonObject1.getString("unit"));
                        hashMap.put("unitType", jsonObject1.getString("unitType"));
                        if (jsonObject1.getString("sellingPrice").isEmpty())
                            hashMap.put("price", jsonObject1.getString("price"));
                        else {
                            hashMap.put("price", jsonObject1.getString("sellingPrice"));
                        }
                        hashMap.put("stockQuantity", jsonObject1.getString("quantity"));
                        hashMap.put("quantity", "1");

                        arrayList.add(hashMap);

                    }

                } else
                    AppUtils.showMessageDialog(mActivity, getString(R.string.products), jsonObject.getString(AppConstants.resMsg), 2);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();
        }
    }

    private void showImageInFull() {

        Dialog dialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image);
        dialog.setCancelable(false);

        //Get the ImageView from the dialog layout
        ImageView dialogImage = dialog.findViewById(R.id.ivImage);
        ImageView ivBack = dialog.findViewById(R.id.ivBack);

        //Set the drawable to the dialog ImageView
        dialogImage.setImageDrawable(b.ivPicture.getDrawable());

        //Set an OnClickListener for the dialog ImageView to dismiss the dialog
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //Show the dialog
        dialog.show();

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_product, viewGroup, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

//            if (isAlreadyAdded) {
//                holder.rlAdd.setVisibility(View.GONE);
//            } else {
//                holder.rlAdd.setVisibility(View.VISIBLE);
//            }

            holder.tvName.setText(data.get(position).get("name"));
            holder.tvPrice.setText(AppConstants.currency + " " + data.get(position).get("productAmount"));
            holder.ivRemove.setVisibility(View.GONE);
//            holder.tvQuantity.setText(data.get(position).get("quantity"));
//
//            if (data.get(position).get("unit").isEmpty()) {
//                holder.tvUnit.setVisibility(View.GONE);
//            } else {
//                holder.tvUnit.setText(data.get(position).get("unit") + " " + data.get(position).get("unitType"));
//                holder.tvUnit.setVisibility(View.VISIBLE);
//            }
//
//            holder.ivPlus.setOnClickListener(view -> {
//
//                int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());
//
//                if (qty < AppUtils.returnInt(data.get(position).get("stockQuantity"))) {
//
//                    qty = qty + 1;
//
//                    HashMap<String, String> hashMap = data.get(position);
//                    hashMap.put("quantity", String.valueOf(qty));
//                    data.set(position, hashMap);
//                    notifyItemChanged(position);
//
//                    calculateBill();
//                } else {
//                    AppUtils.showToastSort(mActivity, getString(R.string.only) + " " + qty + " " + getString(R.string.quantityAvailable));
//                }
//            });
//
//            holder.ivMinus.setOnClickListener(view -> {
//
//                int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());
//
//                if (qty > 1) {
//                    qty = qty - 1;
//
//                    HashMap<String, String> hashMap = data.get(position);
//                    hashMap.put("quantity", String.valueOf(qty));
//                    data.set(position, hashMap);
//                    notifyItemChanged(position);
//
//                    calculateBill();
//                } else {
//                    data.remove(position);
//                    notifyItemChanged(position);
//                    calculateBill();
//                }
//
//            });
        }


        @Override
        public int getItemCount() {
            return data.size();


        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvPrice;
            ImageView ivRemove;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvName);
                ivRemove = itemView.findViewById(R.id.ivRemove);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
        }
    }


    private class AdapterSearch extends RecyclerView.Adapter<AdapterSearch.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;

        BottomSheetDialog bottomSheetDialog;


        private AdapterSearch(ArrayList<HashMap<String, String>> arrayList, BottomSheetDialog bottomSheetDialog) {

            data = arrayList;
            this.bottomSheetDialog = bottomSheetDialog;
        }

        @NonNull
        @Override
        public AdapterSearch.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_search_product, viewGroup, false);
            return new AdapterSearch.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterSearch.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvName.setText(data.get(position).get("name"));

            if (data.get(position).get("unit").isEmpty()) {
                holder.tvUnit.setVisibility(View.GONE);
            } else {
                holder.tvUnit.setText(data.get(position).get("unit") + " " + data.get(position).get("unitType"));
                holder.tvUnit.setVisibility(View.VISIBLE);
            }

            holder.tvAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for (int i = 0; i < arrayList.size(); i++) {
                        if (arrayList.get(i).get("productId").equals(data.get(position).get("productId"))) {
                            AppUtils.showToastSort(mActivity, getString(R.string.productAlreadyAdded));
                            return;
                        }
                    }
                    arrayList.add(data.get(position));
                    bottomSheetDialog.dismiss();
                    adapter.notifyDataSetChanged();
                    calculateBill();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvUnit;

            TextView tvAdd;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvName);
                tvUnit = itemView.findViewById(R.id.tvUnit);

                tvAdd = itemView.findViewById(R.id.tvAdd);

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateBill() {

        double tAmount = 0;

        for (int i = 0; i < arrayList.size(); i++) {

            tAmount = tAmount + (AppUtils.returnDouble(arrayList.get(i).get("productAmount")));

        }
        b.tvTotalBill.setText(getString(R.string.totalBill) + " " + AppConstants.currency + " " + tAmount);
    }

    private void showAddItemBox() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity);
        bottomSheetDialog.setContentView(R.layout.dialog_add_product);
        bottomSheetDialog.show();

        EditText etProductName = bottomSheetDialog.findViewById(R.id.etProductName);
        EditText etPrice = bottomSheetDialog.findViewById(R.id.etPrice);
        TextView tvAdd = bottomSheetDialog.findViewById(R.id.tvAdd);
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etProductName.getText().toString().equalsIgnoreCase("") || etProductName.getText().toString().isEmpty()) {
                    AppUtils.showToastSort(mActivity, getString(R.string.enterProductName));
                } else if (etPrice.getText().toString().equalsIgnoreCase("") || etPrice.getText().toString().isEmpty()) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPrice));
                } else {
                    String productName = etProductName.getText().toString();
                    double price = Double.parseDouble(etPrice.getText().toString());
                    Product product = new Product(productName, price);
                    productList.add(product);
                    productAdapter.notifyDataSetChanged();
                    calculateItemBill();
                    bottomSheetDialog.dismiss();
                }
            }
        });
    }

    private class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.MyViewHolder> {
        private ArrayList<Product> productList;

        private AdapterProduct(ArrayList<Product> productList) {
            this.productList = productList;
        }

        @NonNull
        @Override
        public AdapterProduct.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_product, viewGroup, false);
            return new AdapterProduct.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterProduct.MyViewHolder holder, final int position) {
            Product product = productList.get(position);
            holder.tvName.setText(product.getProductName());
            holder.tvPrice.setText(getString(R.string.rupeeSymbol) + String.valueOf(product.getPrice()));
            ///delete product
            holder.ivRemove.setOnClickListener(v -> {
                productList.remove(position);
                productAdapter.notifyItemRemoved(position);
                productAdapter.notifyItemRangeChanged(position, productList.size());
                calculateItemBill();
            });

        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            ImageView ivRemove;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                ivRemove = itemView.findViewById(R.id.ivRemove);
            }
        }
    }

    private void calculateItemBill() {
        double tAmount = 0;
        for (int i = 0; i < productList.size(); i++) {
            tAmount = tAmount + (productList.get(i).getPrice());
        }
        b.tvTotalBill.setText(getString(R.string.totalBill) + " " + AppConstants.currency + " " + tAmount);
    }
}