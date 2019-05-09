package full.movie.tubem.player.search_fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;
import full.movie.tubem.player.player.BackgroundPlayer;
import java.util.List;

public class SuggestionListAdapter extends ResourceCursorAdapter {
    private static final int INDEX_ID = 0;
    private static final int INDEX_TITLE = 1;
    private static final String[] columns = {"_id", BackgroundPlayer.TITLE};

    private class ViewHolder {
        /* access modifiers changed from: private|final */
        public final TextView suggestionTitle;

        private ViewHolder(View view) {
            this.suggestionTitle = (TextView) view.findViewById(16908308);
        }
    }

    public SuggestionListAdapter(Context context) {
        super(context, 17367043, null, 0);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        new ViewHolder(view).suggestionTitle.setText(cursor.getString(1));
    }

    public void updateAdapter(List<String> suggestions) {
        MatrixCursor cursor = new MatrixCursor(columns, suggestions.size());
        int i = 0;
        for (String suggestion : suggestions) {
            String[] columnValues = new String[columns.length];
            columnValues[1] = suggestion;
            columnValues[0] = Integer.toString(i);
            cursor.addRow(columnValues);
            i++;
        }
        changeCursor(cursor);
    }

    public String getSuggestion(int position) {
        return ((Cursor) getItem(position)).getString(1);
    }
}
