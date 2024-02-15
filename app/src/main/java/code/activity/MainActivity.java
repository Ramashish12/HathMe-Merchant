package code.activity;

import static code.common.MyApplication.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;


import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityMainBinding;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.android.user.User;


import code.call.CallService;
import code.chat.ChatListActivity;
import code.common.MyApplication;
import code.fragment.HomeFragment;
import code.fragment.OrdersFragment;
import code.fragment.ProductFragment;
import code.fragment.SettingsFragment;
import code.utils.AppSettings;
import code.utils.AppUtils;
import code.view.BaseActivity;
import code.view.BaseFragment;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    ActivityMainBinding b;

    private BaseFragment fragment;

    private FragmentManager fragmentManager;

    private boolean isHomeFragOpen=false;
    final int PERMISSION_REQUEST_CODE =112;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.llMenu.setOnClickListener(this);
        b.llSettings.setOnClickListener(this);
        b.llProduct.setOnClickListener(this);
        b.llOrders.setOnClickListener(this);
        b.llChat.setOnClickListener(this);
       // Log.v("tokenz",AppSettings.getString(AppSettings.token));
        displayView(getIntent().getIntExtra("displayView", 0));

        AppUtils.checkAppUpdate(mActivity);
        AppUtils.checkAndRequestPermissions(mActivity);
        loginForChat();
        MyApplication.getInstance().setUpVoip();
        if (Build.VERSION.SDK_INT > 32) {
            if (!shouldShowRequestPermissionRationale("112")){
                getNotificationPermission();
            }
        }
      //  startServiceViaWorker();
    }
    public void getNotificationPermission(){
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }catch (Exception e){

        }
    }
    public void displayView(int position) {

        isHomeFragOpen=false;

        fragmentManager = getSupportFragmentManager();

        switch (position) {

            case 0:

                isHomeFragOpen=true;
                setDefaultBottomBar();
                b.ivHome.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorPrimary));
                b.tvHome.setTextColor(getResources().getColor(R.color.colorPrimary));
                fragment = new HomeFragment();

                break;


            case 1:

                setDefaultBottomBar();
                b.ivOrders.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorPrimary));
                b.tvOrders.setTextColor(getResources().getColor(R.color.colorPrimary));
                fragment = new OrdersFragment();

                break;

            case 2:

                setDefaultBottomBar();
                b.ivProduct.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorPrimary));
                b.tvProduct.setTextColor(getResources().getColor(R.color.colorPrimary));
                fragment = new ProductFragment();

                break;


            case 3:

                setDefaultBottomBar();
                b.ivSettings.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorPrimary));
                b.tvSettings.setTextColor(getResources().getColor(R.color.colorPrimary));
                fragment = new SettingsFragment();

                break;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.flMain, fragment)
                .commit();

    }

    private void setDefaultBottomBar() {

        b.ivHome.setColorFilter(ContextCompat.getColor(mActivity, R.color.black));
        b.ivSettings.setColorFilter(ContextCompat.getColor(mActivity, R.color.black));
        b.ivProduct.setColorFilter(ContextCompat.getColor(mActivity, R.color.black));
        b.ivOrders.setColorFilter(ContextCompat.getColor(mActivity, R.color.black));
        b.ivChat.setColorFilter(ContextCompat.getColor(mActivity, R.color.black));

        b.tvHome.setTextColor(getResources().getColor(R.color.black));
        b.tvSettings.setTextColor(getResources().getColor(R.color.black));
        b.tvProduct.setTextColor(getResources().getColor(R.color.black));
        b.tvOrders.setTextColor(getResources().getColor(R.color.black));
        b.tvChat.setTextColor(getResources().getColor(R.color.black));

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.llMenu:

                displayView(0);

                break;

            case R.id.llOrders:

                displayView(1);

                break;

            case R.id.llSettings:

                displayView(3);

                break;

            case R.id.llProduct:

                displayView(2);

                break;

            case R.id.llChat:

               // AppUtils.showToastSort(mActivity, getString(R.string.coming_soon));
                startActivity(new Intent(mActivity, ChatListActivity.class));

                break;
        }
    }


    @Override
    public void onBackPressed() {

        AppSettings.putString(AppSettings.isUpdateImage,"disabled");

        if (isHomeFragOpen){
            showConfirmationDialog();
        }
        else{
            displayView(0);
        }

    }

    private void showConfirmationDialog() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        finishAffinity();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(getString(R.string.sureYouExit)).setPositiveButton(getString(R.string.exit), dialogClickListener)
                .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
    }

    @Override
    protected void onResume() {

        super.onResume();
        //Calling in Home Fragment Already

    }

    private void loginForChat() {


        SendbirdChat.connect(AppSettings.getString(AppSettings.userId), new ConnectHandler() {
            @Override
            public void onConnected(@Nullable User user, @Nullable SendbirdException e) {


            }
        });

    }

    public void stopService() {
        Log.d(TAG, "stopService called");
        if (!CallService.isServiceRunning) {
            Intent serviceIntent = new Intent(this, CallService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // allow

                }  else {
                    //deny
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                }

        }

    }
}