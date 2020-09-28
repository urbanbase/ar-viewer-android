package com.urbanbase.app.arviewer.sample.kotlin.product.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.urbanbase.app.arviewer.sample.R
import com.urbanbase.sdk.arviewer.model.network.UBAssetStyle

class StyleItemAdapter(
    val activity: Activity,
    val itemclick: ((UBAssetStyle) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var assetStyleList: ArrayList<UBAssetStyle> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val baseView: View =
            LayoutInflater.from(activity).inflate(R.layout.item_product_style, parent, false)

        return HimartAssetStyleViewHolder(baseView, itemclick)
    }

    override fun getItemCount(): Int {
        return assetStyleList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val assetStyleData = assetStyleList[position]
        if (holder is HimartAssetStyleViewHolder) {
            if (!assetStyleData.color_chip_path.isNullOrEmpty()) {
                Glide.with(activity)
                    .load(assetStyleData.color_chip_path)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.styleImageView)
            }

            holder.styleNameTextView.text = assetStyleData.name
            holder.assetStyleItem = assetStyleData
        }
    }

    fun setItems(itemList: List<UBAssetStyle>) {
        assetStyleList.clear()
        assetStyleList.addAll(itemList)

        notifyDataSetChanged()
    }

    class HimartAssetStyleViewHolder(
        itemView: View,
        val itemclick: ((UBAssetStyle) -> Unit)? = null
    ) : RecyclerView.ViewHolder(itemView) {
        var assetStyleItem: UBAssetStyle? = null

        var styleWrapper: FrameLayout = itemView.findViewById(R.id.fl_wrapper)
        var styleImageView: ImageView = itemView.findViewById(R.id.iv_color)
        var styleNameTextView: TextView = itemView.findViewById(R.id.tv_name)

        init {
            itemView.setOnClickListener {
                assetStyleItem?.let {
                    Log.d("HimartDemo", "Select asset style : ${it.name}")
                    itemclick?.invoke(it)
                }
            }
        }
    }
}