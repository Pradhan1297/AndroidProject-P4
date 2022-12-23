package com.example.project4cs478;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Handler playerA, playerB; // Handlers for playerAThread and playerBThread respectively
    int count = 0;
    int totalMoves;
    PlayerAThread playerAThread;
    PlayerBThread playerBThread;
    List<Integer> cellList = new ArrayList<>();//cellList stores the ids of all the edit text elements in the table layout

    public Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String AStatus = msg.getData().getString("StatusA"); // playerAThread sends message to the UI thread informing that the game has ended
            if (AStatus != null) {
                finishGame(AStatus);
            }
            String BStatus = msg.getData().getString("StatusB");// playerBThread sends message to the UI thread informing that the game has ended
            {
                if (BStatus != null) {
                    finishGame(BStatus);
                }
            }
        }
    };// Handler is created for UI Thread and is associated with it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cellList.add(R.id.editText1); // adding the id of edit text element editText1 to the cellList
        cellList.add(R.id.editText2); // adding the id of edit text element editText2 to the cellList
        cellList.add(R.id.editText3); // adding the id of edit text element editText3 to the cellList
        cellList.add(R.id.editText4); // adding the id of edit text element editText4 to the cellList
        cellList.add(R.id.editText5); // adding the id of edit text element editText5 to the cellList
        cellList.add(R.id.editText6); // adding the id of edit text element editText6 to the cellList
        cellList.add(R.id.editText7); // adding the id of edit text element editText7 to the cellList
        cellList.add(R.id.editText8); // adding the id of edit text element editText8 to the cellList
        cellList.add(R.id.editText9); // adding the id of edit text element editText9 to the cellList
        Button startGameButton = findViewById(R.id.startGame);
        startGameButton.setOnClickListener(view -> {
            if (count == 0) {
                startGame();
            } else {
                startNewGame();
            }
            count++;
        });
    }

    //Starts the game by starting the threads PlayerAThread and PlayerBThread
    public void startGame() {
        playerAThread = new PlayerAThread("PlayerAThread");
        playerAThread.start();
        playerBThread = new PlayerBThread("PlayerBThread");
        playerBThread.start();
        totalMoves=0;
    }

    //startNewGame restarts the game by clearing all the old entries of the editText elements
    public void startNewGame() {
        for (int i : cellList) {
            editText = findViewById(i);
            editText.getText().clear();
        }//clears the old entries of the edit text elements
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (playerAThread != null) playerAThread.quit();
        if (playerBThread != null) playerBThread.quit();
        startGame();
    }

    //finishGame checks the game's current status and displays the name of the winner in the toast message
    public void finishGame(String Status) {
        if (Status.equalsIgnoreCase("A wins")) {
            Toast.makeText(this, "Player A Wins", Toast.LENGTH_SHORT).show();
        } else if (Status.equalsIgnoreCase("B Wins")) {
            Toast.makeText(this, "Player B Wins", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Game Tied", Toast.LENGTH_SHORT).show();
        }
        playerAThread.quit();
        playerBThread.quit();
    }

    // This code runs in background thread in parallel with UI thread. PlayerAThread follows a strategy that randomly selects cells and sets their values.
    public class PlayerAThread extends HandlerThread {
        int idx;
        int ACount = 0;
        int c = 0;
        Queue<Integer> playerABuffer = new LinkedList<>(); //playerABuffer keeps track of the edit text elements whose values are set to A
        int[] playerABufferArr = new int[3];
        int t = 0;
        int prev = 0;


        public PlayerAThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();

            fillCell();
            notifyB();

            playerA = new Handler(getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    boolean status = msg.getData().getBoolean("StartA");
                    if (status) {
                        totalMoves++;
                        fillCell();
                        notifyB();
                    }
                }
            };
        }

        //notifyB notifies PlayerBThread that it is it's turn after PlayerAThread has made it's move
        public void notifyB() {
            Message message = playerB.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putBoolean("StartB", true);
            message.setData(bundle);
            message.sendToTarget();
        }

        //notifyUIA notifies UI thread that the game is complete and it's won by player A
        public void notifyUIA() {
            playerBThread.quit();
            Message message = mainHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("StatusA", "A wins");
            bundle.putBoolean("End Game", true);
            message.setData(bundle);
            message.sendToTarget();
        }

        // Fills the cells(edit text elements) in the table layout randomly
        public void fillCell() {
            if (totalMoves > 20) {
                finishGame("");
            }
            // Post runnable on mHandler (feeding UI thread's message queue)
            mainHandler.post(
                    () -> {

                        idx = (int) (Math.random() * (8 - 3) + 3);
                        while (prev == idx) {
                            idx = (int) (Math.random() * (8 - 3) + 3);
                        }
                        editText = findViewById(cellList.get(idx));
                        if (!(editText.getText().toString().equalsIgnoreCase("B")) && !(editText.getText().toString().equals("A"))) {
                            //Checking if there are more than 3 pawns on the board. This is tracked using a queue. The least recently used pawn is removed.

                            if (ACount < 3) {
                                editText.setText("A");
                                playerABuffer.add(idx);
                                ACount++;
                            } else {
                                for (int i : playerABuffer) {
                                    if (t < 3)
                                        playerABufferArr[t++] = i;
                                }
                                //winning condition
                                if ((Math.abs(playerABufferArr[0] - playerABufferArr[1]) == Math.abs(playerABufferArr[1] - playerABufferArr[2])) ||
                                        (Math.abs(playerABufferArr[2] - playerABufferArr[0]) == Math.abs(playerABufferArr[0] - playerABufferArr[1])) ||
                                        (Math.abs(playerABufferArr[2] - playerABufferArr[0]) == Math.abs(playerABufferArr[1] - playerABufferArr[2]))) {
                                    notifyUIA();
                                } else {// the least recently used pawn is removed here
                                    int lruIdx = playerABuffer.remove();
                                    playerABuffer.add(idx);
                                    editText.setText("A");
                                    editText = findViewById(cellList.get(lruIdx));
                                    editText.getText().clear();
                                }
                            }
                        }
                        prev = idx;
                    }
            );
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // This code runs in background thread in parallel with UI thread. PlayerAThread follows a strategy that fills the cells(edit text elements) in the table layout sequentially from the starting.
    public class PlayerBThread extends HandlerThread {
        int BCount = 0;
        int idx;
        int[] playerBBufferArr = new int[3];
        int i, k = 0;
        Queue<Integer> playerBBuffer = new LinkedList<>(); //playerBBuffer keeps track of the edit text elements whose values are set to B

        public PlayerBThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();

            playerB = new Handler(getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    boolean status = msg.getData().getBoolean("StartB");
                    if (status) {
                        totalMoves++;
                        fillCell();
                        notifyA();
                    }

                }
            };
        }

        //notifyA notifies PlayerAThread that it is it's turn after PlayerBThread has made it's move
        public void notifyA() {
            Message message = playerA.obtainMessage();
            Bundle b = new Bundle();
            b.putBoolean("StartA", true);
            message.setData(b);
            message.sendToTarget();
        }

        // Fills the cells(edit text elements) in the table layout in a sequential order
        public void fillCell() {
            if (totalMoves > 20) {
                finishGame("");
            }
            // Post runnable on mHandler (feeding UI thread's message queue)
            mainHandler.post(() -> {
                gridTraversal(cellList);
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //notifyUIB notifies UI thread that the game is complete and it's won by player B
        public void notifyUIB() {
            playerAThread.quit();
            Message message = mainHandler.obtainMessage();
            Bundle bundleB = new Bundle();
            bundleB.putString("StatusB", "B wins");
            bundleB.putBoolean("End Game", true);
            message.setData(bundleB);
            message.sendToTarget();
        }

        public void gridTraversal(List<Integer> cellList) {
            if (i < cellList.size()) {
                editText = findViewById(cellList.get(i));
                if (!(editText.getText().toString().equalsIgnoreCase("A")) &&
                        !(editText.getText().toString().equalsIgnoreCase("B"))) {
                    i++;
                    //Checking if there are more than 3 pawns on the board. This is tracked using a queue. The least recently used pawn is removed.
                    if (BCount < 3) {
                        editText.setText("B");
                        playerBBuffer.add(i);
                        BCount++;
                    } else {
                        for (int i : playerBBuffer) {
                            if (k < 3)
                                playerBBufferArr[k++] = i;
                        }
                        if ((Math.abs(playerBBufferArr[0] - playerBBufferArr[1]) == Math.abs(playerBBufferArr[1] - playerBBufferArr[2])) ||
                                (Math.abs(playerBBufferArr[2] - playerBBufferArr[0]) == Math.abs(playerBBufferArr[0] - playerBBufferArr[1])) ||
                                (Math.abs(playerBBufferArr[2] - playerBBufferArr[0]) == Math.abs(playerBBufferArr[1] - playerBBufferArr[2]))) {
                            notifyUIB();
                        } else {// the least recently used pawn is removed here
                            int lruBIdx = playerBBuffer.remove();
                            playerBBuffer.add(i);
                            editText = findViewById(cellList.get(lruBIdx));
                            editText.getText().clear();
                        }
                    }
                }
            }
        }
    }
}

