package com.hse.dalexiv.vksignintest.model;

import junit.framework.ComparisonFailure;

import org.joda.time.DateTime;

/**
 * Created by dalex on 11/3/2015.
 */
public class Post implements Comparable<Post>{
    private String TAG = Post.class.getName();
    private int _id;

    private transient DateTime time;

    private String url;

    private String text;
    private int hours;

    private int mins;
    private String timeText;

    private String uriToImage;
    public Post(String time, String url, String text) {
        this.url = url;
        this.text = text;

        this.hours = Integer.parseInt(time.split(":")[0]);
        this.mins = Integer.parseInt(time.split(":")[1]);

        this.timeText = time;
        this.time = new DateTime(2015, 1, 1, hours, mins);
    }

    public Post(int _id, String text, String url, String timeText,
                int hours, int mins, String uriToImage) {
        this._id = _id;
        this.mins = mins;
        this.hours = hours;
        this.text = text;
        this.timeText = timeText;
        this.uriToImage = uriToImage;
        this.url = url;
    }

    public Post(DateTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return this.getTime()+ "\n" + text + "\n" + url + "\n";
    }

    public int getHours() {
        return hours;
    }

    public int getMins() {
        return mins;
    }

    public DateTime getTime() {
        if (time == null)
            this.time = new DateTime(2015, 1, 1, hours, mins);
        return time;
    }

    public String getText() {
        return text;
    }

    public String getTimeText() {
        return timeText;
    }

    public String getUriToImage() {
        return uriToImage;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int compareTo(Post another) {
        return this.getTime().compareTo(another.getTime());
    }
}
