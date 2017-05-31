package io.openmessaging.demo;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yche on 5/31/17.
 */
public class DataFileIndexer implements Serializable {
    public int INIT_TOPIC_NUMBER = 100;
    public int MAX_MINI_CHUNK_NUMBER_PER_TOPIC = 20;
    public int TOPIC_CHUNK_SIZE = 80 * 1024 * 1024; // 80 MB
    public int MINI_CHUNK_SIZE = 4 * 1024 * 1024; // 4MB

    public String[] topicNames = new String[INIT_TOPIC_NUMBER];
    public ConcurrentHashMap<String, Integer> topicNameToNumber = new ConcurrentHashMap<>(INIT_TOPIC_NUMBER);

    public long[] topicOffsets = new long[INIT_TOPIC_NUMBER];
    public int[] topicMiniChunkNumber = new int[INIT_TOPIC_NUMBER];
    public int[][] topicMiniChunkLengths;

    public long currentGlobalDataOffset = 0;
    public int currentTopicNumber = 0;
    public ReentrantLock assignLock = new ReentrantLock();

    public DataFileIndexer() {
        topicNameToNumber.clear();
        topicMiniChunkLengths = new int[INIT_TOPIC_NUMBER][];
        for (int i = 0; i < INIT_TOPIC_NUMBER; i++) {
            topicOffsets[i] = 0L;
            topicMiniChunkNumber[i] = 0;
            topicMiniChunkLengths[i] = new int[MAX_MINI_CHUNK_NUMBER_PER_TOPIC];
        }
    }

    private void assignNumberToTopic(String topicName) {
        assignLock.lock();
        if (!topicNameToNumber.containsKey(topicName)) {
            topicNameToNumber.put(topicName, currentTopicNumber);
            topicOffsets[currentTopicNumber] = currentGlobalDataOffset;
            currentGlobalDataOffset += TOPIC_CHUNK_SIZE;
            topicNames[currentTopicNumber] = topicName;
            currentTopicNumber++;
        }
        assignLock.unlock();
    }

    int getAssignedTopicNumber(String topicName) {
        if (!topicNameToNumber.containsKey(topicName)) {
            assignNumberToTopic(topicName);
        }
        return topicNameToNumber.get(topicName);
    }
}
