package com.ljg.ganggangweather.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ljg.ganggangweather.bean.CityDetailBean;
import com.ljg.ganggangweather.bean.WeatherBean;
import com.ljg.ganggangweather.db.CityDatabaseHelper;
import com.ljg.ganggangweather.txweather.TxWeatherHelper;
import com.ljg.ganggangweather.util.AsyncHttpRequestListener;
import com.ljg.ganggangweather.util.DateHelper;
import com.ljg.ganggangweather.util.HttpLiteBusHelper;
import com.ljg.ganggangweather.view.adapter.HourAdapter;
import com.ljg.ganggangweather.view.adapter.IndexAdapter;
import com.ljg.ganggangweather.R;
import com.ljg.ganggangweather.view.adapter.DayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {
    private static final String TAG="CityFragment" ;
    private TextView tv_thum_day,tv_thum_desc,tv_thum_temp,tv_current_temp,tv_current_state,tv_current_wind,tv_current_tips,tv_header_date
    ,tv_more_humi,tv_more_press,tv_more_air_index,tv_more_air_quality;
    private ImageView iv_thum_weather_icon,iv_header_more;
    private LinearLayout ll_header_show_more;
    private RecyclerView rv_hour_list;
    private RecyclerView rv_day_list;
    private RecyclerView rv_index_list;
    private HourAdapter hourAdapter;
    private DayAdapter dayAdapter;
    private IndexAdapter indexAdapter;
    private List<WeatherBean.ForecastHour> forecastHourList=new ArrayList<>(48);
    private List<WeatherBean.ForecastDay> forecastDayList=new ArrayList<>(15);
    private List<WeatherBean.Index> indexList=new ArrayList<>(9);
    private boolean showMore=true;

    private CityDatabaseHelper dbHelper;

    public CityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_city, container, false);
        initView(view);
        refreshView();
        return view;
    }

    private void initView(View view){
        dbHelper=new CityDatabaseHelper(getActivity());

        tv_header_date=view.findViewById(R.id.tv_header_date);

        tv_current_temp=view.findViewById(R.id.tv_current_temp);
        tv_current_state=view.findViewById(R.id.tv_current_state);
        tv_current_wind=view.findViewById(R.id.tv_current_wind);
        tv_current_tips=view.findViewById(R.id.tv_current_tips);


        tv_thum_day=view.findViewById(R.id.tv_thum_day);
        tv_thum_desc=view.findViewById(R.id.tv_thum_desc);
        tv_thum_temp=view.findViewById(R.id.tv_thum_temp);
        iv_thum_weather_icon=view.findViewById(R.id.iv_thum_weather_icon);

        ll_header_show_more=view.findViewById(R.id.ll_header_show_more);
        iv_header_more=view.findViewById(R.id.iv_header_more);
        tv_more_humi=view.findViewById(R.id.tv_more_humi);
        tv_more_press=view.findViewById(R.id.tv_more_press);
        tv_more_air_index=view.findViewById(R.id.tv_more_air_index);
        tv_more_air_quality=view.findViewById(R.id.tv_more_air_quality);

        iv_header_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showMore){
                    Glide.with(getContext()).load(R.drawable.ic_keyboard_arrow_up_black_24dp).into(iv_header_more);
                    ll_header_show_more.setVisibility(View.VISIBLE);
                }else {
                    Glide.with(getContext()).load(R.drawable.ic_keyboard_arrow_down_black_24dp).into(iv_header_more);
                    ll_header_show_more.setVisibility(View.GONE);
                }
                showMore=!showMore;
            }
        });



        rv_hour_list=view.findViewById(R.id.rv_hour_list);
        rv_day_list=view.findViewById(R.id.rv_day_list);
        rv_index_list=view.findViewById(R.id.rv_index_list);

        LinearLayoutManager hourManager=new LinearLayoutManager(view.getContext());
        hourManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_hour_list.setLayoutManager(hourManager);
        hourAdapter=new HourAdapter(forecastHourList);
        rv_hour_list.setAdapter(hourAdapter);

        LinearLayoutManager dayManager=new LinearLayoutManager(view.getContext());
        dayManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_day_list.setLayoutManager(dayManager);
        dayAdapter=new DayAdapter(forecastDayList);
        rv_day_list.setAdapter(dayAdapter);

        GridLayoutManager indexManager=new GridLayoutManager(view.getContext(),4);
        rv_index_list.setLayoutManager(indexManager);
        indexAdapter=new IndexAdapter(indexList);
        rv_index_list.setAdapter(indexAdapter);
    }

    private int retryCount=0;
    private void refreshView(){
        Bundle bundle=getArguments();
        //???????????????????????????????????????????????????
        String updateLink=bundle.getString("updateLink");
        CityDetailBean cityDetailBean=(CityDetailBean)bundle.getSerializable("cityDetailBean");
        HttpLiteBusHelper helper=new HttpLiteBusHelper(new AsyncHttpRequestListener() {
            @Override
            public void onSuccess(String result) {
                //??????JSON ??????UI
                WeatherBean weatherBean= TxWeatherHelper.parseWeatherData(result);

                //48????????????????????????????????????
                if(forecastHourList.isEmpty()==false){
                    forecastHourList.clear();
                }
                Map<String, WeatherBean.ForecastHour> map=weatherBean.getData().getForecastHour();
                for(String key:map.keySet()){
                    forecastHourList.add(map.get(key));
                }
                Collections.sort(forecastHourList, (o1, o2) -> o1.getUpdateTime().compareTo(o2.getUpdateTime()));
                hourAdapter.notifyDataSetChanged();
                Log.i("Fragment??????","48??????");
                //7?????????????????????
                if(forecastDayList.size()!=0){
                    forecastDayList.clear();
                }
                Map<String, WeatherBean.ForecastDay> mmp=weatherBean.getData().getForecastDay();
                for(String key:mmp.keySet()){
                    forecastDayList.add(Integer.valueOf(key),mmp.get(key));
                }
                Collections.sort(forecastDayList, (o1, o2) -> o1.getTime().compareTo(o2.getTime()));
                dayAdapter.notifyDataSetChanged();
                Log.i("Fragment??????","????????????");
                //??????????????????
                if(indexList.size()!=0){
                    indexList.clear();
                }
                WeatherBean.Indexs indexs=weatherBean.getData().getIndex();
                indexList.add(indexs.getAirconditioner());
                indexList.add(indexs.getCarwash());
                indexList.add(indexs.getClothes());
                indexList.add(indexs.getDrying());
                indexList.add(indexs.getSports());
                indexList.add(indexs.getTourism());
                indexList.add(indexs.getUltraviolet());
                indexAdapter.notifyDataSetChanged();
                Log.i("Fragment??????","????????????");

                //??????????????????
                tv_thum_desc.setText(forecastDayList.get(1).getDayWeather());
                tv_thum_temp.setText(forecastDayList.get(1).getMaxDegree()+"???/"+forecastDayList.get(1).getMinDegree()+"???");
                String baseIconUrl=TxWeatherHelper.getWeatherStateIcon(forecastDayList.get(1).getDayWeatherCode(),true);
                if(getContext()!=null){//??????fragment?????????Glide??????????????????
                    Glide.with(getContext()).load(baseIconUrl).override(50).into(iv_thum_weather_icon);
                }
                Log.i("Fragment??????","????????????");

                //header????????????
                WeatherBean.Observe observe=weatherBean.getData().getObserve();
                tv_current_temp.setText(observe.getDegree()+"???");
                tv_current_state.setText(observe.getWeather());
                tv_current_tips.setText(weatherBean.getData().getTips().get("observe").getTip0()+"\n"+weatherBean.getData().getTips().get("observe").getTip1());
                tv_current_wind.setText(observe.getWindDirection()+"/"+observe.getWindPower()+"???");
                String time=forecastDayList.get(1).getTime();
                tv_header_date.setText(time.substring(5)+" "+ DateHelper.convertDateToWeek(time));
                Log.i("Fragment??????","header??????");

                //??????????????????
                tv_more_press.setText("???????????????"+observe.getPressure());
                tv_more_humi.setText("???????????????"+observe.getHumidity());
                WeatherBean.Air air=weatherBean.getData().getAir();
                tv_more_air_index.setText("???????????????"+air.getAqi());
                tv_more_air_quality.setText("???????????????"+air.getAqiName());
                Log.i("?????????",air.toString());

                ll_header_show_more.setVisibility(View.VISIBLE);

                //?????????????????????
                dbHelper.open();
                dbHelper.updateCity(cityDetailBean);
                dbHelper.close();
                Log.i("Fragment??????","???????????????");

            }
            @Override
            public void onFailed(String reason) {
                Log.i(TAG+"onFailed()\t",reason);
                if(retryCount<2){
                    retryCount++;
                    refreshView();
                }
            }
        });
        helper.doAsyncRequest(updateLink);

    }
}
