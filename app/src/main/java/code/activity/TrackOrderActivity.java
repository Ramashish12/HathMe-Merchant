package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityTrackOrderBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import code.maplivetracking.LiveTrackingActivity;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class TrackOrderActivity extends BaseActivity implements View.OnClickListener {

    private ActivityTrackOrderBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    String deliveryBoyLat = "", deliveryBoyLong = "", orderId = "", number = "",customerName = "",customerProfile = "";
    private AdapterItem adapterItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTrackOrderBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        adapterItem = new AdapterItem(arrayList);
        b.rvList.setAdapter(adapterItem);

        b.header.ivBack.setOnClickListener(view -> onBackPressed());

        b.header.tvHeading.setText(getString(R.string.trackOrderDetails));
        b.tvOrderReady.setOnClickListener(this);
        b.rlTrack.setOnClickListener(this);
        b.rlSupport.setOnClickListener(this);
        b.rlCallDriver.setOnClickListener(this);
        b.ivQrCode.setOnClickListener(this);
        b.tvRateCustomer.setOnClickListener(this);

        getIntentValues();

    }

    private void getIntentValues() {

        if (getIntent().getExtras() != null) {
            orderId = getIntent().getStringExtra("orderId");
            hitGetOrderDetailApi(orderId);
        }

    }

    private void hitGetOrderDetailApi(String orderId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("orderId", orderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.OrderDetail, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOrderDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseOrderDetail(JSONObject response) {


        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                jsonObject = jsonObject.getJSONObject("data");
                JSONObject  customerDetails = jsonObject.getJSONObject("customerDetails");

                b.tvOrderId.setText("#" + jsonObject.getString("orderNo"));
                b.tvDateTime.setText(AppUtils.changeDateFormat3(jsonObject.getString("createdAt")));
                customerName = jsonObject.getString("customerName");
                customerProfile = jsonObject.getString("profileImage");


                if (jsonObject.getString("status").equalsIgnoreCase("2")) {
                    b.tvPlacedTime.setText(extractTime(jsonObject.getString("placeOrderTime")));
                    b.tvAcceptedTime.setText(extractTime(jsonObject.getString("merchantOrderAcceptedTime")));
                    b.view3.setVisibility(View.GONE);

                    b.rlDeliveredOrder.setVisibility(View.GONE);
                    b.llDriverDetails.setVisibility(View.GONE);
                    if (!jsonObject.getString("driverOrderAcceptedTime").equalsIgnoreCase("")) {
                        b.tvDriverAssignTime.setText(extractTime(jsonObject.getString("driverOrderAcceptedTime")));
                        b.rlDriverAssign.setVisibility(View.VISIBLE);
                        b.view2.setVisibility(View.VISIBLE);
                        b.llDriverDetails.setVisibility(View.VISIBLE);
                        JSONObject object = jsonObject.getJSONObject("driverData");
                        AppUtils.loadPicassoImage(object.getString("profileImage"), b.ivDriverImage);
                        b.tvDriverName.setText(object.getString("name"));
                        number = object.getString("mobile");
                        // b.rlTrack.setVisibility(View.VISIBLE);
                        if (!jsonObject.getString("driverLatitude").equalsIgnoreCase("")) {
                            b.rlTrack.setVisibility(View.VISIBLE);
                        } else {
                            b.rlTrack.setVisibility(View.GONE);
                        }
                    } else {
                        b.rlDriverAssign.setVisibility(View.GONE);
                        b.view2.setVisibility(View.GONE);
                        b.llDriverDetails.setVisibility(View.GONE);
                    }

                } else if (jsonObject.getString("status").equalsIgnoreCase("4")) {
                    b.view2.setVisibility(View.VISIBLE);
                    b.view3.setVisibility(View.GONE);
                    b.rlDriverAssign.setVisibility(View.VISIBLE);
                    b.llDriverDetails.setVisibility(View.VISIBLE);
                    b.rlDeliveredOrder.setVisibility(View.GONE);
                    b.tvPlacedTime.setText(extractTime(jsonObject.getString("createdAt")));
                    b.tvAcceptedTime.setText(extractTime(jsonObject.getString("merchantOrderAcceptedTime")));
                    b.tvDriverAssignTime.setText(extractTime(jsonObject.getString("driverOrderAcceptedTime")));
                    JSONObject object = jsonObject.getJSONObject("driverData");
                    AppUtils.loadPicassoImage(object.getString("profileImage"), b.ivDriverImage);
                    b.tvDriverName.setText(object.getString("name"));
                    number = object.getString("mobile");
                    // b.rlTrack.setVisibility(View.VISIBLE);
                    if (!jsonObject.getString("driverLatitude").equalsIgnoreCase("")) {
                        b.rlTrack.setVisibility(View.VISIBLE);
                    } else {
                        b.rlTrack.setVisibility(View.GONE);
                    }
                } else if (jsonObject.getString("status").equalsIgnoreCase("5")) {
                    b.view2.setVisibility(View.VISIBLE);
                    b.view3.setVisibility(View.GONE);
                    b.rlDriverAssign.setVisibility(View.VISIBLE);
                    b.llDriverDetails.setVisibility(View.VISIBLE);
                    b.rlDeliveredOrder.setVisibility(View.GONE);
                    b.tvPlacedTime.setText(extractTime(jsonObject.getString("createdAt")));
                    b.tvAcceptedTime.setText(extractTime(jsonObject.getString("merchantOrderAcceptedTime")));
                    b.tvDriverAssignTime.setText(extractTime(jsonObject.getString("driverOrderAcceptedTime")));
                    JSONObject object = jsonObject.getJSONObject("driverData");
                    AppUtils.loadPicassoImage(object.getString("profileImage"), b.ivDriverImage);
                    b.tvDriverName.setText(object.getString("name"));
                    number = object.getString("mobile");
                    // b.rlTrack.setVisibility(View.VISIBLE);
                    if (!jsonObject.getString("driverLatitude").equalsIgnoreCase("")) {
                        b.rlTrack.setVisibility(View.VISIBLE);
                    } else {
                        b.rlTrack.setVisibility(View.GONE);
                    }
                } else if (jsonObject.getString("status").equalsIgnoreCase("6")) {
                    b.view2.setVisibility(View.VISIBLE);
                    b.view3.setVisibility(View.VISIBLE);
                    b.rlDriverAssign.setVisibility(View.VISIBLE);
                    b.rlDeliveredOrder.setVisibility(View.VISIBLE);
                    b.llDriverDetails.setVisibility(View.VISIBLE);
                    b.tvPlacedTime.setText(extractTime(jsonObject.getString("createdAt")));
                    b.tvAcceptedTime.setText(extractTime(jsonObject.getString("merchantOrderAcceptedTime")));
                    b.tvDriverAssignTime.setText(extractTime(jsonObject.getString("driverOrderAcceptedTime")));
                    b.tvOrderDeliveredTime.setText(extractTime(jsonObject.getString("deliveredOrderTime")));
                    JSONObject object = jsonObject.getJSONObject("driverData");
                    AppUtils.loadPicassoImage(object.getString("profileImage"), b.ivDriverImage);
                    b.tvDriverName.setText(object.getString("name"));
                    number = object.getString("mobile");
                    b.rlTrack.setVisibility(View.GONE);
                    if (jsonObject.getString("mercToCustRating").equals(0)||jsonObject.getString("mercToCustRating").equalsIgnoreCase("0"))
                    {
                        b.rlSelfRateReview.setVisibility(View.GONE);
                        b.tvRateCustomer.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        b.tvRateCustomer.setVisibility(View.GONE);
                        b.rlSelfRateReview.setVisibility(View.VISIBLE);
                        b.SelfRatingBar.setRating(AppUtils.returnFloat(jsonObject.getString("mercToCustRating")));
                        b.tvSelfReview.setText(jsonObject.getString("mercToCustComment"));
                    }
                }
                if (jsonObject.getString("discount").isEmpty()) {
                    b.tvDiscount.setVisibility(View.GONE);
                } else {
                    b.tvDiscount.setVisibility(View.VISIBLE);
                    b.tvDiscount.setText(getString(R.string.discount) + " : " + AppConstants.currency + jsonObject.getString("discount") + " (" +
                            jsonObject.getString("couponCode") + ")");
                }

//                b.tvTaxCharges.setText(getString(R.string.taxCharges) + " : " + AppConstants.currency + jsonObject.getString("taxesAndCharges"));
                b.tvCustomerName.setText(getString(R.string.customerName) + " : " + jsonObject.getString("customerName"));

                b.tvTotalAmount.setText(AppConstants.currency  + " " +
                        (AppUtils.returnDouble(jsonObject.getString("totalAmount"))-
                                AppUtils.returnDouble(jsonObject.getString("discount"))));

                if (jsonObject.getString("paymentStatus").equals("1")) {

                    b.tvPaid.setText(getString(R.string.paid));
                    b.tvPaid.setTextColor(getResources().getColor(R.color.green));
                    b.tvPaid.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.green));

                } else {
                    b.tvPaid.setText(getString(R.string.unpaid));
                    b.tvPaid.setTextColor(getResources().getColor(R.color.red));
                    b.tvPaid.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.red));

                }

                try {

                    arrayList.clear();

                    JSONArray jsonArray = new JSONArray(jsonObject.getString("products"));
                    if (jsonObject.getString("orderType").equalsIgnoreCase("1")) {
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("name", jsonObject1.getString("name"));
                            hashMap.put("quantity", jsonObject1.getString("quantity"));
                            hashMap.put("priceWithQuantity", jsonObject1.getString("priceWithQuantity"));

                            arrayList.add(hashMap);

                        }
                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("name", jsonObject1.getString("productName"));
                            hashMap.put("quantity", jsonObject1.getString("quantity"));
                            hashMap.put("priceWithQuantity", jsonObject1.getString("productAmount"));

                            arrayList.add(hashMap);

                        }
                    }


                    //Preparing
                    if (jsonObject.getString("orderState").equals("1")) {
                        b.tvOrderReady.setVisibility(View.VISIBLE);

                        b.fabTrack.setVisibility(View.GONE);
                    }

                    //Ready
                    else if (jsonObject.getString("orderState").equals("2")) {
                        b.tvOrderReady.setVisibility(View.GONE);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapterItem.notifyDataSetChanged();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.personalDetails),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvOrderReady:

                hitReadyApi();

                break;
            case R.id.rlTrack:
                startActivity(new Intent(mActivity, LiveTrackingActivity.class).putExtra("orderId", orderId));
                break;
            case R.id.rlSupport:
                AppUtils.makeCall(mActivity, "9876543210");
                break;
            case R.id.rlCallDriver:
                AppUtils.makeCall(mActivity, number);
                break;

            case R.id.ivQrCode:

                showQr();

                break;
            case R.id.tvRateCustomer:
                startActivity(new Intent(mActivity, RateUserActivity.class)
                        .putExtra("orderId",orderId)
                        .putExtra("customerName",customerName)
                        .putExtra("customerProfile",customerProfile)

                );

                break;


        }

    }

    private void showQr() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_qr);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();

        ImageView ivClose, ivQr;

        ivClose = bottomSheetDialog.findViewById(R.id.ivClose);
        ivQr = bottomSheetDialog.findViewById(R.id.ivQr);

        ivClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        generateQr(orderId, ivQr);

    }

    private void generateQr(String address, ImageView ivQr) {

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ivQr.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }


    private void hitReadyApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("orderId", getIntent().getStringExtra("orderId"));

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.readyOrder, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseReadyJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseReadyJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                onBackPressed();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
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
    private String extractTime(String dateTimeString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Handle the parse exception as needed
        }
    }
    @Override
    protected void onResume() {
        hitGetOrderDetailApi(orderId);
        super.onResume();
    }
}