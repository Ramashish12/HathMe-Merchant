package code.basic;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityRegisterBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.activity.MainActivity;
import code.common.AdapterSpinnerHashMap;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    ActivityRegisterBinding b;

    ArrayList<HashMap<String, String>> arrayListCountry = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.tvGetStarted.setOnClickListener(this);

        hitGetCountryApi();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvGetStarted:

                validate();

                break;

        }

    }

    private void validate() {

        if (b.etName.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterName));
        } else if (!AppUtils.isValidMobileNo(b.etMobile.getText().toString())) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterMobile));
        } else if (!AppUtils.isEmailValid(b.etEmailId.getText().toString())) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectEmail));
        } else if (b.etPassword.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPassword));
        } else if (!b.etPassword.getText().toString().trim().equals(b.etConfirmPassword.getText().toString().trim())) {
            AppUtils.showToastSort(mActivity, getString(R.string.confirmPasswordNotMatch));
        } else {
            hitSignUpApi();
        }

    }

    private void hitSignUpApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("name", b.etName.getText().toString().trim());
            jsonObject.put("mobile", b.etMobile.getText().toString().trim());
            jsonObject.put("email", b.etEmailId.getText().toString().trim());
            jsonObject.put("password", b.etPassword.getText().toString().trim());
            jsonObject.put("referralCode", b.etReferralCode.getText().toString().trim());
            jsonObject.put("countryId", arrayListCountry.get(b.spinnerCountry.getSelectedItemPosition()).get("id"));
            jsonObject.put("fcmId", AppSettings.getString(AppSettings.fcmToken));
            jsonObject.put("manufacturer", Build.MANUFACTURER);
            jsonObject.put("deviceName", Build.MODEL);
            jsonObject.put("deviceVersion", Build.VERSION.RELEASE);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.register, json, true, true, new WebServicesCallback() {

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

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject data = jsonObject.getJSONObject("data");

                AppSettings.putString(AppSettings.isProfileCompleted, data.getString("isProfileCompleted"));
                AppSettings.putString(AppSettings.isMobileVerified, data.getString("isMobileVerified"));
                AppSettings.putString(AppSettings.token, data.getString("token"));

                if (AppSettings.getString(AppSettings.isMobileVerified).equals("0")) {
                    startActivity(new Intent(mActivity, OtpActivity.class));
                } else if (AppSettings.getString(AppSettings.isMobileVerified).equals("1") &&
                        AppSettings.getString(AppSettings.isProfileCompleted).equals("0")) {
                    startActivity(new Intent(mActivity, GeneratePinActivity.class));
                } else {
                    AppSettings.putString(AppSettings.name, data.getString("name"));
                    AppSettings.putString(AppSettings.mobile, data.getString("mobile"));
                    AppSettings.putString(AppSettings.email, data.getString("email"));
                    AppSettings.putString(AppSettings.userId, data.getString("merchantId"));
                    AppSettings.putString(AppSettings.isProfileCompleted, data.getString("isProfileCompleted"));
                    AppSettings.putString(AppSettings.isMobileVerified, data.getString("isMobileVerified"));
                    startActivity(new Intent(mActivity, MainActivity.class));
                }
                finishAffinity();
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.register),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
    private void hitGetCountryApi() {

        WebServices.getApi(mActivity, AppUrls.getCountryList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetCountry(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseGetCountry(JSONObject response) {

        arrayListCountry.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", jsonObject1.getString("countryId"));
                    hashMap.put("name", jsonObject1.getString("countryName"));

                    arrayListCountry.add(hashMap);
                }

            } else
                AppUtils.showResMsgToastSort(mActivity, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (arrayListCountry.size()==1)
        {
            b.spinnerCountry.setEnabled(false);
            b.spinnerCountry.setClickable(false);
            b.spinnerCountry.setFocusable(false);
        }
        else
        {
            b.spinnerCountry.setEnabled(true);
            b.spinnerCountry.setClickable(true);
            b.spinnerCountry.setFocusable(true);
        }
        b.spinnerCountry.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayListCountry));

    }
}