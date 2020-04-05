package com.example.sceneform_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import java.net.URI;
import java.net.URISyntaxException;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArFragment arFragment;
    private ModelRenderable bearRenderable, stonefloorRenderable;
    private ImageView bear,stonefloor;

    View arrayView[];
    ViewRenderable name_animal;

    int selected = 1; // Default StoneFloor is choose

    ViewRenderable animal_name;

    private WebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("WebSocket", "===================== INIT =================");
        try {
            createWebSocketClient();
            Log.i("WebSocket", "===================== ENTRO ===============");
        } catch (URISyntaxException e) {
            Log.i("WebSocket", "===================== ERROR ===============");
            e.printStackTrace();
        }

        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);

        //View
        bear = (ImageView)findViewById(R.id.bear);
        stonefloor = (ImageView)findViewById(R.id.stonefloor);

        setArrayView();
        setClickListener();

        setupModel();

        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                // When user tap on plane, we will add model

                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                createModel(anchorNode, selected);
            }
        });


    }

    private void setupModel() {

        ViewRenderable.builder()
                .setView(this, R.layout.name_animal)
                .build()
                .thenAccept(renderable -> name_animal = renderable);

        ModelRenderable.builder()
                .setSource(this,R.raw.stonefloor)
                .build().thenAccept(renderable -> stonefloorRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(this,"unable to load StoneFloor model", Toast.LENGTH_SHORT).show();
                    return null;
                });

        ModelRenderable.builder()
                .setSource(this,R.raw.bear)
                .build().thenAccept(renderable -> bearRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(this,"unable to load Bear model", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void createModel(AnchorNode anchorNode, int selected) {
        if(selected == 1) {
            TransformableNode stonefloor = new TransformableNode(arFragment.getTransformationSystem());
            stonefloor.setParent(anchorNode);
            stonefloor.setRenderable(stonefloorRenderable);
            stonefloor.select();

            selected = 2;
        }
        if(selected == 2) {
            TransformableNode bear = new TransformableNode(arFragment.getTransformationSystem());
            bear.setParent(anchorNode);
            bear.setRenderable(bearRenderable);
            bear.select();

            addName(anchorNode, bear, "Bear");
        }
    }

    private void addName(AnchorNode anchorNode, TransformableNode model, String name) {
        TransformableNode nameView = new TransformableNode(arFragment.getTransformationSystem());
        nameView.setLocalPosition(new Vector3(0f, model.getLocalPosition().y+0.5f,0));
        nameView.setParent(anchorNode);
        nameView.setRenderable(name_animal);
        nameView.select();

        TextView txt_name = (TextView)name_animal.getView();
        txt_name.setText(name);

        txt_name.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                anchorNode.setParent(null);
            }
        });
    }

    private void setClickListener() {
        for (int i = 0; i<arrayView.length; i++) {
            arrayView[i].setOnClickListener(this);
        }
    }

    private void setArrayView() {
        arrayView = new View[]{
                bear,
                stonefloor
        };
    }

    @Override
    public void onClick(View v) {
        selected = 2;
    }

    private void createWebSocketClient() throws URISyntaxException {
        URI uri;
        Log.i("WebSocket", "===================== ENTRO 2 ===============");
        uri = new URI("ws://signalserver.herokuapp.com");
        Log.i("WebSocket", "===================== ENTRO 3 ===============");
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                webSocketClient.send("Hello World");
            }

            @Override
            public void onTextReceived(String message) {
                Log.i("WebSocket", "Message received");
                Log.i("WebSocket", message);
            }

            @Override
            public void onBinaryReceived(byte[] data) {

            }

            @Override
            public void onPingReceived(byte[] data) {

            }

            @Override
            public void onPongReceived(byte[] data) {

            }

            @Override
            public void onException(Exception e) {
                Log.i("WebSocket",  e.getMessage());
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }
}
