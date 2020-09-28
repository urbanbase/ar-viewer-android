package com.urbanbase.app.arviewer.sample.java;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.urbanbase.app.arviewer.sample.R;
import com.urbanbase.app.arviewer.sample.base.PermissionCheckActivity;
import com.urbanbase.app.arviewer.sample.common.Const;
import com.urbanbase.app.arviewer.sample.java.ar.ARJavaActivity;
import com.urbanbase.app.arviewer.sample.java.measure.MeasureJavaActivity;
import com.urbanbase.app.arviewer.sample.java.product.ProductJavaActivity;
import com.urbanbase.sdk.arviewer.ar.UBArViewer;
import com.urbanbase.sdk.arviewer.ar.manager.UBAssetApiManager;
import com.urbanbase.sdk.arviewer.model.network.UBAsset;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class MainJavaActivity extends PermissionCheckActivity {

    public static String TAG = "MainJavaActivity";

    private ConstraintLayout dimView = null;
    private UBAsset asset = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로딩 처리용 Dim View
        dimView = findViewById(R.id.cl_dim);

        // AR Mode 선택 시 처리
        findViewById(R.id.btn_ar_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로딩 한 Asset 정보가 유효한 경우 액티비티 이동
                if (asset != null) {
                    if (asset.getAsset_uuid() != null) {
                        if (UBArViewer.Companion.isSupportedAR(MainJavaActivity.this)) {
                            goArActivity(asset.getAsset_uuid());
                        }
                    }
                }
            }
        });

        // Product Mode(3DViewer) 선택 시 처리
        findViewById(R.id.btn_product_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로딩 한 Asset 정보가 유효한 경우 액티비티 이동
                if (asset != null) {
                    if (asset.getAsset_uuid() != null) {
                        if (UBArViewer.Companion.isSupportedAR(MainJavaActivity.this)) {
                            goProductActivity(asset.getAsset_uuid());
                        }
                    }
                }
            }
        });

        // Measure Mode 선택 시 처리
        findViewById(R.id.btn_measure_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UBArViewer.Companion.isSupportedAR(MainJavaActivity.this)) {
                    goMeasureActivity();
                }
            }
        });

        // 서비스키에 해당되는 Asset List 정보 요청
        if (UBArViewer.Companion.isSupportedAR(this)) {
            showLoading(true);
            new UBAssetApiManager(this).getPrivateAssetList(
                    new Function1<ArrayList<UBAsset>, Unit>() {
                        @Override
                        public Unit invoke(ArrayList<UBAsset> ubAssets) {
                            Log.e(TAG, "AssetData load success. [ " + ubAssets.size() + " ]");
                            // 첫번째 Asset 정보로 고정
                            if (ubAssets.size() > 0) {
                                asset = ubAssets.get(0);
                                Log.e(TAG, "Select Asset : [ " + asset + "]");
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showLoading(false);
                                }
                            });
                            return null;
                        }
                    },
                    new Function1<String, Unit>() {
                        @Override
                        public Unit invoke(String s) {
                            Log.e(TAG, "AssetData load failed. [ " + s + " ]");
                            return null;
                        }
                    }
            );
        }
    }

    private void showLoading(boolean isShow) {
        if (isShow) {
            dimView.setVisibility(View.VISIBLE);
        } else {
            dimView.setVisibility(View.GONE);
        }
    }

    private void goArActivity(String assetUUID) {
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkPermission(new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Intent intent = new Intent(MainJavaActivity.this, ARJavaActivity.class);
                                intent.putExtra(Const.INTENT_EXTRA_ASSET_UUID, assetUUID);
                                startActivity(intent);
                                return null;
                            }
                        });
                    }
                }, 200);
    }

    private void goProductActivity(String assetUUID) {
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        {
                            checkPermission(new Function0<Unit>() {
                                @Override
                                public Unit invoke() {
                                    {
                                        Intent intent = new Intent(MainJavaActivity.this, ProductJavaActivity.class);
                                        intent.putExtra(Const.INTENT_EXTRA_ASSET_UUID, assetUUID);
                                        startActivity(intent);
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                }, 200);
    }

    private void goMeasureActivity() {
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        {
                            checkPermission(new Function0<Unit>() {
                                @Override
                                public Unit invoke() {
                                    {
                                        Intent intent = new Intent(MainJavaActivity.this, MeasureJavaActivity.class);
                                        startActivity(intent);
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                }, 200);
    }
}
