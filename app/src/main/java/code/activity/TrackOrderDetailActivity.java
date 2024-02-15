package code.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityTrackOrderDetailBinding;

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

public class
TrackOrderDetailActivity extends BaseActivity implements View.OnClickListener {

    private ActivityTrackOrderDetailBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    AdapterPreparing adapterPreparing;
    AdapterReady adapterReady;
    AdapterPickedUp adapterPickedUp;

    String type = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTrackOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));

        adapterPreparing = new AdapterPreparing(arrayList);
        b.rvList.setAdapter(adapterPreparing);

        b.tvPreparing.setOnClickListener(this);
        b.tvReady.setOnClickListener(this);
        b.tvPickedUp.setOnClickListener(this);
        hitGetOrdersApi();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvPreparing:

                setDefault();
                b.tvPreparing.setBackgroundResource(R.drawable.rectangular_border_color);
                b.tvPreparing.setTextColor(getResources().getColor(R.color.colorPrimary));

                arrayList.clear();
                adapterPreparing = new AdapterPreparing(arrayList);
                b.rvList.setAdapter(adapterPreparing);

                type = "1";
                hitGetOrdersApi();

                break;

            case R.id.tvReady:

                setDefault();
                b.tvReady.setBackgroundResource(R.drawable.rectangular_border_color);
                b.tvReady.setTextColor(getResources().getColor(R.color.colorPrimary));

                arrayList.clear();
                adapterReady = new AdapterReady(arrayList);
                b.rvList.setAdapter(adapterReady);

                type = "2";
                hitGetOrdersApi();
                break;

            case R.id.tvPickedUp:

                setDefault();
                b.tvPickedUp.setBackgroundResource(R.drawable.rectangular_border_color);
                b.tvPickedUp.setTextColor(getResources().getColor(R.color.colorPrimary));

                arrayList.clear();
                adapterPickedUp = new AdapterPickedUp(arrayList);
                b.rvList.setAdapter(adapterPickedUp);

                type = "3";
                hitGetOrdersApi();
                break;
        }
    }

    private void hitGetOrdersApi() {

        String url = "";

        switch (type) {
            case "1":
                url = AppUrls.getPreparingOrders;
                break;
            case "2":
                url = AppUrls.getReadyOrders;

                break;
            case "3":
                url = AppUrls.getPickedupOrders;

                break;
        }

        WebServices.getApi(mActivity, url, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                switch (type) {
                    case "1":
                        parsePreparingOrders(response);
                        break;
                    case "2":
                        parseReadyOrders(response);
                        break;
                    case "3":
                        parsePickedUpOrders(response);
                        break;
                }
            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseReadyOrders(JSONObject response) {

        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("orderId", jsonData.getString("orderId"));
                    hashMap.put("products", jsonData.getString("products"));
                    hashMap.put("bill", jsonData.getString("bill"));
                    arrayList.add(hashMap);
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterReady.notifyDataSetChanged();

    }

    private void parsePickedUpOrders(JSONObject response) {

        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("orderId", jsonData.getString("orderId"));
                    hashMap.put("orderNo", jsonData.getString("orderNo"));
                    hashMap.put("deliveryAddress", jsonData.getString("deliveryAddress"));
                    arrayList.add(hashMap);
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterPickedUp.notifyDataSetChanged();

    }

    private void parsePreparingOrders(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("orderId", jsonData.getString("orderId"));
                    hashMap.put("orderNo", jsonData.getString("orderNo"));
                    hashMap.put("customerName", jsonData.getString("customerName"));
                    hashMap.put("products", jsonData.getString("products"));
                    hashMap.put("deliveryAddress", jsonData.getString("deliveryAddress"));
                    hashMap.put("customerMobile", jsonData.getString("customerMobile"));
                    hashMap.put("placedAt", jsonData.getString("placedAt"));
                    hashMap.put("acceptedAt", jsonData.getString("acceptedAt"));

                    arrayList.add(hashMap);

                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterPreparing.notifyDataSetChanged();

    }

    private void setDefault() {

        b.tvPreparing.setBackgroundResource(R.drawable.et_rectangular_border);
        b.tvReady.setBackgroundResource(R.drawable.et_rectangular_border);
        b.tvPickedUp.setBackgroundResource(R.drawable.et_rectangular_border);

        b.tvPreparing.setTextColor(getResources().getColor(R.color.textGrey));
        b.tvReady.setTextColor(getResources().getColor(R.color.textGrey));
        b.tvPickedUp.setTextColor(getResources().getColor(R.color.textGrey));

    }

    private class AdapterPreparing extends RecyclerView.Adapter<AdapterPreparing.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterPreparing(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterPreparing.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_prepairing_order, viewGroup, false);
            return new AdapterPreparing.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterPreparing.MyViewHolder holder, final int position) {

            holder.tvCustomerName.setText(data.get(position).get("customerName"));
            holder.tvCustomerLocation.setText(data.get(position).get("deliveryAddress"));
            holder.tvOrderNo.setText("#" + data.get(position).get("orderNo"));

            holder.tvPlacedTime.setText(AppUtils.changeTimeFormat(data.get(position).get("placedAt")));
            holder.tvAcceptedTime.setText(AppUtils.changeTimeFormat(data.get(position).get("acceptedAt")));

            holder.ivCall.setOnClickListener(view -> AppUtils.makeCall(mActivity, data.get(position).get("customerMobile")));

            setItemData(holder.rvItems, data.get(position).get("products"));

            holder.tvReady.setOnClickListener(view -> hitChangeOrderStatusApi(data.get(position).get("orderId")));
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvCustomerName, tvCustomerLocation, tvOrderNo, tvPlacedTime, tvAcceptedTime, tvReady;

            RecyclerView rvItems;

            ImageView ivCall;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvCustomerLocation = itemView.findViewById(R.id.tvCustomerLocation);
                tvOrderNo = itemView.findViewById(R.id.tvOrderNo);
                tvPlacedTime = itemView.findViewById(R.id.tvPlacedTime);
                tvAcceptedTime = itemView.findViewById(R.id.tvAcceptedTime);
                tvReady = itemView.findViewById(R.id.tvReady);

                rvItems = itemView.findViewById(R.id.rvItems);

                ivCall = itemView.findViewById(R.id.ivCall);
            }
        }
    }

    private void hitChangeOrderStatusApi(String orderId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("orderId", orderId);
//            jsonObject.put("orderState", state);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.readyOrder, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseChangeOrderStatus(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseChangeOrderStatus(JSONObject response) {


        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                hitGetOrdersApi();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void setItemData(RecyclerView rvItems, String products) {

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

        try {

            JSONArray jsonArray = new JSONArray(products);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", jsonObject.getString("name"));
                hashMap.put("quantity", jsonObject.getString("quantity"));
                hashMap.put("priceWithQuantity", jsonObject.getString("priceWithQuantity"));

                arrayList.add(hashMap);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        rvItems.setLayoutManager(new GridLayoutManager(mActivity, 1));
        rvItems.setAdapter(new AdapterItem(arrayList));
    }

    private class AdapterReady extends RecyclerView.Adapter<AdapterReady.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterReady(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterReady.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_ready_order, viewGroup, false);
            return new AdapterReady.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterReady.MyViewHolder holder, final int position) {

            holder.tvTotalAmount.setText(AppConstants.currency + "" + data.get(position).get("bill"));

            setItemData(holder.rvItems, data.get(position).get("products"));
/*
            holder.tvPickedUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hitChangeOrderStatusApi(data.get(position).get("orderId"), "3");
                }
            });*/
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            RecyclerView rvItems;

            TextView tvTotalAmount, tvPickedUp;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                rvItems = itemView.findViewById(R.id.rvItems);

                tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
                tvPickedUp = itemView.findViewById(R.id.tvPickedUp);
            }
        }
    }

    private class AdapterPickedUp extends RecyclerView.Adapter<AdapterPickedUp.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterPickedUp(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterPickedUp.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_picked_up_temp, viewGroup, false);
            return new AdapterPickedUp.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterPickedUp.MyViewHolder holder, final int position) {
            holder.tvAddress.setText(data.get(position).get("deliveryAddress"));
            holder.tvOrderNo.setText("#" + data.get(position).get("orderNo"));
/*
            holder.tvDelivered.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hitChangeOrderStatusApi(data.get(position).get("orderId"), "4");
                }
            });*/
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvOrderNo, tvAddress, tvDelivered;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvOrderNo = itemView.findViewById(R.id.tvOrderNo);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvDelivered = itemView.findViewById(R.id.tvDelivered);
            }
        }
    }

    private class AdapterItem extends RecyclerView.Adapter<AdapterItem.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterItem(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterItem.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_item, viewGroup, false);
            return new AdapterItem.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterItem.MyViewHolder holder, final int position) {

            holder.tvItemName.setText(data.get(position).get("quantity") + "*" + data.get(position).get("name"));
            holder.tvAmount.setText(AppConstants.currency + "" + data.get(position).get("priceWithQuantity"));

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvItemName, tvAmount;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvAmount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }


}