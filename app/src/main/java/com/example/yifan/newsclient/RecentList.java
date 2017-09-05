package com.example.yifan.newsclient;

/**
 * Created by Yifan on 2017/9/5.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yifan.newsclient.HttpGet.sendGet;

public class RecentList extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recentlist);

        String str = sendGet("http://166.111.68.66:2042/news/action/query/latest");
        TextView tv = (TextView)findViewById(R.id.textView);

        //根据字符串生成JSON对象
        try {
            JSONObject resultObj = new JSONObject(str);
            //获取数据项
            String article_list = resultObj.getString("list");
            ListView lv = (ListView)findViewById(R.id.listView);
            final JSONArray objectList = new JSONArray(article_list);
            int length = objectList.length();
            String artList = "";
            String[] strs = new String [length];

            for (int i = 0; i < length; i++) {
                JSONObject o = objectList.getJSONObject(i);
                strs[i] = o.getString("news_Title");
                artList = artList + o.getString("news_Title") + '\n';
            }

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                       long arg3) {
                    //点击后在标题上显示点击了第几行
                    Intent intent = new Intent(RecentList.this, Article.class);
                    try {
                        intent.putExtra("news_id", objectList.getJSONObject(arg2).getString("news_ID"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });

            lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs));
            tv.setText(artList);
        } catch (JSONException e) {
            tv.setText(e.toString());
        }
    }
}
