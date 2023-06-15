package com.aanda.mobilloscope;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SerialInputOutputManager.Listener {
    UsbManager manager;
    List<UsbSerialDriver> availableDrivers;
    UsbSerialDriver driver;
    UsbDeviceConnection connection;
    UsbSerialPort port;
    TextView ttv,sym;
    Button btnconect;
    SerialInputOutputManager smanager;
    ArrayList spinItem;
    Spinner spinview;
    Handler mainhand;
    String sdata = "woo";
    ImageButton but2;
    private Button connectbtn;
    private String idata,mdata,gra;
    private GraphView graph;
    private LinearLayout linear;
    private Boolean close = false;
    private ArrayList<HashMap<String,Object>> paired = new ArrayList<>();
    ArrayList deviceItem,modeItem;
    private Animation anim2,anim;
    ImageView addblue;
    private Spinner deviceSpinner,modeSpinner;
    private LineGraphSeries<DataPoint> mSeries;
    private int x = 0, minX=0, maxX=100,xTemp=100, minY=0;
    Double y = 0.0;
    BluetoothConnect bConnect;
    BluetoothConnect.BluetoothConnectionListener bConnectListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        mainhand = new Handler();
        linkResource();
        //but2.setBackground(getDrawable(R.drawable.arrow_for));
        graphSetup();
        spinnerSetup();

        connectbtn.setOnClickListener(view -> {
            switch (modeSpinner.getSelectedItemPosition()){
                case 0:
                    bConnect = new BluetoothConnect(this);
                    bConnect.getPairedDevices(paired);
                    String adres = deviceSpinner.getSelectedItem().toString();
                    bluetoothSetup(adres);
                    break;
                case 1:
                    otgSetup();
                    break;
            }
        });
        but2.setOnClickListener(view -> {
            if(close){
                linear.startAnimation(anim);
            }else {
                linear.startAnimation(anim2);
            }
        });
        anim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                close = true;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                close = false;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void otgSetup() {
        manager = (UsbManager)getSystemService(Context.USB_SERVICE);
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()){
            Toast.makeText(getApplicationContext(),"No Available Drivers",Toast.LENGTH_SHORT).show();
            return;
        }
        driver = availableDrivers.get(0);
        connection = manager.openDevice(driver.getDevice());
        if (connection==null){
            Toast.makeText(getApplicationContext(),"No Connection Available", Toast.LENGTH_SHORT).show();
            return;
        }
        port = driver.getPorts().get(0);

        try {
            port.open(connection);
            port.setParameters(2000000,8,UsbSerialPort.STOPBITS_1,UsbSerialPort.PARITY_NONE);
            smanager = new SerialInputOutputManager(port,this);
            smanager.start();
            // btnconect.setText("Connected!");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onNewData(byte[] data) {
       mainhand.post(() -> {
           mdata = new String(data);
           idata = mdata.replaceAll("[^0-9.]","");
           try {
               y = Double.parseDouble(idata);
           }catch (NumberFormatException e){}
           Viewport vport = graph.getViewport();

           mSeries.appendData(new DataPoint(x,y),false,100000);
           vport.setMaxY(8);
           x++;
           if(x == xTemp){
               xTemp = x;
               vport.setMinX(xTemp);
               xTemp=xTemp+100;
               vport.setMaxX(xTemp);
           }
       });
    }

    @Override
    public void onRunError(Exception e) {

    }
    private void graphSetup() {
        Viewport vport = graph.getViewport();
        vport.setXAxisBoundsManual(true);
        vport.setScrollable(true);
        vport.setScalable(true);
        vport.setScrollableY(true);
        vport.setMinX(minX);
        vport.setMaxX(maxX);
        vport.setMinY(minY);
        vport.setMaxY(8);
        graph.setBackgroundColor(getResources().getColor(R.color.black));
        GridLabelRenderer gridLabelRenderer = graph.getGridLabelRenderer();
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        mSeries.setColor(getResources().getColor(R.color.purple_700));
        graph.animate();
        gridLabelRenderer.setHorizontalLabelsColor(getResources().getColor(R.color.white));
        gridLabelRenderer.setVerticalLabelsColor(getResources().getColor(R.color.white));
        gridLabelRenderer.setGridColor(getResources().getColor(R.color.white));
    }
    private void linkResource() {
        linear = findViewById(R.id.linear);
        addblue = findViewById(R.id.refresh);
        but2 = findViewById(R.id.button1);
        connectbtn = findViewById(R.id.connectbtn);
        deviceSpinner = findViewById(R.id.device);
        modeSpinner = findViewById(R.id.mode);
        anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.back);
        anim2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.move);
        graph = findViewById(R.id.graph);
        //bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    private void spinnerSetup() {
        deviceItem = new ArrayList<>();
        modeItem = new ArrayList<>();
        modeItem.add("Bluetooth");
        modeItem.add("OTG");
        bConnect = new BluetoothConnect(this);
        bConnect.getPairedDevices(paired);
        for (HashMap<String,Object>dev : paired
        ) {
            deviceItem.add(dev.get("name"));
        }
        modeSpinner.setAdapter(new ArrayAdapter<String>(this,R.layout.spinlayout,modeItem));
        deviceSpinner.setAdapter(new ArrayAdapter<String>(this,R.layout.spinlayout,deviceItem));
    }
    private void bluetoothSetup(String devAddress) {
        bConnectListener = new BluetoothConnect.BluetoothConnectionListener() {
            @Override
            public void onConnected(String tag, HashMap<String, Object> deviceData) {
                connectbtn.setText("CONNECTED");

            }

            @Override
            public void onDataReceived(String tag, byte[] data, int bytes) {
                mdata = new String(data);
                idata = mdata.replaceAll("[^0-9.]+","");
                try {
                    y = Double.parseDouble(idata);
                }catch (NumberFormatException e){}

                mSeries.appendData(new DataPoint(x,y),true,100000);
                x++;


            }

            @Override
            public void onDataSent(String tag, byte[] data) {

            }

            @Override
            public void onConnectionError(String tag, String connectionState, String message) {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onConnectionStopped(String tag) {
                connectbtn.setText("DISCONNECTED");

            }
        };
        bConnect.startConnection(bConnectListener,paired.get(0).get("address").toString(),"yes");
        bConnect.readyConnection(bConnectListener,"yes");


    }
}