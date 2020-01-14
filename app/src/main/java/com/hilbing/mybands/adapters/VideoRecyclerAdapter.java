package com.hilbing.mybands.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hilbing.mybands.R;
import com.hilbing.mybands.models.Item;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.VideoViewHolder> {
    private final List<Item> videos;

    public VideoRecyclerAdapter(List<Item> videos) {
        this.videos = videos;
    }

    public List<Item> getVideos() {
        return videos;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_video, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        holder.bind(videos.get(position));
    }

    @Override
    public int getItemCount() {
        return videos != null ? videos.size() : 0;
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.youtube_title)
        TextView title;
        @BindView(R.id.youtube_channel)
        TextView channel;
        @BindView(R.id.youtube_imageView)
        ImageView imageView;

        public VideoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        public void bind(Item item) {
            title.setText(item.getSnippet().getTitle());
            channel.setText(item.getSnippet().getChannelTitle());
            Picasso.get().load(item.getSnippet().getThumbnails().getHigh().getUrl()).into(imageView);
        }
    }
}
