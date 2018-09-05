package io.phizer.phizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

public class SelectFolderFileAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflator;
    private List<String> dataSource;
    private String mPath;
    private int length;

    public SelectFolderFileAdapter(Context context, List<String> items, String mPath) {
        mContext = context;
        this.inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sortList(items);
        dataSource = items;
        this.mPath = mPath;

    }
    public void sortList(List<String> items) {
        Stack<String> folderStack = new Stack<String>();
        for (int i = 0; i < items.size(); i++) {
            String fileName = items.get(i);
            if (!(fileName.length()>4&&fileName.substring(fileName.length()-4).equals(".jpg"))) {
                folderStack.push(items.remove(i));
                i--;
            }
        }
        length = folderStack.size();
        while (!folderStack.isEmpty()) {
            items.add(0, folderStack.pop());
        }

    }
    public int getCount() {
        Log.d("FUCK",""+length);
        return length;
    }

    public Object getItem(int position) {
        return dataSource.get(position);
    }
    public long getItemId(int position) {
        return 0;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
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