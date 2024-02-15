package code.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityCategoryBinding;
import com.hathme.merchat.android.databinding.ActivityTransactionHistoryBinding;

import java.util.ArrayList;
import java.util.HashMap;

import code.view.BaseActivity;

public class TransactionHistoryActivity extends BaseActivity {

    ActivityTransactionHistoryBinding b;


    Adapter adapter;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity,3));

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_transaction_history, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {



        }

        @Override
        public int getItemCount() {
            // return data.size();
            return 8;

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {



            public MyViewHolder(@NonNull View itemView) {
                super(itemView);



            }
        }
    }

}