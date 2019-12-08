package com.example.cardexchange

import jdk.nashorn.internal.runtime.GlobalConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class Main {
    companion object{
    }
}

fun main(args: Array<String>) = runBlocking{
    val exchanger = CardExchanger(52, 7, 6)
    exchanger.exchange()
    exchanger.printExchanged()
    exchanger.printNotExchanged()
}