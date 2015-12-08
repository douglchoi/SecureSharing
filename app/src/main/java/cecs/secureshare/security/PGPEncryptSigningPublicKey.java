package cecs.secureshare.security;

import android.util.Log;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Container for holding both the signing and public keys
 * Created by Douglas on 12/8/2015.
 */
public class PGPEncryptSigningPublicKey {
    private PGPPublicKey encryptKey;
    private PGPPublicKey signingKey;

    /**
     * @param encryptKey
     * @param signingKey
     */
    public PGPEncryptSigningPublicKey(PGPPublicKey encryptKey, PGPPublicKey signingKey) {
        this.encryptKey = encryptKey;
        this.signingKey = signingKey;
    }

    /**
     * @param encodedPublicKeyRing
     */
    public PGPEncryptSigningPublicKey(byte[] encodedPublicKeyRing) {
        try {
            InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(encodedPublicKeyRing));
            PGPPublicKeyRingCollection pgpPub = new JcaPGPPublicKeyRingCollection(in);
            PGPPublicKey key = null;
            Iterator rIt = pgpPub.getKeyRings();
            while (key == null && rIt.hasNext()) {
                PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();

                Iterator kIt = kRing.getPublicKeys();
                while (key == null && kIt.hasNext()) {
                    PGPPublicKey k = (PGPPublicKey) kIt.next();
                    if (k.isEncryptionKey()) {
                        encryptKey = k;
                    } else {
                        signingKey = k;
                    }
                }
            }
        } catch (IOException | PGPException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        }
    }

    public PGPPublicKey getEncryptKey() {
        return encryptKey;
    }

    public PGPPublicKey getSigningKey() {
        return signingKey;
    }
}
