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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by zoup on 2018/10/16
 * E-Mail：2479008771@qq.com
 */
public class LocalService extends Service {
    private LocalBinder localBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this,"1")
                .setContentTitle("Local Service")
                .setContentText("hello")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.animals_13)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.animals_13))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (localBinder == null) {
            localBinder = new LocalBinder();
        }
        return localBinder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //在LocalService运行后,我们对RemoteService进行绑定。 把优先级提升为前台优先级
        this.bindService(new Intent(LocalService.this, RemoteService.class),
                serviceConnection, Context.BIND_ABOVE_CLIENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (localBinder != null) {
                try {
                    Toast.makeText(LocalService.this, localBinder.getName(), Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("TAG", "RemoteService被杀死了");
            Intent localService = new Intent(LocalService.this, RemoteService.class);
            LocalService.this.startService(localService);
            LocalService.this.bindService(new Intent(LocalService.this, RemoteService.class),
                    serviceConnection, Context.BIND_ABOVE_CLIENT);
            Toast.makeText(LocalService.this, "RemoteService被杀死!", Toast.LENGTH_SHORT).show();
        }
    };

    private class LocalBinder extends GuardAidl.Stub {

        @Override
        public String getName() throws RemoteException {
            Intent localService = new Intent(LocalService.this, RemoteService.class);
            LocalService.this.startService(localService);
            LocalService.this.bindService(localService, serviceConnection, Context.BIND_IMPORTANT);
            return "LocalService";
        }
    }
}
