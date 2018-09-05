package io.phizer.phizer;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Stack;


/**
 * Created by mv__ on 5/22/17.
 */

public class FileAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflator;
    private List<String> dataSource;
    private String mPath;
    private int numberOfFolders;
    public FileAdapter(Context context, List<String> items, String mPath) {
        mContext = context;
        this.inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sortList(items);
        dataSource = items;
        Log.d("test",dataSource.toString());
        this.mPath = mPath;

    }
    public void sortList(List<String> items) {
        numberOfFolders = 0;
        Stack<String> folderStack = new Stack<String>();
        for (int i = 0; i < items.size(); i++) {
            String fileName = items.get(i);
            if (!(fileName.length()>4&&fileName.substring(fileName.length()-4).equals(".jpg"))) {
                folderStack.push(items.remove(i));
                i--;
            }
        }
        numberOfFolders = folderStack.size();
        while (!folderStack.isEmpty()) {
            items.add(0, folderStack.pop());
        }

    }
    public int getCount() {
        return dataSource.size()+1;
    }

    public Object getItem(int position) {
        return dataSource.get(position);
    }
    public long getItemId(int position) {
        if (position == numberOfFolders) {
            // new folder position
            return 2;
        }
        if (position > numberOfFolders) {
            position--;
        }
        if (isImage(position)) {
            // is image
            return 1;
        } else {
            // is folder
            return 0;
        }
    }
    public int getNumberOfFolders() {
        return numberOfFolders;
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position==numberOfFolders){
            Button b = new Button(mContext);
            b.setFocusable(false);
            b.setClickable(false);
            b.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            b.setBackgroundResource(R.drawable.new_folder_button);
            b.setTextColor(Color.DKGRAY);
            b.setText("ADD NEW FOLDER");
            return b;
        }

        if (position > numberOfFolders) {
            position--;
        }
        if (isImage(position)) {
            ImageView imageView = new ImageView(mContext);
            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    500));
            //imageView.setAdjustViewBounds(true);
            if(position==dataSource.size()-1){
                imageView.setPadding(0, 0, 0, 40);
            }
            imageView.setClickable(false);
            Glide
                    .with(imageView.getContext())
                    .load(mPath+"/"+dataSource.get(position))
                    .centerCrop()
                    .into(imageView);
            imageView.setBackgroundColor(Color.WHITE);
            //imageView.setImageURI(Uri.parse(mPath+"/"+dataSource.get(position)));
            return imageView;
        } else {
            Button b = new Button(mContext);
            b.setFocusable(false);
            b.setClickable(false);
            b.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            b.setBackgroundColor(Color.rgb(255, 204, 153));
            b.setText(dataSource.get(position));
            b.setTextColor(Color.WHITE);
            b.setAllCaps(true);
            return b;
        }
    }
    private boolean isImage(int position) {
        if (position>numberOfFolders)
            position--;
        String fileName = dataSource.get(position);
        return (fileName.length()>4&&fileName.substring(fileName.length()-4).equals(".jpg"));
    }
}
