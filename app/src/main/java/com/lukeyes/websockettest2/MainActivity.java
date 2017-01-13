package com.lukeyes.websockettest2;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    public Button connectButton;
    public Button disconnectButton;
    private EditText addressInput;
    private WebSocketClient mWebSocketClient;
    private Context context;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        connectButton = (Button) findViewById(R.id.buttonConnect);
        disconnectButton = (Button) findViewById(R.id.buttonDisconnect);

        addressInput = (EditText) findViewById(R.id.address);
        addressInput.setText("192.168.1.2");

        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);

        textToSpeech = new TextToSpeech(this, this);
    }

    public void onConnect(View v) {
        String address = addressInput.getText().toString();

        connectWebSocket(address);
    }

    public void onDisconnect(View v) {
        Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
    }

    public void displayString(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void connectWebSocket(String address) {
        URI uri;
        try {
            String socketAddress = String.format("ws://%s:8080", address);
            String toastText = String.format("Connecting to %s", socketAddress);
            Toast.makeText(this,toastText,Toast.LENGTH_SHORT).show();
            uri = new URI(socketAddress);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(false);
                        disconnectButton.setEnabled(true);
                        displayString("Opened");
                    }
                });

                this.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(final String message) {
                Log.i("Websocket", message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayString(message);
                    }
                });
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("Websocket", "Closed " + reason);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(true);
                        disconnectButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onError(Exception ex) {
                Log.i("Websocket", "Error " + ex.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    @Override
    public void onInit(int status) {
// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = textToSpeech.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                Log.d("TTS","Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                // The TTS engine has been successfully initialized.

            }
        } else {
            // Initialization failed.
            Log.e("TTS", "Could not initialize TextToSpeech.");
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }
}
