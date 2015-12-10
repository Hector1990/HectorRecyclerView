package com.hector.hectorrecyclerview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import com.hector.recyclerview.HectorRecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    HectorRecyclerView recyclerView;
    List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (HectorRecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add("item" + i);
        }
        final MyAdapter adapter = new MyAdapter(data);
        recyclerView.setAdapter(adapter);
        recyclerView.setListener(new HectorRecyclerView.RefreshListener() {
            @Override
            public void refresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.refreshComplete();
                    }
                }, 2000);
            }

            @Override
            public void loadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (data.size() >= 40) {
                            recyclerView.noMoreData();
                        }
                        else {
                            for (int i = 20; i < 40; i++) {
                                data.add("item" + i);
                            }
                            adapter.notifyDataSetChanged();
                            recyclerView.loadComplete();
                        }
                    }
                }, 1000);
            }
        });
        recyclerView.autoRefresh();
    }


}
