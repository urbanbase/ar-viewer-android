package com.urbanbase.app.arviewer.sample.java.ar;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.urbanbase.app.arviewer.sample.R;
import com.urbanbase.app.arviewer.sample.common.Const;
import com.urbanbase.sdk.arviewer.ar.UBArViewer;
import com.urbanbase.sdk.arviewer.ar.listener.OnLoadListener;
import com.urbanbase.sdk.arviewer.ar.manager.FigureLoadManager;
import com.urbanbase.sdk.arviewer.ar.manager.UBAssetApiManager;
import com.urbanbase.sdk.arviewer.ar.node.Figure;
import com.urbanbase.sdk.arviewer.common.UBEnums;
import com.urbanbase.sdk.arviewer.model.network.UBAssetInfo;
import com.urbanbase.sdk.arviewer.model.network.UBAssetStyle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ARJavaActivity extends AppCompatActivity
        implements Figure.OnFigureStateListener,
        UBArViewer.UBArViewerListener,
        UBArViewer.UBArViewerMeasureListener,
        View.OnClickListener {

    public static String TAG = "ARJavaActivity";

    private UBArViewer mArViewer = null;
    private ConstraintLayout mStyleLayout = null;
    private ConstraintLayout mInfoLayout = null;
    private LinearLayout mLlStyleWrapper = null;
    private ArrayList<Figure> mFigureList = new ArrayList<>();
    private Figure mFigure = null;
    private TextView mScaleTextView = null;
    private ImageButton mStyleButton = null;
    private ImageButton mRemoveButton = null;
    private ImageButton mStyleCloseButton = null;
    private ImageButton mCheckButton = null;
    private ImageButton mDropButton = null;

    private CheckBox mModeCheckBox = null;

    private ConstraintLayout mMeasureLayout = null;
    private Button mMeasureResultBtn = null;
    private ImageButton mMeasureAddBtn = null;
    private ImageButton mMeasureCancelBtn = null;

    private ConstraintLayout mArLayout = null;

    private String mAssetUUID = null;
    private UBAssetInfo mAssetInfo = null;
    private ArrayList<View> mFlStyleViews = new ArrayList<>();
    private int mStyleEdgeMargin = 30;
    private int mStyleMargin = 10;
    private UBEnums.MEASURE_STATE measureState = UBEnums.MEASURE_STATE.NONE;    // 현재 측정 상태를 확인 하기 위한 변수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        mAssetUUID = getIntent().getStringExtra(Const.INTENT_EXTRA_ASSET_UUID);

        Log.d(TAG, "onCreate. getIntent() AssetUUID : " + mAssetUUID);

        mInfoLayout = findViewById(R.id.cl_info_layout);
        mStyleLayout = findViewById(R.id.cl_ar_style_layout);
        mLlStyleWrapper = findViewById(R.id.ll_ar_asset_info_style_wrapper);
        mScaleTextView = findViewById(R.id.tv_scale);
        mStyleButton = findViewById(R.id.ib_style);
        mStyleButton.setOnClickListener(this);
        mRemoveButton = findViewById(R.id.ib_remove);
        mRemoveButton.setOnClickListener(this);
        mStyleCloseButton = findViewById(R.id.ib_style_close);
        mStyleCloseButton.setOnClickListener(this);
        mCheckButton = findViewById(R.id.ib_check);
        mCheckButton.setOnClickListener(this);
        mDropButton = findViewById(R.id.ib_drop);
        mDropButton.setOnClickListener(this);

        mMeasureLayout = findViewById(R.id.cl_measure_layout);
        mMeasureResultBtn = findViewById(R.id.btn_result);
        mMeasureResultBtn.setOnClickListener(this);
        mMeasureAddBtn = findViewById(R.id.ib_add);
        mMeasureAddBtn.setOnClickListener(this);
        mMeasureCancelBtn = findViewById(R.id.ib_cancel);
        mMeasureCancelBtn.setOnClickListener(this);
        mArLayout = findViewById(R.id.cl_ar_layout);
        findViewById(R.id.btn_measure_clear).setOnClickListener(this);

        mModeCheckBox = findViewById(R.id.chb_mode);
        mModeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeMode(isChecked);
            }
        });
        initARViewer();

        loadAssetInfo();
    }

    private void changeMode(boolean isMeasure) {
        if (isMeasure) {    // 측정 모드로 변환
            mMeasureLayout.setVisibility(View.VISIBLE);
            mArLayout.setVisibility(View.GONE);

            // 배치 진행 중인 제품이 있는 경우 배치 취소
            if (mFigure != null && mFigure.isPreviewing()) {
                mArViewer.cancelAttach();
            }

            // AR Ruler Mode
            mArViewer.setARBehavior(UBEnums.BehaviorMode.RULER);
            mArViewer.startARRuler();
        } else {    // AR 제품 배치 모드로 변환
            mMeasureLayout.setVisibility(View.GONE);
            mArLayout.setVisibility(View.VISIBLE);
            // 측정 모드 종료
//            mArViewer.closeARRuler(false);  // 화면 초기화가 필요한 경우 파라메터 true

            // 측정 모드 종료 없이 모드 변경
            // 현재 측정 상태에 따라 하기와 같이 추가 처리
            if (measureState == UBEnums.MEASURE_STATE.MEASURE_END) {
                mArViewer.setMeasure(); // 측정 상태를 MEASURE_FINISH 로 변경하여 노드 유지 시킨다
                mArViewer.stopARRuler();    // 측정 기능 정지
            } else if (measureState == UBEnums.MEASURE_STATE.NONE
            || measureState == UBEnums.MEASURE_STATE.MEASURE_START) {
                mArViewer.closeARRuler(false);  // 측정 중이거나 취소 되 경우 기존 측정 노드를 초기화 한다.
            } else {    // 그외 상태인 경우 측정기능만 정지
                mArViewer.stopARRuler();
            }

            // AR Viewer Mode
            mArViewer.setARBehavior(UBEnums.BehaviorMode.VIEWRENDERABLE_VECTOR_RETICLE);

            // Figure Reload
            loadFigure(mAssetUUID);
        }
    }

    private void initARViewer() {
        mArViewer = findViewById(R.id.ub_arviewer);
        if (mArViewer != null) {
            mArViewer.setArViewerListener(this);
            mArViewer.setFigureStateListener(this);
            mArViewer.setArViewerMeasureListener(this);
        }
    }

    private void showControlLayout(boolean isShow) {
        if (mCheckButton != null) {
            if (isShow) {
                mCheckButton.setVisibility(View.VISIBLE);
            } else {
                mCheckButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showInfoLayout(boolean isShow) {
        if (mInfoLayout != null) {
            if (isShow) {
                mInfoLayout.setVisibility(View.VISIBLE);
            } else {
                mInfoLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showStyleLayout(boolean isShow) {
        if (mStyleLayout != null) {
            if (isShow) {
                mStyleLayout.setVisibility(View.VISIBLE);
            } else {
                mStyleLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setScaleText(float scaleRatio) {
        if (mScaleTextView != null) {
            int ratio = (int) (scaleRatio * 100f);
            mScaleTextView.setText("Scale\n" + ratio + "%");
        }
    }

    private void setupStyle() {
        if (mLlStyleWrapper != null) {
            mLlStyleWrapper.removeAllViews();
            if (mAssetInfo != null) {
                if (mAssetInfo.getAsset_styles() != null) {
                    for (int i = 0; i < mAssetInfo.getAsset_styles().size(); i++) {
                        UBAssetStyle ubAssetStyle = mAssetInfo.getAsset_styles().get(i);

                        View styleView = LayoutInflater.from(this).inflate(
                                R.layout.item_ar_style, null, false
                        );

                        mFlStyleViews.add(styleView);

                        String styleName = "Default";
                        if (ubAssetStyle.getName() != null && !ubAssetStyle.getName().isEmpty()) {
                            styleName = ubAssetStyle.getName();
                        }

                        TextView styleNameTextView = styleView.findViewById(R.id.tv_style_name);
                        styleNameTextView.setText(styleName);

                        if (mFigure != null) {
                            if (mFigure.getSelectedStyleId() != null) {
                                if (mFigure.getSelectedStyleId().equals(ubAssetStyle.getId())) {
                                    styleView.findViewById(R.id.fl_select_line).setVisibility(View.VISIBLE);
                                    styleView.findViewById(R.id.iv_select_check).setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (ubAssetStyle != null) {
                                    if (ubAssetStyle.is_default() != null) {
                                        if (ubAssetStyle.is_default()) {
                                            styleView.findViewById(R.id.fl_select_line).setVisibility(View.VISIBLE);
                                            styleView.findViewById(R.id.iv_select_check).setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        }

                        styleView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mFlStyleViews != null) {
                                    for (int i = 0; i < mFlStyleViews.size(); i++) {
                                        View innerStyle = mFlStyleViews.get(i);
                                        innerStyle.findViewById(R.id.fl_select_line).setVisibility(View.INVISIBLE);
                                        innerStyle.findViewById(R.id.iv_select_check).setVisibility(View.GONE);
                                    }
                                }

                                styleView.findViewById(R.id.fl_select_line).setVisibility(View.VISIBLE);
                                styleView.findViewById(R.id.iv_select_check).setVisibility(View.VISIBLE);

                                if (mFigure != null) {
                                    mFigure.setStyle(ARJavaActivity.this, ubAssetStyle.getId());
                                }
                            }
                        });

                        ImageView ivStyle = styleView.findViewById(R.id.iv_style);
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                            ivStyle.setClipToOutline(true);
                        }

                        if (ubAssetStyle.getColor_chip_path() != null && !ubAssetStyle.getColor_chip_path().isEmpty() ) {
                            Glide.with(ARJavaActivity.this)
                                    .load(ubAssetStyle.getColor_chip_path())
                                    .centerCrop()
                                    .into(ivStyle);
                        }

                        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                        );

                        if (i == 0) {
                            layoutParams.setMarginStart(mStyleEdgeMargin);
                            layoutParams.setMarginEnd(mStyleMargin);
                        } else if (i == mAssetInfo.getAsset_styles().size() - 1) {
                            layoutParams.setMarginStart(mStyleMargin);
                            layoutParams.setMarginEnd(mStyleEdgeMargin);
                        } else {
                            layoutParams.setMarginStart(mStyleMargin);
                            layoutParams.setMarginEnd(mStyleMargin);
                        }

                        styleView.setLayoutParams(layoutParams);
                        mLlStyleWrapper.addView(styleView);
                    }
                }
            }
        }
    }

    /**
     * assetUUID 를 기반으로 Figure Load
     * @param assetUUID Asset UUID 정보
     */
    private void loadFigure(String assetUUID) {
        if (mArViewer != null) {
            mArViewer.setLoadingProgress(true);

            // Asset 정보 로드 후 Figure 로드 실행
            FigureLoadManager.Companion.getInstance(this)
                    .loadFigure(assetUUID, null, true)
                    .setOnLoadListener(new OnLoadListener() {
                        @Override
                        public void onLoad(@NotNull Figure figure) {
                            Log.d(TAG, "FigureLoadManager loadFigure onLoad : " + figure);
                        }

                        @Override
                        public void onLoadProgress(@NotNull String s, int percent, int wholePercent) {
                            Log.d(TAG, "FigureLoadManager loadFigure [" + assetUUID + "] Progress : " + percent + " / " + wholePercent);
                        }

                        @Override
                        public void onLoadFail(@NotNull String s, int errorCode, @NotNull String errorMsg) {
                            Log.d(TAG, "FigureLoadManager loadFigure onLoadFail : [" + assetUUID + "] errorCode : " + errorCode + ", errorMessage : " + errorMsg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mArViewer.setLoadingProgress(false);
                                    mArViewer.cancelAttach();
                                }
                            });
                        }

                        @Override
                        public void onFinally(@NotNull ArrayList<Figure> figures, int requestCount, int successCount, int failCount) {
                            Log.d(TAG, "FigureLoadManager loadFigure onFinally : " + figures + ", requestCount : " + requestCount +
                                    ", successCount : " + successCount + ", failCount : " + failCount);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 로딩 프로그래스 제거
                                    mArViewer.setLoadingProgress(false);
                                    if (failCount > 0) {
                                        Toast.makeText(ARJavaActivity.this, getString(R.string.toast_message_internet_error), Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (figures.size() > 0) {
                                            mArViewer.attachFigure(figures.get(0));
                                            mFigureList.add(figures.get(0));
                                            mFigure = figures.get(0);

                                            // Figure 로드 후 스타일 설정
                                            setupStyle();
                                            if (mFigure.getActionNode() != null) {
                                                setScaleText(mFigure.getActionNode().getCurrentScaleRatio());
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
        }

    }

    private void loadAssetInfo() {
        // 로딩 프로그래스 노출
        if (mArViewer != null) {
            mArViewer.setLoadingProgress(true);
            if (mAssetUUID != null && !mAssetUUID.isEmpty()) {
                new UBAssetApiManager(this).getAssetInfo(mAssetUUID,
                        new Function1<UBAssetInfo, Unit>() {
                            @Override
                            public Unit invoke(UBAssetInfo ubAssetInfo) {
                                mAssetInfo = ubAssetInfo;
                                return null;
                            }
                        },
                        new Function1<String, Unit>() {
                            @Override
                            public Unit invoke(String s) {
                                Log.e(TAG, "Load AssetInfo Failed : " + s);
                                return null;
                            }
                        });
            }
        }
    }

    /**
     * ARViewer 선택 되어졌을 경우 호출
     * @param isFigure 터치한 지점에 Figure 가 존재하는지 여부
     */
    @Override
    public void onArViewerClick(boolean isFigure) {

    }

    /**
     * ARViewer 내부적으로 View 초기화가 되었을 경우 호출
     * 기본 UI 를 사용하는 경우 해당 초기화가 완료 된 시점에서부터 정확한 View 사이즈 지정이 가능하다.
     */
    @Override
    public void onArViewerInitialized() {
        // ARViewer 초기화가 완료 되는 시점에 Figure 로드
        // ARViewer 초기화가 안된 시점에서 Figure 배치 시도 할 경우 내부 온보딩 UI가 표시 되지 않는다.
        if (mAssetUUID != null && !mAssetUUID.isEmpty()) {
            if (UBArViewer.Companion.isSupportedAR(this)) {
                loadFigure(mAssetUUID);
            }
        }
    }

    /**
     * Figure 클되었을 때 호출
     * @param figure 선택된 Figure
     */
    @Override
    public void onSingleClick(@NotNull Figure figure) {
        figure.setSelectFigure(true);
        showInfoLayout(true);
        showControlLayout(true);
    }

    /**
     * Figure 롱 클릭 되었을 때 호출
     * @param figure 선택된 Figure
     */
    @Override
    public void onLongClick(@NotNull Figure figure) {

    }

    /**
     * Figure 배치 되기 직전 호출
     * @param figure 선택된 Figure
     */
    @Override
    public void onCreated(@NotNull Figure figure) {

    }

    /**
     * Figure 회전 변경 시 호출
     * @param figure 선택된 Figure
     */
    @Override
    public void onRotateChanged(@NotNull Figure figure) {

    }

    /**
     * Figure 크기 변경 시 호출
     * @param figure 선택된 Figure
     */
    @Override
    public void onScaleChanged(@NotNull Figure figure) {
        float ratio = 1f;
        if (figure.getActionNode() != null) {
            ratio = figure.getActionNode().getCurrentScaleRatio();
        }
        Log.e(TAG, "Figure Scale Changed : " + ratio);
        float finalRatio = ratio;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setScaleText(finalRatio);
            }
        });
    }

    /**
     * Figure 더블 클릭릭 되었을 때 호출
     * @param figure 선택된 Figure
     */
    @Override
    public void onDoubleTab(@NotNull Figure figure) {

    }

    /**
     * Figure 선택 상태 되었을 때 호출
     * @param figure 선택된 Figure
     */
    @Override
    public void onSelected(@NotNull Figure figure) {

    }

    /**
     * Figure 배치 실패시 호출
     */
    @Override
    public void onLoadFail() {
        if (mArViewer != null) {
            mArViewer.cancelAttach();
        }
    }

    /**
     * ub_useDefaultPlaneGuide true 설정 된 경우
     * 기본 온보딩 프로세스 중 Figure 가 제거 되어질 경우 호출
     */
    @Override
    public void onDetached() {
        showInfoLayout(false);
        showControlLayout(false);
    }

    /**
     * Figure 가 바닥이 아닌 곳에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    @Override
    public void onNotPlaned(@NotNull Figure figure) {
        mDropButton.setVisibility(View.GONE);
    }

    /**
     * Figure 가 바닥인지 모호한 곳에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    @Override
    public void onAmbiguousPlaned(@NotNull Figure figure) {
        mDropButton.setVisibility(View.GONE);
    }

    /**
     * Figure 가 바닥에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    @Override
    public void onPlaned(@NotNull Figure figure) {

    }

    /**
     * Figure 미리보기 상태에서 바닥에 위치 되었을 때 호출
     * 온보딩 프로세스 직접 개발 시 해당 위치에 온보딩 관련 기능 추가
     * @param figure 선택된 Figure
     */
    @Override
    public void onPreviewPlaned(@NotNull Figure figure) {
        mDropButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_style : {
                showStyleLayout(true);
                break;
            }
            case R.id.ib_remove : {
                if (mArViewer != null) {
                    mArViewer.cancelAttach();
                    showStyleLayout(false);
                    showInfoLayout(false);
                    showControlLayout(false);
                }
                break;
            }
            case R.id.ib_style_close : {
                showStyleLayout(false);
                break;
            }
            case R.id.ib_drop : {
                mArViewer.dropFigure();
                mDropButton.setVisibility(View.GONE);
                break;
            }
            case R.id.ib_check : {
                if (mFigure != null) {
                    mFigure.setSelectFigure(false);
                    showInfoLayout(false);
                    showControlLayout(false);
                }
                break;
            }
            case R.id.ib_add:
                mArViewer.setMeasure();    // ARViewer에 측정 계산 요청
                setAddBtnState(true);
                break;

            case R.id.ib_cancel:
                mArViewer.cancelMeasure(); // ARViewer에 측정 계산 취소 요청
                setAddBtnState(false);
                break;

            case R.id.btn_result:
                float measureSize = mArViewer.getMeasureSize(); // 측정 결과를 가져온다
                Toast toast = Toast.makeText(this, "Measure Size : "+ measureSize + " mm", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;

            case R.id.btn_measure_clear:
                mArViewer.clearARRuler();   // ARRuler Clear
                break;
        }
    }

    @Override
    public void onChangedMeasureState(@NotNull UBEnums.MEASURE_STATE measure_state) {
        measureState = measure_state;

        // 측정 상태가 완료 인 경우만 결과보기 버튼 활성화
        if (measureState == UBEnums.MEASURE_STATE.MEASURE_END) {
            setResultBtnState(true);
        } else {
            setResultBtnState(false);
        }
    }

    @Override
    public void onFindHitResult() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initButtonEnable(true);
            }
        });
    }

    @Override
    public void onNotFindHitResult() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initButtonEnable(false);
            }
        });
    }

    private void setResultBtnState(boolean isEnable) {
        mMeasureResultBtn.setEnabled(isEnable);
    }

    private void setAddBtnState(boolean isMeasureStarted) {
        if (isMeasureStarted) {
            mMeasureAddBtn.setBackgroundResource(R.drawable.ic_plus_red);
        } else {
            mMeasureAddBtn.setBackgroundResource(R.drawable.ic_add_white);
        }
    }

    private void initButtonEnable(boolean isEnable) {
        switch (measureState) {
            case NONE : {
                if (isEnable) {
                    mMeasureAddBtn.setEnabled(true);
                    mMeasureAddBtn.setAlpha(1f);
                } else {
                    mMeasureAddBtn.setEnabled(false);
                    mMeasureAddBtn.setAlpha(0.5f);
                }
            }
            case MEASURE_START : {
                if (isEnable) {
                    mMeasureAddBtn.setEnabled(true);
                    mMeasureAddBtn.setAlpha(1f);
                } else {
                    mMeasureAddBtn.setEnabled(false);
                    mMeasureAddBtn.setAlpha(0.5f);
                }
            }
            default:
                break;
        }
    }
}
