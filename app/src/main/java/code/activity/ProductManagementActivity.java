package code.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityProductManagementBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.adapter.AdapterSpinnerHashMap;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class ProductManagementActivity extends BaseActivity implements View.OnClickListener {

    ActivityProductManagementBinding b;

    ArrayList<HashMap<String, String>> arrayListCategory = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListSubCategory = new ArrayList<>();

    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProductManagementBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.fabAdd.setOnClickListener(this);

        b.header.ivBack.setOnClickListener(view -> onBackPressed());

        b.header.tvHeading.setText(getString(R.string.subCategory));

        adapter = new Adapter(arrayListSubCategory);
        b.rvList.setAdapter(adapter);

        hitGetAllSubCategoryApi();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fabAdd:

                if (arrayListCategory.size() == 0)
                    hitGetCategoryApi();
                else
                    showAddSubCategoryDialog(null);

                break;

        }

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

        arrayListCategory.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("name"));


                    arrayListCategory.add(hashMap);
                }

                showAddSubCategoryDialog(null);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void showAddSubCategoryDialog(HashMap<String, String> hashMap) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_add_sub_category);
        bottomSheetDialog.show();

        bottomSheetDialog.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        Spinner spinnerCategory = bottomSheetDialog.findViewById(R.id.spinnerCategory);

        EditText etSubCategory = bottomSheetDialog.findViewById(R.id.etSubCategory);

        TextView tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        spinnerCategory.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayListCategory));


        if (hashMap != null) {

            etSubCategory.setText(hashMap.get("subCategoryName"));

            for (int i = 0; i < arrayListCategory.size(); i++) {

                if (hashMap.get("categoryId").equals(arrayListCategory.get(i).get("_id"))) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }

        }

        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialog.dismiss();

                if (hashMap == null) {
                    hitAddSubCategoryApi(etSubCategory.getText().toString().trim(),
                            arrayListCategory.get(spinnerCategory.getSelectedItemPosition()).get("_id"));
                } else {
                    hitEditSubCategoryApi(etSubCategory.getText().toString().trim(),
                            arrayListCategory.get(spinnerCategory.getSelectedItemPosition()).get("_id"),
                            hashMap.get("subCategoryId"));
                }

            }
        });
    }

    private void hitEditSubCategoryApi(String subCategory, String id, String subCategoryId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("name", subCategory);
            jsonObject.put("categoryId", id);
            jsonObject.put("subCategoryId", subCategoryId);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.patchApi(mActivity, AppUrls.editSubCategory, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSubCategory(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void hitAddSubCategoryApi(String subCategory, String id) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("name", subCategory);
            jsonObject.put("categoryId", id);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.addSubCategory, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSubCategory(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseSubCategory(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetAllSubCategoryApi();
            }
            else
                AppUtils.showMessageDialog(mActivity, getString(R.string.subCategory),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitGetAllSubCategoryApi() {

        WebServices.getApi(mActivity, AppUrls.listSubCategories, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAllSubCategory(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseAllSubCategory(JSONObject response) {

        arrayListSubCategory.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("subCategoryId", jsonObject1.getString("subCategoryId"));
                    hashMap.put("subCategoryName", jsonObject1.getString("subCategoryName"));
                    hashMap.put("categoryName", jsonObject1.getString("categoryName"));
                    hashMap.put("categoryId", jsonObject1.getString("categoryId"));
                    hashMap.put("status", jsonObject1.getString("status"));

                    arrayListSubCategory.add(hashMap);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_sub_category, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvSubCategory.setText(data.get(position).get("subCategoryName"));
            holder.tvCategory.setText(data.get(position).get("categoryName"));

            if (data.get(position).get("status").equals("1")){

                holder.llEditDelete.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(getString(R.string.pending));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.golden));
            }
            else if (data.get(position).get("status").equals("2")){
                holder.llEditDelete.setVisibility(View.GONE);
                holder.tvStatus.setText(getString(R.string.approved));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.green));
            }
            else {
                holder.llEditDelete.setVisibility(View.VISIBLE);
                holder.tvStatus.setText(getString(R.string.rejected));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.red));
            }

            holder.tvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (arrayListCategory.size() == 0)
                        hitGetCategoryApi();
                    else
                        showAddSubCategoryDialog(data.get(position));


                }
            });

            holder.tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    hitDeleteSubcategoryApi(data.get(position).get("subCategoryId"));
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvSubCategory, tvCategory, tvEdit, tvDelete, tvStatus;

            LinearLayout llEditDelete;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvSubCategory = itemView.findViewById(R.id.tvSubCategory);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvEdit = itemView.findViewById(R.id.tvEdit);
                tvDelete = itemView.findViewById(R.id.tvDelete);
                tvStatus = itemView.findViewById(R.id.tvStatus);

                llEditDelete = itemView.findViewById(R.id.llEditDelete);
            }
        }
    }

    private void hitDeleteSubcategoryApi(String subCategoryId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("subCategoryId", subCategoryId);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.deleteApi(mActivity, AppUrls.deleteSubCategory, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseDeleteCategory(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseDeleteCategory(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

               hitGetAllSubCategoryApi();
            }
            else
                AppUtils.showMessageDialog(mActivity, getString(R.string.subCategory),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}