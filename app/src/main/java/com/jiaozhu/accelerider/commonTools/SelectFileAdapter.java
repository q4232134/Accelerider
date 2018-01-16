package com.jiaozhu.accelerider.commonTools;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jiaozhu.accelerider.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by apple on 15/11/5.
 */
public class SelectFileAdapter extends RecyclerView.Adapter<SelectFileAdapter.ViewHolder> {
    private List<File> list;
    private List<File> selected = new ArrayList<>();
    private FileListListener fileListListener;

    public interface FileListListener {
        /**
         * 条目被按下
         *
         * @param position
         * @param file
         */
        void onItemClick(int position, File file, ViewHolder holder);
    }

    public SelectFileAdapter(List<File> fileModels) {
        list = fileModels;
    }

    public FileListListener getFileListListener() {
        return fileListListener;
    }

    public void setFileListListener(FileListListener fileListListener) {
        this.fileListListener = fileListListener;
    }

    /**
     * 获取选中的列表
     *
     * @return
     */
    public List<File> getSelectedSet() {
        return selected;
    }

    /**
     * 清除所有选中项目
     */
    public void clearSelect() {
        selected.clear();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_select_list, parent, false);
        final ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileListListener != null)
                    fileListListener.onItemClick(vh.getLayoutPosition(), list.get(vh.getLayoutPosition()), vh);
            }
        });
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final File model = list.get(position);
        holder.mTitle.setText(model.getName());
        if (!canSelect(position)) {
            holder.mCheckBox.setVisibility(View.GONE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);
            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(selected.contains(model));
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selected.clear();
                        selected.add(model);
                        notifyDataSetChanged();
                    } else {
                        selected.remove(model);
                    }
                }
            });
        }
    }

    boolean canSelect(int position) {
        return true;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTitle;
        public CheckBox mCheckBox;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mTitle = (TextView) v.findViewById(R.id.title);
            mCheckBox = (CheckBox) v.findViewById(R.id.checkbox);
        }
    }
}
