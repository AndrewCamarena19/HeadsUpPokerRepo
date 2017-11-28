package com.andyisdope.headsuppoker;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
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
import java.util.Random;


public class Table extends AppCompatActivity {

    private SeekBar SeekBet;
    private EditText BetAmount, stack, ToSend;
    private Button Raise, TakeSeat, LeaveSeat, PostBlind, Bet, Fold, Check, Call;
    private ImageView Dealer1, Dealer2;
    private ArrayList<FrameLayout> SeatCards;
    private ArrayList<ImageView> Streets;
    private FirebaseDatabase mRef = FirebaseDatabase.getInstance();
    private DatabaseReference CurrentStreet, Message, Players, Board, Seat1Ref, Seat2Ref, SeatCardsRef, Button, BlindsRef, NumRef, PotRef, Ongoing, ActionSeat;
    private TextView Seat1Name, Seat1Chips, TableText, HandStrength, Seat2Name, Seat2Chips, Pot, RoomDeets, StakesText, MessageHistory;
    private String[] Seat1CardImages = new String[2];
    private String[] Seat2CardImages = new String[2];
    private String[] Seat1 = new String[2];
    private String[] Seat2 = new String[2];
    private String[] Blinds = new String[2];
    private String[] action = new String[3];
    private String[] BoardCards = new String[5];
    private ArrayList<String> Suits = new ArrayList<>(Arrays.asList("c", "s", "h", "d"));
    private ArrayList<Card> Deck = new ArrayList<>();
    private ArrayList<Card> BoardCheck = new ArrayList<>();
    private Player Player, Villian;
    private int numPlayers;
    private Double Min, Max, CurrentPot, SmallBlind, BigBlind;
    private String TableName, CurrStreet;
    private boolean inHand;


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
        inHand = false;
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
        Board = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Board");
        BlindsRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Blinds");
        Seat1Ref = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Seat1");
        Seat2Ref = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Seat2");
        SeatCardsRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Cards");
        Button = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Button");
        Ongoing = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Ongoing");
        Players = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Users/" + Player.getUID());
        NumRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Players");
        PotRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/Pot");
        CurrentStreet = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/Andy's Room/CurrentStreet");
        ActionSeat = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/" + TableName + "/ActionSeat");
        SetListeners();
    }

    private void InitUI() {
        PostBlind = (Button) findViewById(R.id.PostBlindBtn);
        PostBlind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostBlind();
                LeaveSeat.setVisibility(View.GONE);
            }
        });

        Raise = (Button) findViewById(R.id.BtnRaise);
        Raise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double currbet = Double.parseDouble(BetAmount.getText().toString());
                if (currbet >= 2 * Double.parseDouble(action[2])) {
                        Player.removeFromStack(currbet);
                        CurrentPot += currbet;
                        Pot.setText("$ " + CurrentPot);
                        PokerUtilities.SetActionLabel("Raise: " + currbet, Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player);
                        PotRef.setValue(CurrentPot);
                        ActionSeat.setValue(Player.getSeat() + ",Raise," + currbet);
                        BetAmount.setText("");
                        SetOffButtons();
                } else {
                    Toast.makeText(getBaseContext(), "Raise must be at least 2x the bet", Toast.LENGTH_LONG).show();
                }
            }
        });
        Bet = (Button) findViewById(R.id.BtnBet);
        Bet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Player.removeFromStack(Double.parseDouble(BetAmount.getText().toString()));
                CurrentPot += Double.parseDouble(BetAmount.getText().toString());
                Pot.setText("$ " + CurrentPot);
                PokerUtilities.SetActionLabel("Bet: " + BetAmount.getText().toString(), Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player);
                PotRef.setValue(CurrentPot);
                ActionSeat.setValue(Player.getSeat() + ",Bet," + BetAmount.getText().toString());
                BetAmount.setText("");
                SetOffButtons();
            }
        });
        Fold = (Button) findViewById(R.id.BtnFold);
        Fold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionSeat.setValue(Player.getSeat() + ",Fold," + CurrentPot);
                Ongoing.setValue(false);
                PostBlind.setVisibility(View.VISIBLE);
                if (Player.getDealer()) {
                    if (Player.getSeat().equals("Seat1"))
                        Button.setValue("Seat1");
                    else
                        Button.setValue("Seat1");
                }
                PokerUtilities.SetActionLabel("Fold", Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player);
                Player.setDealer(Player.getDealer());
                ActionSeat.setValue("empty");
                SetOffButtons();
                Board.setValue("NA,NA,NA,NA,NA");
                SeatCardsRef.child("Seat1").setValue("NA,NA");
                SeatCardsRef.child("Seat2").setValue("NA,NA");
                ResetCards();
            }
        });
        Check = (Button) findViewById(R.id.BtnCheck);
        Check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        Call = (Button) findViewById(R.id.BtnCall);
        Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Double currbet = Double.parseDouble(action[2]);
                    Player.removeFromStack(currbet);
                    CurrentPot += currbet;
                    Pot.setText("$ " + CurrentPot);
                    PokerUtilities.SetActionLabel("Call: " + currbet, Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player);
                    PotRef.setValue(CurrentPot);
                    ActionSeat.setValue(Player.getSeat() + ",Call," + currbet);
                    BetAmount.setText("");
                    SetOffButtons();
                    if(Player.getDealer() && !CurrStreet.equals("Pre"))
                    {
                        NextStreet();
                    }
            }
        });
        TakeSeat = (Button) findViewById(R.id.TakeSeat);
        TakeSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Seat1Name.getText().toString().equals("")) {
                    setStack("Seat1");
                } else if (Seat2Name.getText().toString().equals("")) {
                    setStack("Seat2");
                }
            }
        });
        LeaveSeat = (Button) findViewById(R.id.LeaveSeat);
        LeaveSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Player.getSeat().equals("Seat1"))
                    Seat1Ref.setValue("empty");
                else
                    Seat2Ref.setValue("empty");
                SendMessage(Player.getUsername() + " has left the table");
                Player.addtoBank(Player.getStack());
                Player.setSeat("NoSeat");
                Players.child("Seat").setValue("empty");
                Toast.makeText(getBaseContext(), Player.getBankroll() + "", Toast.LENGTH_LONG).show();
                numPlayers--;
                NumRef.setValue(numPlayers);
                LeaveSeat.setVisibility(View.GONE);
                TakeSeat.setVisibility(View.VISIBLE);
                PostBlind.setVisibility(View.GONE);
                Players.child("BankRoll").setValue(Player.getBankroll());
                SetOffButtons();
            }
        });
        BetAmount = (EditText) findViewById(R.id.BetText);
        BetAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Integer prog = (int) Math.round(Double.parseDouble(BetAmount.getText().toString()));
                if (i == EditorInfo.IME_ACTION_DONE)
                    SeekBet.setProgress(prog);
                return true;
            }
        });
        SeekBet = (SeekBar) findViewById(R.id.BetSlider);
        SeekBet.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double bet;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                bet = i / 100000.0;
                BetAmount.setText(String.format("%.2f", bet * Player.getStack()) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                BetAmount.setText(String.format("%.2f", bet * Player.getStack()) + "");
            }
        });

        MessageHistory = (TextView) findViewById(R.id.MessageHistory);
        MessageHistory.setMovementMethod(new ScrollingMovementMethod());

        ToSend = (EditText) findViewById(R.id.MessageBox);
        ToSend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
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
            }
        });
        SetOffButtons();
        LeaveSeat.setVisibility(View.GONE);

    }

    private void SetListeners() {

        CurrentStreet.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CurrStreet = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ActionSeat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                action = dataSnapshot.getValue().toString().split(",");
                if (!Player.getSeat().equals(action[0]) && action.length > 1) {
                    if (action[1].equals("Bet") || action[1].equals("Raise")) {
                        SetBetButtons();
                        Call.setText("Call " + action[2]);
                    } else if (action[1].equals("Fold")) {
                        if (Player.getSeat().equals("Seat1")) {
                            Seat1Ref.setValue(Player.getUsername() + "," + (Player.getStack() + CurrentPot));
                        } else
                            Seat2Ref.setValue(Player.getUsername() + "," + (Player.getStack() + CurrentPot));
                        PostBlind.setVisibility(View.VISIBLE);
                        PotRef.setValue(0);
                        ResetCards();
                    } else if (action[1].equals("Call") && CurrStreet.equals("Pre") && !Player.getDealer()) {
                        SetNoBetButtons();
                        NextStreet();

                    } else if (action[1].equals("Check")) {
                        SetNoBetButtons();
                    }
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
                    Seat1 = dataSnapshot.getValue().toString().split(",");
                    Seat1Name.setText(Seat1[0]);
                    Seat1Chips.setText("$ " + Seat1[1]);
                } else {
                    Seat1Name.setText("");
                    Seat1Chips.setText("");
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
                    Seat2 = dataSnapshot.getValue().toString().split(",");
                    Seat2Name.setText(Seat2[0]);
                    Seat2Chips.setText("$ " + Seat2[1]);
                } else {
                    Seat2Name.setText("");
                    Seat2Chips.setText("");
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
                if (dataSnapshot.getValue().toString().equals("2")) {
                    Random rand = new Random();
                    if (rand.nextInt(10000) > 5000)
                        Button.setValue("Seat1");
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
                Pot.setText(String.format("Pot : $ %.2f", Double.parseDouble(dataSnapshot.getValue().toString())));
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

        Board.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BoardCards = dataSnapshot.getValue().toString().split(",");
                for (int i = 0; i < 5; i++) {
                    AddCardDisplay(Streets.get(i), BoardCards[i]);
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
                if (dataSnapshot.getValue().toString().equals("true") && !Player.getSeat().equals("NoSeat"))
                    LeaveSeat.setVisibility(View.GONE);
                else if (!Player.getSeat().equals("NoSeat") && dataSnapshot.getValue().toString().equals("false"))
                    LeaveSeat.setVisibility(View.VISIBLE);
                inHand = Boolean.parseBoolean(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SeatCardsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (inHand && Player.getSeat().equals("NoSeat")) {
                    AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(0), "back");
                    AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(1), "back");

                    AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(0), "back");
                    AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(1), "back");
                }
                if (Player.getSeat().equals("Seat1") && inHand) {
                    //Player.setHand(new PokerHand(Deck.get(0), Deck.get(1)));
                    String[] cref = dataSnapshot.child("Seat1").getValue().toString().split(",");
                    AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(0), cref[0]);
                    AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(1), cref[1]);
                    AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(0), "back");
                    AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(1), "back");
                }
                if (Player.getSeat().equals("Seat2") && inHand) {
                    //Player.setHand(new PokerHand(Deck.get(2), Deck.get(3)));
                    String[] cref = dataSnapshot.child("Seat2").getValue().toString().split(",");
                    AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(0), "back");
                    AddCardDisplay((ImageView) SeatCards.get(0).getChildAt(1), "back");
                    AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(0), cref[0]);
                    AddCardDisplay((ImageView) SeatCards.get(1).getChildAt(1), cref[1]);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void InitSeats() {
        Dealer1 = (ImageView) findViewById(R.id.Seat1Dealer);
        Dealer2 = (ImageView) findViewById(R.id.Seat2Dealer);
        StakesText = (TextView) findViewById(R.id.StakesText);
        Pot = (TextView) findViewById(R.id.Pot);
        RoomDeets = (TextView) findViewById(R.id.RoomDeets);
        HandStrength = (TextView) findViewById(R.id.HandStrength);
        Seat1Name = (TextView) findViewById(R.id.Seat1Name);
        Seat1Chips = (TextView) findViewById(R.id.Seat1ChipAction);
        Seat2Name = (TextView) findViewById(R.id.Seat2Name);
        Seat2Chips = (TextView) findViewById(R.id.Seat2ChipAction);
        TableText = (TextView) findViewById(R.id.RoomText);

        SeatCards = new ArrayList<>();
        SeatCards.add((FrameLayout) findViewById(R.id.SeatOne));
        SeatCards.add((FrameLayout) findViewById(R.id.SeatTwo));

        Streets = new ArrayList<>();
        Streets.add((ImageView) findViewById(R.id.Flop1));
        Streets.add((ImageView) findViewById(R.id.Flop2));
        Streets.add((ImageView) findViewById(R.id.Flop3));
        Streets.add((ImageView) findViewById(R.id.Turn));
        Streets.add((ImageView) findViewById(R.id.River));
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

    private void setStack(final String Seat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        stack = new EditText(this);
        stack.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setMessage("Min: " + Min + " Max: " + Max)
                .setView(stack)
                .setCancelable(false)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Double.parseDouble(stack.getText().toString()) <= Max &&
                                Double.parseDouble(stack.getText().toString()) >= Min) {
                            Player.setStack(Double.parseDouble(stack.getText().toString()));
                            Player.setSeat(Seat);
                            numPlayers++;
                            if (Seat.equals("Seat1"))
                                Seat1Ref.setValue(Player.getUsername() + ", " + Player.getStack());
                            else
                                Seat2Ref.setValue(Player.getUsername() + ", " + Player.getStack());
                            SendMessage(Player.getUsername() + " has joined the table");
                            Players.child("Seat").setValue(Seat);
                            NumRef.setValue(numPlayers);
                            LeaveSeat.setVisibility(View.VISIBLE);
                            TakeSeat.setVisibility(View.GONE);
                            PostBlind.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getBaseContext(), "Must bet more than min and less than max buy in", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    }
                })
                .setNegativeButton("Leave", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
            Seat1Ref.setValue(Player.getUsername() + "," + Player.getStack());
        else
            Seat2Ref.setValue(Player.getUsername() + "," + Player.getStack());
        PostBlind.setVisibility(View.GONE);
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

    private void StartHand() {
        BlindsRef.child("Big").setValue(false);
        BlindsRef.child("Small").setValue(false);
        Ongoing.setValue(true);
        if (Player.getDealer()) {
            SendMessage("Starting Hand");
            ShuffleDeck();
            SetBetButtons();
        } else {

            ActionSeat.setValue(Player.getSeat() + ",Bet," + BigBlind);
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
        for (int i = 0; i < Suits.size(); i++) {
            for (int k = 1; k < 14; k++) {
                Deck.add(new Card(k, Suits.get(i)));
            }
        }
        Collections.shuffle(Deck);
        SeatCardsRef.child("Seat1").setValue(Deck.get(0).toString() + "," + Deck.get(1).toString());
        SeatCardsRef.child("Seat2").setValue(Deck.get(2).toString() + "," + Deck.get(3).toString());
        BoardCheck.add(Deck.get(4));
        BoardCheck.add(Deck.get(5));
        BoardCheck.add(Deck.get(6));
        BoardCheck.add(Deck.get(7));
        BoardCheck.add(Deck.get(8));

    }

    private void SendMessage(String message) {
        Message.setValue(message);
        Message.setValue("empty");
    }

    private void PreFlop() {

    }

    private void NextStreet()
    {
        switch (CurrStreet) {
            case "Pre":
                break;
            case "Flop":
                break;
            case "Turn":
                break;
            default:
                break;
        }
    }
    //TODO start preflop bet phases with ActionSeat field
}
