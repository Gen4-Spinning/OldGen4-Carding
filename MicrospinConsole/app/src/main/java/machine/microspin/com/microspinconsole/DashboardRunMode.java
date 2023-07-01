package machine.microspin.com.microspinconsole;


import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;

import java.io.UnsupportedEncodingException;

import machine.microspin.com.microspinconsole.entity.Packet;
import machine.microspin.com.microspinconsole.entity.Pattern;
import machine.microspin.com.microspinconsole.entity.Settings;
import machine.microspin.com.microspinconsole.entity.SettingsCommunicator;
import machine.microspin.com.microspinconsole.fragment.RunModeFragment;
import machine.microspin.com.microspinconsole.fragment.SettingsFragment;

@SuppressWarnings("ALL")
public class DashboardRunMode extends AppCompatActivity implements SettingsCommunicator, BluetoothService.OnBluetoothEventCallback, TabLayout.OnTabSelectedListener {

    public static final String TAG = "MicroSpin";
    private static String deviceAddress;
    public static Boolean isExpectingSettings = false;
    public static Boolean isInIdleMode = false;
    public static Boolean isWaitingForAck = false;
    public Boolean hasResetLengthinIdleMode = false;
    public TabLayout tabLayout;
    boolean doubleBackToExitPressedOnce = false;

    final private static String SETTINGS_REQ_PAYLOAD = "7E020B0101029900010002007E";

    private BluetoothService mService;
    private BluetoothWriter mWriter;

    private static Boolean isSecondConnectionTry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //******* Release Change v2
        if(Settings.device != null) {
            setTitle(Settings.device.getName());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(this);

        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        // ===== Request for Settings ======
        mWriter.writeln(SETTINGS_REQ_PAYLOAD.toUpperCase());
        isExpectingSettings = true;
    }

    //================================== BLUETOOTH EVENT CALLBACKS =================================
    @Override
    public void onDataRead(byte[] bytes, int length) {
        final RunModeFragment runFragment = (RunModeFragment) getSupportFragmentManager().getFragments().get(0);
        final SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager().getFragments().get(1);
        if (runFragment instanceof RunModeFragment) {
            try {
                String payload = new String(bytes, "UTF-8").replaceAll("(\\r|\\n)", "");
                if (!payload.isEmpty()) {
                    //=============================================================
                    String packetPayload;
                    switch (payload) {
                        case "XXRAMPUP":
                            packetPayload = "7E010001010102020301020383020204B0000200007E";
                            break;
                        case "XXNORMAL":
                            packetPayload = "7E010001010102010305044118000000020000000200007E";
                            break;
                        case "XXPAUSE1": //user press
                            packetPayload = "7E01130101010311030102000800020000000200007E";
                            break;
                        case "XXPAUSE2": // sliver cut - needs to be updated in embedded code
                            packetPayload = "7E01110102010311030102000900020000000200007E";
                            break;
                        case "XXHALT"://RPM error in cylinder motor, we are saying Overload? instead of rpm error
                            packetPayload = "7E010001010103120301020001020200020304000000007E";
                            break;
                        case "XXFEEDOVERLOAD": // feed Overload
                            packetPayload = "7E01110102010312030102000A00020000000200007E";
                            break;
                        case "XXLENGTHOVER": // target length reached
                            packetPayload = "7E01110102010312030102000B00020000000200007E";
                            break;
                        case "XXIDLE":
                            packetPayload = "7E01000101010100007E";
                            break;
                        case "XXCYLRUNNING":
                            packetPayload = "7E01000101010189007E";
                            break;
                        case "XXSETTING": // LAST 4 VALS ARE THE SAVED CURRENT LENGTH SETTINGS.
                            packetPayload = "7E0121010102040001102400FA4020000004DD4096666603203F99999A3F99999A01C200783F80000000FA7E";
                            break;
                        case "XXACK":
                            packetPayload = "7E010A010102049901000000007E";
                            break;
                        case "XXSAVED":
                            packetPayload = "7E010A010102048801000000007E";
                            break;
                        case "XXZEROACK":
                            packetPayload = "7E010A010102047701000000007E";
                            break;
                        default:
                            packetPayload = payload;
                    }

                    if (packetPayload.length() >= 20) { //size of header is 20 . Min Size of packet 20
                        if (Packet.getHeadersScreen(packetPayload).equals(Pattern.Screen.SETTING.name())) {
                            if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.ACK.name())) {
                                    Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_updated, Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();
                            }
                            else if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.SAVED.name())){
                                Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_saved, Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                            else if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.ZEROEDLENGTH.name())){
                                Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_zeroed, Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                            else if (Settings.processSettingsPacket(packetPayload)) {
                                    settingsFragment.updateContent();
                                    runFragment.savedLengthText.setText(Settings.getSavedLengthString());
                            }
                        } else {
                            //Update Run, Stop,Pause & Idle Mode Screen(s)
                            runFragment.updateContent(packetPayload);
                            int canEditSettings = runFragment.canEditSettings();
                            settingsFragment.isEditMode(canEditSettings);
                        }
                    }
                    //=================================================================
                }
            } catch (UnsupportedEncodingException | NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onStatusChange(BluetoothStatus bluetoothStatus) {
        if (bluetoothStatus == BluetoothStatus.NONE) {
            if (Settings.device != null) {
                mService.connect(Settings.device);
                /*isSecondConnectionTry = true;*/
            }
            /*if (isSecondConnectionTry) {
                Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_disconnected, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                isSecondConnectionTry = false;
            }*/
        }
    }

    @Override
    public void onDeviceName(String s) {

    }

    @Override
    public void onToast(String message) {
        Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void onDataWrite(byte[] bytes) {

    }

    //============================== FRAGMENT IMPLEMENTED FUNCTIONS ================================
    @Override
    public void onSettingsUpdate(String payload) {
        mWriter.writeln(payload.toUpperCase());
        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_updating, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null).show();
    }

    public boolean IdleLengthResetStatus(){
        return hasResetLengthinIdleMode;
    }

    @Override
    public void onViewChangeClick(View view) {
        switch (view.getId()) {
            case R.id.diagnoseBtn:
                startActivity(new Intent(DashboardRunMode.this, Diagnose.class));
                break;
            case R.id.resetLengthIdleBtn:
                mWriter.writeln(Pattern.RESET_LENGTH_LIMIT.toUpperCase());
                hasResetLengthinIdleMode = true;
                break;
            case R.id.resetLengthTop:
                mWriter.writeln(Pattern.RESET_LENGTH_LIMIT.toUpperCase());
                break;
        }
    }

    @Override
    public void updateIdleModeStatus(Boolean bol) {
        isInIdleMode = bol;
    }

    @Override
    public void raiseMessage(String Message) {
        if (Message.equals(getString(R.string.msg_halt_restart))) {
            Snackbar.make(getWindow().getDecorView().getRootView(), Message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_restart, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    }).show();
        } else {
            Snackbar.make(getWindow().getDecorView().getRootView(), Message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
    //==================================== CUSTOM FUNCTIONS ========================================


    //===================================== ACTIVITY EVENTS ========================================
    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.disconnect();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAndRemoveTask();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    //======================================= MENU EVENTS ==========================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_quit) {
            mService.disconnect();
            mService = null;
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //==================================== FRAGMENT SELECTION ======================================

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RunModeFragment();
                case 1:
                    return new SettingsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        // Get Tab Title(s)
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.label_status);
                case 1:
                    return getResources().getString(R.string.label_settings);
            }
            return null;
        }
    }

    //========================  TAB Events ===============================
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (isInIdleMode) {
            switch (tab.getPosition()) {
                case 1 :
                    mWriter.writeln(Pattern.DISABLE_MACHINE_START_SETTINGS.toUpperCase());
                    break;
                case 0:
                    mWriter.writeln(Pattern.ENABLE_MACHINE_START.toUpperCase());
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

}
