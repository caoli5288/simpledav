package com.github.caoli5288.simpledav.fs.mongodb;

import com.github.caoli5288.simpledav.Utils;
import com.github.caoli5288.simpledav.fs.FileNode;
import com.github.caoli5288.simpledav.fs.FileType;
import com.github.caoli5288.simpledav.fs.IFileSystem;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class GridFileSystem implements IFileSystem {

    private final Map<String, GridFSBucket> buckets = new ConcurrentHashMap<>();
    private final String url;
    private final String dbName;
    private MongoDatabase db;

    @Override
    public void setup() {
        MongoClient mongoClient = MongoClients.create(url);
        db = mongoClient.getDatabase(dbName);
    }

    @Override
    @Nullable
    public List<FileNode> ls(String fullUrl) {
        // top levels
        if (fullUrl.equals("/")) {
            List<FileNode> nodes = new ArrayList<>();
            for (String s : db.listCollectionNames()) {
                if (s.endsWith(".files")) {
                    nodes.add(new FileNode("/" + s.substring(0, s.length() - 6), 0, new Date(), FileType.DIR));
                }
            }
            return nodes;
        }
        // others
        GridFilepath filepath = GridFilepath.extract(fullUrl);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = buckets.computeIfAbsent(filepath.getBucket(), s -> GridFSBuckets.create(db, s));
        if (fullUrl.endsWith("/")) {
            GridFSFindIterable files = fs.find(Filters.regex("filename", filepath.getFilename() + "[^/]+"));
            List<FileNode> list = new ArrayList<>();
            for (GridFSFile file : files) {
                list.add(toNode(filepath.getBucket(), file));
            }
            return list;
        } else {
            GridFSFile first = fs.find(Filters.eq("filename", filepath.getFilename())).first();
            if (first == null) {
                return null;
            }
            return Collections.singletonList(toNode(filepath.getBucket(), first));
        }
    }

    @Override
    @Nullable
    public InputStream cat(String fullUrl) {
        GridFilepath filepath = GridFilepath.extract(fullUrl);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = buckets.computeIfAbsent(filepath.getBucket(), s -> GridFSBuckets.create(db, s));
        GridFSFile first = fs.find(Filters.eq("filename", filepath.getFilename())).first();
        if (first == null) {
            return null;
        }
        return fs.openDownloadStream(first.getFilename());
    }

    @Override
    public void rm(String fullUrl) {
        GridFilepath filepath = GridFilepath.extract(fullUrl);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = buckets.computeIfAbsent(filepath.getBucket(), s -> GridFSBuckets.create(db, s));
        if (Utils.isNullOrEmpty(filepath.getFilename())) {
            fs.drop();
            return;
        }
        GridFSFile first = fs.find(Filters.eq("filename", filepath.getFilename())).first();
        if (first != null) {
            fs.delete(first.getObjectId());
        }
    }

    @Override
    public void put(String fullUrl, InputStream buf) {
        GridFilepath filepath = GridFilepath.extract(fullUrl);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = buckets.computeIfAbsent(filepath.getBucket(), s -> GridFSBuckets.create(db, s));
        GridFSFile first = fs.find(Filters.eq("filename", filepath.getFilename())).first();
        if (first != null) {
            fs.delete(first.getObjectId());
        }
        fs.uploadFromStream(filepath.getFilename(), buf);
    }

    @Override
    public void mkdir(String fullUrl) {
        GridFilepath filepath = GridFilepath.extract(fullUrl);
        Objects.requireNonNull(filepath);
        if (Utils.isNullOrEmpty(filepath.getFilename())) {
            db.createCollection(filepath.getBucket() + ".files");
        }
    }

    public static FileNode toNode(String namespace, GridFSFile file) {
        return new FileNode(String.format("/%s/%s", namespace, file.getFilename()), file.getLength(), file.getUploadDate(), FileType.FILE);
    }
}
