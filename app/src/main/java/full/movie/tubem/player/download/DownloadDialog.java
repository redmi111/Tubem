package full.movie.tubem.player.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.online.garam.service.DownloadManagerService;
import full.movie.tubem.player.App;
import full.movie.tubem.player.R;
import full.movie.tubem.player.settings.NewSettings;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadDialog extends DialogFragment {
    public static final String AUDIO_URL = "audio_url";
    public static final String FILE_SUFFIX_AUDIO = "file_suffix_audio";
    public static final String FILE_SUFFIX_VIDEO = "file_suffix_video";
    private static final String TAG = DialogFragment.class.getName();
    public static final String TITLE = "name";
    public static final String VIDEO_URL = "video_url";

    public static DownloadDialog newInstance(Bundle args) {
        DownloadDialog dialog = new DownloadDialog();
        dialog.setArguments(args);
        dialog.setStyle(1, 0);
        return dialog;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
        }
        return inflater.inflate(R.layout.dialog_url, container);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        EditText name = (EditText) view.findViewById(R.id.file_name);
        final TextView tCount = (TextView) view.findViewById(R.id.threads_count);
        SeekBar threads = (SeekBar) view.findViewById(R.id.threads);
        toolbar.setTitle((int) R.string.download_dialog_title);
        toolbar.setNavigationIcon((int) R.drawable.ic_arrow_back_black_24dp);
        toolbar.inflateMenu(R.menu.dialog_url);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DownloadDialog.this.getDialog().dismiss();
            }
        });
        threads.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                tCount.setText(String.valueOf(progress + 1));
            }

            public void onStartTrackingTouch(SeekBar p1) {
            }

            public void onStopTrackingTouch(SeekBar p1) {
            }
        });
        checkDownloadOptions();
        threads.setProgress(2);
        tCount.setText(String.valueOf(3));
        name.setText(createFileName(arguments.getString(TITLE)));
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() != R.id.okay) {
                    return false;
                }
                DownloadDialog.this.download();
                return true;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void checkDownloadOptions() {
        View view = getView();
        Bundle arguments = getArguments();
        RadioButton audioButton = (RadioButton) view.findViewById(R.id.audio_button);
        RadioButton videoButton = (RadioButton) view.findViewById(R.id.video_button);
        if (arguments.getString(AUDIO_URL) == null) {
            audioButton.setVisibility(8);
            videoButton.setChecked(true);
        } else if (arguments.getString("video_url") == null) {
            videoButton.setVisibility(8);
            audioButton.setChecked(true);
        }
    }

    private String createFileName(String fName) {
        List<String> forbiddenCharsPatterns = new ArrayList<>();
        forbiddenCharsPatterns.add("[:]+");
        forbiddenCharsPatterns.add("[\\*\"/\\\\\\[\\]\\:\\;\\|\\=\\,]+");
        forbiddenCharsPatterns.add("[^\\w\\d\\.]+");
        String nameToTest = fName;
        for (String pattern : forbiddenCharsPatterns) {
            nameToTest = nameToTest.replaceAll(pattern, "_");
        }
        return nameToTest;
    }

    /* access modifiers changed from: private */
    public void download() {
        String url;
        String location;
        String filename;
        View view = getView();
        Bundle arguments = getArguments();
        SeekBar threads = (SeekBar) view.findViewById(R.id.threads);
        RadioButton audioButton = (RadioButton) view.findViewById(R.id.audio_button);
        RadioButton radioButton = (RadioButton) view.findViewById(R.id.video_button);
        String fName = ((EditText) view.findViewById(R.id.file_name)).getText().toString().trim();
        boolean isAudio = audioButton.isChecked();
        if (isAudio) {
            url = arguments.getString(AUDIO_URL);
            location = NewSettings.getAudioDownloadPath(getContext());
            filename = fName + arguments.getString(FILE_SUFFIX_AUDIO);
        } else {
            url = arguments.getString("video_url");
            location = NewSettings.getVideoDownloadPath(getContext());
            filename = fName + arguments.getString(FILE_SUFFIX_VIDEO);
        }
        DownloadManagerService.startMission(getContext(), url, location, filename, isAudio, threads.getProgress() + 1);
        getDialog().dismiss();
    }

    private void download(String url, String title, String fileSuffix, File downloadDir, Context context) {
        File saveFilePath = new File(downloadDir, createFileName(title) + fileSuffix);
        Log.i(TAG, "Started downloading '" + url + "' => '" + saveFilePath + "' #" + 0);
        if (App.isUsingTor()) {
            FileDownloader.downloadFile(getContext(), url, saveFilePath, title);
            return;
        }
        Intent intent = new Intent(getContext(), DownloadActivity.class);
        intent.setAction(DownloadActivity.INTENT_DOWNLOAD);
        intent.setData(Uri.parse(url));
        intent.putExtra("fileName", createFileName(title) + fileSuffix);
        startActivity(intent);
    }
}
