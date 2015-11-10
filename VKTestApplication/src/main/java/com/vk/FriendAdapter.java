package com.vk;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.Sub.Profile;
import com.vk.vktestapp.R;

import java.util.List;

/**
 * Created by Артем on 07.11.2015.
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<Profile> items;
    private int itemLayout;

    public FriendAdapter(List<Profile> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            public void onPotato(View caller) {
                Toast.makeText(v.getContext(), "view", Toast.LENGTH_SHORT).show();

            }

            public void onTomato(ImageView callerImage) {
                Toast.makeText(v.getContext(), "image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void longPressed(View view) {
                Toast.makeText(v.getContext(), "long", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Profile item = items.get(position);
        holder.text.setText(item.name);
        // holder.image.setImageBitmap(null);
        holder.online.setImageResource(!item.online ? R.drawable.abc_btn_radio_material : R.drawable.abc_btn_radio_to_on_mtrl_015);
        //   Picasso.with(holder.image.getContext()).cancelRequest(holder.image);
        //   Picasso.with(holder.image.getContext()).load(item.getImage()).into(holder.image);
        //   holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView image;
        public TextView text;
        public ImageView online;
        public IMyViewHolderClicks mListener;
        public IMyViewHolderClicks mLongListener;

        public ViewHolder(View itemView, IMyViewHolderClicks listener) {
            super(itemView);
            mListener = mLongListener = listener;
            image = (ImageView) itemView.findViewById(R.id.micro_avatar);
            text = (TextView) itemView.findViewById(R.id.name);
            online = (ImageView) itemView.findViewById(R.id.online);

            itemView.setOnClickListener(this);
            online.setOnClickListener(this);
            image.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView) {
                mListener.onTomato((ImageView) v);
            } else {
                mListener.onPotato(v);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            mLongListener.longPressed(v);
            return true;
        }

        public static interface IMyViewHolderClicks {
            public void onPotato(View caller);

            public void onTomato(ImageView callerImage);

            public void longPressed(View view);
        }
    }
}