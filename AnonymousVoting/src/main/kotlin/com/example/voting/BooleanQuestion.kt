package com.example.voting

data class BooleanQuestion(var question: String)

data class BooleanAnswer(var question: BooleanQuestion, var answer: Boolean)