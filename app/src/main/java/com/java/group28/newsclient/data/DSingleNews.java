package com.java.group28.newsclient.data;

import com.java.group28.newsclient.tools.SerialBitmap;
import com.java.group28.newsclient.tools.StringFormatTransfer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

import static com.java.group28.newsclient.tools.Http.sendGet;

public class DSingleNews implements java.io.Serializable{
    public String lang_type, news_author,
            news_id, news_source, news_time,
            news_title, news_url, news_video, news_intro, content, readable_content;
    public String news_pictures;
    public SerialBitmap news_intropic;
    public boolean readed;
    public boolean loaded;
    public String[] wordList;
    public double[] scoreList;
    int length;

    public int news_tag;

    DSingleNews(JSONObject o, int i){
        news_pictures = "";
        news_id = "";
        content = "";
        readed = false;
        loaded = false;
        String pictureList = "";
        try {
            news_tag = i;
            lang_type = o.getString("lang_Type");
            news_author = o.getString("news_Author");
            news_id = o.getString("news_ID");
            news_time = o.getString("news_Time");
            news_title = o.getString("news_Title");
            news_url = o.getString("news_URL");
            news_video = o.getString("news_Video");
            news_intro = o.getString("news_Intro");
            news_source = o.getString("news_Source");
            news_time = o.getString("news_Time");
            news_intropic = new SerialBitmap();
            pictureList = o.getString("news_Pictures");
        } catch (JSONException e) {
        }

        if (!pictureList.equals("") && !pictureList.startsWith(" ")) {
            news_pictures = pictureList.split(";| ")[0];
        }
        else {
            news_pictures = "";
        }
    }
    public DSingleNews(String id) {
        news_id = id;
        news_intropic = new SerialBitmap();
        news_pictures = "";
    }
    public void load() {
        if (loaded) return;
        loaded = true;
        String url = "http://166.111.68.66:2042/news/action/query/detail?newsId=" + news_id;
        String txt = sendGet(url);
        try {
            JSONObject art = new JSONObject(txt);
            content = art.getString("news_Content");
            news_title = art.getString("news_Title");
            news_time = art.getString("news_Time");
            news_source = art.getString("news_Source");
            String keyst = art.getString("seggedPListOfContent");
            String discretekeyst[] = keyst.split(" ");
            HashSet<String> neededkeyst = new HashSet<String>();
            for(int i = 0; i < discretekeyst.length; i++){
                if (discretekeyst[i].length() < 7) continue;
                if (discretekeyst[i].contains("/ORG"))
                    neededkeyst.add(discretekeyst[i].substring(0, discretekeyst[i].length() - 5));
                if (discretekeyst[i].contains("/PER"))
                    neededkeyst.add(discretekeyst[i].substring(0, discretekeyst[i].length() - 5));
                if (discretekeyst[i].contains("/LOC"))
                    neededkeyst.add(discretekeyst[i].substring(0, discretekeyst[i].length() - 5));
                if (discretekeyst[i].contains("/nx"))
                    neededkeyst.add(discretekeyst[i].substring(0, discretekeyst[i].length() - 4));
            }
            readable_content = content;
            for(String str:neededkeyst){
                if (content.indexOf(str) != -1)
                    content = content.replaceFirst(str,"<a href=\"https://baike.baidu.com/item/" + str + "\">"
                    + str + "</a>");
            }
            if (news_intro == null) news_intro = "";

            String pictureList = art.getString("news_Pictures");
            if (!pictureList.equals("") && !pictureList.startsWith(" ")) {
                news_pictures = pictureList.split(";| ")[0];
            }

            JSONArray wordsList = art.getJSONArray("Keywords");
            length = wordsList.length();
            length = min(5, length);
            wordList = new String [length];
            scoreList = new double [length];
            for (int i = 0; i < length; i++) {
                JSONObject word = wordsList.getJSONObject(i);
                wordList[i] = word.getString("word");
                scoreList[i] = word.getDouble("score");
            }
        } catch (Exception e) {
            content = e.toString();
        }
    }

    private int min(int a, int b) {
        if (a < b) return a;
        return b;
    }

    public String displayTitle(){
        return news_title;
    }

    public String displaySource(){
        return StringFormatTransfer.toDBC("来源： " + news_source);
    }

    public String displayTime(){
        // time's format:
        // 20160912000000
        String str;
        try {
            str = Integer.parseInt(news_time.substring(4, 6)) + "月" + Integer.parseInt(news_time.substring(6, 8)) + "日";
        } catch (Exception e) {
            str = "";
        }
        return str;
    }

    public String displayContent(){
        return content.replaceAll("  ", "\n");
    }

    public String displayIntro() { return news_intro;}
}
