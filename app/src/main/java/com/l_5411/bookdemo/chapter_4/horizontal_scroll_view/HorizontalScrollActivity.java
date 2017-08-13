package com.l_5411.bookdemo.chapter_4.horizontal_scroll_view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.Utils.ScreenUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HorizontalScrollActivity extends AppCompatActivity {

    private static final String TAG = HorizontalScrollActivity.class.getSimpleName();
    private Context mContext;

    @BindView(R.id.container)
    public HorizontalScrollViewEx mListContainer;

    public static Intent newIntent(Context context) {
        return new Intent(context, HorizontalScrollActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_scroll);
        ButterKnife.bind(this);
        mContext = this;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        final int screenWidth = ScreenUtils.getScreenMetrics(mContext).widthPixels;
        final int screenHeight = ScreenUtils.getScreenMetrics(mContext).heightPixels;
        for (int i = 0 ; i < 3; i++) {
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.content_layout, mListContainer, false);
            layout.getLayoutParams().width = screenWidth;
            TextView textView = (TextView) layout.findViewById(R.id.title);
            textView.setText("page " + (i + 1));
            layout.setBackgroundColor(Color.rgb(255 / (i + 1), 255 / (i + 1), 0));
            createList(layout);
            mListContainer.addView(layout);
        }
    }

    private void createList(ViewGroup layout) {
        ListView listView = (ListView) layout.findViewById(R.id.list);
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            data.add("name " + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                data);
        listView.setAdapter(adapter);
    }
}
