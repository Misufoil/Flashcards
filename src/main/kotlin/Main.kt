import java.io.File
import java.lang.Exception

val logText = mutableListOf<String>()

fun main(args: Array<String>) {
    val cards = LinkedHashMap<String, String>()
    val mistakes = LinkedHashMap<String, String>()
    var export = ""

    for (i in args.indices) {
        if (args[i] == "-import") {
            val maps = import(args[i + 1])
            cards.putAll(maps.first)
            mistakes.putAll(maps.second)
            println("${maps.first.size} cards have been loaded.\n")
        }
    }

    while (true) {
        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")

        when (readln()) {
            "add" -> {
                //cards += add(cards)
                val map = add(cards)
                cards += map.first
                mistakes += map.second
            }

            "remove" -> cards.remove(remove(cards))

            "import" -> {
                val maps = import()
                cards.putAll(maps.first)

                for (mistake in maps.second) {
                    if (mistakes.containsKey(mistake.key)) {
                        mistakes[mistake.key] = (mistakes[mistake.key]!!.toInt() + mistake.value.toInt()).toString()
                    } else {
                        mistakes[mistake.key] = mistake.value
                    }
                }
                //mistakes.putAll(maps.second)
                println("${maps.first.size} cards have been loaded.\n")
            }

            "export" -> export(cards, mistakes)

            "ask" -> {
                mistakes.putAll(ask(cards, mistakes))
            }

            "log" -> log()
            "hardest card" -> hardestCard(mistakes)
            "reset stats" -> {
                mistakes.forEach { (t, _) -> mistakes[t] = "0" }
                println("Card statistics have been reset.\n")
            }

            "exit" -> {
                for (i in args.indices) {
                    if (args[i] == "-export") {
                        export(args[i + 1], cards, mistakes)
                    }
                }
                println("Bye bye!")
                break
            }
        }
    }
}

fun println(str: String = "") {
    kotlin.io.println(str)
    logText += str + "\n"
}

fun readln(): String {
    val str = kotlin.io.readln()
    logText += str + "\n"
    return str
}

fun add(cards: LinkedHashMap<String, String>): Pair<LinkedHashMap<String, String>, LinkedHashMap<String, String>> {
    println("Card:")

    val term: String = readln()
    if (cards.containsKey(term)) {
        println("The card \"$term\" already exists.\n")
    } else {
        println("The definition of the card:")
        val definition = readln()

        if (cards.containsValue(definition)) {
            println("The definition \"$definition\" already exists.\n")
        } else {
            println("The pair (\"$term\":\"$definition\") has been added.\n")
            return Pair(linkedMapOf(term to definition), linkedMapOf(term to "0"))
        }
    }

    return Pair(LinkedHashMap<String, String>(), LinkedHashMap<String, String>())
}

fun remove(cards: LinkedHashMap<String, String>): String {
    println("Which card?")
    val term = readln()

    return if (cards.containsKey(term)) {
        println("The card has been removed.\n")
        term

    } else {
        println("Can't remove \"$term\": there is no such card.\n")
        ""
    }
}

fun import(): Pair<LinkedHashMap<String, String>, LinkedHashMap<String, String>> {
    val cardsImports = LinkedHashMap<String, String>()
    val mistakesImports = LinkedHashMap<String, String>()

    println("File name:")
    val fileName = readln()
    try {
        File(fileName).forEachLine {
            val (term, definition, mistake) = it.split(":")
            cardsImports[term] = definition
            mistakesImports[term] = mistake
        }
    } catch (e: Exception) {
        println("File not found.\n")
    }

    return Pair(cardsImports, mistakesImports)
}

fun import(fileName: String): Pair<LinkedHashMap<String, String>, LinkedHashMap<String, String>> {
    val cardsImports = LinkedHashMap<String, String>()
    val mistakesImports = LinkedHashMap<String, String>()

    File(fileName).forEachLine {
        val (term, definition, mistake) = it.split(":")
        cardsImports[term] = definition
        mistakesImports[term] = mistake
    }
    return Pair(cardsImports, mistakesImports)
}

fun export(cards: LinkedHashMap<String, String>, mistakes: LinkedHashMap<String, String>) {
    println("File name:")
    val fileName = readln()
    val output = File(fileName)
    output.delete()

    for (term in cards.keys) {
        output.appendText("${term}:${cards[term]}:${mistakes[term]}\n")
    }
    println("${cards.size} cards have been saved.")
}

fun export(fileName: String, cards: LinkedHashMap<String, String>, mistakes: LinkedHashMap<String, String>) {
    val output = File(fileName)
    output.delete()

    for (term in cards.keys) {
        output.appendText("${term}:${cards[term]}:${mistakes[term]}\n")
    }
    println("${cards.size} cards have been saved.")
}

fun ask(cards: LinkedHashMap<String, String>, mistakes: LinkedHashMap<String, String>): LinkedHashMap<String, String> {
    println("How many times to ask?")
    val quantity = readln().toInt()
    var verge = 0

    for (i in cards) {
        if (verge == quantity) {
            break
        }
        println("Print the definition of \"${i.key}\":")
        val answer = readln()
        if (answer == i.value) {
            println("Correct!\n")
        } else {
            if (cards.containsValue(answer)) {
                for (j in cards) {
                    if (answer == j.value) {
                        println(
                            "Wrong. The right answer is \"${i.value}\", " +
                                    "but your definition is correct for \"${j.key}\"\n"
                        )
                    }
                }
            } else println("Wrong. The right answer is \"${i.value}\".\n")
            mistakes[i.key] = (mistakes[i.key]!!.toInt() + 1).toString()
        }
        verge++
    }
    return mistakes
}

fun log() {
    println("File name:")
    val fileName = readln()
    val saveLog = File(fileName)

    for (card in logText) {
        saveLog.appendText(card)
    }

    println("The log has been saved.")
}

fun hardestCard(mistakes: LinkedHashMap<String, String>) {
    val maxMistakes = mutableListOf<String>()
    var maxKey = 0

    for (mistake in mistakes.values) {
        if (mistake.toInt() > maxKey) maxKey = mistake.toInt()
    }

    for (mistake in mistakes) {
        if (mistake.value.toInt() != 0 && mistake.value.toInt() == maxKey) maxMistakes += mistake.key
    }

    if (maxMistakes.isEmpty()) {
        println("There are no cards with errors.\n")
    } else if (maxMistakes.size == 1) {
        println("The hardest card is \"${maxMistakes.joinToString()}\". You have $maxKey errors answering it.\n")
    } else {
        println(
            "The hardest cards are "
                    + maxMistakes.joinToString("\", \"", "\"", "\"")
                    + ". You have ${maxKey * maxMistakes.size} errors answering them.\n"
        )
    }
}