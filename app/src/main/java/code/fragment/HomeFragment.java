package code.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.activity.AddProductActivity;
import code.activity.KycActivity;
import code.activity.MainActivity;
import code.activity.StoreViewActivity;
import code.activity.WalletActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseFragment;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    FragmentHomeBinding b;

    Adapter adapter;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    boolean onlineStatus = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        b = FragmentHomeBinding.inflate(inflater, container, false);

        inits();

        return b.getRoot();

    }

    private void inits() {


        b.llAddProducts.setOnClickListener(this);

        b.ivWallet.setOnClickListener(this);
        b.ivStoreProfile.setOnClickListener(this);
        b.ivOnOff.setOnClickListener(this);
        b.tvViewMore.setOnClickListener(this);
        b.tvClickCall.setOnClickListener(this);
        b.tvStartKyc.setOnClickListener(this);
        b.ivScan.setOnClickListener(this);
        Log.v("tokenz",AppSettings.getString(AppSettings.token));
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetPendingOrderApi();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivScan:

                showScannerDialog();

                break;
            case R.id.llAddProducts:
                AppSettings.putString(AppSettings.isFrom, "Add");
                startActivity(new Intent(mActivity, AddProductActivity.class));
                break;
            case R.id.ivWallet:

                startActivity(new Intent(mActivity, WalletActivity.class));

                break;
            case R.id.ivStoreProfile:

                startActivity(new Intent(mActivity, StoreViewActivity.class));

                break;

            case R.id.ivOnOff:

                hitOnOffApi(onlineStatus ? "2" : "1");

                break;

            case R.id.tvViewMore:

                ((MainActivity) getActivity()).displayView(1);

                break;

            case R.id.tvClickCall:

                AppUtils.makeCall(mActivity, "9876543210");

                break;

            case R.id.tvStartKyc:

                startActivity(new Intent(mActivity, KycActivity.class));

                break;

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        hitGetMerchantDetailApi();
        b.rlKyc.setVisibility(AppSettings.getString(AppSettings.documentStatus).equals("3") ? View.GONE : View.VISIBLE);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_recent_orders, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvOrderNo.setText("#" + data.get(position).get("orderNo"));

            StringBuilder itemName = new StringBuilder();

            try {
                JSONArray jsonArray = new JSONArray(data.get(position).get("products"));
                if (data.get(position).get("orderType").equalsIgnoreCase("1"))
                {
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

                if (jsonArray.length() > 2) {
                    b.tvViewMore.setVisibility(View.VISIBLE);
                } else
                    b.tvViewMore.setVisibility(View.GONE);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("orderId", jsonObject1.getString("orderId"));
                    hashMap.put("orderNo", jsonObject1.getString("orderNo"));
                    hashMap.put("orderType", jsonObject1.getString("orderType"));
                    hashMap.put("products", jsonObject1.getString("products"));
                    arrayList.add(hashMap);
                    if (i == 2) break;
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();


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

    private void hitOnOffApi(String status) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("isOnOff", status);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.status, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOnOffJson(response, status);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseOnOffJson(JSONObject response, String status) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                if (status.equals("1")) {

                    b.ivOnOff.setImageResource(R.drawable.ic_switch_on);
                    onlineStatus = true;
                } else {
                    b.ivOnOff.setImageResource(R.drawable.ic_switch_off);
                    onlineStatus = false;
                }


            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void hitGetMerchantDetailApi() {

        WebServices.getApi(mActivity, AppUrls.myProfile, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseGetDetail(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");

                if (jsonData.getJSONObject("merchantDetailData").getString("isOnOff").equals("1")) {
                    onlineStatus = true;
                    b.ivOnOff.setImageResource(R.drawable.ic_switch_on);

                } else {
                    onlineStatus = false;
                    b.ivOnOff.setImageResource(R.drawable.ic_switch_off);
                }

                // AppSettings.putString(AppSettings.profileImage,jsonData.getString("profileImage"));
                AppUtils.loadPicassoImage(jsonData.getString("profileImage"), b.ivStoreProfile);


                b.tvDoneCount.setText(jsonData.getString("completedOrder"));
                b.tvInProgressCount.setText(jsonData.getString("inProgess"));
                b.tvOutDeliveryCount.setText(jsonData.getString("outForDelivery"));
                b.tvWaitingReviewCount.setText(jsonData.getString("waitingForReview"));
                b.tvCancellOrderCount.setText(jsonData.getString("cancelOrder"));

                AppSettings.putString(AppSettings.documentId, jsonData.getJSONObject("merchantDocumentData").getString("documentId"));
                AppSettings.putString(AppSettings.documentNumber, jsonData.getJSONObject("merchantDocumentData").getString("number"));
                AppSettings.putString(AppSettings.documentFrontImage, jsonData.getJSONObject("merchantDocumentData").getString("frontImage"));
                AppSettings.putString(AppSettings.documentBackImage, jsonData.getJSONObject("merchantDocumentData").getString("backImage"));
                AppSettings.putString(AppSettings.documentStatus, jsonData.getJSONObject("merchantDocumentData").getString("documentStatus"));

                if (jsonObject.has("withdrawalPin")){
                    AppSettings.putString(AppSettings.isWithdrawPinCreated, jsonObject.getString("withdrawalPin"));
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void showScannerDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_qr_code);
        bottomSheetDialog.setCancelable(false);
        //   bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvContinue;
        ImageView ivQrCode;


        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);
        ivQrCode = bottomSheetDialog.findViewById(R.id.ivQrCode);
        ivQrCode.setImageResource(R.drawable.qr_code);

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();


        });

    }
}
