package com.xpacer.journalapp.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Map;

@Entity(tableName = "gists")
public class GistEntry {

    @Ignore
    public GistEntry(String caption, String content, Date createdAt) {
        this.caption = caption;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    @Ignore
    public GistEntry(Map<String, Object> gist) {
        this.id = (String) gist.get("id");
        this.caption = (String) gist.get("caption");
        this.content = (String) gist.get("content");
        this.createdAt = (Date) gist.get("createdAt");
        this.updatedAt = (Date) gist.get("updatedAt");
    }


    public GistEntry(@NonNull String id, String caption, String content, Date createdAt, Date updatedAt) {
        this.id = id;
        this.caption = caption;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    @PrimaryKey
    private String id;

    private String caption;

    private String content;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
