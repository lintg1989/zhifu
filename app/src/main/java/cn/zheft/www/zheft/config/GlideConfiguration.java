package cn.zheft.www.zheft.config;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

/**
 * 用于修改Glide加载高质量图
 * 需要在AndroidManifest.xml中将GlideModule定义为meta-data
 * <meta-data android:name="cn.zheft.www.zheft.config.GlideConfiguration" android:value="GlideModule"/>
 */
public class GlideConfiguration implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888)
                .setDiskCache(new InternalCacheDiskCacheFactory(context, 30 * 1024 * 1024));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        // register ModelLoaders here.
    }
}
