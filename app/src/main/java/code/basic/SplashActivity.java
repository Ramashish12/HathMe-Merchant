package code.basic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;

import com.hathme.merchat.android.R;

import code.activity.CategoryActivity;
import code.activity.MainActivity;
import code.utils.AppSettings;
import code.view.BaseActivity;

public class SplashActivity extends BaseActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);



        if (!AppSettings.getString(AppSettings.userId).isEmpty()) {

            if (AppSettings.getString(AppSettings.isProfileCompleted).equals("0")) {
                startActivity(new Intent(mActivity, SignUpActivity.class));
            } else if (AppSettings.getString(AppSettings.isCategorySelected).equals("0")) {
                startActivity(new Intent(mActivity, CategoryActivity.class));

            } else if (AppSettings.getString(AppSettings.isBusinessSave).equals("0")) {
                startActivity(new Intent(mActivity, BusinessActivity.class));

            } else {

                startActivity(new Intent(mActivity, MainActivity.class));
            }

            finishAffinity();
        } else {

            new Handler(Looper.myLooper()).postDelayed(() -> {
                startActivity(new Intent(mActivity, LoginTypeActivity.class));

                finishAffinity();

            }, 1000);
        }

    }

    @Override
    public void onClick(View view) {

    }
}