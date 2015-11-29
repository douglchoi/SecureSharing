package cecs.secureshare.security;

import android.util.Log;

import java.security.KeyPair;
import java.security.Security;

/**
 * This manages security classes and keys. This is a singleton class.
 * Created by Douglas on 11/27/2015.
 */
public class CryptoManager {

    public static final String TAG = "CryptoLog";

    private static CryptoManager instance = new CryptoManager();
    private KeyPair masterKeyPair;

    public static CryptoManager getInstance() {
        return instance;
    }

    /**
     * @return master key pair
     */
    public KeyPair getMasterKeyPair() {
        return masterKeyPair;
    }
    // --------------------------------------------------------------------------------------------

    /**
     * Initialization sets BouncyCastle as the security provider, and fetches master keys.
     */
    private CryptoManager() {
        try {
            Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }

        masterKeyPair = PGPKeyManager.initializeMasterKeyPair();
    }

}
