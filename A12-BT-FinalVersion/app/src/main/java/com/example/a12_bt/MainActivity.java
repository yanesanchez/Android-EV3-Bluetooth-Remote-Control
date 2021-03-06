package com.example.a12_bt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import java.io.InputStream;
import java.io.OutputStream;

import com.example.a12_bt.databinding.ActivityMainBinding;
import com.example.a12_bt.databinding.FragmentConnectBinding;
import com.example.a12_bt.databinding.FragmentSoundBinding;
import com.example.a12_bt.databinding.FragmentSensorBinding;
import com.example.a12_bt.databinding.FragmentDriveBinding;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity implements FragmentDataPassListener {
    private ActivityMainBinding binding;
    private FragmentConnectBinding connectBinding;
    private FragmentDriveBinding driveBinding;
    private FragmentSoundBinding soundBinding;
    private FragmentSensorBinding sensorBinding;

    // Data stream to/from NXT bluetooth
    private InputStream cv_is = null;
    private OutputStream cv_os = null;

    // BT Variables
    private final String CV_ROBOTNAME = "W";
    private BluetoothAdapter cv_btInterface = null;
    private Set<BluetoothDevice> cv_pairedDevices = null;
    private BluetoothDevice cv_btDevice = null;
    private BluetoothSocket cv_btSocket = null;

    // Fragment Variables
    DriveFragment fragDrive;
    ConnectFragment fragConnect;
    SensorFragment fragSensor;
    SoundFragment fragSound;

    static boolean isConnected = false;
    static boolean isPowerOn = false;


    private TabLayout cv_tabBar;

    /* onCreate START --------------------------------------- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        connectBinding = FragmentConnectBinding.inflate(getLayoutInflater());
        driveBinding = FragmentDriveBinding.inflate(getLayoutInflater());
        sensorBinding = FragmentSensorBinding.inflate(getLayoutInflater());
        soundBinding = FragmentSoundBinding.inflate(getLayoutInflater());

        // +++
        // Create fragments for Drive & Connect
        fragDrive = new DriveFragment();
        fragConnect = new ConnectFragment(MainActivity.this);
        fragSensor = new SensorFragment();
        fragSound = new SoundFragment();

        cv_tabBar = binding.tabLayout;

        // tab sync (2a) : tablayout.addOnTabSelectedListener -- tab changes page
        cv_tabBar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //viewPager2.setCurrentItem(tab.getPosition());
                setContentFragment(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        driveBinding.vvBtnFORWARD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragDrive.cpf_EV3MoveMotorFORWARD();
            }
        });
        driveBinding.vvBtnBACK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragDrive.cpf_EV3MoveMotorBACKWARD();
            }
        });
        driveBinding.vvBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragDrive.cpf_EV3MotorStart();
            }
        });
        driveBinding.vvBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragDrive.cpf_EV3MotorStop();
            }
        });

        connectBinding.vvRefreshBatteryLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragConnect.cpf_batteryLevel();
            }
        });

        soundBinding.vvBtnPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        setContentFragment(0);

    }
    /* onCreate END --------------------------------------- */

    // FRAGMENT METHODS =============
    public static void loadFragment(AppCompatActivity activity, int containerId, Fragment fragment, String tag) {
        activity.getSupportFragmentManager().beginTransaction().
                replace(containerId, fragment, tag).commitAllowingStateLoss();
    }

    public void setContentFragment(int id) {
        // id number corresponds to tab position
        switch (id) {
            case 0:
                loadFragment(this, R.id.vv_fragmentContainer, fragDrive, "DriveFragment");
                break;
            case 1:
                loadFragment(this, R.id.vv_fragmentContainer, fragSound, "SoundFragment");
                break;
            case 2:
                loadFragment(this, R.id.vv_fragmentContainer, fragSensor, "SensorFragment");
                break;
            case 3:
                loadFragment(this, R.id.vv_fragmentContainer, fragConnect, "ConnectFragment");
                break;
        }
    }

    @Override
    public void cf_firedByFragment(String str, int source) {
        switch (source) {
            case 0:
                setContentFragment(0);
                getSupportFragmentManager().executePendingTransactions();
                //fragDrive
                break;
            case 1:
                setContentFragment(1);
                getSupportFragmentManager().executePendingTransactions();
                // sound
                break;
            case 2:
                setContentFragment(2);
                getSupportFragmentManager().executePendingTransactions();
                // sensor
                break;
            case 3:
                setContentFragment(3);
                getSupportFragmentManager().executePendingTransactions();
                //fragConnect
                break;
        }

    }

    // MENU OPTIONS ==================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating menu by overriding inflate() method of MenuInflater class.
        //Inflating here means parsing layout XML to views.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //Overriding onOptionsItemSelected to perform event on menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_first:  cpf_requestBTPermissions();
                return true;
            case R.id.menu_second: cv_btDevice = cpf_locateInPairedBTList(CV_ROBOTNAME);
                return true;
            case R.id.menu_third: cpf_connectToEV3(cv_btDevice);
                return true;
            case R.id.menu_fourth: cpf_EV3MoveMotor();
                return true;
            case R.id.menu_fifth: cpf_EV3PlayTone();
                return true;
            case R.id.menu_sixth: cpf_disconnFromEV3(cv_btDevice);
                return true;
            case R.id.menu_seventh: fragConnect.cf_connectionStatus();/*cpf_disconnFromEV3(cv_btDevice);*/
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    //public void cf_connectionStatus() {};

    // --- MENU OPTION 4
    // Communication Developer Kit Page 27
    // 4.2.2 Start motor B & C forward at power 50 for 3 rotation and braking at destination
    private void cpf_EV3MoveMotor() {
        try {
            byte[] buffer = new byte[20];       // 0x12 command length

            buffer[0] = (byte) (20-2);
            buffer[1] = 0;

            buffer[2] = 34;
            buffer[3] = 12;

            buffer[4] = (byte) 0x80;

            buffer[5] = 0;
            buffer[6] = 0;

            buffer[7] = (byte) 0xae;
            buffer[8] = 0;

            buffer[9] = (byte) 0x06;

            buffer[10] = (byte) 0x81;
            buffer[11] = (byte) 0x32;

            buffer[12] = 0;

            buffer[13] = (byte) 0x82;
            buffer[14] = (byte) 0x84;
            buffer[15] = (byte) 0x03;

            buffer[16] = (byte) 0x82;
            buffer[17] = (byte) 0xB4;
            buffer[18] = (byte) 0x00;

            buffer[19] = 1;

            cv_os.write(buffer);
            cv_os.flush();
        }
        catch (Exception e) {
           driveBinding.vvTvStatusLabel.setText("Error in MoveForward(" + e.getMessage() + ")");
        }
    } // moved to DriveFragment

    // --- MENU OPTION 5
    // 4.2.5 Play a 1Kz tone at level 2 for 1 sec.

    private void cpf_EV3PlayTone() {
        // +++
        try {
            byte[] buffer = new byte[17];       // 0x12 command length

            buffer[0] = (byte) (17 - 2);
            buffer[1] = 0;

            buffer[2] = 34;
            buffer[3] = 12;

            buffer[4] = (byte) 0x80;

            buffer[5] = 0;
            buffer[6] = 0;

            buffer[7] = (byte) 0x94;
            buffer[8] = 1;

            buffer[9] = (byte) 0x81;

            buffer[10] = 2;
            buffer[11] = (byte) 0x82;

            buffer[12] = (byte) 0xE8;

            buffer[13] = 3;
            buffer[14] = (byte) 0x82;
            buffer[15] = (byte) 0xE8;

            buffer[16] = 3;

            cv_os.write(buffer);
            cv_os.flush();
        }
        catch (Exception e) {
            soundBinding.tvSoundStatus.setText("Error in PlayTone(" + e.getMessage() + ")");
        }
    }

    // ******** MOVED TO CONNECT-FRAGMENT
    // BLUETOOTH ===============================
    private void cpf_checkBTPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            connectBinding.vvTvOut2.setText("BLUETOOTH_SCAN already granted.\n");
        }
        else {
           connectBinding.vvTvOut2.setText("BLUETOOTH_SCAN NOT granted.\n");
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
           connectBinding.vvTvOut2.setText("BLUETOOTH_CONNECT NOT granted.\n");
        }
        else {
            connectBinding.vvTvOut2.setText("BLUETOOTH_CONNECT already granted.\n");
        }
    }

    // ALL MOVED TO CONNECT-FRAGMENT ===============
    // --- MENU OPTION 1
    // https://www.geeksforgeeks.org/android-how-to-request-permissions-in-android-application/
    private void cpf_requestBTPermissions() {
        // We can give any value but unique for each permission.
        final int BLUETOOTH_SCAN_CODE = 100;
        final int BLUETOOTH_CONNECT_CODE = 101;

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    BLUETOOTH_SCAN_CODE);
        }
        else {
            Toast.makeText(MainActivity.this,
                    "BLUETOOTH_SCAN already granted", Toast.LENGTH_SHORT) .show();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_CONNECT_CODE);
        }
        else {
            Toast.makeText(MainActivity.this,
                    "BLUETOOTH_CONNECT already granted", Toast.LENGTH_SHORT) .show();
        }
    }
    // ******** MOVED TO CONNECT-FRAGMENT
    // --- MENU OPTION 2
    // Modify from chap14, pp390 findRobot()
    private BluetoothDevice cpf_locateInPairedBTList(String name) {
        BluetoothDevice lv_bd = null;
        try {
            cv_btInterface = BluetoothAdapter.getDefaultAdapter();
            cv_pairedDevices = cv_btInterface.getBondedDevices();
            Iterator<BluetoothDevice> lv_it = cv_pairedDevices.iterator();
            while (lv_it.hasNext())  {
                lv_bd = lv_it.next();
                if (lv_bd.getName().equalsIgnoreCase(name)) {
                    //binding.vvTvOut1.setText(name + " is in paired list");
                    return lv_bd;
                }
            }
            connectBinding.vvTvOut2.setText(name + " is NOT in paired list");
        }
        catch (Exception e) {
            connectBinding.vvTvOut2.setText("Failed in findRobot() " + e.getMessage());
        }
        return null;
    }

    // ******** MOVED TO CONNECT-FRAGMENT
    // --- MENU OPTION 3
    // Modify from chap14, pp391 connectToRobot()
    private void cpf_connectToEV3(BluetoothDevice bd) {
        try  {
            cv_btSocket = bd.createRfcommSocketToServiceRecord
                    (UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            cv_btSocket.connect();
            connectBinding.vvTvOut2.setText("Connected to " + bd.getName() + " at " + bd.getAddress());
            connectBinding.vvBluetoothIcon.setImageResource(R.drawable.bluetooth_icon);
            connectBinding.tvPowerStatus.setText(R.string.powerStatusLabelOn);
            isPowerOn = true;
            isConnected = true;
        }
        catch (Exception e) {
            connectBinding.vvTvOut2.setText("Error interacting with remote device [" + e.getMessage() + "]");
            connectBinding.vvBluetoothIcon.setImageResource(R.drawable.bluetooth_icon_gray);
            isConnected = false;
        }
    }

    // ******** MOVED TO CONNECT-FRAGMENT
    // --- MENU OPTION 6

    private void cpf_disconnFromEV3(BluetoothDevice bd) {
        try {
            cv_btSocket.close();
            cv_is.close();
            cv_os.close();
            connectBinding.vvTvOut2.setText(bd.getName() + " is disconnect " );
        } catch (Exception e) {
            connectBinding.vvTvOut2.setText("Error in disconnect -> " + e.getMessage());
        }
    }

    //-------------------------------------


}