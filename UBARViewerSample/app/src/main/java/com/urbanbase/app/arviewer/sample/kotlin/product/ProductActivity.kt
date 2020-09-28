package com.urbanbase.app.arviewer.sample.kotlin.product

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.urbanbase.app.arviewer.sample.R
import com.urbanbase.app.arviewer.sample.common.Const
import com.urbanbase.app.arviewer.sample.kotlin.product.adapter.StyleItemAdapter
import com.urbanbase.app.arviewer.sample.kotlin.product.adapter.StyleItemDecoration
import com.urbanbase.sdk.arviewer.ar.UBProductViewer
import com.urbanbase.sdk.arviewer.ar.listener.OnLoadListener
import com.urbanbase.sdk.arviewer.ar.manager.FigureLoadManager
import com.urbanbase.sdk.arviewer.ar.manager.UBAssetApiManager
import com.urbanbase.sdk.arviewer.ar.node.Figure
import com.urbanbase.sdk.arviewer.model.network.UBAssetInfo

class ProductActivity : AppCompatActivity() {

    var TAG = "ProductActivity"
    private var assetUUID: String? = null
    private var mAssetInfo: UBAssetInfo? = null

    private var ubProductViewer: UBProductViewer? = null
    private var styleRecyclerView: RecyclerView? = null
    private var styleAdapter: StyleItemAdapter? = null
    private var selectedStyleTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        assetUUID = intent.getStringExtra(Const.INTENT_EXTRA_ASSET_UUID)

        initControls()
    }

    /**
     * onResume() 시 ProductViewer startProductViewer() 호출
     * 라이프사이클 관련되어 해당 함수 호출 필수
     */
    override fun onResume() {
        super.onResume()

        // start 시점에 Surface 관련 에러가 발생할 수 있어 try 구문으로 처리
        try {
            ubProductViewer?.startProductViewer()
            // ProductViewer start 후 Asset 정보 로딩
            loadAssetInfo()
        } catch (e: Exception) {
            Log.e(TAG, "ProductViewer start failed")
        }
    }

    /**
     * onPause() 시 ProductViewer stopProductViewer() 호출
     * 라이프사이클 관련되어 해당 함수 호출 필수
     */
    override fun onPause() {
        super.onPause()
        ubProductViewer?.stopProductViewer()
    }

    /**
     * onDestroy() 시 ProductViewer stopProductViewer(), finishProductViewer() 호출
     * 라이프사이클 관련되어 해당 함수 호출 필수
     */
    override fun onDestroy() {
        ubProductViewer?.stopProductViewer()
        ubProductViewer?.finishProductViewer()

        super.onDestroy()
    }

    private fun initControls() {
        ubProductViewer = findViewById(R.id.ub_product_viewer)
        styleRecyclerView = findViewById(R.id.rv_asset_style)
        styleRecyclerView?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        styleRecyclerView?.addItemDecoration(StyleItemDecoration(20, 20))
        styleAdapter = StyleItemAdapter(this) {
            // Style 선택 되었을 경우 처리
            selectedStyleTextView?.text = it.name  // 선택 된 스타일 이름 표시
            setStyleForColorChip(it.id)
        }
        styleRecyclerView?.adapter = styleAdapter
        selectedStyleTextView = findViewById(R.id.tv_style_select)
    }

    private fun setStyleForColorChip(styleId: String?) {
        runOnUiThread {
            styleId?.let {
                ubProductViewer?.setProductStyle(it)
            }
        }
    }

    private fun loadAssetInfo() {
        assetUUID?.let { it ->
            UBAssetApiManager(this).getAssetInfo(it,
                onSuccess = { assetInfo ->
                    runOnUiThread {
                        mAssetInfo = assetInfo

                        // AssetInfo 에서 스타일 데이터 가져와 설정
                        assetInfo?.asset_styles?.let { styleList ->
                            styleAdapter?.setItems(styleList)

                            // 스타일 데이터 초기화 시 선택 된 스타일 정보 노출
                            styleList.forEach { style ->
                                if (style.is_default == true) {
                                    selectedStyleTextView?.text = style.name  // 선택 된 스타일 이름 표시
                                }
                            }
                        }

                        // Asset 정보 로드 후 Figure 로드 실행
                        FigureLoadManager.getInstance(this).loadFigure(
                            it, isPreview = false
                        ).setOnLoadListener(object : OnLoadListener {
                            override fun onLoad(figure: Figure) {
                                Log.d(TAG, "FigureLoadManager loadFigure onLoad : $figure")
                            }

                            override fun onLoadProgress(assetUUID: String, percent: Int, wholePercent: Int) {
                                Log.d(TAG, "FigureLoadManager loadFigure [$assetUUID] Progress : $percent / $wholePercent")
                                runOnUiThread {
                                    ubProductViewer?.setProgress(percent)
                                }
                            }

                            override fun onLoadFail(assetUUID: String, errorCode: Int, errorMsg: String) {
                                Log.d(TAG, "FigureLoadManager loadFigure onLoadFail : [$assetUUID] errorCode : $errorCode, errorMessage : $errorMsg")
                            }

                            override fun onFinally(
                                figures: ArrayList<Figure>,
                                requestCount: Int,
                                successCount: Int,
                                failCount: Int
                            ) {
                                Log.d(TAG, "FigureLoadManager loadFigure onFinally : $figures, requestCount : $requestCount, successCount : $successCount, failCount : $failCount")
                                if (failCount > 0) {
                                    runOnUiThread {
                                        Toast.makeText(this@ProductActivity, getString(R.string.toast_message_internet_error), Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    ubProductViewer?.attachFigure(figures.first())
                                }
                            }
                        })
                    }
                },
                onFailure = {
                    Log.e(TAG, "Load AssetInfo Failed : $it")
                }
            )
        }
    }
}