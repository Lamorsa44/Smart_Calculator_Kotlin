# Smart Calculator (Kotlin)

A command-line arithmetic expression evaluator written in Kotlin that supports variable assignments, parenthesized
expressions, arbitrary-precision integers, and exponentiation.

---

## Features

- **Arbitrary Precision**: Uses Java's `BigInteger` for calculations, preventing integer overflow.
- **Arithmetic Operators**: Supports addition (`+`), subtraction (`-`), multiplication (`*`), integer division (`/`),
  exponentiation (`^`), and unary negation (`-`).
- **Consecutive Signs**: Normalizes multiple consecutive plus and minus signs (e.g., `2 --- 2` evaluates to
  `2 - 2 = 0`).
- **Parentheses Support**: Handles arbitrary nested parentheses with precedence tracking.
- **Variable Storage**: Assign values or results of expressions to variables (letters only: `a-z`, `A-Z`) and reference
  them in subsequent computations.
- **Built-in Commands**: Includes `/help` for basic usage information and `/exit` to terminate the program.
- **Robust Error Handling**: Detects and reports invalid expressions, unmatched parentheses, unknown variables, division
  by zero, and invalid identifiers/assignments.

---

## Architecture & Package Structure

The `calculator` package contains the following components:

- **`Main.kt`**: Entry point that instantiates the components and defines domain-specific exceptions
  (`InvalidExpressionException`, `UnknownVariableException`, `InvalidIdentifierException`,
  `InvalidAssignmentException`).
- **`CalculatorUI.kt`**: Manages the REPL loop, parses user inputs, delegates tasks to the calculator engine or command
  dispatcher, and formats error messages.
- **`Calculator.kt`**: The core calculation engine responsible for:
    - Storing and managing variables in-memory (`Map<String, BigInteger>`).
    - Handling variable assignment expressions (`handleAssignment`).
    - Tokenizing input strings and simplifying consecutive signs (`tokenize`).
    - Converting infix tokens to postfix (RPN) using Dijkstra's Shunting-Yard algorithm (`toPostfix`).
    - Evaluating postfix expressions using a stack-based evaluator (`evaluatePostfix`).
- **`Command.kt`**: Sealed class structure implementing the Command pattern for terminal instructions (`/help`, `/exit`,
  and fallback for unknown commands).

---

## Supported Syntax & Rules

### Operators & Precedence

| Operator | Associativity | Precedence | Description                                    |
|:---------|:--------------|:-----------|:-----------------------------------------------|
| `unary-` | Right         | 4          | Unary minus (e.g., `-5`, `-(2 + 3)`)           |
| `^`      | Right         | 3          | Exponentiation (non-negative integer exponent) |
| `*`, `/` | Left          | 2          | Multiplication and integer division            |
| `+`, `-` | Left          | 1          | Addition and subtraction                       |

### Variables

- Variable identifiers must consist only of Latin letters (`[a-zA-Z]+`).
- Assignments follow the syntax `<variable> = <number | variable | expression>`.
- Entering a variable's name on its own prints its current value.

### Commands

- `/help`: Prints a short help message explaining program features.
- `/exit`: Terminates the program with a farewell message.

---

## Usage Examples

### Example 1: Arbitrary Precision (Large Numbers)
Demonstrates handling very large numbers that would otherwise overflow standard 32-bit or 64-bit integer types:

```plain text
> a = 112233445566778899
> b = 998877665544332211
> a * b
11210672152847648366914594680879289
```


### Example 2: Complex Expression with Unary Minus and Nested Parentheses
Demonstrates operator precedence, right-associative unary negation, and nested groupings:

```plain text
> -(-5 + (2 * (4 + 6)))
-15
```

### Example 3: Variable Lookup and Error Reporting
Demonstrates inspecting variable values, identifier validation, and error feedback for unknown variables or malformed syntax:

```plain text
> count = 10
> count
10

> count + unknownVar
Unknown variable

> 2a = 5
Invalid identifier

> 10 / (5 - 5)
Invalid expression
```


---

## Error Handling

The application provides descriptive messages for malformed input:
- `Invalid expression`: Unbalanced parentheses, invalid characters, consecutive forbidden operators (e.g., `**`, `//`), or division by zero.
- `Unknown variable`: Attempting to use a variable before assigning a value to it.
- `Invalid identifier`: Variable names containing characters other than Latin letters.
- `Invalid assignment`: Improper syntax in assignment operations (e.g., multiple `=` signs).
- `Unknown command`: Commands starting with `/` that are not recognized.
