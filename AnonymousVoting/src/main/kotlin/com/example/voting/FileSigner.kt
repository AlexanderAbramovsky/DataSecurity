package com.example.voting

import com.sun.org.apache.xpath.internal.operations.Mod
import java.io.*
import java.lang.Math.pow
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.asJavaRandom

class FileSigner(var filePath: String) {
    companion object {
        const val SIGN_FILE = "./sign.txt"
    }

    private var p: Long = 0L
    private var q: Long = 0L
    private var g: Long = 0L
    private var n: Long = 0L
    private var d: Long = 0L
    private var y: Long = 0L
    private var r: Long = 0L
    private var a: Long = 0L
    private var b: Long = 0L
    private var rand = Random(System.currentTimeMillis())

    fun signRSA(destinationFile: String) {
        if (!File(filePath).exists()) return

        val sourceFile = BufferedReader(FileReader(filePath))
        val encryptFile = BufferedWriter(FileWriter(destinationFile))

        generateRSAPrimePair().let {
            p = it.first
            q = it.second
        }

        n = p * q
        val phi = (p - 1) * (q - 1)

        do {
            d = rand.nextInt().absoluteValue % (phi - 1)
        } while (ModularArithmetic.greatestCommonDenominator(d, phi) != 1L)

        var e = ModularArithmetic.inverseTriple(phi, d).third
        if (e < 0) e += phi

        println(
            "Generated params:\n" +
                    "p = $p\n" +
                    "q = $q\n" +
                    "n = p*q = $n\n" +
                    "phi = (p-1)*(q-1) = $phi\n" +
                    "d = $d\n" +
                    "e = $e"
        )

        println("Encrypting:")
        md5Custom(sourceFile.readLine()).forEach { byte ->
            val data = byte.toLong()
            val encrypted = ModularArithmetic.pows(data, e, n)
            println("Hash $data encrypted as $encrypted")
            encryptFile.write("$encrypted ")
        }
        sourceFile.close()
        encryptFile.close()

        val signedString = if (checkSignRSA(destinationFile)) {
            "Data successfully signed!"
        } else {
            "Data wasn't signed!"
        }
        println(signedString)
    }

    fun checkSignRSA(destinationFile: String): Boolean {
        var signed = true
        val sourceFile = BufferedReader(FileReader(filePath))
        val encryptFileScanner = Scanner(FileInputStream(destinationFile))
        var e = 0L
        var m = 0L
        var i = 0

        val hash = md5Custom(sourceFile.readLine())
        println("Decrypting:")
        while (encryptFileScanner.hasNext()) {
            if (encryptFileScanner.hasNextLong()) {
                e = encryptFileScanner.nextLong()
                m = ModularArithmetic.pows(e, d, n)
                val equalsString = if (m == hash[i].toLong()) {
                    "equals"
                } else {
                    signed = false
                    "not equals"
                }
                println("Encrypted $e decrypted as $m and $equalsString to hash = ${hash[i]}")
                i++
            } else {
                encryptFileScanner.next()
            }
        }
        return signed
    }

    fun signElgamal(destinationFile: String) {
        if (!File(filePath).exists()) return

        val sourceFile = BufferedReader(FileReader(filePath))
        val encryptFile = BufferedWriter(FileWriter(destinationFile))

        generateElgamalPrimePair().let {
            p = it.first
            q = it.second
        }
        g = ModularArithmetic.generateG(p, q)
        val x = (rand.nextLong().absoluteValue % (p - 1)) + 1
        y = ModularArithmetic.pows(g, x, p)
        var k = 0L
        do {
            k = rand.nextLong().absoluteValue % (p - 1) + 2
        } while (ModularArithmetic.greatestCommonDenominator(k, p - 1) != 1L)

        var kInversed = ModularArithmetic.inverseTriple(p - 1, k).third
        if (kInversed < 0) kInversed += p - 1

        println(
            "Generated params:\n" +
                    "p = $p\n" +
                    "q = $q\n" +
                    "g = $g\n" +
                    "x = $x\n" +
                    "y = $y\n" +
                    "k = $k\n" +
                    "k inversed = $kInversed\n" +
                    ""
        )

        r = ModularArithmetic.pows(g, k, p)

        md5Custom(sourceFile.readLine()).forEach { byte ->
            var u = (byte.toLong() - (x * r)) % (p - 1)
            u += if (u < 0) p - 1 else 0
            val encrypted = (kInversed * u) % (p - 1)
            println("Hash $byte encrypted as $encrypted")
            encryptFile.write("$encrypted ")
        }

        sourceFile.close()
        encryptFile.close()

        val signedString = if (checkSignElgamal(destinationFile)) {
            "Data successfully signed!"
        } else {
            "Data wasn't signed!"
        }
        println(signedString)
    }

    fun checkSignElgamal(destinationFile: String): Boolean {
        var signed = true
        val sourceFile = BufferedReader(FileReader(filePath))
        val encryptFileScanner = Scanner(FileInputStream(destinationFile))
        var i = 0
        val hash = md5Custom(sourceFile.readLine())
        println("Decrypting:")
        while (encryptFileScanner.hasNext()) {
            if (encryptFileScanner.hasNextLong()) {
                val encrypted = encryptFileScanner.nextLong()
                val decrypted = ((ModularArithmetic.pows(y, r, p) * ModularArithmetic.pows(r, encrypted, p))) % p
                val h = ModularArithmetic.pows(g, hash[i].toLong(), p)
                val equalsString = if (decrypted == h) {
                    "equals"
                } else {
                    signed = false
                    "not equals"
                }
                println("Encrypted $encrypted decrypted as $decrypted and $equalsString to hash = ${h}")
                i++
            } else {
                encryptFileScanner.next()
            }
        }
        return signed
    }

    fun signGOST(destinationFile: String) {
        if (!File(filePath).exists()) return

        val sourceFile = BufferedReader(FileReader(filePath))
        val encryptFile = BufferedWriter(FileWriter(destinationFile))

        generateGOSTPrimePair().let{
            p = it.first
            q = it.second
        }

        do {
            g = rand.nextLong().absoluteValue % p + 1
            a = ModularArithmetic.pows(g, b, p)
        } while (a <= 1)

        val x = rand.nextLong().absoluteValue % (q) + 1
        y = ModularArithmetic.pows(a, x, p)

        println(
            "Generated params:\n" +
                    "p = $p\n" +
                    "q = $q\n" +
                    "g = $g\n" +
                    "x = $x\n" +
                    "y = $y\n"
        )
        var s = 0L
        md5Custom(sourceFile.readLine()).forEach { byte ->
            while (true){
                val k = rand.nextLong().absoluteValue % q
                r = ModularArithmetic.pows(a, k, p) % q
                if (r != 0L) {
                    s = (k * byte + x * r) % q
                    if (s != 0L) break
                }
            }
            println("Hash $byte encrypted as s=$s and r=$r")
            encryptFile.write("$s $r ")
        }

        sourceFile.close()
        encryptFile.close()

        val signedString = if (checkSignGOST(destinationFile)) {
            "Data successfully signed!"
        } else {
            "Data wasn't signed!"
        }
        println(signedString)
    }

    fun checkSignGOST(destinationFile: String): Boolean {
        var signed = true
        val sourceFile = BufferedReader(FileReader(filePath))
        val encryptFileScanner = Scanner(FileInputStream(destinationFile))
        var i = 0
        var hInverse = 0L
        val hash = md5Custom(sourceFile.readLine())
        println("Decrypting:")
        while (encryptFileScanner.hasNext()) {
            if (encryptFileScanner.hasNextLong()) {
                val s = encryptFileScanner.nextLong()
                while (!encryptFileScanner.hasNextLong()) encryptFileScanner.next()
                val r = encryptFileScanner.nextLong()

                if ((r > 0 && r < q) && (s > 0 && s < q)) {
                    hInverse = ModularArithmetic.inverseTriple(q, hash[i].toLong()).third
                    if (hInverse < 0) hInverse += q
                    val u1 = (s*hInverse) % q
                    var u2 = (-r*hInverse) % q
                    u2 += if (u2 < 0) q else 0
                    val v = (ModularArithmetic.pows(a, u1, p) * ModularArithmetic.pows(y, u2, p)) % p % q
                    val equalsString = if (v == r) {
                        "equals"
                    } else {
                        signed = false
                        "not equals"
                    }
                    println("Encrypted $s decrypted as v=$v and $equalsString to hash = ${r}")
                }
                i++
            } else {
                encryptFileScanner.next()
            }
        }
        return signed
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

    fun generateElgamalPrimePair(): Pair<Long, Long> {
        val module = ModularArithmetic.MODULE
        do {
            q = ModularArithmetic.generatePrime(module)
            p = (q * 2L) + 1L
        } while (!ModularArithmetic.checkFerma(p))
        return Pair(p, q)
    }

    fun generateGOSTPrimePair(): Pair<Long, Long> {
        val poW = pow(2.0, 31.0).toLong()
        val poS = pow(2.0, 16.0).toLong()
        val module = pow(2.0, 15.0).toLong()
        var newP = 0L
        var newQ = 0L
        do {
            newQ = rand.nextLong().absoluteValue % module + module
        } while (!ModularArithmetic.checkFerma(newQ, 1000))

        while (true) {
            b = rand.nextLong().absoluteValue  % newQ + module
            newP = b * newQ + 1
            if (ModularArithmetic.checkFerma(newP, 1000) && newP <= poW) {
                break
            }
        }
        return Pair(newP, newQ)
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


fun main(args: Array<String>) {
    FileSigner("./sourceFile.txt").signGOST("./resultFile.txt")
    FileSigner("./sourceFile.txt").signGOST("./resultFile.txt")
    FileSigner("./sourceFile.txt").signGOST("./resultFile.txt")


}

