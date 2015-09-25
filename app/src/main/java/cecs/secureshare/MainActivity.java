package cecs.secureshare;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Attach click listeners to main menu buttons
        final Button hostGroupButton = (Button) findViewById(R.id.host_group_button);
        hostGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Host a group
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
