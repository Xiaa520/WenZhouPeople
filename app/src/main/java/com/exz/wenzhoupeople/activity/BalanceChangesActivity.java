package com.exz.wenzhoupeople.activity;

import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.exz.wenzhoupeople.R;
import com.exz.wenzhoupeople.adapter.BanlanceChangeAdapter;
import com.exz.wenzhoupeople.appclication.App;
import com.exz.wenzhoupeople.entity.BalanceChangeModel;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.szw.lib.myframework.base.BaseActivity;
import cn.com.szw.lib.myframework.config.Constants;
import cn.com.szw.lib.myframework.utils.DateUtils;
import cn.com.szw.lib.myframework.utils.RecycleViewDivider;
import cn.com.szw.lib.myframework.utils.net.NetEntity;
import cn.com.szw.lib.myframework.utils.net.callback.DialogCallback;
import cn.com.szw.lib.myframework.view.CustomLoadMoreView;

import static com.exz.wenzhoupeople.config.Urls.BalanceRecord;


/**
 * Created by weicao on 2017/7/7.
 *
 */

public class BalanceChangesActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {


    @BindView(R.id.mLeft)
    TextView mLeft;
    @BindView(R.id.mTitle)
    TextView mTitle;
    @BindView(R.id.mRight)
    TextView mRight;
    @BindView(R.id.mRightImg)
    ImageView mRightImg;
    @BindView(R.id.mLeftImg)
    ImageView mLeftImg;
    @BindView(R.id.parent_lay)
    RelativeLayout parentLay;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    private int page = 1;
    public int refreshState = Constants.RefreshState.STATE_REFRESH;
    private BanlanceChangeAdapter adapter;

    @Override
    public boolean initToolbar() {
        mTitle.setText("余额记录");
        mTitle.setTextSize(18);
        toolbar.setContentInsetsAbsolute(0, 0);
        setSupportActionBar(toolbar);
        return false;
    }

    @Override
    public int setInflateId() {
        return R.layout.activity_banlacr_change;
    }

    @Override
    public void init() throws Exception {
        super.init();

        initView();
        onRefresh();

    }

    private void initView() {
        adapter = new BanlanceChangeAdapter();
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setLoadMoreView(new CustomLoadMoreView());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.HORIZONTAL, 5, ContextCompat.getColor(mContext, R.color.lin_gray)));
        recyclerView.setAdapter(adapter);
        recyclerView.setClipToPadding(false);
        recyclerView.setPadding(SizeUtils.dp2px(0), SizeUtils.dp2px(10), SizeUtils.dp2px(0), SizeUtils.dp2px(0));
        adapter.setOnLoadMoreListener(this, recyclerView);
        refresh.setOnRefreshListener(this);

    }


    @Override
    public void onRefresh() {
        refresh.setRefreshing(false);
        page = 1;
        refreshState = Constants.RefreshState.STATE_REFRESH;
        initPort();

    }

    @Override
    public void onLoadMoreRequested() {
        refresh.setEnabled(false);
        refreshState = Constants.RefreshState.STATE_LOAD_MORE;
        initPort();
    }


    @OnClick(R.id.mLeftImg)
    public void onViewClicked() {
        finish();
    }


    private void initPort() {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", App.getLoginUserId());
        params.put("timestamp", String.valueOf(DateUtils.dateToUnixTimestamp()));
        params.put("page", String.valueOf(page));
        params.put("requestCheck", EncryptUtils.encryptMD5ToString(App.getLoginUserId() + String.valueOf(DateUtils.dateToUnixTimestamp()), App.salt).toLowerCase());
        OkGo.<NetEntity<List<BalanceChangeModel>>>post(BalanceRecord)
                .params(params)
                .tag(this)
                .execute(new DialogCallback<NetEntity<List<BalanceChangeModel>>>(mContext) {

                    @Override
                    public void onSuccess(Response<NetEntity<List<BalanceChangeModel>>> response) {
                        refresh.setEnabled(true);
                        refresh.setRefreshing(false);

                        if (response.body().getCode() == cn.com.szw.lib.myframework.config.Constants.NetCode.SUCCESS) {
                            if (refreshState == cn.com.szw.lib.myframework.config.Constants.RefreshState.STATE_REFRESH) {
                                adapter.setNewData(response.body().getData());

                            } else {
                                adapter.addData(response.body().getData());
                            }

                            if (!response.body().getData().isEmpty()) {
                                adapter.loadMoreComplete();
                                page++;
                            } else {
                                adapter.loadMoreEnd();
                            }
                        }

                    }

                    @Override
                    public void onError(Response<NetEntity<List<BalanceChangeModel>>> response) {
                        super.onError(response);
                        refresh.setEnabled(true);
                        refresh.setRefreshing(false);
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        adapter.loadMoreFail();
                    }
                });
    }


}
