package code.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import code.utils.AppConstants
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.merchat.android.R
import com.hathme.merchat.android.databinding.ActivityReaviewListBinding
import org.json.JSONObject

class ReviewListActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityReaviewListBinding

    private var arrayList = ArrayList<HashMap<String, String>>()

    private lateinit var adapter: Adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaviewListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inits()
    }


    private fun inits() {

        binding.header.tvHeading.text = getString(R.string.reviewList)
        binding.header.ivBack.setOnClickListener(this)

        adapter = Adapter(arrayList)
        binding.rvList.adapter = adapter
    }

    private fun hitReviewListApi() {

        WebServices.getApi(
            mActivity,
            AppUrls.ratingReview,
            true,
            true,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {

                    parseJson(response)

                }

                override fun OnFail(response: String?) {

                }
            });
    }

    private fun parseJson(response: JSONObject?) {

        arrayList.clear()
        try {
            val jsonObject = response?.getJSONObject(AppConstants.projectName)

            if (jsonObject!!.getString(AppConstants.resCode) == "1") {


                val jsonObj = jsonObject.getJSONObject("data")
                val jsonArray = jsonObj.getJSONArray("resultData")
                for (i in 0 until jsonArray.length()) {

                    val jsonObject1 = jsonArray.getJSONObject(i)
                    val hashMap = HashMap<String, String>()
                    hashMap["_id"] = jsonObject1.getString("_id")
                    hashMap["remark"] = jsonObject1.getString("remark")
                    hashMap["rating"] = jsonObject1.getString("rating")
                    hashMap["name"] = jsonObject1.getString("name")
                    hashMap["productImage"] = jsonObject1.getString("productImage")
                    hashMap["createdAt"] = jsonObject1.getString("createdAt")
                    hashMap["orderId"] = jsonObject1.getString("orderId")
                    hashMap["productName"] = jsonObject1.getString("productName")
                    arrayList.add(hashMap)
                }
                } else {
                    AppUtils.showToastSort(
                        mActivity,
                        jsonObject.getString(AppConstants.resMsg) ?: ""
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            adapter.notifyDataSetChanged()

        }

        override fun onClick(v: View?) {
            when (v) {
                binding.header.ivBack -> {
                    onBackPressed()
                }
            }
        }

        inner class Adapter(var data: ArrayList<HashMap<String, String>>) :
            RecyclerView.Adapter<Adapter.MyViewHolder?>() {
            override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.inflate_review_list, viewGroup, false)
                return MyViewHolder(view)
            }

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

                holder.ratingBar.rating = AppUtils.returnFloat(data[position]["rating"])
                holder.tvReview.text = data[position]["remark"]
                holder.tvName.text = data[position]["name"]
                holder.tvTime.text = data[position]["createdAt"]
                holder.tvOrderId.text = "#"+data[position]["orderId"]
                holder.tvProductName.text = data[position]["productName"]
                holder.tvRatingCount.text = ""+AppUtils.returnDouble(data[position]["rating"])
                AppUtils.loadPicassoImage(data[position]["productImage"], holder.ivImage)
            }

            override fun getItemCount(): Int {
                return data.size
            }

            inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


                var ratingBar: RatingBar
                var tvReview: TextView
                var tvName: TextView
                var tvTime: TextView
                var tvProductName: TextView
                var tvOrderId: TextView
                var tvRatingCount: TextView
                var ivImage: ImageView

                init {

                    ratingBar = itemView.findViewById(R.id.ratingBar)
                    tvReview = itemView.findViewById(R.id.tvReview)
                    tvName = itemView.findViewById(R.id.tvName)
                    tvRatingCount = itemView.findViewById(R.id.tvRatingCount)
                    tvTime = itemView.findViewById(R.id.tvTime)
                    tvProductName = itemView.findViewById(R.id.tvProductName)
                    tvOrderId = itemView.findViewById(R.id.tvOrderId)
                    ivImage = itemView.findViewById(R.id.ivImage)
                }
            }
        }

        override fun onResume() {
            super.onResume()
            hitReviewListApi()

        }
    }