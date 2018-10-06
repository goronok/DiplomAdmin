package com.example.goron.diplomadmin.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.goron.diplomadmin.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    private RemoteMessage currentMessage;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getData().size() > 0){
            currentMessage = remoteMessage;
            handleMessage();
        }
    }

    // обработка полученного сообщения
    public void handleMessage(){
        int messageType = Integer.parseInt(currentMessage.getData().get("type"));

        switch (messageType){
            // тип сообщения 0 для теста
            case 0:
                sendNotification(
                        0,
                        createNotification(
                                currentMessage.getData().get("title"),
                                currentMessage.getData().get("body"),
                                null
                        )
                );
                break;
            default:
                break;
        }
    }

    // отправка уведомления
    private void sendNotification(int notificationId, NotificationCompat.Builder notificationBuilder){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, notificationBuilder.build());
    }

    // создание оповещения (можно и без него)
    // title - заголовок уведомления
    // body  - текст уведомления
    // notifyPendingIntent - для вызова активности/фрагмента при нажатии
    //                       null - если не требуется переход
    // !!! если нет notifyPendingIntent, может перейти на главную активность TODO: проверить !!!
    private NotificationCompat.Builder createNotification(String title, String body, PendingIntent notifyPendingIntent){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        if(notifyPendingIntent != null)
            builder.setContentIntent(notifyPendingIntent);
        return builder;
    }

    // создание интента для перехода на нужную активность/фрагмент при нажатии на уведомление
    // targetClass   - класс активности/фрагмента, который будем вызывать
    // extraDataKeys - набор ключей дополнительных данных, содержащихся в сообщении
    //                 null - если дополнительные данные не требуются (пример: изменение в расписании)
    private PendingIntent createIntent(Class targetClass, String[] extraDataKeys){
        // создание Intent для задания действия при нажатии на оповещение
        Intent notifyIntent = new Intent(this, targetClass);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // добавляем дополнительные данные в Intent, если такие были переданы
        // наличие таковых данных определяется по типу сообщения
        if(extraDataKeys != null){
            for (String key:extraDataKeys) {
                notifyIntent.putExtra(key, currentMessage.getData().get(key));
            }
        }

        // возвращаем PendingIntent, сформированный на базе notifyIntent
        // TODO: выяснить необходимость изменять requestCode
        return PendingIntent.getActivity(this, 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onNewToken(String s) {
        // TODO: send new token to server
        Log.d("CurrentToken", "new token: " + s);
    }
}
