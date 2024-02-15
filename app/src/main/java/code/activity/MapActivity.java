package code.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivityEditProfileBinding;
import com.hathme.merchat.android.databinding.ActivityMapBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import code.common.SingleShotLocationProvider;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class MapActivity extends BaseActivity implements View.OnClickListener, OnMapReadyCallback {

    ActivityMapBinding b;

    public static boolean isMapTouched = false;

    private GoogleMap mMap;

    double currentLat = 0, currentLong = 0;

    boolean isEditingAddress = false; // this will be true only when activity open

    private PlacesClient placesClient;

    private List<AutocompletePrediction> predictionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeading.setText(getString(R.string.selectLocation));

        b.tvChange.setOnClickListener(this);
        b.tvSubmit.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.tvChange:

                showSearchLocationDialog();

                break;

            case R.id.tvSubmit:


                onBackPressed();

                break;

        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));

        goToCurrentLocation();

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                currentLat = mMap.getCameraPosition().target.latitude;
                currentLong = mMap.getCameraPosition().target.longitude;

                b.llBottom.setVisibility(View.VISIBLE);

                getMarkerAddress();
                // b.included.tvPickup.setText(AppUtils.getMarkerAddress(mActivity, currentLat, currentLong));

               /* if (isAutoCompleteClicked) {

                    isAutoCompleteClicked = false;

                    if (AppSettings.getString(AppSettings.autoCompleteType).equals("2"))
                        getMarkerAddress();

                } else {

                    getMarkerAddress();
                }*/
            }
        });
    }

    private void showSearchLocationDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_auto_complete);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();

        EditText etSearch = bottomSheetDialog.findViewById(R.id.etSearch);

        RecyclerView rvList = bottomSheetDialog.findViewById(R.id.rvList);
        rvList.setLayoutManager(new GridLayoutManager(mActivity,1));
        rvList.addItemDecoration(new DividerItemDecoration(rvList.getContext(), DividerItemDecoration.VERTICAL));


        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

        Places.initialize(mActivity, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                arrayList.clear();

                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        // .setCountry("sa")
                        // .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();

                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {

                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.v("llhhhl", e.getLocalizedMessage());
                            }
                        });

                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                //List<String> suggestionsList = new ArrayList<>();


                                for (int i = 0; i < predictionList.size(); i++) {

                                    AutocompletePrediction prediction = predictionList.get(i);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("fullAdd", prediction.getFullText(null).toString());
                                    hashMap.put("primaryAdd", prediction.getPrimaryText(null).toString());
                                    arrayList.add(hashMap);

                                }

                                rvList.setAdapter(new AutocompleteAdapter(mActivity, arrayList, bottomSheetDialog));


                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {
                    arrayList.clear();
                    rvList.setAdapter(null);
                }

            }
        });

    }

    private class AutocompleteAdapter extends RecyclerView.Adapter<AutocompleteAdapter.MyViewHolder> {

        Activity activity;
        ArrayList<HashMap<String, String>> resultList;
        BottomSheetDialog bottomSheetDialog;

        public AutocompleteAdapter(Activity mActivity, ArrayList<HashMap<String, String>> suggestionsList, BottomSheetDialog bottomSheetDialog) {

            activity = mActivity;
            resultList = suggestionsList;
            this.bottomSheetDialog = bottomSheetDialog;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_autocpmplete, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AutocompleteAdapter.MyViewHolder holder, final int i) {

            holder.tvAddress.setText(resultList.get(i).get("fullAdd"));
            holder.tvSubAddress.setText(resultList.get(i).get("primaryAdd"));

            holder.llMain.setOnClickListener(v -> {

                AutocompletePrediction selectedPrediction = predictionList.get(i);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(@NonNull FetchPlaceResponse fetchPlaceResponse) {

                        Place place = fetchPlaceResponse.getPlace();
                        LatLng latLngOfPlace = place.getLatLng();

                        assert latLngOfPlace != null;
                        currentLat = latLngOfPlace.latitude;
                        currentLong = latLngOfPlace.longitude;

                        bottomSheetDialog.dismiss();
                        moveMapOnLocation(currentLat, currentLong);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        if (e instanceof ApiException) {

                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    }
                });

            });


        }

        @Override
        public int getItemCount() {
            return resultList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvAddress, tvSubAddress;
            LinearLayout llMain;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvSubAddress = itemView.findViewById(R.id.tvSubAddress);

                llMain = itemView.findViewById(R.id.llMain);
            }
        }
    }

    private void moveMapOnLocation(double latitude, double longitude) {

        if (mMap != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);

            LatLng gps = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, 15));

        }
    }

    private void goToCurrentLocation() {

        SingleShotLocationProvider.requestSingleUpdate(mActivity, (SingleShotLocationProvider.GPSCoordinates location) -> {

            if (isEditingAddress){
                currentLat = AppUtils.returnDouble(getIntent().getStringExtra("latitude"));
                currentLong = AppUtils.returnDouble(getIntent().getStringExtra("longitude"));
                isEditingAddress=false;
            }
            else {
                currentLat = location.latitude;
                currentLong = location.longitude;
            }

            moveMapOnLocation(currentLat, currentLong);

        });


    }
    private void getMarkerAddress() {

        try {

            String address = getString(R.string.na);
            String premises = getString(R.string.na);

            Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());

            List<Address> addresses = geocoder.getFromLocation(currentLat, currentLong, 1);

            if (addresses.get(0).getAddressLine(0) != null) {
                address = addresses.get(0).getAddressLine(0);
            }

            if (addresses.get(0).getSubLocality() != null) {
                premises = addresses.get(0).getSubLocality();
            }

            b.tvAddress.setText(premises);
            b.tvSubAddress.setText(address);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}