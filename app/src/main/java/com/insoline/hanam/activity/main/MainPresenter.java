package com.insoline.hanam.activity.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.inavi.mapsdk.geometry.LatLng;
import com.inavi.mapsdk.geometry.LatLngBounds;
import com.inavi.mapsdk.maps.CameraAnimationType;
import com.inavi.mapsdk.maps.CameraPosition;
import com.inavi.mapsdk.maps.CameraUpdate;
import com.inavi.mapsdk.maps.InaviMap;
import com.inavi.mapsdk.maps.InvMapView;
import com.inavi.mapsdk.maps.LocationIcon;
import com.inavi.mapsdk.style.shapes.InvImage;
import com.inavi.mapsdk.style.shapes.InvMarker;
import com.inavi.mapsdk.style.shapes.InvRoute;
import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.activity.CommonAssets;
import com.insoline.hanam.activity.menu.CallCenterActivity;
import com.insoline.hanam.activity.menu.FavoriteLocationActivity;
import com.insoline.hanam.activity.menu.HistoryActivity;
import com.insoline.hanam.activity.menu.MenuSettingActivity;
import com.insoline.hanam.adapter.SearchResultBaseAdapter;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.HttpCallback;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.AuthResult;
import com.insoline.hanam.net.response.CarInfoResult;
import com.insoline.hanam.net.response.RequestResult;
import com.insoline.hanam.net.response.VisitedLocationData;
import com.insoline.hanam.net.response.inaviapi.InaviApiCoord;
import com.insoline.hanam.net.response.inaviapi.InaviApiPath;
import com.insoline.hanam.net.response.inaviapi.InaviApiReverseGeocode;
import com.insoline.hanam.net.response.inaviapi.InaviApiRouteNormalResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiSearchResult;
import com.insoline.hanam.service.CallCheckService;
import com.insoline.hanam.util.CommonUtil;
import com.insoline.hanam.util.NotificationUtil;
import com.insoline.hanam.view.HoustonStyleDialog;
import com.insoline.hanam.view.HoustonStyleDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;

public class MainPresenter
{
    public final static int SCREEN_STATUS_DEFAULT                       = 200;  // 메인 화면 기본(바로 호출 준비 상태)
    public final static int SCREEN_STATUS_DEFAULT_FULL_MAP              = 210;  // 메인 화면 지도 최대 상태
    public final static int SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT    = 220;  // 메인 화면 바로 호출 출발지 확인

    public final static int SCREEN_STATUS_SEARCH_RESULT_SELECT          = 330;  // 검색 중 출/도착지 설정
    public final static int SCREEN_STATUS_SEARCH_EXPECTED_ROUTE         = 400;  // 검색 중 예상 경로

    public final static int SCREEN_STATUS_RUNNING_CALL                  = 600;

    private Context context;
    private BaseActivity activity;
    private LayoutInflater inflater;

    private boolean fullStatusBarStyleEnable;

    // 아이나비 맵
    private InaviMap mInaviMap;
    private InvMapView invMapView;

    // 액션바 버튼
    private View actionMenuBtn;
    private View actionBackBtn;
    private View actionExitBtn;

    // 메뉴
    private DrawerLayout drawerLayout;
    private View leftDrawerMenu;
    private View leftDrawerBtnContainer;
    private TextView menuUserMobileNumberText;
    private View menuTravelHistoryBtn;
    private View menuSettingBtn;
    private View menuCallCenterBtn;

    // 지도 정중앙 바로 호출 마커
    private View callDirectlyMarker;
    private TextView myLocationNameText;

    // 지도 영역 버튼
    private View mapBtnLayout;
    private View callCenterBtn;
    private View myLocationBtn;

    // 바로 호출 영역(최근 검색어 표시부 포함)
    private View callDirectlyControlLayout;
    private View homeBtn;
    private View officeBtn;
    private View searchBtn;

    private View frequencyVisitedLayout;
    private HorizontalScrollView frequencyVisitedScrollView;
    private LinearLayout frequencyVisitedContainer;

    // 바로 호출 출발지 확인 레이아웃
    private View callDirectlyConfirmView;
    private TextView callDirectlyPositionNameText;
    private TextView callDirectlyCancelBtn;
    private TextView callDirectlyOkBtn;

    // 배차 요청 커버
    private View requestCover;
    private TextView requestCoverText2;
    private TextView cancelCallBtn;

    // 이미지 뷰
    private ImageView callRequestImg;
    private ImageView tipImg;

    // 택시 운행 정보
    private View taxiInfoLayout;
    private TextView taxiInfoText1;
    private View taxiInfoText2;
    private TextView taxiInfoText21Location;
    private TextView taxiInfoText21Suffix;
    private TextView taxiInfoText22;
    private TextView taxiInfoTextArrive;
    private View carInfoLayout;
    private TextView carModelText;
    private TextView carNumberText;
    private TextView expectedRemainTimeText;

    private View taxiRunningBtnLayout;  // 택시 배차 후 기사님 이동, 승차시 버튼
    private TextView cancelCallBtn2;    // 호출 취소 버튼
    private View phoneCallDriverBtn;    // 기사님께 전화 버튼
    private View messageBtn;            // 안심메시지 버튼

    private TextView rateBtn;           // 운행 완료: 평가하기 버튼
    private TextView closeBtn;          // 운행 완료: 닫기 버튼

    // 검색
    private View searchLayout;
    private View searchActionBar;
    private View searchActionExitBtn;
    private View searchInputLayout;
    private EditText searchDepartureEdit;
    private EditText searchDestinationEdit;
    private View searchDepartureBtn;
    private View searchDestinationBtn;

    private View searchBtnLayout;
    private View searchResultHomeBtn;
    private View searchResultOfficeBtn;
    private ListView searchResultListView;
    private SearchResultBaseAdapter adapter;

    private View noSearchResultLayout;
    private View searchKeywordSuggestionLayout;
    private TextView searchKeywordSuggestionText;
    private View retryUsingSuggestionBtn;

    private TextView selectLocationBtn;

    private View callConfirmLayout;
    private TextView callConfirmBtn;
    //private TextView expectedFeeText;
    private TextView expectedDistanceText;
    private TextView expectedTimeText;

    private View searchProgressView;
    private View searchProgressViewCover;

    // 기타
    private View progressView;

    // 바로 호출 가이드
    private View directlyCallTipLayout;
    private View directlyCallTipContentLayout;
    private View directlyCallTipCloseBtn;
    private TextView directlyCallTipTitleText;
    private TextView directlyCallTipOkBtn;
    private boolean tipImgSettingOk = true;

    // 지도 마커 관련
    private LocationIcon userLocationIcon;
    private Bitmap markerBitmap;
    private InvImage markerImg;
    private InvMarker driverMarker;
    private InvMarker startPtMarker;
    private InvMarker endPtMarker;
    private InvMarker myPositionMarker; // 택시 승차 후 마커
    private InvImage userLocationIconImg;

    private InvMarker pathStartPtMarker;
    private InvMarker pathEndPtMarker;
    private InvRoute expectedRoutePath; // 택시 예상 경로

    // 데이터
    private int screenStatus;           // 메인 액티비티 화면 상태
    private MainModel mainModel;
    private int statusBarHeight;

    boolean downAnimationRunningFlag;
    boolean upAnimationRunningFlag;
    boolean downAnimationRunningFlag2;
    boolean upAnimationRunningFlag2;
    boolean downAnimationRunningFlag3;
    boolean upAnimationRunningFlag3;

    private boolean inRequest;              // 배차 요청 중(callId 받기 전)
    private boolean setSpendTime;           // 배차 후 승차 전 소요시간 표시를 위해(첫 번째 소요시간은 표시 안 하기 위해)
    private boolean setEditTextAlready;      // 검색 결과 터치 후 해당 위치로 이동할 때 주소를 받아오지 않게 하기 위해

    // 애니메이션
    private Animation bottomUpAnim;
    private Animation topDownAnim;
    private Animation bottomUpAnim2;
    private Animation topDownAnim2;
    private Animation bottomUpAnim3;
    private Animation topDownAnim3;
    private Animation scaleShowAnim;
    private Animation scaleHideAnim;
    private Animation blinkAnim;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Future<?> timerTask = null;
    private Future<?> carPositionTimerTask = null;

    private InputMethodManager inputMethodManager;

    private HoustonStyleDialogBuilder dialogBuilder;
    private HoustonStyleDialog currentDialog;

    private Typeface typeface;
    private Typeface boldTypeface;

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog callCenterDialog;

    private String[] selectedList;
    private boolean userTouchMap;

    private boolean lazySettingMapMarker;
    private LatLng userPosition;

    private Call<InaviApiSearchResult> searchTaskCall;  // 검색 작업

    public MainPresenter(BaseActivity activity, boolean fullStatusBarStyleEnable)
    {
        this.context = activity;
        this.activity = activity;
        this.fullStatusBarStyleEnable = fullStatusBarStyleEnable;
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        inflater = LayoutInflater.from(context);

        // 모델(데이터 관리) 생성
        mainModel = new MainModel(this.context);
        mainModel.setFrequencyVisited(activity);    // 최빈 방문 목적지 목록 설정
        mainModel.getCallCenterCheckFlag();         // 콜센터 플래그 제거

        AssetManager assetManager = context.getAssets();
        typeface = CommonAssets.getTypeface(assetManager);
        boldTypeface = CommonAssets.getBoldTypeface(assetManager);

        alertDialogBuilder = new AlertDialog.Builder(context);

        initView();

        initAnimation();

        String callId = mainModel.getCallId();
        if(callId != null && !callId.trim().isEmpty())
        {
            screenStatus = SCREEN_STATUS_RUNNING_CALL;
            setActionBarStatus();

            Intent intent = activity.getIntent();
            String callStatusStr = intent.getStringExtra("callStatus");

            try
            {
                mainModel.setCallStatus(callStatusStr);
            }
            catch(Exception e)
            {
                //e.printStackTrace();
                hideMainLayoutComponent();  // 모든 요소 감춤(다음 call check에서 처리)
            }

            int callStatus = mainModel.getCallStatus();
            if(callStatus == MainModel.CALL_STATUS_CANCEL ||
                    callStatus == MainModel.CALL_STATUS_CANCEL_BY_DRIVER)
            {
                // 앱을 닫았는데 호출이 자동 취쇠됨(디폴트 화면으로 설정)
                screenStatus = SCREEN_STATUS_DEFAULT;
                initStatusBarStyle();
                setActionBarStatus();

                // 취소 알림 다이얼로그 표시
                showCallCancelDialog();
            }
            else
            {
                setScreenLayout(callStatus, 0);
            }
        }
        else
        {
            screenStatus = SCREEN_STATUS_DEFAULT;
            initStatusBarStyle();
        }
    }

    private void initView()
    {
        // 상태바 높이 측정
        statusBarHeight = getStatusBarHeight();

        // 액션바 버튼 초기화
        initMainActionBar();

        // 왼쪽 사이드 메뉴 초기화
        initDrawerLayout();

        // 지도 관련 뷰 초기화
        initMapViews();

        // 바로 호출 영역
        initDirectlyCallViews();

        // 택시 운행 레이아웃
        initTaxiRunningViews();

        // 검색
        initSearchViews();

        // 기타
        initExtraViews();

        // 이미지 처리(OOM 이슈 때문에 따로 처리함)
        initImageViews();

        // 상단 마진 조정(상태바 전체 모드-레이아웃이 상태바 밑으로 들어가는 모드-의 경우)
        setViewsTopMargin();

        // 굵은 글꼴을 사용하는 TextView 설정
        setBoldTypefaceTextView();
    }

    private void initMainActionBar()
    {
        actionMenuBtn = activity.findViewById(R.id.action_btn_menu);
        actionBackBtn = activity.findViewById(R.id.action_btn_back);
        actionExitBtn = activity.findViewById(R.id.action_btn_exit);
        actionMenuBtn.setOnClickListener(actionBarBtnClickListener);
        actionBackBtn.setOnClickListener(actionBarBtnClickListener);
        actionExitBtn.setOnClickListener(actionBarBtnClickListener);
    }

    private void initDrawerLayout()
    {
        drawerLayout = activity.findViewById(R.id.drawer_layout);
        leftDrawerMenu = activity.findViewById(R.id.left_drawer_view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.setScrimColor(Color.parseColor("#33000000"));

        // 메뉴 리스너
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener()
        {
            @Override
            public void onDrawerSlide(@NonNull View view, float v)
            {

            }

            @Override
            public void onDrawerOpened(@NonNull View view)
            {
                try
                {

                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onDrawerClosed(@NonNull View view)
            {
                try
                {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onDrawerStateChanged(int i)
            {

            }
        });

        leftDrawerBtnContainer = activity.findViewById(R.id.menu_container);
        menuUserMobileNumberText = activity.findViewById(R.id.user_mobile_number);
        menuTravelHistoryBtn = activity.findViewById(R.id.btn_travel_history);
        menuSettingBtn = activity.findViewById(R.id.btn_setting);
        menuCallCenterBtn = activity.findViewById(R.id.btn_call_center2);

        // 휴대폰 전화번호 표시
        String mobileNum = mainModel.getMobileNum();
        int len = mobileNum.length();
        String first = mobileNum.substring(0, 3);       // 앞 세 자리
        String middle = mobileNum.substring(3, len-4);  // 중간 자리
        String last = mobileNum.substring(len-4);       // 마지막 네 자리
        mobileNum = String.format("%s-%s-%s", first, middle, last);
        menuUserMobileNumberText.setText(mobileNum);

        menuTravelHistoryBtn.setOnClickListener(buttonClickListener);
        menuSettingBtn.setOnClickListener(buttonClickListener);
        menuCallCenterBtn.setOnClickListener(buttonClickListener);

        menuUserMobileNumberText.setTypeface(boldTypeface);

        TextView menuTravelHistoryBtnText = activity.findViewById(R.id.btn_travel_history_text);
        TextView menuSettingBtnText = activity.findViewById(R.id.btn_setting_text);
        menuTravelHistoryBtnText.setTypeface(boldTypeface);
        menuSettingBtnText.setTypeface(boldTypeface);
    }

    private void initMapViews()
    {
        // 지도 정중앙 마커(바로 호출 버튼)
        callDirectlyMarker = activity.findViewById(R.id.marker_call_taxi_directly);
        myLocationNameText = activity.findViewById(R.id.main_my_position_address_text);
        callDirectlyMarker.setOnClickListener(buttonClickListener);

        // 지도 영역 버튼
        mapBtnLayout = activity.findViewById(R.id.bottom_btn_layout);
        callCenterBtn = activity.findViewById(R.id.btn_call_center);
        myLocationBtn = activity.findViewById(R.id.btn_map_my_location);
        callCenterBtn.setOnClickListener(buttonClickListener);
        myLocationBtn.setOnClickListener(buttonClickListener);

        initMapMarker();
    }

    private void initMapMarker()
    {
        // 지도 마커 리소스

        View iconUserLocationView = inflater.inflate(R.layout.ico_user_location_view, null);
        Bitmap iconBitmap = CommonUtil.createMarkerBitmapUsingView(iconUserLocationView, Bitmap.Config.ARGB_8888);
        userLocationIconImg = new InvImage(iconBitmap);

        startPtMarker = makeCustomMarker(R.drawable.map_pin_dep, R.id.marker_view, R.layout.marker_layout_point);
        endPtMarker = makeCustomMarker(R.drawable.map_pin_arr, R.id.marker_view, R.layout.marker_layout_point);
        myPositionMarker = makeCustomMarker(Integer.MAX_VALUE, Integer.MAX_VALUE, R.layout.marker_layout_my_position);
    }

    private void initDirectlyCallViews()
    {
        callDirectlyControlLayout = activity.findViewById(R.id.call_taxi_directly_controller);
        homeBtn = activity.findViewById(R.id.btn_my_home);
        officeBtn = activity.findViewById(R.id.btn_my_office);
        searchBtn = activity.findViewById(R.id.btn_search);
        homeBtn.setOnClickListener(buttonClickListener);
        officeBtn.setOnClickListener(buttonClickListener);
        searchBtn.setOnClickListener(buttonClickListener);

        frequencyVisitedLayout = activity.findViewById(R.id.frequency_visited_location_layout);
        frequencyVisitedScrollView = activity.findViewById(R.id.frequency_visited_scroll_view);
        frequencyVisitedContainer = activity.findViewById(R.id.frequency_visited_item_container);

        callDirectlyConfirmView = activity.findViewById(R.id.call_taxi_directly_confirm_view);
        callDirectlyPositionNameText = activity.findViewById(R.id.call_taxi_start_position_name_text);
        callDirectlyCancelBtn = activity.findViewById(R.id.btn_direct_call_cancel);
        callDirectlyOkBtn = activity.findViewById(R.id.btn_direct_call_ok);
        callDirectlyCancelBtn.setOnClickListener(buttonClickListener);
        callDirectlyOkBtn.setOnClickListener(buttonClickListener);

        addFrequencyVisitedLocationItems();
    }

    @SuppressWarnings("unchecked")
    public void refreshFrequencyVisitedLocations(HttpCallback callback)
    {
        RetrofitConnector.getFrequencyVisitedLocations(mainModel.getMobileNum(), resultObj ->
        {
            try
            {
                List<VisitedLocationData> dataList = null;
                if(resultObj != null)
                {
                    dataList = (List<VisitedLocationData>) resultObj;
                }

                mainModel.setFrequencyVisited(dataList);

                addFrequencyVisitedLocationItems();
            }
            catch(Exception e)
            {

            }

            // 콜백 무조건 실행(있다면)
            if(callback != null)
            {
                callback.onResult(true);
            }
        });
    }

    private void addFrequencyVisitedLocationItems()
    {
        activity.runOnUiThread(() ->
        {
            try
            {
                frequencyVisitedContainer.removeAllViews();

                List<VisitedLocationData> dataList = mainModel.getFrequencyVisited();
                if(dataList != null && dataList.size() > 0)
                {
                    int size = dataList.size();
                    for(int i = 0; i < size; i++)
                    {
                        VisitedLocationData data = dataList.get(i);
                        String destinationName = data.getDestinationName();

                        View view = inflater.inflate(R.layout.layout_frequency_visited_location, null);

                        TextView textView = view.findViewById(R.id.frequency_visited_location_name);
                        textView.setTypeface(typeface);
                        textView.setText(destinationName);

                        View marginRight = view.findViewById(R.id.margin_right);
                        marginRight.setVisibility((i < size-1) ? View.VISIBLE : View.GONE);

                        view.setTag(data);
                        view.setOnClickListener(visitedLocationClickListener);

                        frequencyVisitedContainer.addView(view);
                    }

                    frequencyVisitedLayout.setVisibility(View.VISIBLE);

                    frequencyVisitedScrollView.post(() ->
                    {
                        try
                        {
                            frequencyVisitedScrollView.scrollTo(0, 0);
                        }
                        catch(Exception e)
                        {

                        }
                    });
                }
                else
                {
                    frequencyVisitedLayout.setVisibility(View.GONE);
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    private View.OnClickListener visitedLocationClickListener = v ->
    {
        try
        {
            VisitedLocationData data = (VisitedLocationData) v.getTag();

            RequestLocation location = new RequestLocation();
            location.positionName = data.getDestinationName();
            location.lat = Double.parseDouble(data.getDestinationLat());
            location.lng = Double.parseDouble(data.getDestinationLng());

            // 목적지 설정 후 출발지 확인 화면으로 이동
            mainModel.setFavoriteLocationClickFlag(true);
            mainModel.setRequestDestinationLocation(location);
            onClickCallDirectlyMarker();
        }
        catch(Exception e)
        {

        }
    };

    private void initTaxiRunningViews()
    {
        taxiInfoLayout = activity.findViewById(R.id.taxi_info_layout);
        taxiInfoText1 = activity.findViewById(R.id.taxi_info_text1);

        taxiInfoText2 = activity.findViewById(R.id.taxi_info_text2_layout);
        taxiInfoText21Location = activity.findViewById(R.id.taxi_info_text2_1_location);
        taxiInfoText21Suffix = activity.findViewById(R.id.taxi_info_text2_1_suffix);
        taxiInfoText22 = activity.findViewById(R.id.taxi_info_text2_2);
        taxiInfoTextArrive = activity.findViewById(R.id.taxi_info_text_arrive);

        carInfoLayout = activity.findViewById(R.id.taxi_car_info_layout);
        carModelText = activity.findViewById(R.id.car_model_text);
        carNumberText = activity.findViewById(R.id.car_number_text);
        expectedRemainTimeText = activity.findViewById(R.id.expected_remain_time);

        taxiRunningBtnLayout = activity.findViewById(R.id.taxi_running_btn_layout);
        cancelCallBtn2 = activity.findViewById(R.id.btn_cancel_call2);
        phoneCallDriverBtn = activity.findViewById(R.id.btn_phone_call_driver);
        messageBtn = activity.findViewById(R.id.btn_message);

        requestCover = activity.findViewById(R.id.request_cover);
        requestCoverText2 = activity.findViewById(R.id.request_cover_text2);
        requestCoverText2.setTypeface(boldTypeface);
        cancelCallBtn = activity.findViewById(R.id.btn_cancel_call);

        rateBtn = activity.findViewById(R.id.btn_rate);
        closeBtn = activity.findViewById(R.id.btn_close);

        cancelCallBtn.setOnClickListener(buttonClickListener);
        cancelCallBtn2.setOnClickListener(buttonClickListener);
        phoneCallDriverBtn.setOnClickListener(buttonClickListener);
        messageBtn.setOnClickListener(buttonClickListener);
        rateBtn.setOnClickListener(buttonClickListener);
        closeBtn.setOnClickListener(buttonClickListener);

        taxiInfoText21Location.setTypeface(boldTypeface);
        callDirectlyCancelBtn.setTypeface(boldTypeface);
        callDirectlyOkBtn.setTypeface(boldTypeface);
        cancelCallBtn.setTypeface(boldTypeface);
        cancelCallBtn2.setTypeface(boldTypeface);

        taxiInfoText1.setTypeface(boldTypeface);
        carModelText.setTypeface(boldTypeface);
        carNumberText.setTypeface(boldTypeface);
        expectedRemainTimeText.setTypeface(boldTypeface);

        rateBtn.setTypeface(boldTypeface);
        closeBtn.setTypeface(boldTypeface);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchViews()
    {
        searchLayout = activity.findViewById(R.id.search_layout);
        searchActionBar = activity.findViewById(R.id.search_action_bar);
        searchActionExitBtn = activity.findViewById(R.id.search_action_btn_exit);
        searchInputLayout = activity.findViewById(R.id.search_input_layout);

        View searchInputLayoutShadow = activity.findViewById(R.id.search_input_layout_shadow);
        View searchInputLayoutContent = activity.findViewById(R.id.search_input_layout_content);
        searchInputLayoutContent.bringToFront();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            searchInputLayoutShadow.setOutlineSpotShadowColor(Color.parseColor("#1a86a4"));
            searchInputLayoutContent.setOutlineSpotShadowColor(Color.parseColor("#1a86a4"));
        }

        searchDepartureEdit = activity.findViewById(R.id.edit_departure);
        searchDestinationEdit = activity.findViewById(R.id.edit_destination);
        searchDepartureBtn = activity.findViewById(R.id.btn_search_departure);
        searchDestinationBtn = activity.findViewById(R.id.btn_search_destination);
        searchDepartureBtn.setOnClickListener(buttonClickListener);
        searchDestinationBtn.setOnClickListener(buttonClickListener);

        searchDepartureEdit.setOnClickListener(editClickListener);
        searchDestinationEdit.setOnClickListener(editClickListener);

        searchDepartureEdit.setOnTouchListener(editTouchListener);
        searchDestinationEdit.setOnTouchListener(editTouchListener);

        searchDepartureEdit.setOnFocusChangeListener(editFocusChangeListener1);
        searchDestinationEdit.setOnFocusChangeListener(editFocusChangeListener2);

        searchDepartureEdit.setOnEditorActionListener(editSearchKeyListener1);
        searchDestinationEdit.setOnEditorActionListener(editSearchKeyListener2);

        searchDepartureEdit.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {
                    if(screenStatus != SCREEN_STATUS_SEARCH_RESULT_SELECT)
                    {
                        int len  = searchDepartureEdit.getText().toString().length();
                        if(len <= 0 && searchBtnLayout.getVisibility() == View.GONE)
                        {
                            // 검색 결과 삭제
                            initSearchResultLayout();
                            searchDepartureBtn.setVisibility(View.GONE);
                        }
                        else
                        {
                            searchDepartureBtn.setVisibility((len > 0) ? View.VISIBLE : View.GONE);
                        }
                    }
                    else
                    {
                        searchDepartureBtn.setVisibility(View.GONE);
                    }
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        searchDestinationEdit.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {
                    if(screenStatus != SCREEN_STATUS_SEARCH_RESULT_SELECT)
                    {
                        int len  = searchDestinationEdit.getText().toString().length();
                        if(len <= 0 && searchBtnLayout.getVisibility() == View.GONE)
                        {
                            // 검색 결과 삭제
                            initSearchResultLayout();
                            searchDestinationBtn.setVisibility(View.GONE);
                        }
                        else
                        {
                            searchDestinationBtn.setVisibility((len > 0) ? View.VISIBLE : View.GONE);
                        }
                    }
                    else
                    {
                        searchDestinationBtn.setVisibility(View.GONE);
                    }
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        searchActionExitBtn.setOnClickListener(buttonClickListener);

        searchBtnLayout = activity.findViewById(R.id.search_result_btn_layout);
        searchResultHomeBtn = activity.findViewById(R.id.btn_search_home);
        searchResultOfficeBtn = activity.findViewById(R.id.btn_search_office);
        searchResultListView = activity.findViewById(R.id.search_result_list_view);
        searchResultListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                try
                {
                    if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    {
                        hideSoftKeyboard();
                    }
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {

            }
        });

        adapter = new SearchResultBaseAdapter(context);
        searchResultListView.setAdapter(adapter);
        searchResultListView.setOnItemClickListener(onSearchItemClickListener);

        searchResultHomeBtn.setOnClickListener(buttonClickListener);
        searchResultOfficeBtn.setOnClickListener(buttonClickListener);

        noSearchResultLayout = activity.findViewById(R.id.no_search_result_layout);
        searchKeywordSuggestionLayout = activity.findViewById(R.id.search_keyword_suggestion_layout);
        searchKeywordSuggestionText = activity.findViewById(R.id.search_keyword_suggestion_text);
        retryUsingSuggestionBtn = activity.findViewById(R.id.btn_retry_using_suggestion);
        retryUsingSuggestionBtn.setOnClickListener(buttonClickListener);

        selectLocationBtn = activity.findViewById(R.id.btn_select_location);
        selectLocationBtn.setOnClickListener(buttonClickListener);

        callConfirmLayout = activity.findViewById(R.id.call_taxi_confirm_view);
        callConfirmBtn = activity.findViewById(R.id.btn_call_ok);
        //expectedFeeText = activity.findViewById(R.id.expected_fee_text);
        expectedDistanceText = activity.findViewById(R.id.expected_distance_text);
        expectedTimeText = activity.findViewById(R.id.expected_time_text);
        callConfirmBtn.setOnClickListener(buttonClickListener);

        searchProgressView = activity.findViewById(R.id.search_progress);
        searchProgressViewCover = activity.findViewById(R.id.search_progress_cover);

        expectedDistanceText.setTypeface(boldTypeface);
        expectedTimeText.setTypeface(boldTypeface);
        searchKeywordSuggestionText.setTypeface(boldTypeface);
        selectLocationBtn.setTypeface(boldTypeface);
    }

    private void initExtraViews()
    {
        progressView = activity.findViewById(R.id.progress);
        dialogBuilder = new HoustonStyleDialogBuilder(context);

        directlyCallTipLayout = activity.findViewById(R.id.directly_call_tip_layout);
        directlyCallTipContentLayout = activity.findViewById(R.id.directly_call_tip_content_layout);
        directlyCallTipCloseBtn = activity.findViewById(R.id.btn_tip_close);
        directlyCallTipTitleText = activity.findViewById(R.id.tip_title_text);
        directlyCallTipOkBtn = activity.findViewById(R.id.btn_tip_directly_call_ok);

        directlyCallTipTitleText.setTypeface(boldTypeface);
        directlyCallTipOkBtn.setTypeface(boldTypeface);

        directlyCallTipCloseBtn.setOnClickListener(buttonClickListener);
        directlyCallTipOkBtn.setOnClickListener(buttonClickListener);
    }

    private void initImageViews()
    {
        callRequestImg = activity.findViewById(R.id.call_request_image);
        tipImg = activity.findViewById(R.id.tip_image);

        try
        {
            Glide.with(context).load(R.drawable.img_call).into(callRequestImg);
        }
        catch(OutOfMemoryError oom)
        {
            oom.printStackTrace();

            // Glide 이미지 로드 실패시 해당 이미지 설정하지 않음
            // ignore...
        }

        try
        {
            Glide.with(context).load(R.drawable.img_tip_directcall).into(tipImg);
        }
        catch(OutOfMemoryError oom)
        {
            oom.printStackTrace();

            // Glide 이미지 로드 실패. 샘플 사이즈를 줄여서 시도

            try
            {
                setImageViewBitmap(4, R.drawable.img_tip_directcall, tipImg);
            }
            catch(OutOfMemoryError oom2)
            {
                try
                {
                    setImageViewBitmap(8, R.drawable.img_tip_directcall, tipImg);
                }
                catch(OutOfMemoryError oom3)
                {
                    // 바로호출 가이드를 생략
                    tipImgSettingOk = false;
                }
            }
        }
    }

    private void setImageViewBitmap(int sampleSize, int drawableResId, ImageView imageView)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId, options);
        imageView.setImageBitmap(bitmap);
    }

    private void setViewsTopMargin()
    {
        if(fullStatusBarStyleEnable)
        {
            // SDK 버전 21부터 상태바 투명으로 변경이 가능해서, 이 버전부터 지도가 상태바 아래 들어간다.
            // 따라서 화면을 상태바 높이만큼 밑으로 내려야 한다.

            // 메인 화면 액션바
            View actionBar = activity.findViewById(R.id.action_bar);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) actionBar.getLayoutParams();
            layoutParams.topMargin = statusBarHeight;
            actionBar.setLayoutParams(layoutParams);

            // 운행 중 정보 표시 상단 마진 높이 설정(상태바 높이만큼 더함)
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) taxiInfoLayout.getLayoutParams();
            lp.topMargin = statusBarHeight + lp.topMargin;
            taxiInfoLayout.setLayoutParams(lp);

            // 검색 액션바
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) searchActionBar.getLayoutParams();
            lp2.topMargin = statusBarHeight;
            searchActionBar.setLayoutParams(lp2);

            // 검색창
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) searchInputLayout.getLayoutParams();
            lp3.topMargin = statusBarHeight + lp3.topMargin;
            searchInputLayout.setLayoutParams(lp3);

            // 사이드 메뉴
            RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) leftDrawerBtnContainer.getLayoutParams();
            lp4.topMargin = statusBarHeight + lp4.topMargin;
            leftDrawerBtnContainer.setLayoutParams(lp4);

            // 바로 호출 가이드
            RelativeLayout.LayoutParams lp5 = (RelativeLayout.LayoutParams) directlyCallTipContentLayout.getLayoutParams();
            lp5.topMargin = statusBarHeight;
            directlyCallTipContentLayout.setLayoutParams(lp5);

            // 검색창 프로그레스
            RelativeLayout.LayoutParams lp6 = (RelativeLayout.LayoutParams) searchProgressViewCover.getLayoutParams();
            lp6.topMargin += statusBarHeight;
            searchProgressViewCover.setLayoutParams(lp6);
        }
    }

    private void setBoldTypefaceTextView()
    {
        TextView phoneCallDriverBtnText = activity.findViewById(R.id.btn_phone_call_driver_text);
        TextView messageBtnText = activity.findViewById(R.id.btn_message_text);
        phoneCallDriverBtnText.setTypeface(boldTypeface);
        messageBtnText.setTypeface(boldTypeface);

        TextView directlyCallTitle = activity.findViewById(R.id.call_taxi_directly_controller_title);
        TextView requestCoverText1 = activity.findViewById(R.id.request_cover_text1);

        TextView directlyCallConfirmTitle = activity.findViewById(R.id.call_taxi_directly_confirm_view_title);
        directlyCallTitle.setTypeface(boldTypeface);
        requestCoverText1.setTypeface(boldTypeface);

        directlyCallConfirmTitle.setTypeface(boldTypeface);
    }

    public void initStatusBarStyle()
    {
        // 상태바 원래대로
        initStatusBarStyle(true);
    }

    public void initStatusBarStyleLightText()
    {
        initStatusBarStyle(false);
    }

    private void initStatusBarStyle(boolean textDark)
    {
        Window window = activity.getWindow();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if(textDark)
            {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR/*SKD 23부터 가능*/);
            }
            else
            {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            // 상태바 투명하게 변경(SDK 21부터 가능)
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            // 글자색 어둡게 표현 안 됨(23부터 가능)

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            // 상태바 투명하게 변경(SDK 21부터 가능)
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public int getStatusBarHeight()
    {
        Resources res = activity.getResources();
        int resId = res.getIdentifier("status_bar_height", "dimen", "android");
        return res.getDimensionPixelSize(resId);
    }

    public boolean isGpsAvailable()
    {
        return mainModel.isGpsAvailable();
    }

    public String getCallId()
    {
        return mainModel.getCallId();
    }

    public long getCallCenterDtime()
    {
        return mainModel.getCallCenterDtime();
    }

    public void removeCallCenterDtime()
    {
        mainModel.removeCallCenterDtime();
    }

    public int getCallStatus()
    {
        return mainModel.getCallStatus();
    }

    public RequestLocation getHomeLocationInfo()
    {
        return mainModel.getHomeInfo();
    }

    public RequestLocation getOfficeLocationInfo()
    {
        return mainModel.getOfficeInfo();
    }

    private void initAnimation()
    {
        // 하단 바로 호출 컨트롤 레이아웃 애니메이션 ///////////////////////////////////////

        // 아래에서 위로 나타나는 애니메이션
        bottomUpAnim = AnimationUtils.loadAnimation(context, R.anim.bottom_up_anim);
        bottomUpAnim.setDuration(200);
        bottomUpAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                upAnimationRunningFlag = false;

                try
                {
                    callDirectlyControlLayout.clearAnimation();
                    mapBtnLayout.clearAnimation();
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        bottomUpAnim2 = AnimationUtils.loadAnimation(context, R.anim.bottom_up_anim);
        bottomUpAnim2.setDuration(200);
        bottomUpAnim2.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                upAnimationRunningFlag2 = false;

                try
                {
                    callDirectlyConfirmView.clearAnimation();
                    mapBtnLayout.clearAnimation();
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        bottomUpAnim3 = AnimationUtils.loadAnimation(context, R.anim.bottom_up_anim);
        bottomUpAnim3.setDuration(200);
        bottomUpAnim3.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                upAnimationRunningFlag3 = false;

                try
                {
                    callConfirmLayout.clearAnimation();
                    mapBtnLayout.clearAnimation();
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        // 위에서 아래로 사라지는 애니메이션
        topDownAnim = AnimationUtils.loadAnimation(context, R.anim.top_down_anim);
        topDownAnim.setDuration(200);
        topDownAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                try
                {
                    callDirectlyControlLayout.setVisibility(View.VISIBLE);
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                downAnimationRunningFlag = false;

                try
                {
                    callDirectlyControlLayout.setVisibility(View.GONE);
                    callDirectlyControlLayout.clearAnimation();
                    mapBtnLayout.clearAnimation();
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        topDownAnim2 = AnimationUtils.loadAnimation(context, R.anim.top_down_anim);
        topDownAnim2.setDuration(200);
        topDownAnim2.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                try
                {
                    callDirectlyConfirmView.setVisibility(View.VISIBLE);
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                downAnimationRunningFlag2 = false;

                try
                {
                    callDirectlyConfirmView.setVisibility(View.GONE);
                    callDirectlyConfirmView.clearAnimation();
                    mapBtnLayout.clearAnimation();
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        topDownAnim3 = AnimationUtils.loadAnimation(context, R.anim.top_down_anim);
        topDownAnim3.setDuration(200);
        topDownAnim3.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                try
                {
                    callConfirmLayout.setVisibility(View.VISIBLE);
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                downAnimationRunningFlag3 = false;

                try
                {
                    callConfirmLayout.setVisibility(View.GONE);
                    callConfirmLayout.clearAnimation();
                    mapBtnLayout.clearAnimation();
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        // 콜센터 버튼 애니메이션

        // show
        scaleShowAnim = AnimationUtils.loadAnimation(context, R.anim.scale_show_anim);

        // hide
        scaleHideAnim = AnimationUtils.loadAnimation(context, R.anim.scale_hide_anim);
        scaleHideAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                try
                {
                    callCenterBtn.setVisibility(View.GONE);
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        blinkAnim = AnimationUtils.loadAnimation(context, R.anim.blink_anim);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void initScreenDefault(boolean anim)
    {
        screenStatus = SCREEN_STATUS_DEFAULT;

        // 지도 위 마커 모두 삭제
        if(driverMarker != null)
        {
            driverMarker.setMap(null);
        }

        if(myPositionMarker != null)
        {
            myPositionMarker.setMap(null);
        }

        if(startPtMarker != null)
        {
            startPtMarker.setMap(null);
        }

        if(endPtMarker != null)
        {
            endPtMarker.setMap(null);
        }

        if(pathStartPtMarker != null)
        {
            pathStartPtMarker.setMap(null);
            pathStartPtMarker = null;
        }

        if(pathEndPtMarker != null)
        {
            pathEndPtMarker.setMap(null);
            pathEndPtMarker = null;
        }

        // 경로 poly line 삭제
        if(expectedRoutePath != null)
        {
            expectedRoutePath.setMap(null);
            expectedRoutePath = null;
        }

        taxiRunningBtnLayout.setVisibility(View.GONE);
        taxiInfoLayout.setVisibility(View.GONE);
        callDirectlyConfirmView.setVisibility(View.GONE);
        hideRequestCover();

        initLocation(true);

        if(anim)
        {
            showMainBottomController(true);
        }
        else
        {
            callDirectlyControlLayout.setVisibility(View.VISIBLE);
            callCenterBtn.setVisibility(View.VISIBLE);
        }

        callConfirmLayout.setVisibility(View.GONE);
        callDirectlyMarker.setVisibility(View.VISIBLE);
    }

    private void setScreenLayout(int callStatus, int callStatusOld)
    {
        removeUserLocationIcon();

        userTouchMap = false;

        mainModel.setFavoriteLocationClickFlag(false);

        activity.runOnUiThread(() ->
        {
            try
            {
                // 지도 위 마커 삭제
                if(driverMarker != null)
                {
                    driverMarker.setMap(null);
                }

                if(myPositionMarker != null)
                {
                    myPositionMarker.setMap(null);
                }

                switch(callStatus)
                {
                    case MainModel.CALL_STATUS_ALLOC_WAIT:
                        showRequestCover();
                        hideMainLayoutComponent();
                        taxiRunningBtnLayout.setVisibility(View.GONE);
                        break;

                    case MainModel.CALL_STATUS_ALLOC:
                        hideRequestCover();
                        hideMainLayoutComponent();
                        getDriverInfo();    // 기사님 정보 가져오기
                        taxiRunningBtnLayout.setVisibility(View.VISIBLE);
                        cancelCallBtn2.setVisibility(View.VISIBLE);
                        phoneCallDriverBtn.setVisibility(View.VISIBLE);
                        messageBtn.setVisibility(View.GONE);
                        rateBtn.setVisibility(View.GONE);
                        closeBtn.setVisibility(View.GONE);

                        taxiInfoText2.setVisibility(View.GONE);
                        break;

                    case MainModel.CALL_STATUS_BOARD_ON:
                        hideRequestCover();
                        hideMainLayoutComponent();

                        taxiInfoText1.setVisibility(View.GONE);
                        taxiInfoTextArrive.setVisibility(View.GONE);

                        taxiInfoText21Location.setVisibility(View.VISIBLE);
                        taxiInfoText21Suffix.setVisibility(View.VISIBLE);
                        taxiInfoText22.setVisibility(View.VISIBLE);
                        taxiInfoText22.setGravity(Gravity.LEFT);

                        taxiInfoText2.setVisibility(View.VISIBLE);

                        expectedRemainTimeText.setVisibility(View.GONE);
                        carInfoLayout.setVisibility(View.GONE);
                        taxiInfoLayout.setVisibility(View.GONE);

                        LatLng destination = mainModel.getCallDestinationPoint();
                        if(destination == null)
                        {
                            taxiInfoText21Suffix.setText(context.getString(R.string.main_text_taxi_moving_message1_suffix));
                            taxiInfoText22.setText(context.getString(R.string.main_text_taxi_moving_message1));
                        }
                        else
                        {
                            // 도착지 이름 설정
                            String destinationName = mainModel.getCallDestinationName();
                            taxiInfoText21Location.setText(destinationName);
                            taxiInfoText21Suffix.setText(context.getString(R.string.main_text_taxi_moving_message2_suffix));
                            taxiInfoText22.setText(context.getString(R.string.main_text_taxi_moving_message2));
                        }

                        if(startPtMarker != null)
                        {
                            startPtMarker.setMap(null);
                        }

                        // 내 위치를 기반으로 이동 정보 가져오기
                        setCarPositionTask();

                        taxiRunningBtnLayout.setVisibility(View.VISIBLE);
                        cancelCallBtn2.setVisibility(View.GONE);
                        phoneCallDriverBtn.setVisibility(View.GONE);
                        messageBtn.setVisibility(View.VISIBLE);
                        rateBtn.setVisibility(View.GONE);
                        closeBtn.setVisibility(View.GONE);

                        // 기사님 정보 누락시 받아오기(안심메시지)
                        getDriverInfoWithoutLayoutSetting();
                        break;

                    case MainModel.CALL_STATUS_BOARD_OFF:
                        stopTimerTaskAll();

                        hideRequestCover();
                        hideMainLayoutComponent();

                        taxiInfoText1.setVisibility(View.GONE);
                        taxiInfoText21Location.setVisibility(View.GONE);
                        taxiInfoText21Suffix.setVisibility(View.GONE);

                        expectedRemainTimeText.setVisibility(View.GONE);
                        carInfoLayout.setVisibility(View.GONE);

                        taxiInfoText22.setText(context.getString(R.string.main_text_taxi_arrive_message1));
                        taxiInfoText22.setVisibility(View.VISIBLE);
                        taxiInfoText22.setGravity(Gravity.CENTER_HORIZONTAL);
                        taxiInfoTextArrive.setVisibility(View.VISIBLE);

                        taxiInfoText2.setVisibility(View.VISIBLE);
                        taxiInfoLayout.setVisibility(View.VISIBLE);

                        cancelCallBtn2.setVisibility(View.GONE);
                        phoneCallDriverBtn.setVisibility(View.GONE);
                        messageBtn.setVisibility(View.GONE);

                        taxiRunningBtnLayout.setVisibility(View.VISIBLE);
                        rateBtn.setVisibility(View.VISIBLE);
                        closeBtn.setVisibility(View.VISIBLE);

                        // 내 현재 위치 마커 추가
                        LatLng myPosition = mainModel.getLocationInstantly();
                        addMyRunningLocationMarker(myPosition);

                        // 내 위치로 지동 이동
                        CameraUpdate cameraUpdate = CameraUpdate.targetTo(myPosition, 16f);
                        cameraUpdate.setAnimationType(CameraAnimationType.Linear);
                        mInaviMap.moveCamera(cameraUpdate);

                        // 내 현재 위치 버튼 감춤
                        myLocationBtn.setVisibility(View.GONE);
                        break;

                    case MainModel.CALL_STATUS_CANCEL:
                    case MainModel.CALL_STATUS_CANCEL_BY_DRIVER:
                        // 경고창 보여주고 CallId 삭제 후 종료

                        if(currentDialog != null)
                        {
                            currentDialog.dismiss();
                        }

                        // 콜 체크 종료
                        stopTimerTaskAll();

                        // 커버 텍스트 애니메이션 종료
                        requestCoverText2.clearAnimation();

                        String title = null;
                        boolean autoCancelTaxiCall = false;
                        if(callStatus == MainModel.CALL_STATUS_CANCEL)
                        {
                            // 배차 요청 중 취소
                            title = context.getString(R.string.main_text_retry_request_title1);
                            autoCancelTaxiCall = true;
                        }
                        else if(callStatus == MainModel.CALL_STATUS_CANCEL_BY_DRIVER)
                        {
                            // 배차 후 취소
                            title = context.getString(R.string.main_text_retry_request_title2);
                        }
                        else
                        {
                            // 콜센터 연결 후 배차 취소되는 경우
                            screenStatus = SCREEN_STATUS_DEFAULT;
                            clearCallInfo();
                            return;
                        }

                        dialogBuilder
                                .setCancelable(false)
                                .setTitle(title).setSubTitle(context.getString(R.string.main_text_retry_request_sub_title))
                                .setPositiveButton(context.getString(R.string.main_text_retry_call_yes), () ->
                                {
                                    try
                                    {
                                        currentDialog = null;

                                        // 다시 호출
                                        clearCallInfo();
                                        requestCall(mainModel.getMobileNum(), mainModel.getRequestLocation(), mainModel.getRequestDestinationLocation());
                                    }
                                    catch(Exception e)
                                    {

                                    }
                                })
                                .setNegativeButton(context.getString(R.string.main_text_retry_call_no), () ->
                                {
                                    try
                                    {
                                        // 호출 취소 확인 알림
                                        boolean flag = (boolean) currentDialog.getData();

                                        String message = context.getString(R.string.main_text_call_cancel_complete);
                                        if(flag)
                                        {
                                            message = context.getString(R.string.main_text_call_cancel_complete2);
                                        }

                                        dialogBuilder.setAutoCancelable(true).setTitle(message);
                                        currentDialog = dialogBuilder.build();
                                        currentDialog.show();
                                        currentDialog = null;

                                        // 메인 화면으로 이동
                                        initStatusBarStyle();
                                        clearCallInfo();
                                        initScreenDefault(true);
                                        setActionBarStatus();
                                    }
                                    catch(Exception e)
                                    {

                                    }
                                });

                        currentDialog = dialogBuilder.build();
                        currentDialog.setData(autoCancelTaxiCall);
                        currentDialog.show();
                        return;
                }

                if(callStatus == MainModel.CALL_STATUS_ALLOC_WAIT)
                {
                    initStatusBarStyleLightText();
                }
                else
                {
                    initStatusBarStyle();
                }

                if(callStatus != MainModel.CALL_STATUS_ALLOC &&
                        callStatus != MainModel.CALL_STATUS_BOARD_ON)
                {
                    // 이동 중이 아닌 경우
                    if(startPtMarker != null)
                    {
                        startPtMarker.setMap(null);
                    }

                    if(endPtMarker != null)
                    {
                        endPtMarker.setMap(null);
                    }
                }
                else
                {
                    // 이동 중

                    LatLng departure = mainModel.getCallDeparturePoint();
                    LatLng destination = mainModel.getCallDestinationPoint();

                    // 지도에 출발지 마커 추가
                    startPtMarker.setPosition(departure);
                    startPtMarker.setMap(mInaviMap);

                    // 지도에 도착지 마커 추가
                    if(destination != null)
                    {
                        endPtMarker.setPosition(destination);
                        endPtMarker.setMap(mInaviMap);
                    }
                    else
                    {
                        endPtMarker.setMap(null);
                    }
                }
            }
            catch(Exception e)
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
            }
        });
    }

    private void showCallCancelDialog()
    {
        String title = context.getString(R.string.main_text_retry_request_title1);

        int callStatus = mainModel.getCallStatus();
        if(callStatus == MainModel.CALL_STATUS_CANCEL_BY_DRIVER)
        {
            title = context.getString(R.string.main_text_retry_request_title2);
        }

        dialogBuilder
                .setCancelable(false)
                .setTitle(title).setSubTitle(context.getString(R.string.main_text_retry_request_sub_title))
                .setPositiveButton(context.getString(R.string.main_text_retry_call_yes), () ->
                {
                    try
                    {
                        currentDialog = null;

                        // 다시 호출

                        LatLng departurePosition = mainModel.getCallDeparturePoint();
                        String departureName = mainModel.getCallDepartureName();
                        String departureAddress = mainModel.getCallDepartureAddress();

                        RequestLocation departure = new RequestLocation();
                        departure.lat = departurePosition.latitude;
                        departure.lng = departurePosition.longitude;
                        departure.positionName = departureName;
                        departure.address = departureAddress;

                        mainModel.setRequestLocation(departure);

                        LatLng destinationPosition = mainModel.getCallDeparturePoint();
                        if(destinationPosition != null)
                        {
                            String destinationName = mainModel.getCallDepartureName();
                            String destinationAddress = mainModel.getCallDepartureAddress();

                            RequestLocation destination = new RequestLocation();
                            destination.lat = destinationPosition.latitude;
                            destination.lng = departurePosition.longitude;
                            destination.positionName = destinationName;
                            destination.address = destinationAddress;

                            mainModel.setRequestDestinationLocation(destination);
                        }

                        clearCallInfo();
                        requestCall(mainModel.getMobileNum(), mainModel.getRequestLocation(), mainModel.getRequestDestinationLocation());
                    }
                    catch(Exception e)
                    {

                    }
                })
                .setNegativeButton(context.getString(R.string.main_text_retry_call_no), () ->
                {
                    try
                    {
                        // 호출 취소 확인 알림
                        String message = context.getString(R.string.main_text_call_cancel_complete2);
                        dialogBuilder.setAutoCancelable(true).setTitle(message);
                        currentDialog = dialogBuilder.build();
                        currentDialog.show();
                        currentDialog = null;

                        clearCallInfo();
                    }
                    catch(Exception e)
                    {

                    }
                });

        currentDialog = dialogBuilder.build();
        currentDialog.show();
    }

    private void hideMainLayoutComponent()
    {
        // 메인 화면 요소 모두 감춤
        callDirectlyMarker.setVisibility(View.GONE);
        callDirectlyControlLayout.setVisibility(View.GONE);
        callDirectlyConfirmView.setVisibility(View.GONE);
        myLocationNameText.setVisibility(View.GONE);
        callCenterBtn.setVisibility(View.GONE);
        callConfirmLayout.setVisibility(View.GONE);

        if(pathStartPtMarker != null)
        {
            pathStartPtMarker.setMap(null);
            pathStartPtMarker = null;
        }

        if(pathEndPtMarker != null)
        {
            pathEndPtMarker.setMap(null);
            pathEndPtMarker = null;
        }

        if(expectedRoutePath != null)
        {
            expectedRoutePath.setMap(null);
            expectedRoutePath = null;
        }

        if(driverMarker != null)
        {
            driverMarker.setMap(null);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 현재 위치로 위치 초기화
     */
    public void initLocation(boolean anim)
    {
        LatLng latLng = mainModel.getLocationInstantly();
        if(latLng != null)
        {
            if(screenStatus == SCREEN_STATUS_DEFAULT/*메인 디폴트 화면*/ ||
                    screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT/*출발지 확인*/ ||
                    (screenStatus == SCREEN_STATUS_RUNNING_CALL &&
                            mainModel.getCallStatus() == MainModel.CALL_STATUS_ALLOC)/*배차 후 승차 대기*/)
            {
                showUserLocationIcon(latLng);
            }
            else
            {
                removeUserLocationIcon();
            }

            CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);

            if(anim)
            {
                cameraUpdate.setAnimationType(CameraAnimationType.Linear);
            }

            mInaviMap.moveCamera(cameraUpdate);
        }
    }

    public void initLocationWithListener(boolean anim)
    {
        // 위치 정보를 가져올 때, 방금 위치 정보가 켜진 상태면
        // 바로 정보를 못 가져오는 경우가 있다. 위치 측정을 해야 그 다음부터 정상 작동됨.
        // 위치 정보를 가져오는 데 실패한 경우, 장소 리스너를 연결해서 위치를 받아온 후 처리하게 한다.
        // 사용자가 위치 정보 서비스를 OFF 했다가 설정 페이지에서 ON으로 바꾼 후에 발생

        LatLng latLng = mainModel.getLocationInstantly(new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                try
                {
                    // 위치 정보 측정 성공
                    activity.runOnUiThread(() ->
                    {
                        try
                        {
                            LatLng newLatLat = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraUpdate cameraUpdate = CameraUpdate.targetTo(newLatLat, 16f);

                            if(anim)
                            {
                                cameraUpdate.setAnimationType(CameraAnimationType.Linear);
                            }

                            mInaviMap.moveCamera(cameraUpdate);

                            showUserLocationIcon(newLatLat);

                            hideProgress();
                        }
                        catch(Exception e)
                        {

                        }
                    });

                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {

            }

            @Override
            public void onProviderEnabled(String provider)
            {

            }

            @Override
            public void onProviderDisabled(String provider)
            {

            }
        });

        if(latLng != null)
        {
            CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);

            if(anim)
            {
                cameraUpdate.setAnimationType(CameraAnimationType.Linear);
            }

            if(mInaviMap != null)
            {
                mInaviMap.moveCamera(cameraUpdate);
            }

            showUserLocationIcon(latLng);
        }
        else
        {
            // 위치 정보를 받지 못했다면 프로그레스를 보여주고 다른 조작을 막는다.
            showProgress();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setInaviMap(InaviMap inaviMap, InvMapView invMapView)
    {
        mInaviMap = inaviMap;
        userLocationIcon = mInaviMap.getLocationIcon();

        // 아이콘 이미지 설정
        userLocationIcon.setImage(userLocationIconImg);
        userLocationIcon.setCircleRadius((int) CommonUtil.convertDpToPixel(62f));
        userLocationIcon.setCircleColor(Color.parseColor("#1a0ab9b4"));
        userLocationIcon.setGlobalZIndex(1);

        // 지도 정지 후 처리
        mInaviMap.addOnCameraIdleListener(() ->
        {
            try
            {
                if(screenStatus == SCREEN_STATUS_SEARCH_RESULT_SELECT)
                {
                    // 검색 중 출발지/도착지 설정 화면
                    if(setEditTextAlready)
                    {
                        setEditTextAlready = false;
                    }
                    else
                    {
                        getCurrentPositionInfoInKeywordSearch();
                    }
                }
                else if(screenStatus != SCREEN_STATUS_RUNNING_CALL &&
                        screenStatus != SCREEN_STATUS_SEARCH_EXPECTED_ROUTE &&
                    !mainModel.isRequestUsingHistory())
                {
                    CameraPosition cameraPosition = inaviMap.getCameraPosition();
                    double lat = cameraPosition.target.latitude;
                    double lng = cameraPosition.target.longitude;
                    TextView textView = null;

                    if(screenStatus == SCREEN_STATUS_DEFAULT ||
                            screenStatus == SCREEN_STATUS_DEFAULT_FULL_MAP)
                    {
                        textView = myLocationNameText;
                    }
                    else if(screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT)
                    {
                        textView = callDirectlyPositionNameText;

                        if(myLocationNameText.getVisibility() == View.VISIBLE)
                        {
                            myLocationNameText.setVisibility(View.GONE);
                        }
                    }
                    else if(screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
                    {
                        if(myLocationNameText.getVisibility() == View.VISIBLE)
                        {
                            myLocationNameText.setVisibility(View.GONE);
                        }
                    }

                    getAddressByLatLng(lat, lng, textView);
                }
            }
            catch(Exception e)
            {

            }
        });

        // 지도 뷰 터치 이벤트 처리
        if(invMapView != null)
        {
            invMapView.setOnTouchListener((v, event) ->
            {
                try
                {
                    switch(event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            if(screenStatus == SCREEN_STATUS_RUNNING_CALL)
                            {
                                userTouchMap = true;
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if(screenStatus == SCREEN_STATUS_DEFAULT)
                            {
                                // 바로 호출 화면. 메인
                                screenStatus = SCREEN_STATUS_DEFAULT_FULL_MAP;
                                setActionBarStatus();
                                hideMainBottomController(true);
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                            if(screenStatus == SCREEN_STATUS_DEFAULT_FULL_MAP)
                            {
                                downAnimationRunningFlag = false;

                                // 지도 이동 화면에서 지도 터치 종료
                                screenStatus = SCREEN_STATUS_DEFAULT;
                                setActionBarStatus();
                                showMainBottomController(true);

                                // 현재 위치 주소를 받아서 마커 하단에 표시
                                getCurrentPositionAddress();
                            }
                            else if(screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT)
                            {
                                downAnimationRunningFlag2 = false;

                                // 바로 호출 확인 화면에서 지도 터치 종료
                                CameraPosition cameraPosition = mInaviMap.getCameraPosition();
                                double lat = cameraPosition.target.latitude;
                                double lng = cameraPosition.target.longitude;
                                getAddressByLatLng(lat, lng, callDirectlyPositionNameText);
                            }
                            break;
                    }
                }
                catch(Exception e)
                {

                }

                return false;
            });
        }

        if(lazySettingMapMarker)
        {
            // 승차 상태에서 앱이 시작되는 경우, 지도가 초기화되지 않은 상태에서 마커를 추가할 수 없기 때문에
            // 내 위치, 도착 마커를 지도 초기화 후에 다시 추가한다.
            // 경로의 경우는 아이나비 서버에서 검색 결과를 가져와야 해서 그 안에 지도가 초기화된다.
            lazySettingMapMarker = false;
            setMapMarkerWhenUserOnTaxi(userPosition, mainModel.getCallDestinationPoint());
        }
    }

    private void showUserLocationIcon(LatLng latLng)
    {
        if(userLocationIcon != null)
        {
            userLocationIcon.setPosition(latLng);
            userLocationIcon.setVisible(true);
        }
    }

    private void removeUserLocationIcon()
    {
        if(userLocationIcon != null)
        {
            userLocationIcon.setVisible(false);
        }
    }

    private void getCurrentPositionAddress()
    {
        CameraPosition cameraPosition = mInaviMap.getCameraPosition();
        double lat = cameraPosition.target.latitude;
        double lng = cameraPosition.target.longitude;
        getAddressByLatLng(lat, lng, myLocationNameText);
    }

    /**
     * 검색 중에 지도 위치가 바뀌는 경우. 역지오코딩으로 장소 정보 가져온 후 표시
     */
    private void getCurrentPositionInfoInKeywordSearch()
    {
        CameraPosition cameraPosition = mInaviMap.getCameraPosition();
        double lat = cameraPosition.target.latitude;
        double lng = cameraPosition.target.longitude;

        RetrofitConnector.getReverseGeocode(lat, lng, resultObj ->
        {
            try
            {
                if(resultObj != null)
                {
                    InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;

                    EditText editText = null;
                    int directionFlag = adapter.getDirectionFlag();
                    LatLng latLng = new LatLng(lat, lng);

                    if(directionFlag == 1)
                    {
                        editText = searchDepartureEdit;

                        mainModel.setRequestLocation(result, latLng);

                        startPtMarker.setPosition(latLng);
                        startPtMarker.setMap(mInaviMap);
                    }
                    else if(directionFlag == 2)
                    {
                        editText = searchDestinationEdit;
                        mainModel.setRequestDestinationLocation(result, latLng);

                        endPtMarker.setPosition(latLng);
                        endPtMarker.setMap(mInaviMap);
                    }

                    String text = CommonUtil.getReverseGeocodeString(result, false);
                    if(text != null && !text.trim().isEmpty())
                    {
                        editText.setText(text.trim());
                        selectLocationBtn.setEnabled(true);

                        if(adapter.getDirectionFlag() == 1)
                        {
                            selectLocationBtn.setText(context.getString(R.string.main_text_select_departure));
                        }
                        else
                        {
                            selectLocationBtn.setText(context.getString(R.string.main_text_select_destination));
                        }
                    }
                    else
                    {
                        // 정보 없음. 택시 호출 불가 지역
                        editText.setText("");
                        selectLocationBtn.setText(context.getString(R.string.main_text_invalid_location));
                        selectLocationBtn.setEnabled(false);
                    }
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    private void getAddressByLatLng(double lat, double lng, TextView textView)
    {
        RetrofitConnector.getReverseGeocode(lat, lng, resultObj ->
        {
            boolean hasAddress = false;

            try
            {
                if(resultObj != null)
                {
                    InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                    String text = CommonUtil.getReverseGeocodeString(result, false);
                    hasAddress = (text != null && !text.trim().isEmpty());

                    if(hasAddress)
                    {
                        textView.setText(text);
                    }

                    if(textView.getId() == R.id.main_my_position_address_text)
                    {
                        // 바로 호출 마커 아래 주소를 가져올 때 검색창 출발지 힌트 문구도 함께 설정
                        searchDepartureEdit.setHint(myLocationNameText.getText().toString());
                    }

                    // 바로 호출시를 위해 위치 정보 저장해두기
                    if(hasAddress)
                    {
                        mainModel.setRequestLocation(result, new LatLng(lat, lng));
                    }
                    else
                    {
                        InaviApiReverseGeocode nullObj = null;
                        mainModel.setRequestLocation(null, null);
                    }
                }
            }
            catch(Exception e)
            {

            }

            if(!hasAddress)
            {
                // 바로 호출 불가
                callDirectlyMarker.setEnabled(false);

                // 받아온 주소가 없다.
                if(screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT)
                {
                    callDirectlyOkBtn.setEnabled(false);
                    textView.setText(context.getString(R.string.main_text_invalid_location));
                }
                else
                {
                    textView.setVisibility(View.GONE);
                }
            }
            else
            {
                // 바로 호출 가능
                callDirectlyMarker.setEnabled(true);

                textView.setVisibility(View.VISIBLE);

                if(screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT)
                {
                    callDirectlyOkBtn.setEnabled(true);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public boolean canIQuit()
    {
        if(drawerLayout.isDrawerOpen(leftDrawerMenu))
        {
            return false;
        }
        else if(directlyCallTipLayout.getVisibility() == View.VISIBLE)
        {
            return false;
        }
        else if(screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT)
        {
            return false;
        }
        else if(screenStatus == SCREEN_STATUS_RUNNING_CALL && inRequest)
        {
            return false;
        }
        else if(screenStatus == SCREEN_STATUS_RUNNING_CALL &&
                mainModel.getCallStatus() == MainModel.CALL_STATUS_BOARD_OFF)
        {
            return false;
        }
        else if(screenStatus == SCREEN_STATUS_SEARCH_RESULT_SELECT ||
                screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
        {
            return false;
        }
        else if(searchLayout.getVisibility() == View.VISIBLE ||
                searchInputLayout.getVisibility() == View.VISIBLE)
        {
            return false;
        }

        return true;
    }

    public void onResume()
    {
        // 백그라운드 서비스에서 콜센터 체크를 했는지(했다면 접수가 이루어졌는지)
        boolean callCenterCheckFlag = mainModel.getCallCenterCheckFlag();
        if(callCenterCheckFlag)
        {
            // 콜체크를 시작하게 화면 상태를 전환한다.
            screenStatus = SCREEN_STATUS_RUNNING_CALL;
        }

        if(screenStatus == SCREEN_STATUS_RUNNING_CALL)
        {
            // 콜 체크 시작
            int callStatus = mainModel.getCallStatus();
            if(callStatus != MainModel.CALL_STATUS_BOARD_OFF &&
                    callStatus != MainModel.CALL_STATUS_CANCEL &&
                    callStatus != MainModel.CALL_STATUS_CANCEL_BY_DRIVER)
            {
                setCallCheckTask();
            }

            // 위치 확인
            setCarPositionTask();
        }
        else if(screenStatus == SCREEN_STATUS_DEFAULT)
        {
            // 현재 위치로 이동
            initLocationWithListener(true);

            // 콜센터 체크
            long now = System.currentTimeMillis();
            long callCenterDtime = mainModel.getCallCenterDtime();
            if(callCenterDtime > 0)
            {
                // 1분 이내라면, 2초 주기로 콜 상태 확인
                if(now - callCenterDtime <= CallCheckService.ONE_MIN_IN_MILLS)
                {
                    setCallCenterCheckTask();
                }
                else
                {
                    mainModel.removeCallCenterDtime();
                }
            }
        }
    }

    public void onPause()
    {
        if(screenStatus == SCREEN_STATUS_RUNNING_CALL)
        {
            // timer 작업 모두 종료
            stopTimerTaskAll();

            userTouchMap = false;
        }

        // 검색 중이라면 키보드 감춤
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard()
    {
        if(searchDepartureEdit.hasFocus())
        {
            inputMethodManager.hideSoftInputFromWindow(searchDepartureEdit.getWindowToken(), 0);
            searchDepartureEdit.clearFocus();
        }
        else if(searchDestinationEdit.hasFocus())
        {
            inputMethodManager.hideSoftInputFromWindow(searchDestinationEdit.getWindowToken(), 0);
            searchDestinationEdit.clearFocus();
        }
    }

    public void onDestroy()
    {
        try
        {
            if(!scheduler.isShutdown())
            {
                scheduler.shutdown();
            }
        }
        catch(Exception e)
        {

        }
    }

    public void onBackPressed()
    {
        if(drawerLayout.isDrawerOpen(leftDrawerMenu))
        {
            drawerLayout.closeDrawer(leftDrawerMenu, true);
        }
        else if(directlyCallTipLayout.getVisibility() == View.VISIBLE)
        {
            mainModel.setDirectlyCallTip();
            directlyCallTipLayout.setVisibility(View.GONE);
        }
        else if(screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT)
        {
            onClickCallDirectlyCancelBtn();
        }
        else if(screenStatus == SCREEN_STATUS_SEARCH_RESULT_SELECT ||
                screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
        {
            if(screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
            {
                if(mainModel.isFavoriteLocationClickFlag())
                {
                    // 전 화면으로 이동(출발지 확인)
                    onBackCheckFavoriteStartPoint();
                }
                else if(mainModel.isRequestUsingHistory())
                {
                    // 메인 화면으로 전환
                    hideSearchLayout();
                    setActionBarStatus();
                }
                else
                {
                    // 검색 화면으로 전환(검색 초기화)
                    initSearchResultLayout();
                    searchDepartureEdit.setText("");
                    searchDestinationEdit.setText("");
                    backToSearchMode();
                }
            }
            else
            {
                // 검색 화면으로 전환(검색 초기화)
                initSearchResultLayout();
                searchDepartureEdit.setText("");
                searchDestinationEdit.setText("");
                backToSearchMode();
            }
        }
        else if(searchLayout.getVisibility() == View.VISIBLE ||
            searchInputLayout.getVisibility() == View.VISIBLE)
        {
            if(searchLayout.getVisibility() == View.VISIBLE)
            {
                hideSearchLayout();
                setActionBarStatus();
            }
            else
            {
                // 기존 검색 화면으로 돌아간다.
                showSearchLayout(false);
            }
        }
    }

    private void onBackCheckFavoriteStartPoint()
    {
        mainModel.setFavoriteLocationClickFlag(true);

        if(pathStartPtMarker != null)
        {
            pathStartPtMarker.setMap(null);
        }

        if(pathEndPtMarker != null)
        {
            pathEndPtMarker.setMap(null);
        }

        if(expectedRoutePath != null)
        {
            expectedRoutePath.setMap(null);
            expectedRoutePath = null;
        }

        hideCallConfirmView(true);

        new Handler().postDelayed(() ->
        {
            try
            {
                activity.runOnUiThread(() ->
                {
                    try
                    {
                        // 출발지 확인 레이아웃 보여주기
                        onClickCallDirectlyMarker();

                        // 사용자 위치 마커 표시
                        LatLng userLocation = mainModel.getLocationInstantly();
                        showUserLocationIcon(userLocation);

                        // 이전 출발지 위치로 지도 이동
                        RequestLocation location = mainModel.getRequestLocation();
                        LatLng latLng = new LatLng(location.lat, location.lng);

                        CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);
                        cameraUpdate.setAnimationType(CameraAnimationType.Linear);
                        mInaviMap.moveCamera(cameraUpdate);
                    }
                    catch(Exception e)
                    {

                    }
                });
            }
            catch(Exception e)
            {

            }
        }, 200);
    }

    private void backToSearchMode()
    {
        int directionFlag = adapter.getDirectionFlag();
        int count = adapter.getCount();
        if(count > 0)
        {
            searchResultListView.setSelection(0);
        }

        showSearchLayout(false);
        int oldScreenStatus = screenStatus;
        screenStatus = SCREEN_STATUS_DEFAULT;

        if(directionFlag == 1)
        {
            searchDepartureEdit.setText(adapter.getKeyword());
            searchDepartureEdit.requestFocus();
            searchDepartureEdit.setSelection(searchDepartureEdit.length());
            mainModel.setRequestLocation(null);

            refreshDeparturePosition();
        }
        else if(directionFlag == 2)
        {
            searchDestinationEdit.setText(adapter.getKeyword());
            searchDestinationEdit.requestFocus();
            searchDestinationEdit.setSelection(searchDestinationEdit.length());
            mainModel.setRequestDestinationLocation(null);
        }
        else
        {
            searchDestinationEdit.requestFocus();

            mainModel.setRequestLocation(null);
            mainModel.setRequestDestinationLocation(null);

            refreshDeparturePosition();
        }

        if(startPtMarker != null)
        {
            startPtMarker.setMap(null);
        }

        if(endPtMarker != null)
        {
            endPtMarker.setMap(null);
        }

        if(oldScreenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
        {
            // 경로검색 관련 요소 모두 삭제

            if(pathStartPtMarker != null)
            {
                pathStartPtMarker.setMap(null);
            }

            if(pathEndPtMarker != null)
            {
                pathEndPtMarker.setMap(null);
            }

            // 경로 poly line 삭제
            if(expectedRoutePath != null)
            {
                expectedRoutePath.setMap(null);
            }

            // 호출 확인 버튼 레이아웃 감춤
            hideCallConfirmView(false);
        }

        setActionBarStatus();
        selectLocationBtn.setVisibility(View.GONE);
        setEditTextAlready = false;

        //adapter.setDirectionFlag(0);
    }

    private void refreshDeparturePosition()
    {
        // 현재 위치 가져와서 재설정
        LatLng latLng = mainModel.getLocationInstantly();
        if(latLng == null)
        {
            return;
        }

        double lat = latLng.latitude;
        double lng = latLng.longitude;

        RetrofitConnector.getReverseGeocode(lat, lng, resultObj ->
        {
            try
            {
                if(resultObj != null)
                {
                    InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                    String text = CommonUtil.getReverseGeocodeString(result, false);

                    if(text != null && !text.trim().isEmpty())
                    {
                        searchDepartureEdit.setHint(text);
                    }
                    else
                    {
                        searchDepartureEdit.setHint(null);
                    }

                    mainModel.setRequestLocation(result, new LatLng(lat, lng));
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    public void showProgress()
    {
        activity.runOnUiThread(() ->
        {
            try
            {
                progressView.setVisibility(View.VISIBLE);
            }
            catch(Exception e)
            {

            }
        });
    }

    public void hideProgress()
    {
        activity.runOnUiThread(() ->
        {
            try
            {
                progressView.setVisibility(View.GONE);
            }
            catch(Exception e)
            {

            }
        });
    }

    private void stopTimerTaskAll()
    {
        // timer 작업 모두 종료
        if(timerTask != null)
        {
            timerTask.cancel(true);
            timerTask = null;
        }

        if(carPositionTimerTask != null)
        {
            carPositionTimerTask.cancel(true);
            carPositionTimerTask = null;
        }
    }

    public void showSearchProgress()
    {
        activity.runOnUiThread(() ->
        {
            try
            {
                searchProgressView.setVisibility(View.VISIBLE);
            }
            catch(Exception e)
            {

            }
        });
    }

    public void hideSearchProgress()
    {
        activity.runOnUiThread(() ->
        {
            try
            {
                searchProgressView.setVisibility(View.GONE);
            }
            catch(Exception e)
            {

            }
        });
    }

    public void showRequestCover()
    {
        activity.runOnUiThread(() ->
        {
            try
            {
                requestCover.setVisibility(View.VISIBLE);
                requestCoverText2.startAnimation(blinkAnim);
            }
            catch(Exception e)
            {

            }
        });
    }

    public void hideRequestCover()
    {
        activity.runOnUiThread(() ->
        {
            try
            {
                requestCover.setVisibility(View.GONE);
                requestCoverText2.clearAnimation();
            }
            catch(Exception e)
            {

            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private View.OnClickListener actionBarBtnClickListener = v ->
    {
        try
        {
            switch(v.getId())
            {
                case R.id.action_btn_menu:
                    if(screenStatus == SCREEN_STATUS_DEFAULT)
                    {
                        if(!drawerLayout.isDrawerOpen(leftDrawerMenu))
                        {
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                            drawerLayout.openDrawer(leftDrawerMenu, true);
                        }
                    }
                    break;

                case R.id.action_btn_back:
                    if(screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
                    {
                        // 다시 검색 화면으로 변경
                        if(mainModel.isFavoriteLocationClickFlag())
                        {
                            // 전 화면으로 이동(출발지 확인)
                            onBackCheckFavoriteStartPoint();
                        }
                        else if(mainModel.isRequestUsingHistory())
                        {
                            // 메인 화면으로 전환
                            hideSearchLayout();
                            setActionBarStatus();
                        }
                        else
                        {
                            initSearchResultLayout();
                            searchDepartureEdit.setText("");
                            searchDestinationEdit.setText("");
                            backToSearchMode();
                        }
                    }
                    break;

                case R.id.action_btn_exit:
                    if(screenStatus == SCREEN_STATUS_SEARCH_RESULT_SELECT)
                    {
                        if(mainModel.isFavoriteLocationClickFlag() ||
                                mainModel.isRequestUsingHistory())
                        {
                            // 메인 화면으로 변경
                            hideSearchLayout();
                            setActionBarStatus();
                        }
                        else
                        {
                            // 검색 모드로 전환
                            initSearchResultLayout();
                            searchDepartureEdit.setText("");
                            searchDestinationEdit.setText("");
                            backToSearchMode();
                        }
                    }
                    else if(screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
                    {
                        // 메인 화면으로 변경
                        hideSearchLayout();
                        setActionBarStatus();
                    }
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private View.OnClickListener editClickListener = v ->
    {
        try
        {
            if(screenStatus == SCREEN_STATUS_SEARCH_RESULT_SELECT)
            {
                int directionFlag = adapter.getDirectionFlag();
                backToSearchInSelectionMode(v, directionFlag);
            }
            else if(searchLayout.getVisibility() != View.VISIBLE)
            {
                searchLayout.setVisibility(View.VISIBLE);
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener editTouchListener = (v, event) ->
    {
        try
        {
            if(!v.hasFocus() && screenStatus == SCREEN_STATUS_SEARCH_RESULT_SELECT)
            {
                int directionFlag = adapter.getDirectionFlag();
                if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    backToSearchInSelectionMode(v, directionFlag);
                    return true;
                }
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }

        return false;
    };

    private void backToSearchInSelectionMode(View v, int directionFlag)
    {
        if(v.getId() == R.id.edit_departure)
        {
            adapter.setDirectionFlag(1);
        }
        else if(v.getId() == R.id.edit_destination)
        {
            adapter.setDirectionFlag(2);
        }

        if(directionFlag != adapter.getDirectionFlag())
        {
            setSearchBtnLayout(true);
            setSearchResultList(false);
            setNoSearchResultLayout(true, null);

            adapter.setLocationList(null);
            adapter.setKeyword(null);
            adapter.notifyDataSetInvalidated();
        }

        backToSearchMode();
    }

    private View.OnFocusChangeListener editFocusChangeListener1 = (v, hasFocus) ->
    {
        try
        {
            int hintColor = hasFocus ? Color.parseColor("#b3ffffff") : Color.parseColor("#66ffffff");
            searchDepartureEdit.setHintTextColor(hintColor);

            if(hasFocus)
            {
                inputMethodManager.showSoftInput(searchDepartureEdit, InputMethodManager.SHOW_FORCED);

                // 도착지 검색 중이었는지 확인
                String text = searchDestinationEdit.getText().toString();
                if(text != null && !text.trim().isEmpty())
                {
                    // 검색 중이던 도착지가 있었음. 검색 종료.
                    initSearchResultLayout();

                    RequestLocation destination = mainModel.getRequestDestinationLocation();
                    if(destination != null)
                    {
                        // 설정된 도착지를 hint text로 설정해준다.
                        searchDestinationEdit.setHint(destination.positionName);
                    }

                    // 도착지 검색창 지움
                    searchDestinationEdit.setText("");
                }
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private View.OnFocusChangeListener editFocusChangeListener2 = (v, hasFocus) ->
    {
        try
        {
            int hintColor = hasFocus ? Color.parseColor("#b3ffffff") : Color.parseColor("#66ffffff");
            searchDestinationEdit.setHintTextColor(hintColor);

            if(hasFocus)
            {
                inputMethodManager.showSoftInput(searchDestinationEdit, InputMethodManager.SHOW_FORCED);

                // 출발지 검색 중이었는지 확인
                String text = searchDepartureEdit.getText().toString();
                if(text != null && !text.trim().isEmpty())
                {
                    // 검색 중이던 출발지가 있었음. 검색 종료.
                    initSearchResultLayout();

                    RequestLocation departure = mainModel.getRequestLocation();
                    if(departure != null)
                    {
                        // 설정된 출발지를 hint text로 설정해준다.
                        searchDepartureEdit.setHint(departure.positionName);
                    }

                    // 출발지 검색창 지움
                    searchDepartureEdit.setText("");
                }
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private TextView.OnEditorActionListener editSearchKeyListener1 = (v, actionId, event) ->
    {
        try
        {
            if(actionId == EditorInfo.IME_ACTION_SEARCH)
            {
                searchDeparture();
            }
        }
        catch(Exception e)
        {

        }

        return false;
    };

    private TextView.OnEditorActionListener editSearchKeyListener2 = (v, actionId , event) ->
    {
        try
        {
            if(actionId == EditorInfo.IME_ACTION_SEARCH)
            {
                searchDestination();
            }
        }
        catch(Exception e)
        {

        }

        return false;
    };

    private void searchDeparture()
    {
        inputMethodManager.hideSoftInputFromWindow(searchDepartureEdit.getWindowToken(), 0);

        // 검색 버튼 클릭
        String keyword = searchDepartureEdit.getText().toString();
        searchByKeyword(keyword, 1);
    }

    private void searchDestination()
    {
        inputMethodManager.hideSoftInputFromWindow(searchDestinationEdit.getWindowToken(), 0);

        String keyword = searchDestinationEdit.getText().toString();
        searchByKeyword(keyword, 2);
    }

    /**
     * 검색 결과 선택
     */
    private AdapterView.OnItemClickListener onSearchItemClickListener = (parent, view, position, id) ->
    {
        try
        {
            SearchResultBaseAdapter.ViewHolder holder = (SearchResultBaseAdapter.ViewHolder) view.getTag();
            RequestLocation location = (RequestLocation) holder.selectBtn.getTag();
            onSelectLocation(location, adapter.getDirectionFlag());
        }
        catch(Exception e)
        {

        }
    };

    private void onSelectLocation(RequestLocation selectedItem, int directionFlag)
    {
        // 사용자 현재 위치 마커 표시
        LatLng userLocation = mainModel.getLocationInstantly();
        showUserLocationIcon(userLocation);

        screenStatus = SCREEN_STATUS_SEARCH_RESULT_SELECT;
        setEditTextAlready = true;

        searchDepartureEdit.clearFocus();
        searchDestinationEdit.clearFocus();

        if(directionFlag == 1/*출발지 선택*/)
        {
            inputMethodManager.hideSoftInputFromWindow(searchDepartureEdit.getWindowToken(), 0);

            if(endPtMarker != null)
            {
                endPtMarker.setMap(null);
            }

            hideMainLayoutComponent();

            LatLng latLng = new LatLng(selectedItem.lat, selectedItem.lng);
            startPtMarker.setPosition(latLng);
            startPtMarker.setMap(mInaviMap);

            CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);
            mInaviMap.moveCamera(cameraUpdate);

            mainModel.setRequestLocation(selectedItem);

            selectLocationBtn.setText(context.getString(R.string.main_text_select_departure));
            ColorStateList colorStateList = CommonUtil.getBtnTextColorStateList(
                    "#3c3c3c", "#383838", "#b8b8b8");
            selectLocationBtn.setTextColor(colorStateList);

            searchDepartureEdit.setText(selectedItem.positionName);
        }
        else    // 도착지 선택
        {
            inputMethodManager.hideSoftInputFromWindow(searchDestinationEdit.getWindowToken(), 0);

            if(startPtMarker != null)
            {
                startPtMarker.setMap(null);
            }

            hideMainLayoutComponent();

            LatLng latLng = new LatLng(selectedItem.lat, selectedItem.lng);
            endPtMarker.setPosition(latLng);
            endPtMarker.setMap(mInaviMap);

            CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);
            mInaviMap.moveCamera(cameraUpdate);

            mainModel.setRequestDestinationLocation(selectedItem);

            selectLocationBtn.setText(context.getString(R.string.main_text_select_destination));
            ColorStateList colorStateList = CommonUtil.getBtnTextColorStateList(
                    "#e25321", "#d54e1e", "#b8b8b8");
            selectLocationBtn.setTextColor(colorStateList);

            searchDestinationEdit.setText(selectedItem.positionName);
        }

        setActionBarStatus();
        searchLayout.setVisibility(View.GONE);
        selectLocationBtn.setVisibility(View.VISIBLE);
        selectLocationBtn.setEnabled(true);
    }

    private View.OnClickListener buttonClickListener = v ->
    {
        try
        {
            int directionFlag = 0;
            Intent intent = null;
            Uri uri = null;
            String message = "";

            switch(v.getId())
            {
                case R.id.marker_call_taxi_directly:
                    // 바로 호출. 출발지 확인으로 전환
                    onClickCallDirectlyMarker();
                    break;

                case R.id.btn_direct_call_cancel:
                    // 바로 호출 취소 화면. 메인 화면으로 복귀
                    onClickCallDirectlyCancelBtn();
                    break;

                case R.id.btn_direct_call_ok:
                    // 바로 호출 확인 버튼
                    if(mainModel.isFavoriteLocationClickFlag())
                    {
                        // 예상 경로 화면으로 이동
                        if(mainModel.canCall())
                        {
                            if(!mainModel.sameTwoPoints())
                            {
                                hideMainDirectCallCheckView(true);
                                callDirectlyMarker.setVisibility(View.GONE);
                            }

                            onSelectLocation();
                        }
                    }
                    else
                    {
                        if(mainModel.readDirectlyCallTip())
                        {
                            // 호출 확정.
                            onClickCallOkBtn();
                        }
                        else
                        {
                            // 바로 호출 최초 가이드 보여주기
                            if(tipImgSettingOk)
                            {
                                directlyCallTipLayout.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                // 이미지 처리에 실패했다면 가이드 생략(다음에 다시 시도함)
                                // 그대로 호출 확정
                                onClickCallOkBtn();
                            }
                        }
                    }
                    break;

                case R.id.btn_call_ok:
                    // 출발지, 도착지 확정 후 호출
                    // 호출 확정.
                    onClickCallOkBtn();
                    break;

                case R.id.btn_call_center:
                case R.id.btn_call_center2:
                    // 사이드 메뉴 닫기
                    if(drawerLayout.isDrawerOpen(leftDrawerMenu))
                    {
                        drawerLayout.closeDrawer(leftDrawerMenu, true);

                        new Handler().postDelayed(() ->
                        {
                            try
                            {
                                activity.runOnUiThread(() ->
                                {
                                    try
                                    {
                                        showCallCenterDialog();
                                    }
                                    catch(Exception e)
                                    {

                                    }
                                });
                            }
                            catch(Exception e)
                            {

                            }
                        }, 200);    // 사이드 메뉴 애니메이션 처리를 기다린다.
                        break;
                    }
                    else
                    {
                        showCallCenterDialog();
                    }
                    break;

                case R.id.btn_travel_history:
                    // 메뉴 > 탑승이력

                    // 사이드 메뉴 닫기
                    if(drawerLayout.isDrawerOpen(leftDrawerMenu))
                    {
                        drawerLayout.closeDrawer(leftDrawerMenu, true);
                    }

                    new Handler().postDelayed(() ->
                    {
                        try
                        {
                            // 메뉴 > 설정
                            Intent intent1 = new Intent(context, HistoryActivity.class);
                            activity.startActivityForResult(intent1, AppConstant.REQ_CODE_MENU_HISTORY);
                        }
                        catch(Exception e)
                        {

                        }
                    }, 200);    // 사이드 메뉴 애니메이션 처리를 기다린다.
                    break;

                case R.id.btn_setting:
                    // 사이드 메뉴 닫기
                    if(drawerLayout.isDrawerOpen(leftDrawerMenu))
                    {
                        drawerLayout.closeDrawer(leftDrawerMenu, true);
                    }

                    new Handler().postDelayed(() ->
                    {
                        try
                        {
                            // 메뉴 > 설정
                            Intent intent1 = new Intent(context, MenuSettingActivity.class);
                            activity.startActivityForResult(intent1, AppConstant.REQ_CODE_MENU_SETTING);
                        }
                        catch(Exception e)
                        {

                        }
                    }, 200);    // 사이드 메뉴 애니메이션 처리를 기다린다.
                    break;

                case R.id.btn_map_my_location:
                    userTouchMap = false;   // 사용자 맵 터치 플래그 해제
                    if(screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
                    {
                        // 예상 경로 화면
                        if(pathStartPtMarker != null && pathEndPtMarker != null)
                        {
                            LatLng start = pathStartPtMarker.getPosition();
                            LatLng end = pathEndPtMarker.getPosition();
                            CameraUpdate cameraUpdate = getCameraUpdate(start, end);
                            mInaviMap.moveCamera(cameraUpdate);
                        }
                    }
                    else if(screenStatus == SCREEN_STATUS_RUNNING_CALL &&
                        mainModel.getCallStatus() == MainModel.CALL_STATUS_ALLOC)
                    {
                        // 배차 완료 후 기사님이 오고 있는 경우
                        if(driverMarker != null && startPtMarker != null)
                        {
                            LatLng start = startPtMarker.getPosition();
                            LatLng driverPosition = driverMarker.getPosition();
                            CameraUpdate cameraUpdate = getCameraUpdate(start, driverPosition);
                            mInaviMap.moveCamera(cameraUpdate);
                        }
                    }
                    else if(screenStatus == SCREEN_STATUS_RUNNING_CALL &&
                        mainModel.getCallStatus() == MainModel.CALL_STATUS_BOARD_ON &&
                        mainModel.getCallDestinationPoint() != null)
                    {
                        // 승차 후 이동 중(목적지 있는 경우)
                        if(myPositionMarker != null)
                        {
                            LatLng currentPosition = myPositionMarker.getPosition();
                            LatLng destination = mainModel.getCallDestinationPoint();
                            CameraUpdate cameraUpdate = getCameraUpdate(currentPosition, destination);
                            mInaviMap.moveCamera(cameraUpdate);
                        }
                    }
                    else
                    {
                        initLocation(true); // 현재 위치로 이동
                    }
                    break;

                case R.id.btn_my_home:
                case R.id.btn_search_home:
                    // 홈 버튼
                    hideSoftKeyboard();

                    RequestLocation home = mainModel.getHomeInfo();
                    if(home == null)
                    {
                        // 등록 설정 팝업
                        if(currentDialog != null)
                        {
                            currentDialog.dismiss();
                        }

                        dialogBuilder.setTitle(context.getString(R.string.menu_text_no_home_favorite))
                                .setNegativeButton(context.getString(R.string.app_text_cancel), () -> currentDialog = null)
                                .setPositiveButton(context.getString(R.string.menu_text_regist_right_now), () ->
                                {
                                    try
                                    {
                                        Intent intent1 = new Intent(context, FavoriteLocationActivity.class);
                                        intent1.putExtra("type", AppConstant.HOME);
                                        activity.startActivityForResult(intent1, AppConstant.REQ_CODE_FAVORITE_REGIST);
                                    }
                                    catch(Exception e)
                                    {

                                    }
                                });

                        currentDialog = dialogBuilder.build();
                        currentDialog.show();
                    }
                    else
                    {
                        // 검색 화면 감춤
                        selectLocationBtn.setVisibility(View.GONE);
                        searchInputLayout.setVisibility(View.GONE);
                        searchLayout.setVisibility(View.GONE);

                        // 목적지 설정하고 출발지 확인 화면으로 이동
                        mainModel.setFavoriteLocationClickFlag(true);
                        mainModel.setRequestDestinationLocation(home);
                        onClickCallDirectlyMarker();
                    }
                    break;

                case R.id.btn_my_office:
                case R.id.btn_search_office:
                    // 회사 버튼
                    hideSoftKeyboard();

                    RequestLocation office = mainModel.getOfficeInfo();
                    if(office == null)
                    {
                        if(currentDialog != null)
                        {
                            currentDialog.dismiss();
                        }

                        dialogBuilder.setTitle(context.getString(R.string.menu_text_no_office_favorite))
                                .setNegativeButton(context.getString(R.string.app_text_cancel), () -> currentDialog = null)
                                .setPositiveButton(context.getString(R.string.menu_text_regist_right_now), () ->
                                {
                                    try
                                    {
                                        Intent intent1 = new Intent(context, FavoriteLocationActivity.class);
                                        intent1.putExtra("type", AppConstant.OFFICE);
                                        activity.startActivityForResult(intent1, AppConstant.REQ_CODE_FAVORITE_REGIST);
                                    }
                                    catch(Exception e)
                                    {

                                    }
                                });

                        currentDialog = dialogBuilder.build();
                        currentDialog.show();
                    }
                    else
                    {
                        // 검색 화면 감춤
                        selectLocationBtn.setVisibility(View.GONE);
                        searchInputLayout.setVisibility(View.GONE);
                        searchLayout.setVisibility(View.GONE);

                        // 목적지 설정하고 출발지 확인 화면으로 이동
                        mainModel.setFavoriteLocationClickFlag(true);
                        mainModel.setRequestDestinationLocation(office);
                        onClickCallDirectlyMarker();
                    }
                    break;

                case R.id.btn_search:
                    // 검색
                    showSearchLayout(true);
                    break;

                case R.id.btn_search_departure:
                    // 입력창 검색 버튼(출발지)
                    searchDeparture();
                    break;

                case R.id.btn_search_destination:
                    // 입력창 검색 버튼(도착지)
                    searchDestination();
                    break;

                case R.id.btn_cancel_call:
                    // 호출 취소(배차 전)
                    onClickCancelCallBtn(1);
                    break;

                case R.id.btn_cancel_call2:
                    // 호출 취소(배차 후)
                    onClickCancelCallBtn(2);
                    break;

                case R.id.btn_phone_call_driver:
                    // 기사님께 전화
                    uri = Uri.parse("tel:" + mainModel.getCallDriverPhoneNumber());
                    intent = new Intent(Intent.ACTION_DIAL, uri);
                    activity.startActivity(intent);
                    break;

                case R.id.btn_message:
                    // 안심메시지 보내기
                    message = makeMessage();
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.putExtra("sms_body", message);
                    intent.setType("vnd.android-dir/mms-sms");
                    activity.startActivity(intent);
                    break;

                case R.id.btn_close:
                    // 운행 종료: 닫기 버튼

                    // 빈도수 체크
                    refreshFrequencyVisitedLocations(resultObj ->
                    {
                        try
                        {
                            // 종료
                            clearCallInfo();
                            initScreenDefault(true);
                            setActionBarStatus();
                        }
                        catch(Exception e)
                        {

                        }
                    });
                    break;

                case R.id.search_action_btn_exit:
                    // 검색창 닫기 버튼
                    if(searchProgressView.getVisibility() == View.VISIBLE)
                    {
                        // 검색 중. 검색 취소
                        if(searchTaskCall != null)
                        {
                            searchTaskCall.cancel();
                            searchTaskCall = null;

                            hideSearchProgress();
                            //setSearchResultList(false);
                            //setSearchBtnLayout(true);
                            //setNoSearchResultLayout(true, null);
                        }
                    }
                    else
                    {
                        hideSearchLayout();
                    }
                    break;

                case R.id.btn_retry_using_suggestion:
                    // 추천 검색어 이용해서 재검색
                    String keyword = searchKeywordSuggestionText.getText().toString().trim();
                    directionFlag = adapter.getDirectionFlag();
                    if(directionFlag == 1)
                    {
                        searchDepartureEdit.setText(keyword);
                    }
                    else if(directionFlag == 2)
                    {
                        searchDestinationEdit.setText(keyword);
                    }

                    searchByKeyword(keyword, directionFlag);
                    break;

                case R.id.btn_select_location:
                    // 출발지/도착지 설정
                    onSelectLocation();
                    break;

                case R.id.btn_tip_close:
                case R.id.btn_tip_directly_call_ok:
                    mainModel.setDirectlyCallTip();
                    directlyCallTipLayout.setVisibility(View.GONE);

                    if(v.getId() == R.id.btn_tip_directly_call_ok)
                    {
                        // 바로 호출 시작
                        onClickCallOkBtn();
                    }
                    break;

                case R.id.btn_rate:
                    if(currentDialog != null)
                    {
                        currentDialog.dismiss();
                    }

                    String title = context.getString(R.string.main_text_review_popup_title);
                    dialogBuilder.setTitle(title)
                            .showReviewStars(true)
                            .setNegativeButton(context.getString(R.string.app_text_cancel), () -> currentDialog = null)
                            .setPositiveButton(context.getString(R.string.app_text_send), () ->
                            {
                                try
                                {
                                    int rate = currentDialog.getReviewRate();
                                    currentDialog = null;

                                    // 평점 보내기
                                    RetrofitConnector.sendReview(
                                            mainModel.getMobileNum(), mainModel.getCallId(), mainModel.getCallDt(),
                                            rate, resultObj ->
                                    {
                                        try
                                        {
                                            //AuthResult result = (AuthResult) resultObj;

                                            // 빈도수 갱신 후 종료(성공 여부와 상관없이)
                                            refreshFrequencyVisitedLocations(resultObj1 ->
                                            {
                                                try
                                                {
                                                    // 종료
                                                    clearCallInfo();
                                                    initScreenDefault(true);
                                                    setActionBarStatus();
                                                }
                                                catch(Exception e)
                                                {

                                                }
                                            });
                                        }
                                        catch(Exception e)
                                        {

                                        }
                                    });
                                }
                                catch(Exception e)
                                {

                                }
                            });

                    currentDialog = dialogBuilder.build();
                    currentDialog.show();
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private String makeMessage()
    {
        // - 목적지 있음 : 차량정보, 현재위치, 목적지, 도착시간
        // - 목적지 없음 : 차량정보, 현재위치
        /*
         *  '성남시 삼평동'에서 '강남역 1 번출구'로 가는 택시를 탔어요.
         *  차량번호는 '서울 12 가 1234'이며 약 12 시 12 분에 도착 예정입니다.
        * */

        String message = null;

        // 차량 정보와 현재 위치
        String carNumber = mainModel.getCallDriverCarNumber();
        String currentPositionName = mainModel.getMyCurrentPositionName();

        // 도착 예정 시간
        int hour = 0;
        int min = 0;
        int spendTime = mainModel.getCallSpendTime();
        if(spendTime > 0)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, spendTime);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            min = calendar.get(Calendar.MINUTE);
        }

        // 출발지 이름(축약 주소)
        String departureAddress = mainModel.getCallDepartureAddress();
        String[] split = departureAddress.split(" ");
        String departureName = "";
        int len = split.length;
        if(len < 3)
        {
            departureName = departureAddress;
        }
        else
        {
            departureName = String.format("%s %s", split[len-3], split[len-2]);
        }

        LatLng destination = mainModel.getCallDestinationPoint();
        if(destination != null)
        {
            // 도착지 있음
            String destinationName = mainModel.getCallDestinationName();
            message = String.format(Locale.getDefault(),
                    "%s에서 %s로 가는 택시를 탔어요. 차량번호는 %s이며, 현재 %s를 지나고 있습니다. 약 %02d시 %02d분에 도착 예정입니다.",
                    departureName, destinationName, carNumber, currentPositionName, hour, min);
        }
        else
        {
            // 도착지 없음
            message = String.format(Locale.getDefault(),
                    "%s에서 택시를 탔어요. 차량번호는 %s이며, 현재 %s를 지나고 있습니다",
                    departureName, carNumber, currentPositionName);
        }

        return message;
    }

    private void onSelectLocation()
    {
        try
        {
            String message = null;
            int directionFlag = 0;

            if(mainModel.canCall())
            {
                if(mainModel.sameTwoPoints())
                {
                    // 출/도착지가 동일함. 경고창을 보여준다.
                    if(currentDialog != null)
                    {
                        currentDialog.dismiss();
                    }

                    message = context.getString(R.string.main_text_select_dep_dest_equal);
                    dialogBuilder.setTitle(message)
                            .setPositiveButton(context.getString(R.string.app_text_ok),
                                    () -> currentDialog = null);
                    currentDialog = dialogBuilder.build();
                    currentDialog.show();
                }
                else
                {
                    // 호출 요청을 위한 경로검색 실시
                    setExpectedPathLayout();
                }
            }
            else
            {
                // 설정하고 다시 검색 화면으로 전환
                directionFlag = adapter.getDirectionFlag();

                if(directionFlag == 1)
                {
                    // 출발지로 설정
                    RequestLocation departure = mainModel.getRequestLocation();
                    searchDepartureEdit.setHint(departure.positionName);
                    searchDepartureEdit.setText("");
                    searchDestinationEdit.requestFocus();
                }
                else if(directionFlag == 2)
                {
                    // 도착지로 설정(이런 경우는 없을 것 같다)
                    RequestLocation destination = mainModel.getRequestDestinationLocation();
                    searchDestinationEdit.setHint(destination.positionName);
                    searchDestinationEdit.setText("");
                    searchDepartureEdit.requestFocus();
                }

                showSearchLayout(true);
                screenStatus = SCREEN_STATUS_DEFAULT;

                if(startPtMarker != null)
                {
                    startPtMarker.setMap(null);
                }

                if(endPtMarker != null)
                {
                    endPtMarker.setMap(null);
                }

                setActionBarStatus();
                selectLocationBtn.setVisibility(View.GONE);
                setEditTextAlready = false;
                adapter.setDirectionFlag(0);
            }
        }
        catch(Exception e)
        {

        }
    }

    private void setExpectedPathLayout()
    {
        removeUserLocationIcon();

        showProgress();

        RequestLocation departure = mainModel.getRequestLocation();
        RequestLocation destination = mainModel.getRequestDestinationLocation();

        LatLng latLng1 = new LatLng(departure.lat, departure.lng);
        LatLng latLng2 = new LatLng(destination.lat, destination.lng);

        RetrofitConnector.getRouteSearchNormalResult(latLng1, latLng2, resultObj ->
        {
            try
            {
                screenStatus = SCREEN_STATUS_SEARCH_EXPECTED_ROUTE;
                setActionBarStatus();

                searchLayout.setVisibility(View.GONE);
                searchInputLayout.setVisibility(View.GONE);

                if(startPtMarker != null)
                {
                    startPtMarker.setMap(null);
                }

                if(endPtMarker != null)
                {
                    endPtMarker.setMap(null);
                }

                selectLocationBtn.setVisibility(View.GONE);

                InaviApiRouteNormalResult result = (InaviApiRouteNormalResult) resultObj;
                RequestLocation dep = mainModel.getRequestLocation();
                RequestLocation dest = mainModel.getRequestDestinationLocation();

                // 출/도착지 마커 표시, 경로 표시
                int spendTime = result.route.data.spendTime;
                makeExpectedPathMarker(dep, 1, 0);
                makeExpectedPathMarker(dest, 2, (spendTime/60)/*min*/);

                float distance = result.route.data.distance;    // meter
                String distanceStr = String.format(Locale.getDefault(), "%,.1f", (distance/1000f));

                // 경로 표시
                if(expectedRoutePath != null)
                {
                    expectedRoutePath.setMap(null);
                }

                List<InaviApiPath> paths = result.route.data.paths;
                int lineColor = Color.parseColor("#0ab9b4");
                if(paths != null)
                {
                    List<InvRoute.InvRouteLink> routeLinkList = makeRouteLink(
                            new LatLng(dep.lat, dep.lng), new LatLng(dest.lat, dest.lng), paths, lineColor);

                    expectedRoutePath = new InvRoute();
                    expectedRoutePath.setLinks(routeLinkList);
                    expectedRoutePath.setLineWidth((int) CommonUtil.convertDpToPixel(4f));
                    expectedRoutePath.setStrokeWidth(0);
                    expectedRoutePath.setMap(mInaviMap);
                }

                // 예상 거리, 예상 소요시간
                expectedTimeText.setText(String.valueOf(spendTime/60)/*min*/);
                expectedDistanceText.setText(distanceStr/*km*/);

                showCallConfirmView(true);

                // 출발지, 도착지가 한 화면에 들어오게 카메라 포인트 설정
                CameraUpdate cameraUpdate = getCameraUpdate(latLng1, latLng2);
                if(cameraUpdate != null)
                {
                    mInaviMap.moveCamera(cameraUpdate);
                }

                hideProgress();
            }
            catch(Exception e)
            {
                hideProgress();
            }
        });
    }

    private List<InvRoute.InvRouteLink> makeRouteLink(
            LatLng dep, LatLng dest, List<InaviApiPath> paths, int lineColor)
    {
        List<InvRoute.InvRouteLink> routeLinkList = new ArrayList<>();

        int size1 = paths.size();
        for(int i = 0; i < size1; i++)
        {
            InaviApiPath path = paths.get(i);
            if(path.coords != null && path.coords.size() > 0)
            {
                List<LatLng> latLngList = new ArrayList<>();

                int size2 = path.coords.size();
                for(int j = 0; j < size2; j++)
                {
                    if(i == 0 && j == 0)
                    {
                        // 출발점 추가
                        latLngList.add(dep);
                    }

                    InaviApiCoord coord = path.coords.get(j);
                    latLngList.add(new LatLng(coord.y, coord.x));

                    if(i == size1-1 && j == size2-1)
                    {
                        // 도착점 추가
                        latLngList.add(dest);
                    }
                }

                InvRoute.InvRouteLink link = new InvRoute.InvRouteLink(latLngList);
                link.setLineColor(lineColor);
                routeLinkList.add(link);
            }
        }

        return routeLinkList;
    }

    private InvMarker makeCustomMarker(int resId, int viewId, int layoutId)
    {
        View markerView = inflater.inflate(layoutId, null);

        if(viewId != Integer.MAX_VALUE)
        {
            View view = markerView.findViewById(viewId);
            view.setBackgroundResource(resId);
        }

        InvMarker marker = new InvMarker();

        Bitmap bitmap = CommonUtil.createMarkerBitmapUsingView(markerView, Bitmap.Config.ARGB_8888);
        InvImage image = new InvImage(bitmap);

        marker.setIconImage(image);
        marker.setMap(null);

        return marker;
    }

    private void makeExpectedPathMarker(RequestLocation location, int directionFlag, int spendTime)
    {
        View markerView = null;
        InvMarker marker = null;

        if(directionFlag == 1)
        {
            if(pathStartPtMarker != null)
            {
                pathStartPtMarker.setMap(null);
            }

            pathStartPtMarker = new InvMarker();
            marker = pathStartPtMarker;

            markerView = inflater.inflate(R.layout.layout_expected_path_start, null);
            TextView positionNameText = markerView.findViewById(R.id.position_name_text);
            positionNameText.setTypeface(typeface);

            positionNameText.setText(location.positionName);

            PointF anchor = new PointF(0.5f, 0.5937f);
            pathStartPtMarker.setAnchor(anchor);
        }
        else
        {
            if(pathEndPtMarker != null)
            {
                pathEndPtMarker.setMap(null);
            }

            pathEndPtMarker = new InvMarker();
            marker = pathEndPtMarker;

            markerView = inflater.inflate(R.layout.layout_expected_path_end, null);
            TextView positionNameText = markerView.findViewById(R.id.position_name_text);
            positionNameText.setTypeface(typeface);

//            spendTimeText.setText(String.valueOf(spendTime));
            positionNameText.setText(location.positionName);

            PointF anchor = new PointF(0.5f, 0.5937f);
            pathEndPtMarker.setAnchor(anchor);
        }

        Bitmap bitmap = CommonUtil.createMarkerBitmapUsingView(markerView, Bitmap.Config.ARGB_8888);
        InvImage image = new InvImage(bitmap);

        marker.setIconImage(image);
        marker.setPosition(new LatLng(location.lat, location.lng));
        marker.setMap(mInaviMap);
    }

    private void searchByKeyword(String keyword, int editTextType)
    {
        if(keyword == null || keyword.trim().isEmpty())
        {
            return;
        }

        showSearchProgress();

        adapter.setDirectionFlag(editTextType);

        // 아이나비 POI 검색
        final String keywordModified = keyword.replaceAll(" ", "+");
        //final String keywordModified = URLEncoder.encode(keyword, "UTF-8");
        searchTaskCall = RetrofitConnector.search(keywordModified.trim(), resultObj ->
        {
            try
            {
                if(resultObj != null)
                {
                    List<RequestLocation> locationList = (List<RequestLocation>) resultObj;
                    adapter.setKeyword(keyword);
                    adapter.setLocationList(locationList);
                    adapter.notifyDataSetChanged();

                    searchResultListView.setSelection(0);   // 목록 맨 위로 이동

                    setSearchBtnLayout(false);
                    setSearchResultList(true);
                    setNoSearchResultLayout(false, null);
                }
                else
                {
                    // 결과 없음
                    // 추천 검색어를 찾아본다.
                    RetrofitConnector.getSuggestedKeywords(keyword.trim(), resultObj1 ->
                    {
                        try
                        {
                            String suggestion = (String) resultObj1;
                            setNoSearchResultLayout(true, suggestion);
                        }
                        catch(Exception e)
                        {
                            setNoSearchResultLayout(true, null);
                        }
                    });

                    setSearchBtnLayout(false);
                    setSearchResultList(false);
                    setNoSearchResultLayout(false, null);
                }

                hideSearchProgress();
                searchTaskCall = null;
            }
            catch(Exception e)
            {
                hideSearchProgress();
                searchTaskCall = null;
            }
        });
    }

    private void setSearchResultList(boolean show)
    {
        searchResultListView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setNoSearchResultLayout(boolean show, String suggestedKeyword)
    {
        boolean validKeyword = (suggestedKeyword != null && !suggestedKeyword.trim().isEmpty());
        searchKeywordSuggestionText.setText(suggestedKeyword + " ");
        searchKeywordSuggestionLayout.setVisibility(validKeyword ? View.VISIBLE : View.GONE);
        retryUsingSuggestionBtn.setVisibility(validKeyword ? View.VISIBLE : View.GONE);
        noSearchResultLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setSearchBtnLayout(boolean show)
    {
        searchBtnLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initSearchResultLayout()
    {
        setSearchBtnLayout(true);
        setSearchResultList(false);
        setNoSearchResultLayout(true, null);

        adapter.setLocationList(null);
        adapter.setDirectionFlag(0);
        adapter.setKeyword(null);
        adapter.notifyDataSetInvalidated();
    }

    private void onClickCancelCallBtn(int cancelType/*1:배차 전, 2:배차 후*/)
    {
        if(currentDialog != null)
        {
            currentDialog.dismiss();
        }

        // 타이머 작업 모두 종료(일단)
        stopTimerTaskAll();

        // 경고창 보여주기

        String title = null;
        String contentText = null;

        if(cancelType == 1)
        {
            // 배차 완료 전 호출 취소
            title = context.getString(R.string.main_text_call_cancel_title1);
        }
        else
        {
            // 배차 후 호출 취소
            title = context.getString(R.string.main_text_call_cancel_title2);
            contentText = context.getString(R.string.main_text_call_cancel_content);
        }

        dialogBuilder.setCancelable(false).setTitle(title).setContent(contentText)
                .setNegativeButton(context.getString(R.string.app_text_no), () ->
                {
                    try
                    {
                        currentDialog = null;
                        onResume();     // 콜 체크 재개
                    }
                    catch(Exception e)
                    {

                    }
                })
                .setPositiveButton(context.getString(R.string.app_text_yes), () ->
                {
                    try
                    {
                        // 호출 취소 확정
                        currentDialog = null;
                        confirmCancelCall();
                    }
                    catch(Exception e)
                    {

                    }
                });

        currentDialog = dialogBuilder.build();
        currentDialog.show();
    }

    private void confirmCancelCall()
    {
        // 서버에 호출 취소 요청
        RetrofitConnector.cancelCall(
                mainModel.getMobileNum(), mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
                {
                    try
                    {
                        RequestResult result = (RequestResult) resultObj;
                        if(result.isSuccessful())
                        {
                            if(currentDialog != null)
                            {
                                currentDialog.dismiss();
                            }

                            // 호출 취소 확인 팝업
                            String message = context.getString(R.string.main_text_call_cancel_complete);
                            dialogBuilder.setAutoCancelable(true).setTitle(message);
                            currentDialog = dialogBuilder.build();
                            currentDialog.show();
                            currentDialog = null;

                            clearCallInfo();
                            initScreenDefault(true);
                            setActionBarStatus();
                        }
                    }
                    catch(Exception e)
                    {

                    }
                });
    }

    private void onClickCallDirectlyMarker()
    {
        if(screenStatus == SCREEN_STATUS_DEFAULT ||
            screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
        {
            int oldStatus = screenStatus;

            screenStatus = SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT;

            callDirectlyMarker.setVisibility(View.VISIBLE);

            RequestLocation requestLocation = mainModel.getRequestLocation();
            if(requestLocation != null)
            {
                callDirectlyPositionNameText.setText(requestLocation.positionName);
                callDirectlyOkBtn.setEnabled(true);

                CameraPosition cameraPosition = mInaviMap.getCameraPosition();
                if(cameraPosition.zoom != 16)
                {
                    CameraUpdate cameraUpdate = CameraUpdate.targetTo(new LatLng(requestLocation.lat, requestLocation.lng), 16);
                    cameraUpdate.setAnimationType(CameraAnimationType.Linear);
                    mInaviMap.moveCamera(cameraUpdate);
                }
            }
            else
            {
                callDirectlyPositionNameText.setText(context.getString(R.string.main_text_invalid_location));
                //callDirectlyPositionNameText.setText(AppConstant.noValueText);
                callDirectlyOkBtn.setEnabled(false);
            }

            myLocationNameText.setVisibility(View.GONE);

            // 현재 위치 표시
            if(userLocationIcon != null && !userLocationIcon.isVisible())
            {
                showUserLocationIcon(mainModel.getLocationInstantly());
            }

            // 액션바 설정
            setActionBarStatus();

            // 하단의 디폴트 블록을 감춘다.
            if(oldStatus != SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
            {
                hideMainBottomController(true);

                // 하단 디폴트 블록 애니메이션이 종료되면 그 후에 바로 호출 확인 블록을 보여준다.
                new Handler().postDelayed(() ->
                {
                    try
                    {
                        activity.runOnUiThread(() ->
                        {
                            try
                            {
                                showMainDirectCallCheckView(true);
                            }
                            catch(Exception e)
                            {

                            }
                        });
                    }
                    catch(Exception e)
                    {

                    }
                }, 200);
            }
            else
            {
                showMainDirectCallCheckView(true);
            }
        }
    }

    private void onClickCallDirectlyCancelBtn()
    {
        if(screenStatus == SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT)
        {
            screenStatus = SCREEN_STATUS_DEFAULT;

            mainModel.setFavoriteLocationClickFlag(false);
            mainModel.setRequestDestinationLocation(null);

            hideMainDirectCallCheckView(true);

            // 액션바 설정
            setActionBarStatus();

            new Handler().postDelayed(() ->
            {
                try
                {
                    activity.runOnUiThread(() ->
                    {
                        try
                        {
                            showMainBottomController(true);

                            // 디폴트 화면 현재 주소 가져오기
                            getCurrentPositionAddress();

                            boolean visible =
                                    !mainModel.isFavoriteLocationClickFlag() &&
                                    !mainModel.isRequestUsingHistory();

                            myLocationNameText.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }
                        catch(Exception e)
                        {

                        }
                    });
                }
                catch(Exception e)
                {

                }
            }, 200);
        }
    }

    public void requestUsingHistory(RequestLocation departure, RequestLocation destination)
    {
        if(destination != null)
        {
            // 목적지 있음

            // 하단 블록 감춤
            hideMainBottomController(false);

            // 바로 호출 마커 감춤
            callDirectlyMarker.setVisibility(View.GONE);
            myLocationNameText.setVisibility(View.GONE);

            // 백버튼 처리를 위해 플래그 설정
            mainModel.setRequestUsingHistory(true);

            mainModel.setRequestLocation(departure);
            mainModel.setRequestDestinationLocation(destination);

            // 예상 경로 보여주기
            onSelectLocation();
        }
        else
        {
            // 목적지 없음

            mainModel.setRequestLocation(departure);
            mainModel.setRequestDestinationLocation(null);

            // 지도를 출발지로 이동
            LatLng latLng = new LatLng(departure.lat, departure.lng);
            CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);
            cameraUpdate.setAnimationType(CameraAnimationType.Linear);
            mInaviMap.moveCamera(cameraUpdate);

            // 현재 위치 표시
            LatLng userLocation = mainModel.getLocationInstantly();
            showUserLocationIcon(userLocation);

            // 바로 호출 모드 시작
            onClickCallDirectlyMarker();
        }
    }

    private void onClickCallOkBtn()
    {
        requestCall(mainModel.getMobileNum(),
                mainModel.getRequestLocation(), mainModel.getRequestDestinationLocation());
    }

    public void requestCall(String mobileNum, RequestLocation departure, RequestLocation destination)
    {
        // 다른 동작을 막기 위해 잠시 프로그레스 레이아웃을 보여준다.
        showProgress();
        screenStatus = SCREEN_STATUS_RUNNING_CALL;
        inRequest = true;

        RetrofitConnector.requestCall(
                mobileNum, departure, destination,
                resultObj ->
                {
                    try
                    {
                        RequestResult result = (RequestResult) resultObj;
                        if(result == null)
                        {
                            hideProgress();
                            screenStatus = SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT;
                            inRequest = false;
                            return;
                        }

                        if(result.isSuccessful())
                        {
                            showRequestCover();
                            mainModel.setCallId(result.getCallID());
                            mainModel.setCallDt(result.getCallDT());
                            mainModel.setCallStatus(MainModel.CALL_STATUS_ALLOC_WAIT);

                            // 출발지 위치 저장
                            mainModel.setCallDeparturePoint(departure.lat, departure.lng);
                            mainModel.setCallDepartureName(departure.positionName);
                            mainModel.setCallDepartureAddress(departure.address);

                            // 도착지 위치 저장
                            if(destination != null)
                            {
                                mainModel.setCallDestinationName(destination.positionName);
                                mainModel.setCallDestinationPoint(destination.lat, destination.lng);
                                mainModel.setCallDestinationAddress(destination.address);
                            }

                            // 콜 체크 시작
                            setCallCheckTask();
                        }
                        else
                        {
                            // 기존 화면 상태(출발지 확인)으로 전환
                            screenStatus = SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT;
                        }

                        inRequest = false;
                        hideProgress();
                    }
                    catch(Exception e)
                    {
                        hideProgress();
                        screenStatus = SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT;
                        inRequest = false;
                    }
                });
    }

    private void showCallCenterDialog()
    {
        SharedPreferences pref = context.getSharedPreferences(AppConstant.COMMON_DATA, Context.MODE_PRIVATE);
        String[] callCenterList = MainModel.callCenterPhoneNums;
        TreeSet<String> callCenterSet = CommonUtil.getSelectedCallCenter(pref);

        if(callCenterSet.size() <= 0)
        {
            // 이용 중인 콜센터 없음. 콜센터 설정 화면으로 이동
            Intent intent = new Intent(context, CallCenterActivity.class);
            intent.putExtra("fromMain", true);
            activity.startActivity(intent);
        }
        else
        {
            String callCenterText = context.getString(R.string.app_text_call_center);
            int size = callCenterSet.size();
            String[] callCenterNameList = new String[size];
            selectedList = new String[size];

            int idx = 0;
            int len = callCenterList.length;
            for(int i = 0; i < len; i++)
            {
                String callCenterNum = callCenterList[i];
                if(callCenterSet.contains(callCenterNum))
                {
                    callCenterNameList[idx] = callCenterText + " " + (i+1);
                    selectedList[idx] = callCenterNum;
                    idx++;
                }
            }

            if(size == 1)
            {
                // 콜센터로 사용자 위치 정보 전송 후 전화번호 앱 열기
                String phoneNum = selectedList[0];
                sendPassengerLocationToCallCenter(phoneNum);
            }
            else
            {
                // 선택 대화상자 보여주기
                alertDialogBuilder.setItems(callCenterNameList, (dialog, which) ->
                {
                    try
                    {
                        // 콜센터로 사용자 위치 정보 전송 후 전화번호 앱 열기
                        String phoneNum = selectedList[which];
                        sendPassengerLocationToCallCenter(phoneNum);
                    }
                    catch(Exception e)
                    {

                    }
                });

                alertDialogBuilder.setNegativeButton(context.getString(R.string.app_text_cancel), null);

                callCenterDialog = alertDialogBuilder.create();
                callCenterDialog.show();
            }
        }
    }

    private void sendPassengerLocationToCallCenter(String phoneNum)
    {
        LatLng latLng = mainModel.getLocationInstantly();
        double lat = latLng.latitude;
        double lng = latLng.longitude;

        // 리버스 지오코딩
        RetrofitConnector.getReverseGeocode(lat, lng, resultObj ->
        {
            try
            {
                if(resultObj != null)
                {
                    InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                    String text = CommonUtil.getReverseGeocodeString(result, false);

                    // 콜센터에 사용자 위치 정보 보내기
                    RetrofitConnector.sendPassengerLocation(mainModel.getMobileNum(), latLng.latitude, latLng.longitude, text, resultObj1 ->
                    {
                        try
                        {
                            AuthResult result1 = (AuthResult) resultObj1;
                            if(result1 != null && result1.isSuccessful())
                            {
                                // 기존에 시행되고 있는 task가 있는지 확인
                                long callCenterDtime = mainModel.getCallCenterDtime();
                                if(callCenterDtime > 0)
                                {
                                    //Log.d(AppConstant.LOG_TEMP_TAG, "    stop service!!!");

                                    // 있다면 기존 task를 종료
                                    if(timerTask != null)
                                    {
                                        timerTask.cancel(true);
                                        timerTask = null;
                                    }

                                    // 관련 서비스도 중지
                                    Intent intent = new Intent(context, CallCheckService.class);
                                    context.stopService(intent);

                                    // 전화 일시 삭제
                                    mainModel.removeCallCenterDtime();
                                }

                                // 콜센터 전화 시도 일시 기록
                                mainModel.setCallCenterDtime(System.currentTimeMillis());

                                // 전화번호 앱 열기
                                Uri uri = Uri.parse("tel:" + phoneNum);
                                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                                activity.startActivity(intent);
                            }
                        }
                        catch(Exception e)
                        {

                        }
                    });
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void showSearchLayout(boolean init)
    {
        if(init)
        {
            searchDepartureEdit.setText("");
            searchDestinationEdit.setText("");
            searchDestinationEdit.setHint(context.getString(R.string.main_text_search_destination));
            searchDestinationEdit.requestFocus();

            initSearchResultLayout();
        }

        searchInputLayout.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
    }

    private void hideSearchLayout()
    {
        mainModel.setFavoriteLocationClickFlag(false);
        mainModel.setRequestUsingHistory(false);

        hideSoftKeyboard();

        if(startPtMarker != null)
        {
            startPtMarker.setMap(null);
        }

        if(endPtMarker != null)
        {
            endPtMarker.setMap(null);
        }

        if(screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
        {
            // 경로검색 관련 요소 모두 삭제

            if(pathStartPtMarker != null)
            {
                pathStartPtMarker.setMap(null);
            }

            if(pathEndPtMarker != null)
            {
                pathEndPtMarker.setMap(null);
            }

            // 경로 poly line 삭제
            if(expectedRoutePath != null)
            {
                expectedRoutePath.setMap(null);
                expectedRoutePath = null;
            }

            selectLocationBtn.setVisibility(View.GONE);
            searchInputLayout.setVisibility(View.GONE);
            searchLayout.setVisibility(View.GONE);

            // 호출 확인 버튼 레이아웃 감춤
            hideCallConfirmView(true);

            // 200ms 지연 후 하단 레이아웃 애니메이션 시작
            new Handler().postDelayed(() ->
            {
                try
                {
                    initScreenDefault(true);
                }
                catch(Exception e)
                {

                }
            }, 200);
        }
        else
        {
            initScreenDefault(false);
            selectLocationBtn.setVisibility(View.GONE);
            searchInputLayout.setVisibility(View.GONE);
            searchLayout.setVisibility(View.GONE);
        }

        screenStatus = SCREEN_STATUS_DEFAULT;
        setActionBarStatus();
        setEditTextAlready = false;
    }

    private void clearCallInfo()
    {
        userTouchMap = false;

        mainModel.removeCallInfo();
        mainModel.setMyCurrentPositionName(null);
        mainModel.setRequestDestinationLocation(null);
        mainModel.setCallStatus(0);
        setSpendTime = false;

        // 모든 상태바 알림 종료
        NotificationUtil.cancelNotificationAll(context);

        // 내 위치 버튼 노출
        myLocationBtn.setVisibility(View.VISIBLE);
    }

    // 주기적으로 서버에 콜 체크를 한다.
    private void setCallCheckTask()
    {
        if(screenStatus == SCREEN_STATUS_RUNNING_CALL)
        {
            if(timerTask != null)
            {
                timerTask.cancel(true);
                timerTask = null;
            }

            int callStatus = mainModel.getCallStatus();
            int period = 0;

            if(callStatus == MainModel.CALL_STATUS_ALLOC_WAIT)
            {
                period = 2000;  // 2sec
            }
            else
            {
                period = 10000; // 10sec
            }

            timerTask = scheduler.scheduleAtFixedRate(callCheckRunnable, 0, period, TimeUnit.MILLISECONDS);
        }
    }

    private void setCallCenterCheckTask()
    {
        if(screenStatus != SCREEN_STATUS_RUNNING_CALL)
        {
            if(timerTask != null)
            {
                timerTask.cancel(true);
                timerTask = null;
            }

            timerTask = scheduler.scheduleAtFixedRate(callCenterCheckRunnable, 0, 2000, TimeUnit.MILLISECONDS);
        }
    }

    private Runnable callCenterCheckRunnable = () ->
    {
        try
        {
            callCenterCheck();
        }
        catch(Exception e)
        {

        }
    };

    private void callCenterCheck()
    {
        RetrofitConnector.recentAllocation(mainModel.getMobileNum(), resultObj ->
        {
            try
            {
                long now = System.currentTimeMillis();
                long callCenterDtime = mainModel.getCallCenterDtime();

                if(callCenterDtime <= 0)
                {
                    // 중간에 콜센터 체크가 종료됨
                    if(timerTask != null)
                    {
                        timerTask.cancel(true);
                        timerTask = null;
                    }
                    return;
                }

                RequestResult result = (RequestResult) resultObj;
                if(result != null && result.isSuccessful())
                {
                    String callId = result.getCallID();
                    String callStatus = result.getCallStatus();

                    if(callId != null && !callId.trim().isEmpty())
                    {
                        mainModel.removeCallCenterDtime();

                        if(timerTask != null)
                        {
                            timerTask.cancel(true);
                            timerTask = null;
                        }

                        mainModel.setCallId(callId);
                        mainModel.setCallDt(result.getCallDT());
                        mainModel.setCallStatus(callStatus);

                        screenStatus = SCREEN_STATUS_RUNNING_CALL;
                        setScreenLayout(mainModel.getCallStatus(), 0);

                        // 콜체크로 변경
                        setCallCheckTask();
                        return;
                    }
                }

                if(now - callCenterDtime > CallCheckService.ONE_MIN_IN_MILLS)
                {
                    // 1분 초과. 콜센터 체크 종료
                    mainModel.removeCallCenterDtime();

                    if(timerTask != null)
                    {
                        timerTask.cancel(true);
                        timerTask = null;
                    }
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    private void setCarPositionTask()
    {
        if(screenStatus == SCREEN_STATUS_RUNNING_CALL)
        {
            if(carPositionTimerTask != null)
            {
                carPositionTimerTask.cancel(true);
                carPositionTimerTask = null;
            }

            int period = 10000;
            int callStatus = mainModel.getCallStatus();
            if(callStatus == MainModel.CALL_STATUS_ALLOC)
            {
                // 기사님 이동 중
                // 서버에서 받아온 정보 이용
                carPositionTimerTask = scheduler.scheduleAtFixedRate(
                        carPositionRunnable, 0, period, TimeUnit.MILLISECONDS);
            }
            else if(callStatus == MainModel.CALL_STATUS_BOARD_ON)
            {
                // 승차 후 이동 중
                carPositionTimerTask = scheduler.scheduleAtFixedRate(
                        myPositionRunnable, 0, period, TimeUnit.MILLISECONDS);
            }
        }
    }

    private Runnable callCheckRunnable = () ->
    {
        try
        {
            callCheck();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private void callCheck()
    {
        RetrofitConnector.callCheck(mainModel.getMobileNum(), mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
        {
            try
            {
                RequestResult result = (RequestResult) resultObj;
                if(result.isSuccessful())
                {
                    int callStatusOld = mainModel.getCallStatus();

                    try
                    {
                        String status = result.getCallStatus();
                        //Log.d(AppConstant.LOG_TEMP_TAG, "status = " + status);
                        mainModel.setCallStatus(status);
                    }
                    catch(Exception e)
                    {
                        return;
                    }

                    int callStatus = mainModel.getCallStatus();
                    if(callStatus != callStatusOld)
                    {
                        setScreenLayout(callStatus, callStatusOld);

                        if(callStatusOld == MainModel.CALL_STATUS_ALLOC_WAIT &&
                            callStatus == MainModel.CALL_STATUS_ALLOC)
                        {
                            // 주기가 바뀌므로 콜 체크 작업을 재설정한다.
                            setCallCheckTask();
                        }

                        //setCarPositionTask();
                    }

                    if(callStatus == MainModel.CALL_STATUS_BOARD_OFF ||
                            callStatus == MainModel.CALL_STATUS_CANCEL ||
                            callStatus == MainModel.CALL_STATUS_CANCEL_BY_DRIVER)
                    {
                        // 하차(도착) 또는 배차 취소. 콜 체크 중지
                        if(timerTask != null)
                        {
                            timerTask.cancel(true);
                            timerTask = null;
                        }
                    }
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    // 기사님 정보가 누락된 경우 받아오기
    private void getDriverInfoWithoutLayoutSetting()
    {
        String phoneNum = mainModel.getCallDriverPhoneNumber();
        String carNum = mainModel.getCallDriverCarNumber();

        if((phoneNum == null || phoneNum.trim().isEmpty()) ||
                (carNum == null || carNum.trim().isEmpty()))
        {
            RetrofitConnector.getDriverInfo(mainModel.getMobileNum(),
                    mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
                    {
                        try
                        {
                            // 기사님 정보
                            CarInfoResult result = (CarInfoResult) resultObj;
                            String carModel = result.getCarModel();
                            String carNumber = result.getCarNum();

                            String carNumberFull = null;
                            if(carModel != null && !carModel.trim().isEmpty())
                            {
                                carNumberFull = (carModel + " " + carNumber);
                            }
                            else
                            {
                                carNumberFull = carNumber;
                            }

                            String driverPhoneNum = result.getDriverPhoneNum();
                            mainModel.setCallDriverPhoneNumber(driverPhoneNum);
                            mainModel.setCallDriverCarNumber(carNumberFull);
                        }
                        catch(Exception e)
                        {

                        }
                    });
        }
    }

    private void getDriverInfo()
    {
        RetrofitConnector.getDriverInfo(mainModel.getMobileNum(),
                mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
                {
                    try
                    {
                        // 기사님 정보
                        CarInfoResult result = (CarInfoResult) resultObj;
                        //String driverName = result.getDriverName();
                        String carModel = result.getCarModel();
                        String carNumber = result.getCarNum();

                        String carNumberFull = null;
                        if(carModel != null && !carModel.trim().isEmpty())
                        {
                            carNumberFull = (carModel + " " + carNumber);
                        }
                        else
                        {
                            carNumberFull = carNumber;
                        }

                        String carNumberShort = null;
                        int len = 0;
                        if(carNumber != null)
                        {
                            carNumberShort = carNumber.trim().replaceAll(" ", "");
                            len = carNumberShort.length();
                            if(len > 4)
                            {
                                // 차량 번호 마지막 네 자리
                                carNumberShort = carNumberShort.substring(len-4, len);
                            }
                        }

                        String driverPhoneNum = result.getDriverPhoneNum();
                        mainModel.setCallDriverPhoneNumber(driverPhoneNum);
                        mainModel.setCallDriverCarNumber(carNumberFull);

                        // 화면에 표시
                        taxiInfoText1.setVisibility(View.VISIBLE);
                        taxiInfoText1.setText(context.getString(R.string.main_text_allocate_taxi_message));
                        taxiInfoText2.setVisibility(View.GONE);
                        expectedRemainTimeText.setVisibility(View.GONE);
                        carModelText.setText(carModel);
                        carNumberText.setText(carNumber);
                        carInfoLayout.setVisibility(View.VISIBLE);
                        taxiInfoLayout.setVisibility(View.VISIBLE);

                        // 지도 마커 생성
                        if(driverMarker != null)
                        {
                            // 기존 마커 지도에서 제거
                            driverMarker.setMap(null);
                        }

                        driverMarker = new InvMarker();

                        // 뷰를 이용해서 마커 비트맵 생성
                        View driverMarkerView = inflater.inflate(R.layout.layout_driver_marker, null);
                        TextView driverNameText = driverMarkerView.findViewById(R.id.driver_name_text);
                        //driverNameText.setText(String.format(Locale.getDefault(), context.getString(R.string.main_text_driver_name_format), driverName));
                        driverNameText.setText(carNumberShort); // 기사님 이름 대신 차량 번호 맨 뒤 4자리로 교체
                        driverNameText.setTypeface(activity.getBoldTypeface());
                        markerBitmap = CommonUtil.createMarkerBitmapUsingView(driverMarkerView, Bitmap.Config.ARGB_8888);

                        markerImg = new InvImage(markerBitmap);
                        driverMarker.setIconImage(markerImg);

                        // 사용자 현재 위치 표시
                        LatLng latLng = mainModel.getLocationInstantly();
                        showUserLocationIcon(latLng);

                        // 기사님 위치 확인 시작
                        setCarPositionTask();
                    }
                    catch(Exception e)
                    {

                    }
                });
    }

    private void getCarPosition()
    {

        if(mainModel.getCallStatus() == MainModel.CALL_STATUS_ALLOC)
        {
            RetrofitConnector.getCarPosition(mainModel.getMobileNum(),
                    mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
                    {
                        try
                        {
                            if(mainModel.getCallStatus() != MainModel.CALL_STATUS_ALLOC)
                            {
                                if(driverMarker != null)
                                {
                                    driverMarker.setMap(null);
                                    driverMarker = null;
                                }
                                return;
                            }

                            // 기사님 위치
                            CarInfoResult result = (CarInfoResult) resultObj;
                            double lat = Double.parseDouble(result.getCarLat());
                            double lng = Double.parseDouble(result.getCarLon());
                            LatLng driverLatLng = new LatLng(lat, lng);

                            // 출발지
                            LatLng departure = mainModel.getCallDeparturePoint();

                            // 기사님 위치 지도에 표시
                            activity.runOnUiThread(() ->
                            {
                                try
                                {
                                    driverMarker.setPosition(new LatLng(lat, lng));
                                    driverMarker.setMap(mInaviMap);

                                    if(!userTouchMap)
                                    {
                                        // 기사님 위치와 출발 위치가 모두 보이도록 스케일 조정
                                        CameraUpdate cameraUpdate = getCameraUpdate(departure, driverLatLng);
                                        if(cameraUpdate != null)
                                        {
                                            mInaviMap.moveCamera(cameraUpdate);
                                        }
                                    }
                                }
                                catch(Exception e)
                                {

                                }
                            });

                            // 기사님 위치와 출발지 사이의 소요시간 측정(아이나비)
                            RetrofitConnector.getRouteSearchNormalResult(departure, driverLatLng, resultObj1 ->
                            {
                                try
                                {
                                    InaviApiRouteNormalResult result1 = (InaviApiRouteNormalResult) resultObj1;
                                    int spendTime = RetrofitConnector.getRouteSpendTime(result1);

                                    if(spendTime >= 0)
                                    {
                                        int min = spendTime / 60;
                                        String text = null;

                                        if(min <= 0)
                                        {
                                            text = context.getString(R.string.main_text_taxi_driver_arrive_soon);
                                        }
                                        else
                                        {
                                            text = String.format(context.getString(R.string.main_text_taxi_driver_remain_time_format), min);
                                        }

                                        final String timeText = text;
                                        activity.runOnUiThread(() ->
                                        {
                                            try
                                            {
                                                if(!setSpendTime)
                                                {
                                                    // 최초 표시. 3초 후에 시간 표시한다.
                                                    new Handler().postDelayed(() ->
                                                    {
                                                        try
                                                        {
                                                            taxiInfoText1.setText(timeText);
                                                        }
                                                        catch(Exception e)
                                                        {

                                                        }

                                                    }, 3000);

                                                    setSpendTime = true;
                                                }
                                                else
                                                {
                                                    taxiInfoText1.setText(timeText);
                                                }
                                            }
                                            catch(Exception e)
                                            {

                                            }
                                        });
                                    }
                                }
                                catch(Exception e)
                                {

                                }
                            });
                        }
                        catch(Exception e)
                        {

                        }
                    });
        }
    }

    private Runnable carPositionRunnable = () ->
    {
        try
        {
            getCarPosition();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private Runnable myPositionRunnable = () ->
    {
        try
        {
            activity.runOnUiThread(() ->
            {
                try
                {
                    getMyPositionInfo();
                }
                catch(Exception e)
                {
                    //e.printStackTrace();
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private void getMyPositionInfo()
    {
        if(mainModel.getCallStatus() != MainModel.CALL_STATUS_BOARD_ON)
        {
            return;
        }

        if(startPtMarker != null)
        {
            startPtMarker.setMap(null);
        }

        LatLng latLng = mainModel.getLocationInstantly();
        if(latLng == null)
        {
            return;
        }

        LatLng destination = mainModel.getCallDestinationPoint();
        if(destination == null)
        {
            // 목적지 없음

            addMyRunningLocationMarker(latLng);

            // 현재 위치 리버스 지오코드만
            RetrofitConnector.getReverseGeocode(latLng.latitude, latLng.longitude, resultObj ->
            {
                try
                {
                    if(mainModel.getCallStatus() != MainModel.CALL_STATUS_BOARD_ON)
                    {
                        expectedRemainTimeText.setVisibility(View.GONE);
                        return;
                    }

                    InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                    String text = CommonUtil.getTextFromAddress(result);

                    if(text != null && !text.trim().isEmpty())
                    {
                        taxiInfoText21Location.setText(text);
                    }
                    else
                    {
                        taxiInfoText21Location.setText(AppConstant.noValueText);
                    }

                    taxiInfoLayout.setVisibility(View.VISIBLE);
                    expectedRemainTimeText.setVisibility(View.GONE);

                    // 현재 위치 저장(안심메시지)
                    mainModel.setMyCurrentPositionName(text);
                }
                catch(Exception e)
                {

                }
            });
        }
        else
        {
            // 목적지 있음
            // 현재 위치부터 목적지까지 경로검색 후 경로 및 예상 소요시간 표시

            // 현재 위치 지도에 표시
            if(myPositionMarker != null)
            {
                if(mInaviMap != null)
                {
                    myPositionMarker.setPosition(latLng);
                    myPositionMarker.setMap(mInaviMap);
                }
                else
                {
                    lazySettingMapMarker = true;
                    userPosition = latLng;
                }
            }

            // 도착 위치 표시
            if(endPtMarker != null)
            {
                if(mInaviMap != null)
                {
                    endPtMarker.setPosition(destination);
                    endPtMarker.setMap(mInaviMap);
                }
                else
                {
                    lazySettingMapMarker = true;
                }
            }

            taxiInfoLayout.setVisibility(View.VISIBLE);

            RetrofitConnector.getRouteSearchNormalResult(latLng, destination, resultObj ->
            {
                try
                {
                    if(mainModel.getCallStatus() != MainModel.CALL_STATUS_BOARD_ON)
                    {
                        expectedRemainTimeText.setVisibility(View.GONE);
                        return;
                    }

                    InaviApiRouteNormalResult result = (InaviApiRouteNormalResult) resultObj;

                    // 경로 표시
                    if(expectedRoutePath == null)
                    {
                        //expectedRoutePath.setMap(null);
                        expectedRoutePath = new InvRoute();
                    }

                    int spendTime = result.route.data.spendTime;

                    // 최초로 측정된 예상 소요 시간을 저장한다.
                    int spendTimeStored = mainModel.getCallSpendTime();
                    if(spendTimeStored < 0)
                    {
                        mainModel.setCallSpendTime(spendTime);
                    }

                    List<InaviApiPath> paths = result.route.data.paths;
                    int lineColor = Color.parseColor("#e25321");
                    if(paths != null)
                    {
                        List<InvRoute.InvRouteLink> routeLinkList = makeRouteLink(
                                latLng, destination, paths, lineColor);

                        expectedRoutePath.setLinks(routeLinkList);
                        expectedRoutePath.setLineWidth((int) CommonUtil.convertDpToPixel(2f));
                        expectedRoutePath.setStrokeWidth(0);
                        expectedRoutePath.setMap(mInaviMap);
                    }

                    spendTime = spendTime/60;
                    String text = null;
                    if(spendTime > 0)
                    {
                        text = String.format(context.getString(R.string.main_text_expected_remain_time_format), spendTime);
                    }
                    else
                    {
                        text = context.getString(R.string.main_text_expected_remain_time_arrive_soon);
                    }

                    expectedRemainTimeText.setText(text);
                    expectedRemainTimeText.setVisibility(View.VISIBLE);

                    if(!userTouchMap)
                    {

                        // 출/도착지가 모두 보이게 조정
                        if(mInaviMap != null)
                        {
                            CameraUpdate cameraUpdate = getCameraUpdate(latLng, destination);
                            mInaviMap.moveCamera(cameraUpdate);
                        }
                    }
                }
                catch(Exception e)
                {

                }
            });

            // 현재 위치 저장(안심메시지)
            RetrofitConnector.getReverseGeocode(latLng.latitude, latLng.longitude, resultObj ->
            {
                try
                {
                    InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                    String text = CommonUtil.getTextFromAddress(result);
                    mainModel.setMyCurrentPositionName(text);
                }
                catch(Exception e)
                {

                }
            });
        }
    }

    private void setMapMarkerWhenUserOnTaxi(LatLng latLng, LatLng destination)
    {
        myPositionMarker.setPosition(latLng);
        myPositionMarker.setMap(mInaviMap);

        endPtMarker.setPosition(destination);
        endPtMarker.setMap(mInaviMap);
    }

    private void addMyRunningLocationMarker(LatLng latLng)
    {
        if(latLng == null)
        {
            return;
        }

        activity.runOnUiThread(() ->
        {
            try
            {
                // 현재 위치 지도에 표시
                myPositionMarker.setPosition(latLng);
                myPositionMarker.setMap(mInaviMap);

                // 해당 위치로 카메라 이동
                CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);
                cameraUpdate.setAnimationType(CameraAnimationType.Linear);
                mInaviMap.moveCamera(cameraUpdate);
            }
            catch(Exception e)
            {

            }
        });
    }

    /**
     * 두 지점이 한 화면에 들어올 수 있도록 중심 좌표와 줌 배율 측정
     * @param latLng1
     * @param latLng2
     * @return
     */
    private CameraUpdate getCameraUpdate(LatLng latLng1, LatLng latLng2)
    {
        if(mInaviMap == null)
        {
            return null;
        }

        LatLngBounds bounds = new LatLngBounds(latLng1, latLng2);

        int padding = (int) CommonUtil.convertDpToPixel(100f/*마커 넓이를 감안*/);
        int paddingTop = padding;
        int paddingBottom = padding;
        int callStatus = mainModel.getCallStatus();

        if(screenStatus == SCREEN_STATUS_SEARCH_EXPECTED_ROUTE)
        {
            // 하단 버튼 레이아웃 크기만큼 하단 패딩 추가
            paddingBottom += ((int) CommonUtil.convertDpToPixel(168f));
        }
        else if(screenStatus == SCREEN_STATUS_RUNNING_CALL)
        {
            // 상단 패딩 조정
            if(callStatus == MainModel.CALL_STATUS_BOARD_ON/*승차*/ ||
                    callStatus == MainModel.CALL_STATUS_ALLOC/*배차완료(기사님 이동)*/)
            {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) taxiInfoLayout.getLayoutParams();
                int topMargin = lp.topMargin;
                int taxiInfoLayoutHeight = taxiInfoLayout.getMeasuredHeight();

                paddingTop += (padding/*패딩 하나 더*/ + topMargin +
                        taxiInfoLayoutHeight + (int) CommonUtil.convertDpToPixel(12f));

                // 하단 버튼 레이아웃 크기만큼 하단 패딩 추가
                paddingBottom += ((int) CommonUtil.convertDpToPixel(118f));
            }
        }

        CameraPosition cameraPosition = mInaviMap.getCameraFitBounds(bounds, padding, paddingTop, padding, paddingBottom);

        double zoom = cameraPosition.zoom;
        if(zoom > 16)
        {
            // 최대 줌 레벨
            zoom = 16;
        }

        CameraUpdate cameraUpdate = CameraUpdate.targetTo(cameraPosition.target, zoom);
        cameraUpdate.setAnimationType(CameraAnimationType.Linear);

        return cameraUpdate;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void setActionBarStatus()
    {
        switch(screenStatus)
        {
            case SCREEN_STATUS_DEFAULT:
                actionMenuBtn.setVisibility(View.VISIBLE);
                actionBackBtn.setVisibility(View.GONE);
                actionExitBtn.setVisibility(View.GONE);
                break;

            case SCREEN_STATUS_DEFAULT_FULL_MAP:
            case SCREEN_STATUS_DIRECT_CALL_CHECK_START_PT:
                actionMenuBtn.setVisibility(View.GONE);
                actionBackBtn.setVisibility(View.GONE);
                actionExitBtn.setVisibility(View.GONE);
                break;

            case SCREEN_STATUS_RUNNING_CALL:
                actionMenuBtn.setVisibility(View.GONE);
                actionBackBtn.setVisibility(View.GONE);
                actionExitBtn.setVisibility(View.GONE);
                break;

            case SCREEN_STATUS_SEARCH_RESULT_SELECT:
                actionMenuBtn.setVisibility(View.GONE);
                actionBackBtn.setVisibility(View.GONE);
                actionExitBtn.setVisibility(View.VISIBLE);
                break;

            case SCREEN_STATUS_SEARCH_EXPECTED_ROUTE:
                actionMenuBtn.setVisibility(View.GONE);
                actionBackBtn.setVisibility(View.VISIBLE);
                actionExitBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void showMainBottomController(boolean anim)
    {
        if(anim)
        {
            callDirectlyMarker.setVisibility(View.VISIBLE);

            downAnimationRunningFlag = false;   // 반대 방향 플래그를 해제

            if(upAnimationRunningFlag)
            {
                return;
            }

            upAnimationRunningFlag = true;

            int distanceY = callDirectlyControlLayout.getMeasuredHeight();
            TranslateAnimation btnAnim = makeShowBtnAnim(distanceY, true);

            callDirectlyControlLayout.setVisibility(View.VISIBLE);
            callDirectlyControlLayout.startAnimation(bottomUpAnim);
            mapBtnLayout.startAnimation(btnAnim);
        }
        else
        {
            callCenterBtn.setVisibility(View.VISIBLE);
            callDirectlyControlLayout.setVisibility(View.VISIBLE);
        }
    }

    public void hideMainBottomController(boolean anim)
    {
        if(anim)
        {
            upAnimationRunningFlag = false;   // 반대 방향 플래그를 해제

            if(downAnimationRunningFlag)
            {
                return;
            }

            downAnimationRunningFlag = true;

            int distanceY = callDirectlyControlLayout.getMeasuredHeight();
            TranslateAnimation btnAnim = makeHideBtnAnim(distanceY);

            callDirectlyControlLayout.startAnimation(topDownAnim);
            mapBtnLayout.startAnimation(btnAnim);
        }
        else
        {
            callCenterBtn.setVisibility(View.GONE);
            callDirectlyControlLayout.setVisibility(View.GONE);
        }
    }

    public void showMainDirectCallCheckView(boolean anim)
    {
        if(anim)
        {
            if(upAnimationRunningFlag2)
            {
                return;
            }

            upAnimationRunningFlag2 = true;

            int distanceY = callDirectlyConfirmView.getMeasuredHeight();
            TranslateAnimation btnAnim = makeShowBtnAnim(distanceY, false);

            callDirectlyConfirmView.setVisibility(View.VISIBLE);
            callDirectlyConfirmView.startAnimation(bottomUpAnim2);
            mapBtnLayout.startAnimation(btnAnim);
        }
        else
        {
            callDirectlyConfirmView.setVisibility(View.VISIBLE);
        }
    }

    public void hideMainDirectCallCheckView(boolean anim)
    {
        if(anim)
        {
            if(downAnimationRunningFlag2)
            {
                return;
            }

            downAnimationRunningFlag2 = true;

            int distanceY = callDirectlyConfirmView.getMeasuredHeight();
            TranslateAnimation btnAnim = makeHideBtnAnim(distanceY);

            callDirectlyConfirmView.startAnimation(topDownAnim2);
            mapBtnLayout.startAnimation(btnAnim);
        }
        else
        {
            callDirectlyConfirmView.setVisibility(View.GONE);
        }
    }

    private void showCallConfirmView(boolean anim)
    {
        if(anim)
        {
            if(upAnimationRunningFlag3)
            {
                return;
            }

            upAnimationRunningFlag3 = true;

            int distanceY = callConfirmLayout.getMeasuredHeight();
            TranslateAnimation btnAnim = makeShowBtnAnim(distanceY, false);

            callConfirmLayout.setVisibility(View.VISIBLE);
            callConfirmLayout.startAnimation(bottomUpAnim3);
            mapBtnLayout.startAnimation(btnAnim);
        }
        else
        {
            callConfirmLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideCallConfirmView(boolean anim)
    {
        if(anim)
        {
            if(downAnimationRunningFlag3)
            {
                return;
            }

            downAnimationRunningFlag3 = true;

            int distanceY = callConfirmLayout.getMeasuredHeight();
            TranslateAnimation btnAnim = makeHideBtnAnim(distanceY);

            callConfirmLayout.startAnimation(topDownAnim3);
            mapBtnLayout.startAnimation(btnAnim);
        }
        else
        {
            callConfirmLayout.setVisibility(View.GONE);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private TranslateAnimation makeShowBtnAnim(int movingDistance, boolean showCallCenterBtn)
    {
        TranslateAnimation btnAnim = new TranslateAnimation(0, 0, movingDistance, 0);
        btnAnim.setDuration(200);

        if(showCallCenterBtn)
        {
            btnAnim.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {

                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    try
                    {
                        if(callCenterBtn.getVisibility() == View.GONE)
                        {
                            callCenterBtn.setVisibility(View.VISIBLE);
                            callCenterBtn.startAnimation(scaleShowAnim);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {

                }
            });
        }
        else
        {
            callCenterBtn.setVisibility(View.GONE);
        }

        return btnAnim;
    }

    private TranslateAnimation makeHideBtnAnim(int movingDistance)
    {
        TranslateAnimation btnAnim = new TranslateAnimation(0, 0, 0, movingDistance);
        btnAnim.setDuration(200);

        if(callCenterBtn.getVisibility() == View.VISIBLE)
        {
            btnAnim.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {

                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    try
                    {
                        callCenterBtn.startAnimation(scaleHideAnim);
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {

                }
            });
        }

        return btnAnim;
    }

    public void measureLocationOneTime()
    {
        mainModel.measureLocation();
    }
}
