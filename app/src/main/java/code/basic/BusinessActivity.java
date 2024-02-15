package code.basic;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.BuildConfig;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityBusinessBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import code.activity.MainActivity;
import code.common.SingleShotLocationProvider;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class BusinessActivity extends BaseActivity implements View.OnClickListener {

    ActivityBusinessBinding b;

    private Set<String> arrayMode = new HashSet<>();

    private String encodedImage1 = "", picturePath = "", categoryId = "";

    private static final int selectPicture = 1, capturePicture = 100;

    private Uri fileUri;

    private ArrayList<HashMap<String, String>> arrayListImages = new ArrayList<>();

    private AdapterImages adapterImages;

    private double currentLat = 0, currentLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityBusinessBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeading.setText(getString(R.string.yourBusinessDetails));

        b.rlHome.setOnClickListener(this);
        b.rlDineIn.setOnClickListener(this);
        b.rlTakeaway.setOnClickListener(this);

        b.tvYear.setOnClickListener(this);
        b.tvStartTime.setOnClickListener(this);
        b.tvEndTime.setOnClickListener(this);
        b.tvAddImage.setOnClickListener(this);
        b.tvSave.setOnClickListener(this);

        adapterImages = new AdapterImages(arrayListImages);
        b.rvImages.setAdapter(adapterImages);

        hitGetUploadedImagesApi(false);
        hitGetBusinessDetails();


        locationUpdate();

    }

    private void locationUpdate() {

        if (!AppUtils.checkLocationPermissions(mActivity)) {

            AppUtils.requestLocationPermissions(mActivity);

        }
        //If permission granted but Gps not enabled
        else if (!AppUtils.checkIfGpsEnable(mActivity)) {

            turnOnGps();
        } else {

            getLocation();

        }
    }

    private void getLocation() {

        SingleShotLocationProvider.requestSingleUpdate(mActivity, (SingleShotLocationProvider.GPSCoordinates location) -> {
            currentLat = location.latitude;
            currentLong = location.longitude;
        });
    }

    private void hitGetBusinessDetails() {

        WebServices.getApi(mActivity, AppUrls.GetBusinessDetails, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseBusinessDetails(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseBusinessDetails(JSONObject response) {

        arrayMode.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                jsonObject = jsonObject.getJSONObject("data");

                b.etName.setText(jsonObject.getString("businessName"));
                b.tvYear.setText(jsonObject.getString("establishmentYear"));
                b.etAddress.setText(jsonObject.getString("address"));
                b.etLandmark.setText(jsonObject.getString("landmark"));
                b.etPinCode.setText(jsonObject.getString("pincode"));
                b.tvStartTime.setText(jsonObject.getString("startTime"));
                b.tvEndTime.setText(jsonObject.getString("endTime"));
                b.etGst.setText(jsonObject.getString("GSTIN"));
                b.etFssai.setText(jsonObject.getString("FSSAI"));
                b.etLicenseNo.setText(jsonObject.getString("licenseNo"));
                categoryId = jsonObject.getString("categoryId");

                String[] mode = jsonObject.getString("mode").split(",");

                arrayMode.addAll(Arrays.asList(mode));
                setMode();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rlHome:

                if (arrayMode.contains("1")) arrayMode.remove("1");
                else arrayMode.add("1");

                setMode();

                break;

            case R.id.rlTakeaway:

                if (arrayMode.contains("2")) arrayMode.remove("2");
                else arrayMode.add("2");

                setMode();

                break;

            case R.id.rlDineIn:

                if (arrayMode.contains("3")) arrayMode.remove("3");
                else arrayMode.add("3");
                setMode();

                break;

            case R.id.tvYear:

                AppUtils.showDateDialog(b.tvYear, mActivity);

                break;

            case R.id.tvStartTime:
                showTimePicker("1", b.tvStartTime, b.tvEndTime);
                break;

            case R.id.tvEndTime:
                showTimePicker("2", b.tvStartTime, b.tvEndTime);
                break;

            case R.id.tvAddImage:

                if (AppUtils.checkAndRequestPermissions(mActivity)) {
                    AlertCameraGallery();
                } else {
                    AppUtils.showToastSort(mActivity, getString(R.string.provideRequiredPermission));
                }

                break;

            case R.id.tvSave:

                validate();

                break;

        }

    }

    private void setMode() {

        if (arrayMode.contains("1")) {
            b.ivHomeDelivery.setImageResource(R.drawable.ic_radio_button_checked);
        } else {
            b.ivHomeDelivery.setImageResource(R.drawable.ic_radio_button_unchecked);
        }

        if (arrayMode.contains("2")) {
            b.ivTakeaway.setImageResource(R.drawable.ic_radio_button_checked);
        } else {
            b.ivTakeaway.setImageResource(R.drawable.ic_radio_button_unchecked);
        }

        if (arrayMode.contains("3")) {
            b.ivvDineIn.setImageResource(R.drawable.ic_radio_button_checked);
        } else {
            b.ivvDineIn.setImageResource(R.drawable.ic_radio_button_unchecked);
        }
    }

    private void validate() {

        if (b.etName.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterBusinessName));
        } else if (b.tvYear.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterEstYear));
        } else if (b.etAddress.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterAddress));
        } else if (b.etAddress.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterAddress));
        } else if (b.etLandmark.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterLandmark));
        } else if (b.etPinCode.getText().toString().trim().length() < 6) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPinCode));
        } else if (b.tvStartTime.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectStartTime));
        } else if (b.tvEndTime.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectEndTime));
        } else if (!b.etGst.getText().toString().isEmpty() && !isValidGst(b.etGst.getText().toString().trim())) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectGst));
        } else if (arrayListImages.size() == 0) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseUploadImage));
        } else if (currentLat == 0) {
            AppUtils.showToastSort(mActivity, getString(R.string.gettingLocation));
            locationUpdate();

        } else {
            hitSaveBusinessApi();
        }
    }

    private boolean isValidGst(String str) {

        String regex = "^[0-9]{2}[A-Z]{5}[0-9]{4}"
                + "[A-Z]{1}[1-9A-Z]{1}"
                + "Z[0-9A-Z]{1}$";

        Pattern p = Pattern.compile(regex);

        if (str == null) {
            return false;
        }

        Matcher m = p.matcher(str);
        return m.matches();
    }

    private void hitSaveBusinessApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("businessName", b.etName.getText().toString().trim());
            jsonObject.put("establishmentYear", b.tvYear.getText().toString().trim());
            jsonObject.put("address", b.etAddress.getText().toString().trim());
            jsonObject.put("landmark", b.etLandmark.getText().toString().trim());
            jsonObject.put("pincode", b.etPinCode.getText().toString().trim());
            jsonObject.put("startTime", b.tvStartTime.getText().toString().trim());
            jsonObject.put("endTime", b.tvEndTime.getText().toString().trim());
            jsonObject.put("GSTIN", b.etGst.getText().toString().trim());
            jsonObject.put("FSSAI", b.etFssai.getText().toString().trim());
            jsonObject.put("licenseNo", b.etLicenseNo.getText().toString().trim());
//            jsonObject.put("mode", String.valueOf(arrayMode.toString().replace("[", "").replace("]", "").replace(" ", "")));
            jsonObject.put("mode", "");

            jsonObject.put("latitude", String.valueOf(currentLat));
            jsonObject.put("longitude", String.valueOf(currentLong));
            if (getIntent().hasExtra("categoryId"))
                jsonObject.put("categoryId", getIntent().getStringExtra("categoryId"));
            else
                jsonObject.put("categoryId", categoryId);


            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls. EnterBusinessDetails, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSaveJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseSaveJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                if (getIntent().hasExtra("pageFrom")) {

                    startActivity(new Intent(mActivity, MainActivity.class));

                } else {
                    AppSettings.putString(AppSettings.isBusinessSave, "1");
                    AppSettings.putString(AppSettings.isCategorySelected, "1");

                    startActivity(new Intent(mActivity, MainActivity.class));

                }
                finishAffinity();
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.businessDetails), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    private void showTimePicker(String type, TextView tvStartTime, TextView tvEndTime) {

        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(mActivity, (timePicker, selectedHour, selectedMinute) -> {

            String hour1 = String.valueOf(selectedHour);
            String mnt = String.valueOf(selectedMinute);

            if (selectedHour < 10) hour1 = "0" + selectedHour;
            if (selectedMinute < 10) mnt = "0" + selectedMinute;

            if (type.equals("1")) {
                tvStartTime.setText(AppUtils.changeHrFormat(hour1 + ":" + mnt));
                tvEndTime.setText("");
            } else {

                int hourInt = Integer.parseInt(tvStartTime.getText().toString().split(":")[0]);
                int mntInt = Integer.parseInt(tvStartTime.getText().toString().split(":")[1].split(" ")[0]);
                tvEndTime.setText(AppUtils.changeHrFormat(hour1 + ":" + mnt));
//                validateTime(tvStartTime.getText().toString(),AppUtils.changeHrFormat(hour1 + ":" + mnt),tvEndTime);
              /*  if (selectedHour < hourInt) {

                    AppUtils.showToastSort(mActivity, getString(R.string.endTimeShouldBeGreater));

                } else if (selectedHour == hourInt && selectedMinute == mntInt) {

                    AppUtils.showToastSort(mActivity, getString(R.string.endTimeShouldBeGreater));
                } else {
                    tvEndTime.setText(AppUtils.changeHrFormat(hour1 + ":" + mnt));
                }*/

            }

        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();

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

        fileUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".provider", Objects.requireNonNull(AppUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE)));
        AppSettings.putString(AppSettings.imagePath, String.valueOf(fileUri));
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(it, capturePicture);
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

                encodedImage1 = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);

                hitUploadImageApi();
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

                    encodedImage1 = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);

                    hitUploadImageApi();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                Toast.makeText(mActivity, "Unable to Select the Image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void hitUploadImageApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("image", encodedImage1);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.UploadBusinessImage, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseUploadImage(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseUploadImage(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                new Handler(Looper.myLooper()).postDelayed(() -> {

                    hitGetUploadedImagesApi(true);

                }, 1500);

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.register), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitGetUploadedImagesApi(boolean b) {


        WebServices.getApi(mActivity, AppUrls.GetBusinessImage, b, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetImages(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseGetImages(JSONObject response) {

        arrayListImages.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("imageUrl", jsonObject1.getString("imageUrl"));

                    arrayListImages.add(hashMap);
                }
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.forgotPassword), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterImages.notifyDataSetChanged();
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
        public void onBindViewHolder(@NonNull AdapterImages.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            AppUtils.loadPicassoImage(data.get(position).get("imageUrl"), holder.ivImage);

            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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

        WebServices.deleteApi(mActivity, AppUrls.RemoveBusinessImage, json, true, true, new WebServicesCallback() {

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

                hitGetUploadedImagesApi(true);

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.products), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void turnOnGps() {

        LocationRequest locationRequest = LocationRequest.create().setInterval(1000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        LocationServices.getSettingsClient(this).checkLocationSettings(builder.build()).addOnSuccessListener(this, (LocationSettingsResponse response) -> {

            getLocation();
        }).addOnFailureListener(this, ex -> {
            if (ex instanceof ResolvableApiException) {
                // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) ex;
                    resolvable.startResolutionForResult(mActivity, 2);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    private void validateTime(String strStartTime, String strEndTime, TextView tvEndTime) {
// Convert the input strings to time values (you might need to adjust this depending on your format)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date startTime, endTime;
        try {
            startTime = sdf.parse(strStartTime);
            endTime = sdf.parse(strEndTime);

            // Compare start and end times
            if (endTime.before(startTime)) {
                // Display an error message
                Toast.makeText(getApplicationContext(), "End time cannot be less than start time", Toast.LENGTH_SHORT).show();
                // Exit the method or event handler
            } else {
                tvEndTime.setText(strEndTime);
            }
            // Continue with your logic if the times are valid

        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the parsing exception if needed
        }

    }
}
