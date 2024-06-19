package com.example.omrifit.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaItem {
    public enum MediaType { IMAGE, VIDEO }

    private String url; // Media's url
    private MediaType type; // Media type: image or video

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    int id;

    // Constructor
    public MediaItem(String url, MediaType type) {
        this.url = url;
        this.type = type;
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            this.id = Integer.parseInt(matcher.group());
        } else {
            System.out.println("No numbers found in the string."+matcher.group());
        }
    }


    // Getters
    public String getUrl() {
        return url;
    }

    public MediaType getType() {
        return type;
    }

    // Setters
    public void setUrl(String url) {
        this.url = url;
    }

    public void setType(MediaType type) {
        this.type = type;
    }
}
