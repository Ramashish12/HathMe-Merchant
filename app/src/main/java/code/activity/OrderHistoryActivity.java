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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityOrderHistoryBinding;

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

public class OrderHistoryActivity extends BaseActivity implements View.OnClickListener {

    ActivityOrderHistoryBinding b;

    Adapter adapter;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity,3));

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetOrderHistoryApi();

    }

    private void hitGetOrderHistoryApi() {

        WebServices.getApi(mActivity, AppUrls.OrderHistory, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOrderHistory(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseOrderHistory(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray= jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length() ; i++) {

                    JSONObject jsonObject1=jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap=new HashMap<>();
                    hashMap.put("orderNo", jsonObject1.getString("orderNo"));
                    hashMap.put("createdAt", jsonObject1.getString("createdAt"));
                    hashMap.put("toPay", jsonObject1.getString("toPay"));
                    hashMap.put("status", jsonObject1.getString("status"));
                    hashMap.put("itemCount", String.valueOf(jsonObject1.getJSONArray("products").length()));

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

    @Override
    public void onClick(View view) {

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_order_history, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvOrderNo.setText("#"+data.get(position).get("orderNo"));
            holder.tvAmount.setText(AppConstants.currency+""+data.get(position).get("toPay"));

            holder.tvDateAndCount.setText(AppUtils.changeDateTimeFormat(data.get(position).get("placedAt"))+" | "+
                    data.get(position).get("itemCount")+" "+getString(R.string.items));

            if (data.get(position).get("status").equals("3")){
                holder.tvStatus.setText(getString(R.string.cancelled));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.red));
            }
            else {
                holder.tvStatus.setText(getString(R.string.delivered));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.green));
            }

            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(mActivity, FeedbackActivity.class);
                    intent.putExtra("orderNo", data.get(position).get("orderNo"));
                    intent.putExtra("toPay", data.get(position).get("toPay"));
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
             return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvOrderNo, tvAmount, tvDateAndCount, tvStatus;

            LinearLayout llMain;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvOrderNo = itemView.findViewById(R.id.tvOrderNo);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvDateAndCount = itemView.findViewById(R.id.tvDateAndCount);
                tvStatus = itemView.findViewById(R.id.tvStatus);

                llMain = itemView.findViewById(R.id.llMain);

            }
        }
    }

}