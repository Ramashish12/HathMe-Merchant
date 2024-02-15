package code.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityCategoryBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.basic.BusinessActivity;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class CategoryActivity extends BaseActivity implements View.OnClickListener {

    ActivityCategoryBinding b;

    Adapter adapter;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetCategoryApi();

    }

    private void hitGetCategoryApi() {

        WebServices.getApi(mActivity, AppUrls.category, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCategory(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });

    }

    private void parseCategory(JSONObject response) {

        arrayList.clear();


        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("image", jsonObject1.getString("image"));


                    arrayList.add(hashMap);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_category, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvName.setText(data.get(position).get("name"));

            AppUtils.loadPicassoImage(data.get(position).get("image"), holder.ivImage);

            holder.llMain.setOnClickListener(view -> showItOnDialog(data.get(position).get("image"),
                    data.get(position).get("name"), data.get(position).get("_id")));

        }

        @Override
        public int getItemCount() {
            return data.size();


        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout llMain;

            ImageView ivImage;
            TextView tvName;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                llMain = itemView.findViewById(R.id.llMain);

                ivImage = itemView.findViewById(R.id.ivImage);
                tvName = itemView.findViewById(R.id.tvName);

                llMain.setBackgroundResource(R.drawable.btn_rectangular_white);
                llMain.setElevation(5.0f);

            }
        }
    }

    private void showItOnDialog(String image, String name, String id) {

        final Dialog dialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar);
        dialog.setContentView(R.layout.dialog_full_category);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        ImageView ivImage = dialog.findViewById(R.id.ivImage);
        TextView tvName = dialog.findViewById(R.id.tvName);
        TextView tvGo = dialog.findViewById(R.id.tvGo);

        tvName.setText(name);
        AppUtils.loadPicassoImage(image, ivImage);

        tvGo.setOnClickListener(view -> {
            startActivity(new Intent(mActivity, BusinessActivity.class).putExtra("categoryId",id));
            finish();
//            hitSaveCategoryApi(id);
        });

    }

    private void hitSaveCategoryApi(String id) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("categoryId", id);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.SelectCategory, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response,id);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJson(JSONObject response, String id) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

//                AppSettings.putString(AppSettings.isCategorySelected,"1");
                startActivity(new Intent(mActivity, BusinessActivity.class).putExtra("categoryId",id));
                finish();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}