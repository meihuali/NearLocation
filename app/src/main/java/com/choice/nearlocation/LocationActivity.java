package com.choice.nearlocation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView;
    private TextView tvOk;
    private TextView tvBack;
    private TextView tvSeek;
    private ImageView imgNow;
    private TextView tvLoad;
    private RecyclerView mRecyclerView;

    private float zoom = 17f;

    private locationBean bean = new locationBean();

    private LocationSource.OnLocationChangedListener mListener;
    private AMap mMap;
    private AMapLocationClient mlocationClient;
    private LatLonPoint mCurrentPoint;
    private AMapLocation mAmapLocation;
    private PoiItem backPoiItem;
    private boolean first = true;
    private boolean refresh=true;
    private boolean seekResult=true;
    private static HashMap<String, Boolean> hashMap = new HashMap<>();
    private List<PoiItem> poiItems=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mlocationClient != null && mlocationClient.isStarted()) {
            mlocationClient.stopLocation();
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void initView() {
        setContentView(R.layout.activity_location);
        mMapView = (MapView) findViewById(R.id.map_location);
        tvOk = (TextView) findViewById(R.id.tv_ok);
        tvBack = (TextView) findViewById(R.id.tv_back);
        tvSeek = (TextView) findViewById(R.id.tv_seek);
        imgNow = (ImageView) findViewById(R.id.img_now);
        tvLoad = (TextView) findViewById(R.id.tv_load);
        mRecyclerView = (RecyclerView) findViewById(R.id.recy_location);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void initData(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        tvOk.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        tvSeek.setOnClickListener(this);
        imgNow.setOnClickListener(this);
        setupLocationStyle();
    }

    private void setupLocationStyle() {
        if (mMap == null) {
            mMap = mMapView.getMap();
        }
        // 自定义定位蓝点图标
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        myLocationStyle.anchor(0.5f, 0.5f);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        // 将自定义的 myLocationStyle 对象添加到地图上
        mMap.setMyLocationStyle(myLocationStyle);
        //定位监听
        mMap.setLocationSource(new MyLocationSource());
        // 设置默认定位按钮是否显示
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        // 隐藏缩放按钮
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        setMarker();
    }

    /**
     * Marker
     */
    private Marker mCenterMarker;
    private GeocodeSearch mGeocoderSearch;

    private void setMarker() {
        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.draggable(true);//可拖放性
        mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_pin));
        mCenterMarker = mMap.addMarker(mMarkerOptions);
        ViewTreeObserver vto = mMapView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new MyGlobalLayoutListener());
        mGeocoderSearch = new GeocodeSearch(this);
        mGeocoderSearch.setOnGeocodeSearchListener(new MyGeocodeSearchListener());
    }

    /**
     * location
     */
    private void startlocation() {
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mlocationClient.setLocationListener(new MyAMapLocationListener());
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位请求超时时间,单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(5000);
            //获取最近3s内精度最高的一次定位结果：
            // 设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            //启动定位
            mlocationClient.startLocation();
        }
    }


    /**
     * 反编码POI
     */

    private void doSearchQuery(LatLonPoint latLonPoint) {
        mCurrentPoint = new LatLonPoint(latLonPoint.getLatitude(), latLonPoint.getLongitude());
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLonPoint.getLatitude(), latLonPoint.getLongitude()), 200, GeocodeSearch.AMAP);
        mGeocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                thisFinish();
                break;
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_seek:
                toSeek();
                break;
            case R.id.img_now:
                if (mListener != null && mAmapLocation != null) {
                    if (mAmapLocation.getErrorCode() == 0) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mAmapLocation.getLatitude(), mAmapLocation.getLongitude()), zoom));
                    }
                }
                break;
        }
    }

    private void toSeek() {
        Intent intent = new Intent(LocationActivity.this, SeekActivity.class);
        intent.putExtra("city",mAmapLocation.getCity());
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode==1001&&resultCode==1002){
            PoiItem mPoiItem= intent.getParcelableExtra("PoiItem");
            refresh = true;
            seekResult=false;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mPoiItem.getLatLonPoint().getLatitude(), mPoiItem.getLatLonPoint().getLongitude()), zoom));
        }
    }


    public void itemOnclick(PoiItem poiItem,boolean first) {
        this.first=first;
        this.backPoiItem = poiItem;
        refresh=false;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude()), zoom));

    }

    private void thisFinish(){
        if (backPoiItem == null) {
            if (poiItems.size()<1){
                Toast.makeText(this,"请等待数据加载完成。",Toast.LENGTH_SHORT).show();
                return;
            }
            backPoiItem = poiItems.get(0);
        }
        Intent intent = new Intent();
        intent.putExtra("PoiItem", backPoiItem);
        setResult(1002, intent);
        finish();
    }

    public boolean isFirst() {
        return first;
    }

    private class MyLocationSource implements LocationSource {

        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            LocationActivity.this.mListener = onLocationChangedListener;
            startlocation();
        }

        @Override
        public void deactivate() {
            mListener = null;
            if (mlocationClient != null) {
                mlocationClient.stopLocation();
                mlocationClient.onDestroy();
            }
            mlocationClient = null;
        }
    }

    private class MyGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            mMapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            mCenterMarker.setPositionByPixels(mMapView.getWidth() >> 1, mMapView.getHeight() >> 1);
            mCenterMarker.showInfoWindow();
        }
    }

    private class MyGeocodeSearchListener implements GeocodeSearch.OnGeocodeSearchListener {

        @Override
        public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
            if (rCode == 1000) {

                if (result != null && result.getRegeocodeAddress() != null &&
                        result.getRegeocodeAddress().getFormatAddress() != null) {
                    setAddress(result.getRegeocodeAddress());
                    String mType = "地名地址信息|餐饮服务|购物服务|生活服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|公共设施";
                    // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
                    PoiSearch.Query query = new PoiSearch.Query("", mType, result.getRegeocodeAddress().getCityCode());
                    query.setPageSize(100);// 设置每页最多返回多少条poiitem
                    query.setPageNum(0);//设置第几页
                    PoiSearch poiSearch = new PoiSearch(LocationActivity.this, query);
                    poiSearch.setOnPoiSearchListener(new MyOnPoiSearchListener());//设置数据返回的监听器
                    poiSearch.setBound(new PoiSearch.SearchBound(mCurrentPoint, 1000, true));//
                    poiSearch.searchPOIAsyn();
                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        }

        private void setAddress(RegeocodeAddress address) {
            String s = address.getFormatAddress();
            if (s.trim().length() > 0) {
                if ((address.getProvince() != null) && (s.indexOf(address.getProvince()) != -1)) {
                    s = s.replaceAll(address.getProvince(), "");
                    if ((address.getCity() != null) && (s.indexOf(address.getCity()) != -1)) {
                        s = s.replaceAll(address.getCity(), "");
                    }
                }
            } else {
                s = "未知位置，请重新定位";
            }

            bean.setTitle(s);
        }
    }

    private class MyOnPoiSearchListener implements PoiSearch.OnPoiSearchListener {

        @Override
        public void onPoiSearched(PoiResult result, int rCode) {
            if (rCode == 1000) {
                if (result != null && result.getQuery() != null) {// 搜索poi的结果
                   poiItems = result.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    if (seekResult){
                    poiItems.add(0, new PoiItem("", new LatLonPoint(bean.getLatitude(), bean.getLongitude()), bean.getTitle(), ""));
                    }
                    if (!seekResult){
                        seekResult=true;
                    }
                  tvLoad.setVisibility(View.GONE);
               //     StyledDialog.buildLoading("加载中···").show();
                    LocationAdapter adapter = new LocationAdapter(LocationActivity.this, poiItems, hashMap);
                    mRecyclerView.setAdapter(adapter);
                }
            }
        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }
    }

    private class MyAMapLocationListener implements AMapLocationListener {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                    //手动移动地图监听
                    mMap.setOnCameraChangeListener(new MyCameraChangeListener());
                    mlocationClient.stopLocation();
                    mAmapLocation = aMapLocation;
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    }

    private class MyCameraChangeListener implements AMap.OnCameraChangeListener {

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

        }

        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {

            if (0 != LocationActivity.this.bean.getLatitude()) {
                float distance = AMapUtils.calculateLineDistance(cameraPosition.target, new LatLng(bean.getLatitude(), bean.getLongitude()));
                if (distance < 10) {
                    return;
                }
            }
            bean.setLatitude(cameraPosition.target.latitude);
            bean.setLongitude(cameraPosition.target.longitude);
            if (refresh) {
//                LocationSeekActivity.this.adapterStatus = true;
            first = true;
            hashMap.clear();
              tvLoad.setVisibility(View.VISIBLE);
            doSearchQuery(new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude));
            } else {
                refresh = true;
            }
        }
    }
}
