package com.choice.nearlocation;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

public class SeekActivity extends AppCompatActivity {
    private TextView tvBack;
    private EditText etSeek;
    private RecyclerView mRecyclerView;
    private InputMethodManager imm;

    private String seekText;
    private String city;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();

    }

    private void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_seek);
        tvBack= (TextView) findViewById(R.id.tv_back);
        etSeek= (EditText) findViewById(R.id.et_seek);
        mRecyclerView= (RecyclerView) findViewById(R.id.recy_seek);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        city= getIntent().getExtras().getString("city");
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 200);
        etSeek.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                seekText=s.toString();
                doSearchQuery(seekText,city);
            }
        });
    }

    private void doSearchQuery(String key, String city) {
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        PoiSearch.Query mPoiQuery;// Poi查询条件类
        PoiSearch mPoiSearch;
        mPoiQuery = new PoiSearch.Query(key, "", city);
        mPoiSearch = new PoiSearch(this, mPoiQuery);
        mPoiQuery.setPageSize(50);// 设置每页最多返回多少条poiitem
        mPoiQuery.setPageNum(0);//设置查第一页
        mPoiSearch.setOnPoiSearchListener(new MyOnPoiSearchListener());
        mPoiSearch.searchPOIAsyn();//开始搜索
    }
    private class MyOnPoiSearchListener implements PoiSearch.OnPoiSearchListener {

        @Override
        public void onPoiSearched(PoiResult poiResult, int rCode) {
            SeekAdapter adapter=new SeekAdapter(SeekActivity.this,poiResult.getPois());
            mRecyclerView.setAdapter(adapter);
//            tvClear.setVisibility(View.GONE);
//            poiItems.clear();
//            List<PoiItem> list = poiResult.getPois();
//            for (int i=0;i<list.size();i++){
//                poiItems.add(list.get(i));
//            }
//            if (list.size()<1){
//
//            }
//            myAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }
    }
}
