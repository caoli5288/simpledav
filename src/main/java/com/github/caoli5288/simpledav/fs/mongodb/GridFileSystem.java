package com.github.caoli5288.simpledav.fs.mongodb;

import com.github.caoli5288.simpledav.Utils;
import com.github.caoli5288.simpledav.fs.FileNode;
import com.github.caoli5288.simpledav.fs.FileType;
import com.github.caoli5288.simpledav.fs.IFileSystem;
import com.mongodb.assertions.Assertions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
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
    public List<FileNode> ls(String path) throws IOException {
        // top levels
        if (path.equals("/")) {
            List<FileNode> nodes = new ArrayList<>();
            for (String s : db.listCollectionNames()) {
                if (s.endsWith(".files")) {
                    nodes.add(new FileNode("/" + s.substring(0, s.length() - 6), 0, new Date(), FileType.DIR));
                }
            }
            return nodes;
        }
        // others
        GridFilepath filepath = GridFilepath.extract(path);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = asGrid(filepath.getBucket());
        if (path.endsWith("/")) {
            GridFSFindIterable files = fs.find(Filters.regex("filename", filepath.getFilename() + "[^/]+"));
            List<FileNode> list = new ArrayList<>();
            for (GridFSFile file : files) {
                list.add(toNode(filepath.getBucket(), file));
            }
            return list;
        } else {
            GridFSFile first = fs.find(Filters.eq("filename", filepath.getFilename())).first();
            if (first == null) {
                throw new FileNotFoundException();
            }
            return Collections.singletonList(toNode(filepath.getBucket(), first));
        }
    }

    @Override
    public InputStream cat(String path) throws IOException {
        GridFilepath filepath = GridFilepath.extract(path);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = asGrid(filepath.getBucket());
        GridFSFile obj = fs.find(Filters.eq("filename", filepath.getFilename())).first();
        if (obj == null) {
            throw new FileNotFoundException();
        }
        return fs.openDownloadStream(obj.getFilename());
    }

    @Override
    public void rm(String path) {
        GridFilepath filepath = GridFilepath.extract(path);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = asGrid(filepath.getBucket());
        if (Utils.isNullOrEmpty(filepath.getFilename())) {
            fs.drop();
            return;
        }
        GridFSFile first = fs.find(Filters.eq("filename", filepath.getFilename())).first();
        if (first != null) {//Silent
            fs.delete(first.getObjectId());
        }
    }

    @Override
    public void put(String path, InputStream buf) {
        GridFilepath filepath = GridFilepath.extract(path);
        Objects.requireNonNull(filepath);
        GridFSBucket fs = asGrid(filepath.getBucket());
        GridFSFile first = fs.find(Filters.eq("filename", filepath.getFilename())).first();
        if (first != null) {
            fs.delete(first.getObjectId());
        }
        fs.uploadFromStream(filepath.getFilename(), buf);
    }

    @Override
    public void mkdir(String path) {
        GridFilepath filepath = GridFilepath.extract(path);
        Objects.requireNonNull(filepath);
        if (Utils.isNullOrEmpty(filepath.getFilename())) {
            db.createCollection(filepath.getBucket() + ".files");
        }
    }

    @Override
    public void mv(String source, String des, boolean force) throws IOException {
        GridFilepath fpSrc = GridFilepath.extract(source);
        Objects.requireNonNull(fpSrc);
        // check buckets
        GridFilepath fp2 = GridFilepath.extract(des);
        Objects.requireNonNull(fp2);
        Assertions.isTrue("Cross buckets", fpSrc.getBucket().equals(fp2.getBucket()));
        // check src exists
        GridFSBucket fs = asGrid(fpSrc.getBucket());
        GridFSFile objSrc = fs.find(Filters.eq("filename", fpSrc.getFilename())).first();
        if (objSrc == null) {
            throw new FileNotFoundException();
        }
        // check des file
        GridFSFile obj2 = fs.find(Filters.eq("filename", fp2.getFilename())).first();
        if (obj2 != null) {
            Assertions.isTrue("Overwrite", force);
            fs.delete(obj2.getObjectId());// So delete old first
        }
        fs.rename(objSrc.getObjectId(), fp2.getFilename());
    }

    @NotNull
    private GridFSBucket asGrid(String name) {
        return buckets.computeIfAbsent(name, s -> GridFSBuckets.create(db, s));
    }

    public static FileNode toNode(String namespace, GridFSFile file) {
        return new FileNode(String.format("/%s/%s", namespace, file.getFilename()), file.getLength(), file.getUploadDate(), FileType.FILE);
    }
}
