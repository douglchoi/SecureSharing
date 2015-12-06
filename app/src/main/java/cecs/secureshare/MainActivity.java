package cecs.secureshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.spongycastle.openpgp.PGPKeyRingGenerator;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.Iterator;

import cecs.secureshare.security.CryptoManager;
import cecs.secureshare.security.PGPCipher;
import cecs.secureshare.security.PGPKeyManager;

/**
 * Entry point
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CryptoManager.getInstance().initialize(true);

        setContentView(R.layout.activity_main);


        /* Sample code to call encrypt, decrypt and access keys*/
        CryptoManager cryptManager = new CryptoManager();
        /*String secretString = "THIS IS SECRET";
        ByteArrayOutputStream cipherText = cryptManager.Encrypt(new ByteArrayInputStream(secretString.getBytes()), "uniqueId");
        ByteArrayOutputStream secret = cryptManager.Decrypt(cipherText, cryptManager.sk, cryptManager.pass);*/
        /* End Sample */


        // Attach click listeners to main menu buttons
        final Button hostGroupButton = (Button) findViewById(R.id.host_group_button);
        hostGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GroupViewActivity.class);
                startActivity(intent);
            }
        });

        final Button findGroupButton = (Button) findViewById(R.id.find_group_button);
        findGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Listen to network for an existing group
                Intent intent = new Intent(getApplicationContext(), FindPeerActivity.class);
                startActivity(intent);
            }
        });

        final Button joinGroupNFCButton = (Button) findViewById(R.id.join_nfc_group_button);
        joinGroupNFCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Listen to network for an existing group
                Intent intent = new Intent(getApplicationContext(), NFCPeerActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
