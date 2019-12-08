package com.example.voting

data class OptionedQuestion(var question: String, var options: List<String>)

data class OptionedAnswer(var question: OptionedQuestion, var selectedOptionNumber: Int)

data class OptionedResults(var question: OptionedQuestion, var optionedAnswers: Array<Int>)