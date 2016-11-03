package com.zdc.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdc.view.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.FaceGvAdapter;
import adapter.FaceVpAdapter;

/**
 * 描述：自定义含有Enoji表情的文本输入面板(给发送按钮再添加一个回调接监听器)
 * Created by zhaodecang on 2016-11-03 0003 18:40
 * 邮箱：zhaodecang@gmail.com
 */

public class EmojiLayout extends FrameLayout implements View.OnClickListener {
    private int columns = 6;
    public EditText etReplay;
    public ImageButton ibEmoji;
    public ImageButton ibSend;
    public ImageView ivDelete;
    public LinearLayout llChatEmoji;
    public LinearLayout llEmojiContainer;
    private int rows = 3;
    private ArrayList<String> staticFacesList;
    public TextView tvEmojiFace;
    public TextView tvQqFace;
    private ArrayList<View> views = new ArrayList();
    public ViewPager vpEmoji;

    public EmojiLayout(Context paramContext) {
        super(paramContext);
        initView();
    }

    public EmojiLayout(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.emoji_edittext_layout, null);
        initUI(view);
        addView(view);
        initEmoji();
        initListener();
    }

    private void initEmoji() {
        initStaticFaces();
        InitViewPager();
    }

    private void initUI(View paramView) {
        etReplay = ((EditText) paramView.findViewById(R.id.ed_replay));
        ibEmoji = ((ImageButton) paramView.findViewById(R.id.ib_emoji));
        ibSend = ((ImageButton) paramView.findViewById(R.id.ib_send));
        vpEmoji = ((ViewPager) paramView.findViewById(R.id.emoji_viewpager));
        tvQqFace = ((TextView) paramView.findViewById(R.id.tv_qq_face));
        tvEmojiFace = ((TextView) paramView.findViewById(R.id.tv_emoji_face));
        llEmojiContainer = ((LinearLayout) paramView.findViewById(R.id.emoji_controller));
        ivDelete = ((ImageView) paramView.findViewById(R.id.iv_delete));
        llChatEmoji = ((LinearLayout) paramView.findViewById(R.id.ll_chat_emoji));
    }

    private void initListener() {
        ibEmoji.setOnClickListener(this);
        ibSend.setOnClickListener(this);
        tvQqFace.setOnClickListener(this);
        tvEmojiFace.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
    }

    //初始化表情列表
    private void initStaticFaces() {
        try {
            staticFacesList = new ArrayList<>();
            String[] faces = getContext().getAssets().list("face/png");
            // 将Assets中的表情名称转为字符串一一添加进staticFacesList
            for (int i = 0; i < faces.length; i++) {
                staticFacesList.add(faces[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void InitViewPager() {
        for (int i = 0; i < getPagerCount(); i++)
            this.views.add(viewPagerItem(i));
        FaceVpAdapter localFaceVpAdapter = new FaceVpAdapter(this.views);
        this.vpEmoji.setAdapter(localFaceVpAdapter);
    }

    /**
     * 根据表情数量以及GridView设置的行数和列数计算Pager数量
     */
    private int getPagerCount() {
        int count = staticFacesList.size();
        int pageCount = columns * rows;
        return count % pageCount == 0 ? count / pageCount : count / pageCount + 1;
    }

    private View viewPagerItem(int position) {
        View layout = View.inflate(getContext(), R.layout.face_gridview, null);// 表情布局
        GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
        List<String> subList = new ArrayList<>();
        int row = position * (columns * rows);
        int column = (columns * rows) * (position + 1) > staticFacesList.size() ? staticFacesList.size() : (columns * rows) * (position + 1);
        subList.addAll(staticFacesList.subList(row, column));
        FaceGvAdapter mGvAdapter = new FaceGvAdapter(subList, getContext());
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(columns);
        // 单击表情执行的操作
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String png = ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString();
                insert(getFace(png));
            }
        });
        return gridview;
    }

    private SpannableStringBuilder getFace(String png) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        try {
            String tempText = "#[" + png + "]#";
            sb.append(tempText);
            Drawable drawable = Drawable.createFromStream(getContext().getAssets().open(png), png);
            drawable.setBounds(0, 0, 25, 25);
            ImageSpan imageSpan = new ImageSpan(drawable);
            sb.setSpan(imageSpan, sb.length() - tempText.length(), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_emoji:
                showOrHideEmoji();
                break;
            case R.id.ib_send:
                // TODO
                break;
            case R.id.tv_qq_face:
                vpEmoji.setCurrentItem(0);
                break;
            case R.id.tv_emoji_face:
                vpEmoji.setCurrentItem(vpEmoji.getAdapter().getCount() / 2);
                break;
            case R.id.iv_delete:
                delete();
                break;
        }
    }

    private void insert(CharSequence text) {
        int iCursorStart = Selection.getSelectionStart((etReplay.getText()));
        int iCursorEnd = Selection.getSelectionEnd((etReplay.getText()));
        if (iCursorStart != iCursorEnd) {
            etReplay.getText().replace(iCursorStart, iCursorEnd, "");
        }
        int iCursor = Selection.getSelectionEnd((etReplay.getText()));
        etReplay.getText().insert(iCursor, text);
    }

    private void delete() {
        if (etReplay.getText().length() != 0) {
            int iCursorEnd = Selection.getSelectionEnd(etReplay.getText());
            int iCursorStart = Selection.getSelectionStart(etReplay.getText());
            if (iCursorEnd > 0) {
                if (iCursorEnd == iCursorStart) {
                    if (isDeletePng(iCursorEnd)) {
                        String st = "#[face/png/f_static_000.png]#";
                        etReplay.getText().delete(iCursorEnd - st.length(), iCursorEnd);
                    } else {
                        etReplay.getText().delete(iCursorEnd - 1, iCursorEnd);
                    }
                } else {
                    etReplay.getText().delete(iCursorStart, iCursorEnd);
                }
            }
        }
    }

    private boolean isDeletePng(int cursor) {
        String st = "#[face/png/f_static_000.png]#";
        String content = etReplay.getText().toString().substring(0, cursor);
        if (content.length() >= st.length()) {
            String checkStr = content.substring(content.length() - st.length(), content.length());
            String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(checkStr);
            return m.matches();
        }
        return false;
    }

    private void showOrHideEmoji() {
        if (llChatEmoji.getVisibility() == View.GONE)
            llChatEmoji.setVisibility(View.VISIBLE);
        else
            llChatEmoji.setVisibility(View.GONE);
    }
}