package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.hathme.merchat.android.R;
import com.hathme.merchat.android.databinding.ActivitySalesInsightBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import code.common.CustomMarkerView;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class SalesInsightActivity extends BaseActivity implements View.OnClickListener {

    ActivitySalesInsightBinding b;

    private int type = 1;
    private String dateTimeOrMonth = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySalesInsightBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {
        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeading.setText(getString(R.string.salesInsight));
        b.ll1D.setOnClickListener(this);
        b.ll1W.setOnClickListener(this);
        b.ll1M.setOnClickListener(this);
        b.ll1Y.setOnClickListener(this);
        b.llAddProducts.setOnClickListener(this);
        initialiseChart();

        hitGetInsightApi();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ll1D:

                type = 1;
                setDefault();
                hitGetInsightApi();
                b.view1D.setVisibility(View.VISIBLE);

                break;
            case R.id.llAddProducts:
                AppSettings.putString(AppSettings.isFrom, "Add");
                startActivity(new Intent(mActivity, AddProductActivity.class));
                break;
            case R.id.ll1W:
                type = 2;
                hitGetInsightApi();
                setDefault();

                b.view1W.setVisibility(View.VISIBLE);
                break;

            case R.id.ll1M:
                type = 3;

                hitGetInsightApi();
                setDefault();

                b.view1M.setVisibility(View.VISIBLE);
                break;

            case R.id.ll1Y:
                type = 4;

                hitGetInsightApi();
                setDefault();

                b.view1Y.setVisibility(View.VISIBLE);
                break;


        }

    }

    private void setDefault() {

        b.view1D.setVisibility(View.INVISIBLE);
        b.view1W.setVisibility(View.INVISIBLE);
        b.view1M.setVisibility(View.INVISIBLE);
        b.view1Y.setVisibility(View.INVISIBLE);

    }

    private void hitGetInsightApi() {

        String url = "";

        switch (type) {

            case 1:

                url = AppUrls.graphDataByDay;

                break;
            case 2:

                url = AppUrls.graphDataByWeek;

                break;
            case 3:

                url = AppUrls.graphDataByMonth;

                break;
            case 4:

                url = AppUrls.graphDataByYear;

                break;

        }

        WebServices.getApi(mActivity, url, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSalesInsight(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseSalesInsight(JSONObject response) {

        b.lineChart.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject data = jsonObject.getJSONObject("data");

                b.tvDeliveredOrders.setText(data.getString("totalDelivered") + " " + getString(R.string.orders));
                b.tvRejectedOrders.setText(data.getString("totalRejected") + " " + getString(R.string.orders));
                b.tvTotalRevenue.setText(data.getString("totalRevenue"));
                b.tvLossCancellation.setText(data.getString("totalLoss"));
                setChart(data.getJSONArray("result"));

//                setData(data.getJSONArray("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setChart(JSONArray jsonArray) {

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        ArrayList<Entry> receivedArray = new ArrayList<>();
        ArrayList<Entry> completedArray = new ArrayList<>();
        ArrayList<Entry> rejectedArray = new ArrayList<>();

        b.lineChart.clear();

        if (jsonArray.length() != 0) {

            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (type == 1) {

                        dateTimeOrMonth = jsonObject.getString("hour");
                        setFormattedHour();

                    } else if (type == 2) {

                        dateTimeOrMonth = jsonObject.getString("day");
                        dateTimeOrMonth = AppUtils.parseDateTime(dateTimeOrMonth);

                    } else if (type == 3) {

                        dateTimeOrMonth = jsonObject.getString("day");
                        dateTimeOrMonth = AppUtils.parseDateTime(dateTimeOrMonth);

                    } else if (type == 4) {
                        dateTimeOrMonth = jsonObject.getString("month");
                        dateTimeOrMonth = AppUtils.parseMonthOnly(dateTimeOrMonth);
                    }
                    receivedArray.add(new Entry(i, AppUtils.returnFloat(jsonObject.getString("receivedOrder")), dateTimeOrMonth));

                    completedArray.add(new Entry(i, AppUtils.returnFloat(jsonObject.getString("completedOrder")), dateTimeOrMonth));

                    rejectedArray.add(new Entry(i, AppUtils.returnFloat(jsonObject.getString("rejectedOrder")), dateTimeOrMonth));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            LineDataSet d = new LineDataSet(receivedArray, "");
            d.setColor(getResources().getColor(R.color.colorPrimary));
//          d.setCircleColor(getResources().getColor(R.color.colorPrimary));
            d.setCircleHoleColor(Color.YELLOW);
            d.getEntryForIndex(receivedArray.size() - 1).setIcon(getDrawable(R.drawable.circle_for_chart_yellow));
            setLineData(d);

            dataSets.add(d);

            LineDataSet d1 = new LineDataSet(completedArray, "");
            d1.setColor(getResources().getColor(R.color.green));
            d1.setCircleHoleColor(Color.GREEN);
            d1.getEntryForIndex(receivedArray.size() - 1).setIcon(getDrawable(R.drawable.circle_for_chart));
            setLineData(d1);
            dataSets.add(d1);

            LineDataSet d2 = new LineDataSet(rejectedArray, "");
            d2.setColor(getResources().getColor(R.color.red));
            setLineData(d2);
            d2.setCircleHoleColor(Color.RED);
            d2.getEntryForIndex(receivedArray.size() - 1).setIcon(getDrawable(R.drawable.circle_for_chart_red));
            dataSets.add(d2);

            Legend legend = b.lineChart.getLegend();
            legend.setEnabled(false);

            LineData data = new LineData(dataSets);

            b.lineChart.setData(data);
            b.lineChart.invalidate();


//            setDataSet(entryArrayList);

        }
    }

    private void setFormattedHour() {
        if (AppUtils.returnInt(dateTimeOrMonth) < 12) {
            dateTimeOrMonth = dateTimeOrMonth + " AM";
        } else if (AppUtils.returnInt(dateTimeOrMonth) == 12) {
            dateTimeOrMonth = dateTimeOrMonth + " PM";
        } else if (AppUtils.returnInt(dateTimeOrMonth) == 24) {
            dateTimeOrMonth = (AppUtils.returnInt(dateTimeOrMonth) - 12) + " AM";

        } else {
            dateTimeOrMonth = (AppUtils.returnInt(dateTimeOrMonth) - 12) + " PM";
        }
    }

    private void setLineData(LineDataSet lineDataSet) {

        lineDataSet.setLineWidth(3f);

        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.2f);
        lineDataSet.setDrawFilled(false);
        lineDataSet.setFillColor(Color.WHITE);
        lineDataSet.setFillAlpha(80);
        lineDataSet.setDrawCircles(false);

    }


    private void setMaxChart(int maxOrder) {


        /*YAxis leftAxis = b.lineChart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaximum(maxOrder+10);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);*/

        YAxis rightAxis = b.lineChart.getAxisRight();
        rightAxis.setTextColor(Color.RED);
        rightAxis.setAxisMaximum(maxOrder + 10);
        rightAxis.setAxisMinimum(0);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setGranularityEnabled(false);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initialiseChart() {

        b.lineChart.setTouchEnabled(true);
        b.lineChart.setDragEnabled(true);
        b.lineChart.setScaleEnabled(false);
        b.lineChart.setPinchZoom(false);

        // Set a custom MarkerView to display values on click

        b.lineChart.getDescription().setEnabled(false);

        b.lineChart.setMaxHighlightDistance(240);
        b.lineChart.setViewPortOffsets(0, 0, 0, 0);
        b.lineChart.getAxisLeft().setDrawGridLines(false);
        b.lineChart.getAxisRight().setDrawGridLines(false);
        b.lineChart.getXAxis().setDrawGridLines(false);
        b.lineChart.getXAxis().setDrawAxisLine(false);
        b.lineChart.setBackgroundColor(getResources().getColor(R.color.white)); //set whatever color you prefer
        b.lineChart.setDrawGridBackground(false);// this is a must
        b.lineChart.getXAxis().setSpaceMax(17);

        Description description = new Description();
        description.setText("");

        b.lineChart.setDescription(description);


        YAxis y = b.lineChart.getAxisRight();
        y.setLabelCount(6, false);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.WHITE);
        CustomMarkerView markerView = new CustomMarkerView(mActivity, R.layout.custom_marker_view, dateTimeOrMonth);
        b.lineChart.setMarker(markerView);
    }
/*
    @SuppressLint("ClickableViewAccessibility")
    private void initialiseChart() {

//        b.lineChart.setOnChartValueSelectedListener(this);

        // no description text
        b.lineChart.getDescription().setEnabled(false);

        // enable touch gestures
        b.lineChart.setTouchEnabled(true);

        b.lineChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        b.lineChart.setDragEnabled(true);
        b.lineChart.setScaleEnabled(false);
        b.lineChart.setDrawGridBackground(false);
        b.lineChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        b.lineChart.setPinchZoom(true);

        // set an alternative background color
        b.lineChart.setBackgroundColor(Color.WHITE);


        b.lineChart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend l = b.lineChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);


        XAxis xAxis = b.lineChart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

    }
*/

}