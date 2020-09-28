package com.urbanbase.app.arviewer.sample.kotlin.ar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.urbanbase.app.arviewer.sample.R
import com.urbanbase.app.arviewer.sample.common.Const
import com.urbanbase.sdk.arviewer.ar.UBArViewer
import com.urbanbase.sdk.arviewer.ar.listener.OnLoadListener
import com.urbanbase.sdk.arviewer.ar.manager.FigureLoadManager
import com.urbanbase.sdk.arviewer.ar.manager.UBAssetApiManager
import com.urbanbase.sdk.arviewer.ar.node.Figure
import com.urbanbase.sdk.arviewer.model.network.UBAssetInfo
import kotlinx.android.synthetic.main.item_ar_style.view.*

class ARActivity : AppCompatActivity(),
    Figure.OnFigureStateListener,
    UBArViewer.UBArViewerListener,
    View.OnClickListener{

    var TAG = "ARActivity"

    private var arViewer: UBArViewer? = null
    private var styleLayout: ConstraintLayout? = null
    private var infoLayout: ConstraintLayout? = null
    private var llStyleWrapper: LinearLayout? = null
    private var figure: Figure? = null
    private var scaleTextView: TextView? = null
    private var styleButton: ImageButton? = null
    private var removeButton: ImageButton? = null
    private var styleCloseButton: ImageButton? = null
    private var checkButton: ImageButton? = null

    private var assetUUID: String? = null
    private var mAssetInfo: UBAssetInfo? = null
    private val flStyleViews = ArrayList<View>()
    private val styleEdgeMargin = 30
    private val styleMargin = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_r)

        assetUUID = intent.getStringExtra(Const.INTENT_EXTRA_ASSET_UUID)

        infoLayout = findViewById(R.id.cl_info_layout)
        styleLayout = findViewById(R.id.cl_ar_style_layout)
        llStyleWrapper = findViewById(R.id.ll_ar_asset_info_style_wrapper)
        scaleTextView = findViewById(R.id.tv_scale)
        styleButton = findViewById(R.id.ib_style)
        styleButton?.setOnClickListener(this)
        removeButton = findViewById(R.id.ib_remove)
        removeButton?.setOnClickListener(this)
        styleCloseButton = findViewById(R.id.ib_style_close)
        styleCloseButton?.setOnClickListener(this)
        checkButton = findViewById(R.id.ib_check)
        checkButton?.setOnClickListener(this)

        initARViewer()

        loadAssetInfo()
    }

    private fun initARViewer() {
        arViewer = findViewById(R.id.ub_arviewer)
        arViewer?.setArViewerListener(this)
        arViewer?.setFigureStateListener(this)
    }

    private fun showControlLayout(isShow: Boolean) {
        if (isShow) {
            checkButton?.visibility = View.VISIBLE
        } else {
            checkButton?.visibility = View.INVISIBLE
        }
    }

    private fun showInfoLayout(isShow: Boolean) {
        if (isShow) {
            infoLayout?.visibility = View.VISIBLE
        } else {
            infoLayout?.visibility = View.INVISIBLE
        }
    }

    private fun showStyleLayout(isShow: Boolean) {
        if (isShow) {
            styleLayout?.visibility = View.VISIBLE
        } else {
            styleLayout?.visibility = View.INVISIBLE
        }
    }

    private fun setScaleText(scaleRatio: Float) {
        scaleTextView?.text = "Scale\n${(scaleRatio * 100f).toInt()}%"
    }

    private fun setupStyle() {
        llStyleWrapper?.removeAllViews()

        mAssetInfo?.asset_styles?.forEachIndexed { index, ubAssetStyle ->

            val styleView = LayoutInflater.from(this).inflate(
                R.layout.item_ar_style, null, false
            )

            flStyleViews.add(styleView)

            val styleName = if (!ubAssetStyle.name.isNullOrEmpty()) {
                ubAssetStyle.name
            } else {
                "Default"
            }
            styleView.tv_style_name.text = styleName

            figure?.selectedStyleId?.let {
                if (it.equals(ubAssetStyle.id)) {
                    styleView.fl_select_line.visibility = View.VISIBLE
                    styleView.iv_select_check.visibility = View.VISIBLE
                }
            } ?: kotlin.run {
                ubAssetStyle.is_default?.let {
                    if (it) {
                        styleView.fl_select_line.visibility = View.VISIBLE
                        styleView.iv_select_check.visibility = View.VISIBLE
                    }
                }
            }

            styleView.setOnClickListener { outerIt ->
                flStyleViews.forEach { innerIt ->
                    innerIt.fl_select_line.visibility = View.INVISIBLE
                    innerIt.iv_select_check.visibility = View.GONE
                }
                outerIt.fl_select_line.visibility = View.VISIBLE
                outerIt.iv_select_check.visibility = View.VISIBLE
                figure?.setStyle(this, ubAssetStyle.id)
            }
            val ivStyle = styleView.findViewById<ImageView>(R.id.iv_style)
            ivStyle.clipToOutline = true

            ubAssetStyle.color_chip_path?.let {
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .into(ivStyle)
            } ?: kotlin.run {

            }

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            when (index) {
                0 -> {
                    layoutParams.marginStart = styleEdgeMargin
                    layoutParams.marginEnd = styleMargin
                }
                mAssetInfo?.asset_styles?.size?.minus(1) -> {
                    layoutParams.marginStart = styleMargin
                    layoutParams.marginEnd = styleEdgeMargin
                }
                else -> {
                    layoutParams.marginStart = styleMargin
                    layoutParams.marginEnd = styleMargin
                }
            }

            styleView.layoutParams = layoutParams
            llStyleWrapper?.addView(styleView)
        }
    }

    /**
     * assetUUID 를 기반으로 Figure Load
     * @param assetUUID Asset UUID 정보
     */
    private fun loadFigure(assetUUID: String) {
        arViewer?.setLoadingProgress(true)

        // Asset 정보 로드 후 Figure 로드 실행
        FigureLoadManager.getInstance(this).loadFigure(
            assetUUID, isPreview = true
        ).setOnLoadListener(object : OnLoadListener {
            override fun onLoad(figure: Figure) {
                Log.d(TAG, "FigureLoadManager loadFigure onLoad : $figure")
            }

            override fun onLoadProgress(assetUUID: String, percent: Int, wholePercent: Int) {
                Log.d(TAG, "FigureLoadManager loadFigure [$assetUUID] Progress : $percent / $wholePercent")
            }

            override fun onLoadFail(assetUUID: String, errorCode: Int, errorMsg: String) {
                Log.d(TAG, "FigureLoadManager loadFigure onLoadFail : [$assetUUID] errorCode : $errorCode, errorMessage : $errorMsg")
                runOnUiThread {
                    arViewer?.setLoadingProgress(false)
                    arViewer?.cancelAttach()
                }
            }

            override fun onFinally(
                figures: ArrayList<Figure>,
                requestCount: Int,
                successCount: Int,
                failCount: Int
            ) {
                Log.d(TAG, "FigureLoadManager loadFigure onFinally : $figures, requestCount : $requestCount, successCount : $successCount, failCount : $failCount")
                runOnUiThread {
                    // 로딩 프로그래스 제거
                    arViewer?.setLoadingProgress(false)
                    if (failCount > 0) {
                        Toast.makeText(this@ARActivity, getString(R.string.toast_message_internet_error), Toast.LENGTH_SHORT).show()
                    } else {
                        // 로드 한 Figure 를 AR 상에 배치
                        // figure 의 경우 첫번째 Figure 로 사용
                        arViewer?.attachFigure(figures.first())
                        figure = figures.first()

                        // Figure 로드 후 스타일 설정
                        setupStyle()
                        setScaleText(figure?.actionNode?.currentScaleRatio ?: 1f)
                    }
                }
            }
        })
    }

    private fun loadAssetInfo() {
        // 로딩 프로그래스 노출
        arViewer?.setLoadingProgress(true)

        assetUUID?.let { it ->
            UBAssetApiManager(this).getAssetInfo(it,
                onSuccess = { assetInfo ->
                    mAssetInfo = assetInfo
                },
                onFailure = {
                    Log.e(TAG, "Load AssetInfo Failed : $it")
                }
            )
        }
    }

    // OnFigureStateListener Func
    /**
     * Figure 클되었을 때 호출
     * @param figure 선택된 Figure
     */
    override fun onSingleClick(figure: Figure) {
        figure.setSelectFigure(true)
        showInfoLayout(true)
        showControlLayout(true)
    }

    /**
     * Figure 롱 클릭 되었을 때 호출
     * @param figure 선택된 Figure
     */
    override fun onLongClick(figure: Figure) {
    }

    /**
     * Figure 배치 되기 직전 호출
     * @param figure 선택된 Figure
     */
    override fun onCreated(figure: Figure) {
    }

    /**
     * Figure 회전 변경 시 호출
     * @param figure 선택된 Figure
     */
    override fun onRotateChanged(figure: Figure) {
    }

    /**
     * Figure 크기 변경 시 호출
     * @param figure 선택된 Figure
     */
    override fun onScaleChanged(figure: Figure) {
        Log.e(TAG, "Figure Scale Changed : ${figure.actionNode?.currentScaleRatio}")
        runOnUiThread {
            setScaleText(figure.actionNode?.currentScaleRatio ?: 1f)
        }
    }

    /**
     * Figure 더블 클릭릭 되었을 때 호출
     * @param figure 선택된 Figure
     */
    override fun onDoubleTab(figure: Figure) {
    }

    /**
     * Figure 선택 상태 되었을 때 호출
     * @param figure 선택된 Figure
     */
    override fun onSelected(figure: Figure) {
    }

    /**
     * Figure 배치 실패시 호출
     */
    override fun onLoadFail() {
        arViewer?.cancelAttach()
    }

    /**
     * ub_useDefaultPlaneGuide true 설정 된 경우
     * 기본 온보딩 프로세스 중 Figure 가 제거 되어질 경우 호출
     */
    override fun onDetached() {
        showInfoLayout(false)
        showControlLayout(false)
    }

    /**
     * Figure 가 바닥이 아닌 곳에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    override fun onNotPlaned(figure: Figure) {
    }

    /**
     * Figure 가 바닥인지 모호한 곳에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    override fun onAmbiguousPlaned(figure: Figure) {
    }

    /**
     * Figure 가 바닥에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    override fun onPlaned(figure: Figure) {
    }

    /**
     * Figure 미리보기 상태에서 바닥에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    override fun onPreviewPlaned(figure: Figure) {

    }

    // ARViewerListener Fuc
    /**
     * ARViewer 내부적으로 View 초기화가 되었을 경우 호출
     * 기본 UI 를 사용하는 경우 해당 초기화가 완료 된 시점에서부터 정확한 View 사이즈 지정이 가능하다.
     */
    override fun onArViewerInitialized() {
        // ARViewer 초기화가 완료 되는 시점에 Figure 로드
        // ARViewer 초기화가 안된 시점에서 Figure 배치 시도 할 경우 내부 온보딩 UI가 표시 되지 않는다.
        assetUUID?.let {
            if (UBArViewer.isSupportedAR(this)) {
                loadFigure(it)
            }
        }
    }

    /**
     * ARViewer 선택 되어졌을 경우 호출
     * @param isFigure 터치한 지점에 Figure 가 존재하는지 여부
     */
    override fun onArViewerClick(isFigure: Boolean) {
    }

    // onClick Listener
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ib_style -> {
                showStyleLayout(true)
            }
            R.id.ib_remove -> {
                arViewer?.cancelAttach()
                showStyleLayout(false)
                showInfoLayout(false)
                showControlLayout(false)
            }
            R.id.ib_style_close -> {
                showStyleLayout(false)
            }
            R.id.ib_check -> {
                figure?.setSelectFigure(false)
                showInfoLayout(false)
                showControlLayout(false)
            }
        }
    }
}