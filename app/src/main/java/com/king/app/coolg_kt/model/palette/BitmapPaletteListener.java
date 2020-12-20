package com.king.app.coolg_kt.model.palette;

import android.graphics.Bitmap;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/2 15:53
 */
public abstract class BitmapPaletteListener implements RequestListener<Bitmap>, LifecycleObserver {

    private Disposable disposable;

    public BitmapPaletteListener(Lifecycle lifecycle) {
        lifecycle.addObserver(this);
    }

    @Override
    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
        new BitmapRepository().createPalette(resource)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Palette>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Palette palette) {
                        onPaletteCreated(palette);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return false;
    }

    protected abstract void onPaletteCreated(Palette palette);

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onLifeDestroy() {
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
