package com.king.app.coolg_kt.model.palette;

import androidx.palette.graphics.Palette;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/2 16:24
 */
public class PaletteUtil {

    public static Palette.Swatch getDefaultSwatch(Palette palette) {
        if (palette == null) {
            return null;
        }
        // vibrant first
        Palette.Swatch swatch = palette.getVibrantSwatch();
        if (swatch == null) {
            // muted second
            swatch = palette.getMutedSwatch();
            if (swatch == null) {
                // then random one or nothing
                List<Palette.Swatch> swatches = palette.getSwatches();
                if (!swatches.isEmpty()) {
                    swatch = swatches.get(0);
                }
            }
        }
        return swatch;
    }
}
