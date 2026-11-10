package calculator

import java.math.BigInteger
import java.util.ArrayDeque

class Calculator {
    val variables = mutableMapOf<String, BigInteger>()

    companion object {
        val validIdentifierRegex = "^[a-zA-Z]+$".toRegex()
        val validNumberRegex = "^-?\\d+$".toRegex()
        val invalidMultiOperatorRegex = "(\\*\\*+|//+|\\^\\^+)".toRegex()
    }

    fun handleAssignment(line: String) {
        val parts = line.split("=")
        if (parts.size != 2) {
            throw InvalidAssignmentException()
        }

        val varName = parts[0].trim()
        val rawValue = parts[1].trim()

        if (!validIdentifierRegex.matches(varName)) {
            throw InvalidIdentifierException()
        }

        if (validNumberRegex.matches(rawValue)) {
            variables[varName] = BigInteger(rawValue)
        } else if (validIdentifierRegex.matches(rawValue)) {
            if (variables.containsKey(rawValue)) {
                variables[varName] = variables[rawValue]!!
            } else {
                throw UnknownVariableException()
            }
        } else {
            // Check if right side is an expression or invalid
            try {
                val value = calculate(rawValue)
                variables[varName] = value
            } catch (_: Exception) {
                throw InvalidAssignmentException()
            }
        }
    }

    fun calculate(expression: String): BigInteger {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) return BigInteger.ZERO

        val postfix = toPostfix(tokens)
        return evaluatePostfix(postfix)
    }

    private fun tokenize(input: String): List<String> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Check for forbidden repeated operators like **, ///, etc.
        if (invalidMultiOperatorRegex.containsMatchIn(trimmed)) {
            throw InvalidExpressionException()
        }

        val rawTokens = mutableListOf<String>()
        var i = 0
        while (i < trimmed.length) {
            val ch = trimmed[i]
            when {
                ch.isWhitespace() -> {
                    i++
                }
                ch == '+' || ch == '-' -> {
                    var countMinus = 0
                    while (i < trimmed.length && (trimmed[i] == '+' || trimmed[i] == '-')) {
                        if (trimmed[i] == '-') countMinus++
                        i++
                    }
                    rawTokens.add(if (countMinus % 2 == 1) "-" else "+")
                }
                ch.isDigit() -> {
                    val start = i
                    while (i < trimmed.length && trimmed[i].isDigit()) i++
                    rawTokens.add(trimmed.substring(start, i))
                }
                ch.isLetter() -> {
                    val start = i
                    while (i < trimmed.length && trimmed[i].isLetter()) i++
                    rawTokens.add(trimmed.substring(start, i))
                }
                ch in "*/()^" -> {
                    rawTokens.add(ch.toString())
                    i++
                }
                else -> {
                    throw InvalidExpressionException()
                }
            }
        }

        // Process unary plus/minus
        val tokens = mutableListOf<String>()
        for (idx in rawTokens.indices) {
            val token = rawTokens[idx]
            val isUnary = (token == "+" || token == "-") &&
                    (idx == 0 || rawTokens[idx - 1] == "(" || rawTokens[idx - 1] in setOf("+", "-", "*", "/", "^"))
            if (isUnary) { if (token == "-") tokens.add("unary-") } else tokens.add(token)
        }
        return tokens
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "^" -> 3
            "unary-" -> 4
            else -> 0
        }
    }

    private fun toPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.all { it.isDigit() } || validIdentifierRegex.matches(token) -> {
                    output.add(token)
                }
                token == "(" -> {
                    stack.push(token)
                }
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isEmpty() || stack.peek() != "(") {
                        throw InvalidExpressionException()
                    }
                    stack.pop() // Discard '('
                }
                token in setOf("+", "-", "*", "/", "^", "unary-") -> {
                    val p = precedence(token)
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        val topP = precedence(stack.peek())
                        // '^' and 'unary-' are right-associative
                        val isRightAssociative = token == "^" || token == "unary-"
                        if ((!isRightAssociative && topP >= p) || (isRightAssociative && topP > p)) {
                            output.add(stack.pop())
                        } else {
                            break
                        }
                    }
                    stack.push(token)
                }
                else -> {
                    throw InvalidExpressionException()
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.pop()
            if (top == "(" || top == ")") {
                throw InvalidExpressionException()
            }
            output.add(top)
        }

        return output
    }

    private fun evaluatePostfix(postfix: List<String>): BigInteger {
        val stack = ArrayDeque<BigInteger>()

        for (token in postfix) {
            when {
                token.all { it.isDigit() } -> {
                    stack.push(BigInteger(token))
                }
                validIdentifierRegex.matches(token) -> {
                    val value = variables[token] ?: throw UnknownVariableException()
                    stack.push(value)
                }
                token == "unary-" -> {
                    if (stack.isEmpty()) throw InvalidExpressionException()
                    val operand = stack.pop()
                    stack.push(operand.negate())
                }
                token in setOf("+", "-", "*", "/", "^") -> {
                    if (stack.size < 2) throw InvalidExpressionException()
                    val b = stack.pop()
                    val a = stack.pop()
                    val res = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> {
                            if (b == BigInteger.ZERO) throw InvalidExpressionException("Division by zero")
                            a / b
                        }
                        "^" -> {
                            if (b < BigInteger.ZERO) throw InvalidExpressionException()
                            a.pow(b.toInt())
                        }
                        else -> throw InvalidExpressionException()
                    }
                    stack.push(res)
                }
                else -> throw InvalidExpressionException()
            }
        }

        if (stack.size != 1) {
            throw InvalidExpressionException()
        }

        return stack.pop()
    }
}