package com.example.shunki.judomovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Favorite extends AppCompatActivity {
    //定数
    private final static int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final static int MENU_ITEM0 = 0;

    //UI
    private ListView listView;//リストビュー
    private ArrayList<JudoItem> items;//要素群

    //アクティビティ起動時に呼ばれる
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setTitle("Favorite");

        //要素群の読み込み
        items = new ArrayList<JudoItem>();
        JudoItem item = new JudoItem();
        item.checked = false;
        item.link = "";
        item.title = "";
        items.add(item);

        loadItems();

        //リストビューの生成
        listView = new ListView(this);
        listView.setScrollingCacheEnabled(false);
        listView.setAdapter(new MyAdapter());
        setContentView(listView);
    }

    @Override
    public void onPause(){
        super.onPause();
        saveItems();
    }

    //アクティビティ停止時に呼ばれる
    @Override
    public void onStop() {
        super.onStop();

        //要素群の書き込み
        saveItems();
    }



    private void startWebIntentActivity(String url) {
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse(url));
        startActivity(intent);
    }

    //自作アダプタ
    private class MyAdapter extends BaseAdapter {
        //要素数の取得
        @Override
        public int getCount() {
            return items.size();
        }

        //要素の取得
        @Override
        public JudoItem getItem(int pos) {
            return items.get(pos);
        }

        //要素IDの取得
        @Override
        public long getItemId(int pos) {
            return pos;
        }

        //セルのビューの生成
        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            JudoItem item = items.get(pos);

            //レイアウトの生成
            if (view == null) {
                //レイアウトの生成
                LinearLayout layout = new LinearLayout(Favorite.this);
                layout.setBackgroundColor(Color.WHITE);
                layout.setPadding(
                        Util.dp2px(Favorite.this, 10),
                        Util.dp2px(Favorite.this, 10),
                        Util.dp2px(Favorite.this, 10),
                        Util.dp2px(Favorite.this, 10));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View sender) {
                        //編集アクティビティの起動
                        int pos = Integer.parseInt((String) sender.getTag());
                        JudoItem item = items.get(pos);
                        startWebIntentActivity(item.link);
//                        WebView mwebView = (WebView) findViewById(R.id.SampleWebView);
//                        mwebView.getSettings().setJavaScriptEnabled(true);
                    }
                });


//              チェックボックスの追加
                CheckBox checkBox = new CheckBox(Favorite.this);
                checkBox.setTextColor(Color.BLACK);
                checkBox.setId(R.id.cell_checkbox);
                checkBox.setChecked(true);
                checkBox.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View sender) {
                        //ToDo情報の更新
                        int pos = Integer.parseInt((String)sender.getTag());
                        JudoItem item = items.get(pos);
                        item.checked = ((CheckBox)sender).isChecked();
                    }
                });
                layout.addView(checkBox);


                TextView textView = new TextView(Favorite.this);
                textView.setTextColor(Color.BLACK);
                textView.setId(R.id.cell_textview);
                textView.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
                layout.addView(textView);

                view = layout;
            }

            //値の指定
            CheckBox checkBox = (CheckBox)view.findViewById(R.id.cell_checkbox);
            checkBox.setChecked(item.checked);
//            checkBox.setText(item.title);
            checkBox.setTag(""+pos);
            TextView textView = (TextView)view.findViewById(R.id.cell_textview);
            textView.setText(item.title);   //これでチェックボックスをタッチする幅を抑えられる
            textView.setTag(""+pos);
            view.setTag(""+pos);
            return view;
        }
    }


    //要素群の書き込み
    private void saveItems() {
        //ArrayListをJSONに変換
        String json = list2json(items);

        //プリファレンスへの書き込み
        SharedPreferences pref = getSharedPreferences(
                "JUDOmovies", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("items", json);
        editor.apply();
    }

    //要素群の読み込み
    private void loadItems() {
        //プリファレンスからの読み込み
        SharedPreferences pref = getSharedPreferences(
                "JUDOmovies", MODE_PRIVATE);
        String json = pref.getString("items","");

        //JSONをArrayListに変換
        items = items2list(json);
    }

    //ArrayListをJSONに変換
    private String list2json(ArrayList<JudoItem> items) {
        try {
            JSONArray array = new JSONArray();
            for (JudoItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("title", item.title);
                obj.put("link", item.link);
                obj.put("checked", item.checked);
                array.put(obj);
            }
            return array.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    //JSONをArrayListに変換
    private ArrayList<JudoItem> items2list(String json) {
        ArrayList<JudoItem> items = new ArrayList<JudoItem>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                JudoItem item = new JudoItem();
                item.title = obj.getString("title");
                item.link = obj.getString("link");
                item.checked = obj.getBoolean("checked");
                if(item.checked) {        //これでチェック済みだけを取り出せる
                    items.add(item);
                }
//                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }
}
