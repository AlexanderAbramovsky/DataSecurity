package com.example.voting

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.random.Random

class AnonymousVoting(var votersCount: Int, var question: OptionedQuestion){
    var passports = BooleanArray(votersCount).apply{
        map{ false }
    }
    private var voteResults: OptionedResults = OptionedResults(question, Array(question.options.size){ 0 })

    private var rand = Random(System.currentTimeMillis())
    private var publicRSA = PublicRSA()
    private var c = 0L
    private var d = 0L
    private var n = 0L
    private var r = 0L
    private var rInversed = 0L

    data class VotePaper(var passportId: Int?, var realVote: Int?, var voteHash: LongArray, var sign: LongArray? = null)

    fun askVoter(questionForVoter: OptionedQuestion): VotePaper{
        var passportId = 0
        while(true) {
            passportId = rand.nextInt().absoluteValue % votersCount
            if (!passports[passportId]) break
        }
        val vote = rand.nextInt().absoluteValue % questionForVoter.options.size

        var p = 0L
        var q = 0L
        generateRSAPrimePair().let{
            p = it.first
            q = it.second
        }
        n = p*q
        val phi = (p-1)*(q-1)
        do {
            c = rand.nextLong().absoluteValue % (phi - 1)
        } while (ModularArithmetic.greatestCommonDenominator(c, phi) != 1L)

        d = ModularArithmetic.inverseTriple(phi, c).third
        if (d < 0) d += phi

        val voteHash = md5Custom(vote.toString())

        do {
            r = rand.nextLong().absoluteValue % (n-1)
        } while (ModularArithmetic.greatestCommonDenominator(r, n) != 1L)

        val notSignedVote = LongArray(voteHash.size)
        notSignedVote.forEachIndexed{ i, data ->
            val randPowed = ModularArithmetic.pows(r, d, n)
            notSignedVote[i] = ModularArithmetic.mul(voteHash[i].toLong(), randPowed, n)
        }

        return VotePaper(passportId, vote, notSignedVote)
    }

    fun verifyVote(votePaper: VotePaper): VotePaper{
        if (votePaper.passportId != null && !passports[votePaper.passportId!!]) {
            passports[votePaper.passportId!!] = true
            val signForVote = LongArray(votePaper.voteHash.size)
            signForVote.forEachIndexed{ i, data ->
                signForVote[i] = ModularArithmetic.pows(votePaper.voteHash[i], c, n)
            }
            votePaper.sign = signForVote
        }
        return votePaper
    }

    fun sendVote(votePaper: VotePaper){
        votePaper.sign?: return
        rInversed = ModularArithmetic.inverseTriple(n, r).third
        if (rInversed < 0) rInversed += n
        votePaper.sign!!.forEachIndexed { i, data ->
            votePaper.sign!![i] = ModularArithmetic.mul(votePaper.sign!![i], rInversed, n)
        }
        votePaper.passportId = null

        checkSignAndAcceptVote(votePaper)
    }

    fun checkSignAndAcceptVote(votePaper: VotePaper){
        votePaper.sign?:return
        val vote = votePaper.realVote ?: return
        var signAccepted = true
        val voteHash = md5Custom(vote.toString())
        for (i in votePaper.sign!!.indices) {
            val hash = ModularArithmetic.pows(votePaper.sign!![i], d, n)
            val trueHash = voteHash[i].toLong()
            println("sign= $hash hash= $trueHash")
            if (hash != trueHash) {
                signAccepted = false
                break
            }
        }
        println("---")

        if (signAccepted) {
            voteResults.optionedAnswers[vote]++
        } else {
            println("Something wrong")
        }
    }

    fun startVoting(): OptionedResults{
        for (i in 0 until votersCount) {
            val notSignedVote = askVoter(question)
            val vote = notSignedVote.realVote
            notSignedVote.realVote = null
            val signedVote = verifyVote(notSignedVote)
            signedVote.realVote = vote
            sendVote(signedVote)
        }
        return voteResults
    }

    fun generateRSAPrimePair(): Pair<Long, Long> {
        var p = 0L
        var q = 0L
        var n = 0L
        val module = sqrt(ModularArithmetic.MODULE.toDouble()).toLong() * 2L
        do {
            p = ModularArithmetic.generatePrime(module)
            q = ModularArithmetic.generatePrime(module)
            n = p * q
        } while (n < ModularArithmetic.MODULE)
        return Pair(p, q)
    }

    private fun md5Custom(string: String): ByteArray {
        var messageDigest: MessageDigest? = null
        var bytes = ByteArray(0)
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(string.toByteArray())
            bytes = messageDigest.digest()

            bytes.forEachIndexed { i, byte ->
                bytes[i] = bytes[i].toInt().absoluteValue.toByte()
            }
        } catch (ex: NoSuchAlgorithmException) {
            ex.printStackTrace()
        }
        return bytes
    }
}

fun main(args: Array<String>){
    val votersCount = 100
    val question = OptionedQuestion(
        "Which candidate never lies?",
        listOf("Pinocchio", "Fox the rogue", "Kashpirovsky")
    )
    val voting = AnonymousVoting(votersCount, question)
    val results = voting.startVoting()
    for (i in results.optionedAnswers.indices) {
        println("${results.question.options[i]} : ${results.optionedAnswers[i]}")
    }
}

class PublicRSA(){

}