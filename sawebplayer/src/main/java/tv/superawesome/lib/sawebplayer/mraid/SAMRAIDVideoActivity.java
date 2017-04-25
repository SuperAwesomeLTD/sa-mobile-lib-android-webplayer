package tv.superawesome.lib.sawebplayer.mraid;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import tv.superawesome.lib.sautils.SAImageUtils;
import tv.superawesome.lib.sautils.SAUtils;

public class SAMRAIDVideoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        String link  = bundle.getString("link_url");
        Log.d("SuperAwesome-X", link);

        VideoView videoView = new VideoView(this);
        videoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        Uri video = Uri.parse(link);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(video);
        videoView.start();

        float fp = SAUtils.getScaleFactor(this);
        ImageButton closeButton = new ImageButton(this);
        closeButton.setImageBitmap(SAImageUtils.createCloseButtonBitmap());
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setPadding(0, 0, 0, 0);
        closeButton.setScaleType(ImageView.ScaleType.FIT_XY);
        RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams((int) (30 * fp), (int) (30* fp));
        buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        closeButton.setLayoutParams(buttonLayout);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SAMRAIDVideoActivity.super.onBackPressed();
            }
        });

        RelativeLayout parent = new RelativeLayout(this);
        parent.setId(SAUtils.randomNumberBetween(1000000, 1500000));
        parent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        parent.addView(videoView);
        parent.addView(closeButton);

        setContentView(parent);

    }


}
