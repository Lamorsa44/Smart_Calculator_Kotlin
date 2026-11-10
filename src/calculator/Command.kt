package calculator

sealed class Command {
    abstract fun perform(ui: CalculatorUI)

    object ExitCommand : Command() {
        override fun perform(ui: CalculatorUI) {
            println("Bye!")
            ui.shutdown()
        }
    }

    object HelpCommand : Command() {
        override fun perform(ui: CalculatorUI) {
            println("The program calculates arithmetic expressions with +, -, *, /, ^, parentheses, and variables.")
        }
    }

    object UnknownCommand : Command() {
        override fun perform(ui: CalculatorUI) {
            println("Unknown command")
        }
    }
}