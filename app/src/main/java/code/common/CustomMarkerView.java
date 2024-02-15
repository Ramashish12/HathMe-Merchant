package code.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.hathme.merchat.android.R;

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;
    private String date;
    public CustomMarkerView(Context context, int layoutResource,String date) {

        super(context, layoutResource);
        this.date = date;
        tvContent = findViewById(R.id.tvContent);
    }

    // This method will be called every time the MarkerView is redrawn
    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Display the value at the clicked position
        tvContent.setText(e.getData()+" - "+e.getY());

        super.refreshContent(e, highlight);
    }

    // Optional: Customize the position of the MarkerView
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
