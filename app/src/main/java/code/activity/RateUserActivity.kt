package code.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import code.utils.AppConstants
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.merchat.android.R
import com.hathme.merchat.android.databinding.ActivityRateUserBinding
import org.json.JSONException
import org.json.JSONObject

class RateUserActivity : BaseActivity(),View.OnClickListener{
    lateinit var b: ActivityRateUserBinding
    var orderId = ""
    var customerProfile = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRateUserBinding.inflate(layoutInflater)
        setContentView(b.root)
        inite()

    }

    private fun inite() {
        orderId = intent.getStringExtra("orderId")!!
        customerProfile = intent.getStringExtra("customerProfile")!!
        b.header.tvHeading.text = getString(R.string.rateCustomer)
        b.header.ivBack.setOnClickListener(this)
        b.tvContinue.setOnClickListener(this)
        b.ratingBar.setOnClickListener(this)
        b.tvUserName.text = intent.getStringExtra("customerName")!!
        AppUtils.loadPicassoImage(intent.getStringExtra("customerProfile"), b.ivProfile)

    }

    override fun onClick(v: View?) {
        when(v)
        {
            b.header.ivBack->{
                onBackPressed()
            }
           b.tvContinue->{
               validate()
           }
        }
    }
    private fun validate() {
        if (b.ratingBar.rating == 0f) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseRate))
        }
        else if (b.etDescription.text.toString() == "")
        {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterDescription))
        }
        else {
            hitRateCustomerApi(b.ratingBar.rating,b.etDescription.text.toString())
        }
    }
    private fun hitRateCustomerApi(rating: Float, description: String) {
        val jsonObject = JSONObject()
        val json = JSONObject()
        try {
            jsonObject.put("orderId", orderId)
            jsonObject.put("remark", description)
            jsonObject.put("rating", rating.toDouble())
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        WebServices.postApi(
            mActivity,
            AppUrls.rateCustomer,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseJson(response)
                }

                override fun OnFail(response: String) {
                    AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),response,9)
                }
            })
    }
    private fun parseJson(response: JSONObject) {
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
                val intent = Intent(
                    mActivity,
                    MainActivity::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                overridePendingTransitionExit()
                finish()
                AppUtils.showResMsgToastSort(mActivity, jsonObject)
            } else {
                AppUtils.showMessageDialog(
                    mActivity,
                    getString(R.string.app_name),
                    jsonObject.getString(AppConstants.resMsg),
                    2
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}