package com.example.shunki.judomovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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


public class JUDOmovies extends AppCompatActivity {
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
        this.setTitle("Movies");


        loadItems();

        //リストビューの生成
        listView = new ListView(this);
        listView.setScrollingCacheEnabled(false);
        listView.setAdapter(new MyAdapter());
        setContentView(listView);
    }

//    @Override
//    public void onResume(){
//        super.onResume();
//        loadItems();
//    }

    @Override
    public void onPause() {
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

    //オプションメニューの生成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //追加アイテムの追加
        MenuItem item0 = menu.add(0, MENU_ITEM0, 0, "お気に入り");
        item0.setIcon(android.R.drawable.checkbox_on_background);
        item0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    //メニューアイテム選択イベントの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == MENU_ITEM0) {
            Intent intent = new Intent(this, Favorite.class);
            startActivity(intent);
        }
        return true;
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
                LinearLayout layout = new LinearLayout(JUDOmovies.this);
                layout.setBackgroundColor(Color.WHITE);
                layout.setPadding(
                        Util.dp2px(JUDOmovies.this, 10),
                        Util.dp2px(JUDOmovies.this, 10),
                        Util.dp2px(JUDOmovies.this, 10),
                        Util.dp2px(JUDOmovies.this, 10));
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
                CheckBox checkBox = new CheckBox(JUDOmovies.this);
                checkBox.setTextColor(Color.BLACK);
                checkBox.setId(R.id.cell_checkbox);
                checkBox.setChecked(false);
                checkBox.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View sender) {
                        //情報の更新
                        int pos = Integer.parseInt((String) sender.getTag());
                        JudoItem item = items.get(pos);
                        item.checked = ((CheckBox) sender).isChecked();
                    }
                });
                layout.addView(checkBox);


                TextView textView = new TextView(JUDOmovies.this);
                textView.setTextColor(Color.BLACK);
                textView.setId(R.id.cell_textview);
                textView.setLayoutParams(new LinearLayout.LayoutParams(WC, 250));   //文がリスト内に収まるように調整
                layout.addView(textView);

                view = layout;
            }

            //値の指定
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cell_checkbox);
            checkBox.setChecked(item.checked);
            checkBox.setTag("" + pos);
            TextView textView = (TextView) view.findViewById(R.id.cell_textview);
            textView.setText(item.title);   //これでチェックボックスをタッチする幅を抑えられる
            textView.setTag("" + pos);
            view.setTag("" + pos);
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
        String json = pref.getString("items", "");
        items = new ArrayList<JudoItem>();
        if (json.isEmpty()) {
            for (int i = 0; i < 16; i++) {      //アプリインストール後の初期値を設定する
                JudoItem item = new JudoItem();
                String[] lists = getResources().getStringArray(R.array.lists);
                item.title = lists[i];
                String[] links = getResources().getStringArray(R.array.links);
                item.link = links[i];
                item.checked = false;   //この位置でないと「チェック済み動画」がリストの最上部にきてしまう
                items.add(item);
            }
        } else {
            //JSONをArrayListに変換
            items = items2list(json);
        }
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
            for (int i = 0; i < 16; i++) {  //　[ i < array.length()]にすると、チェックしたリストしか読み込まれない。
                JSONObject obj = array.getJSONObject(i);
                JudoItem item = new JudoItem();
                item.title = obj.getString("title");
                item.link = obj.getString("link");
                item.checked = obj.getBoolean("checked");
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

}