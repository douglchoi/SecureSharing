package cecs.secureshare.security;

import android.util.Log;

import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;

/**
 * This manages security classes and keys. This is a singleton class.
 * Created by Douglas on 11/27/2015.
 */
public class CryptoManager {

    public PGPPublicKey pk;
    public PGPSecretKey sk;
    public char[] pass;
    public static final String TAG = "CryptoLog";

    private static CryptoManager instance = new CryptoManager();
    private KeyPair masterKeyPair;

    /**
     * Initialization sets BouncyCastle as the security provider, and fetches master keys.
     * @param refreshKeys - deletes existing keys if true
     */
    public void initialize(boolean refreshKeys) {
        try {
            Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }

        if (refreshKeys) {
            UnusedPGPKeyManager.clearMasterKeys();
        }
        masterKeyPair = UnusedPGPKeyManager.initializeMasterKeyPair();
    }

    /**
     * @return singleton instance
     */
    public static CryptoManager getInstance() {
        return instance;
    }

    /**
     * @return master key pair
     */
    public KeyPair getMasterKeyPair() {
        return masterKeyPair;
    }

    public ByteArrayOutputStream Encrypt(InputStream instream, String uniqueId) {
        //String secretString = "This is a secret string.";
        String ciphertextStr;
        //String decryptedStr = "didnt work";

        //InputStream plaintext = new ByteArrayInputStream(secretString.getBytes());
        PGPCipher pgpCipher = new PGPCipher();
        ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();
        //PublicKey publicKey = masterKeyPair.getPublic();
        //char pass[] = {'h', 'e', 'l', 'l', 'o'};
        try {
            char[] pass = new String(new SecureRandom().generateSeed(64)).toCharArray();
            PGPKeyManager pgpKeyManager = new PGPKeyManager();
            pgpKeyManager.GenerataKeys(uniqueId, pass);
            pgpCipher.encrypt(instream, ciphertext, pgpKeyManager.pk, true);
            this.pk = pgpKeyManager.pk;
            this.sk = pgpKeyManager.sk;
            this.pass = pass;
        } catch (Exception e) {
            e.printStackTrace();
            //decryptedStr = e.getLocalizedMessage();}
        }
        return ciphertext;
    }

    public ByteArrayOutputStream Decrypt(ByteArrayOutputStream cipherText, PGPSecretKey secretKey, char[] pass)
    {
        String ciphertextStr;
        String decryptedStr = "didnt work";
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try
        {
        InputStream cipherInput = new ByteArrayInputStream(cipherText.toByteArray());
        PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder().build(pass);
        PGPCipher pgpCipher = new PGPCipher();
        pgpCipher.decrypt(cipherInput, outStream, secretKey.extractPrivateKey(decryptor));

        //decryptedStr = new String(outStream.toByteArray());
    }
    catch (Exception e) {
        e.printStackTrace();
        decryptedStr = e.getLocalizedMessage();
    }
        return outStream;
    }

    public void DoTest()
    {
// -------------------------- TESTING ----------------------------
        //KeyPair masterKeyPair = CryptoManager.getInstance().getMasterKeyPair();

        String secretString = "This is a secret string.";
        String ciphertextStr;
        String decryptedStr = "didnt work";

        InputStream plaintext = new ByteArrayInputStream(secretString.getBytes());
        PGPCipher pgpCipher = new PGPCipher();
        ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();
        //PublicKey publicKey = masterKeyPair.getPublic();
        char pass[] = {'h', 'e', 'l', 'l', 'o'};
        try {
            PGPKeyManager pgpKeyManager = new PGPKeyManager();
            pgpKeyManager.GenerataKeys("abc@xyz.com", pass);
            pgpCipher.encrypt(plaintext, ciphertext, pgpKeyManager.pk, true);
            InputStream cipherInput = new ByteArrayInputStream(ciphertext.toByteArray());
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder().build(pass);
            pgpCipher.decrypt(cipherInput, outStream, pgpKeyManager.sk.extractPrivateKey(decryptor));

            ciphertextStr = ciphertext.toString();
            decryptedStr = new String(outStream.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
            decryptedStr = e.getLocalizedMessage();
        }
        // ---------------------------------------------------------------
    }
}
