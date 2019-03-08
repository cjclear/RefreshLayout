package com.cj.refreshlayout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cj.refreshlib.LoadInterface;
import com.cj.refreshlib.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RefreshLayout refreshLayout;
    RecyclerView recyclerView;
    List<String> list, list2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = findViewById(R.id.root);
        final TextView head = findViewById(R.id.head);

        refreshLayout.setLoadInterface(new LoadInterface() {
            @Override
            public void onLoadStart() {
                head.setText("loading");
                head.animate().cancel();
                head.animate().rotation(40 * 360).setDuration(20 * 1000).start();
                refreshLayout.removeCallbacks(runnable);
                refreshLayout.postDelayed(runnable, 2000);
            }

            @Override
            public void onLoadFinish() {
                head.setText("moving");
                head.animate().cancel();

                head.setRotation(0);
            }

            @Override
            public void onLoadCancel() {

            }

            @Override
            public void onLoadMoreBegin() {
                if (isList1) {
                    list.add("加载中");
                    adapter.notifyItemInserted(list.size()-1);
                    refreshLayout.removeCallbacks(runnable2);
                    refreshLayout.postDelayed(runnable2, 100);
                }else {
                    list2.add("加载中");
                    adapter.notifyItemInserted(list2.size()-1);
                    refreshLayout.removeCallbacks(runnable2);
                    refreshLayout.postDelayed(runnable2, 100);
                }
            }

            @Override
            public void onOffsetChanged(int offset) {

            }
        });
        recyclerView = findViewById(R.id.list);
        GridLayoutManager manager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(manager);
        refreshLayout.enableLoadMore(10);
        list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add(String.valueOf(i));
        }
        list2 = new ArrayList<>();
        for (int i = 100; i < 150; i++) {
            list2.add(String.valueOf(i));
        }
        adapter = new MyAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public MyHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return true;
    }

    public boolean isList1 = true;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshLayout.dataGetted();
            if (isList1) {
                isList1 = false;
                adapter = new MyAdapter(list2);
                recyclerView.setAdapter(adapter);
            } else {
                isList1 = true;
                adapter = new MyAdapter(list);
                recyclerView.setAdapter(adapter);
            }

        }
    };
    public Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            if (isList1) {
                list.remove(list.size()-1);
                adapter.notifyItemRemoved(list.size());
                int value = Integer.valueOf(list.get(list.size() -1));
                if(value < 440){
                    int position = list.size() - 1;
                    for( int i = value+1; i < value + 51; i++){
                        list.add(String.valueOf(i));
                    }
                    adapter.notifyItemRangeInserted(position+1,50);
                }

            } else {
                list2.remove(list2.size()-1);
                adapter.notifyItemRemoved(list2.size());
                int value = Integer.valueOf(list2.get(list2.size() -1));
                int position = list2.size() - 1;
                for( int i = value+1; i < value + 51; i++){
                    list2.add(String.valueOf(i));
                }
                adapter.notifyItemRangeInserted(position+1,50);
            }

        }
    };
    MyAdapter adapter;

    public class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        List<String> list;


        public MyAdapter(List<String> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (parent.getContext().getResources().getDisplayMetrics().density * 60 + 0.5f));
            view.setLayoutParams(params);
            view.setGravity(Gravity.CENTER);
            MyHolder holder = new MyHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
            ((TextView) holder.itemView).setText(list.get(position));
            ((TextView) holder.itemView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

}
