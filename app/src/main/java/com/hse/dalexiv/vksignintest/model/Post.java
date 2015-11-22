package com.hse.dalexiv.vksignintest.model;

import org.joda.time.DateTime;

/**
 * Created by dalex on 11/3/2015.
 */
public class Post implements Comparable<Post> {
    private String TAG = Post.class.getName();
    private int _id;

    private transient DateTime time;

    private String previewPicURL;
    private String fullPicURL;
    private String postURL;

    private String text;
    private int hours;

    private int mins;
    private String timeText;

    private String uriToImage;

    private int comments;
    private int likes;

    private Post(String previewPicURL, String fullPicURL, String postURL, String text, String time) {
        this.previewPicURL = previewPicURL;
        this.fullPicURL = fullPicURL;
        this.postURL = postURL;
        this.text = text;
        this.timeText = time;
    }

    public Post(String time, String postURL, String previewPicURL, String fullPicURL, String text,
                int likes, int comments) {
        this(previewPicURL, fullPicURL, postURL, text, time);

        this.hours = Integer.parseInt(time.split(":")[0]);
        this.mins = Integer.parseInt(time.split(":")[1]);
        this.time = new DateTime(2015, 1, 1, hours, mins);

        this.likes = likes;
        this.comments = comments;
    }

    public Post(int _id, String text, String postURL, String previewPicURL, String fullPicURL,
                String timeText, int hours, int mins, String uriToImage, int likes, int comments) {

        this(previewPicURL, fullPicURL, postURL, text, timeText);

        this._id = _id;
        this.mins = mins;
        this.hours = hours;


        this.uriToImage = uriToImage;
        this.previewPicURL = previewPicURL;
        this.likes = likes;
        this.comments = comments;
    }

    public Post(DateTime time) {
        this.time = time;
    }

    public static Post createCurrentTimePost() {
        DateTime curTime = new DateTime();
        DateTime timeToFind = new DateTime(2015, 1, 1,
                curTime.getHourOfDay(), curTime.getMinuteOfHour());

        return new Post(timeToFind);
    }

    @Override
    public String toString() {
        return this.getTime() + "\n" + text + "\n" + previewPicURL + "\n";
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

    public int getComments() {
        return comments;
    }

    public int getLikes() {
        return likes;
    }

    public String getUriToImage() {
        return uriToImage;
    }

    public void setUriToImage(String uriToImage) {
        this.uriToImage = uriToImage;
    }

    public String getPostURL() {
        return postURL;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Post) &&
            getTime().equals(((Post) o).getTime());

    }

    public String getFullPicURL() {
        return fullPicURL;
    }

    public String getPreviewPicURL() {
        return previewPicURL;
    }


    @Override
    public int compareTo(Post another) {
        return this.getTime().compareTo(another.getTime());
    }
}
