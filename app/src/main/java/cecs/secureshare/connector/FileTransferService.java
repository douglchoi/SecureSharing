package cecs.secureshare.connector;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import cecs.secureshare.R;
import cecs.secureshare.security.DHKeyGenerator;
import cecs.secureshare.security.FileCBCCipher;

/**
 * Handles sending the file
 * Created by Douglas on 10/11/2015.
 */
public class FileTransferService extends IntentService {

    public static final String FILE_URL = "fileUrl";
    public static final String HOST_DEVICE_ADDRESS = "deviceAddress";
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final int HOST_PORT = 8988;
    public static final int SOCKET_TIMEOUT = 5000;

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        String storageDir = Environment.getExternalStorageDirectory() + "/"  + context.getPackageName() + "/secureshare-";

        if (intent.getAction().equals(ACTION_SEND_FILE)) {

            // location to write to
            String fileUri = intent.getExtras().getString(FILE_URL);

            // device address to receive file
            String host = intent.getExtras().getString(HOST_DEVICE_ADDRESS);

            Socket socket = new Socket();

            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, HOST_PORT)), SOCKET_TIMEOUT);

                // TESTING
                KeyPair pair = DHKeyGenerator.generateKeys();
                PublicKey pkey = pair.getPublic();
                PrivateKey skey = pair.getPrivate();
                FileCBCCipher encryptCipher = new FileCBCCipher(pkey, FileCBCCipher.CipherMode.Encrypt);
                FileCBCCipher decryptCipher = new FileCBCCipher(skey, FileCBCCipher.CipherMode.Decrypt);

                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));

                    // TESTING Encryption
                    File cipherFile = new File(storageDir + "cipherFile.jpg");
                    FileOutputStream cipherTextFos = new FileOutputStream(cipherFile);
                    encryptCipher.process(is, cipherTextFos);

                    // TESTING Decryption
                    File decryptedFile = new File(storageDir + "decryptedFile.jpg");
                    FileOutputStream decryptFos = new FileOutputStream(decryptedFile);
                    decryptCipher.process(new FileInputStream(cipherFile), decryptFos);


                } catch (FileNotFoundException e) {
                    Log.d("Info", e.toString());
                }
                FileTransferAsyncTask.copyFile(is, stream);
                Log.d("Info", "Data written");
            } catch (IOException e) {
                Log.e("Info", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
