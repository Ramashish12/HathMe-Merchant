package code.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.FragmentSettingsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.activity.BankListActivity;
import code.activity.CreateWithdrawalPinActivity;
import code.activity.EditProfileActivity;
import code.activity.KycActivity;
import code.basic.BusinessActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseFragment;

public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    FragmentSettingsBinding b;
    String status = "0";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentSettingsBinding.inflate(inflater, container, false);

        inits();

        return b.getRoot();
    }

    private void inits() {

        b.rlBusinessDetails.setOnClickListener(this);
        b.rlBankDetails.setOnClickListener(this);
        b.rlPersonalDetails.setOnClickListener(this);
        b.ivOnOff.setOnClickListener(this);
        b.rlHelpSupport.setOnClickListener(this);
        b.rlKyc.setOnClickListener(this);
        b.rlLogout.setOnClickListener(this);
        b.rlCreateUpdateWithdrawPin.setOnClickListener(this);

        if (AppSettings.getString(AppSettings.isWithdrawPinCreated).isEmpty()) {
            b.tvCreateUpdateWithdrawPin.setText(getString(R.string.createWithdrawalPin));
        } else {
            b.tvCreateUpdateWithdrawPin.setText(getString(R.string.changeWithdrawalPin));
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rlBusinessDetails:

                startActivity(new Intent(mActivity, BusinessActivity.class).putExtra("pageFrom", "1"));

                break;

            case R.id.rlBankDetails:

                startActivity(new Intent(mActivity, BankListActivity.class).putExtra("pageFrom", "1"));

                break;

            case R.id.rlPersonalDetails:

                startActivity(new Intent(mActivity, EditProfileActivity.class));

                break;
            case R.id.ivOnOff:
                hitOnOffApi(status);
                break;

            case R.id.rlKyc:

                startActivity(new Intent(mActivity, KycActivity.class));

                break;

            case R.id.rlHelpSupport:

                Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
                selectorIntent.setData(Uri.parse("mailto:"));

                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Support@hathme.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support));
                emailIntent.setSelector(selectorIntent);

                startActivity(Intent.createChooser(emailIntent, "Send email..."));

                break;

            case R.id.rlLogout:

                showLogoutAlert();

                break;
            case R.id.rlCreateUpdateWithdrawPin:
                startActivity(new Intent(mActivity, CreateWithdrawalPinActivity.class).putExtra("pageFrom", "1").putExtra("isWithdrawPinCreated", "2"));
                break;
        }
    }

    private void hitOnOffApi(String status) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("isNotification", status);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.putApi(mActivity, AppUrls.updateNotificationStatus, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOnOffJson(response);

            }

            @Override
            public void OnFail(String response) {
                //AppUtils.showToastSort(mActivity, response);
                Log.v("updateNotificationStatus", response);
            }
        });
    }

    private void parseOnOffJson(JSONObject response) {
        try {
            JSONObject jsonObject = new JSONObject(response.toString());
            if (jsonObject.getString("success").equals("1")) {
                hitGetNotificationStatusApi();

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString("resMsg"), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void hitGetNotificationStatusApi() {

        WebServices.getApi(mActivity, AppUrls.getNotificationStatus, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseNotificationStatusJson(response);

            }

            @Override
            public void OnFail(String response) {
               // AppUtils.showToastSort(mActivity, response);
                Log.v("getNotificationStatus", response);
            }
        });
    }

    private void parseNotificationStatusJson(JSONObject response) {

        try {
            // JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            JSONObject jsonObject = new JSONObject(response.toString());
            if (jsonObject.getString("success").equals("1")) {

                if (jsonObject.getString("notificationStatus").equals("1")) {
                    b.ivOnOff.setImageResource(R.drawable.ic_switch_on);
                    status = "0";
                } else {
                    b.ivOnOff.setImageResource(R.drawable.ic_switch_off);
                    status = "1";
                }


            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString("resMsg"), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showLogoutAlert() {

        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
        alertDialog.setTitle(getString(R.string.logout));
        alertDialog.setMessage(getString(R.string.areYouSureLogout));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    AppUtils.performLogout(mActivity);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    @Override
    public void onResume() {
        hitGetNotificationStatusApi();
        super.onResume();
    }
}
