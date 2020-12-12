package com.king.app.coolg_kt.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.view.View;
import android.widget.ImageView;

import com.king.app.coolg_kt.model.bean.HsvColorBean;

import java.util.Random;

/**
 * @author JingYang
 * @version create time：2016-1-15 下午1:31:42
 *
 */
public class ColorUtil {

	public static String formatColor(int color) {
		String hexColor = "" + Integer.toHexString(color);
		//全不透明的可以去掉ff
		if (hexColor.length() == 8 && hexColor.startsWith("ff")) {
			hexColor = hexColor.substring(2);
		}
		//如果是类似00002233，前面的0全部会被截断
		if (color > 0 && hexColor.length() < 8) {
			StringBuffer buffer = new StringBuffer(hexColor);
			for (int i = 0; i < 8 - hexColor.length(); i ++) {
				buffer.insert(0, "0");
			}
			hexColor = buffer.toString();
		}
		return hexColor;
	}

	/**
	 * 深色背景白色文字，浅色背景#333333文字
	 * @param color
	 * @return
	 */
	public static int generateForgroundColorForBg(int color) {
		if (isDeepColor(color)) {
			return Color.WHITE;
		}
		else {
			return Color.parseColor("#666666");
		}
	}

	/**
	 * 判断是不是深色
	 *
	 * @return
	 */
	public static boolean isDeepColor(int color) {
//		double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
//		return darkness >= 0.5;
		return Color.red(color) * 0.299 + Color.green(color) * 0.578 + Color.blue(color) * 0.114 < 192;
	}

	/**
	 * 随机暗色，作为背景色，配合白色文字
	 * 利用hsv模型，s大于0.7, v大于0.5
	 * H是色彩,S是深浅,V是明暗
	 * 关于HSV模型可以参考http://blog.csdn.net/viewcode/article/details/8203728
	 *
	 * @return
	 */
	public static int randomWhiteTextBgColor() {
		Random random = new Random();
		float[] hsv = new float[3];
		hsv[0] = random.nextFloat() * 360;
		hsv[1] = random.nextFloat();
		if (hsv[1] < 0.7f) {
			hsv[1] = 0.7f + hsv[1] / 0.7f * 0.3f;
		}
		hsv[2] = random.nextFloat();
		if (hsv[2] < 0.5f) {
			hsv[2] += 0.5f;
		}

//        Log.d(TAG, "hsv[" + hsv[0] + "," + hsv[1] + "," + hsv[2] + "]");
		return Color.HSVToColor(hsv);
	}

	/**
	 * 如果s v随机，则随机暗色，作为背景色，配合白色文字
	 *
	 * @param bean
	 * @return
	 */
	public static int randomColorBy(HsvColorBean bean) {
		Random random = new Random();
		float[] hsv = new float[3];
		if (bean.getHStart() >= 0 && bean.getHArg() >= 0) {
			hsv[0] = bean.getHStart() + random.nextFloat() * bean.getHArg();
		} else {
			hsv[0] = random.nextFloat() * 360;
		}
		if (bean.getS() >= 0) {
			hsv[1] = bean.getS();
		} else {
			hsv[1] = random.nextFloat();
			if (bean.getType() == 1) {
				if (hsv[1] < 0.7f) {
					hsv[1] = 0.7f + hsv[1] * 0.3f;
				}
			} else if (bean.getType() == 2) {
				if (hsv[1] > 0.3f) {
					hsv[1] = 0.3f - hsv[1] * 0.3f;
				}
			}
		}
		if (bean.getV() >= 0) {
			hsv[2] = bean.getV();
		} else {
			hsv[2] = random.nextFloat();
			if (bean.getType() == 1) {
				if (hsv[2] < 0.5f) {
					hsv[2] += 0.5f;
				}
			} else if (bean.getType() == 2) {
				if (hsv[2] > 0.5f) {
					hsv[2] -= 0.5f;
				}
			}
		}
		return Color.HSVToColor(hsv);
	}

	public static void updateIconColor(ImageView icon, int color) {
		int red = (color & 0xff0000) >> 16;
		int green = (color & 0x00ff00) >> 8;
		int blue = (color & 0x0000ff);
		icon.setColorFilter(Color.argb(255, red, green, blue));
	}

	public static void updateIconColorWithAlpha(ImageView icon, int color) {
		int alpha = (color & 0xff000000) >> 24;
		int red = (color & 0xff0000) >> 16;
		int green = (color & 0x00ff00) >> 8;
		int blue = (color & 0x0000ff);
		setIconColor(icon, red, green, blue, alpha);
	}

	private static void setIconColor(ImageView icon, int r, int g, int b, int a) {
		float[] colorMatrix = new float[]{
				0, 0, 0, 0, r,
				0, 0, 0, 0, g,
				0, 0, 0, 0, b,
				0, 0, 0, (float) a / 255, 0
		};
		icon.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
	}

	/**
	 * 获取targetView所在bitmap区域的平均色值
	 * @param bitmap
	 * @param targetView
	 * @return
	 */
	public static int averageImageColor(Bitmap bitmap, View targetView) {
		int[] pixels = new int[targetView.getWidth() * targetView.getHeight()];
		int offsetX = targetView.getLeft();
		int offsetY = targetView.getTop();
		long red = 0;
		long green = 0;
		long blue = 0;
		for (int i = 0; i < targetView.getWidth(); i ++) {
			for (int j = 0; j < targetView.getHeight(); j ++) {
				int color = bitmap.getPixel(offsetX + i , offsetY + j);
				red += Color.red(color);
				green += Color.red(color);
				blue += Color.red(color);
			}
		}
		red = red / pixels.length;
		green = green / pixels.length;
		blue = blue / pixels.length;
		return (int) ((0xFF << 24) | (red << 16) | (green << 8) | blue);
	}
}
