package com.king.app.coolg_kt.page.pub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.view.widget.flow.BaseFlowAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述: 规格、做法适配器
 * <p/>作者：景阳
 * <p/>创建时间: 2017/12/20 19:32
 */
public abstract class BaseTagAdapter<T> extends BaseFlowAdapter<T> implements View.OnClickListener {

    /**
     * 多选记录
     */
    private Map<Long, Boolean> mCheckMap;

    /**
     * 已选择的数据列表
     */
    private List<T> selectedMethods;

    /**
     * 启用多选
     */
    private boolean isMultiSelect;

    /**
     * 启用反选（多选情况下肯定支持反选，单选情况下由该字段决定）
     */
    private boolean isEnableUnselect;

    /**
     * 当前被选中的view
     */
    private View lastSelectView;

    /**
     * 当前被选中的位置
     */
    private int selection = -1;

    private OnItemSelectListener<T> onItemSelectListener;

    private OnItemLongCickListener<T> onItemLongClickListener;

    private int layoutResource = -1;

    public BaseTagAdapter() {
        mCheckMap = new HashMap<>();
    }

    public void setLayoutResource(int layoutResource) {
        this.layoutResource = layoutResource;
    }

    private int getInflateResource() {
        int layout;
        if (layoutResource == -1) {
            layout = R.layout.adapter_star_tag_item_pad;
        }
        else {
            layout = layoutResource;
        }
        return layout;
    }

    @Override
    public View getView(int position, ViewGroup group) {
        View view = LayoutInflater.from(group.getContext()).inflate(getInflateResource(), null);
        TextView textView = view.findViewById(R.id.tv_name);
        textView.setText(getText(data.get(position)));
        textView.setOnClickListener(this);
        textView.setOnLongClickListener(view1 -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onLongClickItem(data.get(position));
            }
            return true;
        });
        textView.setTag(position);
        // 不可选
        if (isDisabled(data.get(position))) {
            textView.setEnabled(false);
        }
        else {
            textView.setEnabled(true);
            // 选中状态
            if (isMultiSelect) {
                textView.setSelected(mCheckMap.get(getId(data.get(position))));
            }
            else {
                if (position == selection) {
                    lastSelectView = textView;
                    textView.setSelected(true);
                }
                else {
                    textView.setSelected(false);
                }
            }
        }
        return view;
    }

    protected abstract String getText(T data);

    protected abstract long getId(T data);

    protected abstract boolean isDisabled(T item);

    @Override
    public void setData(List<T> data) {
        super.setData(data);
        mCheckMap.clear();
        if (getCount() > 0) {
            for (T bean:data) {
                mCheckMap.put(getId(bean), false);
            }
        }
    }

    /**
     * 设置当前被选中位置
     * @param selection
     */
    public void setSelection(int selection) {
        this.selection = selection;
    }

    /**
     * 启用多选
     */
    public void enableMultiSelect() {
        isMultiSelect = true;
    }

    /**
     * 启用反选
     */
    public void enableUnselect() {
        this.isEnableUnselect = true;
    }

    /**
     * 初始化已选item
     * @param selectedMethods
     */
    public void setSelectedList(List<T> selectedMethods) {
        this.selectedMethods = selectedMethods;
        if (selectedMethods != null && selectedMethods.size() > 0) {
            if (isMultiSelect) {
                for (T bean:selectedMethods) {
                    mCheckMap.put(getId(bean), true);
                }
            }
            else {
                for (int i = 0; i < data.size(); i ++) {
                    if (getId(selectedMethods.get(0)) == getId(data.get(i))) {
                        selection = i;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        // 不可选
        if (isDisabled(data.get(position))) {
            return;
        }
        if (isMultiSelect) {
            boolean targetCheck = !mCheckMap.get(getId(data.get(position)));
            mCheckMap.put(getId(data.get(position)), targetCheck);
            v.setSelected(targetCheck);

            // 多选必定支持反选
            if (onItemSelectListener != null) {
                if (targetCheck) {
                    onItemSelectListener.onSelectItem(data.get(position));
                }
                else {
                    onItemSelectListener.onUnSelectItem(data.get(position));
                }
            }
        }
        else {

            mCheckMap.clear();

            // 点击的已选中item
            if (position == selection) {
                // 反选取消选中
                if (isEnableUnselect) {
                    selection = -1;
                    if (lastSelectView != null) {
                        lastSelectView.setSelected(false);
                    }
                    if (onItemSelectListener != null) {
                        onItemSelectListener.onUnSelectItem(data.get(position));
                    }
                }
                // 重复点击不改变选中状态
                else {
                    mCheckMap.put(getId(data.get(position)), true);
                }
                return;
            }
            else {
                selection = position;
                if (lastSelectView != null) {
                    lastSelectView.setSelected(false);
                }
                lastSelectView = v;
                mCheckMap.put(getId(data.get(position)), true);
                v.setSelected(true);
            }

            if (onItemSelectListener != null) {
                onItemSelectListener.onSelectItem(data.get(position));
            }
        }
    }

    public void setOnItemSelectListener(OnItemSelectListener<T> onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    public void setOnItemLongClickListener(OnItemLongCickListener<T> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 已选择item
     * @return
     */
    public List<T> getSelectedItems() {
        List<T> list = new ArrayList<>();
        if (data != null) {
            for (T bean:data) {
                Boolean check = mCheckMap.get(getId(bean));
                if (check != null && check) {
                    list.add(bean);
                }
            }
        }
        return list;
    }

    /**
     * 已选择item
     * @return
     */
    public T getSelectedItem() {
        if (selection >= 0 && selection < getCount()) {
            return data.get(selection);
        }
        return null;
    }

    public interface OnItemSelectListener<T> {
        void onSelectItem(T data);

        void onUnSelectItem(T t);
    }

    public interface OnItemLongCickListener<T> {
        void onLongClickItem(T data);
    }
}
