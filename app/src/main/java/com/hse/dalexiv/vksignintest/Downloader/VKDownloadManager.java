package com.hse.dalexiv.vksignintest.Downloader;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.comms.IShow;
import com.hse.dalexiv.vksignintest.model.Post;
import com.hse.dalexiv.vksignintest.model.PostProcessor;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dalex on 11/3/2015.
 */
public class VKDownloadManager implements IShow {
    private final String TEST_ID = "50323156";
    String TAG = VKDownloadManager.class.toString();
    IShow callback;

    public VKDownloadManager(IShow callback) {
        this.callback = callback;
    }

    @Override
    public void show(String text, boolean isLong) {
        callback.show(text, isLong);
    }


    public String testRequest() {
        final String[] myResponse = new String[1];
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, TEST_ID));
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                myResponse[0] = response.json.toString();
            }
        });
        return myResponse[0];
    }

    private Post[] parseDate(VKResponse response) throws JSONException {
        List<Post> mPostInfo = new ArrayList<>();
        JSONObject parsedResponse = ((JSONObject) response.json.get("response"));
        JSONArray postsArray = (JSONArray) parsedResponse.get("items");
        int postNumber = Integer.parseInt(parsedResponse.get("count").toString());

        try {
            for (int i = 0; i < postNumber; ++i) {
                JSONObject post = (JSONObject) postsArray.get(i);
                String postText = post.get("text").toString();
                String time = PostProcessor.findTime(postText);

                if (time != null) {
                    JSONArray attach = (JSONArray) post.get("attachments");
                    try {
                        JSONObject photos = (JSONObject) ((JSONObject) attach.get(0)).get("photo");
                        String linkToPic = photos.get("photo_604").toString();
                        mPostInfo.add(new Post(time, linkToPic, postText));
                    } catch (JSONException e) {
                        if (e.getMessage().equals("No value for photo")) {
                            Log.i(TAG, e.getMessage());
                            continue;
                        }
                        throw e;
                    }

                }
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return mPostInfo.toArray(new Post[mPostInfo.size()]);
    }

    public Bundle downloadAllTimesAndLinks() {
        VKRequest main_req = VKApi.wall()
                .get(VKParameters.from(VKApiConst.OWNER_ID, "-92209938", VKApiConst.COUNT, "100"));
        final Bundle returnedBundle = new Bundle();
        main_req.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onError(VKError error) {
                super.onError(error);
                show(error.errorMessage, true);
            }

            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    Post[] testText = parseDate(response);
                    returnedBundle.putString("data", new Gson().toJson(testText));
                } catch (JSONException e) {
                    show(e.getMessage(), true);
                    Log.i(TAG, e.getMessage());
                } catch (NullPointerException e) {
                    show("Text was null, it's strange, aborting", true);
                    Log.i(TAG, e.getMessage());
                } catch (Exception e) {
                    show(e.getMessage(), true);
                    Log.i(TAG, e.getMessage());
                }
            }
        });
        return returnedBundle;
    }

    public void checkPermissions() {
        final VKRequest checkPermission = VKApi.groups().isMember(VKParameters.from(VKApiConst.GROUP_ID, "92209938"));
        checkPermission.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                JSONObject jsonResp = response.json;
                try {
                    if (jsonResp.get("response").toString().equals("0"))
                        show("You aren't member of this group, sry", true);
                    else if (jsonResp.get("response").toString().equals("1")) {
                        show("Everything is okay, stepping next", false);
                        downloadAllTimesAndLinks();
                    } else
                        throw new Exception("WRONG RESPONSE");
                } catch
                        (Exception e) {
                    show(e.getMessage(), true);
                }

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                show(error.errorMessage, true);
            }
        });
    }
}
