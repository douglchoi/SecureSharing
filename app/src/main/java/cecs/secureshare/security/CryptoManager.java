package cecs.secureshare.security;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.bc.BcPGPObjectFactory;
import org.spongycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.spongycastle.util.test.UncloseableOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;

/**
 * This manages security classes and keys. This is a singleton class.
 * Created by Douglas on 11/27/2015.
 */
public class CryptoManager {

    private PGPPublicKey publicKey;
    private PGPSecretKey secretKey;
    private PGPPublicKeyRing publicKeyRing;

    private String uniqueId;    // TODO: how do we set this?
    private char[] password;
    public static final String TAG = "CryptoLog";

    private static CryptoManager instance = new CryptoManager();

    private CryptoManager() {}

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

        // Generate keys for this session
        uniqueId = new String(new SecureRandom().generateSeed(64));
        password = new String(new SecureRandom().generateSeed(64)).toCharArray();

        PGPKeyPairContainer keyPair = new PGPKeyManager().generateKeys(uniqueId, password);
        publicKeyRing = keyPair.getPublicKeyRing();

        Iterator<PGPPublicKey> pkIt = publicKeyRing.getPublicKeys();
        PGPPublicKey signingPk = pkIt.next();
        PGPPublicKey encPk = pkIt.next();
        publicKey = encPk;

        Iterator<PGPSecretKey> skIt = keyPair.getSecretKeyRing().getSecretKeys();
        PGPSecretKey signingSk = skIt.next();
        PGPSecretKey encSk = skIt.next();
        secretKey = encSk;
    }

    /**
     * Extracts an encryption public key from encoded key ring
     * @param encodedPublicKeyRing
     * @return
     */
    public static PGPPublicKey extractPublicKey(byte[] encodedPublicKeyRing) {
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
                        key = k;
                    }
                }
            }
            return key;
        } catch (IOException | PGPException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * @return singleton instance
     */
    public static CryptoManager getInstance() {
        return instance;
    }

    public PGPPublicKey getPublicKey() {
        return publicKey;
    }

    public PGPPublicKeyRing getPublicKeyRing() {
        return publicKeyRing;
    }

    public PGPSecretKey getSecretKey() {
        return secretKey;
    }

    // ------------------------------------------------------------------------------------------------

    /*
     * Encrypts the input stream into the output stream using public key encryption and AES256
     * @param in - input stream (plain text)
     * @param out - output stream (cipher text)
     * @param publicKey
     */
    public void encrypt(InputStream in, OutputStream out, PGPPublicKey publicKey) {
        encrypt(in, out, publicKey, true);
    }

    /**
     * Encrypts the input stream into the output stream using public key encryption and AES256
     * @param in - input stream (plain text)
     * @param out - output stream (cipher text)
     * @param publicKey
     * @param integrityCheck
     */
    public void encrypt(InputStream in, OutputStream out, PGPPublicKey publicKey, boolean integrityCheck) {
        try {
            byte[] inBytes = IOUtils.toByteArray(in);
            PGPDataEncryptorBuilder encBuilder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256).setWithIntegrityPacket(integrityCheck);
            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(encBuilder);
            encGen.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));

            OutputStream encOut = encGen.open(new UncloseableOutputStream(out), inBytes.length);
            encOut.write(inBytes);
            encOut.close();
        } catch (PGPException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        } catch (IOException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Decrypts the input stream (cipher) into the output stream (plain text) using private key
     * @param in (cipher text)
     * @param out (plain text)
     * @param secretKey
     */
    public void decrypt(InputStream in, OutputStream out, PGPSecretKey secretKey) {
        try {
            PGPObjectFactory pgpFactory = new BcPGPObjectFactory(in);
            PGPEncryptedDataList encList = (PGPEncryptedDataList) pgpFactory.nextObject();
            PGPPublicKeyEncryptedData encP = (PGPPublicKeyEncryptedData) encList.get(0);
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder().build(password);

            InputStream encIn = encP.getDataStream(new BcPublicKeyDataDecryptorFactory(secretKey.extractPrivateKey(decryptor)));
            int ch;
            while ((ch = encIn.read()) >= 0) {
                out.write(ch);
            }
        } catch (PGPException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        } catch (IOException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        }
    }

    // ----------------------------------- TESTING ----------------------------------------
    /*

    public void DoTest()
    {
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
            pgpKeyManager.generateKeys("abc@xyz.com", pass);
            pgpCipher.encrypt(plaintext, ciphertext, pgpKeyManager.publicKey, true);
            InputStream cipherInput = new ByteArrayInputStream(ciphertext.toByteArray());
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder().build(pass);
            pgpCipher.decrypt(cipherInput, outStream, pgpKeyManager.secretKey.extractPrivateKey(decryptor));

            ciphertextStr = ciphertext.toString();
            decryptedStr = new String(outStream.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
            decryptedStr = e.getLocalizedMessage();
        }
        // ---------------------------------------------------------------
    }*/
}
