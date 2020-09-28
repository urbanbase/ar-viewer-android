package com.urbanbase.app.arviewer.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.urbanbase.app.arviewer.sample.R
import com.urbanbase.app.arviewer.sample.kotlin.ar.ARActivity
import com.urbanbase.app.arviewer.sample.base.PermissionCheckActivity
import com.urbanbase.app.arviewer.sample.common.Const
import com.urbanbase.app.arviewer.sample.kotlin.product.ProductActivity
import com.urbanbase.sdk.arviewer.ar.UBArViewer
import com.urbanbase.sdk.arviewer.ar.manager.UBAssetApiManager
import com.urbanbase.sdk.arviewer.model.network.UBAsset

class MainActivity : PermissionCheckActivity() {
    var TAG = "MainActivity"

    private var dimView: ConstraintLayout? = null
    private var asset: UBAsset? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 로딩 처리용 Dim View
        dimView = findViewById(R.id.cl_dim)

        // AR Mode 선택 시 처리
        findViewById<Button>(R.id.btn_ar_mode).setOnClickListener {
            // 로딩 한 Asset 정보가 유효한 경우 액티비티 이동
            asset?.let {
                it.asset_uuid?.let {
                    goArActivity(it)
                }
            }
        }

        // Product Mode(3DViewer) 선택 시 처리
        findViewById<Button>(R.id.btn_product_mode).setOnClickListener {
            // 로딩 한 Asset 정보가 유효한 경우 액티비티 이동
            asset?.let {
                it.asset_uuid?.let {
                    goProductActivity(it)
                }
            }
        }

        // 서비스키에 해당되는 Asset List 정보 요청
        if (UBArViewer.isSupportedAR(this)) {
            showLoading(true)
            UBAssetApiManager(this).getPrivateAssetList(
                onSuccess = {
                    // 첫번째 Asset 정보로 고정
                    asset = it?.first()
                    Log.e(TAG, "AssetData load success. [ ${it?.size} ]")
                    Log.e(TAG, "Select Asset : [ $asset ]")
                    runOnUiThread {
                        showLoading(false)
                    }
                },
                onFailure = {
                    Log.e(TAG, "AssetData load failed. [ $it ]")
                }
            )
        }
    }

    private fun showLoading(isShow: Boolean) {
        if (isShow) {
            dimView?.visibility = View.VISIBLE
        } else {
            dimView?.visibility = View.GONE
        }
    }

    private fun goArActivity(assetUUID: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            checkPermission {
                val intent = Intent(this, ARActivity().javaClass)
//                val intent = Intent(this, TestActivity().javaClass)
                intent.putExtra(Const.INTENT_EXTRA_ASSET_UUID, assetUUID)
                startActivity(intent)
            }
        }, 200)
    }

    private fun goProductActivity(assetUUID: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            checkPermission {
                val intent = Intent(this, ProductActivity().javaClass)
                intent.putExtra(Const.INTENT_EXTRA_ASSET_UUID, assetUUID)
                startActivity(intent)
            }
        }, 200)
    }
}