package com.insoline.hanam.activity.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.AuthResult;
import com.insoline.hanam.net.response.HistoryData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HistoryActivity extends BaseActivity
{
    private Context context;
    private SharedPreferences pref;
    private LayoutInflater inflater;
    private Typeface typeface;
    private Typeface boldTypeface;

    private View actionBackBtn;
    private View actionExitBtn;
    private View actionBarDivider;

    private ScrollView scrollView;
    private LinearLayout container;
    private View deleteHistoryBtn;

    private View editBtnLayout;
    private TextView selectAllBtn;
    private TextView removeBtn;

    private View noHistoryLayout;

    private String mobileNum;

    private TimeZone timeZone;
    private SimpleDateFormat dateFormat1;
    private SimpleDateFormat dateFormat2;

    private boolean editable;

    private int oldScrollY = -1;
    private Handler scrollYCheckHandler;

    private Animation topDownAnim;
    private Animation bottomAnim;

    private boolean topAnimRunning;
    private boolean bottomAnimRunning;
    private boolean reserveTopAnim;
    private boolean reserveBottomAnim;

    private boolean scrollViewTouchDown;
    private boolean scrollViewTouchUp;

    private boolean changeHistory;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_history);

            setStatusBarWhite();

            context = this;
            pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);
            inflater = LayoutInflater.from(context);

            typeface = getTypeface();
            boldTypeface = getBoldTypeface();

            actionBackBtn = findViewById(R.id.action_btn_back);
            actionExitBtn = findViewById(R.id.action_btn_exit);
            actionBarDivider = findViewById(R.id.action_bar_divider);
            actionBackBtn.setOnClickListener(clickListener);
            actionExitBtn.setOnClickListener(clickListener);

            scrollView = findViewById(R.id.scroll_view);
            container = findViewById(R.id.history_item_container);
            deleteHistoryBtn = findViewById(R.id.btn_delete_history);
            deleteHistoryBtn.setOnClickListener(clickListener);

            editBtnLayout = findViewById(R.id.history_edit_btn_layout);
            selectAllBtn = findViewById(R.id.btn_select_all);
            removeBtn = findViewById(R.id.btn_remove);
            selectAllBtn.setTypeface(boldTypeface);
            removeBtn.setTypeface(boldTypeface);
            selectAllBtn.setOnClickListener(clickListener);
            removeBtn.setOnClickListener(clickListener);

            noHistoryLayout = findViewById(R.id.no_history_layout);

            TextView titleText = findViewById(R.id.title_text);
            titleText.setTypeface(boldTypeface);

            mobileNum = pref.getString(AppConstant.COMMON_DATA_MOBILE_NUMBER, null);

            timeZone = TimeZone.getTimeZone("+09:00");
            dateFormat1 = new SimpleDateFormat("yyyy.MM.dd");
            dateFormat2 = new SimpleDateFormat("HH:mm");
            dateFormat1.setTimeZone(timeZone);
            dateFormat2.setTimeZone(timeZone);

            topDownAnim = AnimationUtils.loadAnimation(context, R.anim.top_down_anim);
            topDownAnim.setDuration(120);
            topDownAnim.setAnimationListener(new Animation.AnimationListener()
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
                        topAnimRunning = false;
                        deleteHistoryBtn.setVisibility(View.GONE);

                        if(reserveBottomAnim)
                        {
                            // 버튼이 내려가는 와중에 스크롤이 종료됨.
                            // 다시 버튼을 위로 올린다.
                            reserveBottomAnim = false;
                            showDeleteHistoryBtn();
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

            bottomAnim = AnimationUtils.loadAnimation(context, R.anim.bottom_up_anim);
            bottomAnim.setDuration(120);
            bottomAnim.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {
                    try
                    {
                        deleteHistoryBtn.setVisibility(View.VISIBLE);
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    try
                    {
                        bottomAnimRunning = false;

                        if(reserveTopAnim)
                        {
                            // 버튼이 올라오는 중에 다시 내려가는 모션 진행
                            reserveTopAnim = false;
                            hideDeleteHistoryBtn();
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

            scrollView.setOnTouchListener((v, event) ->
            {
                try
                {
                    switch(event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            scrollViewTouchDown = true;
                            scrollViewTouchUp = false;
                            break;

                        case MotionEvent.ACTION_UP:
                            scrollViewTouchDown = false;
                            scrollViewTouchUp = true;
                            checkScrollY();
                            break;
                    }
                }
                catch(Exception e)
                {

                }

                return false;
            });

            scrollView.getViewTreeObserver().addOnScrollChangedListener(() ->
            {
                try
                {
                    int scrollY = scrollView.getScrollY();
                    oldScrollY = scrollY;

                    if(scrollViewTouchDown)
                    {
                        if(!editable &&
                                deleteHistoryBtn.getVisibility() == View.VISIBLE &&
                                !topAnimRunning)
                        {
                            if(bottomAnimRunning)
                            {
                                reserveTopAnim = true;
                            }
                            else
                            {
                                hideDeleteHistoryBtn();
                            }
                        }

                        checkScrollY();
                    }
                    else if(scrollViewTouchUp)
                    {
                        // 손가락이 떼어진 상태에서도 계속 스크롤이 되는 경우
                        checkScrollY();
                    }

                    actionBarDivider.setVisibility((scrollY > 0) ? View.VISIBLE : View.GONE);
                }
                catch(Exception e)
                {

                }
            });

            getHistoryList();
        }
        catch(Exception e)
        {
            //e.printStackTrace();
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private void checkScrollY()
    {
        if(scrollYCheckHandler != null)
        {
            scrollYCheckHandler.removeCallbacks(scrollYCheckRunnable);
        }

        scrollYCheckHandler = new Handler();
        scrollYCheckHandler.postDelayed(scrollYCheckRunnable, 70);
    }

    private Runnable scrollYCheckRunnable = () ->
    {
        try
        {
            scrollYCheckHandler = null;

            if(!scrollViewTouchDown && oldScrollY == scrollView.getScrollY())
            {
                // 스크롤 정지(idle)
                if(!editable/*편집 상태가 아니고*/ &&
                        deleteHistoryBtn.getVisibility() == View.GONE /*버튼이 보이지 않는 상태*/ &&
                        !bottomAnimRunning/*애니메이션이 이미 실행 중이 아님*/)
                {
                    if(topAnimRunning)
                    {
                        // 버튼이 내려가는 애니메이션 진행 중(스크롤에 의해)
                        reserveBottomAnim = true;
                    }
                    else
                    {
                        reserveBottomAnim = false;
                        showDeleteHistoryBtn();
                    }
                }

                scrollViewTouchUp = false;
            }
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
    };

    private void showDeleteHistoryBtn()
    {
        runOnUiThread(() ->
        {
            try
            {
                // 탑승이력 삭제 버튼 노출
                bottomAnimRunning = true;
                deleteHistoryBtn.startAnimation(bottomAnim);
            }
            catch(Exception e)
            {

            }
        });
    }

    private void hideDeleteHistoryBtn()
    {
        runOnUiThread(() ->
        {
            try
            {
                // 탑승이력 삭제 버튼 감춤
                topAnimRunning = true;
                deleteHistoryBtn.startAnimation(topDownAnim);
            }
            catch(Exception e)
            {

            }
        });
    }

    private void getHistoryList()
    {
        container.removeAllViews();

        RetrofitConnector.getHistory(mobileNum, resultObj ->
        {
            try
            {
                List<HistoryData> historyDataList = (List<HistoryData>) resultObj;
                if(historyDataList != null && historyDataList.size() > 0)
                {
                    Collections.sort(historyDataList, comparator);  // 최근 이력 순으로 정렬

                    for(HistoryData data : historyDataList)
                    {
                        addHistoryView(data);
                    }

                    scrollView.setVisibility(View.VISIBLE);
                    noHistoryLayout.setVisibility(View.GONE);
                    deleteHistoryBtn.setVisibility(View.VISIBLE);
                }
                else
                {
                    scrollView.setVisibility(View.GONE);
                    noHistoryLayout.setVisibility(View.VISIBLE);
                    deleteHistoryBtn.setVisibility(View.GONE);
                }

                scrollView.post(() ->
                {
                   try
                   {
                       scrollView.scrollTo(0, 0);
                   }
                   catch(Exception e)
                   {
                        //e.printStackTrace();
                   }
                });
            }
            catch(Exception e)
            {
                //e.printStackTrace();
            }
        });
    }

    private Comparator<HistoryData> comparator = (o1, o2) ->
    {
        try
        {
            long time1 = o1.getGetInTime();
            long time2 = o2.getGetInTime();

            if(time1 > time2)
            {
                return -1;
            }
            else if(time1 < time2)
            {
                return 1;
            }
        }
        catch(Exception e)
        {

        }

        return 0;
    };

    @Override
    public void onBackPressed()
    {
        try
        {
            if(editable)
            {
                onClickActionBackBtn();
            }
            else
            {
                finishNormal();
            }
        }
        catch(Exception e)
        {

        }
    }

    private void addHistoryView(HistoryData data)
    {
        View view = inflater.inflate(R.layout.layout_history_item, null);

        ViewHolder holder = new ViewHolder();
        view.setTag(holder);

        holder.boardingDateText = view.findViewById(R.id.boarding_date);
        holder.elapsedTimeText = view.findViewById(R.id.elapsed_time);
        holder.checkBtn = view.findViewById(R.id.btn_check);
        holder.departureNameText = view.findViewById(R.id.departure_name_text);
        holder.destinationNameText = view.findViewById(R.id.destination_name_text);
        holder.getInTimeText = view.findViewById(R.id.get_in_time_text);
        holder.getOutTimeText = view.findViewById(R.id.get_out_time_text);
        holder.callBtn = view.findViewById(R.id.btn_call);
        holder.driverInfoText = view.findViewById(R.id.driver_info_text);
        holder.data = data;

        holder.boardingDateText.setTypeface(boldTypeface);
        holder.elapsedTimeText.setTypeface(typeface);
        holder.departureNameText.setTypeface(typeface);
        holder.destinationNameText.setTypeface(typeface);
        holder.getInTimeText.setTypeface(typeface);
        holder.getOutTimeText.setTypeface(typeface);
        holder.callBtn.setTypeface(typeface);
        holder.driverInfoText.setTypeface(typeface);

        // 소요 시간
        String elapsedTimeText = null;
        long elapsedTime = 0;   // min
        long inTime = data.getGetInTime();
        long outTime = data.getGetOutTime();
        if(inTime > 0 && outTime > 0)
        {
            elapsedTime = (outTime - inTime) / 1000/*sec*/ / 60/*min*/;

            long hour = elapsedTime / 60;
            long min = elapsedTime % 60;
            if(hour > 0)
            {
                elapsedTimeText = String.format(getString(R.string.app_text_path_elapsed_time_1), hour, min);
            }
            else
            {
                elapsedTimeText = String.format(getString(R.string.app_text_path_elapsed_time_2), min);
            }
        }

        holder.elapsedTimeText.setText(elapsedTimeText);

        // 탑승일
        if(inTime > 0)
        {
            Calendar calendar1 = Calendar.getInstance(timeZone);
            Calendar calendar2 = Calendar.getInstance(timeZone);
            calendar2.setTimeInMillis(inTime);

            int year1 = calendar1.get(Calendar.YEAR);
            int month1 = calendar1.get(Calendar.MONTH);
            int date1 = calendar1.get(Calendar.DAY_OF_MONTH);

            int year2 = calendar2.get(Calendar.YEAR);
            int month2 = calendar2.get(Calendar.MONTH);
            int date2 = calendar2.get(Calendar.DAY_OF_MONTH);

            if(year1 == year2 && month1 == month2 && date1 == date2)
            {
                // 오늘
                holder.boardingDateText.setText(getString(R.string.app_text_today));
            }
            else
            {
                calendar1.add(Calendar.DAY_OF_MONTH, -1);
                year1 = calendar1.get(Calendar.YEAR);
                month1 = calendar1.get(Calendar.MONTH);
                date1 = calendar1.get(Calendar.DAY_OF_MONTH);

                if(year1 == year2 && month1 == month2 && date1 == date2)
                {
                    // 어제
                    holder.boardingDateText.setText(getString(R.string.app_text_yesterday));
                }
                else
                {
                    // 그 이전
                    String text = dateFormat1.format(new Date(inTime));
                    holder.boardingDateText.setText(text);
                }
            }
        }
        else
        {
            holder.boardingDateText.setText("");
        }

        // 출발지와 도착지
        holder.departureNameText.setText(data.getDepartureName());
        holder.destinationNameText.setText(data.getDestinationName());

        if(inTime > 0)
        {
            holder.getInTimeText.setText(dateFormat2.format(new Date(inTime)));
        }
        else
        {
            holder.getInTimeText.setText("");
        }

        if(outTime > 0)
        {
            holder.getOutTimeText.setText(dateFormat2.format(new Date(outTime)));
        }
        else
        {
            holder.getOutTimeText.setText("");
        }

        //String driverName = String.format(getString(R.string.main_text_driver_name_format), data.getDriverName());
        String carTypeName = data.getCarTypeName();

        String driverInfo = null;
        //Log.d(AppConstant.LOG_TEMP_TAG, "carTypeName = " + carTypeName);
        if(carTypeName != null && !carTypeName.trim().isEmpty())
        {
            driverInfo = carTypeName + " | " + data.getCarId();
        }
        else
        {
            driverInfo = data.getCarId();
        }

        holder.driverInfoText.setText(driverInfo);

        holder.callBtn.setOnClickListener(historyClickListener);
        holder.callBtn.setTag(data);

        holder.checkBtn.setOnClickListener(historyCheckBtnClickListener);

        container.addView(view);
    }

    private View.OnClickListener historyClickListener = v ->
    {
        try
        {
            HistoryData data = (HistoryData) v.getTag();

            // 선택된 경로로 바로 호출

            // 출발지
            RequestLocation departure = new RequestLocation();
            departure.positionName = data.getDepartureName();
            departure.lat = Double.parseDouble(data.getDepartureLat());
            departure.lng = Double.parseDouble(data.getDepartureLng());

            // 도착지
            RequestLocation destination = null;
            String destinationName = data.getDestinationName();
            String destinationLat = data.getDestinationLat();
            String destinationLng = data.getDestinationLng();

            if(destinationName != null && !destinationName.trim().isEmpty() &&
                    destinationLat != null && !destinationLat.trim().isEmpty() &&
                    destinationLng != null && !destinationLng.trim().isEmpty())
            {
                destination = new RequestLocation();
                destination.positionName = destinationName;
                destination.lat = Double.parseDouble(destinationLat);
                destination.lng = Double.parseDouble(destinationLng);
            }

            // 메인으로 연결
            Intent intent = getIntent();
            intent.putExtra("departure", departure);

            if(destination != null)
            {
                intent.putExtra("destination", destination);
            }

            setResult(RESULT_OK, intent);
            finish();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private View.OnClickListener historyCheckBtnClickListener = v ->
    {
        try
        {
            boolean selected = v.isSelected();
            v.setSelected(!selected);

            if(selected)
            {
                // 해제됨
                setSelectAllBtn(false);
            }
            else
            {
                // 선택됨
                boolean selectAll = true;
                int count = container.getChildCount();
                for(int i = 0; i < count; i++)
                {
                    View view = container.getChildAt(i);
                    ViewHolder holder = (ViewHolder) view.getTag();
                    if(!holder.checkBtn.isSelected())
                    {
                        selectAll = false;
                        break;
                    }
                }

                setSelectAllBtn(selectAll);
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private void finishNormal()
    {
        Intent intent = getIntent();
        intent.putExtra("changeHistory", changeHistory);
        setResult(RESULT_OK, intent);
        finish();
    }

    private View.OnClickListener clickListener = v ->
    {
        try
        {
            switch(v.getId())
            {
                case R.id.action_btn_back:
                    onClickActionBackBtn();
                    break;

                case R.id.action_btn_exit:
                    finishNormal();
                    break;

                case R.id.btn_delete_history:
                    actionBackBtn.setVisibility(View.VISIBLE);
                    actionExitBtn.setVisibility(View.GONE);
                    deleteHistoryBtn.setVisibility(View.GONE);
                    editBtnLayout.setVisibility(View.VISIBLE);
                    setHistoryViewEditable(true);
                    break;

                case R.id.btn_select_all:
                    selectAll();
                    break;

                case R.id.btn_remove:
                    String data = getSelectedItemData();
                    if(data != null && !data.trim().isEmpty())
                    {
                        if("ALL".equals(data))
                        {
                            // 모두 삭제
                            RetrofitConnector.removeHistoryAll(mobileNum, resultObj ->
                            {
                                try
                                {
                                    AuthResult result = (AuthResult) resultObj;
                                    if(result.isSuccessful())
                                    {
                                        onRemoveHistorySuccessful();
                                    }
                                }
                                catch(Exception e)
                                {

                                }
                            });
                        }
                        else
                        {
                            // 일부 삭제
                            RetrofitConnector.removeHistory(mobileNum, data, resultObj ->
                            {
                                try
                                {
                                    AuthResult result = (AuthResult) resultObj;
                                    if(result.isSuccessful())
                                    {
                                        onRemoveHistorySuccessful();
                                    }
                                }
                                catch(Exception e)
                                {

                                }
                            });
                        }
                    }
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private void onRemoveHistorySuccessful()
    {
        changeHistory = true;

        // 삭제 모드 종료
        onClickActionBackBtn();

        // 탑승 이력 갱신
        getHistoryList();
    }

    private void onClickActionBackBtn()
    {
        actionBackBtn.setVisibility(View.GONE);
        actionExitBtn.setVisibility(View.VISIBLE);
        deleteHistoryBtn.setVisibility(View.VISIBLE);
        editBtnLayout.setVisibility(View.GONE);
        setHistoryViewEditable(false);

        // 애니메이션 관련 플래그 모두 해제
        topAnimRunning = false;
        bottomAnimRunning = false;
        reserveTopAnim = false;
        reserveBottomAnim = false;

        scrollViewTouchDown = false;
        scrollViewTouchUp = false;
    }

    private void setHistoryViewEditable(boolean editable)
    {
        this.editable = editable;

        int count = container.getChildCount();
        for(int i = 0; i < count; i++)
        {
            View view = container.getChildAt(i);
            ViewHolder holder = (ViewHolder) view.getTag();

            if(editable)
            {
                holder.elapsedTimeText.setVisibility(View.GONE);
                holder.checkBtn.setVisibility(View.VISIBLE);
                holder.checkBtn.setSelected(false);
                holder.callBtn.setEnabled(false);
            }
            else
            {
                holder.elapsedTimeText.setVisibility(View.VISIBLE);
                holder.checkBtn.setVisibility(View.GONE);
                holder.callBtn.setEnabled(true);
            }
        }
    }

    private void selectAll()
    {
        if(!editable)
        {
            return;
        }

        boolean selected = selectAllBtn.isSelected();
        setSelectAllBtn(!selected);

        int count = container.getChildCount();
        for(int i = 0; i < count; i++)
        {
            View view = container.getChildAt(i);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.checkBtn.setSelected(!selected);
        }
    }

    private void setSelectAllBtn(boolean selected)
    {
        selectAllBtn.setSelected(selected);
        selectAllBtn.setText(selected ?
                getString(R.string.app_text_deselect_all) :
                getString(R.string.app_text_select_all));
    }

    private String getSelectedItemData()
    {
        if(!editable)
        {
            return null;
        }

        String data = "";
        boolean allSelected = true;
        int count = container.getChildCount();
        for(int i = 0; i < count; i++)
        {
            View view = container.getChildAt(i);
            ViewHolder holder = (ViewHolder) view.getTag();
            if(holder.checkBtn.isSelected())
            {
                data += (holder.data.getId() + ",");
            }
            else
            {
                allSelected = false;
            }
        }

        if(allSelected)
        {
            return "ALL";
        }

        if(data.endsWith(","))
        {
            data = data.substring(0, data.length()-1);
        }

        return data;
    }

    public class ViewHolder
    {
        public TextView boardingDateText;
        public TextView elapsedTimeText;
        public View checkBtn;

        public TextView departureNameText;
        public TextView destinationNameText;
        public TextView getInTimeText;
        public TextView getOutTimeText;

        public TextView callBtn;
        public TextView driverInfoText;

        public HistoryData data;
    }
}
