package machine.microspin.com.microspinconsole.entity;

import android.view.View;

/**
 * Interface to implement Fragment <-> Activity Communication.
 */

public interface SettingsCommunicator {

    void onSettingsUpdate (String payload);
    void onViewChangeClick(View view);
    void updateIdleModeStatus(Boolean bol);
    void raiseMessage(String Message);
    boolean IdleLengthResetStatus();

}
