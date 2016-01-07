package me.calebjones.spacelaunchnow.ui.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.calebjones.spacelaunchnow.MainActivity;
import me.calebjones.spacelaunchnow.content.database.SharedPreference;
import me.calebjones.spacelaunchnow.content.models.Strings;
import me.calebjones.spacelaunchnow.content.models.Launch;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.content.adapter.LaunchAdapter;
import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;
import me.calebjones.spacelaunchnow.content.services.LaunchDataService;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class LaunchesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View view;
    private RecyclerView mRecyclerView;
    private LaunchAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SlideInBottomAnimationAdapter animatorAdapter;
    private List<Launch> rocketLaunches;
    private SharedPreference sharedPreference;
    private SharedPreferences SharedPreferences;

    public LaunchesFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.sharedPreference = SharedPreference.getInstance(getContext());
        this.rocketLaunches = new ArrayList();
        adapter = new LaunchAdapter(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        LayoutInflater lf = getActivity().getLayoutInflater();

        view = lf.inflate(R.layout.fragment_launches, container, false);
        View menu = view.findViewById(R.id.menu);
        menu.setVisibility(View.GONE);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        if (getResources().getBoolean(R.bool.landscape) && getResources().getBoolean(R.bool.isTablet)) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int topRowVerticalPostion = (mRecyclerView == null || mRecyclerView
                        .getChildCount() == 0) ? 0 : mRecyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(dx == 0 && topRowVerticalPostion >= 0);
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        animatorAdapter = new SlideInBottomAnimationAdapter(adapter);
        animatorAdapter.setDuration(350);
        mRecyclerView.setAdapter(animatorAdapter);

                /*Set up Pull to refresh*/
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        ((MainActivity) getActivity()).setActionBarTitle("Upcoming Launches");

        if (this.sharedPreference.getUpcomingFirstBoot()) {
            this.sharedPreference.setUpcomingFirstBoot(false);
            Timber.d("Upcoming Launch Fragment: First Boot.");
            showLoading();
        } else  {
            Timber.d("Upcoming Launch Fragment: Not First Boot.");
            this.rocketLaunches.clear();
            displayLaunches();
        }
        return view;
    }

    public void displayLaunches() {
        this.rocketLaunches = this.sharedPreference.getLaunchesUpcoming();
        filterData(this.rocketLaunches);
    }

    public void filterData(List<Launch> rocketLaunchList) {
        String text_to_filter = this.sharedPreference.getUpcomingFilterText().toLowerCase();
        List<Launch> filteredModelList = new ArrayList();
        Iterator it = rocketLaunchList.iterator();
        while (it.hasNext()) {
            Launch rocketLaunch = (Launch) it.next();
            String launch_name = rocketLaunch.getName().toLowerCase();
            String location_name = rocketLaunch.getLocation().getName().toLowerCase();
            if (launch_name.contains(text_to_filter) || location_name.contains(text_to_filter)) {
                filteredModelList.add(rocketLaunch);
            }
        }
        adapter.clear();
        adapter.addItems(filteredModelList);
    }

    public void fetchData() {
        this.sharedPreference.removeUpcomingLaunches();
        String url = "https://launchlibrary.net/1.1/launch/" + this.SharedPreferences.getString("upcoming_value", "5");
        Intent intent = new Intent(getContext(), LaunchDataService.class);
        intent.putExtra("URL", url);
        intent.setAction(Strings.ACTION_GET_UP_LAUNCHES);
        Timber.d("Sending service intent!");
        getContext().startService(intent);
    }


    public void showLoading() {
        CircularProgressView progressView = (CircularProgressView)
                view.findViewById(R.id.progress_View);
        progressView.setVisibility(View.VISIBLE);
        progressView.startAnimation();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.upcoming_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            adapter.clear();
            showLoading();
            fetchData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {
        adapter.clear();
        showLoading();
        fetchData();
    }
}
