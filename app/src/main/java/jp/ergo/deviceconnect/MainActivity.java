package jp.ergo.deviceconnect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final Button hostButton = (Button)findViewById(R.id.hostButton);
        final Button guestButton = (Button)findViewById(R.id.guestButton);
        final Button connectButton = (Button)findViewById(R.id.connectButton);
        
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Host host = new Host(MainActivity.this);
                host.createReceiveUdpSocket();
                host.connect();
            }
        });
        final Guest guest = new Guest(MainActivity.this);

        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guest.sendBroadcast();
                guest.receivedHostIp();
            }
        });
        
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guest.connect();
            }
        });
        
    }
}
