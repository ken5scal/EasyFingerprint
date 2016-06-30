package ken5scal.github.com.easyfingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.content.PermissionChecker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import static android.os.Build.VERSION_CODES.M;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PermissionChecker.class})
public class EasyFingerprintTest {

    private Context mockedContext;
    @Mock
    EasyFingerprintCallback mockedCallback;
    @Mock
    KeyguardManager mockedKeyguardManager;
    @Mock
    FingerprintManager mockedFingerprintManager;

    private EasyFingerprint easyFingerprint;

    @TargetApi(Build.VERSION_CODES.M)
    @Before
    public void setUp() throws Exception {
        assumeApiLevel(M);
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(PermissionChecker.class);
        BDDMockito.given(PermissionChecker.checkSelfPermission(any(Context.class), anyString())).willReturn(PackageManager.PERMISSION_GRANTED);
        mockedContext = Mockito.mock(Context.class);
        when(mockedContext.getSystemService(Context.KEYGUARD_SERVICE)).thenReturn(mockedKeyguardManager);
        when(mockedContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockedFingerprintManager);
        easyFingerprint = new EasyFingerprint(mockedContext, mockedCallback);
        resetFingerprintAvailability();
    }

    @Test
    public void isAvailableIfThingsAreReady() {
        assertThat(easyFingerprint.isFingerprintAvailable(), is(true));
        resetFingerprintAvailability();
    }

    @Test
    public void isNotAvailableIfNoSensorExists() {
        easyFingerprint.doesFingerprintSensorExist = false;
        assertThat(easyFingerprint.isFingerprintAvailable(), is(not(true)));
        resetFingerprintAvailability();
    }

    @Test
    public void isNotAvailableIfFingerprintNotRegistered() {
        easyFingerprint.isFingerprintRegistered = false;
        assertThat(easyFingerprint.isFingerprintAvailable(), is(not(true)));
        verify(mockedCallback).requireFingerprintRegistration();
        resetFingerprintAvailability();
    }

    @Test
    public void isNotAvailableIfKeyguardIsNotSet() {
        easyFingerprint.isKeyguardSecured = false;
        assertThat(easyFingerprint.isFingerprintAvailable(), is(not(true)));
        verify(mockedCallback).requireKeyguardSetting();
        resetFingerprintAvailability();
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void whenAuthenticationSucceeded_callback_is_called() throws NoSuchPaddingException, NoSuchAlgorithmException {
        FingerprintManager.AuthenticationResult result = notNull(FingerprintManager.AuthenticationResult.class);
        easyFingerprint.onAuthenticationSucceeded(result);
        verify(mockedCallback).authenticated(result);
    }

    @Test
    public void whenAuthenticationFailed() {
        String failMessage = mockedContext.getString(R.string.failed_reading_fingerprint);
        easyFingerprint.onAuthenticationFailed();
        verify(mockedCallback).authenticationFailed(failMessage);
    }

    @Test
    public void whenAuthenticationError() {
        int errMsgId = 0;
        CharSequence errString = "hogehoge";

        easyFingerprint.hasSelfCancelled = false;
        easyFingerprint.onAuthenticationError(errMsgId, errString);
        verify(mockedCallback).authenticationError(errMsgId, errString);
    }

    @Test
    public void whenAuthenticationErrorInvokedBySelfCancellation() {
        int errMsgId = 0;
        CharSequence errString = "hogehoge";

        easyFingerprint.hasSelfCancelled = true;
        easyFingerprint.onAuthenticationError(errMsgId, errString);
        verify(mockedCallback, never()).authenticationError(errMsgId, errString);
    }

    @Test
    public void whenAuthenticationNeedsHelp() {
        int helpMsgId = 0;
        CharSequence helpString = "hogehoge";
        easyFingerprint.onAuthenticationHelp(helpMsgId, helpString);
        verify(mockedCallback).authenticationHelp(helpMsgId, helpString);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void testStartIdentifyingUser() throws NoSuchPaddingException, NoSuchAlgorithmException {
        resetFingerprintAvailability();

        assertThat(checkSelfPermission(mockedContext, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED, is(true));
        easyFingerprint.startIdentifyingUser();
        verify(mockedFingerprintManager).authenticate(null, easyFingerprint.cancellationSignal, 0, easyFingerprint, null);
        assertThat(easyFingerprint.cancellationSignal, notNullValue());
        assertThat(easyFingerprint.hasSelfCancelled, is(not(true)));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void testStopReadingSensorWithNonNullCancellationSignal() {
        easyFingerprint.cancellationSignal = notNull(CancellationSignal.class);
        easyFingerprint.stopReadingSensor();
        assertThat(easyFingerprint.cancellationSignal, nullValue());
        assertThat(easyFingerprint.hasSelfCancelled, is(not(true)));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void testStopReadingSensorWithCancellationSignal() {
        boolean previousFlag = easyFingerprint.hasSelfCancelled;
        easyFingerprint.cancellationSignal = isNull(CancellationSignal.class);
        easyFingerprint.stopReadingSensor();
        assertThat(easyFingerprint.hasSelfCancelled, is(previousFlag));
    }

    private void assumeApiLevel(int apiLevel) throws Exception {
        // Adjust the value of Build.VERSION.SDK_INT statically using reflection
        Field sdkIntField = Build.VERSION.class.getDeclaredField("SDK_INT");
        sdkIntField.setAccessible(true);

        // Temporarily remove the SDK_INT's "final" modifier
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(sdkIntField, sdkIntField.getModifiers() & ~Modifier.FINAL);

        // Update the SDK_INT value, re-finalize the field, and lock it again
        sdkIntField.set(null, apiLevel);
        modifiersField.setInt(sdkIntField, sdkIntField.getModifiers() | Modifier.FINAL);
        sdkIntField.setAccessible(false);
    }

    private void resetFingerprintAvailability() {
        easyFingerprint.doesFingerprintSensorExist = true;
        easyFingerprint.isFingerprintRegistered = true;
        easyFingerprint.isKeyguardSecured = true;
    }
}