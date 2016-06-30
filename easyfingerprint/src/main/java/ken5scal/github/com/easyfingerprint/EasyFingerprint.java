package ken5scal.github.com.easyfingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.PermissionChecker;

/**
 * 指紋センサー
 * Created by suzuki on 16/06/19.
 */
@TargetApi(Build.VERSION_CODES.M)
public class EasyFingerprint extends FingerprintManager.AuthenticationCallback {
    private final Context context;
    private final EasyFingerprintCallback easyFingerprintCallback;
    private final FingerprintManager fingerprintManager;
    @VisibleForTesting
    boolean doesFingerprintSensorExist;
    @VisibleForTesting
    boolean isFingerprintRegistered;
    @VisibleForTesting
    boolean isKeyguardSecured;
    @VisibleForTesting
    boolean hasSelfCancelled;
    @VisibleForTesting
    CancellationSignal cancellationSignal;

    public EasyFingerprint(@NonNull Context context, @NonNull EasyFingerprintCallback easyFingerprintCallback) {
        this.context = context;
        this.easyFingerprintCallback = easyFingerprintCallback;
        this.fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        this.isKeyguardSecured = ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure();

        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
            this.doesFingerprintSensorExist = fingerprintManager.isHardwareDetected();
            this.isFingerprintRegistered = fingerprintManager.hasEnrolledFingerprints();
        } else {
            this.easyFingerprintCallback.requirePermission();
            this.doesFingerprintSensorExist = false;
            this.isFingerprintRegistered = false;
        }
    }

    @VisibleForTesting
    boolean isFingerprintAvailable() {
        if (!doesFingerprintSensorExist) {
            return false;
        } else if (!isFingerprintRegistered) {
            easyFingerprintCallback.requireFingerprintRegistration();
            return false;
        } else if (!isKeyguardSecured) {
            easyFingerprintCallback.requireKeyguardSetting();
            return false;
        } else {
            return true;
        }
    }

    public void startIdentifyingUser() {
        this.startReadingSensor(null);
    }

    public void startReadingSensor(@Nullable FingerprintManager.CryptoObject cryptoObject) {
        assert PermissionChecker.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
        if (isFingerprintAvailable()) {
            cancellationSignal = new CancellationSignal();
            hasSelfCancelled = false;
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0 /* flags */, this, null);
        }
    }

    public void stopReadingSensor() {
        if (cancellationSignal != null) {
            hasSelfCancelled = true;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!hasSelfCancelled) {
            easyFingerprintCallback.authenticationError(errMsgId, errString);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        easyFingerprintCallback.authenticationHelp(helpMsgId, helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        easyFingerprintCallback.authenticationFailed(context.getString(R.string.failed_reading_fingerprint));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        easyFingerprintCallback.authenticated(result);
    }
}
