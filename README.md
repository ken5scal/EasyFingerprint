 [ ![Download](https://api.bintray.com/packages/ken5scal/maven/easyfingerprint/images/download.svg) ](https://bintray.com/ken5scal/maven/easyfingerprint/_latestVersion)

# EasyFingerprint
Utility that lets developer easily implement fingerprint related functionalities

# How To Use
* In Class where you want to receive fingerprint results, implement EasyFingerprintCallback
* When you no longer require fingerprint to be used, call easyFingerprint.stopReadingSensor(). 
    * For example in Activity, you may want to call it on onPause();
