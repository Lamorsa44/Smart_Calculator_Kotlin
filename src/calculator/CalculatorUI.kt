package calculator

import java.util.Scanner

class CalculatorUI(val calculator: Calculator, val sc: Scanner) {
    var running = true
        private set

    companion object {
        val commands = mapOf(
            "/exit" to Command.ExitCommand,
            "/help" to Command.HelpCommand
        )
        val identifierRegex = "^[a-zA-Z]+$".toRegex()
    }

    fun shutdown() {
        running = false
    }

    fun start() {
        while (running && sc.hasNextLine()) {
            val line = sc.nextLine().trim()
            if (line.isNotEmpty()) {
                processLine(line)
            }
        }
    }

    private fun processLine(line: String) {
        try {
            when {
                line.startsWith("/") -> handleCommand(line)
                line.contains("=") -> calculator.handleAssignment(line)
                identifierRegex.matches(line) -> handleVariableLookup(line)
                else -> println(calculator.calculate(line))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun handleCommand(commandLine: String) {
        val command = commands[commandLine] ?: Command.UnknownCommand
        command.perform(this)
    }

    private fun handleVariableLookup(name: String) {
        val value = calculator.variables[name]
        if (value != null) {
            println(value)
        } else {
            println("Unknown variable")
        }
    }

    private fun handleException(e: Exception) {
        when (e) {
            is InvalidExpressionException, is NumberFormatException, is IndexOutOfBoundsException -> println("Invalid expression")
            is UnknownVariableException -> println("Unknown variable")
            is InvalidIdentifierException -> println("Invalid identifier")
            is InvalidAssignmentException -> println("Invalid assignment")
            is IllegalArgumentException -> println(e.message ?: "Invalid expression")
            else -> throw e
        }
    }
}