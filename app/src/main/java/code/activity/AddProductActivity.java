package code.activity;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.BuildConfig;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityAddProductBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import code.adapter.AdapterSpinnerHashMap;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class AddProductActivity extends BaseActivity implements View.OnClickListener {

    ActivityAddProductBinding b;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private ProgressDialog progressDialog;
    private String picturePath = "", productId = "", subCategoryId = "", unitType = "", brandId = "", size = "", color = "";
    private String colorValue = "", brandValue = "", sizeValue = "";
    private static final int selectPicture = 1, capturePicture = 100;
    String messages = "";
    private Uri fileUri;

    ArrayList<HashMap<String, String>> arrayListImages = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListSubCategory = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListUnitType = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListBrand = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListSize = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListColor = new ArrayList<>();
    private AdapterImages adapterImages;

    private JSONObject jsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (checkAndRequestPermissions()) {

        } else {
            checkAndRequestPermissions();
        }
        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(this);
        b.header.tvHeading.setText(getString(R.string.addProduct));
        b.tvAddProduct.setOnClickListener(this);
        b.tvAddImage.setOnClickListener(this);
        b.etBestBefore.setOnClickListener(this);
        b.etFullName.setOnClickListener(this);
        b.etPrice.setOnClickListener(this);
        b.etSellingPrice.setOnClickListener(this);
        b.etOfferPrice.setOnClickListener(this);
        b.etSpecialOffer.setOnClickListener(this);
        //extra fields
        b.etPackedType.setOnClickListener(this);
        b.etExpiryDate.setOnClickListener(this);
        b.etBatchNumber.setOnClickListener(this);
        b.etDistributorName.setOnClickListener(this);
        b.etPurchaseInvoiceNo.setOnClickListener(this);
        b.etMaterialType.setOnClickListener(this);
        b.etAboutThisItem.setOnClickListener(this);
        b.etManufacturer.setOnClickListener(this);
        b.etLicenseNo.setOnClickListener(this);
        b.etDisclaimer.setOnClickListener(this);
        b.etShelfLife.setOnClickListener(this);
        b.etFssaiLicense.setOnClickListener(this);
        b.etCountryOfOrigin.setOnClickListener(this);
        b.etSellerName.setOnClickListener(this);
        b.etIngredients.setOnClickListener(this);
        b.etContent.setOnClickListener(this);
        b.etMrp.setOnClickListener(this);
        b.etPurchaseInvoiceDate.setOnClickListener(this);
        b.etPrepTime.setOnClickListener(this);

        adapterImages = new AdapterImages(arrayListImages);
        b.rvImages.setAdapter(adapterImages);
        AppUtils.checkAndRequestPermissions(mActivity);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        if (getIntent().getExtras() != null) {
            productId = getIntent().getStringExtra("productId");
            b.header.tvHeading.setText(getString(R.string.updateProduct));
            b.tvAddProduct.setText(getString(R.string.updateProduct));
        } else {
            b.header.tvHeading.setText(getString(R.string.addProduct));
            b.tvAddProduct.setText(getString(R.string.addProduct));

        }

        hitGetFormDataApi();

    }

    private void hitGetFormDataApi() {

        WebServices.getApi(mActivity, AppUrls.getproductModelStructure, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {
                parseFormData(response);
            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseFormData(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                jsonData = jsonObject.getJSONObject("data");
                if (jsonData.has("message")) {
                    jsonData.put("message", jsonData.getString("message"));
                } else {
                    jsonData.put("message", "");
                }
                if (jsonData.getString("message").equals("Category not persent")) {
                    b.svMain.setVisibility(View.GONE);
                    AppUtils.showMessageDialog(mActivity, getString(R.string.products), getString(R.string.modelNotPersent), 1);
                } else {
                    b.svMain.setVisibility(View.VISIBLE);
                }
                if (jsonData.getString("productName").equals("1")) {
                    if (jsonData.getString("productRequire").equalsIgnoreCase("1")) {
                        b.tvFullName.setText(getString(R.string.productNameRequire));
                    } else {
                        b.tvFullName.setText(getString(R.string.productName));
                    }
                    if (jsonData.getString("productNameType").equals("1")) {
                        b.etFullName.setInputType(setInputType("1"));
                        b.etFullName.setFilters(new InputFilter[]{new AppUtils.SpecialCharacterFilter()});
                        b.etFullName.setFocusable(true);
                        b.etFullName.setEnabled(true);
                    } else if (jsonData.getString("productNameType").equals("2")) {

                        b.etFullName.setInputType(setInputType("2"));
                        b.etFullName.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etFullName.setFocusable(true);
                        b.etFullName.setEnabled(true);
                    } else if (jsonData.getString("productNameType").equals("3")) {
                        b.etFullName.setFocusable(false);
                        b.etFullName.setClickable(true);
                        b.etFullName.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("productNameType").equals("4")) {
                        b.etFullName.setFocusable(false);
                        b.etFullName.setHint(getString(R.string.enterTime));
                        b.etFullName.setClickable(true);
                    }
                    b.tvFullName.setVisibility(View.VISIBLE);
                    b.etFullName.setVisibility(View.VISIBLE);
                } else {
                    b.tvFullName.setVisibility(View.GONE);
                    b.etFullName.setVisibility(View.GONE);
                }
                if (jsonData.getString("productPrice").equals("1")) {
                    if (jsonData.getString("priceRequire").equalsIgnoreCase("1")) {
                        b.tvPrice.setText(getString(R.string.priceRequire));
                    } else {
                        b.tvPrice.setText(getString(R.string.price));
                    }
                    if (jsonData.getString("productPriceType").equalsIgnoreCase("1")) {
                        b.etPrice.setRawInputType(setInputType("1"));
                        b.etPrice.setFocusable(true);
                        b.etPrice.setEnabled(true);
                    } else if (jsonData.getString("productPriceType").equalsIgnoreCase("2")) {
                        b.etPrice.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etPrice);
                        b.etPrice.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etPrice.setFocusable(true);
                        b.etPrice.setEnabled(true);
                    } else if (jsonData.getString("productPriceType").equalsIgnoreCase("3")) {
                        b.etPrice.setFocusable(false);
                        b.etPrice.setClickable(true);
                        b.etPrice.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("productPriceType").equalsIgnoreCase("4")) {
                        b.etPrice.setFocusable(false);
                        b.etPrice.setClickable(true);
                        b.etPrice.setHint(getString(R.string.enterTime));
                    }
                    b.tvPrice.setVisibility(View.VISIBLE);
                    b.etPrice.setVisibility(View.VISIBLE);
                } else {
                    b.tvPrice.setVisibility(View.GONE);
                    b.etPrice.setVisibility(View.GONE);
                }

                if (jsonData.getString("Mrp").equals("1")) {
                    if (jsonData.getString("MrpRequire").equalsIgnoreCase("1")) {
                        b.tvMrp.setText(getString(R.string.mrpRequire));
                    } else {
                        b.tvMrp.setText(getString(R.string.mrp));
                    }
                    if (jsonData.getString("MrpType").equalsIgnoreCase("1")) {
                        b.etMrp.setRawInputType(setInputType("1"));
                        b.etMrp.setFocusable(true);
                        b.etMrp.setEnabled(true);
                    } else if (jsonData.getString("MrpType").equalsIgnoreCase("2")) {
                        b.etMrp.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etMrp);
                        b.etMrp.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etMrp.setFocusable(true);
                        b.etMrp.setEnabled(true);
                    } else if (jsonData.getString("MrpType").equalsIgnoreCase("3")) {
                        b.etMrp.setFocusable(false);
                        b.etMrp.setClickable(true);
                        b.etMrp.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("MrpType").equalsIgnoreCase("4")) {
                        b.etMrp.setFocusable(false);
                        b.etMrp.setClickable(true);
                        b.etMrp.setHint(getString(R.string.enterTime));
                    }
                    b.tvMrp.setVisibility(View.VISIBLE);
                    b.etMrp.setVisibility(View.VISIBLE);
                } else {
                    b.tvMrp.setVisibility(View.GONE);
                    b.etMrp.setVisibility(View.GONE);
                }

                if (jsonData.getString("sellingPrice").equals("1")) {
                    if (jsonData.getString("sellingPriceRequire").equalsIgnoreCase("1")) {
                        b.tvSellingPrice.setText(getString(R.string.sellingPriceRequire));
                    } else {
                        b.tvSellingPrice.setText(getString(R.string.sellingPrice));
                    }
                    if (jsonData.getString("sellingPriceType").equalsIgnoreCase("1")) {
                        b.etSellingPrice.setRawInputType(setInputType("1"));
                        b.etSellingPrice.setFocusable(true);
                        b.etSellingPrice.setEnabled(true);
                    } else if (jsonData.getString("sellingPriceType").equalsIgnoreCase("2")) {
                        b.etSellingPrice.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etSellingPrice);
                        b.etSellingPrice.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etSellingPrice.setFocusable(true);
                        b.etSellingPrice.setEnabled(true);
                    } else if (jsonData.getString("sellingPriceType").equalsIgnoreCase("3")) {
                        b.etSellingPrice.setFocusable(false);
                        b.etSellingPrice.setClickable(true);
                        b.etSellingPrice.setHint(getString(R.string.enterDate));

                    } else if (jsonData.getString("sellingPriceType").equalsIgnoreCase("4")) {
                        b.etSellingPrice.setFocusable(false);
                        b.etSellingPrice.setClickable(true);
                        b.etSellingPrice.setHint(getString(R.string.enterTime));
                    }
                    b.tvSellingPrice.setVisibility(View.VISIBLE);
                    b.etSellingPrice.setVisibility(View.VISIBLE);
                } else {
                    b.tvSellingPrice.setVisibility(View.GONE);
                    b.etSellingPrice.setVisibility(View.GONE);
                }
                if (jsonData.getString("offerPrice").equals("1")) {
                    if (jsonData.getString("offerRequire").equalsIgnoreCase("1")) {
                        b.tvOfferPrice.setText(getString(R.string.discountRequired));
                    } else {
                        b.tvOfferPrice.setText(getString(R.string.discount));
                    }
                    if (jsonData.getString("offerPriceType").equalsIgnoreCase("1")) {
                        b.etOfferPrice.setRawInputType(setInputType("1"));
                        b.etOfferPrice.setFocusable(true);
                        b.etOfferPrice.setEnabled(true);
                    } else if (jsonData.getString("offerPriceType").equalsIgnoreCase("2")) {
                        b.etOfferPrice.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etOfferPrice);
                        b.etOfferPrice.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etOfferPrice.setFocusable(true);
                        b.etOfferPrice.setEnabled(true);
                    } else if (jsonData.getString("offerPriceType").equalsIgnoreCase("3")) {
                        b.etOfferPrice.setFocusable(false);
                        b.etOfferPrice.setClickable(true);
                        b.etOfferPrice.setHint(getString(R.string.enterDate));

                    } else if (jsonData.getString("offerPriceType").equalsIgnoreCase("4")) {
                        b.etOfferPrice.setFocusable(false);
                        b.etOfferPrice.setClickable(true);
                        b.etOfferPrice.setHint(getString(R.string.enterTime));
                    }

                    b.tvOfferPrice.setVisibility(View.VISIBLE);
                    b.etOfferPrice.setVisibility(View.VISIBLE);
                } else {
                    b.tvOfferPrice.setVisibility(View.GONE);
                    b.etOfferPrice.setVisibility(View.GONE);
                }
                if (jsonData.getString("specialFeature").equals("1")) {
                    if (jsonData.getString("specialFeatureRequire").equalsIgnoreCase("1")) {
                        b.tvSpecialOffer.setText(getString(R.string.specialOfferRequire));
                    } else {
                        b.tvSpecialOffer.setText(getString(R.string.specialOffer));
                    }
                    if (jsonData.getString("specialFeatureType").equalsIgnoreCase("1")) {
                        b.etSpecialOffer.setRawInputType(setInputType("1"));
                        b.etSpecialOffer.setFocusable(true);
                        b.etSpecialOffer.setEnabled(true);
                    } else if (jsonData.getString("specialFeatureType").equalsIgnoreCase("2")) {
                        b.etSpecialOffer.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etSpecialOffer);
                        b.etSpecialOffer.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etSpecialOffer.setFocusable(true);
                        b.etSpecialOffer.setEnabled(true);
                    } else if (jsonData.getString("specialFeatureType").equalsIgnoreCase("3")) {
                        b.etSpecialOffer.setFocusable(false);
                        b.etSpecialOffer.setClickable(true);
                        b.etSpecialOffer.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("specialFeatureType").equalsIgnoreCase("4")) {
                        b.etSpecialOffer.setFocusable(false);
                        b.etSpecialOffer.setClickable(true);
                        b.etSpecialOffer.setHint(getString(R.string.enterTime));
                    }

                    b.tvSpecialOffer.setVisibility(View.VISIBLE);
                    b.etSpecialOffer.setVisibility(View.VISIBLE);
                } else {
                    b.tvSpecialOffer.setVisibility(View.GONE);
                    b.etSpecialOffer.setVisibility(View.GONE);
                }
                if (jsonData.getString("description").equals("1")) {
                    if (jsonData.getString("descriptionRequire").equalsIgnoreCase("1")) {
                        b.tvDescription.setText(getString(R.string.productDescriptionRequire));
                    } else {
                        b.tvDescription.setText(getString(R.string.productDescription));
                    }
                    b.tvDescription.setVisibility(View.VISIBLE);
                    b.etDescription.setVisibility(View.VISIBLE);

                    if (jsonData.getString("descriptionType").equalsIgnoreCase("1")) {
                        b.etDescription.setRawInputType(setInputType("1"));
                        b.etDescription.setFocusable(true);
                        b.etDescription.setEnabled(true);
                    } else if (jsonData.getString("descriptionType").equalsIgnoreCase("2")) {
                        b.etDescription.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etDescription);
                        b.etDescription.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etDescription.setFocusable(true);
                        b.etDescription.setEnabled(true);
                    } else if (jsonData.getString("descriptionType").equalsIgnoreCase("3")) {
                        b.etDescription.setFocusable(false);
                        b.etDescription.setClickable(true);
                        b.etDescription.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("descriptionType").equalsIgnoreCase("4")) {
                        b.etDescription.setFocusable(false);
                        b.etDescription.setClickable(true);
                        b.etDescription.setHint(getString(R.string.enterTime));
                    }

                } else {
                    b.tvDescription.setVisibility(View.GONE);
                    b.etDescription.setVisibility(View.GONE);
                }

                colorValue = jsonData.getString("brand");
                brandValue = jsonData.getString("size");
                sizeValue = jsonData.getString("color");
                if (jsonData.getString("brand").equals("1")) {
                    if (jsonData.getString("brandRequire").equalsIgnoreCase("1")) {
                        b.tvBrand.setText(getString(R.string.brandRequire));
                    } else {
                        b.tvBrand.setText(getString(R.string.brand));
                    }
                    b.tvBrand.setVisibility(View.VISIBLE);
                    b.spinnerBrand.setVisibility(View.VISIBLE);
                    b.rlspinnerBrand.setVisibility(View.VISIBLE);
                    hitGetDropDownData(1);

                } else {
                    b.tvBrand.setVisibility(View.GONE);
                    b.spinnerBrand.setVisibility(View.GONE);
                    b.rlspinnerBrand.setVisibility(View.GONE);
                }
                if (jsonData.getString("size").equals("1")) {
                    if (jsonData.getString("sizeRequire").equalsIgnoreCase("1")) {
                        b.tvSize.setText(getString(R.string.sizeRequire));
                    } else {
                        b.tvSize.setText(getString(R.string.size));
                    }
                    b.tvSize.setVisibility(View.VISIBLE);
                    b.spinnerSize.setVisibility(View.VISIBLE);
                    b.rlspinnerSize.setVisibility(View.VISIBLE);
                    hitGetDropDownData(2);

                } else {
                    b.tvSize.setVisibility(View.GONE);
                    b.spinnerSize.setVisibility(View.GONE);
                    b.rlspinnerSize.setVisibility(View.GONE);
                }
                if (jsonData.getString("color").equals("1")) {

                    if (jsonData.getString("colorRequire").equalsIgnoreCase("1")) {
                        b.tvColor.setText(getString(R.string.colorRequire));
                    } else {
                        b.tvColor.setText(getString(R.string.color));
                    }
                    b.tvColor.setVisibility(View.VISIBLE);
                    b.spinnerColor.setVisibility(View.VISIBLE);
                    b.rlspinnerColor.setVisibility(View.VISIBLE);
                    hitGetDropDownData(3);

                } else {
                    b.tvColor.setVisibility(View.GONE);
                    b.spinnerColor.setVisibility(View.GONE);
                    b.rlspinnerColor.setVisibility(View.GONE);
                }


                if (jsonData.getString("subcategory").equals("1")) {
                    if (jsonData.getString("subcategoryRequire").equals("1")) {
                        b.tvSubCategory.setText(getString(R.string.subCategoryRequire));
                    } else {
                        b.tvSubCategory.setText(getString(R.string.subCategory));
                    }
                    b.tvSubCategory.setVisibility(View.VISIBLE);
                    b.spinnerSubCategory.setVisibility(View.VISIBLE);
                    hitGetSubCategoryApi();

                } else {
                    b.tvSubCategory.setVisibility(View.GONE);
                    b.spinnerSubCategory.setVisibility(View.GONE);
                }
                if (jsonData.getString("bestBeforeDate").equals("1")) {
                    if (jsonData.getString("bestBeforeDateRequire").equalsIgnoreCase("1")) {
                        b.tvBestBeforeTitle.setText(getString(R.string.bestBeforeRequire));
                    } else {
                        b.tvBestBeforeTitle.setText(getString(R.string.bestBefore));
                    }
                    b.tvBestBeforeTitle.setVisibility(View.VISIBLE);
                    b.etBestBefore.setVisibility(View.VISIBLE);

                    if (jsonData.getString("bestBeforeDateType").equalsIgnoreCase("1")) {
                        b.etBestBefore.setRawInputType(setInputType("1"));
                        b.etBestBefore.setFocusable(true);
                        b.etBestBefore.setEnabled(true);
                    } else if (jsonData.getString("bestBeforeDateType").equalsIgnoreCase("2")) {
                        b.etBestBefore.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etBestBefore);
                        b.etBestBefore.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etBestBefore.setFocusable(true);
                        b.etBestBefore.setEnabled(true);
                    } else if (jsonData.getString("bestBeforeDateType").equalsIgnoreCase("3")) {
                        b.etBestBefore.setFocusable(false);
                        b.etBestBefore.setClickable(true);
                        b.etBestBefore.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("bestBeforeDateType").equalsIgnoreCase("4")) {
                        b.etBestBefore.setFocusable(false);
                        b.etBestBefore.setClickable(true);
                        b.etBestBefore.setHint(getString(R.string.enterTime));
                    }
                } else {
                    b.tvBestBeforeTitle.setVisibility(View.GONE);
                    b.etBestBefore.setVisibility(View.GONE);
                }
                if (jsonData.getString("quantity").equals("1")) {
                    if (jsonData.getString("quantityRequire").equalsIgnoreCase("1")) {
                        b.tvQuantity.setText(getString(R.string.quantityRequire));
                    } else {
                        b.tvQuantity.setText(getString(R.string.quantity));
                    }
                    b.tvQuantity.setVisibility(View.VISIBLE);
                    b.etQuantity.setVisibility(View.VISIBLE);

                    if (jsonData.getString("quantityType").equalsIgnoreCase("1")) {
                        b.etQuantity.setRawInputType(setInputType("1"));
                        b.etQuantity.setFocusable(true);
                        b.etQuantity.setEnabled(true);
                    } else if (jsonData.getString("quantityType").equalsIgnoreCase("2")) {
                        b.etQuantity.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etQuantity);
                        b.etQuantity.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etQuantity.setFocusable(true);
                        b.etQuantity.setEnabled(true);
                    } else if (jsonData.getString("quantityType").equalsIgnoreCase("3")) {
                        b.etQuantity.setFocusable(false);
                        b.etQuantity.setClickable(true);
                        b.etQuantity.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("quantityType").equalsIgnoreCase("4")) {
                        b.etQuantity.setFocusable(false);
                        b.etQuantity.setClickable(true);
                        b.etQuantity.setHint(getString(R.string.enterTime));
                    }
                } else {
                    b.tvQuantity.setVisibility(View.GONE);
                    b.etQuantity.setVisibility(View.GONE);
                }
                if (jsonData.getString("unit").equals("1")) {
                    if (jsonData.getString("unitRequire").equalsIgnoreCase("1")) {
                        b.tvUnit.setText(getString(R.string.unitRequire));
                    } else {
                        b.tvUnit.setText(getString(R.string.unit));
                    }
                    b.tvUnit.setVisibility(View.VISIBLE);
                    b.etUnit.setVisibility(View.VISIBLE);
                    b.tvUnitType.setVisibility(View.VISIBLE);
                    b.rlUnitType.setVisibility(View.VISIBLE);
                    hitGetUnitTypeApi();

                    if (jsonData.getString("unitType").equalsIgnoreCase("1")) {
                        b.etUnit.setRawInputType(setInputType("1"));
                        b.etUnit.setFocusable(true);
                        b.etUnit.setEnabled(true);
                    } else if (jsonData.getString("unitType").equalsIgnoreCase("2")) {
                        b.etUnit.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etUnit);
                        b.etUnit.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etUnit.setFocusable(true);
                        b.etUnit.setEnabled(true);
                    } else if (jsonData.getString("unitType").equalsIgnoreCase("3")) {
                        b.etUnit.setFocusable(false);
                        b.etUnit.setClickable(true);
                        b.etUnit.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("unitType").equalsIgnoreCase("4")) {
                        b.etUnit.setFocusable(false);
                        b.etUnit.setClickable(true);
                        b.etUnit.setHint(getString(R.string.enterTime));
                    }

                } else {
                    b.tvUnit.setVisibility(View.GONE);
                    b.etUnit.setVisibility(View.GONE);
                    b.tvUnitType.setVisibility(View.GONE);
                    b.rlUnitType.setVisibility(View.GONE);
                }


                //added extra fields
                if (jsonData.getString("packedType").equals("1")) {
                    if (jsonData.getString("packedTypeRequire").equalsIgnoreCase("1")) {
                        b.tvPackedType.setText(getString(R.string.packedTypeRequire));
                    } else {
                        b.tvPackedType.setText(getString(R.string.packedType));
                    }

                    if (jsonData.getString("packedTypeType").equalsIgnoreCase("1")) {
                        b.etPackedType.setRawInputType(setInputType("1"));
                        b.etPackedType.setFocusable(true);
                        b.etPackedType.setEnabled(true);
                    } else if (jsonData.getString("packedTypeType").equalsIgnoreCase("2")) {
                        b.etPackedType.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etPackedType);
                        b.etPackedType.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etPackedType.setFocusable(true);
                        b.etPackedType.setEnabled(true);
                    } else if (jsonData.getString("packedTypeType").equalsIgnoreCase("3")) {
                        b.etPackedType.setFocusable(false);
                        b.etPackedType.setClickable(true);
                        b.etPackedType.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("packedTypeType").equalsIgnoreCase("4")) {
                        b.etPackedType.setFocusable(false);
                        b.etPackedType.setClickable(true);
                        b.etPackedType.setHint(getString(R.string.enterTime));
                    }
                    b.tvPackedType.setVisibility(View.VISIBLE);
                    b.etPackedType.setVisibility(View.VISIBLE);

                } else {
                    b.tvPackedType.setVisibility(View.GONE);
                    b.etPackedType.setVisibility(View.GONE);
                }

                if (jsonData.getString("purchaseInvoiceDate").equals("1")) {
                    if (jsonData.getString("purchaseInvoiceDateRequire").equalsIgnoreCase("1")) {
                        b.tvPurchaseInvoiceDate.setText(getString(R.string.purchaseInvoiceDateRequire));
                    } else {
                        b.tvPurchaseInvoiceDate.setText(getString(R.string.purchaseInvoiceDate));
                    }

                    if (jsonData.getString("purchaseInvoiceDateType").equalsIgnoreCase("1")) {
                        b.etPurchaseInvoiceDate.setRawInputType(setInputType("1"));
                        b.etPurchaseInvoiceDate.setFocusable(true);
                        b.etPurchaseInvoiceDate.setEnabled(true);
                    } else if (jsonData.getString("purchaseInvoiceDateType").equalsIgnoreCase("2")) {
                        b.etPurchaseInvoiceDate.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etPurchaseInvoiceDate);
                        b.etPurchaseInvoiceDate.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etPurchaseInvoiceDate.setFocusable(true);
                        b.etPurchaseInvoiceDate.setEnabled(true);
                    } else if (jsonData.getString("purchaseInvoiceDateType").equalsIgnoreCase("3")) {
                        b.etPurchaseInvoiceDate.setFocusable(false);
                        b.etPurchaseInvoiceDate.setClickable(true);
                        b.etPurchaseInvoiceDate.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("purchaseInvoiceDateType").equalsIgnoreCase("4")) {
                        b.etPurchaseInvoiceDate.setFocusable(false);
                        b.etPurchaseInvoiceDate.setClickable(true);
                        b.etPurchaseInvoiceDate.setHint(getString(R.string.enterTime));
                    }
                    b.tvPurchaseInvoiceDate.setVisibility(View.VISIBLE);
                    b.etPurchaseInvoiceDate.setVisibility(View.VISIBLE);

                } else {
                    b.tvPurchaseInvoiceDate.setVisibility(View.GONE);
                    b.etPurchaseInvoiceDate.setVisibility(View.GONE);
                }

                if (jsonData.getString("expiryDate").equals("1")) {
                    if (jsonData.getString("expiryDateRequire").equalsIgnoreCase("1")) {
                        b.tvExpiryDate.setText(getString(R.string.expiryDateRequire));
                    } else {
                        b.tvExpiryDate.setText(getString(R.string.expiryDate));
                    }
                    b.tvExpiryDate.setVisibility(View.VISIBLE);
                    b.etExpiryDate.setVisibility(View.VISIBLE);

                    if (jsonData.getString("expiryDateType").equalsIgnoreCase("1")) {
                        b.etExpiryDate.setRawInputType(setInputType("1"));
                        b.etExpiryDate.setFocusable(true);
                        b.etExpiryDate.setEnabled(true);
                    } else if (jsonData.getString("expiryDateType").equalsIgnoreCase("2")) {
                        b.etExpiryDate.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etExpiryDate);
                        b.etExpiryDate.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etExpiryDate.setFocusable(true);
                        b.etExpiryDate.setEnabled(true);
                    } else if (jsonData.getString("expiryDateType").equalsIgnoreCase("3")) {
                        b.etExpiryDate.setFocusable(false);
                        b.etExpiryDate.setClickable(true);
                        b.etExpiryDate.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("expiryDateType").equalsIgnoreCase("4")) {
                        b.etExpiryDate.setFocusable(false);
                        b.etExpiryDate.setClickable(true);
                        b.etExpiryDate.setHint(getString(R.string.enterTime));
                    }
                } else {
                    b.tvExpiryDate.setVisibility(View.GONE);
                    b.etExpiryDate.setVisibility(View.GONE);
                }

                if (jsonData.getString("preparationTime").equals("1")) {
                    if (jsonData.getString("preparationTimeRequire").equalsIgnoreCase("1")) {
                        b.tvPrepTime.setText(getString(R.string.prepTimeRequire));
                    } else {
                        b.tvPrepTime.setText(getString(R.string.prepTime));
                    }
                    b.tvPrepTime.setVisibility(View.VISIBLE);
                    b.etPrepTime.setVisibility(View.VISIBLE);

                    if (jsonData.getString("preparationTimeType").equalsIgnoreCase("1")) {
                        b.etPrepTime.setRawInputType(setInputType("1"));
                        b.etPrepTime.setFocusable(true);
                        b.etPrepTime.setEnabled(true);
                    } else if (jsonData.getString("preparationTimeType").equalsIgnoreCase("2")) {
                        b.etPrepTime.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etPrepTime);
                        b.etPrepTime.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etPrepTime.setFocusable(true);
                        b.etPrepTime.setEnabled(true);
                    } else if (jsonData.getString("preparationTimeType").equalsIgnoreCase("3")) {
                        b.etPrepTime.setFocusable(false);
                        b.etPrepTime.setClickable(true);
                        b.etPrepTime.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("preparationTimeType").equalsIgnoreCase("4")) {
                        b.etPrepTime.setFocusable(false);
                        b.etPrepTime.setClickable(true);
                        b.etPrepTime.setHint(getString(R.string.enterTime));
                    }
                } else {
                    b.tvPrepTime.setVisibility(View.GONE);
                    b.etPrepTime.setVisibility(View.GONE);
                }


                if (jsonData.getString("batchNumber").equals("1")) {
                    if (jsonData.getString("batchNumberRequire").equalsIgnoreCase("1")) {
                        b.tvBatchNumber.setText(getString(R.string.batchNumberRequire));
                    } else {
                        b.tvBatchNumber.setText(getString(R.string.batchNumber));
                    }
                    if (jsonData.getString("batchNumberType").equalsIgnoreCase("1")) {
                        b.etBatchNumber.setRawInputType(setInputType("1"));
                        b.etBatchNumber.setFocusable(true);
                        b.etBatchNumber.setEnabled(true);
                    } else if (jsonData.getString("batchNumberType").equalsIgnoreCase("2")) {
                        b.etBatchNumber.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etBatchNumber);
                        b.etBatchNumber.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etBatchNumber.setFocusable(true);
                        b.etBatchNumber.setEnabled(true);
                    } else if (jsonData.getString("batchNumberType").equalsIgnoreCase("3")) {
                        b.etBatchNumber.setFocusable(false);
                        b.etBatchNumber.setClickable(true);
                        b.etBatchNumber.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("batchNumberType").equalsIgnoreCase("4")) {
                        b.etBatchNumber.setFocusable(false);
                        b.etBatchNumber.setClickable(true);
                        b.etBatchNumber.setHint(getString(R.string.enterTime));
                    }
                    b.tvBatchNumber.setVisibility(View.VISIBLE);
                    b.etBatchNumber.setVisibility(View.VISIBLE);
                } else {
                    b.tvBatchNumber.setVisibility(View.GONE);
                    b.etBatchNumber.setVisibility(View.GONE);
                }
                if (jsonData.getString("distributorName").equals("1")) {
                    if (jsonData.getString("distributorNameRequire").equalsIgnoreCase("1")) {
                        b.tvDistributorName.setText(getString(R.string.distributorNameRequire));
                    } else {
                        b.tvDistributorName.setText(getString(R.string.distributorName));
                    }
                    if (jsonData.getString("distributorNameType").equalsIgnoreCase("1")) {
                        b.etDistributorName.setRawInputType(setInputType("1"));
                        b.etDistributorName.setFocusable(true);
                        b.etDistributorName.setEnabled(true);
                    } else if (jsonData.getString("distributorNameType").equalsIgnoreCase("2")) {
                        b.etDistributorName.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etDistributorName);
                        b.etDistributorName.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etDistributorName.setFocusable(true);
                        b.etDistributorName.setEnabled(true);
                    } else if (jsonData.getString("distributorNameType").equalsIgnoreCase("3")) {
                        b.etDistributorName.setFocusable(false);
                        b.etDistributorName.setClickable(true);
                        b.etDistributorName.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("distributorNameType").equalsIgnoreCase("4")) {
                        b.etDistributorName.setFocusable(false);
                        b.etDistributorName.setClickable(true);
                        b.etDistributorName.setHint(getString(R.string.enterTime));
                    }

                    b.tvDistributorName.setVisibility(View.VISIBLE);
                    b.etDistributorName.setVisibility(View.VISIBLE);
                } else {
                    b.tvDistributorName.setVisibility(View.GONE);
                    b.etDistributorName.setVisibility(View.GONE);
                }
                if (jsonData.getString("purchaseInvoiceNo").equals("1")) {
                    if (jsonData.getString("purchaseInvoiceNoRequire").equalsIgnoreCase("1")) {
                        b.tvPurchaseInvoiceNo.setText(getString(R.string.purchaseInvoiceNoRequire));
                    } else {
                        b.tvPurchaseInvoiceNo.setText(getString(R.string.purchaseInvoiceNo));
                    }
                    if (jsonData.getString("purchaseInvoiceNoType").equalsIgnoreCase("1")) {
                        b.etPurchaseInvoiceNo.setRawInputType(setInputType("1"));
                        b.etPurchaseInvoiceNo.setFocusable(true);
                        b.etPurchaseInvoiceNo.setEnabled(true);
                    } else if (jsonData.getString("purchaseInvoiceNoType").equalsIgnoreCase("2")) {
                        b.etPurchaseInvoiceNo.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etPurchaseInvoiceNo);
                        b.etPurchaseInvoiceNo.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etPurchaseInvoiceNo.setFocusable(true);
                        b.etPurchaseInvoiceNo.setEnabled(true);
                    } else if (jsonData.getString("purchaseInvoiceNoType").equalsIgnoreCase("3")) {
                        b.etPurchaseInvoiceNo.setFocusable(false);
                        b.etPurchaseInvoiceNo.setClickable(true);
                        b.etPurchaseInvoiceNo.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("purchaseInvoiceNoType").equalsIgnoreCase("4")) {
                        b.etPurchaseInvoiceNo.setFocusable(false);
                        b.etPurchaseInvoiceNo.setClickable(true);
                        b.etPurchaseInvoiceNo.setHint(getString(R.string.enterTime));
                    }
                    b.tvPurchaseInvoiceNo.setVisibility(View.VISIBLE);
                    b.etPurchaseInvoiceNo.setVisibility(View.VISIBLE);
                } else {
                    b.tvPurchaseInvoiceNo.setVisibility(View.GONE);
                    b.etPurchaseInvoiceNo.setVisibility(View.GONE);
                }
                if (jsonData.getString("materialType").equals("1")) {
                    if (jsonData.getString("materialTypeRequire").equalsIgnoreCase("1")) {
                        b.tvMaterialType.setText(getString(R.string.materialTypeRequire));
                    } else {
                        b.tvMaterialType.setText(getString(R.string.materialType));
                    }
                    if (jsonData.getString("materialTypeType").equalsIgnoreCase("1")) {
                        b.etMaterialType.setRawInputType(setInputType("1"));
                        b.etMaterialType.setFocusable(true);
                        b.etMaterialType.setEnabled(true);
                    } else if (jsonData.getString("materialTypeType").equalsIgnoreCase("2")) {
                        b.etMaterialType.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etMaterialType);
                        b.etMaterialType.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etMaterialType.setFocusable(true);
                        b.etMaterialType.setEnabled(true);
                    } else if (jsonData.getString("materialTypeType").equalsIgnoreCase("3")) {
                        b.etMaterialType.setFocusable(false);
                        b.etMaterialType.setClickable(true);
                        b.etMaterialType.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("materialTypeType").equalsIgnoreCase("4")) {
                        b.etMaterialType.setFocusable(false);
                        b.etMaterialType.setClickable(true);
                        b.etMaterialType.setHint(getString(R.string.enterTime));
                    }

                    b.tvMaterialType.setVisibility(View.VISIBLE);
                    b.etMaterialType.setVisibility(View.VISIBLE);
                } else {
                    b.tvMaterialType.setVisibility(View.GONE);
                    b.etMaterialType.setVisibility(View.GONE);
                }
                if (jsonData.getString("aboutThisItem").equals("1")) {
                    if (jsonData.getString("aboutThisItemTypeRequire").equalsIgnoreCase("1")) {
                        b.tvAboutThisItem.setText(getString(R.string.aboutThisItemRequire));
                    } else {
                        b.tvAboutThisItem.setText(getString(R.string.aboutThisItem));
                    }
                    if (jsonData.getString("aboutThisItemType").equalsIgnoreCase("1")) {
                        b.etAboutThisItem.setRawInputType(setInputType("1"));
                        b.etAboutThisItem.setFocusable(true);
                        b.etAboutThisItem.setEnabled(true);
                    } else if (jsonData.getString("aboutThisItemType").equalsIgnoreCase("2")) {
                        b.etAboutThisItem.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etAboutThisItem);
                        b.etAboutThisItem.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etAboutThisItem.setFocusable(true);
                        b.etAboutThisItem.setEnabled(true);
                    } else if (jsonData.getString("aboutThisItemType").equalsIgnoreCase("3")) {
                        b.etAboutThisItem.setFocusable(false);
                        b.etAboutThisItem.setClickable(true);
                        b.etAboutThisItem.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("aboutThisItemType").equalsIgnoreCase("4")) {
                        b.etAboutThisItem.setFocusable(false);
                        b.etAboutThisItem.setClickable(true);
                        b.etAboutThisItem.setHint(getString(R.string.enterTime));
                    }


                    b.tvAboutThisItem.setVisibility(View.VISIBLE);
                    b.etAboutThisItem.setVisibility(View.VISIBLE);
                } else {
                    b.tvAboutThisItem.setVisibility(View.GONE);
                    b.etAboutThisItem.setVisibility(View.GONE);
                }
                if (jsonData.getString("manufacturer").equals("1")) {
                    if (jsonData.getString("manufacturerRequire").equalsIgnoreCase("1")) {
                        b.tvManufacturer.setText(getString(R.string.manufacturerRequire));
                    } else {
                        b.tvManufacturer.setText(getString(R.string.manufacturer));
                    }
                    if (jsonData.getString("manufacturerType").equalsIgnoreCase("1")) {
                        b.etManufacturer.setRawInputType(setInputType("1"));
                        b.etManufacturer.setFocusable(true);
                        b.etManufacturer.setEnabled(true);
                    } else if (jsonData.getString("manufacturerType").equalsIgnoreCase("2")) {
                        b.etManufacturer.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etManufacturer);
                        b.etManufacturer.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etManufacturer.setFocusable(true);
                        b.etManufacturer.setEnabled(true);
                    } else if (jsonData.getString("manufacturerType").equalsIgnoreCase("3")) {
                        b.etManufacturer.setFocusable(false);
                        b.etManufacturer.setClickable(true);
                        b.etManufacturer.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("manufacturerType").equalsIgnoreCase("4")) {
                        b.etManufacturer.setFocusable(false);
                        b.etManufacturer.setClickable(true);
                        b.etManufacturer.setHint(getString(R.string.enterTime));
                    }


                    b.tvManufacturer.setVisibility(View.VISIBLE);
                    b.etManufacturer.setVisibility(View.VISIBLE);
                } else {
                    b.tvManufacturer.setVisibility(View.GONE);
                    b.etManufacturer.setVisibility(View.GONE);
                }

                if (jsonData.getString("licenseNo").equals("1")) {
                    if (jsonData.getString("licenseNoRequire").equalsIgnoreCase("1")) {
                        b.tvLicenseNo.setText(getString(R.string.licenseNoRequire));
                    } else {
                        b.tvLicenseNo.setText(getString(R.string.licenseNo));
                    }
                    if (jsonData.getString("licenseNoType").equalsIgnoreCase("1")) {
                        b.etLicenseNo.setRawInputType(setInputType("1"));
                        b.etLicenseNo.setFocusable(true);
                        b.etLicenseNo.setEnabled(true);
                    } else if (jsonData.getString("licenseNoType").equalsIgnoreCase("2")) {
                        b.etLicenseNo.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etLicenseNo);
                        b.etLicenseNo.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etLicenseNo.setFocusable(true);
                        b.etLicenseNo.setEnabled(true);
                    } else if (jsonData.getString("licenseNoType").equalsIgnoreCase("3")) {
                        b.etLicenseNo.setFocusable(false);
                        b.etLicenseNo.setClickable(true);
                        b.etLicenseNo.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("licenseNoType").equalsIgnoreCase("4")) {
                        b.etLicenseNo.setFocusable(false);
                        b.etLicenseNo.setClickable(true);
                        b.etLicenseNo.setHint(getString(R.string.enterTime));
                    }


                    b.tvLicenseNo.setVisibility(View.VISIBLE);
                    b.etLicenseNo.setVisibility(View.VISIBLE);
                } else {
                    b.tvLicenseNo.setVisibility(View.GONE);
                    b.etLicenseNo.setVisibility(View.GONE);
                }
                if (jsonData.getString("disclaimer").equals("1")) {
                    if (jsonData.getString("disclaimerRequire").equalsIgnoreCase("1")) {
                        b.tvDisclaimer.setText(getString(R.string.disclaimerRequire));
                    } else {
                        b.tvDisclaimer.setText(getString(R.string.disclaimer));
                    }
                    if (jsonData.getString("disclaimerType").equalsIgnoreCase("1")) {
                        b.etDisclaimer.setRawInputType(setInputType("1"));
                        b.etDisclaimer.setFocusable(true);
                        b.etDisclaimer.setEnabled(true);
                    } else if (jsonData.getString("disclaimerType").equalsIgnoreCase("2")) {
                        b.etDisclaimer.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etDisclaimer);
                        b.etDisclaimer.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etDisclaimer.setFocusable(true);
                        b.etDisclaimer.setEnabled(true);
                    } else if (jsonData.getString("disclaimerType").equalsIgnoreCase("3")) {
                        b.etDisclaimer.setFocusable(false);
                        b.etDisclaimer.setClickable(true);
                        b.etDisclaimer.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("disclaimerType").equalsIgnoreCase("4")) {
                        b.etDisclaimer.setFocusable(false);
                        b.etDisclaimer.setClickable(true);
                        b.etDisclaimer.setHint(getString(R.string.enterTime));
                    }
                    b.tvDisclaimer.setVisibility(View.VISIBLE);
                    b.etDisclaimer.setVisibility(View.VISIBLE);
                } else {
                    b.tvDisclaimer.setVisibility(View.GONE);
                    b.etDisclaimer.setVisibility(View.GONE);
                }
                if (jsonData.getString("shelfLife").equals("1")) {
                    if (jsonData.getString("shelfLifeRequire").equalsIgnoreCase("1")) {
                        b.tvShelfLife.setText(getString(R.string.shelfLifeRequire));
                    } else {
                        b.tvShelfLife.setText(getString(R.string.shelfLife));
                    }
                    if (jsonData.getString("shelfLifeType").equalsIgnoreCase("1")) {
                        b.etShelfLife.setRawInputType(setInputType("1"));
                        b.etShelfLife.setFocusable(true);
                        b.etShelfLife.setEnabled(true);
                    } else if (jsonData.getString("shelfLifeType").equalsIgnoreCase("2")) {
                        b.etShelfLife.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etShelfLife);
                        b.etShelfLife.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etShelfLife.setFocusable(true);
                        b.etShelfLife.setEnabled(true);
                    } else if (jsonData.getString("shelfLifeType").equalsIgnoreCase("3")) {
                        b.etShelfLife.setFocusable(false);
                        b.etShelfLife.setClickable(true);
                        b.etShelfLife.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("shelfLifeType").equalsIgnoreCase("4")) {
                        b.etShelfLife.setFocusable(false);
                        b.etShelfLife.setClickable(true);
                        b.etShelfLife.setHint(getString(R.string.enterTime));
                    }
                    b.tvShelfLife.setVisibility(View.VISIBLE);
                    b.etShelfLife.setVisibility(View.VISIBLE);
                } else {
                    b.tvShelfLife.setVisibility(View.GONE);
                    b.etShelfLife.setVisibility(View.GONE);
                }
                if (jsonData.getString("fssaiLicense").equals("1")) {
                    if (jsonData.getString("fssaiLicenseRequire").equalsIgnoreCase("1")) {
                        b.tvFssaiLicense.setText(getString(R.string.fssaiLicenseRequire));
                    } else {
                        b.tvFssaiLicense.setText(getString(R.string.fssaiLicense));
                    }
                    if (jsonData.getString("fssaiLicenseType").equalsIgnoreCase("1")) {
                        b.etFssaiLicense.setRawInputType(setInputType("1"));
                        b.etFssaiLicense.setFocusable(true);
                        b.etFssaiLicense.setEnabled(true);
                    } else if (jsonData.getString("fssaiLicenseType").equalsIgnoreCase("2")) {
                        b.etFssaiLicense.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etFssaiLicense);
                        b.etFssaiLicense.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etFssaiLicense.setFocusable(true);
                        b.etFssaiLicense.setEnabled(true);
                    } else if (jsonData.getString("fssaiLicenseType").equalsIgnoreCase("3")) {
                        b.etFssaiLicense.setFocusable(false);
                        b.etFssaiLicense.setClickable(true);
                        b.etFssaiLicense.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("fssaiLicenseType").equalsIgnoreCase("4")) {
                        b.etFssaiLicense.setFocusable(false);
                        b.etFssaiLicense.setClickable(true);
                        b.etFssaiLicense.setHint(getString(R.string.enterTime));
                    }
                    b.tvFssaiLicense.setVisibility(View.VISIBLE);
                    b.etFssaiLicense.setVisibility(View.VISIBLE);
                } else {
                    b.tvFssaiLicense.setVisibility(View.GONE);
                    b.etFssaiLicense.setVisibility(View.GONE);
                }
                if (jsonData.getString("countryOfOrigin").equals("1")) {
                    if (jsonData.getString("countryOfOriginRequire").equalsIgnoreCase("1")) {
                        b.tvCountryOfOrigin.setText(getString(R.string.countryOfOriginRequire));
                    } else {
                        b.tvCountryOfOrigin.setText(getString(R.string.countryOfOrigin));
                    }
                    if (jsonData.getString("countryOfOriginType").equalsIgnoreCase("1")) {
                        b.etCountryOfOrigin.setRawInputType(setInputType("1"));
                        b.etCountryOfOrigin.setFocusable(true);
                        b.etCountryOfOrigin.setEnabled(true);
                    } else if (jsonData.getString("countryOfOriginType").equalsIgnoreCase("2")) {
                        b.etCountryOfOrigin.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etCountryOfOrigin);
                        b.etCountryOfOrigin.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etCountryOfOrigin.setFocusable(true);
                        b.etCountryOfOrigin.setEnabled(true);
                    } else if (jsonData.getString("countryOfOriginType").equalsIgnoreCase("3")) {
                        b.etCountryOfOrigin.setFocusable(false);
                        b.etCountryOfOrigin.setClickable(true);
                        b.etCountryOfOrigin.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("countryOfOriginType").equalsIgnoreCase("4")) {
                        b.etCountryOfOrigin.setFocusable(false);
                        b.etCountryOfOrigin.setClickable(true);
                        b.etCountryOfOrigin.setHint(getString(R.string.enterTime));
                    }
                    b.tvCountryOfOrigin.setVisibility(View.VISIBLE);
                    b.etCountryOfOrigin.setVisibility(View.VISIBLE);
                } else {
                    b.tvCountryOfOrigin.setVisibility(View.GONE);
                    b.etCountryOfOrigin.setVisibility(View.GONE);
                }
                if (jsonData.getString("seller").equals("1")) {
                    if (jsonData.getString("sellerRequire").equalsIgnoreCase("1")) {
                        b.tvSellerName.setText(getString(R.string.sellerRequire));
                    } else {
                        b.tvSellerName.setText(getString(R.string.seller));
                    }
                    if (jsonData.getString("sellerType").equalsIgnoreCase("1")) {
                        b.etSellerName.setRawInputType(setInputType("1"));
                        b.etSellerName.setFocusable(true);
                        b.etSellerName.setEnabled(true);
                    } else if (jsonData.getString("sellerType").equalsIgnoreCase("2")) {
                        b.etSellerName.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etSellerName);
                        b.etSellerName.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etSellerName.setFocusable(true);
                        b.etSellerName.setEnabled(true);
                    } else if (jsonData.getString("sellerType").equalsIgnoreCase("3")) {
                        b.etSellerName.setFocusable(false);
                        b.etSellerName.setClickable(true);
                        b.etSellerName.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("sellerType").equalsIgnoreCase("4")) {
                        b.etSellerName.setFocusable(false);
                        b.etSellerName.setClickable(true);
                        b.etSellerName.setHint(getString(R.string.enterTime));
                    }
                    b.tvSellerName.setVisibility(View.VISIBLE);
                    b.etSellerName.setVisibility(View.VISIBLE);
                } else {
                    b.tvSellerName.setVisibility(View.GONE);
                    b.etSellerName.setVisibility(View.GONE);
                }
                if (jsonData.getString("ingredients").equals("1")) {
                    if (jsonData.getString("ingredientsRequire").equalsIgnoreCase("1")) {
                        b.tvIngredients.setText(getString(R.string.ingredientsRequire));
                    } else {
                        b.tvIngredients.setText(getString(R.string.ingredients));
                    }
                    if (jsonData.getString("ingredientsType").equalsIgnoreCase("1")) {
                        b.etIngredients.setRawInputType(setInputType("1"));
                        b.etIngredients.setFocusable(true);
                        b.etIngredients.setEnabled(true);
                    } else if (jsonData.getString("ingredientsType").equalsIgnoreCase("2")) {
                        b.etIngredients.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etIngredients);
                        b.etIngredients.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etIngredients.setFocusable(true);
                        b.etIngredients.setEnabled(true);
                    } else if (jsonData.getString("ingredientsType").equalsIgnoreCase("3")) {
                        b.etIngredients.setFocusable(false);
                        b.etIngredients.setClickable(true);
                        b.etIngredients.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("ingredientsType").equalsIgnoreCase("4")) {
                        b.etIngredients.setFocusable(false);
                        b.etIngredients.setClickable(true);
                        b.etIngredients.setHint(getString(R.string.enterTime));
                    }
                    b.tvIngredients.setVisibility(View.VISIBLE);
                    b.etIngredients.setVisibility(View.VISIBLE);
                } else {
                    b.tvIngredients.setVisibility(View.GONE);
                    b.etIngredients.setVisibility(View.GONE);
                }
                if (jsonData.getString("content").equals("1")) {
                    if (jsonData.getString("contentRequire").equalsIgnoreCase("1")) {
                        b.tvContent.setText(getString(R.string.contentRequire));
                    } else {
                        b.tvContent.setText(getString(R.string.content));
                    }
                    if (jsonData.getString("contentType").equalsIgnoreCase("1")) {
                        b.etContent.setRawInputType(setInputType("1"));
                        b.etContent.setFocusable(true);
                        b.etContent.setEnabled(true);
                    } else if (jsonData.getString("contentType").equalsIgnoreCase("2")) {
                        b.etContent.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etContent);
                        b.etContent.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etContent.setFocusable(true);
                        b.etContent.setEnabled(true);
                    } else if (jsonData.getString("contentType").equalsIgnoreCase("3")) {
                        b.etContent.setFocusable(false);
                        b.etContent.setClickable(true);
                        b.etContent.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("contentType").equalsIgnoreCase("4")) {
                        b.etContent.setFocusable(false);
                        b.etContent.setClickable(true);
                        b.etContent.setHint(getString(R.string.enterTime));
                    }
                    b.tvContent.setVisibility(View.VISIBLE);
                    b.etContent.setVisibility(View.VISIBLE);
                } else {
                    b.tvContent.setVisibility(View.GONE);
                    b.etContent.setVisibility(View.GONE);
                }
                if (jsonData.getString("weight").equals("1")) {
                    if (jsonData.getString("weightRequire").equalsIgnoreCase("1")) {
                        b.tvWeight.setText(getString(R.string.weightRequire));
                    } else {
                        b.tvWeight.setText(getString(R.string.weight));
                    }
                    b.etWeight.setVisibility(View.VISIBLE);
                    b.tvWeight.setVisibility(View.VISIBLE);

                    if (jsonData.getString("weightType").equalsIgnoreCase("1")) {
                        b.etWeight.setRawInputType(setInputType("1"));
                        b.etWeight.setFocusable(true);
                        b.etWeight.setEnabled(true);
                    } else if (jsonData.getString("weightType").equalsIgnoreCase("2")) {
                        b.etWeight.setRawInputType(setInputType("2"));
                        setAcceptNumericOnly(b.etWeight);
                        b.etWeight.setFilters(new InputFilter[]{new DecimalInputFilter()});
                        b.etWeight.setFocusable(true);
                        b.etWeight.setEnabled(true);
                    } else if (jsonData.getString("weightType").equalsIgnoreCase("3")) {
                        b.etWeight.setFocusable(false);
                        b.etWeight.setClickable(true);
                        b.etWeight.setHint(getString(R.string.enterDate));
                    } else if (jsonData.getString("weightType").equalsIgnoreCase("4")) {
                        b.etWeight.setFocusable(false);
                        b.etWeight.setClickable(true);
                        b.etWeight.setHint(getString(R.string.enterTime));
                    }

                } else {
                    b.etWeight.setVisibility(View.GONE);
                    b.tvWeight.setVisibility(View.GONE);
                }
                if (getIntent().getExtras() != null) {
                    hitGetProductDetailApi();
                }
                b.tvAddProduct.setVisibility(View.VISIBLE);
            } else {
                b.tvAddProduct.setVisibility(View.GONE);
                AppUtils.showMessageDialog(mActivity, getString(R.string.products), getString(R.string.modelNotPersent), 2);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitGetDropDownData(int type) {

        String url = "";

        switch (type) {

            case 1:

                url = AppUrls.getBrands;
                break;
            case 2:

                url = AppUrls.getSize;
                break;
            case 3:

                url = AppUrls.getColors;
                break;

        }

        WebServices.getApi(mActivity, url, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseDropDownJson(response, type);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseDropDownJson(JSONObject response, int type) {

        switch (type) {

            case 1:
                arrayListBrand.clear();
                break;
            case 2:
                arrayListSize.clear();
                break;
            case 3:
                arrayListColor.clear();
                break;

        }

        int spinnerPosition = 0;

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("name"));

                    switch (type) {

                        case 1:
                            if (jsonObject1.getString("_id").equals(brandId)) spinnerPosition = i;
                            arrayListBrand.add(hashMap);
                            break;
                        case 2:
                            if (jsonObject1.getString("_id").equals(size)) spinnerPosition = i;

                            arrayListSize.add(hashMap);
                            break;
                        case 3:
                            if (jsonObject1.getString("_id").equals(color)) spinnerPosition = i;

                            arrayListColor.add(hashMap);
                            break;

                    }

                }

                switch (type) {

                    case 1:

                        b.spinnerBrand.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayListBrand));
                        b.spinnerBrand.setSelection(spinnerPosition);
                        break;
                    case 2:

                        b.spinnerSize.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayListSize));
                        b.spinnerSize.setSelection(spinnerPosition);
                        break;
                    case 3:

                        b.spinnerColor.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayListColor));
                        b.spinnerColor.setSelection(spinnerPosition);
                        break;

                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitGetProductDetailApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("productId", productId);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.productDetail, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseProductDetail(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseProductDetail(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");
                Log.v("data", jsonData.toString());
                b.etFullName.setText(jsonData.getString("name"));
                b.etUnit.setText(jsonData.getString("unit"));
                b.etWeight.setText(jsonData.getString("weight"));
                b.etQuantity.setText(jsonData.getString("quantity"));
                b.etDescription.setText(jsonData.getString("description"));
                b.etMrp.setText(jsonData.getString("Mrp"));
                b.etPrice.setText(jsonData.getString("price"));
                b.etSellingPrice.setText(jsonData.getString("sellingPrice"));
                b.etOfferPrice.setText(jsonData.getString("offerPrice"));
                b.etBestBefore.setText(jsonData.getString("bestBefore"));

                //extra fields
                b.etPackedType.setText(jsonData.getString("packedType"));
                b.etPurchaseInvoiceDate.setText(jsonData.getString("purchaseInvoiceDate"));
                b.etExpiryDate.setText(jsonData.getString("expiryDate"));
                b.etBatchNumber.setText(jsonData.getString("batchNumber"));
                b.etDistributorName.setText(jsonData.getString("distributorName"));
                b.etPurchaseInvoiceNo.setText(jsonData.getString("purchaseInvoiceNo"));
                b.etMaterialType.setText(jsonData.getString("materialType"));
                b.etAboutThisItem.setText(jsonData.getString("aboutThisItem"));
                b.etManufacturer.setText(jsonData.getString("manufacturer"));
                b.etLicenseNo.setText(jsonData.getString("licenseNo"));
                b.etDisclaimer.setText(jsonData.getString("disclaimer"));
                b.etShelfLife.setText(jsonData.getString("shelfLife"));
                b.etFssaiLicense.setText(jsonData.getString("fssaiLicense"));
                b.etCountryOfOrigin.setText(jsonData.getString("countryOfOrigin"));
                b.etSellerName.setText(jsonData.getString("seller"));
                b.etIngredients.setText(jsonData.getString("ingredients"));
                b.etContent.setText(jsonData.getString("content"));
                b.etSpecialOffer.setText(jsonData.getString("specialFeature"));
                b.etPrepTime.setText(jsonData.getString("preparationTime"));

                subCategoryId = jsonData.getString("subCategoryId");
                unitType = jsonData.getString("unitType");
                brandId = jsonData.getString("brand");
                color = jsonData.getString("color");
                size = jsonData.getString("size");

                JSONArray jsonArray = jsonData.getJSONArray("productImage");

                arrayListImages.clear();

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("image", jsonObject1.getString("imageUrl"));
                    hashMap.put("type", "2");

                    arrayListImages.add(hashMap);
                }

                adapterImages.notifyDataSetChanged();

                hitGetSubCategoryApi();
                hitGetUnitTypeApi();
                hitGetDropDownData(1);
                hitGetDropDownData(2);
                hitGetDropDownData(3);
//                hitGetQuantityApi();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitGetSubCategoryApi() {

        WebServices.getApi(mActivity, AppUrls.subCategory, true, true, new WebServicesCallback() {

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

        arrayListSubCategory.clear();
        int spinnerPosition = 0;

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("subCategoryId", jsonObject1.getString("subCategoryId"));
                    hashMap.put("name", jsonObject1.getString("subCategoryName"));

                    if (jsonObject1.getString("subCategoryId").equals(subCategoryId))
                        spinnerPosition = i;

                    arrayListSubCategory.add(hashMap);
                }

                b.spinnerSubCategory.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayListSubCategory));
                b.spinnerSubCategory.setSelection(spinnerPosition);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ivBack:
                onBackPressed();
                break;
            case R.id.tvAddImage:

                AlertCameraGallery();

                break;

            case R.id.etFullName:
                try {
                    if (jsonData.getString("productNameType").equals("3")) {
                        showDateDialogType(b.etFullName);
                    } else if (jsonData.getString("productNameType").equals("4")) {
                        showTimePicker(b.etFullName);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etPrice:
                try {
                    if (jsonData.getString("productPriceType").equals("3")) {
                        showDateDialogType(b.etPrice);
                    } else if (jsonData.getString("productPriceType").equals("4")) {
                        showTimePicker(b.etPrice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etMrp:
                try {
                    if (jsonData.getString("MrpRequire").equals("3")) {
                        showDateDialogType(b.etMrp);
                    } else if (jsonData.getString("MrpRequire").equals("4")) {
                        showTimePicker(b.etMrp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            case R.id.etContent:
                try {
                    if (jsonData.getString("contentType").equals("3")) {
                        showDateDialogType(b.etContent);
                    } else if (jsonData.getString("contentType").equals("4")) {
                        showTimePicker(b.etContent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etIngredients:
                try {
                    if (jsonData.getString("ingredientsType").equals("3")) {
                        showDateDialogType(b.etIngredients);
                    } else if (jsonData.getString("ingredientsType").equals("4")) {
                        showTimePicker(b.etIngredients);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etSellerName:
                try {
                    if (jsonData.getString("sellerType").equals("3")) {
                        showDateDialogType(b.etSellerName);
                    } else if (jsonData.getString("sellerType").equals("4")) {
                        showTimePicker(b.etSellerName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etCountryOfOrigin:
                try {
                    if (jsonData.getString("countryOfOriginType").equals("3")) {
                        showDateDialogType(b.etCountryOfOrigin);
                    } else if (jsonData.getString("countryOfOriginType").equals("4")) {
                        showTimePicker(b.etCountryOfOrigin);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etFssaiLicense:
                try {
                    if (jsonData.getString("fssaiLicenseType").equals("3")) {
                        showDateDialogType(b.etFssaiLicense);
                    } else if (jsonData.getString("fssaiLicenseType").equals("4")) {
                        showTimePicker(b.etFssaiLicense);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etShelfLife:
                try {
                    if (jsonData.getString("shelfLifeType").equals("3")) {
                        showDateDialogType(b.etShelfLife);
                    } else if (jsonData.getString("shelfLifeType").equals("4")) {
                        showTimePicker(b.etShelfLife);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etDisclaimer:
                try {
                    if (jsonData.getString("disclaimerType").equals("3")) {
                        showDateDialogType(b.etDisclaimer);
                    } else if (jsonData.getString("disclaimerType").equals("4")) {
                        showTimePicker(b.etDisclaimer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etManufacturer:
                try {
                    if (jsonData.getString("manufacturerType").equals("3")) {
                        showDateDialogType(b.etManufacturer);
                    } else if (jsonData.getString("manufacturerType").equals("4")) {
                        showTimePicker(b.etManufacturer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etLicenseNo:
                try {
                    if (jsonData.getString("licenseNoType").equals("3")) {
                        showDateDialogType(b.etLicenseNo);
                    } else if (jsonData.getString("licenseNoType").equals("4")) {
                        showTimePicker(b.etLicenseNo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etAboutThisItem:
                try {
                    if (jsonData.getString("aboutThisItemType").equals("3")) {
                        showDateDialogType(b.etAboutThisItem);
                    } else if (jsonData.getString("aboutThisItemType").equals("4")) {
                        showTimePicker(b.etAboutThisItem);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etMaterialType:
                try {
                    if (jsonData.getString("materialTypeType").equals("3")) {
                        showDateDialogType(b.etMaterialType);
                    } else if (jsonData.getString("materialTypeType").equals("4")) {
                        showTimePicker(b.etMaterialType);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etPurchaseInvoiceNo:
                try {
                    if (jsonData.getString("purchaseInvoiceNoType").equals("3")) {
                        showDateDialogType(b.etPurchaseInvoiceNo);
                    } else if (jsonData.getString("purchaseInvoiceNoType").equals("4")) {
                        showTimePicker(b.etPurchaseInvoiceNo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.etDistributorName:
                try {
                    if (jsonData.getString("distributorNameType").equals("3")) {
                        showDateDialogType(b.etDistributorName);
                    } else if (jsonData.getString("distributorNameType").equals("4")) {
                        showTimePicker(b.etDistributorName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.etBatchNumber:
                try {
                    if (jsonData.getString("batchNumberType").equals("3")) {
                        showDateDialogType(b.etBatchNumber);
                    } else if (jsonData.getString("batchNumberType").equals("4")) {
                        showTimePicker(b.etBatchNumber);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.etPackedType:
                try {
                    if (jsonData.getString("packedTypeType").equals("3")) {
                        showDateDialogType(b.etPackedType);
                    } else if (jsonData.getString("packedTypeType").equals("4")) {
                        showTimePicker(b.etPackedType);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.etPurchaseInvoiceDate:
                try {
                    if (jsonData.getString("purchaseInvoiceDateType").equals("3")) {
                        showDateDialogType(b.etPurchaseInvoiceDate);
                    } else if (jsonData.getString("purchaseInvoiceDateType").equals("4")) {
                        showTimePicker(b.etPurchaseInvoiceDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.etExpiryDate:

                try {
                    if (jsonData.getString("expiryDateType").equals("3")) {
                        showDateDialogType(b.etExpiryDate);
                    } else if (jsonData.getString("expiryDateType").equals("4")) {
                        showTimePicker(b.etExpiryDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case R.id.etSellingPrice:
                try {
                    if (jsonData.getString("sellingPriceType").equals("3")) {
                        showDateDialogType(b.etSellingPrice);
                    } else if (jsonData.getString("sellingPriceType").equals("4")) {
                        showTimePicker(b.etSellingPrice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.etOfferPrice:
                try {
                    if (jsonData.getString("offerPriceType").equals("3")) {
                        showDateDialogType(b.etOfferPrice);
                    } else if (jsonData.getString("offerPriceType").equals("4")) {
                        showTimePicker(b.etOfferPrice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etSpecialOffer:
                try {
                    if (jsonData.getString("specialFeatureType").equals("3")) {
                        showDateDialogType(b.etSpecialOffer);
                    } else if (jsonData.getString("specialFeatureType").equals("4")) {
                        showTimePicker(b.etSpecialOffer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.etPrepTime:
                try {
                    if (jsonData.getString("preparationTimeType").equals("3")) {
                        showDateDialogType(b.etPrepTime);
                    } else if (jsonData.getString("preparationTimeType").equals("4")) {
                        showTimePicker(b.etPrepTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.tvAddProduct:

                validate();

                break;

            case R.id.etBestBefore:

                try {
                    if (jsonData.getString("bestBeforeDateType").equals("3")) {
                        showDateDialogType(b.etBestBefore);
                    } else if (jsonData.getString("bestBeforeDateType").equals("4")) {
                        showTimePicker(b.etBestBefore);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;

        }
    }

    private void validate() {
        try {
            String qty = b.etQuantity.getText().toString().trim();
            if (TextUtils.isEmpty(b.etFullName.getText().toString().trim()) && jsonData.getString("productRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterItemName));
            } else if (TextUtils.isEmpty(b.etUnit.getText().toString().trim()) && jsonData.getString("unitRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterUnit));
            } else if (TextUtils.isEmpty(b.etWeight.getText().toString().trim()) && jsonData.getString("weightRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterWeight));
            } else if (arrayListSubCategory.size() == 0 && jsonData.getString("subcategoryRequire").equals("1")) {
                hitGetSubCategoryApi();
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectSubCategory));
            } else if (TextUtils.isEmpty(b.etWeight.getText().toString().trim()) && jsonData.getString("weightRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterWeight));
            } else if (arrayListBrand.size() == 0 && jsonData.getString("brandRequire").equals("1")) {
                hitGetDropDownData(1);
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectBrand));
            } else if (arrayListColor.size() == 0 && jsonData.getString("colorRequire").equals("1")) {
                hitGetDropDownData(3);
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectColor));
            } else if (arrayListSize.size() == 0 && jsonData.getString("sizeRequire").equals("1")) {
                hitGetDropDownData(2);
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectSize));
            } else if (TextUtils.isEmpty(b.etQuantity.getText().toString().trim()) && jsonData.getString("quantityRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterQuantity));
            }
//            else if (qty.contains("-")) {
//                AppUtils.showToastSort(mActivity, getString(R.string.pleaseValidEnterQuantity));
//            }
            else if (TextUtils.isEmpty(b.etPrice.getText().toString().trim()) && jsonData.getString("priceRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPrice));
            } else if (TextUtils.isEmpty(b.etMrp.getText().toString().trim()) && jsonData.getString("MrpRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterMrp));
            } else if (TextUtils.isEmpty(b.etOfferPrice.getText().toString().trim()) && jsonData.getString("offerRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterOfferPrice));
            } else if (TextUtils.isEmpty(b.etPackedType.getText().toString().trim()) && jsonData.getString("packedTypeRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPackedType));
            } else if (TextUtils.isEmpty(b.etExpiryDate.getText().toString().trim()) && jsonData.getString("expiryDateRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectExpiryDate));
            } else if (TextUtils.isEmpty(b.etBatchNumber.getText().toString().trim()) && jsonData.getString("batchNumberRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterBatchNumber));
            } else if (TextUtils.isEmpty(b.etDistributorName.getText().toString().trim()) && jsonData.getString("distributorNameRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterDistributorName));
            } else if (TextUtils.isEmpty(b.etPurchaseInvoiceNo.getText().toString().trim()) && jsonData.getString("purchaseInvoiceNoRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPurchaseInvoiceNo));
            } else if (TextUtils.isEmpty(b.etMaterialType.getText().toString().trim()) && jsonData.getString("materialTypeRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterMaterialType));
            } else if (TextUtils.isEmpty(b.etAboutThisItem.getText().toString().trim()) && jsonData.getString("aboutThisItemTypeRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterAboutThisItem));
            } else if (TextUtils.isEmpty(b.etManufacturer.getText().toString().trim()) && jsonData.getString("manufacturerRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterManufacturer));
            } else if (TextUtils.isEmpty(b.etDisclaimer.getText().toString().trim()) && jsonData.getString("disclaimerRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterDisclaimer));
            } else if (TextUtils.isEmpty(b.etShelfLife.getText().toString().trim()) && jsonData.getString("shelfLifeRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterShelfLife));
            } else if (TextUtils.isEmpty(b.etFssaiLicense.getText().toString().trim()) && jsonData.getString("fssaiLicenseRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterFssaiLicense));
            } else if (TextUtils.isEmpty(b.etCountryOfOrigin.getText().toString().trim()) && jsonData.getString("countryOfOriginRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCountryOfOrigin));
            } else if (TextUtils.isEmpty(b.etSellerName.getText().toString().trim()) && jsonData.getString("sellerRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterSellerName));
            } else if (TextUtils.isEmpty(b.etIngredients.getText().toString().trim()) && jsonData.getString("ingredientsRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterIngredients));
            } else if (TextUtils.isEmpty(b.etContent.getText().toString().trim()) && jsonData.getString("ingredientsRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterContent));
            } else if (TextUtils.isEmpty(b.etSellingPrice.getText().toString().trim()) && jsonData.getString("sellingPriceRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterSellingPrice));
            } else if (TextUtils.isEmpty(b.etOfferPrice.getText().toString().trim()) && jsonData.getString("offerRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterOfferPrice));
            } else if (TextUtils.isEmpty(b.etBestBefore.getText().toString().trim()) && jsonData.getString("bestBeforeDateRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectBestBefore));
            } else if (TextUtils.isEmpty(b.etDescription.getText().toString().trim()) && jsonData.getString("descriptionRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterDescription));
            } else if (TextUtils.isEmpty(b.etSpecialOffer.getText().toString().trim()) && jsonData.getString("specialFeatureRequire").equals("1")) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterSpecialFeature));
            } else if (arrayListImages.size() == 0) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseAddImage));
            } else {
                hitAddUpdateProductApi();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void hitAddUpdateProductApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("name", b.etFullName.getText().toString().trim());
            jsonObject.put("unit", b.etUnit.getText().toString().trim());
            jsonObject.put("weight", b.etWeight.getText().toString().trim());
            jsonObject.put("quantity", b.etQuantity.getText().toString().trim());
            jsonObject.put("bestBefore", b.etBestBefore.getText().toString().trim());
            jsonObject.put("price", b.etPrice.getText().toString().trim());
            jsonObject.put("sellingPrice", b.etSellingPrice.getText().toString().trim());
            jsonObject.put("offerPrice", b.etOfferPrice.getText().toString().trim());
            jsonObject.put("specialFeature", b.etSpecialOffer.getText().toString().trim());
            jsonObject.put("description", b.etDescription.getText().toString().trim());
            jsonObject.put("unitType", arrayListUnitType.size() == 0 ? "" : arrayListUnitType.get(b.spinnerUnitType.getSelectedItemPosition()).get("_id"));
            jsonObject.put("subCategoryId", arrayListSubCategory.size() == 0 ? "" : arrayListSubCategory.get(b.spinnerSubCategory.getSelectedItemPosition()).get("subCategoryId"));
            if (brandValue.equalsIgnoreCase("1")) {
                jsonObject.put("brand", arrayListBrand.size() == 0 ? "" : arrayListBrand.get(b.spinnerBrand.getSelectedItemPosition()).get("_id"));
            } else {
                jsonObject.put("brand", "");
            }
            if (sizeValue.equalsIgnoreCase("1")) {
                jsonObject.put("size", arrayListSize.size() == 0 ? "" : arrayListSize.get(b.spinnerSize.getSelectedItemPosition()).get("_id"));
            } else {
                jsonObject.put("size", "");
            }
            if (colorValue.equalsIgnoreCase("1")) {
                jsonObject.put("color", arrayListColor.size() == 0 ? "" : arrayListColor.get(b.spinnerColor.getSelectedItemPosition()).get("_id"));
            } else {
                jsonObject.put("color", "");
            }
            //added fields
            jsonObject.put("Mrp", b.etMrp.getText().toString());
            jsonObject.put("packedType", b.etPackedType.getText().toString());
            jsonObject.put("purchaseInvoiceDate", b.etPurchaseInvoiceDate.getText().toString());
            jsonObject.put("expiryDate", b.etExpiryDate.getText().toString());
            jsonObject.put("purchasePrice", b.etPrice.getText().toString());
            jsonObject.put("batchNumber", b.etBatchNumber.getText().toString());
            jsonObject.put("distributorName", b.etDistributorName.getText().toString());
            jsonObject.put("purchaseInvoiceNo", b.etPurchaseInvoiceNo.getText().toString());
            jsonObject.put("materialType", b.etMaterialType.getText().toString());
            jsonObject.put("aboutThisItem", b.etAboutThisItem.getText().toString());
            jsonObject.put("manufacturer", b.etManufacturer.getText().toString());
            jsonObject.put("licenseNo", b.etLicenseNo.getText().toString());
            jsonObject.put("disclaimer", b.etDisclaimer.getText().toString());
            jsonObject.put("shelfLife", b.etShelfLife.getText().toString());
            jsonObject.put("fssaiLicense", b.etFssaiLicense.getText().toString());
            jsonObject.put("countryOfOrigin", b.etCountryOfOrigin.getText().toString());
            jsonObject.put("seller", b.etSellerName.getText().toString());
            jsonObject.put("ingredients", b.etIngredients.getText().toString());
            jsonObject.put("content", b.etContent.getText().toString());
            jsonObject.put("preparationTime", b.etPrepTime.getText().toString());

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "";

        if (productId.isEmpty()) {
            url = AppUrls.addProduct;
            WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {
                @Override
                public void OnJsonSuccess(JSONObject response) {
                    parseAddProductJson(response);
                }

                @Override
                public void OnFail(String response) {

                    AppUtils.showToastSort(mActivity, response);
                }
            });
        } else {
            url = AppUrls.updateProduct + productId;
            WebServices.putApi(mActivity, url, json, true, true, new WebServicesCallback() {
                @Override
                public void OnJsonSuccess(JSONObject response) {
                    parseAddProductJson(response);
                }

                @Override
                public void OnFail(String response) {
                }
            });
        }


    }

    private void parseAddProductJson(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                if (arrayListImages.size() > 0) {
                    messages = jsonObject.getString(AppConstants.resMsg);
                    productId = jsonObject.getJSONObject("data").getString("productId");
                    removeAlreadyAddedImage(jsonObject.getString(AppConstants.resMsg));
                } else {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.addProduct), jsonObject.getString(AppConstants.resMsg), 1);
                }
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.addProduct), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }

    private void hitUpdateProductImageApi() {


        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("productId", productId);
            jsonObject.put("productImage", arrayListImages.size() == 0 ? "" : arrayListImages.get(0).get("image"));

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.AddProductImage, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseUpdateImage(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void removeAlreadyAddedImage(String response) {
        if (arrayListImages.size() > 0 && arrayListImages.get(0).get("type").equals("2")) {
            arrayListImages.remove(0);
        }
        if (arrayListImages.size() > 0 && arrayListImages.get(0).get("type").equals("2")) {
            removeAlreadyAddedImage(response);
        } else if (arrayListImages.size() > 0) {
            hitUpdateProductImageApi();
        } else {
            AppUtils.showMessageDialog(mActivity, getString(R.string.updateProduct), response, 1);

        }

    }

    private void parseUpdateImage(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                arrayListImages.remove(0);

                if (arrayListImages.size() > 0) {
                    progressDialog.show();
                    new Handler(Looper.myLooper()).postDelayed(this::hitUpdateProductImageApi, 5000);


                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        if (AppSettings.getString(AppSettings.isFrom).equalsIgnoreCase("Update")) {
                            AppUtils.showMessageDialog(mActivity, getString(R.string.updateProduct), messages, 1);
                            AppSettings.putString(AppSettings.isFrom, "");
                        } else {
                            AppUtils.showMessageDialog(mActivity, getString(R.string.addProduct), messages, 1);
                            AppSettings.putString(AppSettings.isFrom, "");
                        }
                    }
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.addProduct), jsonObject.getString(AppConstants.resMsg), 1);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void AlertCameraGallery() {

        final BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_camera_gallery);

        dialog.setCancelable(true);

        dialog.show();


        RelativeLayout rlCancel = dialog.findViewById(R.id.rlCancel);
        LinearLayout llCamera = dialog.findViewById(R.id.llCamera);
        LinearLayout llGallery = dialog.findViewById(R.id.llGallery);

        rlCancel.setOnClickListener(v -> dialog.dismiss());

        llCamera.setOnClickListener(v -> {

            captureImage();
            dialog.dismiss();
        });

        llGallery.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //photoPickerIntent.putExtra("crop", "true");
            startActivityForResult(photoPickerIntent, selectPicture);
            dialog.dismiss();
        });
    }

    private void captureImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fileUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".provider", AppUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE));
            AppSettings.putString(AppSettings.imagePath, String.valueOf(fileUri));
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(it, capturePicture);
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = AppUtils.getOutputMediaFileUri(MEDIA_TYPE_IMAGE, mActivity); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
            // start the image capture Intent
            startActivityForResult(intent, capturePicture);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap;
        Bitmap rotatedBitmap;
        if (requestCode == capturePicture) {
            if (resultCode == RESULT_OK) {

                if (fileUri == null) {

                    fileUri = Uri.parse(AppSettings.getString(AppSettings.imagePath));
                    picturePath = fileUri.getPath();

                } else {
                    if (!fileUri.equals("")) picturePath = fileUri.getPath();
                }

                String filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);

                String selectedImagePath = picturePath;

                String ext = "jpg";

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 500;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(selectedImagePath, options);

                Matrix matrix = new Matrix();
                matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                byte[] ba = bao.toByteArray();

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("image", AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap));
                hashMap.put("type", "1");
                arrayListImages.add(hashMap);
                adapterImages.notifyDataSetChanged();

            }
        } else if (requestCode == selectPicture) {
            if (data != null) {

                try {
                    //get the Uri for the captured image
                    Uri picUri = data.getData();

                    Uri contentURI = data.getData();

                    if (contentURI.toString().contains("content://com.google.android.apps.photos")) {
                        bitmap = getBitmapFromUri(contentURI);
                    } else {

                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = mActivity.getContentResolver().query(contentURI, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        picturePath = cursor.getString(columnIndex);
                        System.out.println("Image Path : " + picturePath);
                        cursor.close();
                        String filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);

                        String ext = AppUtils.getFileType(picturePath);

                        String selectedImagePath = picturePath;

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(selectedImagePath, options);
                        final int REQUIRED_SIZE = 500;
                        int scale = 1;
                        while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                            scale *= 2;
                        options.inSampleSize = scale;
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                    }

                    Matrix matrix = new Matrix();
                    matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                    rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                    byte[] ba = bao.toByteArray();

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("image", AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap));
                    hashMap.put("type", "1");
                    arrayListImages.add(hashMap);

                    adapterImages.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                AppUtils.showToastSort(mActivity, getString(R.string.unable));

            }

        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private class AdapterImages extends RecyclerView.Adapter<AdapterImages.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterImages(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterImages.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_business_images, viewGroup, false);
            return new AdapterImages.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterImages.MyViewHolder holder, final int position) {

            if (data.get(position).get("type").equals("1")) {
                holder.ivImage.setImageBitmap(AppUtils.base64ToBitmap(data.get(position).get("image")));
            } else {
                AppUtils.loadPicassoImage(data.get(position).get("image"), holder.ivImage);
            }


            holder.ivDelete.setOnClickListener(view -> {

                if (data.get(position).get("type").equals("1")) {
                    data.remove(position);
                    notifyDataSetChanged();
                } else {
                    hitDeleteImageApi(data.get(position).get("_id"));
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivImage, ivDelete;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);
                ivDelete = itemView.findViewById(R.id.ivDelete);

            }
        }
    }

    private void hitDeleteImageApi(String id) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("imageId", id);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.deleteApi(mActivity, AppUrls.RemoveProductImage, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseDeleteImage(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseDeleteImage(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetProductDetailApi();
            } else {
                AppUtils.showResMsgToastSort(mActivity, jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void hitGetUnitTypeApi() {

        WebServices.getApi(mActivity, AppUrls.unitList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseQuantity(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseQuantity(JSONObject response) {

        arrayListUnitType.clear();
        int spinnerPosition = 0;

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("unit"));

                    if (jsonObject1.getString("_id").equals(unitType)) spinnerPosition = i;

                    arrayListUnitType.add(hashMap);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        b.spinnerUnitType.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayListUnitType));
        b.spinnerUnitType.setSelection(spinnerPosition);
    }

    public void showDateDialog(TextView textView) {

        int mYear, mMonth, mDay;

        // Get Current Date
        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity, (view, year, monthOfYear, dayOfMonth) -> {

            String month = "";


            month = String.valueOf(monthOfYear + 1);

            String day = String.valueOf(dayOfMonth);

            if (monthOfYear + 1 < 10) month = "0" + month;

            if (dayOfMonth < 10) day = "0" + day;


            textView.setText(year + "-" + month + "-" + day);

        }, mYear, mMonth, mDay);


        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

        datePickerDialog.show();
    }

    //date dialog
    public void showDateDialogType(EditText etDate) {

        int mYear, mMonth, mDay;

        // Get Current Date
        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity, (view, year, monthOfYear, dayOfMonth) -> {

            String month = "";


            month = String.valueOf(monthOfYear + 1);

            String day = String.valueOf(dayOfMonth);

            if (monthOfYear + 1 < 10) month = "0" + month;

            if (dayOfMonth < 10) day = "0" + day;


            etDate.setText(year + "-" + month + "-" + day);

        }, mYear, mMonth, mDay);


        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

        datePickerDialog.show();
    }

    //Time Dialog
    private void showTimePicker(EditText etTime) {

        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(mActivity, (timePicker, selectedHour, selectedMinute) -> {

            String hour1 = String.valueOf(selectedHour);
            String mnt = String.valueOf(selectedMinute);

            if (selectedHour < 10) hour1 = "0" + selectedHour;
            if (selectedMinute < 10) mnt = "0" + selectedMinute;
            etTime.setText(AppUtils.changeHrFormat(hour1 + ":" + mnt));

        }, hour, minute, false);//Yes 24 hour time

        mTimePicker.show();

    }

    private class DecimalInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String inputText = dest.toString();

            if (inputText.contains(".") && source.equals(".")) {
                return "";
            }

            // Otherwise, allow the input to be added to the EditText.
            return null;
        }
    }

    private boolean checkAndRequestPermissions() {


        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionReadExternalStorage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
        else
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        int permissionWriteExtarnalStorage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionWriteExtarnalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO);
        else
            permissionWriteExtarnalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (permissionWriteExtarnalStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO);
            else listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }

        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            else listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            int permissionVideoStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO);
            if (permissionVideoStorage != PackageManager.PERMISSION_GRANTED) {

                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO);

            }

            int notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);

            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {

                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);

            }

        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();
            // Initialize the map with both permissions


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.put(Manifest.permission.READ_MEDIA_IMAGES, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_MEDIA_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_MEDIA_VIDEO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.POST_NOTIFICATIONS, PackageManager.PERMISSION_GRANTED);


            } else {
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            }


            // Fill with actual results from user
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


                    if (perms.get(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

                    ) {
                        //else any one or both the permissions are not granted
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)


                        ) {
                            showDialogOK("Necessary Permissions required for this app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            // permissionSettingScreen ( );
                                            //  finish();
                                            break;
                                    }
                                }
                            });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            permissionSettingScreen();

                        }
                    }


                } else {


                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                    ) {
                        //else any one or both the permissions are not granted
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Necessary Permissions required for this app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            Toast.makeText(mActivity, "Necessary Permissions required for this app", Toast.LENGTH_LONG).show();
                                            // permissionSettingScreen ( );
                                            //  finish();
                                            break;
                                    }
                                }
                            });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            permissionSettingScreen();

                        }
                    }


                }


            }
        }

    }

    private void permissionSettingScreen() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
        // finishAffinity();
        finish();

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this).setMessage(message).setPositiveButton(getString(R.string.ok), okListener).setNegativeButton(getString(R.string.cancel), okListener).create().show();
    }

    private int setInputType(String type) {

        if (type.equals("2")) {
            return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        } else {
            return InputType.TYPE_CLASS_TEXT;
        }
    }

    private void setAcceptNumericOnly(EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() > 0 && !Pattern.matches(AppConstants.numberDotRgx, s.toString())) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                } else if (s.toString().indexOf('.', s.toString().indexOf('.') + 1) != -1) {
                    s.delete(s.toString().length() - 1, s.toString().length());

                } else if (s.toString().startsWith(" ")) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                } else if (s.toString().startsWith(".")) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                    s.append("0.");
                }
            }
        });

    }
}