package com.andyisdope.headsuppoker.Strategies

/**
 * Created by Andy on 1/13/2018.
 */
class RazzHand : Hand() {
    var HandStrength: StringBuilder = StringBuilder()
    var hasDupes: Int = 0

    fun calculateHand() {
        var hand = ArrayList<Int>(14)
        var count = 0
        for (c in super.getHand()) {
            if (hand[c.rank] == 0 && count <= 5) {
                hand[c.rank]++
                count++
                super.getHand().remove(c)
                HandStrength.append(c.rank)

            }
            when {
                hand[c.rank] == 2 -> hasDupes += 1
                hand[c.rank] == 3 -> hasDupes = 3
                hand[c.rank] == 4 -> hasDupes = 4
            }
        }
        if (count != 5) {
            HandStrength.append(super.getCard(0).rank)
        }

    }

    fun isAhead(opp: RazzHand): Boolean {
        var hand1 = ArrayList<Int>(14)
        var hand2 = ArrayList<Int>(14)
        for (k in 2..super.HandSize()) {
            hand1!![super.getCard(k).rank]++!!
            hand2!![opp.getCard(k).rank]++!!
        }
        when {
            hasDupes < opp.hasDupes -> return true
            hasDupes > opp.hasDupes -> return false
            else -> for (i in 14 downTo 1) {
                if (hand1[i] > hand2[i])
                    return false
                else if (hand2[i] < hand2[i])
                    return true
            }
        }
        return false
    }
}