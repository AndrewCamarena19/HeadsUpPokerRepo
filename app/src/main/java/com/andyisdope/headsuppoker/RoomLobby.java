package com.andyisdope.headsuppoker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class RoomLobby extends AppCompatActivity {

    private ListView Cash, Tournament;
    private ArrayList<HashMap<String, String>> CashGames, Tournaments;
    private final FirebaseDatabase FB = FirebaseDatabase.getInstance();
    private final DatabaseReference CashDB = FB.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables");
    private final DatabaseReference TourDB = FB.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Tournaments");
    private DatabaseReference BankrollDB;
    private HashMap<String, String> Cgames = new HashMap<>();
    private TextView Game, Stakes, PlayerStacks, Min, Levels, Starting, Prize, BankRollText, PlayerNameText;
    private Button Register, GoRoom;
    private Player player;
    private String TableName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_lobby);
        InitDB(this);
        InitUI();
        CashGames = new ArrayList<>();
        Tournaments = new ArrayList<>();

        Tournament.setOnItemClickListener((parent, view, position, id) -> {

            Game.setText(Tournaments.get(position).get("Type") + ": " + Tournaments.get(position).get("Game"));
            Stakes.setText("Blinds: $" + Tournaments.get(position).get("Stakes"));
            PlayerStacks.setText(Tournaments.get(position).get("Seat1") + "\n" + Tournaments.get(position).get("Seat2"));
            Min.setText("Buy in: $" + Tournaments.get(position).get("Min"));
            Levels.setText("Blind Levels: " + Tournaments.get(position).get("LevelTime"));
            Starting.setText("Starting Stack: $" + Tournaments.get(position).get("Starting Stack"));
            Prize.setText("Prize: " + Tournaments.get(position).get("Prize"));
            TableName = Tournaments.get(position).get("Table");
            Register.setVisibility(View.VISIBLE);
            GoRoom.setVisibility(View.GONE);
        });

        Cash.setOnItemClickListener((parent, view, position, id) -> {
            Game.setText(CashGames.get(position).get("Type") + ": " + CashGames.get(position).get("Game"));
            Stakes.setText("Blinds: $" + CashGames.get(position).get("Stakes"));
            PlayerStacks.setText(CashGames.get(position).get("Seat1") + "\n" + CashGames.get(position).get("Seat2"));
            Min.setText("Minimum Buyin: $" + CashGames.get(position).get("Min"));
            Prize.setText("");
            Starting.setText("");
            Levels.setText("");
            TableName = CashGames.get(position).get("Table");
            Register.setVisibility(View.GONE);
            GoRoom.setVisibility(View.VISIBLE);
        });

    }

    private void InitUI() {

        Register = findViewById(R.id.Register);
        Register.setOnClickListener(view -> {
            if (player.getBankroll() > Double.parseDouble(Min.getText().toString().substring(8))) {
                Toast.makeText(getBaseContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                player.removefromBank(Double.parseDouble(Min.getText().toString().substring(8)));
                BankrollDB.setValue(player.getBankroll());
                //new tourney activity here

            } else
                Toast.makeText(getBaseContext(), "Not enough Chips to register ", Toast.LENGTH_SHORT).show();


        });
        GoRoom = findViewById(R.id.GoToRoom);
        GoRoom.setOnClickListener(view -> {
            Toast.makeText(getBaseContext(), "Going to Table", Toast.LENGTH_LONG).show();
            Intent nIntent = new Intent(getBaseContext(), Table.class);
            nIntent.putExtra("UID", player.getUID());
            nIntent.putExtra("TableName", TableName);
            nIntent.putExtra("Game",Game.getText().toString());
            nIntent.putExtra("Stakes", Stakes.getText().toString());
            nIntent.putExtra("PlayerName", player.getUsername());
            nIntent.putExtra("Bankroll", player.getBankroll());
            nIntent.putExtra("Min", Min.getText().toString().substring(16));
            startActivity(nIntent);
        });
        Button cashier = findViewById(R.id.Cashier);
        cashier.setOnClickListener(view -> {
            // Cashier dialog pop up with current chip count and refresh button
            //TODO implement Cashier button
        });
        BankRollText = findViewById(R.id.BankrollText);
        PlayerNameText = findViewById(R.id.PlayerNameText);
        Game = findViewById(R.id.Game);
        Stakes = findViewById(R.id.Stakes);
        PlayerStacks = findViewById(R.id.PlayersStacks);
        Min = findViewById(R.id.Minimum);
        Levels = findViewById(R.id.Levels);
        Starting = findViewById(R.id.Starting);
        Prize = findViewById(R.id.Prize);
        Cash = findViewById(R.id.CashGames);
        Tournament = findViewById(R.id.Tournaments);

        TabHost tabs = findViewById(R.id.RoomList);
        tabs.setup();
        TabHost.TabSpec spec = tabs.newTabSpec("CashGames");
        spec.setContent(R.id.CashGames);
        spec.setIndicator("CashGames");
        tabs.addTab(spec);
        spec = tabs.newTabSpec("Tournaments");
        spec.setContent(R.id.Tournaments);
        spec.setIndicator("Tournaments");
        tabs.addTab(spec);

        Register.setVisibility(View.GONE);
        GoRoom.setVisibility(View.GONE);
    }

    private void InitDB(Activity currs) {
        Intent intent = getIntent();
        player = new Player(intent.getStringExtra("UID"));
        final Activity curr = currs;
        BankrollDB = FB.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Users/" + player.getUID() + "/BankRoll");
        DatabaseReference playerDB = FB.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Users/" + player.getUID());
        playerDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                player.setUsername(dataSnapshot.child("Username").getValue().toString());
                player.setBankroll(Double.parseDouble(dataSnapshot.child("BankRoll").getValue().toString()));
                BankRollText.setText("BankRoll: $" + String.format(Locale.ENGLISH, "%.2f", player.getBankroll()) + "");
                PlayerNameText.setText(player.getUsername());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TourDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Tournaments = new ArrayList<>();
                for (DataSnapshot snaps : dataSnapshot.getChildren()) {
                    Cgames = new HashMap<>();
                    Cgames.put("Table", snaps.getKey());
                    for (DataSnapshot Table : snaps.getChildren()) {
                        Cgames.put(Table.getKey(), Table.getValue().toString());
                    }
                    Tournaments.add(Cgames);
                }
                ListViewAdapter Tadapt = new ListViewAdapter(curr, Tournaments);
                Tournament.setAdapter(Tadapt);
                Tadapt.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        CashDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CashGames = new ArrayList<>();
                for (DataSnapshot snaps : dataSnapshot.getChildren()) {
                    Cgames = new HashMap<>();
                    Cgames.put("Table", snaps.getKey());
                    for (DataSnapshot Table : snaps.getChildren()) {
                        Cgames.put(Table.getKey(), Table.getValue().toString());
                    }
                    CashGames.add(Cgames);
                }
                ListViewAdapter Cadapt = new ListViewAdapter(curr, CashGames);
                Cash.setAdapter(Cadapt);
                Cadapt.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> RoomLobby.this.finish())
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
