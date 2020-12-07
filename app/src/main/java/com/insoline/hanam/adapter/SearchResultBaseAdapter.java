package com.insoline.hanam.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.CommonAssets;
import com.insoline.hanam.dto.RequestLocation;

import java.util.List;

public class SearchResultBaseAdapter extends BaseAdapter
{
    public static final int ICON_TYPE_BASIC     = 1;
    public static final int ICON_TYPE_HOME      = 2;
    public static final int ICON_TYPE_OFFICE    = 3;

    private Context context;
    private Typeface baseTypeface;
    private LayoutInflater inflater;

    private List<RequestLocation> locationList;
    private String keyword;
    private int directionFlag;      // 1: 출발지, 2: 도착지

    private ForegroundColorSpan foregroundColorSpan;
    private int iconType = ICON_TYPE_BASIC;

    public SearchResultBaseAdapter(Context context)
    {
        this.context = context;
        inflater = LayoutInflater.from(context);
        baseTypeface = CommonAssets.getTypeface(context.getAssets());

        int spannableTextColor = Color.parseColor("#1a86a4");
        foregroundColorSpan = new ForegroundColorSpan(spannableTextColor);
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public int getDirectionFlag()
    {
        return directionFlag;
    }

    public void setDirectionFlag(int directionFlag)
    {
        this.directionFlag = directionFlag;
    }

    public void setLocationList(List<RequestLocation> locationList)
    {
        this.locationList = locationList;
    }

    public void setIconType(int iconType)
    {
        this.iconType = iconType;
    }

    @Override
    public int getCount()
    {
        try
        {
            if(locationList != null)
            {
                return locationList.size();
            }
        }
        catch(Exception e)
        {

        }


        return 0;
    }

    @Override
    public RequestLocation getItem(int position)
    {
        try
        {
            if(locationList != null)
            {
                return locationList.get(position);
            }
        }
        catch(Exception e)
        {

        }

        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        try
        {
            ViewHolder holder = null;

            if(convertView == null)
            {
                convertView = inflater.inflate(R.layout.adapter_search_result_item, null);
                holder = new ViewHolder();

                holder.locationNameText = convertView.findViewById(R.id.location_name);
                holder.locationFullAddressText = convertView.findViewById(R.id.location_address);
                holder.selectBtn = convertView.findViewById(R.id.btn_select_list_item);
                holder.bottomLine = convertView.findViewById(R.id.bottom_line);

                holder.locationNameText.setTypeface(baseTypeface);
                holder.locationFullAddressText.setTypeface(baseTypeface);

                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            RequestLocation location = getItem(position);
            holder.selectBtn.setTag(location);  // 클릭 처리를 위해 버튼에 정보 저장

            if(iconType == ICON_TYPE_BASIC)
            {
                holder.selectBtn.setBackgroundResource(R.drawable.dt_list_btn_sel);
            }
            else if(iconType == ICON_TYPE_HOME)
            {
                holder.selectBtn.setBackgroundResource(R.drawable.dt_list_btn_add_home);
            }
            else if(iconType == ICON_TYPE_OFFICE)
            {
                holder.selectBtn.setBackgroundResource(R.drawable.dt_list_btn_add_office);
            }

            String name = location.positionName;
            String address = location.positionDetailName;

            // 키워드와 일치하는 부분이 있는지 확인
            int[] spannableStringRange = getSpannableStringRange(name, keyword.replaceAll(" ", ""));
            if(spannableStringRange[1] != -1)
            {
                // 일치하는 부분만 Foreground Spannable Text로 처리
                int start = spannableStringRange[0];
                int end = spannableStringRange[1];
                SpannableStringBuilder builder = new SpannableStringBuilder(name);
                builder.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                holder.locationNameText.setText(builder);
            }
            else
            {
                // 일치하는 부분 없음(1글자는 일치하지 않는다고 본다)
                holder.locationNameText.setText(name);
            }

            holder.locationFullAddressText.setText(address);
            holder.locationFullAddressText.setVisibility((address != null) ? View.VISIBLE : View.GONE);

            int count = getCount();
            holder.bottomLine.setVisibility((position < count-1) ? View.VISIBLE : View.GONE);
        }
        catch(Exception e)
        {

        }

        return convertView;
    }

    private int[] getSpannableStringRange(String name, String keyword)
    {
        int len = keyword.length();

        int[] result = new int[2];
        result[0] = 0;
        result[1] = -1;     // 무제약으로 우선 설정

        if(len <= 0)
        {
            return result;
        }

        if(name.equals(keyword))
        {
            result[1] = name.length();
        }
        else if(name.contains(keyword))
        {
            result[0] = name.indexOf(keyword);  // 시작
            result[1] = result[0] + len;        // 끝(exclusive?)
        }
        else
        {
            String org = keyword;

            while(len >= 3)
            {
                keyword = keyword.substring(0, len-1);
                len = keyword.length();

                if(name.equals(keyword))
                {
                    result[1] = name.length();
                    return result;
                }
                else if(name.contains(keyword))
                {
                    result[0] = name.indexOf(keyword);  // 시작
                    result[1] = result[0] + len;        // 끝(exclusive?)
                    return result;
                }
            }

            // 여기까지도 종료가 되지 않음
            // 반대 방향으로 시행
            keyword = org;
            len = keyword.length();

            while(len >= 3)
            {
                keyword = keyword.substring(1); // 앞에서 한 글자를 지운다.
                len = keyword.length();

                if(name.equals(keyword))
                {
                    result[1] = name.length();
                    return result;
                }
                else if(name.contains(keyword))
                {
                    result[0] = name.indexOf(keyword);  // 시작
                    result[1] = result[0] + len;        // 끝(exclusive?)
                    return result;
                }
            }
        }

        return result;
    }

    public class ViewHolder
    {
        public TextView locationNameText;
        public TextView locationFullAddressText;
        public View selectBtn;
        public View bottomLine;
    }
}
