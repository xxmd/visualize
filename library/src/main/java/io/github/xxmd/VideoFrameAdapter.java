package io.github.xxmd;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoFrameAdapter extends RecyclerView.Adapter<VideoFrameAdapter.ViewHolder> {
    private final List<Bitmap> bitmapList;

    public VideoFrameAdapter(List<Bitmap> bitmapList) {
        this.bitmapList = bitmapList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageBitmap(bitmapList.get(position));
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_video_frame);
        }
    }
}
