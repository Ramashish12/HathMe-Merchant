package code.activity;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.BuildConfig;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityEditProfileBinding;
import com.hathme.merchat.android.databinding.ActivityStoreViewBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class EditProfileActivity extends BaseActivity implements View.OnClickListener {

    ActivityEditProfileBinding b;

    private String  encodedProfile = "", picturePath="";

    private static final int selectPicture = 1, capturePicture = 100;

    private Uri fileUri;

    private int from = 0;

    boolean  isProfileUploaded = false;
    private String isFrom = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivProfile.setOnClickListener(this);
        b.tvSave.setOnClickListener(this);

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeading.setText(getString(R.string.editPersonalDetails));

        AppUtils.checkAndRequestPermissions(mActivity);
        from = 0;
        hitGetProfileApi();
    }

    private void hitGetProfileApi() {

        WebServices.getApi(mActivity, AppUrls.myProfile, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseGetDetail(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject jsonData = jsonObject.getJSONObject("data");
                b.etFullName.setText(jsonData.getString("name"));
                b.etMobile.setText(jsonData.getString("mobile"));
                b.etEmailId.setText(jsonData.getString("email"));

                    if (!jsonData.getString("profileImage").isEmpty()){
                        isProfileUploaded=true;
                        AppUtils.loadPicassoImage(jsonData.getString("profileImage"), b.ivProfile);
                        b.ivProfile.setPadding(0,0,0,0);
                    }




            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.register),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){



            case R.id.ivProfile:

                from = 2;

//                if (AppUtils.checkAndRequestPermissions(mActivity)) {
                    AlertCameraGallery();
//                } else {
//                    AppUtils.showToastSort(mActivity, getString(R.string.provideRequiredPermission));
//                }
                break;


            case R.id.tvSave:

                validate();

                break;


        }

    }

    private void validate(){

        if (!isProfileUploaded){
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseUploadProfilePhoto));
        }
        else if (b.etFullName.getText().toString().trim().isEmpty()){
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterFullName));
        }
        else if (!AppUtils.isEmailValid(b.etEmailId.getText().toString().trim())){
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectEmail));
        }

        else{
            isFrom = "Update";
            hitUpdateProfileApi();
        }

    }

    private void hitUpdateProfileApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("name", b.etFullName.getText().toString().trim());
            jsonObject.put("email", b.etEmailId.getText().toString().trim());
//            jsonObject.put("mobile", b.etMobile.getText().toString().trim());
            jsonObject.put("profileImage", encodedProfile);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.UpdateProfile,
                json,
                true,
                true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAddProfile(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseAddProfile(JSONObject response) {


        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");
                if (isFrom.equals("Update"))
                {
                    AppSettings.putString(AppSettings.isUpdateImage, isFrom);
                    AppSettings.putString(AppSettings.updatedProfileImage,jsonData.getString("profileImage"));
                }

                Log.d("data", jsonData.toString());
                AppUtils.showMessageDialog(mActivity, getString(R.string.personalDetails),
                        jsonObject.getString(AppConstants.resMsg), 1);

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.personalDetails),
                        jsonObject.getString(AppConstants.resMsg), 2);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            fileUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".provider",AppUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE));
            AppSettings.putString(AppSettings.imagePath, String.valueOf(fileUri));
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(it, capturePicture);
        }
        else {
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

                Matrix matrix = new Matrix();
                matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                byte[] ba = bao.toByteArray();

             if (from == 2) {
                    encodedProfile = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                    b.ivProfile.setImageBitmap(rotatedBitmap);
                    isProfileUploaded = true;
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

                    Matrix matrix = new Matrix();
                    matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                    rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                    byte[] ba = bao.toByteArray();


                  if (from == 2) {
                        encodedProfile = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                        b.ivProfile.setImageBitmap(rotatedBitmap);
                        isProfileUploaded = true;
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