package code.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityPendingOrdersBinding;

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

public class PendingOrdersActivity extends BaseActivity implements View.OnClickListener {

    ActivityPendingOrdersBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPendingOrdersBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetPendingOrderApi();

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
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvOrderNo.setText("#" + data.get(position).get("orderNo"));

            StringBuilder itemName = new StringBuilder();

            try {
                JSONArray jsonArray = new JSONArray(data.get(position).get("products"));

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    itemName.append("\n").append(jsonObject.getString("name"))
                            .append(" * ").append(jsonObject.getString("quantity"));
                }

                holder.tvItems.setText(itemName);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            holder.tvAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    hitAcceptOrderApi(data.get(position).get("orderId"), "2");
                }
            });


            holder.tvReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    hitAcceptOrderApi(data.get(position).get("orderId"), "3");
                }
            });


        }

        @Override
        public int getItemCount() {
            return data.size();


        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvOrderNo, tvItems, tvAccept, tvReject;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvOrderNo = itemView.findViewById(R.id.tvOrderNo);
                tvItems = itemView.findViewById(R.id.tvItems);
                tvAccept = itemView.findViewById(R.id.tvAccept);
                tvReject = itemView.findViewById(R.id.tvReject);
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

    }
}