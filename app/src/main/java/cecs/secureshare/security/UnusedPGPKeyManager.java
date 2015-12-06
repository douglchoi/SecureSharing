package cecs.secureshare.security;

import android.util.Log;

import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.Date;

/**
 * Class for handling the master keys.
 * Created by Douglas on 11/28/2015.
 */
public class UnusedPGPKeyManager {

    private static final String PRIVATE_KEY_ALIAS = "secureshare.pgp.key";
    private static final String KEY_STORE_TYPE = "AndroidKeyStore";

    private static final String CERT_CN = "secureshare cert";
    private static final String CERT_OU = "cecs";
    private static final String CERT_O = "cecs";

    /**
     * For debugging. Clears out the master keys to be regenerated.
     */
    public static void clearMasterKeys() {
        try {
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
            ks.load(null);
            ks.deleteEntry(PRIVATE_KEY_ALIAS);
        } catch (Exception e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Returns an existing master keypair in android KeyStore. If not, it will create it
     * and store it in KeyStore.
     */
    public static KeyPair initializeMasterKeyPair() {
        UnusedPGPKeyManager keyManager = new UnusedPGPKeyManager();

        KeyPair kp = null;

        // Load it from Android's KeyStore
        try {
            kp = keyManager.loadMasterKeyFromKeyStore();
        } catch (Exception e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        }

        // Private key was not found, so generate a new one
        if (kp == null) {
            Log.d(CryptoManager.TAG, "Private key not found. Generated new pair.");
            try {
                kp = keyManager.generateMasterKeys();
                Log.d(CryptoManager.TAG, "New key pair generated.");

                // Before we store it in the KeyStore, we require to create self-signed certs for the keypair
                X509Certificate certificate = createCertificate(kp);
                keyManager.storeMasterKeyInKeyStore(kp.getPrivate(), certificate);

                Log.d(CryptoManager.TAG, "Private keys stored.");

            } catch (Exception e) {
                Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
            }
        } else {
            Log.d(CryptoManager.TAG, "Existing private keys loaded.");
        }
        return kp;
    }

    /**
     *
     * Save the master private key in android's KeyStore
     * @param privateKey the private key to store
     * @param certificate certificate to verify private key
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void storeMasterKeyInKeyStore(PrivateKey privateKey, Certificate certificate)
            throws KeyStoreException,
                    CertificateException,
                    NoSuchAlgorithmException,
                    IOException {
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        ks.load(null);
        ks.setKeyEntry(PRIVATE_KEY_ALIAS, privateKey, null, new Certificate[]{certificate});
    }

    /**
     *
     * Loads the master key pair from KeyStore. Returns null if not found
     * @return the master key pair
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws UnrecoverableEntryException
     */
    private KeyPair loadMasterKeyFromKeyStore()
            throws KeyStoreException,
                CertificateException,
            NoSuchAlgorithmException,
            IOException,
            UnrecoverableEntryException,
            InvalidKeyException,
            NoSuchProviderException,
            SignatureException {

        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        ks.load(null);
        KeyStore.Entry sEntry = ks.getEntry(PRIVATE_KEY_ALIAS, null);

        if (!(sEntry instanceof KeyStore.PrivateKeyEntry)) {
            Log.d(CryptoManager.TAG, "Not a private key.");
            return null;
        }

        // retrieve private key from KeyStore, and public key from the certificate
        Certificate certificate = ((KeyStore.PrivateKeyEntry) sEntry).getCertificate();
        PrivateKey sKey = ((KeyStore.PrivateKeyEntry) sEntry).getPrivateKey();
        PublicKey pKey = certificate.getPublicKey();

        // verify that the certificate is verified
        certificate.verify(pKey);

        return new KeyPair(pKey, sKey);
    }

    /**
     * Generates a RSA key pair. This is the master key used for PGP encryption
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    private KeyPair generateMasterKeys()
            throws NoSuchProviderException,
                    NoSuchAlgorithmException,
                    InvalidAlgorithmParameterException {
        // Create the master key
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "SC");
        //RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
        kpg.initialize(ecSpec, new SecureRandom());
        return kpg.generateKeyPair();
    }

    /**
     * Generate a certificate for the key pair
     * @param keyPair
     * @return
     * @throws OperatorCreationException
     * @throws CertificateException
     */
    private static X509Certificate createCertificate(KeyPair keyPair)
            throws OperatorCreationException,
                    CertificateException,
                    IOException {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.OU, CERT_OU);
        nameBuilder.addRDN(BCStyle.O, CERT_O);
        nameBuilder.addRDN(BCStyle.CN, CERT_CN);

        Date start = new Date();
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(start);
        endCal.add(Calendar.YEAR, 10); // Expires in 10 years
        Date end = endCal.getTime();

        BigInteger serialNumber = new BigInteger(128, new SecureRandom());

        X509v3CertificateBuilder certificateBuilder =  new JcaX509v3CertificateBuilder(nameBuilder.build(), serialNumber, start, end, nameBuilder.build(), keyPair.getPublic());
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WITHECDSA").setProvider("SC").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(certificateBuilder.build(contentSigner));
    }
}
