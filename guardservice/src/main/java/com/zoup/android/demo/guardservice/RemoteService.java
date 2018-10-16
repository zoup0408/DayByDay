package com.zoup.android.demo.guardservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by zoup on 2018/10/16
 * E-Mail：2479008771@qq.com
 */
public class RemoteService extends Service {

    private MyBilder mBilder;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this,"2")
                .setContentTitle("Remote Service")
                .setContentText("hi")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.animals_13)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.animals_13))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBilder == null)
            mBilder = new MyBilder();
        return mBilder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //在RemoteService运行后,我们对LocalService进行绑定。 把优先级提升为前台优先级
        this.bindService(new Intent(RemoteService.this, LocalService.class),
                connection, Context.BIND_IMPORTANT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private class MyBilder extends GuardAidl.Stub {

        @Override
        public String getName() throws RemoteException {
            return "RemoteService";
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        /**
         * 在终止后调用,我们在杀死服务的时候就会引起意外终止,就会调用onServiceDisconnected
         * 则我们就得里面启动被杀死的服务,然后进行绑定
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("TAG", "LocalService被杀死了");
            Intent remoteService = new Intent(RemoteService.this, LocalService.class);
            RemoteService.this.startService(remoteService);
            RemoteService.this.bindService(new Intent(RemoteService.this, LocalService.class),
                    connection, Context.BIND_IMPORTANT);
            Toast.makeText(RemoteService.this, "LocalService被杀死!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("TAG", "LocalService链接成功!");
            try {
                if (mBilder != null)
                    Toast.makeText(RemoteService.this, mBilder.getName(), Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

}
