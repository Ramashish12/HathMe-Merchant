package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityStoreViewBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class StoreViewActivity extends BaseActivity implements View.OnClickListener {

    ActivityStoreViewBinding b;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> arrayListImages = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityStoreViewBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.llInsight.setOnClickListener(this);
        b.llOrders.setOnClickListener(this);
        b.llProduct.setOnClickListener(this);
        b.llAccount.setOnClickListener(this);
        b.rlWithdraw.setOnClickListener(this);
        b.rlReviews.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.llInsight:

                startActivity(new Intent(mActivity, SalesInsightActivity.class));

                break;

            case R.id.llOrders:

                startActivity(new Intent(mActivity, MainActivity.class).
                        putExtra("displayView", 1));
                finish();

                break;

            case R.id.llProduct:

                startActivity(new Intent(mActivity, MainActivity.class).
                        putExtra("displayView", 2));
                finish();

                break;

            case R.id.llAccount:

                startActivity(new Intent(mActivity, MainActivity.class).
                        putExtra("displayView", 3));
                finish();

                break;
            case R.id.rlWithdraw:
                startActivity(new Intent(mActivity, WithdrawalAmountActivity.class));
                break;
            case R.id.rlReviews:
                startActivity(new Intent(mActivity, ReviewListActivity.class));
                break;


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

               /* if (status.equals("1")){
                    b.tvOpen.setBackgroundResource(R.drawable.btn_rectangular_color);
                    b.tvClosed.setBackgroundResource(0);

                    b.tvOpen.setTextColor(getResources().getColor(R.color.white));
                    b.tvClosed.setTextColor(getResources().getColor(R.color.textBlack));
                }
                else {
                    b.tvOpen.setBackgroundResource(0);
                    b.tvClosed.setBackgroundResource(R.drawable.btn_rectangular_color);

                    b.tvClosed.setTextColor(getResources().getColor(R.color.white));
                    b.tvOpen.setTextColor(getResources().getColor(R.color.textBlack));
                }*/

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.register),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        b.tvName.setText(AppSettings.getString(AppSettings.name));

        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage), b.ivProfile);

        hitGetMerchantDetailApi();
        hitGetUploadedImagesApi();
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

    @SuppressLint("SetTextI18n")
    private void parseGetDetail(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                jsonObject = jsonObject.getJSONObject("data");

                if (!jsonObject.getJSONObject("merchantDetailData").getString("coverPhoto").isEmpty()) {
                    AppUtils.loadPicassoImage(jsonObject.getJSONObject("merchantDetailData").getString("coverPhoto"), b.ivCoverPhoto);
                    b.ivCoverPhoto.setPadding(0, 0, 0, 0);
                    b.ivCoverPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }

                if (!jsonObject.getString("profileImage").isEmpty()) {
                    AppUtils.loadPicassoImage(jsonObject.getString("profileImage"), b.ivProfile);
                }
                b.tvName.setText(jsonObject.getString("name"));
                b.tvCategory.setText(jsonObject.getJSONObject("merchantDetailData").getString("categoryName"));
                b.tvCity.setText(jsonObject.getJSONObject("merchantDetailData").getString("address"));
                b.tvMobile.setText(jsonObject.getString("mobile"));
                b.tvBalance.setText(AppConstants.currency + " " + AppUtils.roundOff2Digit(jsonObject.getString("walletBalance")));
                b.tvCommissionRate.setText(getString(R.string.commissionRate) + " : " + AppUtils.ifEmptyReturn0(jsonObject.getString("commission"))+"%");

                b.tvTiming.setText(getString(R.string.businessHours) + " : " + jsonObject.getJSONObject("merchantDetailData")
                        .getString("startTime") + " - " + jsonObject.getJSONObject("merchantDetailData").getString("endTime"));
                hitGetReviewsApi();
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.register),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void hitGetReviewsApi() {

        WebServices.getApi(mActivity, AppUrls.ratingReview, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseReview(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseReview(JSONObject response) {
        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

               JSONObject jsonObj = jsonObject.getJSONObject("data");
                b.ratingBar.setRating(AppUtils.returnFloat(jsonObj.getString("totalRating")));
                JSONArray jsonArray = jsonObj.getJSONArray("resultData");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("remark", jsonObject1.getString("remark"));
                    arrayList.add(hashMap);
                }
                b.tvReview.setText(arrayList.size() + " " + getString(R.string.reviews));
                 } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitGetUploadedImagesApi() {


        WebServices.getApi(mActivity, AppUrls.GetBusinessImage, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetImages(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseGetImages(JSONObject response) {

        arrayListImages.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("imageUrl", jsonObject1.getString("imageUrl"));
                    arrayListImages.add(hashMap);
                }
                AppUtils.loadPicassoImage(arrayListImages.get(0).get("imageUrl"), b.ivCoverPhoto);

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}