package com.example.danesh.redditcards;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.cyanogenmod.launcher.home.api.cards.DataCard;
import org.cyanogenmod.launcher.home.api.receiver.CmHomeCardChangeReceiver;

public class CardReceiver extends CmHomeCardChangeReceiver {
    public static final String REQUEST_DELETE = "request_delete";
    public static final String DELETE_ID = "delete_id";
    @Override
    protected void onCardDeleted(Context context, DataCard.CardDeletedInfo cardDeletedInfo) {
        Intent i = new Intent(REQUEST_DELETE);
        i.setPackage(context.getPackageName());
        i.putExtra(DELETE_ID, cardDeletedInfo.getInternalId());
        context.sendBroadcast(i);
    }
}
