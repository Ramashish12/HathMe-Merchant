package code.basic;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityLoginBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.common.AdapterSpinnerHashMap;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    ActivityLoginBinding b;
    ArrayList<HashMap<String, String>> arrayListCountry = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

      /*  b.ivBack.setOnClickListener(view -> onBackPressed());

        b.tvForgotPassword.setOnClickListener(this);*/
        b.tvContinue.setOnClickListener(this);
        b.tvTnc.setOnClickListener(this);
        b.tvPrivacyPolicy.setOnClickListener(this);
        // Apply input filter to exclude special characters

        hitGetCountryApi();

        AppUtils.checkAppUpdate(mActivity);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
          /*  case R.id.tvForgotPassword:

                startActivity(new Intent(mActivity, ForgotPasswordActivity.class));

                break;*/

            case R.id.tvContinue:

                validate();

                break;

            case R.id.tvPrivacyPolicy:

                AppUtils.openChromeCustomTabUrl(AppUrls.privacyPolicy, mActivity);

                break;
            case R.id.tvTnc:

                AppUtils.openChromeCustomTabUrl(AppUrls.tnc, mActivity);

                break;

        }

    }

    private void validate() {

        if (arrayListCountry.size() == 0) {
            hitGetCountryApi();
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectCountryCode));
        }
        if (b.etMobile.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectMobile));
        }
        else if (!AppUtils.isValidMobileNo(b.etMobile.getText().toString().trim())){
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectMobile));
        }
        else if (!b.cbTnc.isChecked()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseAcceptTnc));
        }
        else {
            hitLoginApi();
        }

    }

    private void hitLoginApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("mobile", b.etMobile.getText().toString().trim());
            jsonObject.put("countryId", arrayListCountry.get(b.spinnerCountry.getSelectedItemPosition()).get("id"));
            jsonObject.put("fcmId", AppSettings.getString(AppSettings.fcmToken));
            jsonObject.put("manufacturer", Build.MANUFACTURER);
            jsonObject.put("deviceName", Build.MODEL);
            jsonObject.put("deviceVersion", Build.VERSION.RELEASE);


            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.login, json, true, true, new WebServicesCallback() {

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

                startActivity(new Intent(mActivity, OtpActivity.class).putExtra("mobile", b.etMobile.getText().toString().trim()));

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.login),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    hashMap.put("name", jsonObject1.getString("countryCode"));

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

}