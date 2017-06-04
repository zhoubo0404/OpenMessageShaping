package io.openmessaging.demo;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by yche on 5/27/17.
 */
public class NaiveDataReader implements Iterator<DefaultBytesMessage> {
    private final String storePath;

    // 1st-level all folders
    private ArrayList<File[]> folderFilesList;
    private int folderIndex;

    // 2nd-level one folder
    private File[] files;
    private int fileIndex;

    // 3rd-level one file
//    private final static int BUFFER_SIZE = 4 * 1024 * 1024;
    private BufferedReader bufferedReader;
    private String tmpBinString;

    public NaiveDataReader(String storePath) {
        this.storePath = storePath;
    }

    private void fetchNextFolder() {
        files = folderFilesList.get(folderIndex);
        fileIndex = 0;
    }

    private void fetchNextFile() throws FileNotFoundException {
        try {
            GZIPInputStream zip = new GZIPInputStream(new FileInputStream(files[fileIndex]));
            bufferedReader = new BufferedReader(new InputStreamReader(zip));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void attachNames(String queueName, Collection<String> topicNameList) {
        // folder_list
        ArrayList<String> folderNameList;
        folderNameList = new ArrayList<>(topicNameList.size() + 1);
        folderNameList.addAll(topicNameList);
        Collections.sort(folderNameList);

        folderNameList.add(queueName);

        // 1st-level: all folders
        folderFilesList = new ArrayList<>(folderNameList.size());
        folderNameList.forEach((folderString) -> {
            File[] files = new File(storePath + File.separator + folderString).listFiles(File::isFile);
            folderFilesList.add(files);
        });
        folderIndex = 0;

        // 2nd-level: one folder
        fetchNextFolder();

        // 3rd-level: one file
        try {
            fetchNextFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            tmpBinString = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextAndUpdateIteratorStates() throws IOException {
        tmpBinString = bufferedReader.readLine();
        // 3rd-level: reach end of file
        if (tmpBinString == null) {

            // 2nd-level: reach end of files in this folder
            fileIndex++;
            if (fileIndex >= files.length) {
                folderIndex++;

                // 1st-level: go through all the files already
                if (folderIndex >= folderFilesList.size()) {
                    return;
                }
                files = folderFilesList.get(folderIndex);
                fileIndex = 0;
            }

            fetchNextFile();
            tmpBinString = bufferedReader.readLine();
        }
    }

    @Override
    public boolean hasNext() {
        return folderIndex < folderFilesList.size();
    }

    @Override
    public DefaultBytesMessage next() {
        DefaultBytesMessage message = DefaultBytesMessage.valueOf(tmpBinString);
        try {
            nextAndUpdateIteratorStates();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }
}