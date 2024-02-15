package code.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityFeedbackBinding;
import com.hathme.merchat.android.databinding.ActivityOrderHistoryBinding;

import code.view.BaseActivity;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener {

    ActivityFeedbackBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

    }

    @Override
    public void onClick(View view) {

    }
}