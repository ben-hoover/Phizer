package io.phizer.phizer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mv__ on 5/24/17.
 */

class ContentScraper {
    String path;
    public ContentScraper(String path){
        this.path=path;
    }
    public ArrayList<String> getFiles() {
        File file = new File(path);
        if (file.isDirectory()) {
            File directory = file;
            File[] files = directory.listFiles();
            ArrayList<String> fileList = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                fileList.add(i, files[i].getName());
            }
            return fileList;
        } else {
            return null;
        }
    }
}