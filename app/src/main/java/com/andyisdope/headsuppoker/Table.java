package com.andyisdope.headsuppoker;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;


public class Table extends AppCompatActivity {

    private SeekBar SeekBet;
    private EditText BetAmount, stack, ToSend;
    private Button Raise, TakeSeat, LeaveSeat, PostBlind, Bet, Fold, Check, Call, RemoveBlind;
    private ImageView Dealer1, Dealer2;
    private ArrayList<FrameLayout> SeatCards;
    private ArrayList<ImageView> Streets;
    private final ArrayList<String> Suits = new ArrayList<>(Arrays.asList("c", "s", "h", "d"));
    private final FirebaseDatabase mRef = FirebaseDatabase.getInstance();
    private DatabaseReference Message, Players, Seat1Ref, Seat2Ref, SeatCardsRef, Button, BlindsRef, NumRef, PotRef, Ongoing, ActionSeat, BoardStreet;
    private TextView Seat1Name, Seat1Chips, TableText, Seat2Name, Seat2Chips, Pot, RoomDeets, StakesText, MessageHistory;
    private String[] Seat1 = new String[2];
    private String[] Seat2 = new String[2];
    private String[] Blinds = new String[2];
    private String[] cref1;
    private String[] cref2;
    private String[] action = new String[3];
    private Player Player;
    private int numPlayers;
    private Double Min, Max, CurrentPot, SmallBlind, BigBlind, VillianStack;
    private String TableName, CurrStreet, VillianName;
    private boolean inHand, AllIn;
    private PokerHand S1, S2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_table);
        Intent intent = getIntent();
        InitSeats();
        getIntents(intent);
        getReferences();
        InitUI();

        SmallBlind = Double.parseDouble(Blinds[0].substring(9));
        BigBlind = Double.parseDouble(Blinds[1]);
        Max = Min * 3;
        Player.setSeat("NoSeat");
        Player.setDealer(false);
        CurrStreet = "";
        VillianStack = 0.0;
        VillianName = "";
    }

    private void getIntents(Intent intent) {
        TableText.setText(intent.getStringExtra("TableName"));
        RoomDeets.setText(intent.getStringExtra("Game"));
        StakesText.setText(intent.getStringExtra("Stakes"));
        Player = new Player(intent.getStringExtra("UID"));
        Player.setUsername(intent.getStringExtra("PlayerName"));
        Player.setBankroll(intent.getDoubleExtra("Bankroll", 0));
        Min = Double.parseDouble(intent.getStringExtra("Min"));
        Blinds = (intent.getStringExtra("Stakes").split("/"));
        TableName = intent.getStringExtra("TableName");
    }

    private void getReferences() {
        Message = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Message");
        BlindsRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Blinds");
        Seat1Ref = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Seat1");
        Seat2Ref = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Seat2");
        SeatCardsRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Cards");
        Button = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Button");
        Ongoing = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Ongoing");
        Players = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Users/" + Player.getUID());
        NumRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Players");
        PotRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Pot");
        ActionSeat = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/ActionSeat");
        BoardStreet = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/BoardStreet");
        SetListeners();
    }

    private void InitUI() {
        PostBlind = findViewById(R.id.PostBlindBtn);
        PostBlind.setOnClickListener(view -> {
            PostBlind();
            LeaveSeat.setVisibility(View.GONE);
        });

        RemoveBlind = findViewById(R.id.RemoveBlindBtn);
        RemoveBlind.setOnClickListener(view -> {
            RemoveBlind();
            LeaveSeat.setVisibility(View.VISIBLE);
        });

        Raise = findViewById(R.id.BtnRaise);
        Raise.setOnClickListener(view -> {
            if (!BetAmount.getText().toString().equals("")) {
                double currbet = Double.parseDouble(BetAmount.getText().toString());
                if (currbet >= 2 * Double.parseDouble(action[2])) {
                    SetOffButtons();
                    SendAction("Raise", currbet, Player);
                } else
                    Toast.makeText(getBaseContext(), "Raise must be at least 2x the bet", Toast.LENGTH_LONG).show();
            }
        });
        Bet = findViewById(R.id.BtnBet);
        Bet.setOnClickListener(view -> {
            if (!BetAmount.getText().toString().equals("")) {
                SetOffButtons();
                double currbet = Double.parseDouble(BetAmount.getText().toString());
                SendAction("Bet", currbet, Player);
            }
        });
        Fold = findViewById(R.id.BtnFold);
        Fold.setOnClickListener(view -> {
            SetOffButtons();
            ActionSeat.setValue(Player.getSeat() + ",Fold," + CurrentPot);
            PokerUtilities.SetActionLabel("Fold", Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player);
        });
        Check = findViewById(R.id.BtnCheck);
        Check.setOnClickListener(view -> {
            PokerUtilities.SetActionLabel("Check ", Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player);
            ActionSeat.setValue(Player.getSeat() + ",Check," + 0);
            SetOffButtons();
            if (Player.getDealer() && !CurrStreet.equals("Pre")) {
                NextStreet();
            } else if (!Player.getDealer() && CurrStreet.equals("Pre")) {
                NextStreet();
            }
        });
        Call = findViewById(R.id.BtnCall);
        Call.setOnClickListener(view -> {
            SetOffButtons();
            Double currbet = Double.parseDouble(action[2]);
            SendAction("Call", currbet, Player);
        });
        TakeSeat = findViewById(R.id.TakeSeat);
        TakeSeat.setOnClickListener(view -> {
            if (Seat1Name.getText().toString().equals("")) {
                setStack("Seat1");
                Player.setSeat("Seat1");
                numPlayers++;
                NumRef.setValue(numPlayers);
                Seat1Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", 0.0) + "");
            } else if (Seat2Name.getText().toString().equals("")) {
                setStack("Seat2");
                Player.setSeat("Seat2");
                Seat2Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", 0.0) + "");
                numPlayers++;
                NumRef.setValue(numPlayers);
            }
        });
        LeaveSeat = findViewById(R.id.LeaveSeat);
        LeaveSeat.setOnClickListener(view -> {
            if (Player.getSeat().equals("Seat1"))
                Seat1Ref.setValue("empty");
            else
                Seat2Ref.setValue("empty");
            SendMessage(Player.getUsername() + " has left the table");
            Player.addtoBank(Player.getStack());
            Player.setSeat("NoSeat");
            numPlayers--;
            Players.child("Seat").setValue("empty");
            NumRef.setValue(numPlayers);
            Players.child("BankRoll").setValue(Player.getBankroll());
            LeaveSeat.setVisibility(View.GONE);
            TakeSeat.setVisibility(View.VISIBLE);
            PostBlind.setVisibility(View.GONE);
            SetOffButtons();
            Toast.makeText(getBaseContext(), Player.getBankroll() + "", Toast.LENGTH_LONG).show();
        });
        BetAmount = findViewById(R.id.BetText);
        BetAmount.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(BetAmount.getWindowToken(), 0);
                Integer prog = (int) Math.round(Double.parseDouble(BetAmount.getText().toString()));
                SeekBet.setProgress((int) Math.round(prog / Player.getStack() * 100000));
            }
            return true;
        });
        SeekBet = findViewById(R.id.BetSlider);
        SeekBet.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double bet;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                bet = i / 100000.0;
                BetAmount.setText(String.format(Locale.ENGLISH, "%.2f", bet * Player.getStack()) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                BetAmount.setText(String.format(Locale.ENGLISH, "%.2f", bet * Player.getStack()) + "");
            }
        });

        MessageHistory = findViewById(R.id.MessageHistory);
        MessageHistory.setMovementMethod(new ScrollingMovementMethod());

        ToSend = findViewById(R.id.MessageBox);
        ToSend.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                String message = ToSend.getText().toString();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ToSend.getWindowToken(), 0);
                SendMessage(Player.getUsername() + ": " + message);
                ToSend.setText("");
                setBottom();
                return true;
            }
            return false;
        });
        SetOffButtons();
        LeaveSeat.setVisibility(View.GONE);

    }

    private void SetListeners() {

        BoardStreet.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] BoardCards;
                CurrStreet = dataSnapshot.child("Street").getValue().toString();
                BoardCards = dataSnapshot.child("Board").getValue().toString().split(",");
                switch (CurrStreet) {
                    case "Flop":
                        if (!Player.getDealer() && !AllIn)
                            SetNoBetButtons();
                        else
                            SetOffButtons();
                        AddCardDisplay(Streets.get(0), BoardCards[0]);
                        AddCardDisplay(Streets.get(1), BoardCards[1]);
                        AddCardDisplay(Streets.get(2), BoardCards[2]);
                        ActionSeat.setValue("empty");
                        break;
                    case "Turn":
                        if (!Player.getDealer() && !AllIn)
                            SetNoBetButtons();
                        else
                            SetOffButtons();
                        AddCardDisplay(Streets.get(3), BoardCards[3]);
                        ActionSeat.setValue("empty");
                        break;
                    case "River":
                        if (!Player.getDealer() && !AllIn)
                            SetNoBetButtons();
                        else
                            SetOffButtons();
                        AddCardDisplay(Streets.get(4), BoardCards[4]);
                        ActionSeat.setValue("empty");
                        break;
                    case "ShowDown":
                        if (Player.getDealer())
                            for (String x : BoardCards) {
                                Card toAdd = new Card(x.charAt(1), x.charAt(0));
                                S1.addCard(toAdd);
                                S2.addCard(toAdd);
                            }
                        AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(0), cref1[0]);
                        AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(1), cref1[1]);
                        AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(0), cref2[0]);
                        AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(1), cref2[1]);
                        ShowDown();
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ActionSeat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                action = dataSnapshot.getValue().toString().split(",");
                if (!Player.getSeat().equals(action[0]) && action.length > 1 && !Player.getSeat().equals("NoSeat")) {
                    if (action[1].equals("Bet") || action[1].equals("Raise") && !AllIn) {
                        PokerUtilities.SetActionLabelVillian(action[1] + " " + action[2], Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0]);
                        if (Double.parseDouble(action[2]) >= Player.getStack()) {
                            Call.setText("Call " + Player.getStack());
                            SetAllInButtons(action[2]);
                        } else {
                            SetBetButtons();
                            Call.setText("Call " + action[2]);
                        }
                    } else if (action[1].equals("Fold")) {
                        SetOffButtons();
                        PokerUtilities.SetActionLabelVillian("Fold", Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0]);
                        Payout("Fold");
                    } else if (action[1].equals("Call")) {
                        SetNoBetButtons();
                        PokerUtilities.SetActionLabelVillian("Call " + action[2], Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0]);
                    } else if (action[1].equals("Check")) {
                        SetNoBetButtons();
                        PokerUtilities.SetActionLabelVillian("Check", Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0]);
                    } else if (action[1].equals("AllIn")) {
                        AllIn = true;
                        SetAllInButtons(action[2]);
                        PokerUtilities.SetActionLabelVillian("AllIn " + action[2], Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0]);
                    }
                }
                if (Player.getSeat().equals(action[0]) && action[1].equals("Wins")) {
                    Payout("Wins");
                } else if (action[0].equals("Split")) {
                    Payout("Split");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Seat1Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue().toString().equals("empty")) {
                    if (Player.getSeat().equals("Seat2") && !Seat1[1].equals(""))
                        VillianStack = Double.parseDouble(Seat1[1]);
                    Seat1 = dataSnapshot.getValue().toString().split(",");
                    Seat1Name.setText(Seat1[0]);
                    Seat1Chips.setText("$ " + Seat1[1]);
                } else {
                    Seat1Name.setText("");
                    Seat1Chips.setText("");
                    VillianStack = 0.0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Seat2Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue().toString().equals("empty")) {
                    if (Player.getSeat().equals("Seat1") && !Seat2[1].equals(""))
                        VillianStack = Double.parseDouble(Seat2[1]);
                    Seat2 = dataSnapshot.getValue().toString().split(",");
                    Seat2Name.setText(Seat2[0]);
                    Seat2Chips.setText("$ " + Seat2[1]);
                } else {
                    Seat2[0] = "";
                    Seat2[1] = "";
                    Seat2Name.setText("");
                    Seat2Chips.setText("");
                    VillianStack = 0.0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        NumRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("2") || !Player.getSeat().equals("NoSeat"))
                    TakeSeat.setVisibility(View.GONE);
                else
                    TakeSeat.setVisibility(View.VISIBLE);
                numPlayers = Integer.parseInt(dataSnapshot.getValue().toString());
                if (dataSnapshot.getValue().toString().equals("2") && Player.getSeat().equals("Seat1")) {
                    Random rand = new Random();
                    if (rand.nextInt(10000) > 5000)
                        Button.setValue("Seat2");
                    else
                        Button.setValue("Seat1");
                } else
                    Button.setValue("empty");
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        PotRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Pot.setText(String.format(Locale.ENGLISH, "Pot : $ %.2f", Double.parseDouble(dataSnapshot.getValue().toString())));
                CurrentPot = Double.parseDouble(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        BlindsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (Boolean.parseBoolean(dataSnapshot.child("Big").getValue().toString()) && Boolean.parseBoolean(dataSnapshot.child("Small").getValue().toString())) {
                    StartHand();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Message.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue().toString().equals("empty")) {
                    MessageHistory.append(dataSnapshot.getValue().toString() + "\n");
                    setBottom();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("Seat1")) {
                    Dealer1.setVisibility(View.VISIBLE);
                    Dealer2.setVisibility(View.INVISIBLE);
                    if (Player.getSeat().equals("Seat1"))
                        Player.setDealer(true);
                    else
                        Player.setDealer(false);
                } else if (dataSnapshot.getValue().toString().equals("Seat2")) {
                    Dealer2.setVisibility(View.VISIBLE);
                    Dealer1.setVisibility(View.INVISIBLE);
                    if (Player.getSeat().equals("Seat2"))
                        Player.setDealer(true);
                    else
                        Player.setDealer(false);
                } else {
                    Dealer1.setVisibility(View.INVISIBLE);
                    Dealer2.setVisibility(View.INVISIBLE);
                    Button.setValue("empty");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Ongoing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("true") && !Player.getSeat().equals("NoSeat")) {
                    LeaveSeat.setVisibility(View.GONE);
                    RemoveBlind.setVisibility(View.GONE);
                } else if (!Player.getSeat().equals("NoSeat") && dataSnapshot.getValue().toString().equals("false"))
                    LeaveSeat.setVisibility(View.VISIBLE);
                inHand = Boolean.parseBoolean(dataSnapshot.getValue().toString());
                if (!inHand)
                    EndHand();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SeatCardsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (inHand) {
                    cref1 = dataSnapshot.child("Seat1").getValue().toString().split(",");
                    cref2 = dataSnapshot.child("Seat2").getValue().toString().split(",");

                    if (Player.getSeat().equals("NoSeat")) {
                        AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(0), "back");
                        AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(1), "back");

                        AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(0), "back");
                        AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(1), "back");
                    } else {
                        if (Player.getSeat().equals("Seat1")) {
                            AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(0), cref1[0]);
                            AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(1), cref1[1]);
                            AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(0), "back");
                            AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(1), "back");
                        }
                        if (Player.getSeat().equals("Seat2")) {
                            AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(0), "back");
                            AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(1), "back");
                            AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(0), cref2[0]);
                            AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(1), cref2[1]);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void InitSeats() {
        Dealer1 = findViewById(R.id.Seat1Dealer);
        Dealer2 = findViewById(R.id.Seat2Dealer);
        StakesText = findViewById(R.id.StakesText);
        Pot = findViewById(R.id.Pot);
        RoomDeets = findViewById(R.id.RoomDeets);
        TextView handStrength = findViewById(R.id.HandStrength);
        Seat1Name = findViewById(R.id.Seat1Name);
        Seat1Chips = findViewById(R.id.Seat1ChipAction);
        Seat2Name = findViewById(R.id.Seat2Name);
        Seat2Chips = findViewById(R.id.Seat2ChipAction);
        TableText = findViewById(R.id.RoomText);

        SeatCards = new ArrayList<>();
        SeatCards.add(findViewById(R.id.SeatOne));
        SeatCards.add(findViewById(R.id.SeatTwo));

        Streets = new ArrayList<>();
        Streets.add(findViewById(R.id.Flop1));
        Streets.add(findViewById(R.id.Flop2));
        Streets.add(findViewById(R.id.Flop3));
        Streets.add(findViewById(R.id.Turn));
        Streets.add(findViewById(R.id.River));
    }

    private void setBottom() {
        if (MessageHistory.getLayout() != null) {
            int scroll = MessageHistory.getLayout().getLineTop(MessageHistory.getLineCount()) - MessageHistory.getHeight();
            if (scroll > 0)
                MessageHistory.scrollTo(0, scroll);
            else
                MessageHistory.scrollTo(0, 0);
        }
    }

    private void AddCardDisplay(ImageView card, String toAdd) {
        if (!toAdd.equals("NA")) {
            card.setVisibility(View.VISIBLE);
            int id = getResources().getIdentifier(toAdd, "drawable", getPackageName());
            card.setImageResource(id);
        }
    }

    private void ResetCards() {

        for (int i = 0; i < SeatCards.size(); i++) {
            for (int k = 0; k < 2; k++) {
                SeatCards.get(i).getChildAt(k).setVisibility(View.INVISIBLE);
            }
        }
        for (int i = 0; i < 5; i++)
            Streets.get(i).setVisibility(View.INVISIBLE);


    }

    //move seat inc and assign before stack selection
    private void setStack(final String Seat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        stack = new EditText(this);
        stack.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setMessage("Min: " + Min + " Max: " + Max)
                .setView(stack)
                .setCancelable(false)
                .setPositiveButton("Join", (dialog, id) -> {
                    if (Double.parseDouble(stack.getText().toString()) <= Max &&
                            Double.parseDouble(stack.getText().toString()) >= Min) {
                        Player.setStack(Double.parseDouble(stack.getText().toString()));
                        SendMessage(Player.getUsername() + " has joined the table");
                        Players.child("Seat").setValue(Seat);
                        PostBlind.setVisibility(View.VISIBLE);
                        LeaveSeat.setVisibility(View.VISIBLE);
                        TakeSeat.setVisibility(View.GONE);
                        if (Seat.equals("Seat1"))
                            Seat1Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");

                        else
                            Seat2Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");
                    } else {
                        Toast.makeText(getBaseContext(), "Must bet more than min and less than max buy in", Toast.LENGTH_SHORT).show();
                        cancelReserveSeat();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Leave", (dialog, id) -> {
                    cancelReserveSeat();
                    dialog.cancel();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelReserveSeat() {
        numPlayers--;
        NumRef.setValue(numPlayers);
        if (Player.getSeat().equals("Seat1"))
            Seat1Ref.setValue("empty");
        else if (Player.getSeat().equals("Seat2"))
            Seat2Ref.setValue("empty");
        Player.setSeat("NoSeat");
    }

    private void PostBlind() {
        if (Player.getDealer()) {
            Player.removeFromStack(SmallBlind);
            CurrentPot += SmallBlind;
            Pot.setText("$ " + CurrentPot);
            BlindsRef.child("Small").setValue(true);
        } else {
            Player.removeFromStack(BigBlind);
            CurrentPot += BigBlind;
            Pot.setText("$ " + CurrentPot);
            BlindsRef.child("Big").setValue(true);
        }
        PotRef.setValue(CurrentPot);
        if (Player.getSeat().equals("Seat1"))
            Seat1Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");

        else
            Seat2Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");

        PostBlind.setVisibility(View.GONE);
        RemoveBlind.setVisibility(View.VISIBLE);
    }

    private void RemoveBlind() {
        if (Player.getDealer()) {
            Player.addToStack(SmallBlind);
            CurrentPot -= SmallBlind;
            Pot.setText("$ " + CurrentPot);
            BlindsRef.child("Small").setValue(false);
        } else {
            Player.addToStack(BigBlind);
            CurrentPot -= BigBlind;
            Pot.setText("$ " + CurrentPot);
            BlindsRef.child("Big").setValue(false);
        }
        PotRef.setValue(CurrentPot);
        if (Player.getSeat().equals("Seat1"))
            Seat1Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");

        else
            Seat2Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");

        RemoveBlind.setVisibility(View.GONE);
        PostBlind.setVisibility(View.VISIBLE);
    }

    private void SetOffButtons() {
        Raise.setVisibility(View.GONE);
        Bet.setVisibility(View.GONE);
        Fold.setVisibility(View.GONE);
        Check.setVisibility(View.GONE);
        Call.setVisibility(View.GONE);
        SeekBet.setVisibility(View.GONE);
        BetAmount.setVisibility(View.GONE);
    }

    private void SetBetButtons() {
        Bet.setVisibility(View.GONE);
        Check.setVisibility(View.GONE);
        Raise.setVisibility(View.VISIBLE);
        Fold.setVisibility(View.VISIBLE);
        Call.setVisibility(View.VISIBLE);
        SeekBet.setVisibility(View.VISIBLE);
        BetAmount.setVisibility(View.VISIBLE);
    }

    private void SetNoBetButtons() {
        Raise.setVisibility(View.GONE);
        Fold.setVisibility(View.GONE);
        Call.setVisibility(View.GONE);
        SeekBet.setVisibility(View.VISIBLE);
        Check.setVisibility(View.VISIBLE);
        Bet.setVisibility(View.VISIBLE);
        BetAmount.setVisibility(View.VISIBLE);
    }

    private void SetAllInButtons(String bet) {
        SetOffButtons();
        Call.setVisibility(View.VISIBLE);
        Fold.setVisibility(View.VISIBLE);
        Call.setText("Call " + bet);
    }

    private void StartHand() {
        if (Player.getDealer()) {
            BlindsRef.child("Big").setValue(false);
            BlindsRef.child("Small").setValue(false);
            Ongoing.setValue(true);
            SendMessage("Starting Hand");
            ShuffleDeck();
            SetBetButtons();
            BoardStreet.child("Street").setValue("Pre");
        } else if (!Player.getSeat().equals("NoSeat")) {

            ActionSeat.setValue(Player.getSeat() + ",Bet," + SmallBlind);
        }
    }

    @Override
    public void onBackPressed() {
        if (!Player.getSeat().equals("NoSeat") && !inHand) {
            if (Player.getSeat().equals("Seat1"))
                Seat1Ref.setValue("empty");
            else
                Seat2Ref.setValue("empty");
            LeaveSeat.setVisibility(View.GONE);
            SendMessage(Player.getUsername() + " has left the table");
            Players.child("Seat").setValue("empty");
            Player.setSeat("NoSeat");
            TakeSeat.setVisibility(View.VISIBLE);
            numPlayers--;
            NumRef.setValue(numPlayers);
            ResetCards();
        } else if (!Player.getSeat().equals("NoSeat") && inHand) {
            Toast.makeText(getBaseContext(), "Must finish current Hand before leaving Table", Toast.LENGTH_LONG).show();
        } else
            super.onBackPressed();
    }

    private void ShuffleDeck() {
        ArrayList<Card> Deck = new ArrayList<>();
        for (int i = 0; i < Suits.size(); i++) {
            for (int k = 1; k < 14; k++) {
                Deck.add(new Card(k, Suits.get(i)));
            }
        }
        Collections.shuffle(Deck);
        S1 = new PokerHand();
        S2 = new PokerHand();

        S1.addCard(Deck.get(0));
        S1.addCard(Deck.get(1));

        S2.addCard(Deck.get(2));
        S2.addCard(Deck.get(3));


        SeatCardsRef.child("Seat1").setValue(Deck.get(0).toString() + "," + Deck.get(1).toString());
        SeatCardsRef.child("Seat2").setValue(Deck.get(2).toString() + "," + Deck.get(3).toString());
        BoardStreet.child("Board").setValue(Deck.get(4).toString() + "," + Deck.get(5).toString() + "," + Deck.get(6).toString() + "," + Deck.get(7).toString() + "," + Deck.get(8).toString());
    }

    private void SendMessage(String message) {
        Message.setValue(message);
        Message.setValue("empty");
    }

    private void NextStreet() {
        switch (CurrStreet) {
            case "Pre":
                CurrStreet = "Flop";
                break;
            case "Flop":
                CurrStreet = "Turn";
                break;
            case "Turn":
                CurrStreet = "River";
                break;
            case "River":
                CurrStreet = "ShowDown";
                break;
            case "ShowDown":
                break;
        }
        BoardStreet.child("Street").setValue(CurrStreet);
    }

    private void RunOutBoard() {
        final Handler handle = new Handler();
        while (!CurrStreet.equals("ShowDown")) {
            if (Player.getSeat().equals("Seat1"))
                handle.postDelayed(() -> {
                    SendMessage(CurrStreet);
                    NextStreet();
                }, 1500);
        }
    }

    private void ShowDown() {
        if (Player.getDealer()) {
            int winner = PokerUtilities.CheckWinner(S1, S2, Pot);
            if (winner == 1) ActionSeat.setValue("Seat1,Wins," + CurrentPot);
            if (winner == 2) ActionSeat.setValue("Seat2,Wins," + CurrentPot);
            if (winner == 0) ActionSeat.setValue("Split");
            else ActionSeat.setValue("somethingiswrong," + CurrentPot);
        }
    }

    private void Payout(String type) {
        if (!type.equals("Split")) {
            if (Player.getSeat().equals("Seat1")) {
                Pot.setText("Winner");
                SendMessage("Seat 1 Wins: " + CurrentPot);
                Pot.postDelayed(() -> Ongoing.setValue(false), 2500);
                Player.addToStack(CurrentPot);
                Seat1Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");
            } else if (Player.getSeat().equals("Seat2")) {
                SendMessage("Seat 2 Wins: " + CurrentPot);
                Pot.setText("Winner");
                Pot.postDelayed(() -> Ongoing.setValue(false), 2500);
                Player.addToStack(CurrentPot);
                Seat2Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");
            }
        } else {
            double split = CurrentPot - (CurrentPot / 2);
            CurrentPot = CurrentPot - split;
            if (Player.getDealer()) {
                SendMessage("Split Pot");
                Pot.setText("Split Pot");
                Pot.postDelayed(() -> Ongoing.setValue(false), 2500);
            }
            if (Player.getSeat().equals("Seat1")) {
                Player.addToStack(CurrentPot);
                Seat1Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");
            } else if (Player.getSeat().equals("Seat2")) {
                Player.addToStack(split);
                Seat2Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + "");
            }
        }
        PotRef.setValue(0);
    }

    private void EndHand() {
        if (!Player.getSeat().equals("NoSeat")) {
            AllIn = false;
            PostBlind.setVisibility(View.VISIBLE);
            if (Player.getDealer()) {
                if (Player.getSeat().equals("Seat1"))
                    Button.setValue("Seat2");
                else
                    Button.setValue("Seat1");

                ActionSeat.setValue("empty");
                BoardStreet.child("Street").setValue("empty");
                BoardStreet.child("Board").setValue("NA,NA,NA,NA,NA");
                SeatCardsRef.child("Seat1").setValue("NA,NA");
                SeatCardsRef.child("Seat2").setValue("NA,NA");
            }
            Player.setDealer(!Player.getDealer());
            SetOffButtons();
        }
        ResetCards();
    }

    private void SendAction(String Action, Double bet, Player play) {
        double rebate = 0.0;
        if (bet >= play.getStack()) {
            rebate = bet;
            bet = play.getStack();
            rebate -= bet;
        }
        play.removeFromStack(bet);
        CurrentPot += bet;
        switch (Action) {
            case "Call":
                if (play.getStack() == 0 || VillianStack == 0) {
                    ActionSeat.setValue(Player.getSeat() + ",CallIn," + bet);
                    RunOutBoard();
                    Action = "All In";
                } else
                    ActionSeat.setValue(Player.getSeat() + ",Call," + bet);
                if ((!CurrStreet.equals("Pre") || bet > SmallBlind))
                    NextStreet();
                break;
            default:
                if (play.getStack() == 0) {
                    ActionSeat.setValue(Player.getSeat() + ",AllIn," + bet);
                    Action = "All In";
                } else
                    ActionSeat.setValue(Player.getSeat() + "," + Action + "," + bet);
                break;
        }

        PokerUtilities.SetActionLabel(Action + ": " + bet, Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, play);
        if (play.getSeat().equals("Seat1"))
            Seat1Name.postDelayed(() -> Seat1Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + ""), 1500);
        else
            Seat2Name.postDelayed(() -> Seat2Ref.setValue(Player.getUsername() + "," + String.format(Locale.ENGLISH, "%.2f", Player.getStack()) + ""), 1500);
        Pot.setText(getString(R.string.PotDisplay, CurrentPot));
        PotRef.setValue(CurrentPot);
        SetOffButtons();


        //TODO implement hand replay
        //TODO generate hand equities without simulation
        //TODO if allin for more than opponent situation
        //TODO Implement handName with cards, "Two Pair, Queens and threes"
        //TODO Allin logic
        //TODO add reload after all in and remove post blind
    }
}
