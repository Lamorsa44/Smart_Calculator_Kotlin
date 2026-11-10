package calculator

import java.util.Scanner

fun main() {
    Scanner(System.`in`).use {
        CalculatorUI(Calculator(), it).start()
    }
}

class InvalidExpressionException(message: String = "Invalid expression") : RuntimeException(message)
class UnknownVariableException(message: String = "Unknown variable") : RuntimeException(message)
class InvalidIdentifierException(message: String = "Invalid identifier") : RuntimeException(message)
class InvalidAssignmentException(message: String = "Invalid assignment") : RuntimeException(message)