package com.grad.wave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class FragmentWorks extends Fragment {
    private ArrayList<ArticleInfo> data = new ArrayList<ArticleInfo>();
    private View view;
    private RecyclerView rv;
    private SwipeRefreshLayout refresh;
    private TextView allloaded;
    //一次请求的Artini个数和当前处在顶端的Artini。注意，服务器端目录顶端条目同样是客户端顶端条目。
    private int PageSize = 6;
    private int PageBottom = 0;
    private int lastvisible = 0;
    private boolean first_load = true;
    private boolean swiping_up = false;

    public FragmentWorks(@NonNull ArrayList<ArticleInfo> dat){
        data = dat;
    }

    public FragmentWorks(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle saveInstanceState){
        data = AppManager.AppIO.GetList(1,0,PageSize);
        PageBottom = data.size()-1;
        //把分段页面从XML文件载入到container中
        view = inflater.inflate(R.layout.works_fragment,container,false);
        //获取此分段下的View

        rv = view.findViewById(R.id.works_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        //设置列表适配器，向适配器传递data
        ArticleInfoAdapter adapter = new ArticleInfoAdapter(data,view.getContext());
        rv.setAdapter(adapter);

        allloaded = view.findViewById(R.id.works_all_loaded);
        refresh = view.findViewById(R.id.works_refreshlayout);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Toast.makeText(getContext(), "刷新", Toast.LENGTH_SHORT).show();
                    data = AppManager.AppIO.GetList(1, 0, PageSize);
                    PageBottom = data.size() - 1;
                    ArticleInfoAdapter adapter = (ArticleInfoAdapter) rv.getAdapter();
                    adapter.UpdateData(data);
                    adapter.notifyDataSetChanged();
                    PageBottom = data.size() - 1;
                    if (data.size() >= 6)
                        allloaded.setVisibility(View.INVISIBLE);
                    else
                        allloaded.setVisibility(View.VISIBLE);
                    refresh.setRefreshing(false);
                }catch(Exception ex){
                    Toast.makeText(getContext(),"暂无数据",Toast.LENGTH_SHORT).show();
                }
            }
        });


        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                try {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE
                            && lastvisible == data.size() - 1 && swiping_up) {
                        Toast.makeText(getContext(),"正在加载更多",Toast.LENGTH_LONG).show();
                        swiping_up = false;
                        ArticleInfoAdapter adapter = (ArticleInfoAdapter) rv.getAdapter();
                        ArrayList<ArticleInfo> addition = AppManager.AppIO.GetList(1, PageBottom + 1, PageSize);
                        for (int i = 0; i < addition.size(); ++i)
                            data.add(addition.get(i));
                        adapter.UpdateData(data);
                        adapter.notifyDataSetChanged();
                        PageBottom = data.size()-1;
                        if(data.size() >= 6)
                            allloaded.setVisibility(View.INVISIBLE);
                        else
                            allloaded.setVisibility(View.VISIBLE);
                    }
                }catch(Exception ex){
                    Toast.makeText(getContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 0)
                    swiping_up = true;
                else
                    swiping_up = false;
                lastvisible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
            }
        });


        return view;
    }
/*
    @Override
    public void onResume(){
        super.onResume();
        if(first_load){
            first_load = false;
            return;
        }
        ArticleInfoAdapter adapter = (ArticleInfoAdapter)rv.getAdapter();
        data = AppManager.AppIO.GetList(1,1,PageSize);
        if(data != null) {
            adapter.UpdateData(data);
            adapter.notifyDataSetChanged();
        }
    }
 */
}
