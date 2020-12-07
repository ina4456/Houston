package com.insoline.hanam.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.CommonAssets;
import com.insoline.hanam.constant.AppConstant;

public class HoustonStyleDialog extends Dialog
{
    private Handler handler;

    private Context mContext;

    private TextView boldTitleText;
    private TextView titleText;
    private TextView subTitleText;
    private TextView contentText;

    private View divider;

    private View buttonContainer;

    private TextView negativeBtn;
    private TextView positiveBtn;
    private View btnDivider;

    private View reviewStarContainer;
    private View[] reviewStars;

    private boolean autoCancelable;

    private Object data;

    public HoustonStyleDialog(@NonNull Context context)
    {
        super(context);
        mContext = context;
    }

    public HoustonStyleDialog(@NonNull Context context, int themeResId)
    {
        super(context, themeResId);
        mContext = context;
    }

    protected HoustonStyleDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener)
    {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    public interface OnNegativeButtonListener
    {
        void onClick();
    }

    public interface OnPositiveButtonListener
    {
        void onClick();
    }

    public interface OnDismissListener
    {
        void onDismiss();
    }

    private OnNegativeButtonListener mOnNegativeButtonListener;
    private OnPositiveButtonListener mOnPositiveButtonListener;
    private OnDismissListener mOnDismissListener;

    private View.OnClickListener starClickListener = v ->
    {
        try
        {
            int idx = -1;
            switch(v.getId())
            {
                case R.id.star_1: idx = 0; break;
                case R.id.star_2: idx = 1; break;
                case R.id.star_3: idx = 2; break;
                case R.id.star_4: idx = 3; break;
                case R.id.star_5: idx = 4; break;
            }

            if(idx >= 0)
            {
                for(int i = 0; i < 5; i++)
                {
                    if(i <= idx)
                    {
                        reviewStars[i].setSelected(true);
                    }
                    else
                    {
                        reviewStars[i].setSelected(false);
                    }
                }
            }
        }
        catch(Exception e)
        {

        }
    };

    @Override
    public void create()
    {
        try
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View dialogLayout = inflater.inflate(R.layout.houston_style_popup_layout, null);

            setDialogWindowAppearance();

            Point wndSize = new Point();
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            display.getSize(wndSize);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.width = wndSize.x;
            setContentView(dialogLayout, layoutParams);

            boldTitleText = findViewById(R.id.dialog_bold_title_text);
            titleText = findViewById(R.id.dialog_title_text);
            subTitleText = findViewById(R.id.dialog_sub_title_text);
            contentText = findViewById(R.id.dialog_content_text);

            divider = findViewById(R.id.divider);

            buttonContainer = findViewById(R.id.dialog_button_container);
            negativeBtn = findViewById(R.id.dialog_negative_button);
            positiveBtn = findViewById(R.id.dialog_positive_button);
            btnDivider = findViewById(R.id.dialog_button_divider);

            reviewStarContainer = findViewById(R.id.review_stars_container);
            reviewStars = new View[5];
            reviewStars[0] = findViewById(R.id.star_1);
            reviewStars[1] = findViewById(R.id.star_2);
            reviewStars[2] = findViewById(R.id.star_3);
            reviewStars[3] = findViewById(R.id.star_4);
            reviewStars[4] = findViewById(R.id.star_5);

            for(int i = 0; i < 5; i++)
            {
                reviewStars[i].setOnClickListener(starClickListener);
            }

            Typeface basicTypeface = CommonAssets.getTypeface(mContext.getAssets());
            Typeface boldTypeface = CommonAssets.getBoldTypeface(mContext.getAssets());

            boldTitleText.setTypeface(boldTypeface);
            titleText.setTypeface(basicTypeface);
            subTitleText.setTypeface(basicTypeface);
            contentText.setTypeface(basicTypeface);

            negativeBtn.setTypeface(boldTypeface);
            positiveBtn.setTypeface(boldTypeface);
        }
        catch(Exception e)
        {

        }
    }

    @Override
    public void show()
    {
        try
        {
            super.show();

            getWindow().setDimAmount(0.8f);

            if(autoCancelable)
            {
                doAutoCancelable();
            }
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        // 자동 취소 task가 있다면 취소
        try
        {
            cancelAutoCancelTask();
        }
        catch(Exception e)
        {

        }
    }

    @Override
    public void dismiss()
    {
        super.dismiss();

        // 자동 취소 task가 있다면 취소
        try
        {
            cancelAutoCancelTask();

            if(mOnDismissListener != null)
            {
                mOnDismissListener.onDismiss();
            }
        }
        catch(Exception e)
        {

        }
    }

    private void setDialogWindowAppearance()
    {
        // 배경 없게
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 배경 알파값 설정
        setDimAmount();

        // 타이틀 없이
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    private void setDimAmount()
    {
        // 다이얼로그가 나올 때 배경 화면 어둡게
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(layoutParams);
    }

    @Override
    public void setOnCancelListener(@Nullable DialogInterface.OnCancelListener listener)
    {
        super.setOnCancelListener(listener);
    }

    public void setBoldTitleText(String text)
    {
        boldTitleText.setText(text);
        boldTitleText.setVisibility((text != null && !text.trim().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public void setTitleText(String text)
    {
        titleText.setText(text);
        titleText.setVisibility((text != null && !text.trim().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public void setSubTitleText(String text)
    {
        subTitleText.setText(text);
        subTitleText.setVisibility((text != null && !text.trim().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public void setContentText(String text)
    {
        contentText.setText(text);
        contentText.setVisibility((text != null && !text.trim().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public void setNegativeButton(String text, OnNegativeButtonListener onNegativeButtonListener)
    {
        boolean valid = (text != null && !text.trim().isEmpty());
        if(valid)
        {
            negativeBtn.setText(text);
            negativeBtn.setVisibility(View.VISIBLE);
            mOnNegativeButtonListener = onNegativeButtonListener;

            negativeBtn.setOnClickListener(v ->
            {
                try
                {
                    dismiss();

                    if(mOnNegativeButtonListener != null)
                    {
                        mOnNegativeButtonListener.onClick();
                    }
                }
                catch(Exception e)
                {

                }
            });
        }
        else
        {
            negativeBtn.setVisibility(View.GONE);
        }
    }

    public void setPositiveButton(String text, OnPositiveButtonListener onPositiveButtonListener)
    {
        boolean valid = (text != null && !text.trim().isEmpty());
        if(valid)
        {
            positiveBtn.setText(text);
            positiveBtn.setVisibility(View.VISIBLE);
            mOnPositiveButtonListener = onPositiveButtonListener;

            positiveBtn.setOnClickListener(v ->
            {
                try
                {
                    dismiss();

                    if(mOnPositiveButtonListener != null)
                    {
                        mOnPositiveButtonListener.onClick();
                    }
                }
                catch(Exception e)
                {

                }
            });
        }
        else
        {
            positiveBtn.setVisibility(View.GONE);
        }
    }

    public void setReviewStarLayout(boolean show)
    {
        reviewStarContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public int getReviewRate()
    {
        int rate = 5;

        for(int i = 0; i < 5; i++)
        {
            if(!reviewStars[i].isSelected())
            {
                return i;
            }
        }

        return rate;
    }

    public void setCustomOnDismissListener(OnDismissListener mOnDismissListener)
    {
        this.mOnDismissListener = mOnDismissListener;
    }

    public void setNegativeButtonResource(int resId)
    {
        negativeBtn.setBackgroundResource(resId);
    }

    public void setPositiveButtonResource(int resId)
    {
        positiveBtn.setBackgroundResource(resId);
    }

    public void setDivider(boolean show)
    {
        divider.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void setButtonDivider(boolean show)
    {
        btnDivider.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setButtonContainer(boolean show)
    {
        buttonContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 3초 후 자동으로 취소
     * @param autoCancelable
     */
    public void setAutoCancelable(boolean autoCancelable)
    {
       this.autoCancelable = autoCancelable;
    }

    public void doAutoCancelable()
    {
        cancelAutoCancelTask();

        handler = new Handler();
        handler.postDelayed(cancelDialogRunnable, 3000);
    }

    private Runnable cancelDialogRunnable = () ->
    {
        try
        {
            ((Activity) mContext).runOnUiThread(() ->
            {
                try
                {
                    dismiss();
                }
                catch(Exception e)
                {
                    Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    public void cancelAutoCancelTask()
    {
        if(handler != null)
        {
            handler.removeCallbacks(cancelDialogRunnable);
            handler = null;
        }
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }
}
