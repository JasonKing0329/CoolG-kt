package com.king.app.coolg_kt.model.palette;

import android.graphics.Bitmap;
import android.view.View;

import androidx.palette.graphics.Palette;

import com.king.app.coolg_kt.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 16:22
 */
public class BitmapRepository {

    public Observable<Palette> createPalette(final Bitmap resource) {
        return Observable.create(e -> Palette.from(resource)
                .generate(palette -> e.onNext(palette)));
    }

    public Observable<List<ViewColorBound>> createViewColorBound(List<View> viewList, final Bitmap resource) {
        if (viewList == null) {
            viewList = new ArrayList<>();
        }
        return Observable.fromIterable(viewList)
                .map(view -> {
                    int color = ColorUtil.averageImageColor(resource, view);
                    ViewColorBound bound = new ViewColorBound();
                    bound.view = view;
                    bound.color = ColorUtil.generateForgroundColorForBg(color);
                    return bound;
                })
                .toList()
                .toObservable();
    }

}
