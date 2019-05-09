package full.movie.tubem.player.search_fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import full.movie.tubem.player.R;
import full.movie.tubem.player.ReCaptchaActivity;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.search.SearchEngine.Filter;
import full.movie.tubem.player.extractor.search.SearchResult;
import full.movie.tubem.player.info_list.InfoItemBuilder.OnInfoItemSelectedListener;
import full.movie.tubem.player.info_list.InfoListAdapter;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import full.movie.tubem.player.search_fragment.SearchWorker.SearchWorkerResultListener;
import full.movie.tubem.player.util.NavStack;
import java.util.EnumSet;

public class SearchInfoItemFragment extends Fragment {
    private static final String QUERY = "query";
    private static final String STREAMING_SERVICE = "streaming_service";
    private static final String TAG = SearchInfoItemFragment.class.toString();
    private EnumSet<Filter> filter = EnumSet.of(Filter.CHANNEL, Filter.STREAM);
    /* access modifiers changed from: private */
    public InfoListAdapter infoListAdapter = null;
    /* access modifiers changed from: private */
    public boolean isLoading = false;
    private ProgressBar loadingIndicator = null;
    /* access modifiers changed from: private */
    public int pageNumber = 0;
    /* access modifiers changed from: private */
    public String searchQuery = "";
    /* access modifiers changed from: private */
    public LinearLayoutManager streamInfoListLayoutManager = null;
    /* access modifiers changed from: private */
    public int streamingServiceId = -1;
    private SuggestionListAdapter suggestionListAdapter = null;

    public class SearchQueryListener implements OnQueryTextListener {
        public SearchQueryListener() {
        }

        public boolean onQueryTextSubmit(String query) {
            Activity a = SearchInfoItemFragment.this.getActivity();
            try {
                SearchInfoItemFragment.this.search(query);
                try {
                    ((InputMethodManager) a.getSystemService("input_method")).hideSoftInputFromWindow(a.getCurrentFocus().getWindowToken(), 2);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    ErrorActivity.reportError((Context) a, (Throwable) e, null, a.findViewById(16908290), ErrorInfo.make(0, Newapp.getNameOfService(SearchInfoItemFragment.this.streamingServiceId), "Could not get widget with focus", R.string.general_error));
                }
                a.getCurrentFocus().clearFocus();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return true;
        }

        public boolean onQueryTextChange(String newText) {
            if (!newText.isEmpty()) {
                SearchInfoItemFragment.this.searchSuggestions(newText);
            }
            return true;
        }
    }

    public static SearchInfoItemFragment newInstance(int streamingServiceId2, String searchQuery2) {
        Bundle args = new Bundle();
        args.putInt(STREAMING_SERVICE, streamingServiceId2);
        args.putString("query", searchQuery2);
        SearchInfoItemFragment fragment = new SearchInfoItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.searchQuery = "";
        if (savedInstanceState != null) {
            this.searchQuery = savedInstanceState.getString("query");
            this.streamingServiceId = savedInstanceState.getInt(STREAMING_SERVICE);
        } else {
            try {
                Bundle args = getArguments();
                if (args != null) {
                    this.searchQuery = args.getString("query");
                    this.streamingServiceId = args.getInt(STREAMING_SERVICE);
                } else {
                    this.streamingServiceId = Newapp.getIdOfService(SearchWorker.SearchRunnable.YOUTUBE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ErrorActivity.reportError((Context) getActivity(), (Throwable) e, null, getActivity().findViewById(16908290), ErrorInfo.make(0, Newapp.getNameOfService(this.streamingServiceId), "", R.string.general_error));
            }
        }
        setHasOptionsMenu(true);
        SearchWorker.getInstance().setSearchWorkerResultListener(new SearchWorkerResultListener() {
            public void onResult(SearchResult result) {
                SearchInfoItemFragment.this.infoListAdapter.addInfoItemList(result.resultList);
                SearchInfoItemFragment.this.setDoneLoading();
            }

            public void onNothingFound(int stringResource) {
                Toast.makeText(SearchInfoItemFragment.this.getActivity(), SearchInfoItemFragment.this.getString(stringResource), 0).show();
                SearchInfoItemFragment.this.setDoneLoading();
            }

            public void onError(String message) {
                Toast.makeText(SearchInfoItemFragment.this.getActivity(), message, 1).show();
                SearchInfoItemFragment.this.setDoneLoading();
            }

            public void onReCaptchaChallenge() {
                Toast.makeText(SearchInfoItemFragment.this.getActivity(), "ReCaptcha Challenge requested", 1).show();
                SearchInfoItemFragment.this.startActivityForResult(new Intent(SearchInfoItemFragment.this.getActivity(), ReCaptchaActivity.class), 10);
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_searchinfoitem, container, false);
        Context context = view.getContext();
        this.loadingIndicator = (ProgressBar) view.findViewById(R.id.progressBar);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        this.streamInfoListLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(this.streamInfoListLayoutManager);
        this.infoListAdapter = new InfoListAdapter(getActivity(), getActivity().findViewById(16908290));
        this.infoListAdapter.setOnStreamInfoItemSelectedListener(new OnInfoItemSelectedListener() {
            public void selected(String url, int serviceId) {
                NavStack.getInstance().openDetailActivity(SearchInfoItemFragment.this.getContext(), url, serviceId);
            }
        });
        this.infoListAdapter.setOnChannelInfoItemSelectedListener(new OnInfoItemSelectedListener() {
            public void selected(String url, int serviceId) {
                NavStack.getInstance().openChannelActivity(SearchInfoItemFragment.this.getContext(), url, serviceId);
            }
        });
        recyclerView.setAdapter(this.infoListAdapter);
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (SearchInfoItemFragment.this.streamInfoListLayoutManager.getChildCount() + SearchInfoItemFragment.this.streamInfoListLayoutManager.findFirstVisibleItemPosition() >= SearchInfoItemFragment.this.streamInfoListLayoutManager.getItemCount() && !SearchInfoItemFragment.this.isLoading) {
                        SearchInfoItemFragment.this.pageNumber = SearchInfoItemFragment.this.pageNumber + 1;
                        SearchInfoItemFragment.this.search(SearchInfoItemFragment.this.searchQuery, SearchInfoItemFragment.this.pageNumber);
                    }
                }
            }
        });
        return view;
    }

    public void onStart() {
        super.onStart();
        if (!this.searchQuery.isEmpty()) {
            search(this.searchQuery);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("query", this.searchQuery);
        outState.putInt(STREAMING_SERVICE, this.streamingServiceId);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
        setupSearchView((SearchView) menu.findItem(R.id.action_search).getActionView());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter_all /*2131689783*/:
                changeFilter(item, EnumSet.of(Filter.STREAM, Filter.CHANNEL));
                return true;
            case R.id.menu_filter_video /*2131689784*/:
                changeFilter(item, EnumSet.of(Filter.STREAM));
                return true;
            case R.id.menu_filter_channel /*2131689785*/:
                changeFilter(item, EnumSet.of(Filter.CHANNEL));
                return true;
            default:
                return false;
        }
    }

    private void changeFilter(MenuItem item, EnumSet<Filter> filter2) {
        this.filter = filter2;
        item.setChecked(true);
        if (this.searchQuery != null && !this.searchQuery.isEmpty()) {
            Log.d(TAG, "Fuck+ " + this.searchQuery);
            search(this.searchQuery);
        }
    }

    private void setupSearchView(SearchView searchView) {
        this.suggestionListAdapter = new SuggestionListAdapter(getActivity());
        searchView.setSuggestionsAdapter(this.suggestionListAdapter);
        searchView.setOnSuggestionListener(new SearchSuggestionListener(searchView, this.suggestionListAdapter));
        searchView.setOnQueryTextListener(new SearchQueryListener());
        if (this.searchQuery != null && !this.searchQuery.isEmpty()) {
            searchView.setQuery(this.searchQuery, false);
            searchView.setIconifiedByDefault(false);
        }
    }

    /* access modifiers changed from: private */
    public void search(String query) {
        this.infoListAdapter.clearSteamItemList();
        this.pageNumber = 0;
        this.searchQuery = query;
        search(query, this.pageNumber);
        hideBackground();
        this.loadingIndicator.setVisibility(0);
    }

    /* access modifiers changed from: private */
    public void search(String query, int page) {
        this.isLoading = true;
        SearchWorker.getInstance().search(this.streamingServiceId, query, page, getActivity(), this.filter);
    }

    /* access modifiers changed from: private */
    public void setDoneLoading() {
        this.isLoading = false;
        this.loadingIndicator.setVisibility(8);
    }

    private void hideBackground() {
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.mainBG).setVisibility(8);
        }
    }

    /* access modifiers changed from: private */
    public void searchSuggestions(String query) {
        new Thread(new SuggestionSearchRunnable(this.streamingServiceId, query, getActivity(), this.suggestionListAdapter)).start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 10:
                if (resultCode != -1) {
                    Log.d(TAG, "ReCaptcha failed");
                    return;
                } else if (this.searchQuery.length() != 0) {
                    search(this.searchQuery);
                    return;
                } else {
                    return;
                }
            default:
                Log.e(TAG, "Request code from activity not supported [" + requestCode + "]");
                return;
        }
    }
}
