package ken5scal.github.com.easyfingerprintsample;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ken5scal.github.com.easyfingerprint.EasyFingerprint;
import ken5scal.github.com.easyfingerprint.EasyFingerprintCallback;

public class MainActivity extends AppCompatActivity implements EasyFingerprintCallback {

    private EasyFingerprint easyFingerprint;
    private static final String EF = "ef";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        easyFingerprint = new EasyFingerprint(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (easyFingerprint != null) {
            easyFingerprint.startIdentifyingUser();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (easyFingerprint != null) {
            easyFingerprint.stopReadingSensor();
        }
    }

    @Override
    public void authenticated(FingerprintManager.AuthenticationResult result) {
        Log.d(EF, "authenticated");
    }

    @Override
    public void authenticationFailed(CharSequence failString) {
        Log.d(EF, "auth failed");
    }

    @Override
    public void authenticationHelp(int helpMsgId, CharSequence helpString) {
        Log.d(EF, "auth help");
    }

    @Override
    public void authenticationError(int errMsgId, CharSequence errString) {
        Log.d(EF, "auth error");
    }

    @Override
    public void requireFingerprintRegistration() {
        Log.d(EF, "fingerpirnt registartion required");
    }

    @Override
    public void requirePermission() {
        Log.d(EF, "permission required");
    }

    @Override
    public void requireKeyguardSetting() {
        Log.d(EF, "keyguard setting required");
    }
}
