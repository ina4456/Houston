package com.insoline.hanam.view;

import android.content.Context;

import com.insoline.hanam.R;

public class HoustonStyleDialogBuilder
{
    private Context context;

    private String boldTitleText;
    private String titleText;
    private String subTitleText;
    private String contentText;

    private String negativeBtnText;
    private String positiveBtnText;
    private HoustonStyleDialog.OnNegativeButtonListener onNegativeButtonListener;
    private HoustonStyleDialog.OnPositiveButtonListener onPositiveButtonListener;
    private HoustonStyleDialog.OnDismissListener onDismissListener;

    private boolean showReviewStars;

    private boolean cancelable = true;

    private boolean autoCancelable;

    public HoustonStyleDialogBuilder(Context context)
    {
        this.context = context;
    }

    /**
     * 대화상자의 타이틀 텍스트(볼드체)를 설정한다.
     * @param text
     * @return
     */
    public HoustonStyleDialogBuilder setBoldTitle(String text)
    {
        boldTitleText = text;
        return this;
    }

    /**
     * 대화상자의 타이틀 텍스트를 설정한다.
     * @param text
     * @return
     */
    public HoustonStyleDialogBuilder setTitle(String text)
    {
        titleText = text;
        return this;
    }

    /**
     * 대화상자의 서브 타이틀 텍스트(타이틀 아래 약간의 간격을 두고 표현)를 설정한다.
     * @param text
     * @return
     */
    public HoustonStyleDialogBuilder setSubTitle(String text)
    {
        subTitleText = text;
        return this;
    }

    /**
     * 대화상자의 상세 설명 텍스트를 설정한다(작은 회색 글씨).
     * @param text
     * @return
     */
    public HoustonStyleDialogBuilder setContent(String text)
    {
        contentText = text;
        return this;
    }

    /**
     * 왼쪽(부정의 의미) 버튼 설정
     * @param text          버튼 텍스트
     * @param listener      버튼 클릭시 동작(대화상자 감추기는 기본 동작으로 포함됨)
     * @return
     */
    public HoustonStyleDialogBuilder setNegativeButton(String text, HoustonStyleDialog.OnNegativeButtonListener listener)
    {
        negativeBtnText = text;
        onNegativeButtonListener = listener;
        return this;
    }

    /**
     * 오른쪽(긍정의 의미) 버튼 설정
     * @param text          버튼 텍스트
     * @param listener      버튼 클릭시 동작(대화상자 감추기는 기본 동작으로 포함됨)
     * @return
     */
    public HoustonStyleDialogBuilder setPositiveButton(String text, HoustonStyleDialog.OnPositiveButtonListener listener)
    {
        positiveBtnText = text;
        onPositiveButtonListener = listener;
        return this;
    }

    /**
     * 팝업 종료시 이벤트 처리
     * @param listener
     * @return
     */
    public HoustonStyleDialogBuilder setDismissListener(HoustonStyleDialog.OnDismissListener listener)
    {
        onDismissListener = listener;
        return this;
    }

    /**
     * 대화상자 자동 감춤 설정
     * @param autoCancelable    true이면 3초 후에 자동으로 사라짐
     * @return
     */
    public HoustonStyleDialogBuilder setAutoCancelable(boolean autoCancelable)
    {
        this.autoCancelable = autoCancelable;
        return this;
    }

    /**
     * 대화상자 취소 가능(백버튼이나 배경 터치시 사라짐)
     * @param cancelable
     * @return
     */
    public HoustonStyleDialogBuilder setCancelable(boolean cancelable)
    {
        this.cancelable = cancelable;
        return this;
    }

    /**
     * 리뷰 평가용 별 노출 여부
     * @param show
     * @return
     */
    public HoustonStyleDialogBuilder showReviewStars(boolean show)
    {
        this.showReviewStars = show;
        return this;
    }

    /**
     * 대화상자를 만든다.
     * @return
     */
    public HoustonStyleDialog build()
    {
        // make a dialog
        HoustonStyleDialog dialog = new HoustonStyleDialog(context);
        dialog.create();

        setDialogLayout(dialog);

        return dialog;
    }

    private void setDialogLayout(HoustonStyleDialog dialog)
    {
        dialog.setBoldTitleText(boldTitleText);
        dialog.setTitleText(titleText);
        dialog.setSubTitleText(subTitleText);
        dialog.setContentText(contentText);

        boolean hasNegativeBtn = (negativeBtnText != null && !negativeBtnText.trim().isEmpty());
        boolean hasPositiveBtn = (positiveBtnText != null && !positiveBtnText.trim().isEmpty());

        if(hasPositiveBtn && hasNegativeBtn)
        {
            // 양쪽 버튼 모두 보임

            dialog.setNegativeButtonResource(R.drawable.dialog_button_left);
            dialog.setPositiveButtonResource(R.drawable.dialog_button_right);

            dialog.setButtonContainer(true);
            dialog.setDivider(true);
            dialog.setButtonDivider(true);
        }
        else if(!hasNegativeBtn && !hasPositiveBtn)
        {
            // 버튼 없음

            dialog.setButtonContainer(false);
            dialog.setDivider(false);
            dialog.setButtonDivider(false);
        }
        else
        {
            // 둘 중 하나의 버튼만 있음

            if(hasNegativeBtn)
            {
                dialog.setNegativeButtonResource(R.drawable.dialog_button_bottom);
            }
            else
            {
                dialog.setPositiveButtonResource(R.drawable.dialog_button_bottom);
            }

            dialog.setButtonContainer(true);
            dialog.setDivider(true);
            dialog.setButtonDivider(false);
        }

        dialog.setNegativeButton(negativeBtnText, onNegativeButtonListener);
        dialog.setPositiveButton(positiveBtnText, onPositiveButtonListener);
        dialog.setCustomOnDismissListener(onDismissListener);

        dialog.setAutoCancelable(autoCancelable);
        dialog.setCancelable(cancelable);

        dialog.setReviewStarLayout(showReviewStars);

        // all value init
        boldTitleText = null;
        titleText = null;
        subTitleText = null;
        contentText = null;
        negativeBtnText = null;
        onNegativeButtonListener = null;
        positiveBtnText = null;
        onPositiveButtonListener = null;
        onDismissListener = null;
        cancelable = true;
        autoCancelable = false;
        showReviewStars = false;
    }
}
