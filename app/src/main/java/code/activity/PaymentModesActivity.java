package code.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityPaymentModesBinding;

import code.view.BaseActivity;

public class PaymentModesActivity extends BaseActivity implements View.OnClickListener {

    ActivityPaymentModesBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPaymentModesBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.rlBankAccount.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.rlBankAccount:

                startActivity(new Intent(mActivity, BankActivity.class));

                break;

        }

    }
}