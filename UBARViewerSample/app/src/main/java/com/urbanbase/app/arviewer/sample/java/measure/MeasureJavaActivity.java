package com.urbanbase.app.arviewer.sample.java.measure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.urbanbase.app.arviewer.sample.R;
import com.urbanbase.sdk.arviewer.ar.UBArViewer;
import com.urbanbase.sdk.arviewer.common.UBEnums;

import org.jetbrains.annotations.NotNull;

public class MeasureJavaActivity extends AppCompatActivity
        implements View.OnClickListener, UBArViewer.UBArViewerListener, UBArViewer.UBArViewerMeasureListener {

    private UBEnums.MEASURE_STATE measureState = UBEnums.MEASURE_STATE.NONE;

    private UBArViewer mARViewer;
    private ImageButton mAddBtn;
    private ImageButton mCancelBtn;
    private Button mResultBtn;
    private ConstraintLayout mGuideLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);

        mARViewer = findViewById(R.id.ub_arviewer);
        mAddBtn = findViewById(R.id.ib_add);
        mAddBtn.setOnClickListener(this);
        mCancelBtn = findViewById(R.id.ib_cancel);
        mCancelBtn.setOnClickListener(this);
        mResultBtn = findViewById(R.id.btn_result);
        mResultBtn.setOnClickListener(this);
        mGuideLayout = findViewById(R.id.cl_measure_guide);

        // ARViewer Measure 사용 초기화
        initMeasureAr();
    }

    @Override
    protected void onDestroy() {
        // AR Ruler 기능 종료
        if (mARViewer != null) {
            mARViewer.closeARRuler(true);
        }

        super.onDestroy();
    }

    private void initMeasureAr() {
        if (mARViewer != null) {
            mARViewer.setArViewerListener(this);
            mARViewer.setArViewerMeasureListener(this);
        }
    }

    private void setResultBtnState(boolean isEnable) {
        mResultBtn.setEnabled(isEnable);
    }

    private void setAddBtnState(boolean isMeasureStarted) {
        if (isMeasureStarted) {
            mAddBtn.setBackgroundResource(R.drawable.ic_plus_red);
        } else {
            mAddBtn.setBackgroundResource(R.drawable.ic_add_white);
        }
    }

    private void initButtonEnable(boolean isEnable) {
        switch (measureState) {
            case NONE : {
                if (isEnable) {
                    mAddBtn.setEnabled(true);
                    mAddBtn.setAlpha(1f);
                } else {
                    mAddBtn.setEnabled(false);
                    mAddBtn.setAlpha(0.5f);
                }
            }
            case MEASURE_START : {
                if (isEnable) {
                    mAddBtn.setEnabled(true);
                    mAddBtn.setAlpha(1f);
                } else {
                    mAddBtn.setEnabled(false);
                    mAddBtn.setAlpha(0.5f);
                }
            }
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_add:
                mARViewer.setMeasure();    // ARViewer에 측정 계산 요청
                setAddBtnState(true);
                break;

            case R.id.ib_cancel:
                mARViewer.cancelMeasure(); // ARViewer에 측정 계산 취소 요청
                setAddBtnState(false);
                break;

            case R.id.btn_result:
                float measureSize = mARViewer.getMeasureSize(); // 측정 결과를 가져온다
                Toast toast = Toast.makeText(this, "Measure Size : "+ measureSize + " mm", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;
        }
    }

    /**
     * 측정 상태가 변경이 된 경우 호출
     * @param measure_state 현재 측정 상태
     */
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

    /**
     * 측정 포인트를 찾은 경우 처리
     */
    @Override
    public void onFindHitResult() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initButtonEnable(true);
            }
        });
    }

    /**
     * 측정 포인트를 찾지 못한 경우 처리
     */
    @Override
    public void onNotFindHitResult() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initButtonEnable(false);
            }
        });
    }

    @Override
    public void onArViewerClick(boolean isFigure) {

    }

    /**
     * ARViewer 가 초기화 되는 시점에 ARRuler 모드 실행
     */
    @Override
    public void onArViewerInitialized() {
        // AR Ruler 시작
        if (mARViewer != null) {
            mARViewer.startARRuler();
        }
    }
}