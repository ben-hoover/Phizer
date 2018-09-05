package io.phizer.phizer;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.phizer.phizer.R;

public class MoveFolder extends AppCompatActivity {
    private boolean image;
    private Bundle extras;
    private int savedPosition=-1;
    private String mPath;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_folder);
        Intent in = getIntent();
        extras = in.getExtras();
        image = extras.getBoolean("isImage");
        if(image) {
            Log.d("isStillImage", "yeah still true");
        } else {
            Log.d("isStillImage", "naw shit's fucked");
        }
        if (extras.containsKey("position")){
            savedPosition=extras.getInt("position");
        }
        if (extras!=null&&extras.containsKey("path")&&!extras.getString("path").equals("Home")){
            mPath=extras.getString("path");
        } else {
            mPath=getFilesDir().getPath();
        }
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    public void onSelect(View view) {
        String fromPath = extras.getString("currentPath");
        String fileName = fromPath.substring(fromPath.lastIndexOf("/"));
        if(!image) {
            File file = new File(fromPath);
            Log.d("fromFile", file.getAbsolutePath());
            File dir = new File(mPath + "/" + fileName);
            dir.mkdir();
            File toFile = new File(dir.getAbsolutePath());
            Log.d("toFile", dir.getAbsolutePath());
            Log.d("image", "no");
            file.renameTo(toFile);
        } else {
            File file = new File(fromPath+".jpg");
            Log.d("fromFile", file.getAbsolutePath());
            File toFile = new File(mPath+"//");
            toFile.mkdir();
            Log.d("toFile", toFile.toString());
            Log.d("image", "yes");
            Log.d("fileWritable", String.valueOf(toFile.canWrite()));
            try {
                boolean renamed = file.renameTo(toFile);
                Log.d("fileWorked", String.valueOf(renamed));
                Log.d("finalFile", file.getPath());
                Log.d("null?", "naw shit works");
            } catch (NullPointerException n) {
                Log.d("null?", "yeah");
            } catch (SecurityException s) {
                Log.d("security", "no security");
            }
        }
        ContentScraper c = new ContentScraper(mPath);
        ArrayList<String> files = c.getFiles();
        Log.d("files", files.toString());
        Intent exitMoveFolder = new Intent(MoveFolder.this,MainActivity.class);
        exitMoveFolder.putExtra("path", mPath);
        startActivity(exitMoveFolder);
    }

    public void init(){
        TextView mytv = (TextView) findViewById(R.id.textview);
        mytv.setText(mPath.replace(getFilesDir().getPath(),"Home"));
        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPath=mPath.substring(0,mPath.lastIndexOf('/'));
                Intent i = new Intent(MoveFolder.this,MoveFolder.class);
                i.putExtra("path",mPath);
                startActivity(i);
            }
        });
        if(mytv.getText().equals("Home")){
            b.setVisibility(View.INVISIBLE);
        } else {
            mytv.setText(((String)mytv.getText()).replace("Home/",""));
            b.setVisibility(View.VISIBLE);
        }


        List<String> data = (new ContentScraper(mPath)).getFiles();

        final ListView lv = (ListView) findViewById(R.id.listview);
        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.constraint);
        cl.setBackgroundColor(Color.WHITE);
        lv.setAdapter(new SelectFolderFileAdapter(this, data,mPath));
        if (savedPosition>=0){
            lv.setSelection(savedPosition);
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                mPath+="/"+lv.getItemAtPosition(position);
                Intent i = new Intent(MoveFolder.this,MoveFolder.class);
                i.putExtra("path",mPath);
                i.putExtra("isImage", image);
                i.putExtra("currentPath", extras.getString("currentPath"));
                startActivity(i);

            }
        });
    }
}