package full.movie.tubem.player.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import full.movie.tubem.player.ChannelActivity;
import full.movie.tubem.player.MainActivity;
import full.movie.tubem.player.detail.VideoItemDetailActivity;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.StreamingService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class NavStack {
    private static final String NAV_STACK = "nav_stack";
    public static final String SERVICE_ID = "service_id";
    private static final String TAG = NavStack.class.toString();
    public static final String URL = "url";
    private static NavStack instance = new NavStack();
    private Stack<NavEntry> stack = new Stack<>();

    private enum ActivityId {
        CHANNEL,
        DETAIL
    }

    private class NavEntry {
        public int serviceId;
        public String url;

        public NavEntry(String url2, int serviceId2) {
            this.url = url2;
            this.serviceId = serviceId2;
        }
    }

    private NavStack() {
    }

    public static NavStack getInstance() {
        return instance;
    }

    private void addEntry(String url, Class ac, int serviceId) {
        this.stack.push(new NavEntry(url, serviceId));
    }

    public void navBack(Activity activity) throws Exception {
        if (this.stack.size() == 0) {
            activity.finish();
            return;
        }
        this.stack.pop();
        if (this.stack.size() == 0) {
            openMainActivity(activity);
            return;
        }
        NavEntry entry = (NavEntry) this.stack.pop();
        try {
            StreamingService service = Newapp.getService(entry.serviceId);
            switch (service.getLinkTypeByUrl(entry.url)) {
                case STREAM:
                    openDetailActivity(activity, entry.url, entry.serviceId);
                    return;
                case CHANNEL:
                    openChannelActivity(activity, entry.url, entry.serviceId);
                    return;
                case NONE:
                    throw new Exception("Url not known to service. service=" + Integer.toString(entry.serviceId) + " url=" + entry.url);
                default:
                    openMainActivity(activity);
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       // e.printStackTrace();
    }

    public void openChannelActivity(Context context, String url, int serviceId) {
        openActivity(context, url, serviceId, ChannelActivity.class);
    }

    public void openDetailActivity(Context context, String url, int serviceId) {
        openActivity(context, url, serviceId, VideoItemDetailActivity.class);
    }

    private void openActivity(Context context, String url, int serviceId, Class acitivtyClass) {
        this.stack.push(new NavEntry(url, serviceId));
        Intent i = new Intent(context, acitivtyClass);
        i.putExtra("service_id", serviceId);
        i.putExtra("url", url);
        context.startActivity(i);
    }

    public void openMainActivity(Activity a) {
        this.stack.clear();
        Intent i = new Intent(a, MainActivity.class);
        i.addFlags(67108864);
        NavUtils.navigateUpTo(a, i);
    }

    public void onSaveInstanceState(Bundle state) {
        ArrayList<String> sa = new ArrayList<>();
        Iterator it = this.stack.iterator();
        while (it.hasNext()) {
            sa.add(((NavEntry) it.next()).url);
        }
        state.putStringArrayList(NAV_STACK, sa);
    }

    public void restoreSavedInstanceState(Bundle state) {
        Iterator it = state.getStringArrayList(NAV_STACK).iterator();
        while (it.hasNext()) {
            String url = (String) it.next();
            this.stack.push(new NavEntry(url, Newapp.getServiceByUrl(url).getServiceId()));
        }
    }
}
