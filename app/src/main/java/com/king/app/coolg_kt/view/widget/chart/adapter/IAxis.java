package com.king.app.coolg_kt.view.widget.chart.adapter;

/**
 * Desc:描述坐标轴，方向为从原点开始向外扩散
 * 原点position默认为0
 *
 * @author：Jing Yang
 * @date: 2018/9/30 15:27
 */
public interface IAxis {

    /**
     * 刻度总数
     * @return
     */
    int getDegreeCount();

    /**
     * 坐标轴总权值，描述坐标轴的总长度
     * @return
     */
    int getTotalWeight();

    /**
     * 描述刻度位置对应的权值，用于刻度定位
     * @param position
     * @return
     */
    int getWeightAt(int position);

    /**
     * 刻度描述
     * @param position
     * @return
     */
    String getTextAt(int position);

    /**
     * 是否绘制该刻度线及文字
     * @param position
     * @return
     */
    boolean isNotDraw(int position);
}
