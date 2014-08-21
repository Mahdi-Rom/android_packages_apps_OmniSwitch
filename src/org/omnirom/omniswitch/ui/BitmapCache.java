/*
 *  Copyright (C) 2014 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omniswitch.ui;

import org.omnirom.omniswitch.PackageManager;
import org.omnirom.omniswitch.SwitchConfiguration;
import org.omnirom.omniswitch.TaskDescription;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

public class BitmapCache {

    private static BitmapCache sInstance;
    private Context mContext;
    private LruCache<String, Drawable> mMemoryCache;

    public static BitmapCache getInstance(Context context) {
        if (sInstance == null){
            sInstance = new BitmapCache();
        }
        sInstance.setContext(context);
        return sInstance;
    }

    private BitmapCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Drawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, Drawable bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                if (bitmap instanceof BitmapDrawable){
                    return ((BitmapDrawable)bitmap).getBitmap().getByteCount() / 1024;
                } else {
                    return 1;
                }
            }
        };
    }

    private void setContext(Context context) {
        mContext = context;
    }

    public void clear() {
        mMemoryCache.evictAll();
    }

    private String bitmapHash(String intent, int iconSize) {
        return intent + iconSize;
    }

    private String bitmapHash(String intent) {
        return intent;
    }

    private IconPackHelper getIconPackHelper() {
        return IconPackHelper.getInstance(mContext);
    }

    public Drawable getResized(Resources resources, PackageManager.PackageItem packageItem, SwitchConfiguration configuration, int size) {
        String key = bitmapHash(packageItem.getIntent(), size);
        Drawable d = getBitmapFromMemCache(key);
        if (d == null){
            Drawable icon = PackageManager.getInstance(mContext).getPackageIcon(packageItem);
            if (getIconPackHelper().isIconPackLoaded() && (getIconPackHelper()
                    .getResourceIdForActivityIcon(packageItem.getActivityInfo()) == 0)) {
                icon = BitmapUtils.compose(resources,
                        icon, mContext, getIconPackHelper().getIconBack(),
                        getIconPackHelper().getIconMask(), getIconPackHelper().getIconUpon(), getIconPackHelper().getIconScale());
            }
            d = BitmapUtils.resize(resources,
                    icon,
                    size,
                    configuration.mIconBorder,
                    configuration.mDensity);
            addBitmapToMemoryCache(key, d);
        }
        return d;
    }

    public Drawable getPackageIcon(Resources resources, PackageManager.PackageItem packageItem) {
        String key = bitmapHash(packageItem.getIntent());
        Drawable d = getBitmapFromMemCache(key);
        if (d == null){
            d = PackageManager.getInstance(mContext).getPackageIcon(packageItem);
            if (getIconPackHelper().isIconPackLoaded() && (getIconPackHelper()
                    .getResourceIdForActivityIcon(packageItem.getActivityInfo()) == 0)) {
                d = BitmapUtils.compose(resources,
                        d, mContext, getIconPackHelper().getIconBack(),
                        getIconPackHelper().getIconMask(), getIconPackHelper().getIconUpon(), getIconPackHelper().getIconScale());
            }
            addBitmapToMemoryCache(key, d);
        }
        return d;
    }

    public Drawable getResized(Resources resources, TaskDescription ad, Drawable icon, SwitchConfiguration configuration, int size) {
        String key = bitmapHash(ad.getIntent().toString(), size);
        Drawable d = getBitmapFromMemCache(key);
        if (d == null){
            if (getIconPackHelper().isIconPackLoaded() && (getIconPackHelper()
                    .getResourceIdForActivityIcon(ad.getActivityInfo()) == 0)) {
                icon = BitmapUtils.compose(resources,
                        icon, mContext, getIconPackHelper().getIconBack(),
                        getIconPackHelper().getIconMask(), getIconPackHelper().getIconUpon(), getIconPackHelper().getIconScale());
            }
            d = BitmapUtils.resize(resources,
                    icon,
                    size,
                    configuration.mIconBorder,
                    configuration.mDensity);
            addBitmapToMemoryCache(key, d);
        }
        return d;
    }

    public void addBitmapToMemoryCache(String key, Drawable bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Drawable getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
