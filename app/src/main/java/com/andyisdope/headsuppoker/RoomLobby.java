package com.andyisdope.headsuppoker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
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

@SuppressWarnings("ALL")
public class RoomLobby extends AppCompatActivity {

    private ListView Cash, Tournament;
    private ArrayList<HashMap<String, String>> CashGames;
    private ArrayList<HashMap<String, String>> Tournaments;
    private final FirebaseDatabase FB = FirebaseDatabase.getInstance();
    private final DatabaseReference CashDB = FB.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables");
    private final DatabaseReference TourDB = FB.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Tournaments");
    private DatabaseReference BankrollDB;
    private HashMap<String, String> Cgames = new HashMap<>();
    private TextView Game;
    private TextView Stakes;
    private TextView PlayerStacks;
    private TextView Min;
    private TextView Levels;
    private TextView Starting;
    private TextView Prize;
    private TextView BankRollText;
    private TextView PlayerNameText;
    private Button Register;
    private Button GoRoom;
    private Player player;
    private Activity Current;
    private String TableName;


    private void InitUI() {

        Register = (Button) findViewById(R.id.Register);
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.getBankroll() > Double.parseDouble(Min.getText().toString().substring(8))) {
                    Toast.makeText(getBaseContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                    player.removefromBank(Double.parseDouble(Min.getText().toString().substring(8)));
                    BankrollDB.setValue(player.getBankroll());
                    //new tourney activity here

                } else
                    Toast.makeText(getBaseContext(), "Not enough Chips to register ", Toast.LENGTH_SHORT).show();


            }
        });
        GoRoom = (Button) findViewById(R.id.GoToRoom);
        GoRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });
        Button cashier = (Button) findViewById(R.id.Cashier);
        cashier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cashier dialog pop up with current chip count and refresh button
            }
        });
        TextView roomName = (TextView) findViewById(R.id.RoomName);
        BankRollText = (TextView) findViewById(R.id.BankrollText);
        PlayerNameText = (TextView) findViewById(R.id.PlayerNameText);
        Game = (TextView) findViewById(R.id.Game);
        Stakes = (TextView) findViewById(R.id.Stakes);
        PlayerStacks = (TextView) findViewById(R.id.PlayersStacks);
        Min = (TextView) findViewById(R.id.Minimum);
        Levels = (TextView) findViewById(R.id.Levels);
        Starting = (TextView) findViewById(R.id.Starting);
        Prize = (TextView) findViewById(R.id.Prize);
        Cash = (ListView) findViewById(R.id.CashGames);
        Tournament = (ListView) findViewById(R.id.Tournaments);

        TabHost tabs = (TabHost) findViewById(R.id.RoomList);
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
                BankRollText.setText(getString(R.string.ChipBankRoll, player.getBankroll()));
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
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RoomLobby.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_lobby);
        InitDB(this);
        InitUI();
        CashGames = new ArrayList<>();
        Tournaments = new ArrayList<>();

        Tournament.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

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
            }

        });


        Cash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
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
            }

        });

    }
}
