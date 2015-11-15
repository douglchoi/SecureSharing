package cecs.secureshare.security;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class encrypts and decrypts a file using AES-CBC
 * Created by Douglas on 11/12/2015.
 */
public class FileCBCCipher {

    // Register BouncyCastle for use
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public static enum CipherMode { Encrypt, Decrypt };

    // ----------------------------------------------------------------

    private final byte[] key;   // encryption / decryption key
    private final byte[] IV;    // initialization vector for CBC
    private final CipherMode cipherMode;  // whether we are encrypting or decrypting

    private static final int BLOCK_SIZE = 16;
    private Cipher cipher;

    /**
     * Initialize the file cipher.
     * @param secretKey - The encryption/decryption key. Make sure that
     *                      this secret key is randomly generated by PRNG (?)
     * @param cipherMode - whether we want to encrypt or decrypt
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    public FileCBCCipher(String secretKey, CipherMode cipherMode)
                throws NoSuchPaddingException,
                        NoSuchAlgorithmException,
                        NoSuchProviderException,
                        InvalidAlgorithmParameterException,
                        InvalidKeyException {

        key = secretKey.getBytes();
        IV = new byte[BLOCK_SIZE];
        this.cipherMode = cipherMode;

        // 1. create cipher using BouncyCastle
        // Using AES, CBC, and padding. Key size is 128
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");

        // 2. create the key
        SecretKey keyValue = new SecretKeySpec(key, "AES");

        // 3. initialize the IV
        AlgorithmParameterSpec IVSpec = new IvParameterSpec(IV);

        // 4. initialize the cipher
        if (cipherMode == CipherMode.Encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, keyValue, IVSpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keyValue, IVSpec);
        }
    }

    /**
     * In Encrypt Mode, takes a raw file and outputs encrypted file into OutputStream
     * In Decrypt Mode, takes an encrypted file and outputs raw file int OutputStream
     * @param file
     * @param fos
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ShortBufferException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public void process(File file, OutputStream fos)
            throws FileNotFoundException,
                    IOException,
                    ShortBufferException,
                    BadPaddingException,
                    IllegalBlockSizeException {

        FileInputStream fis = new FileInputStream(file);

        if (cipherMode == CipherMode.Decrypt) {
            decrypt(fis, fos);
        } else {
            encrypt(fis, fos);
        }

    }

    /**
     * Encrypt a file input stream (fis) and output the encrypted file to output stream (fos)
     * @param fis - input stream containing unencrypted file
     * @param fos - output stream containing encrypted file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ShortBufferException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private void encrypt(InputStream fis, OutputStream fos)
            throws FileNotFoundException,
            IOException,
            ShortBufferException,
            BadPaddingException,
            IllegalBlockSizeException {

        //optionally put the IV at the beggining of the cipher file
        //fos.write(IV, 0, IV.length);

        byte[] buffer = new byte[BLOCK_SIZE];
        int noBytes = 0;
        byte[] cipherBlock = new byte[cipher.getOutputSize(buffer.length)];
        int cipherBytes;
        while((noBytes = fis.read(buffer))!=-1) {
            cipherBytes = cipher.update(buffer, 0, noBytes, cipherBlock);
            fos.write(cipherBlock, 0, cipherBytes);
        }
        //always call doFinal
        cipherBytes = cipher.doFinal(cipherBlock,0);
        fos.write(cipherBlock,0,cipherBytes);

        //close the files
        fos.close();
        fis.close();
    }

    /**
     * Encrypt an encrypted file input stream (fis) and output the raw file to output stream (fos)
     * @param fis
     * @param fos
     * @throws IOException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public void decrypt(InputStream fis, OutputStream fos)
            throws IOException,
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException {

        // get the IV from the file


        // DO NOT FORGET TO reinit the cipher with the IV
        //fis.read(IV,0,IV.length);
        //this.InitCiphers();

        byte[] buffer = new byte[BLOCK_SIZE];
        int noBytes = 0;
        byte[] cipherBlock = new byte[cipher.getOutputSize(buffer.length)];
        int cipherBytes;
        while((noBytes = fis.read(buffer))!=-1) {
            cipherBytes = cipher.update(buffer, 0, noBytes, cipherBlock);
            fos.write(cipherBlock, 0, cipherBytes);
        }

        //always call doFinal
        cipherBytes = cipher.doFinal(cipherBlock,0);
        fos.write(cipherBlock,0,cipherBytes);

        //close the files
        fos.close();
        fis.close();
    }
}

