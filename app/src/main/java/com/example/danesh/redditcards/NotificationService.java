package com.example.danesh.redditcards;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import org.cyanogenmod.launcher.home.api.cards.DataCard;
import org.cyanogenmod.launcher.home.api.cards.DataCardImage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

public class NotificationService extends NotificationListenerService {
    public NotificationService() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(CardReceiver.REQUEST_DELETE);
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                cancelNotification();
//            }
//        }, filter);
    }


    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        CharSequence notificationTitle = null;

        if (!notification.extras.containsKey(Notification.EXTRA_TITLE_BIG)) {
            // Regular notification style
            notificationTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        } else {
            // Big view notification style (BigText,BigPicture,Inbox)
            notificationTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
        }

        if (TextUtils.isEmpty(notificationTitle)) {
            return;
        }

        CharSequence message = null;
        if (notification.extras.containsKey(Notification.EXTRA_TEXT)) {
            message = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        } else {
            message = TextUtils.join(",", notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES));
        }
        System.out.println("Message " + message);
        if (TextUtils.isEmpty(message)) {
            return;
        }

        DataCard card = null;
        for (DataCard publishedCard : DataCard.getAllPublishedDataCards(this)) {
            if (TextUtils.equals(publishedCard.getInternalId(), getIdForNotification(statusBarNotification))) {
                card = publishedCard;
                card.setTitle(notificationTitle.toString());
                card.setContentCreatedDate(new Date(notification.when));
                break;
            }
        }
        if (card == null) {
            card = new DataCard(notificationTitle.toString(), new Date(notification.when));
        }
        card.setBodyText(String.valueOf(message));
        if (notification.extras.containsKey(Notification.EXTRA_PICTURE)) {
            DataCardImage cardImage = new DataCardImage(card);
            cardImage.setImage((android.graphics.Bitmap) notification.extras.getParcelable(Notification.EXTRA_PICTURE));
            card.addDataCardImage(cardImage);
            card.setBodyText(null);
        }
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(statusBarNotification.getPackageName(), 0);
            if(appInfo.icon != 0) {
                Uri uri = Uri.parse("android.resource://" + statusBarNotification.getPackageName() + "/" + appInfo.icon);
                card.setContentSourceImage(uri);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (notification.extras.containsKey(Notification.EXTRA_LARGE_ICON_BIG)) {
            card.setAvatarImage((android.graphics.Bitmap) notification.extras.getParcelable(Notification.EXTRA_LARGE_ICON_BIG));
        } else if (notification.extras.containsKey(Notification.EXTRA_LARGE_ICON)) {
            card.setAvatarImage((android.graphics.Bitmap) notification.extras.getParcelable(Notification.EXTRA_LARGE_ICON));
        } else if (notification.extras.containsKey(Notification.EXTRA_SMALL_ICON)) {
            try {
                Resources res = createPackageContext(statusBarNotification.getPackageName(), 0).getResources();
                Bitmap b = BitmapFactory.decodeResource(res, notification.extras.getInt(Notification.EXTRA_SMALL_ICON));
                card.setAvatarImage(b);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            Method m = notification.contentIntent.getClass().getMethod("getIntent", null);
            card.setCardClickIntent((Intent) m.invoke(notification.contentIntent), false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        card.setInternalId(getIdForNotification(statusBarNotification));
        card.publish(this);
    }

    private String getIdForNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getPackageName() 
               + "/" + statusBarNotification.getId();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        for (DataCard cards : DataCard.getAllPublishedDataCards(this)) {
            if (cards.getInternalId() != null) {
                if (cards.getInternalId().equals(getIdForNotification(statusBarNotification))) {
                    cards.unpublish(this);
                    break;
                }
            }
        }
    }
}
