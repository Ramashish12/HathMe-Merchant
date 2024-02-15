package code.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityBankBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class BankActivity extends BaseActivity implements View.OnClickListener {

    ActivityBankBinding b;
    int currentLength, minLength, maxLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityBankBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeading.setText(getString(R.string.bankDetails));





        b.etAccountNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check the length when the text is changed
                minLength = 9; // Minimum length you want to check for
                maxLength = 17; // Maximum length you want to check for

                currentLength = charSequence.length();

                if (currentLength < minLength) {
                    // Display an error message for insufficient length
                    b.etAccountNumber.setError(getString(R.string.accountNumberErrorMin));
                } else if (currentLength > maxLength) {
                    // Display an error message for exceeding length
                    b.etAccountNumber.setError(getString(R.string.accountNumberErrorMax));
                } else {
                    // Clear any previous error message
                    b.etAccountNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });

        b.tvSave.setOnClickListener(this);

        hitGetBankDetailApi();
    }

    private void hitGetBankDetailApi() {

        WebServices.getApi(mActivity, AppUrls.getBankDetails, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetBank(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseGetBank(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");

                b.etName.setText(jsonData.getString("name"));
                b.etBankName.setText(jsonData.getString("bankName"));
                b.etAccountNumber.setText(jsonData.getString("accountNumber"));
                b.etConfirmAccountNumber.setText(jsonData.getString("accountNumber"));
                b.etIfscCode.setText(jsonData.getString("ifsc"));
//                b.etBranch.setText(jsonData.getString("branch"));

                if (jsonData.getString("accountType").equals("1"))
                    b.spinnerAccount.setSelection(0);
                else
                    b.spinnerAccount.setSelection(1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvSave:

                validate();

                break;

        }

    }

    private void validate() {

        if (b.etName.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterName));
        } else if (b.etBankName.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterBankName));
        } else if (b.etAccountNumber.getText().toString().trim().isEmpty()) {
            b.etAccountNumber.setError(getString(R.string.accountNumberEmptyError));
        } else if (!b.etAccountNumber.getText().toString().trim().equals(b.etConfirmAccountNumber.getText().toString().trim())) {
            AppUtils.showToastSort(mActivity, getString(R.string.confirmAccountNotMatch));
        } else if (currentLength < minLength) {
            AppUtils.showToastSort(mActivity, getString(R.string.EnterValidAccountNumber));
        } else if (b.etIfscCode.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterIfsc));
        } else if (b.etIfscCode.getText().toString().trim().length() < 11) {
            AppUtils.showToastSort(mActivity, getString(R.string.EnterValidIfsc));
        }
        /*else if (b.etBranch.getText().toString().trim().isEmpty()){
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterBranchName));
        }*/
        else {
            hitSaveBankApi();
        }

    }

    private void hitSaveBankApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("name", b.etName.getText().toString().trim());
            jsonObject.put("bankName", b.etBankName.getText().toString().trim());
            jsonObject.put("accountNumber", b.etAccountNumber.getText().toString().trim());
            jsonObject.put("ifsc", b.etIfscCode.getText().toString().trim());
//            jsonObject.put("branch", b.etBranch.getText().toString().trim());
            jsonObject.put("accountType", String.valueOf(b.spinnerAccount.getSelectedItemPosition() + 1));

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.bankDetails, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSaveBankJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseSaveBankJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 1);

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}