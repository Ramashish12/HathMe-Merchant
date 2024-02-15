package code.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityStoreProfileBinding;

import code.utils.AppSettings;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class StoreProfileActivity extends BaseActivity implements View.OnClickListener {

    ActivityStoreProfileBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityStoreProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.tvViewActivity.setOnClickListener(this);
        b.tvLogout.setOnClickListener(this);
        b.rlPaymentModes.setOnClickListener(this);
        b.rlProductManagement.setOnClickListener(this);
        b.rlTrackOrder.setOnClickListener(this);

        findViewById(R.id.ivBack).setOnClickListener(view -> onBackPressed());

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.tvViewActivity:

                startActivity(new Intent(mActivity, StoreViewActivity.class));

                break;
            case R.id.tvLogout:

                showLogoutAlert();

                break;
            case R.id.rlPaymentModes:

                startActivity(new Intent(mActivity, PaymentModesActivity.class));

                break;

            case R.id.rlTrackOrder:

                startActivity(new Intent(mActivity, TrackOrderDetailActivity.class));

                break;

            case R.id.rlProductManagement:

                startActivity(new Intent(mActivity, ProductManagementActivity.class));

                break;
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
    protected void onResume() {
        super.onResume();


        b.tvStoreName.setText(AppSettings.getString(AppSettings.name));

        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage), b.ivProfile);
    }
}