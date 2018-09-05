package io.phizer.phizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.support.design.widget.BottomSheetDialog;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private int savedPosition=-1;
    private String mPath;
    private int numberOfFolders;
    private List<String> data;
    private Context mContext;
    private View mView;
   // private Button btn_cancel;
   // private BottomSheetDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent in = getIntent();
        Bundle extras = in.getExtras();
        mContext=this;
        if (extras!=null&&extras.containsKey("position")){
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

    public void init_modal_file_sheet() {

    }
    public void init(){

        TextView mytv = (TextView) findViewById(R.id.textview);
        mytv.setText(mPath.replace(getFilesDir().getPath(),"Home"));
        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPath=mPath.substring(0,mPath.lastIndexOf('/'));
                Intent i = new Intent(MainActivity.this,MainActivity.class);
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
        Button cam = (Button) findViewById(R.id.button5);
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,CameraActivity.class);
                i.putExtra("path",mPath);
                startActivity(i);
            }
        });

        data = (new ContentScraper(mPath)).getFiles();

        final ListView lv = (ListView) findViewById(R.id.listview);
        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.constraint);
        cl.setBackgroundColor(Color.WHITE);
        lv.setAdapter(new FileAdapter(this, data,mPath));
        numberOfFolders = ((FileAdapter)lv.getAdapter()).getNumberOfFolders();
        if (savedPosition>=0){
            lv.setSelection(savedPosition);
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(lv.getItemIdAtPosition(position)==0) {
                    mPath+="/"+lv.getItemAtPosition(position);
                    Intent i = new Intent(MainActivity.this,MainActivity.class);
                    i.putExtra("path",mPath);
                    startActivity(i);
                } else if (lv.getItemIdAtPosition(position)==1) {
                    /*FileAdapter adapt = (FileAdapter)lv.getAdapter();
                    numberOfFolders = adapt.getNumberOfFolders();*/
                    if (position > numberOfFolders) {
                        position--;
                    }
                    Intent i = new Intent(MainActivity.this, PhotoViewer.class);
                    //i.putExtra("name",((String)lv.getItemAtPosition(position)).substring(0,((String)lv.getItemAtPosition(position)).length()-4));
                    i.putExtra("name",(String)lv.getItemAtPosition(position));
                    i.putExtra("path",mPath);
                    i.putExtra("position",lv.getFirstVisiblePosition());
                    startActivity(i);
                } else {
                    Intent i = new Intent(MainActivity.this, AddNewFolder.class);
                    i.putExtra("path",mPath);
                    startActivity(i);
                }
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                test(position, (int) id);
                return true;
            }
        });
    }

    // THIS METHOD CONTROLS THE POPUP DIALOG
    public void test(int position, int id){
        if (position > numberOfFolders) {
            position--;
        }

        // if is image
        String fileName = data.get(position);
        if (id == 1) {
            showFilePopup(fileName);
        } else if (id == 0) {
            showFolderPopup(fileName);
        }

    }

    // File Options
    private void showFilePopup(final String fileName) {
        View modalbottomsheet = getLayoutInflater().inflate(R.layout.file_popup, null);
        final BottomSheetDialog dialog;
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(modalbottomsheet);
        dialog.setCanceledOnTouchOutside(true);
        //dialog.setCancelable(false);



        TextView fileLabel = (TextView) modalbottomsheet.findViewById(R.id.imageData);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date convertedDate = new Date();

        String adjustFileName = fileName.replace("IMG_","").replace(".jpg","");

        try {
            SimpleDateFormat newDateFormatter = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat newDateFormatter2 = new SimpleDateFormat("h:mm:ss a");
            String timeStamp = dateFormatter.format(convertedDate);
            convertedDate = dateFormatter.parse(adjustFileName);
            fileLabel.setText(newDateFormatter.format(convertedDate) + " at " +
                    newDateFormatter2.format(convertedDate));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Button btn_cancel = (Button) modalbottomsheet.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });

        Button btn_move = (Button) modalbottomsheet.findViewById(R.id.btn_move);
        btn_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveFile = new Intent(MainActivity.this,MoveFolder.class);
                moveFile.putExtra("currentPath", mPath+"/"+ fileName);
                moveFile.putExtra("isImage", "yes");
                startActivity(moveFile);
            }
        });

        Button btn_delete = (Button) modalbottomsheet.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(new File(mPath+'/'+fileName));
                dialog.cancel();
                init();
            }
        });

        Button btn_share = (Button) modalbottomsheet.findViewById(R.id.btn_share);
        btn_share.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateGDriveDirectories(mContext,mPath,null);
                dialog.hide();
                Toast.makeText(mContext,"Uploading to Google Drive",Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    // Folder Options
    private void showFolderPopup(final String fileName) {
        View modalbottomsheet = getLayoutInflater().inflate(R.layout.folder_popup, null);
        final BottomSheetDialog dialog;
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(modalbottomsheet);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);



        TextView folderLabel = (TextView) modalbottomsheet.findViewById(R.id.folderData);
        folderLabel.setText(fileName);



        Button btn_cancel = (Button) modalbottomsheet.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // write code on what to do if move button pressed
                dialog.hide();
            }
        });

        Button btn_move = (Button) modalbottomsheet.findViewById(R.id.btn_move);
        btn_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveFile = new Intent(MainActivity.this,MoveFolder.class);
                moveFile.putExtra("currentPath", mPath+"/"+ fileName);
                startActivity(moveFile);
            }
        });

        Button btn_delete = (Button) modalbottomsheet.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(new File(mPath+'/'+fileName));
                dialog.cancel();
                init();
            }
        });

        Button btn_share = (Button) modalbottomsheet.findViewById(R.id.btn_rename);
        btn_share.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dialog.show();
    }

    private void delete(File file){
        if (file.isDirectory()){
            for (File child : file.listFiles()){
                delete(child);
            }
        }
        file.delete();
    }
}



