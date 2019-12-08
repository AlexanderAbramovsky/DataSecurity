package com.example.voting

import kotlin.math.absoluteValue
import kotlin.math.sqrt
import kotlin.random.Random

object ModularArithmetic {
    var MODULE = 1_000_000_000L
    var ITERATIONS_COUNT = 100

    fun generatePrime(module: Long = MODULE, iterationsCount: Int = ITERATIONS_COUNT): Long {
        val rand = Random(System.currentTimeMillis())
        var newP = 1L
        while (true) {
            newP = 2L * ((rand.nextLong().absoluteValue) % module) + 1
            if (checkFerma(newP, iterationsCount)) break
        }
        return newP
    }

    fun generateCD(prime: Long): Pair<Long, Long> {
        val rand = Random(System.currentTimeMillis())
        var c = 0L
        var d = 0L
        var count = 0L
        while (true) {
            c = rand.nextInt().absoluteValue.toLong()
            if (c and 1L == 0L) continue
            if (greatestCommonDenominator(c, prime - 1) == 1L) {
                d = inverse(c, prime - 1)
                if (d < 0) d += prime - 1
            }
            if ((c * d % (prime - 1L)) == 1L) break
        }
        return Pair(c, d)
    }

    fun generateG(p: Long, q: Long): Long {
        for (i in 2..p-2) {
            if (pows(i, q, p) != 1L) {
                return i
            }
        }
        return 0
    }

    fun greatestCommonDenominator(a: Long, b: Long): Long {
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

    fun pow(num: Long, step: Long, mdt: Long): Long {
        var y = 1UL
        var s = num.toULong()
        val md = mdt.toULong()
        var currentStep = step
        while (currentStep != 0L) {
            if (currentStep and 1L == 1L) y = (y % md * s % md) % md
            s = (s % md * s % md) % md
            currentStep /= 2L
        }
        return y.toLong()
    }

    fun mul(number1: Long, number2: Long, module: Long): Long {
        return when {
            number2 == 1L -> { number1 }
            number2 and 1L == 0L -> {
                val temp = mul(number1, number2 / 2, module)
                (2 * temp) % module
            }
            else -> {
                (mul(number1, number2-1, module) + number1) % module
            }
        }
    }

    fun pows(number: Long, degree: Long, module: Long): Long{
        return when{
            degree == 0L -> { 1 }
            degree and 1L == 0L -> {
                val temp = pows(number, degree/2, module)
                mul(temp, temp, module) % module
            }
            else -> {
                mul(pows(number, degree-1, module), number, module) % module
            }
        }
    }

    fun inverse(a: Long, b: Long): Long {
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

    fun inverseTriple(a: Long, b: Long): Triple<Long, Long, Long> {
        var tempTriple1 = Triple(a, 1L, 0L)
        if (b == 0L) {
            return tempTriple1
        }
        val tempTriple2 = inverseTriple(b, a % b)
        tempTriple1 = Triple(
            first = tempTriple2.first,
            second = tempTriple2.third,
            third = tempTriple2.second - (a / b) * tempTriple2.third
        )
        return tempTriple1
    }

    fun checkFerma(p: Long, iterationsCount: Int = ITERATIONS_COUNT): Boolean {
        val rand = Random(System.currentTimeMillis())
        when {
            p == 2L -> return true
            p and 1L == 0L -> return false
            else -> {
                for (i in 0 until iterationsCount) {
                    val a = rand.nextLong().absoluteValue % (p - 1L) + 1L
                    if ((greatestCommonDenominator(a, p) != 1L) || (pow(a, (p - 1), p) != 1L)) {
                        return false
                    }
                }
                return true
            }
        }
    }

}