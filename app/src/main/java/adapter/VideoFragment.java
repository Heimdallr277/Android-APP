package adapter;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.java.mashihe.R;

public class VideoFragment extends Fragment {

    private VideoView videoView;
    private String url;

    public VideoFragment() {

    }

    public VideoFragment(String url) {
//        this.url = "https://vjs.zencdn.net/v/oceans.mp4";
//        this.url = "https://flv2.bn.netease.com/videolib1/1811/26/OqJAZ893T/HD/OqJAZ893T-mobile.mp4";
        this.url = url;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_video, container, false);
        view.setFocusable(false);
        videoView = view.findViewById(R.id.vv_video);
        videoView.requestFocus();
//        videoView.setVideoURI(Uri.parse(url));
        videoView.setVideoPath(url);
        MediaController mediaController = new MediaController(getContext());
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setOnCompletionListener(new MyPlayerOnCompletionListener());
        videoView.start();
        Log.d("VideoViewFragment", "加载完成："+url);
        return view;
    }

    class MyPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText(getContext(), "播放完成", Toast.LENGTH_SHORT).show();
        }
    }
}
