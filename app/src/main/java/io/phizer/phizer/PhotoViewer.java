package io.phizer.phizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.github.chrisbanes.photoview.PhotoView;

public class PhotoViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        final Context mContext = this;
        Intent in = getIntent();
        final Bundle b = in.getExtras();
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativelayout);
        rl.setBackgroundColor(Color.BLACK);
        final Button butt = (Button) findViewById(R.id.button2);
        butt.setText("<");
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PhotoViewer.this,MainActivity.class);
                i.putExtra("path",b.getString("path"));
                i.putExtra("position",b.getInt("position"));
                Animation an = AnimationUtils.loadAnimation(mContext, R.anim.slide_to_right);
                butt.startAnimation(an);
                startActivity(i);
            }
        });
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setImageURI(Uri.parse(b.getString("path")+"/"+b.getString("name")));
    }
}
