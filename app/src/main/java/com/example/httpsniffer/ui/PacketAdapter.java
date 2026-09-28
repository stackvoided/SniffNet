package com.example.httpsniffer.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.httpsniffer.databinding.ItemPacketBinding;
import com.example.httpsniffer.model.HttpRequestPacket;

import java.util.Locale;

public class PacketAdapter extends ListAdapter<HttpRequestPacket, PacketAdapter.PacketViewHolder> {

    protected PacketAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<HttpRequestPacket> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull HttpRequestPacket oldItem, @NonNull HttpRequestPacket newItem) {
                    return oldItem.getTimestamp() == newItem.getTimestamp()
                            && oldItem.getSourcePort() == newItem.getSourcePort();
                }

                @Override
                public boolean areContentsTheSame(@NonNull HttpRequestPacket oldItem, @NonNull HttpRequestPacket newItem) {
                    return oldItem.getUri().equals(newItem.getUri())
                            && oldItem.getHost().equals(newItem.getHost());
                }
            };

    @NonNull
    @Override
    public PacketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPacketBinding binding = ItemPacketBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new PacketViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PacketViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class PacketViewHolder extends RecyclerView.ViewHolder {
        private final ItemPacketBinding binding;

        PacketViewHolder(ItemPacketBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HttpRequestPacket item) {
            binding.tvMethod.setText(item.getMethod());
            binding.tvHost.setText(item.getHost());
            binding.tvUri.setText(item.getUri());
            binding.tvEndpoints.setText(
                    String.format(Locale.US, "%s:%d -> %s:%d",
                            item.getSourceIp(), item.getSourcePort(),
                            item.getDestinationIp(), item.getDestinationPort())
            );
        }
    }
}
