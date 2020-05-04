package com.public.asyncdec;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // reference : https://docs.huihoo.com/android/4.4/guide/topics/providers/document-provider.html
    private static final String TAG = "codecexperi";
    private static final int READ_REQUEST_CODE = 42;
    MediaCodecInfo.CodecCapabilities codeccap = new MediaCodecInfo.CodecCapabilities();
    Button browse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        browse=(Button)findViewById(R.id.browse);
        Log.d(TAG,"codecexperi is now starting. Please standby ...");

        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /**
                 * Fires an intent to spin up the "file chooser" UI and select an image.
                 */
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                // Filter to only show results that can be "opened", such as a file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // To search for all documents available via installed storage providers, it would be "*/*".
                intent.setType("video/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        Uri fileuri;
        String[] temp;
        Intent transmit;

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The file's URI selected by the user will be contained in the return intent provided to this method as a parameter.

            if (resultData != null) {
                fileuri = resultData.getData();
                temp = fileuri.getPath().split(":");
                String filepath = temp[1];
                Log.i(TAG, "File Path: " + filepath);
                transmit=new Intent(MainActivity.this,PlayerActivity.class);
                transmit.putExtra("FILEPATH",filepath);
                startActivity(transmit);
            }
        }
    }


}