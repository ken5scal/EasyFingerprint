package ken5scal.github.com.easyfingerprint;

import android.hardware.fingerprint.FingerprintManager;

public interface EasyFingerprintCallback {

    void authenticated(FingerprintManager.AuthenticationResult result);

    void authenticationFailed(CharSequence failString);

    void authenticationHelp(int helpMsgId, CharSequence helpString);

    void authenticationError(int errMsgId, CharSequence errString);

    void requireFingerprintRegistration();

    void requirePermission();

    void requireKeyguardSetting();
}
