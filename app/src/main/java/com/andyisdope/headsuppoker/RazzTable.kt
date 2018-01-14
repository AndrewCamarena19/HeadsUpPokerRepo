package com.andyisdope.headsuppoker

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.firebase.database.*
import java.util.*

class RazzTable : AppCompatActivity() {

    private var stack: EditText? = null
    private var ToSend: EditText? = null
    private var Raise: Button? = null
    private var TakeSeat: Button? = null
    private var LeaveSeat: Button? = null
    private var PostBlind: Button? = null
    private var Bet: Button? = null
    private var Fold: Button? = null
    private var Check: Button? = null
    private var Call: Button? = null
    private var RemoveBlind: Button? = null
    private var Dealer1: ImageView? = null
    private var Dealer2: ImageView? = null
    private var SeatCards: ArrayList<FrameLayout>? = null
    private val Suits = ArrayList(Arrays.asList("c", "s", "h", "d"))
    private val mRef = FirebaseDatabase.getInstance()
    private var Message: DatabaseReference? = null
    private var Players: DatabaseReference? = null
    private var Seat1Ref: DatabaseReference? = null
    private var Seat2Ref: DatabaseReference? = null
    private var SeatCardsRef: DatabaseReference? = null
    private var BlindsRef: DatabaseReference? = null
    private var NumRef: DatabaseReference? = null
    private var PotRef: DatabaseReference? = null
    private var Ongoing: DatabaseReference? = null
    private var Bets: DatabaseReference? = null
    private var OTG: DatabaseReference? = null
    private var ActionSeat: DatabaseReference? = null
    private var Seat1Name: TextView? = null
    private var Seat1Chips: TextView? = null
    private var TableText: TextView? = null
    private var Seat2Name: TextView? = null
    private var Seat2Chips: TextView? = null
    private var Pot: TextView? = null
    private var RoomDeets: TextView? = null
    private var StakesText: TextView? = null
    private var MessageHistory: TextView? = null
    private var Seat1 = arrayOfNulls<String>(2)
    private var Seat2 = arrayOfNulls<String>(2)
    private var Blinds = arrayOfNulls<String>(2)
    private var cref1: Array<String>? = null
    private var cref2: Array<String>? = null
    private var action = arrayOfNulls<String>(3)
    private var Player: Player? = null
    private var numPlayers: Int = 0
    private var OTGNum: Int = 0
    private var Min: Double = 0.0
    private var Max: Double = 0.0
    private var CurrentPot: Double? = null
    private var SmallBlind: Double? = null
    private var BigBlind: Double? = null
    private var VillianStack: Double? = null
    private var TableName: String? = null
    private var CurrStreet: String? = null
    private var VillianName: String? = null
    private var inHand: Boolean = false
    private var AllIn: Boolean = false
    private var S1: RazzHand? = null
    private var S2: RazzHand? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_razz_table)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val intent = intent

        initSeats()
        getIntents(intent)
        getReferences()
        initUI()

        SmallBlind = java.lang.Double.parseDouble(Blinds[0]!!.substring(9))
        BigBlind = java.lang.Double.parseDouble(Blinds[1])
        Max = Min * 3
        Player!!.seat = "NoSeat"
        Player!!.dealer = false
        CurrStreet = ""
        VillianStack = 0.0
        VillianName = ""
    }

    private fun getIntents(intent: Intent?) {
        TableText!!.text = intent!!.getStringExtra("TableName")
        RoomDeets!!.text = (intent.getStringExtra("Game"))
        StakesText!!.text = (intent.getStringExtra("Stakes"))
        Player = Player(intent.getStringExtra("UID"))
        Player!!.username = intent.getStringExtra("PlayerName")
        Player!!.bankroll = intent.getDoubleExtra("Bankroll", 0.0)
        Min = java.lang.Double.parseDouble(intent.getStringExtra("Min"))
        Blinds = intent.getStringExtra("Stakes").split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        TableName = intent.getStringExtra("TableName")
    }

    private fun getReferences() {
        Message = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Message")
        BlindsRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Blinds")
        Seat1Ref = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Seat1")
        Seat2Ref = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Seat2")
        SeatCardsRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Cards")
        Ongoing = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Ongoing")
        Players = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Users/" + Player!!.uid)
        NumRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Players")
        PotRef = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Pot")
        ActionSeat = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/ActionSeat")
        Bets = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/Bets")
        OTG = mRef.getReferenceFromUrl("https://ultra-mason-176521.firebaseio.com/Tables/Cash Games/Tables/$TableName/OTG")
        setListeners()
    }

    private fun setListeners() {

        ActionSeat.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                action = dataSnapshot.value!!.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (Player!!.getSeat() != action[0] && action.size > 1 && Player!!.getSeat() != "NoSeat") {
                    if (action[1] == "Bet" || action[1] == "Raise" && !AllIn) {
                        PokerUtilities.SetActionLabelVillian(action[1] + " " + action[2], Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0])
                        if (java.lang.Double.parseDouble(action[2]) >= Player!!.getStack()) {
                            Call.setText("Call " + Player!!.getStack()!!)
                            SetAllInButtons(action[2])
                        } else {
                            SetBetButtons()
                            Call.setText("Call " + action[2])
                        }
                    } else if (action[1] == "Fold") {
                        SetOffButtons()
                        PokerUtilities.SetActionLabelVillian("Fold", Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0])
                        Payout("Fold")
                    } else if (action[1] == "Call") {
                        SetNoBetButtons()
                        PokerUtilities.SetActionLabelVillian("Call " + action[2], Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0])
                    } else if (action[1] == "Check") {
                        SetNoBetButtons()
                        PokerUtilities.SetActionLabelVillian("Check", Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0])
                    } else if (action[1] == "AllIn") {
                        AllIn = true
                        SetAllInButtons(action[2])
                        PokerUtilities.SetActionLabelVillian("AllIn " + action[2], Seat2Name, Seat1Name, action[0], Seat1[0], Seat2[0])
                    }
                }
                if (Player!!.getSeat() == action[0] && action[1] == "Wins") {
                    Payout("Wins")
                } else if (action[0] == "Split") {
                    Payout("Split")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        Seat1Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value!!.toString() != "empty") {
                    if (Player!!.getSeat() == "Seat2" && Seat1[1] != "")
                        VillianStack = java.lang.Double.parseDouble(Seat1[1])
                    Seat1 = dataSnapshot.value!!.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    Seat1Name.setText(Seat1[0])
                    Seat1Chips.setText("$ " + Seat1[1])
                } else {
                    Seat1Name.setText("")
                    Seat1Chips.setText("")
                    VillianStack = 0.0
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        Seat2Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value!!.toString() != "empty") {
                    if (Player!!.getSeat() == "Seat1" && Seat2[1] != "")
                        VillianStack = java.lang.Double.parseDouble(Seat2[1])
                    Seat2 = dataSnapshot.value!!.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    Seat2Name.setText(Seat2[0])
                    Seat2Chips.setText("$ " + Seat2[1])
                } else {
                    Seat2[0] = ""
                    Seat2[1] = ""
                    Seat2Name.setText("")
                    Seat2Chips.setText("")
                    VillianStack = 0.0
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        NumRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value!!.toString() == "2" || Player!!.getSeat() != "NoSeat")
                    TakeSeat.setVisibility(View.GONE)
                else
                    TakeSeat.setVisibility(View.VISIBLE)
                numPlayers = Integer.parseInt(dataSnapshot.value!!.toString())
                if (dataSnapshot.value!!.toString() == "2" && Player!!.getSeat() == "Seat1") {
                    val rand = Random()
                    if (rand.nextInt(10000) > 5000)
                        android.widget.Button.setValue("Seat2")
                    else
                        android.widget.Button.setValue("Seat1")
                } else
                    android.widget.Button.setValue("empty")
            }


            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        PotRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Pot.setText(String.format(Locale.ENGLISH, "Pot : $ %.2f", java.lang.Double.parseDouble(dataSnapshot.value!!.toString())))
                CurrentPot = java.lang.Double.parseDouble(dataSnapshot.value!!.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        BlindsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (java.lang.Boolean.parseBoolean(dataSnapshot.child("Big").value!!.toString()) && java.lang.Boolean.parseBoolean(dataSnapshot.child("Small").value!!.toString())) {
                    StartHand()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        Message.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value!!.toString() != "empty") {
                    MessageHistory.append(dataSnapshot.value!!.toString() + "\n")
                    setBottom()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        OTG!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                OTGNum = Integer.parseInt(dataSnapshot.value!!.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        Ongoing.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value!!.toString() == "true" && Player!!.getSeat() != "NoSeat") {
                    LeaveSeat.setVisibility(View.GONE)
                    RemoveBlind.setVisibility(View.GONE)
                } else if (Player!!.getSeat() != "NoSeat" && dataSnapshot.value!!.toString() == "false")
                    LeaveSeat.setVisibility(View.VISIBLE)
                inHand = java.lang.Boolean.parseBoolean(dataSnapshot.value!!.toString())
                if (!inHand)
                    EndHand()


            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        SeatCardsRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (inHand) {
                    cref1 = dataSnapshot.child("Seat1").value!!.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    cref2 = dataSnapshot.child("Seat2").value!!.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    if (Player!!.seat == "NoSeat") {
                        addCardDisplay(SeatCards!![0].getChildAt(0) as ImageView, "back")
                        addCardDisplay(SeatCards!![0].getChildAt(1) as ImageView, "back")
                        addCardDisplay(SeatCards!![0].getChildAt(2) as ImageView, cref1!![2])


                        addCardDisplay(SeatCards!![1].getChildAt(0) as ImageView, "back")
                        addCardDisplay(SeatCards!![1].getChildAt(1) as ImageView, "back")
                        addCardDisplay(SeatCards!![1].getChildAt(2) as ImageView, cref2!![2])

                    } else {
                        if (Player!!.seat == "Seat1") {
                            addCardDisplay(SeatCards!![0].getChildAt(0) as ImageView, cref1!![0])
                            addCardDisplay(SeatCards!![0].getChildAt(1) as ImageView, cref1!![1])
                            addCardDisplay(SeatCards!![0].getChildAt(2) as ImageView, cref1!![2])

                            addCardDisplay(SeatCards!![1].getChildAt(0) as ImageView, "back")
                            addCardDisplay(SeatCards!![1].getChildAt(1) as ImageView, "back")
                            addCardDisplay(SeatCards!![1].getChildAt(2) as ImageView, cref2!![2])
                        }
                        if (Player!!.seat == "Seat2") {
                            addCardDisplay(SeatCards!![0].getChildAt(0) as ImageView, "back")
                            addCardDisplay(SeatCards!![0].getChildAt(1) as ImageView, "back")
                            addCardDisplay(SeatCards!![0].getChildAt(2) as ImageView, cref1!![2])

                            addCardDisplay(SeatCards!![1].getChildAt(0) as ImageView, cref2!![0])
                            addCardDisplay(SeatCards!![1].getChildAt(1) as ImageView, cref2!![1])
                            addCardDisplay(SeatCards!![1].getChildAt(2) as ImageView, cref2!![2])

                        }
                    }
                    CurrStreet = dataSnapshot.child("Street").value!!.toString()
                    //Street Names, [Pre, first, second, third, river, showdown]
                    when (CurrStreet) {
                        "First" -> {
                            OTG!!.setValue(checkOTG())
                            addCardDisplay(SeatCards!![0].getChildAt(3) as ImageView, cref1!![3])
                            addCardDisplay(SeatCards!![1].getChildAt(3) as ImageView, cref2!![3])
                        }
                        "Second" -> {
                            OTG!!.setValue(checkOTG())
                            addCardDisplay(SeatCards!![0].getChildAt(4) as ImageView, cref1!![4])
                            addCardDisplay(SeatCards!![1].getChildAt(4) as ImageView, cref2!![4])
                        }
                        "Third" -> {
                            OTG!!.setValue(checkOTG())
                            addCardDisplay(SeatCards!![0].getChildAt(5) as ImageView, cref1!![5])
                            addCardDisplay(SeatCards!![1].getChildAt(5) as ImageView, cref2!![5])
                        }
                        "River" -> {
                            if (Player!!.seat == "NoSeat") {
                                addCardDisplay(SeatCards!![0].getChildAt(6) as ImageView, "back")
                                addCardDisplay(SeatCards!![1].getChildAt(6) as ImageView, "back")

                            } else {
                                if (Player!!.seat == "Seat1") {
                                    addCardDisplay(SeatCards!![0].getChildAt(6) as ImageView, cref1!![6])
                                    addCardDisplay(SeatCards!![1].getChildAt(6) as ImageView, "back")
                                }
                                if (Player!!.seat == "Seat2") {
                                    addCardDisplay(SeatCards!![0].getChildAt(0) as ImageView, "back")
                                    addCardDisplay(SeatCards!![1].getChildAt(2) as ImageView, cref2!![2])

                                }
                            }
                        }
                        "ShowDown" -> {
                            if (Player!!.seat == "Seat1") {
                                for (c in cref1!!) {
                                    S1!!.addCardNoSort(Card(c[1], c[0]))
                                }
                                for (c in cref2!!) {
                                    S2!!.addCardNoSort(Card(c[1], c[0]))
                                }
                                Collections.sort(S1!!.hand, Hand.CardComparatorLow)
                                Collections.sort(S2!!.hand, Hand.CardComparatorLow)
                                S1!!.calculateHand()
                                S2!!.calculateHand()
                                ShowDown()
                            }
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    private fun ShowDown() {
            val winner = PokerUtilities.CheckWinner(S1, S2, Pot)
            if (winner == 1) ActionSeat.setValue("Seat1,Wins," + CurrentPot)
            if (winner == 2) ActionSeat.setValue("Seat2,Wins," + CurrentPot)
            if (winner == 0)
                ActionSeat.setValue("Split")
            else
                ActionSeat.setValue("somethingiswrong," + CurrentPot)
    }

    private fun checkOTG(): Int {
        var goSeat = 3
        if (Player!!.seat != "NoSeat") {
            if (S1!!.isAhead(S2!!) && !AllIn) {
                if (Player!!.seat == "Seat1")
                    setNoBetButtons()
                if (Player!!.seat == "Seat2")
                    setOffButtons()
                goSeat = 1
            } else if (S2!!.isAhead(S1!!) && !AllIn) {
                if (Player!!.seat == "Seat2")
                    setNoBetButtons()
                if (Player!!.seat == "Seat1")
                    setOffButtons()
                goSeat = 2
            }
            ActionSeat!!.setValue("empty")
        }

        return goSeat
    }

    private fun initSeats() {
        Dealer1 = findViewById(R.id.Seat1Dealer)
        Dealer2 = findViewById(R.id.Seat2Dealer)
        StakesText = findViewById(R.id.StakesText)
        Pot = findViewById(R.id.Pot)
        RoomDeets = findViewById(R.id.RoomDeets)
        var handStrength = findViewById<TextView>(R.id.HandStrength)
        Seat1Name = findViewById(R.id.Seat1Name)
        Seat1Chips = findViewById(R.id.Seat1ChipAction)
        Seat2Name = findViewById(R.id.Seat2Name)
        Seat2Chips = findViewById(R.id.Seat2ChipAction)
        TableText = findViewById(R.id.RoomText)
        SeatCards = ArrayList()
        SeatCards!!.add(findViewById(R.id.SeatOne))
        SeatCards!!.add(findViewById(R.id.SeatTwo))
    }

    private fun initUI() {
        PostBlind = findViewById(R.id.PostBlindBtn)
        PostBlind!!.setOnClickListener {
            postBlind()
            LeaveSeat!!.visibility = View.GONE
        }

        RemoveBlind = findViewById(R.id.RemoveBlindBtn)
        RemoveBlind!!.setOnClickListener {
            removeBlind()
            LeaveSeat!!.visibility = View.VISIBLE
        }

        Raise = findViewById(R.id.BtnRaise)
        Raise!!.setOnClickListener {
            if (BetAmount!!.text.toString() != "") {
                val currbet = java.lang.Double.parseDouble(BetAmount!!.text.toString())
                if (currbet >= 2 * java.lang.Double.parseDouble(action[2])) {
                    SetOffButtons()
                    SendAction("Raise", currbet, Player)
                } else
                    Toast.makeText(baseContext, "Raise must be at least 2x the bet", Toast.LENGTH_LONG).show()
            }
        }
        Bet = findViewById(R.id.BtnBet)
        Bet!!.setOnClickListener {
            if (BetAmount!!.text.toString() != "") {
                SetOffButtons()
                val currbet = java.lang.Double.parseDouble(BetAmount!!.text.toString())
                SendAction("Bet", currbet, Player)
            }
        }
        Fold = findViewById(R.id.BtnFold)
        Fold!!.setOnClickListener {
            SetOffButtons()
            ActionSeat!!.setValue(Player!!.seat + ",Fold," + CurrentPot)
            PokerUtilities.SetActionLabel("Fold", Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player)
        }
        Check = findViewById(R.id.BtnCheck)
        Check!!.setOnClickListener {
            PokerUtilities.SetActionLabel("Check ", Seat1Chips, Seat2Chips, Seat2Name, Seat1Name, Player)
            ActionSeat.setValue(Player!!.seat + ",Check," + 0)
            SetOffButtons()
            if (Player!!.dealer!! && CurrStreet != "Pre") {
                NextStreet()
            } else if ((!Player!!.dealer)!! && CurrStreet == "Pre") {
                NextStreet()
            }
        }
        Call = findViewById(R.id.BtnCall)
        Call!!.setOnClickListener {
            SetOffButtons()
            val currbet = java.lang.Double.parseDouble(action[2])
            SendAction("Call", currbet, Player)
        }
        TakeSeat = findViewById(R.id.TakeSeat)
        TakeSeat!!.setOnClickListener {
            if (Seat1Name!!.text.toString() == "") {
                setStack("Seat1")
                Player!!.seat = ("Seat1")
                numPlayers++
                NumRef!!.setValue(numPlayers)
                Seat1Ref!!.setValue(Player!!.username + "," + String.format(Locale.ENGLISH, "%.2f", 0.0) + "")
            } else if (Seat2Name!!.text.toString() == "") {
                setStack("Seat2")
                Player!!.seat = "Seat2"
                Seat2Ref!!.setValue(Player!!.username + "," + String.format(Locale.ENGLISH, "%.2f", 0.0) + "")
                numPlayers++
                NumRef!!.setValue(numPlayers)
            }
        }
        LeaveSeat = findViewById(R.id.LeaveSeat)
        LeaveSeat!!.setOnClickListener {
            if (Player!!.seat == "Seat1")
                Seat1Ref!!.setValue("empty")
            else
                Seat2Ref!!.setValue("empty")
            SendMessage(Player!!.username + " has left the table")
            Player!!.addtoBank(Player!!.stack)
            Player!!.seat = "NoSeat"
            numPlayers--
            Players!!.child("Seat").setValue("empty")
            NumRef!!.setValue(numPlayers)
            Players!!.child("BankRoll").setValue(Player!!.bankroll)
            LeaveSeat!!.visibility = View.GONE
            TakeSeat!!.visibility = View.VISIBLE
            PostBlind!!.visibility = View.GONE
            SetOffButtons()
            Toast.makeText(baseContext, Player!!.bankroll!!.toString(), Toast.LENGTH_LONG).show()
        }
        MessageHistory = findViewById(R.id.MessageHistory)
        MessageHistory!!.movementMethod = ScrollingMovementMethod()

        ToSend = findViewById(R.id.MessageBox)
        ToSend!!.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                val message = ToSend!!.text.toString()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(ToSend!!.windowToken, 0)
                SendMessage(Player!!.username + ": " + message)
                ToSend!!.text = "" as Editable
                setBottom()
                true
            }
            false
        }
        SetOffButtons()
        LeaveSeat!!.visibility = View.GONE

    }

    private fun cancelReserveSeat() {
        numPlayers--
        NumRef!!.setValue(numPlayers)
        if (Player!!.seat == "Seat1")
            Seat1Ref!!.setValue("empty")
        else if (Player!!.seat == "Seat2")
            Seat2Ref!!.setValue("empty")
        Player!!.seat = "NoSeat"
    }

    private fun postBlind() {
        if (Player!!.dealer!!) {
            Player!!.removeFromStack(SmallBlind)
            CurrentPot = SmallBlind!! + CurrentPot!!
            Pot!!.text = "$ $CurrentPot"
            BlindsRef!!.child("Small").setValue(true)
        } else {
            Player!!.removeFromStack(BigBlind)
            CurrentPot = BigBlind!! + CurrentPot!!
            Pot!!.text = "$ $CurrentPot"
            BlindsRef!!.child("Big").setValue(true)
        }
        PotRef!!.setValue(CurrentPot)
        if (Player!!.seat == "Seat1")
            Seat1Ref!!.setValue(Player!!.username + "," + String.format(Locale.ENGLISH, "%.2f", Player!!.stack) + "")
        else
            Seat2Ref!!.setValue(Player!!.username + "," + String.format(Locale.ENGLISH, "%.2f", Player!!.stack) + "")

        PostBlind!!.visibility = View.GONE
        RemoveBlind!!.visibility = View.VISIBLE
    }

    private fun removeBlind() {
        if (Player!!.dealer!!) {
            Player!!.addToStack(SmallBlind)
            CurrentPot = CurrentPot!! - SmallBlind!!
            Pot!!.text = "$ $CurrentPot"
            BlindsRef!!.child("Small").setValue(false)
        } else {
            Player!!.addToStack(BigBlind)
            CurrentPot = CurrentPot!! - BigBlind!!
            Pot!!.text = "$ $CurrentPot"
            BlindsRef!!.child("Big").setValue(false)
        }
        PotRef!!.setValue(CurrentPot)
        if (Player!!.seat == "Seat1")
            Seat1Ref!!.setValue(Player!!.username + "," + String.format(Locale.ENGLISH, "%.2f", Player!!.stack) + "")
        else
            Seat2Ref!!.setValue(Player!!.username + "," + String.format(Locale.ENGLISH, "%.2f", Player!!.stack) + "")

        RemoveBlind!!.visibility = View.GONE
        PostBlind!!.visibility = View.VISIBLE
    }

    private fun setOffButtons() {
        Raise!!.visibility = View.GONE
        Bet!!.visibility = View.GONE
        Fold!!.visibility = View.GONE
        Check!!.visibility = View.GONE
        Call!!.visibility = View.GONE
    }

    private fun setBetButtons() {
        Bet!!.visibility = View.GONE
        Check!!.visibility = View.GONE
        Raise!!.visibility = View.VISIBLE
        Fold!!.visibility = View.VISIBLE
        Call!!.visibility = View.VISIBLE
    }

    private fun setNoBetButtons() {
        Raise!!.visibility = View.GONE
        Fold!!.visibility = View.GONE
        Call!!.visibility = View.GONE
        Check!!.visibility = View.VISIBLE
        Bet!!.visibility = View.VISIBLE
    }

    private fun setAllInButtons(bet: String) {
        SetOffButtons()
        Call!!.visibility = View.VISIBLE
        Fold!!.visibility = View.VISIBLE
        Call!!.text = "Call " + bet
    }

    private fun setBottom() {
        if (MessageHistory!!.layout != null) {
            val scroll = MessageHistory!!.layout.getLineTop(MessageHistory!!.lineCount) - MessageHistory!!.height
            if (scroll > 0)
                MessageHistory!!.scrollTo(0, scroll)
            else
                MessageHistory!!.scrollTo(0, 0)
        }
    }

    private fun addCardDisplay(card: ImageView, toAdd: String) {
        if (toAdd != "NA") {
            card.visibility = View.VISIBLE
            val id = resources.getIdentifier(toAdd, "drawable", packageName)
            card.setImageResource(id)
        }
    }

    private fun resetCards() {

        for (i in SeatCards!!.indices) {
            for (k in 0..6) {
                SeatCards!![i].getChildAt(k).visibility = View.INVISIBLE
            }
        }

    }

    private fun ShuffleDeck() {
        val deck = ArrayList<Card>()
        for (i in Suits.indices) {
            for (k in 1..13) {
                deck.add(Card(k, Suits[i]))
            }
        }
        Collections.shuffle(deck)
        S1 = RazzHand()
        S2 = RazzHand()

        S1!!.addCardNoSort(deck[0])
        S1!!.addCardNoSort(deck[2])
        S1!!.addCardNoSort(deck[4])

        S2!!.addCardNoSort(deck[1])
        S2!!.addCardNoSort(deck[3])
        S2!!.addCardNoSort(deck[5])


        SeatCardsRef!!.child("Seat1").setValue(deck[0].toString() + "," + deck[2].toString() + "," + deck[4].toString() + "," + deck[6].toString() + "," + deck[8].toString() + "," + deck[10].toString() + "," + deck[12].toString())
        SeatCardsRef!!.child("Seat2").setValue(deck[1].toString() + "," + deck[3].toString() + "," + deck[5].toString() + "," + deck[7].toString() + "," + deck[9].toString() + "," + deck[11].toString() + "," + deck[13].toString())
    }

}
