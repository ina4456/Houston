package com.insoline.hanam.activity.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.inavi.mapsdk.geometry.LatLng;
import com.inavi.mapsdk.maps.CameraAnimationType;
import com.inavi.mapsdk.maps.CameraUpdate;
import com.inavi.mapsdk.maps.InaviMap;
import com.inavi.mapsdk.maps.InaviMapSdk;
import com.inavi.mapsdk.maps.InvMapFragment;
import com.inavi.mapsdk.style.shapes.InvImage;
import com.inavi.mapsdk.style.shapes.InvMarker;
import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.activity.main.MainModel;
import com.insoline.hanam.adapter.SearchResultBaseAdapter;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.inaviapi.InaviApiReverseGeocode;
import com.insoline.hanam.util.CommonUtil;
import com.insoline.hanam.view.HoustonStyleDialog;
import com.insoline.hanam.view.HoustonStyleDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class FavoriteLocationActivity extends BaseActivity
{
    private Context context;
    private SharedPreferences pref;
    private LayoutInflater inflater;

    private TextView titleText;

    private EditText editText;
    private View clearTextBtn;
    private View myLocationBtn1;

    private ListView listView;
    private SearchResultBaseAdapter adapter;

    private View searchActionBar;
    private View searchLayout;
    private View noSearchResultLayout;
    private View searchKeywordSuggestionLayout;
    private TextView searchKeywordSuggestionText;
    private View retryUsingSuggestionBtn;

    private View myLocationBtn2;
    private TextView registBtn;

    private InvMapFragment invMapFragment;
    private InaviMap mInaviMap;

    private MainModel mainModel;
    private int type;
    private RequestLocation selectedLocation;

    private InputMethodManager inputMethodManager;

    private InvMarker locationMarker;

    private HoustonStyleDialogBuilder dialogBuilder;

    private boolean firstLoading = true;
    private boolean byItemClick;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_favorite_location);

            context = this;
            pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);
            inflater = LayoutInflater.from(context);

            setStatusBarWhite();
            mainModel = new MainModel(context);

            initView();
            initData();
        }
        catch(Exception e)
        {
            //e.printStackTrace();
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private void initView()
    {
        titleText = findViewById(R.id.title_text);

        View actionBackBtn = findViewById(R.id.btn_title_back);
        actionBackBtn.setOnClickListener(clickListener);

        InaviMapSdk.getInstance(this).setAuthFailureCallback((errCode, msg) ->
        {
            try
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, "inavi map init error! errorCode = " + errCode);
            }
            catch(Exception e)
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
            }
        });

        invMapFragment = (InvMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        invMapFragment.getMapAsync(inaviMap ->
        {
            try
            {
                mInaviMap = inaviMap;
                initLocation(false);

                mInaviMap.addOnCameraIdleListener(() ->
                {
                    try
                    {
                        if(firstLoading)
                        {
                            firstLoading = false;
                            return;
                        }

                        if(byItemClick)
                        {
                            byItemClick = false;
                            return;
                        }

                        // 주소 검색 뒤에 좌표 이동.
                        LatLng latLng = mInaviMap.getCameraPosition().target;
                        RetrofitConnector.getReverseGeocode(latLng.latitude, latLng.longitude, resultObj ->
                        {
                            try
                            {
                                InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                                String text = CommonUtil.getReverseGeocodeString(result, false);
                                editText.setText(text);

                                mainModel.setRequestLocation(result, latLng);
                                selectedLocation = mainModel.getRequestLocation();
                            }
                            catch(Exception e)
                            {

                            }
                        });

                        // 마커 위치 변경
                        locationMarker.setPosition(latLng);
                        locationMarker.setMap(mInaviMap);
                    }
                    catch(Exception e)
                    {

                    }
                });
            }
            catch(Exception e)
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
            }
        });

        editText = findViewById(R.id.edit_text);
        editText.setOnEditorActionListener(editSearchKeyListener);
        editText.addTextChangedListener(textWatcher);
        editText.setOnFocusChangeListener(onFocusChangeListener);
        editText.requestFocus();

        clearTextBtn = findViewById(R.id.btn_clear_text);
        myLocationBtn1 = findViewById(R.id.btn_my_location);
        clearTextBtn.setOnClickListener(clickListener);
        myLocationBtn1.setOnClickListener(clickListener);

        listView = findViewById(R.id.list_view);
        adapter = new SearchResultBaseAdapter(context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onSearchItemClickListener);

        myLocationBtn2 = findViewById(R.id.btn_map_my_location2);
        registBtn = findViewById(R.id.btn_select_location);
        myLocationBtn2.setOnClickListener(clickListener);
        registBtn.setOnClickListener(clickListener);

        Typeface boldTypeface = getBoldTypeface();
        titleText.setTypeface(boldTypeface);
        registBtn.setTypeface(boldTypeface);

        searchActionBar = findViewById(R.id.search_action_bar);
        searchLayout = findViewById(R.id.search_layout);
        noSearchResultLayout = findViewById(R.id.no_search_result_layout);
        searchKeywordSuggestionLayout = findViewById(R.id.search_keyword_suggestion_layout);
        searchKeywordSuggestionText = findViewById(R.id.search_keyword_suggestion_text);
        retryUsingSuggestionBtn = findViewById(R.id.btn_retry_using_suggestion);
        retryUsingSuggestionBtn.setOnClickListener(clickListener);

        // 지도 마커
        View markerView = inflater.inflate(R.layout.marker_layout_point, null);
        View view = markerView.findViewById(R.id.marker_view);
        view.setBackgroundResource(R.drawable.map_pin_regist);

        Bitmap bitmap = CommonUtil.createMarkerBitmapUsingView(markerView, Bitmap.Config.ARGB_8888);
        InvImage image = new InvImage(bitmap);

        locationMarker = new InvMarker();
        locationMarker.setIconImage(image);

        dialogBuilder = new HoustonStyleDialogBuilder(context);
    }

    private void initData()
    {
        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);

        if(type == 0)
        {
            // fatal error!
            finishTask(true);
            return;
        }

        int iconType = 0;
        if(type == AppConstant.HOME)
        {
            titleText.setText(getString(R.string.menu_text_add_home));
            iconType = SearchResultBaseAdapter.ICON_TYPE_HOME;
            registBtn.setText(getString(R.string.menu_text_add_home_confirm));
        }
        else
        {
            titleText.setText(getString(R.string.menu_text_add_office));
            iconType = SearchResultBaseAdapter.ICON_TYPE_OFFICE;
            registBtn.setText(getString(R.string.menu_text_add_office_confirm));
        }

        adapter.setIconType(iconType);

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    private void initLocation(boolean anim)
    {
        LatLng latLng = mainModel.getLocationInstantly();
        if(latLng != null)
        {
            CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);

            if(anim)
            {
                cameraUpdate.setAnimationType(CameraAnimationType.Linear);
            }

            mInaviMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        try
        {
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        catch(Exception e)
        {

        }
    }

    @Override
    public void onBackPressed()
    {
        try
        {
            if(searchLayout.getVisibility() == View.GONE)
            {
                // 등록 화면 상태
                editText.setText(adapter.getKeyword());
                setSearchLayout(true);
            }
            else
            {
                finishTask(true);
            }
        }
        catch(Exception e)
        {
            super.onBackPressed();
        }
    }

    private View.OnClickListener clickListener = v ->
    {
        try
        {
            switch(v.getId())
            {
                case R.id.btn_title_back:
                    finishTask(true);
                    break;

                case R.id.btn_retry_using_suggestion:
                    String keyword = searchKeywordSuggestionText.getText().toString().trim();
                    searchByKeyword(keyword);
                    break;

                case R.id.btn_clear_text:
                    adapter.setLocationList(null);
                    adapter.notifyDataSetChanged();
                    setSearchLayout(true);
                    editText.setText("");
                    break;

                case R.id.btn_my_location:
                    LatLng latLng = mainModel.getLocationInstantly();
                    RetrofitConnector.getReverseGeocode(latLng.latitude, latLng.longitude, resultObj ->
                    {
                        try
                        {
                            if(resultObj != null)
                            {
                                InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                                mainModel.setRequestLocation(result, latLng);
                                RequestLocation location = mainModel.getRequestLocation();
                                List<RequestLocation> locationList = new ArrayList<>();
                                locationList.add(location);

                                setSearchResult(locationList);

                                editText.setText(location.positionName);
                                adapter.setKeyword(location.positionName);
                            }
                        }
                        catch(Exception e)
                        {

                        }
                    });
                    break;

                case R.id.btn_select_location:
                    if(selectedLocation != null)
                    {
                        // 장소 등록
                        CommonUtil.addFavoriteLocation(type, pref, selectedLocation);

                        // 팝업
                        String name = selectedLocation.getNameUsingAddres();
                        String format = null;

                        if(type == AppConstant.HOME)
                        {
                            format = getString(R.string.menu_text_regist_home_success);
                        }
                        else
                        {
                            format = getString(R.string.menu_text_regist_office_success);
                        }

                        HoustonStyleDialog dialog =
                                dialogBuilder.setTitle(String.format(format, name))
                                        .setDismissListener(() ->
                                        {
                                            try
                                            {
                                                // 팝업 종료 후 액티비티 종료
                                                finishTask(false);
                                            }
                                            catch(Exception e)
                                            {

                                            }
                                        })
                                        .setAutoCancelable(true).build();

                        dialog.show();
                    }
                    break;

                case R.id.btn_map_my_location2:
                    initLocation(true);
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private TextWatcher textWatcher = new TextWatcher()
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
                if(searchLayout.getVisibility() == View.VISIBLE)
                {
                    int len = editText.getText().toString().length();
                    if(len <= 0)
                    {
                        adapter.setKeyword(null);
                        adapter.setLocationList(null);
                        adapter.notifyDataSetChanged();

                        setNoSearchResultLayout(true, null);

                        clearTextBtn.setVisibility(View.GONE);
                        myLocationBtn1.setVisibility(View.VISIBLE);

                        selectedLocation = null;

                        editText.requestFocus();
                    }
                    else
                    {
                        clearTextBtn.setVisibility(View.VISIBLE);
                        myLocationBtn1.setVisibility(View.GONE);
                    }
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
    };

    private View.OnFocusChangeListener onFocusChangeListener = (v, hasFocus) ->
    {
        try
        {
            if(hasFocus)
            {
                setSearchLayout(true);

                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        }
        catch(Exception e)
        {

        }
    };

    private TextView.OnEditorActionListener editSearchKeyListener = (v, actionId, event) ->
    {
        try
        {
            if(actionId == EditorInfo.IME_ACTION_SEARCH)
            {
                String keyword = editText.getText().toString();
                searchByKeyword(keyword);
            }
        }
        catch(Exception e)
        {

        }

        return false;
    };

    private void searchByKeyword(String keyword)
    {
        if(keyword == null || keyword.trim().isEmpty())
        {
            return;
        }

        adapter.setKeyword(keyword);

        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        // 아이나비 POI 검색
        final String keywordModified = keyword.replaceAll(" ", "+");
        //final String keywordModified = URLEncoder.encode(keyword, "UTF-8");
        RetrofitConnector.search(keywordModified.trim(), resultObj ->
        {
            try
            {
                if(resultObj != null)
                {
                    List<RequestLocation> locationList = (List<RequestLocation>) resultObj;
                    setSearchResult(locationList);
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

                        }
                    });

                    setNoSearchResultLayout(false, null);
                }

                setSearchLayout(true);
            }
            catch(Exception e)
            {

            }
        });
    }

    private void setSearchResult(List<RequestLocation> locationList)
    {
        adapter.setLocationList(locationList);
        adapter.notifyDataSetChanged();

        setNoSearchResultLayout(false, null);
    }

    private AdapterView.OnItemClickListener onSearchItemClickListener = (parent, view, position, id) ->
    {
        try
        {
            editText.clearFocus();

            SearchResultBaseAdapter.ViewHolder holder = (SearchResultBaseAdapter.ViewHolder) view.getTag();
            RequestLocation selectedItem = (RequestLocation) holder.selectBtn.getTag();

            byItemClick = true;

            selectedLocation = selectedItem;
            editText.setText(selectedLocation.positionName);

            double lat = selectedItem.lat;
            double lng = selectedItem.lng;
            LatLng latLng = new LatLng(lat, lng);

            locationMarker.setPosition(latLng);
            locationMarker.setMap(mInaviMap);

            CameraUpdate cameraUpdate = CameraUpdate.targetTo(latLng, 16f);
            cameraUpdate.setAnimationType(CameraAnimationType.Linear);
            mInaviMap.moveCamera(cameraUpdate);

            setSearchLayout(false);

            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            View editTextLayout = findViewById(R.id.edit_text_layout);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                editTextLayout.setOutlineSpotShadowColor(Color.parseColor("#1a86a4"));
            }
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
    };

    private void setSearchLayout(boolean show)
    {
        searchLayout.setVisibility(show ? View.VISIBLE : View.GONE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            searchActionBar.setElevation(show ? 0 : CommonUtil.convertDpToPixel(16f));
        }

        if(show)
        {
            selectedLocation = null;
        }
    }

    private void setNoSearchResultLayout(boolean show, String suggestedKeyword)
    {
        boolean validKeyword = (suggestedKeyword != null && !suggestedKeyword.trim().isEmpty());
        searchKeywordSuggestionText.setText(suggestedKeyword + " ");
        searchKeywordSuggestionLayout.setVisibility(validKeyword ? View.VISIBLE : View.GONE);
        retryUsingSuggestionBtn.setVisibility(validKeyword ? View.VISIBLE : View.GONE);
        noSearchResultLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void finishTask(boolean cancel)
    {
        Intent intent = getIntent();
        intent.putExtra("type", cancel ? -1 : type);
        setResult(cancel ? RESULT_CANCELED : RESULT_OK, intent);
        finish();
    }
}
