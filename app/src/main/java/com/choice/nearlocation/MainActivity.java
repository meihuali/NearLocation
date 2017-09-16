package com.choice.nearlocation;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.amap.api.services.core.PoiItem;


public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView= (TextView) findViewById(R.id.tv_main_text);
        mButton = (Button) findViewById(R.id.btn_jump);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpActivity();
            }
        });
    }

    public void jumpActivity() {
        Intent intent = new Intent(MainActivity.this, LocationActivity.class);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode==1001&&resultCode==1002){
            PoiItem mPoiItem= intent.getParcelableExtra("PoiItem");
            String title="位置:"+mPoiItem.getTitle();
            String latLonPoint="经纬度:"+mPoiItem.getLatLonPoint();
            mTextView.setText(title+"\n"+latLonPoint);
        }
    }

}


