package full.movie.tubem.player.search_fragment;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnSuggestionListener;

public class SearchSuggestionListener implements OnSuggestionListener {
    private final SuggestionListAdapter adapter;
    private final SearchView searchView;

    public SearchSuggestionListener(SearchView searchView2, SuggestionListAdapter adapter2) {
        this.searchView = searchView2;
        this.adapter = adapter2;
    }

    public boolean onSuggestionSelect(int position) {
        this.searchView.setQuery(this.adapter.getSuggestion(position), true);
        return false;
    }

    public boolean onSuggestionClick(int position) {
        this.searchView.setQuery(this.adapter.getSuggestion(position), true);
        return false;
    }
}
