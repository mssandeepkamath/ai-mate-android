package com.sandeep.chatassistant.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sandeep.chatassistant.R;
import com.sandeep.chatassistant.adapter.MessageRVAdapter;
import com.sandeep.chatassistant.data.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {

    // creating variables for our
    // widgets in xml file.
    private RecyclerView chatsRV;
    private ImageView sendMsgIB;
    private EditText userMsgEdt;
    private ImageView ivBack;
    private final String USER_KEY = "user";
    private final String BOT_KEY = "bot";

    // creating a variable for
    // our volley request queue.
    private RequestQueue mRequestQueue;

    // creating a variable for array list and adapter class.
    private ArrayList<Message> messageModalArrayList;
    private MessageRVAdapter messageRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // on below line we are initializing all our views.
        chatsRV = findViewById(R.id.idRVChats);
        sendMsgIB = findViewById(R.id.idIBSend);
        userMsgEdt = findViewById(R.id.idEdtMessage);
        // below line is to initialize our request queue.
        mRequestQueue = Volley.newRequestQueue(this);
        mRequestQueue.getCache().clear();

        // creating a new array list
        messageModalArrayList = new ArrayList<>();


        // adding on click listener for send message button.
        sendMsgIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking if the message entered
                // by user is empty or not.
                if (userMsgEdt.getText().toString().isEmpty()) {
                    // if the edit text is empty display a toast message.
                    Toast.makeText(MainActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();
                    return;
                }

                // calling a method to send message
                // to our bot to get response.
                sendMessage(userMsgEdt.getText().toString());

                // below line we are setting text in our edit text as empty
                userMsgEdt.setText("");
            }
        });

        // on below line we are initializing our adapter class and passing our array list to it.
        messageRVAdapter = new MessageRVAdapter(messageModalArrayList, this);

        // below line we are creating a variable for our linear layout manager.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        // below line is to set layout
        // manager to our recycler view.
        chatsRV.setLayoutManager(linearLayoutManager);

        // below line we are setting
        // adapter to our recycler view.
        chatsRV.setAdapter(messageRVAdapter);
    }

    private void sendMessage(String userMsg) {
        // below line is to pass message to our
        // array list which is entered by the user.
        messageModalArrayList.add(new Message(userMsg, USER_KEY));
        messageRVAdapter.notifyDataSetChanged();

        // url for our brain
        // make sure to add mshape for uid.
        // make sure to add your url.

        String url = "http://api.brainshop.ai/get?bid=your_id&key=your_api_key&uid=your_email_id&msg=" + userMsg;

        // creating a variable for our request queue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // on below line we are making a json object request for a get request and passing our url .
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // in on response method we are extracting data
                    // from json response and adding this response to our array list.
                    String botResponse = response.getString("cnt");
                    messageModalArrayList.add(new Message(botResponse, BOT_KEY));
                    chatsRV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v,
                                                   int left, int top, int right, int bottom,
                                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {

                            chatsRV.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    chatsRV.smoothScrollToPosition(
                                            chatsRV.getAdapter().getItemCount() - 1);
                                }
                            }, 100);
                        }

                    });
                    // notifying our adapter as data changed.
                    messageRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();

                    // handling error response from bot.
                    messageModalArrayList.add(new Message("No response", BOT_KEY));
                    messageRVAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error handling.
                messageModalArrayList.add(new Message(error.getMessage(), BOT_KEY));
                Toast.makeText(MainActivity.this, "No response from the bot..",Toast.LENGTH_SHORT).show();
            }
        });

        // at last adding json object
        // request to our queue.
        queue.add(jsonObjectRequest);
    }
}