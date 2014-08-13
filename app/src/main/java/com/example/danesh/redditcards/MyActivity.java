package com.example.danesh.redditcards;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.cyanogenmod.launcher.home.api.cards.CardData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }

    public void refresh(View v) {
        v.setEnabled(false);
        Ion.with(this).load("http://www.reddit.com/r/pics/.json?sort=new&limit=20").asJsonObject().setCallback(new FutureCallback<com.google.gson.JsonObject>() {
            @Override
            public void onCompleted(Exception e, com.google.gson.JsonObject jsonObject) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                long lastTime = prefs.getLong("last_time", 0);
                int cardCount = 0;
                if (jsonObject != null) {
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    if (data != null) {
                        JsonArray children = data.getAsJsonArray("children");
                        boolean timeSaved = false;
                        if (children != null) {
                            for (JsonElement object : children) {
                                if (cardCount >= 10) {
                                    break;
                                }
                                JsonObject childData = object.getAsJsonObject().getAsJsonObject("data");
                                if (childData != null) {
                                    JsonPrimitive title = childData.getAsJsonPrimitive("title");
                                    JsonPrimitive url = childData.getAsJsonPrimitive("url");
                                    JsonPrimitive permaLink = childData.getAsJsonPrimitive("permalink");
                                    JsonPrimitive created = childData.getAsJsonPrimitive("created");
                                    if (!url.getAsString().contains("jpg")) {
                                        continue;
                                    }
                                    long createdTime = created.getAsLong();
                                    if (createdTime <= lastTime) {
                                        break;
                                    }
                                    if (!timeSaved) {
                                        prefs.edit().putLong("last_time", createdTime).apply();
                                        timeSaved = true;
                                    }
                                    CardData card = new CardData(title.getAsString(), new Date(created.getAsLong()));
                                    card.addCardDataImage(Uri.parse(url.getAsString()));
                                    card.setContentSourceImage(Uri.parse("https://openmedia.ca/sites/openmedia.ca/files/reddit-logo2.png"));
                                    card.setAvatarImage(card.getContentSourceImageUri());
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com" + permaLink.getAsString()));
                                    card.setCardClickIntent(intent, false);
                                    card.publish(getApplicationContext());
                                    cardCount++;
                                }
                            }
                        }
                    }
                }
                finish();
            }
        });
    }
}
