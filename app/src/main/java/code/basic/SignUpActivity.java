package code.basic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivitySignupBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.activity.CategoryActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class SignUpActivity extends BaseActivity implements View.OnClickListener {

    ActivitySignupBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.tvContinue.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {


            case R.id.tvContinue:

                validate();

                break;

        }

    }

    private void validate() {

        if (b.etName.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterName));
        } else if (!AppUtils.isEmailValid(b.etEmail.getText().toString().trim())) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectEmail));
        } else {
            hitSaveDataApi();
        }
    }

    private void hitSaveDataApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("email", b.etEmail.getText().toString().trim());
            jsonObject.put("name", b.etName.getText().toString().trim());

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.personalDetail, json, true, true, new WebServicesCallback() {

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

                AppSettings.putString(AppSettings.isProfileCompleted,"1");
                startActivity(new Intent(mActivity, CategoryActivity.class));
                finish();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.signUp),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
