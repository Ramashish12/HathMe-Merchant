package code.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityCampaignsBinding;
import com.hathme.merchat.android.databinding.ActivityStoreProfileBinding;

import code.view.BaseActivity;

public class CampaignsActivity extends BaseActivity implements View.OnClickListener {

    ActivityCampaignsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCampaignsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {



    }

    @Override
    public void onClick(View view) {

    }
}