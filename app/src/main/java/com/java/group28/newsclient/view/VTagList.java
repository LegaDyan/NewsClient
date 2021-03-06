package com.java.group28.newsclient.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.java.group28.newsclient.R;
import com.java.group28.newsclient.data.DSingleNews;
import com.java.group28.newsclient.data.DSingleTag;
import com.java.group28.newsclient.data.DTagList;
import com.java.group28.newsclient.data.Data;
import com.java.group28.newsclient.tools.Network;
import com.java.group28.newsclient.tools.PicGetter;

import java.util.List;

public class VTagList extends Activity {
    int tagId;
    Data app;

    @Override
    protected void onResume() {
        super.onResume();
        setTheme((app.is_night_shift_on) ? R.style.DarkTheme : R.style.LightTheme);
        setContentView(R.layout.activity_taglist);
        final DSingleTag dt = DTagList.getNewsById(tagId);

        if (tagId > 12 && dt.news_list.size() == 0) {
            dt.enlarge((String)DTagList.lstImageitem.get(tagId).get("ItemText"));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.taglist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                if (tagId <= 12 && tagId > 0) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int itemCount = layoutManager.getItemCount();
                    int lastposition = layoutManager.findLastVisibleItemPosition();
                    if (itemCount < lastposition + 10) {
                        dt.enlarge();
                    }
                }
            }

        });

        CardAdapter adapter = new CardAdapter(this, R.layout.activity_cardview, new int[]{
                R.id.card_title,
                R.id.card_source,
                R.id.card_intropic,
                R.id.card_intro},
                dt);

        adapter.setOnItemClickLitener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(VTagList.this, VDetails.class);
                try {
                    intent.putExtra("news_id", dt.news_list.get(position).news_id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                VTagList.this.startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        recyclerView.setAdapter(adapter);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Data) getApplication();
        setTheme((app.is_night_shift_on) ? R.style.DarkTheme : R.style.LightTheme);
        setContentView(R.layout.activity_taglist);

        Intent intent = getIntent();
        tagId = intent.getIntExtra("tag_id", -1);
        if (tagId > 0 && tagId <= 12) DTagList.build(tagId);

    }
}

class CardAdapter extends RecyclerView.Adapter<CardAdapter.InnerViewHolder>{
    private Activity activity;
    private int sourceLayout;
    private int[] itemsId;
    private List<DSingleNews> newsList;
    private OnItemClickListener mOnItemClickListener;
    private Data app;

    // constructor
    CardAdapter(Activity activity, int source, int[] itemsId, DSingleTag tag){
        this.activity = activity;
        this.sourceLayout = source;
        this.itemsId = itemsId;
        this.newsList = tag.news_list;
        this.app = (Data)activity.getApplication();
    }
    // set click reflection
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view , int position);
    }

    public void setOnItemClickLitener(OnItemClickListener listener)
    {
        this.mOnItemClickListener = listener;
    }

    // holder
    @Override
    public InnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        InnerViewHolder holder = new InnerViewHolder(LayoutInflater.from(
                activity).inflate(sourceLayout, parent,
                false),
                itemsId);
        return holder;
    }

    @Override
    public void onBindViewHolder(final InnerViewHolder holder, final int position) {

        holder.item.bindValue(position, this.newsList.get(position), app.is_4G_mode_on);

        if (mOnItemClickListener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);

                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    class InnerViewHolder extends RecyclerView.ViewHolder
    {
        CardItemGroup item;

        public InnerViewHolder(View view, int[] args)
        {
            super(view);
            this.initContentList(view, args);
        }

        void initContentList(View parentView, int[] args){
            View[] views = new View[args.length];
            for (int i = 0; i < args.length; i++){
                views[i] = parentView.findViewById(args[i]);
            }
            item = new CardItemGroup(views);
        }
    }
}

class CardItemGroup{
    VAlignTextView title;
    TextView source;
    ImageView intropic;
    VAlignTextView intro;

    CardItemGroup(View[] args){
        title = (VAlignTextView) args[0];
        source = (TextView) args[1];
        intropic = (ImageView) args[2];
        intro = (VAlignTextView) args[3];
    }

    void bindValue(int index, DSingleNews news, boolean hidePic){

        title.setText(news.displayTitle());
        source.setText(news.displaySource() + "   " + news.displayTime());

        if (hidePic){
            intropic.setVisibility(View.GONE);
        }
        else{
            if (news.news_intropic.bitmap != null){
                intropic.setImageBitmap(news.news_intropic.bitmap);
            }
            else{
                if (Network.isConnected()) {
                    PicGetter p = new PicGetter(VRecents.context);
                    p.setImageView(intropic, news);
                }
                else {
                    intropic.setVisibility(View.GONE);
                }
            }
        }
        intro.setText(news.displayIntro());
    }
}


