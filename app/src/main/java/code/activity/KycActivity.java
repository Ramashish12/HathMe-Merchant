package code.activity;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.BuildConfig;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityKycBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import code.common.AdapterSpinnerHashMap;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class KycActivity extends BaseActivity implements View.OnClickListener {

    private ActivityKycBinding b;
    //String
    private String encodedImageAadharFront = "", encodedImageAadharBack = "", picturePath = "";

    //GalleryCamera
    private static final int selectPicture = 1;
    private static final int capturePicture = 100;

    private Uri fileUri;

    private int from = 0;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityKycBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
        AppUtils.checkAndRequestPermissions(mActivity);
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(this);
        b.header.tvHeading.setText(getString(R.string.verification));
        b.btnSubmit.setOnClickListener(this);
        b.ivClickAadharBack.setOnClickListener(this);
        b.ivClickAadharFront.setOnClickListener(this);

        hitGetDocApi();

        for (int i = 0; i < b.llMain.getChildCount(); i++) {
            View view = b.llMain.getChildAt(i);
            view.setEnabled(false); // Or whatever you want to do with the view.
        }

        if (AppSettings.getString(AppSettings.documentStatus).isEmpty() || AppSettings.getString(AppSettings.documentStatus).equals("0")) {
            for (int i = 0; i < b.llMain.getChildCount(); i++) {
                View view = b.llMain.getChildAt(i);
                view.setEnabled(true); // Or whatever you want to do with the view.
            }
        } else {
            b.etInput.setText(AppSettings.getString(AppSettings.documentNumber));

            AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.documentFrontImage), b.ivAadharFrontPicture);
            AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.documentBackImage), b.ivAadharBackPicture);

            //Rejected
            if (AppSettings.getString(AppSettings.documentStatus).equals("2")) {
                b.btnSubmit.setVisibility(View.VISIBLE);
                for (int i = 0; i < b.llMain.getChildCount(); i++) {
                    View view = b.llMain.getChildAt(i);
                    view.setEnabled(true); // Or whatever you want to do with the view.
                }
            } else {
                b.btnSubmit.setVisibility(View.INVISIBLE);

                b.ivClickAadharBack.setVisibility(View.INVISIBLE);
                b.ivClickAadharFront.setVisibility(View.INVISIBLE);
            }
        }

        switch (AppSettings.getString(AppSettings.documentStatus)) {

            case "0":

                b.spinnerDocs.setEnabled(true);

                break;

            case "3":
                b.tvStatus.setText(getString(R.string.approved));
                b.tvStatus.setTextColor(getResources().getColor(R.color.green));
                b.spinnerDocs.setEnabled(false);

                break;
            case "1":
                b.tvStatus.setText(getString(R.string.underProcess));
                b.tvStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                b.spinnerDocs.setEnabled(false);

                break;
            case "2":
                b.tvStatus.setText(getString(R.string.rejected));
                b.tvStatus.setTextColor(getResources().getColor(R.color.red));
                b.spinnerDocs.setEnabled(true);

                break;
        }
    }

    private void hitGetDocApi() {

        WebServices.getApi(mActivity, AppUrls.GetDocumentList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetDocs(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseGetDocs(JSONObject response) {

        arrayList.clear();
        int spinnerPos = 0;

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("name"));

                    arrayList.add(hashMap);

                    if (AppSettings.getString(AppSettings.documentId).equals(jsonObject1.getString("_id"))) {
                        spinnerPos = i;
                    }
                }

            } else
                AppUtils.showResMsgToastSort(mActivity, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        b.spinnerDocs.setAdapter(new AdapterSpinnerHashMap(mActivity, R.layout.adapter_spinner, arrayList));

        b.spinnerDocs.setSelection(spinnerPos);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.ivClickAadharBack:

                from = 4;
                AlertCameraGallery();

                break;

            case R.id.ivClickAadharFront:

                from = 3;
                AlertCameraGallery();

                break;

            case R.id.btnSubmit:

                validate();

                break;

            case R.id.ivBack:

                onBackPressed();

                break;
        }
    }

    private void validate() {

        if (b.etInput.getText().toString().isEmpty()) {
            b.etInput.requestFocus();
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterIdNum));
        } /*else if (!validateAadharNumber(b.etInput.getText().toString())) {
            b.etInput.requestFocus();
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterProperAadhar));
        } */ else if (encodedImageAadharFront.isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.selectIdFront));
        } else if (encodedImageAadharBack.isEmpty())
            AppUtils.showToastSort(mActivity, getString(R.string.selectIdBack));
        else if (AppUtils.isNetworkAvailable(mActivity)) {
            hitVerificationApi();
        } else {
            AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));
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

        if (from == 1) {
            llGallery.setVisibility(View.GONE);
        }

        rlCancel.setOnClickListener(v -> dialog.dismiss());

        llCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                captureImage();
                dialog.dismiss();
            }
        });

        llGallery.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //photoPickerIntent.putExtra("crop", "true");
            startActivityForResult(photoPickerIntent, selectPicture);

            dialog.dismiss();
        });


    }

    private void captureImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".provider", AppUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE));
            AppSettings.putString(AppSettings.imagePath, String.valueOf(fileUri));
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (from == 1) {
                it.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            }
            it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(it, capturePicture);
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = AppUtils.getOutputMediaFileUri(MEDIA_TYPE_IMAGE, mActivity); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
            if (from == 1) {
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            }
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
                    if (!fileUri.equals(""))
                        picturePath = fileUri.getPath();
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

                if (bitmap != null) {

                    Matrix matrix = new Matrix();
                    matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                    rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                    byte[] ba = bao.toByteArray();

                    if (from == 3) {
                        encodedImageAadharFront = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                        b.ivAadharFrontPicture.setImageBitmap(rotatedBitmap);
                        b.ivClickAadharFront.setVisibility(View.GONE);
                    } else if (from == 4) {
                        encodedImageAadharBack = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                        b.ivAadharBackPicture.setImageBitmap(rotatedBitmap);
                        b.ivClickAadharBack.setVisibility(View.GONE);
                    }

                }
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

                    if (bitmap != null) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                        byte[] ba = bao.toByteArray();

                        if (from == 3) {
                            encodedImageAadharFront = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                            b.ivAadharFrontPicture.setImageBitmap(rotatedBitmap);
                            b.ivClickAadharFront.setVisibility(View.GONE);
                        } else if (from == 4) {
                            encodedImageAadharBack = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                            b.ivAadharBackPicture.setImageBitmap(rotatedBitmap);
                            b.ivClickAadharBack.setVisibility(View.GONE);
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                Toast.makeText(mActivity, "Unable to Select the Image", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void hitVerificationApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("documentId", arrayList.get(b.spinnerDocs.getSelectedItemPosition()).get("id"));
            jsonObject.put("number", b.etInput.getText().toString().trim());
            jsonObject.put("frontImage", encodedImageAadharFront);
            jsonObject.put("backImage", encodedImageAadharBack);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.UploadDocuments, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 1);

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}