# Android ARViewer 임포트 가이드

## Overview

## Getting Started

ARViewer 는 Android 의 Sceneform 에 기반한 정적 라이브러리입니다.
아래의 조건에서 정상적으로 동작합니다.
* Android 24 이상 단말기
* Google Play AR Service 지원 단말기
* OpenGL 3.0 이상 지원 단말기

> 'SDK 를 사용하기 위해 Api Key 및 패키지명 인증을 거친 후 사용 가능 합니다.'
## Importing SDK
### SDK 라이브러리 파일 추가
Project폴더/app/libs 에 arviewer.aar 파일 붙여넣기

<div style="text-align : center;">
  <a href="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_1.png"><img width="450" src="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_1.png" alt="img_sdk_to_folder" title="프로젝트 폴더에 framework 붙여넣기"></a>
</div>

### Manifest 파일 변경
AR 기능을 사용하기 위한 Manifest.xml 파일 수정 : 노란색 박스 확인

<div style="text-align : center;">
  <a href="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_2.png"><img width="450" src="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_2.png" alt="img_sdk_to_folder" title="프로젝트 폴더에 framework 붙여넣기"></a>
</div>

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
package="xxx.xxx.xxx"><!-- 등록 된 서비스 패키지명 입력 필수 -->

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<!-- AR 카메라 필수 여부 -->
    <uses-feature
        android:name="android.hardware.camera.ar"
        android:required="true" />

<application>
...
...
  <!-- ARCore 필수 여부 -->
  <meta-data
            android:name="com.google.ar.core"
            android:value="optional" />
  <!-- 발급받은 서비스 키 (필수) -->            
  <meta-data
            android:name="com.urbanbase.sdk.arviewer.apis.key"
            android:value="@string/arviewer_apis_key" />
...
...

</application>
```

### app build.gradle 파일 변경

minSdkVersion 21 정의 및 Java 8 기능과 AR 기능을 사용하기 위한 선언 : 노란색 박스 확인

<div style="text-align : center;">
  <a href="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_3.png"><img width="450" src="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_3.png" alt="img_sdk_to_folder" title="프로젝트 폴더에 framework 붙여넣기"></a>
</div>

외부 종속성 설정

<div style="text-align : center;">
  <a href="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_4.png"><img width="450" src="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_4.png" alt="img_sdk_to_folder" title="프로젝트 폴더에 framework 붙여넣기"></a>
</div>

```gradle
...
android {

    defaultConfig {
        ...
        minSdkVersion 24

    ...

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    ...

    repositories {
        flatDir {
            dirs 'libs'
        }
    }
}
...

repositories {
    mavenCentral()
    jcenter()
    maven { url "https://jitpack.io" }
}

dependencies {
...
    // Network
    // okhttp 3.6.0 버전 이상 적용
    implementation 'com.squareup.okhttp3:okhttp:4.2.0'
    implementation 'com.google.code.gson:gson:2.8.5'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.1'

    // Sceneform
    implementation "com.google.ar.sceneform.ux:sceneform-ux:1.15.0"
    implementation "com.google.ar.sceneform:assets:1.15.0"

    // UI
    implementation 'com.github.bumptech.glide:glide:4.11.0'
    implementation 'com.airbnb.android:lottie:3.2.0'

    // ARViewer
    implementation(name: 'arviewer', ext: 'aar')
}
```


## Using SDK

### 1.xml을 이용한 정적 사용법
UBArViewer 클래스 Layout 에 선언

<div style="text-align : center;">
  <a href="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_5.png"><img width="450" src="https://ub-mobile.s3.ap-northeast-2.amazonaws.com/img_import_guide/android/import_sdk_5.png" alt="img_sdk_to_folder" title="프로젝트 폴더에 framework 붙여넣기"></a>
</div>

#### xml 사용 properties
* ub_findPlaneMode : 바닥 감지 모드 설정
 * all : 바닥, 벽면 모두 감지
 * horizontal : 바닥만 감지
 * vertical : 벽면만 감지
 * none : 사용하지 않음
* ub_zoomable : 제품 확대/축소 이용 여부
 * ture : 확대/축소 지원
 * false : 확대/축소 미지원
* ub_behaviorMode : 제품 처리 Behavior 설정 ( 기본값 : vector_reticle )
 * vector_reticle : 프리뷰 모두 지원 (기본 값)
 * ruler : 단일 측정 ( AR 줄자 기능에만 사용)
 * 다른 모드는 현재 미 지원
* ub_useDefaultPlaneGuide : 온보딩 처리용 UI 내부 기본설정 사용 여부
 * true : 온보딩 관련 UI 앱 내에서 개발이 없는 경우 기본 UI 사용
 * false : 온보딩 관련 UI 앱내에서 직접 구현

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ar.ARActivity">

    <FrameLayout
        android:id="@+id/ar_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">
        <com.urbanbase.sdk.arviewer.ar.UBArViewer
            android:id="@+id/ub_arviewer"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:ub_findPlaneMode="horizontal"
            app:ub_zoomable="true"
            app:ub_useDefaultPlaneGuide="true" />
    </FrameLayout>
    ...
    ...
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### ARViewer 초기화 Sample
```java
public class ARActivity extends AppCompatActivity() implements
      Figure.OnFigureStateListener,
      UBArViewer.UBArViewerListener {

    private UBArViewer arViewer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        initARViewer();
    }

    private void initARViewer() {
        arViewer = findViewById(R.id.ub_arviewer);
        arViewer.setArViewerListener(this);
        arViewer.setFigureStateListener(this)
    }

    // OnFigureStateListener Func
    @Override
    public void onSingleClick(@NotNull Figure figure) {
    }

    @Override
    public void onLongClick(@NotNull Figure figure) {
    }

    @Override
    public void onCreated(@NotNull Figure figure) {
    }

    @Override
    public void onRotateChanged(@NotNull Figure figure) {
    }

    @Override
    public void onScaleChanged(@NotNull Figure figure) {
    }

    @Override
    public void onDoubleTab(@NotNull Figure figure) {
    }

    @Override
    public void onSelected(@NotNull Figure figure) {
    }

    @Override
    public void onLoadFail() {
    }

    @Override
    public void onDetached() {
    }

    @Override
    public void onNotPlaned(@NotNull Figure figure) {
    }

    @Override
    public void onAmbiguousPlaned(@NotNull Figure figure) {
    }

    @Override
    public void onPlaned(@NotNull Figure figure) {
    }

    @Override
    public void onPreviewPlaned(@NotNull Figure figure) {
    }

    // ARViewerListener Fuc
    @Override
    public void onArViewerClick(boolean isFigure) {
    }

    @Override
    public void onArViewerInitialized() {
    }
}
```

### 2.Builder를 이용한 동적 사용법
#### ARViewer 초기화 Sample
```java
public class ARActivity extends AppCompatActivity implements Figure.OnFigureStateListener, UBArViewer.UBArViewerListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);
        UBArViewer arViewer = UBArViewer.builder(this)
                .enableZoom(true)
                .findPlaneMode(UBEnums.FindPlaneMode.HORIZONTAL)
                .useDefaultPlaneGuide(true)
                .build();
        arViewer.setArViewerListener(this);
        arViewer.setFigureStateListener(this);

        FrameLayout root = findViewById(R.id.ar_container);
        root.addView(arViewer);
    }

    @Override
    public void onArViewerClick(boolean isFigure) {

    }

    @Override
    public void onArViewerInitialized() {

    }

    @Override
    public void onSingleClick(@NotNull Figure figure) {

    }

    @Override
    public void onLongClick(@NotNull Figure figure) {

    }

    @Override
    public void onCreated(@NotNull Figure figure) {

    }

    @Override
    public void onRotateChanged(@NotNull Figure figure) {

    }

    @Override
    public void onScaleChanged(@NotNull Figure figure) {

    }

    @Override
    public void onDoubleTab(@NotNull Figure figure) {

    }

    @Override
    public void onSelected(@NotNull Figure figure) {

    }

    @Override
    public void onLoadFail() {

    }

    @Override
    public void onDetached() {

    }

    @Override
    public void onNotPlaned(@NotNull Figure figure) {

    }

    @Override
    public void onAmbiguousPlaned(@NotNull Figure figure) {

    }

    @Override
    public void onPlaned(@NotNull Figure figure) {

    }

    @Override
    public void onPreviewPlaned(@NotNull Figure figure) {

    }
}
```

## Links

You may be using [ARViewer Github](https://github.com/urbanbase/ar-viewer-android).
