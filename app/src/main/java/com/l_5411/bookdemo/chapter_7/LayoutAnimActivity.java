package com.l_5411.bookdemo.chapter_7;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.l_5411.bookdemo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LayoutAnimActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, LayoutAnimActivity.class);
    }

    @BindView(R.id.list)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_anim);
        ButterKnife.bind(this);

        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_expandable_list_item_1,
                getList(100)
                ));
    }

    private List<Integer> getList(int max) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            list.add(i);
        }
        return list;
    }

    private LayoutAnimationController getLayoutAnimationController() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_item);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setDelay(0.5f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }
}
