package com.urbanbase.app.arviewer.sample.java.product;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urbanbase.app.arviewer.sample.R;
import com.urbanbase.app.arviewer.sample.common.Const;
import com.urbanbase.app.arviewer.sample.kotlin.product.adapter.StyleItemAdapter;
import com.urbanbase.app.arviewer.sample.kotlin.product.adapter.StyleItemDecoration;
import com.urbanbase.sdk.arviewer.ar.UBProductViewer;
import com.urbanbase.sdk.arviewer.ar.listener.OnLoadListener;
import com.urbanbase.sdk.arviewer.ar.manager.FigureLoadManager;
import com.urbanbase.sdk.arviewer.ar.manager.UBAssetApiManager;
import com.urbanbase.sdk.arviewer.ar.node.Figure;
import com.urbanbase.sdk.arviewer.model.network.UBAssetInfo;
import com.urbanbase.sdk.arviewer.model.network.UBAssetStyle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ProductJavaActivity extends AppCompatActivity {

    public static String TAG = "ProductJavaActivity";
    private String mAssetUUID = null;
    private UBAssetInfo mAssetInfo = null;

    private UBProductViewer mUbProductViewer = null;
    private RecyclerView mStyleRecyclerView = null;
    private StyleItemAdapter mStyleAdapter = null;
    private TextView mSelectedStyleTextView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        mAssetUUID = getIntent().getStringExtra(Const.INTENT_EXTRA_ASSET_UUID);

        Log.d(TAG, "onCreate. getIntent() AssetUUID : " + mAssetUUID);

        initControls();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start 시점에 Surface 관련 에러가 발생할 수 있어 try 구문으로 처리
        try {
            if (mUbProductViewer != null) {
                mUbProductViewer.startProductViewer();
            }

            // AR 기능 지원 가능한 경우 기능 요청
            if (UBProductViewer.Companion.isSupportedAR(this)) {
                // ProductViewer start 후 Asset 정보 로딩
                loadAssetInfo();
            }
        } catch (Exception ex) {
            Log.e(TAG, "ProductViewer start failed");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUbProductViewer != null) {
            mUbProductViewer.stopProductViewer();
        }
    }

    @Override
    protected void onDestroy() {
        if (mUbProductViewer != null) {
            mUbProductViewer.stopProductViewer();
            mUbProductViewer.finishProductViewer();
        }

        super.onDestroy();
    }

    private void initControls() {
        mUbProductViewer = findViewById(R.id.ub_product_viewer);
        mStyleRecyclerView = findViewById(R.id.rv_asset_style);
        mStyleRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mStyleRecyclerView.addItemDecoration(new StyleItemDecoration(20, 20));
        mStyleAdapter = new StyleItemAdapter(this, new Function1<UBAssetStyle, Unit>() {
            @Override
            public Unit invoke(UBAssetStyle ubAssetStyle) {
                // Style 선택 되었을 경우 처리
                mSelectedStyleTextView.setText(ubAssetStyle.getName()); // 선택 된 스타일 이름 표시
                setStyleForColorChip(ubAssetStyle.getId());
                return null;
            }
        });
        mStyleRecyclerView.setAdapter(mStyleAdapter);
        mSelectedStyleTextView = findViewById(R.id.tv_style_select);
    }

    private void setStyleForColorChip(String styleId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (styleId != null) {
                    if (mUbProductViewer != null) {
                        mUbProductViewer.setProductStyle(styleId);
                    }
                }
            }
        });
    }

    private void loadAssetInfo() {
        if (mAssetUUID != null) {
            new UBAssetApiManager(this).getAssetInfo(mAssetUUID,
                    new Function1<UBAssetInfo, Unit>() {
                        @Override
                        public Unit invoke(UBAssetInfo ubAssetInfo) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // AssetInfo 에서 스타일 데이터 가져와 설정
                                    if (ubAssetInfo!= null) {
                                        mAssetInfo = ubAssetInfo;

                                        mStyleAdapter.setItems(ubAssetInfo.getAsset_styles());
                                        for (UBAssetStyle assetStyle: ubAssetInfo.getAsset_styles()) {
                                            if (assetStyle.is_default() != null && assetStyle.is_default()) {
                                                mSelectedStyleTextView.setText(assetStyle.getName());   // 선택 된 스타일 이름 표시
                                            }
                                        }

                                        FigureLoadManager.Companion.getInstance(ProductJavaActivity.this)
                                                .loadFigure(mAssetUUID, null, false)
                                                .setOnLoadListener(new OnLoadListener() {
                                                    @Override
                                                    public void onLoad(@NotNull Figure figure) {
                                                        Log.d(TAG, "FigureLoadManager loadFigure onLoad : " + figure);
                                                    }

                                                    @Override
                                                    public void onLoadProgress(@NotNull String s, int percent, int wholePercent) {
                                                        Log.d(TAG, "FigureLoadManager loadFigure ["+ mAssetUUID + "] Progress : " + percent + " / " + wholePercent);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (mUbProductViewer != null) {
                                                                    mUbProductViewer.setProgress(percent);
                                                                }
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onLoadFail(@NotNull String s, int errorCode, @NotNull String errorMsg) {
                                                        Log.d(TAG, "FigureLoadManager loadFigure onLoadFail : [" + mAssetUUID
                                                                + "] errorCode : " + errorCode
                                                                + ", errorMessage : " + errorMsg);
                                                    }

                                                    @Override
                                                    public void onFinally(@NotNull ArrayList<Figure> figures, int requestCount, int successCount, int failCount) {
                                                        Log.d(TAG, "FigureLoadManager loadFigure onFinally : "
                                                                + figures + ", requestCount : "
                                                                + requestCount + ", successCount : "
                                                                + successCount + ", failCount : " + failCount);
                                                        if (failCount > 0) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(ProductJavaActivity.this,
                                                                            getString(R.string.toast_message_internet_error),
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        } else {
                                                            if (mUbProductViewer != null) {
                                                                if (figures.size() > 0) {
                                                                    mUbProductViewer.attachFigure(figures.get(0));
                                                                }
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
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
