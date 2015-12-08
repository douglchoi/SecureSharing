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
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPSignatureGenerator;
import org.spongycastle.openpgp.PGPSignatureList;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.bc.BcPGPObjectFactory;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.spongycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.spongycastle.util.test.UncloseableOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private PGPPublicKey signingPublicKey;
    private PGPSecretKey secretKey;
    private PGPSecretKey signingSecretKey;
    private PGPPublicKeyRing publicKeyRing;

    private String uniqueId;    // TODO: how do we set this?
    private char[] password;
    public static final String TAG = "CryptoLog";

    private static final int SIG_LENGTH = 287;
    private static CryptoManager instance = new CryptoManager();

    private CryptoManager() {}

    /**
     * Initialization sets BouncyCastle as the security provider, and fetches master keys.
     */
    public void initialize() {
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
        signingPublicKey = pkIt.next();
        publicKey = pkIt.next();

        Iterator<PGPSecretKey> skIt = keyPair.getSecretKeyRing().getSecretKeys();
        signingSecretKey = skIt.next();
        secretKey = skIt.next();

        // Testing...
        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
        encrypt(new ByteArrayInputStream("test".getBytes()), out, publicKey);
        String encrypted = new String(out.toByteArray());
        ByteArrayOutputStream original = new ByteArrayOutputStream();
        decrypt(new ByteArrayInputStream(out.toByteArray()), original, secretKey, signingPublicKey);
        String originalText = new String(original.toByteArray());*/
    }

    /**
     * Extracts an encryption and signing public key from encoded key ring
     * @param encodedPublicKeyRing
     * @return
     */
    public static PGPEncryptSigningPublicKey extractPublicKey(byte[] encodedPublicKeyRing) {
        return new PGPEncryptSigningPublicKey(encodedPublicKeyRing);
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
            // Encrypt
            byte[] inBytes = IOUtils.toByteArray(in);
            PGPDataEncryptorBuilder encBuilder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256).setWithIntegrityPacket(integrityCheck);
            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(encBuilder);
            encGen.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));

            ByteArrayOutputStream cipherTextOut = new ByteArrayOutputStream();
            OutputStream encOut = encGen.open(new UncloseableOutputStream(cipherTextOut), inBytes.length);
            encOut.write(inBytes);
            encOut.close();
            byte[] cipherText = cipherTextOut.toByteArray();

            // Then, create signature
            PGPSignatureGenerator sigGen = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(signingPublicKey.getAlgorithm(), PGPUtil.SHA256));
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder().build(password);
            sigGen.init(PGPSignature.BINARY_DOCUMENT, signingSecretKey.extractPrivateKey(decryptor));
            sigGen.update(cipherText);
            PGPSignature signature = sigGen.generate();
            byte[] encodedSig = signature.getEncoded();

            // output signature || cipher text
            out.write(encodedSig);
            out.write(cipherText);
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
     * @param signingPublicKey - public key of the encryptor required for verifying
     */
    public void decrypt(InputStream in, OutputStream out, PGPSecretKey secretKey, PGPPublicKey signingPublicKey) {
        try {
            // Get the signature and verify
            byte[] signatureBytes = new byte[SIG_LENGTH];
            in.read(signatureBytes, 0, SIG_LENGTH);
            InputStream sigIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(signatureBytes));
            PGPObjectFactory fact = new BcPGPObjectFactory(sigIn);
            PGPSignatureList sigList = (PGPSignatureList) fact.nextObject();
            PGPSignature signature = sigList.get(0);

            // get the cipher text
            ByteArrayOutputStream cipherTextOut = new ByteArrayOutputStream();
            int sh;
            while((sh = in.read()) >= 0) {
                cipherTextOut.write(sh);
            }
            byte[] cipherText = cipherTextOut.toByteArray();
            cipherTextOut.close();

            // verify
            signature.init(new BcPGPContentVerifierBuilderProvider(), signingPublicKey);
            signature.update(cipherText);
            boolean isVerify = signature.verify();

            if (isVerify) {
                // Decrypt
                PGPObjectFactory pgpFactory = new BcPGPObjectFactory(cipherText);
                PGPEncryptedDataList encList = (PGPEncryptedDataList) pgpFactory.nextObject();
                PGPPublicKeyEncryptedData encP = (PGPPublicKeyEncryptedData) encList.get(0);
                PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(password);

                InputStream encIn = encP.getDataStream(new BcPublicKeyDataDecryptorFactory(secretKey.extractPrivateKey(decryptor)));
                int ch;
                while ((ch = encIn.read()) >= 0) {
                    out.write(ch);
                }
            }
        } catch (PGPException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        } catch (IOException e) {
            Log.d(CryptoManager.TAG, e.getLocalizedMessage(), e);
        } catch (Exception e) {
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
