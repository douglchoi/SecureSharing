package cecs.secureshare.security;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.bc.BcPGPObjectFactory;
import org.spongycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.spongycastle.util.test.UncloseableOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Douglas on 11/28/2015.
 */
public class PGPCipher {
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
     * @param privateKey
     */
    public void decrypt(InputStream in, OutputStream out, PGPPrivateKey privateKey) {
        try {
            PGPObjectFactory pgpFactory = new BcPGPObjectFactory(in);
            PGPEncryptedDataList encList = (PGPEncryptedDataList) pgpFactory.nextObject();
            PGPPublicKeyEncryptedData encP = (PGPPublicKeyEncryptedData) encList.get(0);

            InputStream encIn = encP.getDataStream(new BcPublicKeyDataDecryptorFactory(privateKey));
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
}
