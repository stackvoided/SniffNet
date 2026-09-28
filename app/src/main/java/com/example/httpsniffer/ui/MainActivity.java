package com.example.httpsniffer.ui;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.httpsniffer.databinding.ActivityMainBinding;
import com.example.httpsniffer.model.HttpRequestPacket;
import com.example.httpsniffer.repository.PacketRepository;
import com.example.httpsniffer.service.HttpSnifferVpnService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PacketAdapter adapter;
    private final List<HttpRequestPacket> packetList = new ArrayList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private boolean isServiceRunning = false;

    private final ActivityResultLauncher<Intent> vpnPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startVpnService();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUi();
        subscribePacketStream();
    }

    private void initUi() {
        adapter = new PacketAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.btnToggle.setOnClickListener(v -> {
            if (isServiceRunning) {
                stopVpnService();
            } else {
                prepareAndStartVpn();
            }
        });
    }

    private void prepareAndStartVpn() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            vpnPermissionLauncher.launch(intent);
        } else {
            startVpnService();
        }
    }

    private void startVpnService() {
        Intent intent = new Intent(this, HttpSnifferVpnService.class);
        startService(intent);
        isServiceRunning = true;
        binding.btnToggle.setText("Stop Sniffer");
    }

    private void stopVpnService() {
        Intent intent = new Intent(this, HttpSnifferVpnService.class);
        intent.setAction(HttpSnifferVpnService.ACTION_STOP_SERVICE);
        startService(intent);
        isServiceRunning = false;
        binding.btnToggle.setText("Start Sniffer");
    }

    private void subscribePacketStream() {
        disposables.add(
                PacketRepository.getInstance().getPacketStream()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(packet -> {
                            packetList.add(0, packet);
                            adapter.submitList(new ArrayList<>(packetList));
                            binding.recyclerView.scrollToPosition(0);
                        })
        );
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }
}
