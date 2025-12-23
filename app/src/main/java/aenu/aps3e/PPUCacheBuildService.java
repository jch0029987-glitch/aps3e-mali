// SPDX-License-Identifier: WTFPL
package aenu.aps3e;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import java.util.ArrayList;

public class PPUCacheBuildService extends Service {

    public static final String CHANNEL_ID="PPUCacheBuildService";
    public static final int PPU_CACHE_BUILD_ID=0x66660000;
    public static final String EXTRA_META_INFO="meta_info";

    static final Object lock=new Object();

    static class PPUCacheBuildThread extends Thread{

        final ArrayList<Emulator.MetaInfo> ppu_cache_build_list=new ArrayList<>();
        final PPUCacheBuildService ctx;
        final NotificationManager nm;
        public PPUCacheBuildThread(PPUCacheBuildService ctx){
            this.ctx=ctx;
            nm=(NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        @Override
        public void run() {
            while ( true){
                if(ppu_cache_build_list.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                Emulator.MetaInfo meta_info=ppu_cache_build_list.get(0);
                update_notification("0 / "+ppu_cache_build_list.size(),meta_info.name);
                synchronized (lock){
                    if(meta_info.eboot_path!=null)
                        Emulator.get.precompile_ppu_cache(meta_info.eboot_path);
                    else
                        Emulator.get.precompile_ppu_cache(Utils.detach_open_uri(ctx,Uri.parse(meta_info.iso_uri)));
                    synchronized (PPUCacheBuildService.class) {
                        ppu_cache_build_list.remove(0);
                        if(ppu_cache_build_list.isEmpty()){
                            ServiceCompat.stopForeground(ctx,ServiceCompat.STOP_FOREGROUND_REMOVE);
                            //nm.cancel(id);
                        }
                    }
                }
            }
        }

        void update_notification(String title,String text){
            Notification notification=new NotificationCompat.Builder(ctx,CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.app_icon)
                    .build();
            nm.notify(PPU_CACHE_BUILD_ID,notification);
        }
    }

    static boolean isBuilding(Emulator.MetaInfo meta_info){
        synchronized (PPUCacheBuildService.class) {
            if(thread==null) return false;
            for(Emulator.MetaInfo info:thread.ppu_cache_build_list){
                if(info.iso_uri!=null && info.iso_uri.equals(meta_info.iso_uri))
                    return true;
                if(info.eboot_path!=null && info.eboot_path.equals(meta_info.eboot_path))
                    return true;
            }
            return false;
        }
    }

    static PPUCacheBuildThread thread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Emulator.MetaInfo meta_info;
        if((meta_info=(Emulator.MetaInfo)intent.getSerializableExtra(EXTRA_META_INFO))==null){
            stopSelf();
            return START_NOT_STICKY;
        }

        if(isBuilding(meta_info)){
            stopSelf();
            return START_NOT_STICKY;
        }

        if(thread==null)
            thread=new PPUCacheBuildThread(this);

        NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_ID,NotificationManager.IMPORTANCE_DEFAULT);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Emulator.MetaInfo first_meta_info;
        synchronized (PPUCacheBuildService.class) {
            thread.ppu_cache_build_list.add(meta_info);
            first_meta_info=thread.ppu_cache_build_list.get(0);
            if(!thread.isAlive()) thread.start();
        }
        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("0 / "+thread.ppu_cache_build_list.size())
                .setContentText(first_meta_info.name)
                .setSmallIcon(R.drawable.app_icon)
                .build();

        startForeground(PPU_CACHE_BUILD_ID,notification);

        return START_STICKY;
    }
}
