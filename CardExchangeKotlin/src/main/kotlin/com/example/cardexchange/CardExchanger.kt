package com.example.cardexchange

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue
import kotlin.random.Random

class CardExchanger(var cardsNumber: Int = 52, var playersNumber: Int = 5, var cardsPerPlayer: Int = 2) : CoroutineScope {
    companion object {
        fun cardNameToString(cardNumber: Int): String {
            return when (cardNumber) {
                0 -> "1"
                1 -> "2"
                2 -> "3"
                3 -> "4"
                4 -> "5"
                5 -> "6"
                6 -> "7"
                7 -> "8"
                8 -> "9"
                9 -> "10"
                10 -> "J"
                11 -> "Q"
                12 -> "K"
                13 -> "T"
                else -> cardNumber.toString()
            }
        }
        fun suitToString(suitNumber: Int): String {
            return when (suitNumber) {
                1 -> "♣"
                2 -> "♠"
                3 -> "♥"
                4 -> "♦"
                else -> ""
            }
        }
        const val N = 1000000000
    }

    private var cardsPerSuit = cardsNumber / 4
    private var p: Long = 1
    private var arrayCD = Array<Array<Long>>(playersNumber) { Array(2) { 0L } }
    private var arrayCards = Array<Long>(cardsNumber) { 0L }
    private var arrayOWC = Array<Array<Int>>(playersNumber) { Array(cardsPerPlayer*2) { 0 } }
    private val rand = Random(System.currentTimeMillis())
    var exchanged  = false
    private set

    private var computeScope = CoroutineScope(Dispatchers.Default)
    private var supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + supervisorJob

    fun cardToString(playerCard: Int): String{
        val card = playerCard % cardsPerSuit
        val suitNumber = playerCard / cardsPerSuit
        return "${cardNameToString(card)}${suitToString(suitNumber)}"
    }

    private fun gcd(a: Long, b: Long): Long {
        var r = 0L
        var tempA = a
        var tempB = b
        while (tempB != 0L) {
            r = tempA % tempB
            tempA = tempB
            tempB = r
        }
        return tempA
    }

    private fun gcd3(a: Long, b: Long): Long {
        var q = 0L
        val arrayA = LongArray(2).apply { set(0, a); set(1, 1) }
        val arrayB = LongArray(2).apply { set(0, b); set(1, 0) }
        val arrayT = LongArray(2)
        while (arrayB[0] != 0L) {
            q = arrayA[0] / arrayB[0]
            arrayT[0] = arrayA[0] % arrayB[0]
            arrayT[1] = arrayA[1] - q * arrayB[1]
            for (i in 0..1) {
                arrayA[i] = arrayB[i]
                arrayB[i] = arrayT[i]
            }
        }
        return arrayA[1]
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun bvs(num: ULong, step: ULong, md: ULong): ULong {
        var y = 1UL
        var s = num
        var currentStep = step
        while (currentStep != 0UL) {
            if (currentStep % 2UL == 1UL) y = (y % md * s % md) % md
            s = (s % md * s % md) % md
            currentStep /= 2UL
        }
        return y
    }

    private fun bvs(num: Long, step: Long, mdt: Long): Long {
        var y = 1UL
        var s = num.toULong()
        val md = mdt.toULong()
        var currentStep = step
        while (currentStep != 0L) {
            if (currentStep % 2L == 1L) y = (y % md * s % md) % md
            s = (s % md * s % md) % md
            currentStep /= 2L
        }
        return y.toLong()
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun ferma(p: Long, iterationsCount: Int = 100): Boolean {
        when {
            p == 2L -> return true
            p and 1L == 0L -> return false
            else -> {
                for (i in 0 until iterationsCount) {
                    val a = rand.nextLong().absoluteValue % (p - 1L) + 1L
                    if ((gcd(a, p) != 1L) || (bvs(a, (p - 1), p) != 1L)) {
                        return false
                    }
                }
                return true
            }
        }
    }

    private fun generateP(): Long {
        var newP = 1L
        while (true) {
            newP = 2L * ((rand.nextLong().absoluteValue) % N) + 1
            if (ferma(newP)) break
        }
        return newP
    }

    private suspend fun generatePAsync(): Long {
        var generatedP = 1L
        var generated = false
        while (!generated) {
            //var newP = Array<Long>(10){ 2L * ((rand.nextLong().absoluteValue) % N ) + 1}
            val tasks = Array<Deferred<Long>>(10) {
                async {
                    var newP = 2L * ((rand.nextLong().absoluteValue) % N) + 1
                    println("$newP")
                    return@async if (ferma(newP)) {
                        newP
                    } else {
                        -1
                    }
                }
            }
            for (i in 0..1) {
                val result = tasks[i].await()
                if (result != -1L) {
                    generatedP = result
                    generated = true
                }
            }
        }
        return generatedP
    }

    private fun generateCD(playersCount: Int) {
        var c = 0L
        var d = 0L
        var count = 0L
        for (i in 0 until playersCount) {
            while (true) {
                c = rand.nextInt().absoluteValue.toLong()
                if (c and 1L == 0L) continue
                if (gcd(c, p - 1) == 1L) {
                    d = gcd3(c, p - 1)
                    if (d < 0) d += p - 1
                }
                if ((c * d % (p - 1L)) == 1L) break
            }
            arrayCD[i][0] = c
            arrayCD[i][1] = d
        }
    }

    private fun crypt(playersCount: Int, cardsCount: Int) {
        for (i in 0 until playersCount) {
            //every player encrypt every card with his C
            val playerC = arrayCD[i][0]
            for (j in 0 until cardsCount) {
                val currentCard = arrayCards[j]
                arrayCards[j] = bvs(currentCard, playerC, p)
            }
            //and than shuffle them
            arrayCards = arrayCards.toList().shuffled(rand).toTypedArray()
        }
    }

    private fun decrypt(playersCount: Int, cardsCount: Int) {
        var givedToPlayer = 0
        for (i in 0 until playersCount) {
            givedToPlayer = 0
            val playerD = arrayCD[i][1]
            for (j in 0 until cardsCount) {
                val numberOfPlayer = j % playersCount
                if (givedToPlayer < cardsPerPlayer && numberOfPlayer == i){
                    // if card for this player, we give it to him
                    arrayOWC[i][givedToPlayer] = j
                    givedToPlayer++
                } else {
                    // if not for this player, we ask player to decrypt this card
                    arrayCards[j] = bvs(arrayCards[j], playerD, p)
                }
            }
        }
    }

    private fun decryptOwn(playersCount: Int, cardsCount: Int) {
        for (i in 0 until playersCount) {
            // every player decrypt his own cards
            val playerD = arrayCD[i][1]
            for (j in 0 until cardsPerPlayer) {
                val cardNumber = arrayOWC[i][j]
                val encryptedCard = arrayCards[cardNumber]
                val decryptedCard = bvs(encryptedCard, playerD, p)
                arrayCards[cardNumber] = decryptedCard
                arrayOWC[i][cardsPerPlayer+j] = decryptedCard.toInt()
            }
        }
    }

    suspend fun exchange() {
        if (cardsPerPlayer * playersNumber > cardsNumber){
            println("Wrong configuration! There is not enough cards for all players!")
            return
        }

        for (i in 0 until cardsNumber) {
            arrayCards[i] = i + cardsPerSuit
        }
        p = generateP()
        println("P = $p")
        generateCD(playersNumber)
        arrayCD.forEachIndexed { i, it ->
            println(String.format("Player %d : C = %10d, D = %10d", i + 1, it.get(0), it.get(1)))
        }

        crypt(playersNumber, cardsNumber)
        decrypt(playersNumber, cardsNumber)
        decryptOwn(playersNumber, cardsNumber)
        exchanged = true
    }

    fun printExchanged() {
        if (exchanged) {
            val exchangedCardsCount = playersNumber * cardsPerPlayer
            println("Exchanged cards (${exchangedCardsCount}):")
            for (i in 0 until playersNumber) {
                print("Cards for player ${i + 1}: ")
                for (j in 0 until cardsPerPlayer) {
                    val card = arrayOWC[i][j + cardsPerPlayer].toInt()
                    print(cardToString(card) + " ")
                }
                println()
            }
        }
        else {
            println("There is no exchanged cards")
        }
    }

    fun printNotExchanged() {
        if (exchanged) {
            val exchangedCardsCount = playersNumber * cardsPerPlayer
            println("Not exchanged cards (${cardsNumber - exchangedCardsCount}):")
            for (j in exchangedCardsCount until cardsNumber) {
                val card = arrayCards[j].toInt()
                print(cardToString(card) + " ")
                if (j % 4 == 1) println()
            }
        } else {
            println("All cards not exchanged")
        }
    }
}