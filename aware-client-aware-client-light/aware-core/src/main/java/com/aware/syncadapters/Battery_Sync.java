package com.aware.syncadapters;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aware.Aware;
import com.aware.providers.Applications_Provider;
import com.aware.providers.Aware_Provider;
import com.aware.providers.Battery_Provider;

/**
 * Created by denzil on 22/07/2017.
 */

public class Battery_Sync extends Service {
    private AwareSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Aware.TAG, "创造完成！BATTERY");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new AwareSyncAdapter(getApplicationContext(), true, true);

                Log.d(Aware.TAG, "Aware_Sync 打印 DATABASE_TABLES" + Battery_Provider.DATABASE_TABLES);
                for(int i = 0; i < Battery_Provider.DATABASE_TABLES.length; i++) {
                    Log.d(Aware.TAG, Battery_Provider.DATABASE_TABLES[i]);
                }

                Log.d(Aware.TAG, "Aware_Sync 打印 TABLES_FIELDS"+ Battery_Provider.TABLES_FIELDS.toString());
                for(int i = 0; i < Battery_Provider.TABLES_FIELDS.length; i++) {
                    Log.d(Aware.TAG, "新数据" + Battery_Provider.TABLES_FIELDS[i]);
                }

                Log.d(Aware.TAG, "A长度：" + Battery_Provider.TABLES_FIELDS.length);
                Log.d(Aware.TAG, "B长度：" + Battery_Provider.DATABASE_TABLES.length);



                sSyncAdapter.init(
                        Battery_Provider.DATABASE_TABLES,
                        Battery_Provider.TABLES_FIELDS,
                        new Uri[]{
                                Battery_Provider.Battery_Data.CONTENT_URI,
                                Battery_Provider.Battery_Discharges.CONTENT_URI,
                                Battery_Provider.Battery_Charges.CONTENT_URI
                        });
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
