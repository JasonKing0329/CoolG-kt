package com.king.app.coolg_kt.view.widget.chart.adapter;

import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/10/18 10:58
 */
public class LineData {
    private int color;
    private int startX;
    private int endX;
    private List<String> valuesText;
    private List<Integer> values;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public List<String> getValuesText() {
        return valuesText;
    }

    public void setValuesText(List<String> valuesText) {
        this.valuesText = valuesText;
    }
}
