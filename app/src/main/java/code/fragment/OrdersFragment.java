package code.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.FragmentOrdersBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.activity.PhotoOrderActivity;
import code.activity.TrackOrderActivity;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseFragment;

public class OrdersFragment extends BaseFragment implements View.OnClickListener {

    FragmentOrdersBinding b;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    Adapter adapter;
    AdapterRequestOrder adapterRequestOrder;

    private int type = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        b = FragmentOrdersBinding.inflate(inflater, container, false);

        inits();

        return b.getRoot();
    }

    private void inits() {

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapterRequestOrder = new AdapterRequestOrder(arrayList);
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        b.tvPending.setOnClickListener(this);
        b.tvRequestOrder.setOnClickListener(this);
        b.tvOnGoing.setOnClickListener(this);
        b.tvDelivered.setOnClickListener(this);


    }

    private void hitGetPendingOrderApi() {

        WebServices.getApi(mActivity, AppUrls.OrderList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOrder(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseOrder(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("orderId", jsonObject1.getString("orderId"));
                    hashMap.put("orderNo", jsonObject1.getString("orderNo"));
                    hashMap.put("products", jsonObject1.getString("products"));
                    hashMap.put("totalAmount", jsonObject1.getString("totalAmount"));
                    hashMap.put("orderType", jsonObject1.getString("orderType"));
                    hashMap.put("discount", jsonObject1.getString("discount"));
                    if (jsonObject1.has("orderState")) {
                        hashMap.put("orderState", jsonObject1.getString("orderState"));
                    }
                    if (jsonObject1.has("discount")) {
                        hashMap.put("discount", jsonObject1.getString("discount"));
                    }
                    if (jsonObject1.has("deliverAt")) {
                        hashMap.put("deliverAt", jsonObject1.getString("deliverAt"));
                    }
                    if (jsonObject1.has("paymentStatus")) {
                        hashMap.put("paymentStatus", jsonObject1.getString("paymentStatus"));
                    }

                    arrayList.add(hashMap);
                }

            }

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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_pending_order, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvOrderNo.setText("#" + data.get(position).get("orderNo"));
            holder.tvAmount.setText(AppConstants.currency + " " +
                    (AppUtils.returnDouble(data.get(position).get("totalAmount"))-
                            AppUtils.returnDouble(data.get(position).get("discount"))));

            if (data.get(position).get("discount").equals("") ||
                    data.get(position).get("discount").equals("0") ||
                    data.get(position).get("discount").equals("null")) {
                holder.rlDiscount.setVisibility(View.GONE);
            } else {
                holder.rlDiscount.setVisibility(View.VISIBLE);
                holder.tvDiscountAmount.setText(AppConstants.currency + " " + data.get(position).get("discount"));
            }

            StringBuilder itemName = new StringBuilder();

            try {
                JSONArray jsonArray = new JSONArray(data.get(position).get("products"));
                if (data.get(position).get("orderType").equalsIgnoreCase("1")) {
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        itemName.append("\n").append(jsonObject.getString("name"))
                                .append(" * ").append(jsonObject.getString("quantity"));
                    }

                    holder.tvItems.setText(itemName);
                }
                else
                {
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        itemName.append("\n").append(jsonObject.getString("productName"))
                                .append(" * ").append(jsonObject.getString("quantity"));
                    }

                    holder.tvItems.setText(itemName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.tvAccept.setOnClickListener(view -> hitAcceptOrderApi(data.get(position).get("orderId"), "2"));

            holder.tvReject.setOnClickListener(view -> hitAcceptOrderApi(data.get(position).get("orderId"), "3"));

            if (type == 3) {
                holder.tvTrack.setText(mActivity.getString(R.string.deliveredAt) + " : " + AppUtils.changeDateFormat3(data.get(position).get("deliverAt")));
            }

            holder.tvTrack.setOnClickListener(view -> {

                if (type == 2) {

                    Intent intent = new Intent(mActivity, TrackOrderActivity.class);
                    intent.putExtra("orderId", data.get(position).get("orderId"));
                    startActivity(intent);
                }

            });
            holder.llMain.setOnClickListener(view -> {
                    if (type!=1)
                    {
                        Intent intent = new Intent(mActivity, TrackOrderActivity.class);
                        intent.putExtra("orderId", data.get(position).get("orderId"));
                        startActivity(intent);
                    }
            });


        }

        @Override
        public int getItemCount() {
            return data.size();


        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvOrderNo, tvItems, tvAccept, tvReject, tvTrack, tvStatus, tvAmount, tvDiscountAmount,tvRateUser;

            LinearLayout llAcceptReject, llMain;
            RelativeLayout rlDiscount;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvOrderNo = itemView.findViewById(R.id.tvOrderNo);
                tvItems = itemView.findViewById(R.id.tvItems);
                tvAccept = itemView.findViewById(R.id.tvAccept);
                tvReject = itemView.findViewById(R.id.tvReject);
                tvTrack = itemView.findViewById(R.id.tvTrack);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                llAcceptReject = itemView.findViewById(R.id.llAcceptReject);
                rlDiscount = itemView.findViewById(R.id.rlDiscount);
                tvDiscountAmount = itemView.findViewById(R.id.tvDiscountAmount);
                llMain = itemView.findViewById(R.id.llMain);
                tvRateUser = itemView.findViewById(R.id.tvRateUser);

                switch (type) {

                    case 1:
                        llAcceptReject.setVisibility(View.VISIBLE);
                        tvRateUser.setVisibility(View.GONE);
                        tvTrack.setVisibility(View.GONE);
                        tvStatus.setText(getString(R.string.pending));
                        tvStatus.setBackgroundResource(R.color.red);


                        break;
                    case 2:
                        llAcceptReject.setVisibility(View.GONE);
                        tvRateUser.setVisibility(View.GONE);
                        tvTrack.setVisibility(View.VISIBLE);
                        tvTrack.setText(getString(R.string.trackOrderDetails));
                        tvStatus.setText(getString(R.string.ongoing));
                        tvStatus.setBackgroundResource(com.google.android.libraries.places.R.color.quantum_yellow);


                        break;
                    case 3:
                        llAcceptReject.setVisibility(View.GONE);
                        tvTrack.setVisibility(View.VISIBLE);
                        tvRateUser.setVisibility(View.GONE);
                        tvStatus.setText(getString(R.string.delivered));
                        tvStatus.setBackgroundResource(R.color.green);


                        break;

                }
            }
        }
    }
    private class AdapterRequestOrder extends RecyclerView.Adapter<AdapterRequestOrder.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterRequestOrder(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterRequestOrder.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_requested_order, viewGroup, false);
            return new AdapterRequestOrder.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterRequestOrder.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvName.setText(data.get(position).get("name"));
            holder.tvDateTime.setText(AppUtils.changeDateFormat3(data.get(position).get("createdAt")));

            holder.tvViewOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mActivity, PhotoOrderActivity.class)
                            .putExtra("id", data.get(position).get("_id"))
                            .putExtra("name", data.get(position).get("name"))
                            .putExtra("document", data.get(position).get("document"))
                            .putExtra("createdAt", data.get(position).get("createdAt")));
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();


        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvDateTime,tvViewOrder;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvName);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvViewOrder = itemView.findViewById(R.id.tvViewOrder);
            }
        }
    }

    private void hitAcceptOrderApi(String orderId, String status) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("orderId", orderId);
            jsonObject.put("status", status);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("Body",json.toString());
        String url = "";

        if (status.equals("2")) url = AppUrls.AcceptOrder;
        else url = AppUrls.CancelOrder;

        WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAcceptReject(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseAcceptReject(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetPendingOrderApi();

            } else
                AppUtils.showResMsgToastSort(mActivity, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvPending:

                type = 1;
                setDefault();
                b.tvPending.setBackgroundResource(R.color.red);
                b.tvPending.setTextColor(getResources().getColor(R.color.white));
                arrayList.clear();
                b.rvList.setAdapter(adapter);
                hitGetPendingOrderApi();

                break;

            case R.id.tvRequestOrder:

                type = 4;
                setDefault();
                b.tvRequestOrder.setBackgroundResource(R.color.colorPrimary);
                b.tvRequestOrder.setTextColor(getResources().getColor(R.color.white));
                arrayList.clear();
                b.rvList.setAdapter(adapterRequestOrder);
                hitGetRequestOrderApi();

                break;

            case R.id.tvOnGoing:

                type = 2;
                setDefault();
                b.tvOnGoing.setBackgroundResource(R.color.colorPrimary);
                b.tvOnGoing.setTextColor(getResources().getColor(R.color.white));
                arrayList.clear();
                b.rvList.setAdapter(adapter);
                hitGetOngoingApi();

                break;

            case R.id.tvDelivered:

                type = 3;
                setDefault();
                b.tvDelivered.setBackgroundResource(R.color.green);
                b.tvDelivered.setTextColor(getResources().getColor(R.color.white));
                arrayList.clear();
                b.rvList.setAdapter(adapter);
                hitGetDeliveredApi();

                break;

        }

    }

    private void hitGetRequestOrderApi() {

        WebServices.getApi(mActivity, AppUrls.RequestUserOrderList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseRequestOrder(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseRequestOrder(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("mobile", jsonObject1.getString("mobile"));
                    hashMap.put("document", jsonObject1.getString("document"));
                    hashMap.put("createdAt", jsonObject1.getString("createdAt"));

                    arrayList.add(hashMap);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterRequestOrder.notifyDataSetChanged();
    }

    private void hitGetDeliveredApi() {

        WebServices.getApi(mActivity, AppUrls.getDeliveredOrders, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOrder(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void hitGetOngoingApi() {

        WebServices.getApi(mActivity, AppUrls.getOngoingOrders, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOrder(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void setDefault() {

        b.tvPending.setBackgroundResource(0);
        b.tvRequestOrder.setBackgroundResource(0);
        b.tvOnGoing.setBackgroundResource(0);
        b.tvDelivered.setBackgroundResource(0);

        b.tvPending.setTextColor(getResources().getColor(R.color.textBlack));
        b.tvRequestOrder.setTextColor(getResources().getColor(R.color.textBlack));
        b.tvOnGoing.setTextColor(getResources().getColor(R.color.textBlack));
        b.tvDelivered.setTextColor(getResources().getColor(R.color.textBlack));

    }

    @Override
    public void onResume() {
        super.onResume();

        switch (type) {

            case 1:

                b.tvPending.performClick();
                break;
            case 2:

                b.tvOnGoing.performClick();
                break;
            case 3:

                b.tvDelivered.performClick();
                break;

        }

    }
}
