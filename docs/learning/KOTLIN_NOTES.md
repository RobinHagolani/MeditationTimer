# 📚 Kotlin Learning Notes

## 🎯 Learning Goals
- Understanding Kotlin syntax and differences from Java
- Mastering Kotlin-specific features
- Effective use of Jetpack Compose
- Understanding Coroutines and asynchronous programming

## 🔄 Java vs. Kotlin - Key Differences

### 1. Null-Safety
```kotlin
// Java: Can lead to NullPointerException
String name = null;

// Kotlin: Compiler prevents NullPointerException
var name: String = "Default"        // CAN'T be null
var nullableName: String? = null    // Can be null
nullableName?.length               // Safe call operator
```

### 2. Properties vs. Getter/Setter
```kotlin
// Java
public class Person {
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// Kotlin
class Person {
    var name: String = ""  // Getter and Setter automatically created
    val age: Int = 0      // Only getter (immutable)
}
```

### 3. Data Classes
```kotlin
// Kotlin - automatically generates equals(), hashCode(), toString(), copy()
data class Person(
    val name: String,
    val age: Int
)
```

## 📝 Open Questions
[We will add your questions here as they come up during development]

## 🎓 Important Concepts to Learn

### Basic Syntax
- [ ] Val vs. Var (Immutable vs. Mutable variables)
- [ ] Null-Safety and Elvis Operator (?:)
- [ ] String Templates ("Hello, $name")
- [ ] When Expression (Switch-Case replacement)
- [ ] Type Inference (var vs explicit types)

### Functions
- [ ] Top-Level Functions (no class needed)
- [ ] Extension Functions
- [ ] Lambda Expressions
- [ ] Higher-Order Functions
- [ ] Default Arguments

### Classes and Objects
- [ ] Data Classes
- [ ] Object Declaration (Singleton)
- [ ] Companion Objects
- [ ] Sealed Classes
- [ ] Property Delegation

### Coroutines
- [ ] Basic Understanding
- [ ] Scopes and Contexts
- [ ] Suspend Functions
- [ ] Flow
- [ ] Exception Handling

### Jetpack Compose
- [ ] Composable Functions
- [ ] State Management
- [ ] Side Effects
- [ ] Layouts and Modifiers
- [ ] Recomposition

## 💡 Practical Examples from Our Project

### Basic Kotlin Features
```kotlin
// String templates
val time = 300
println("Time remaining: ${time / 60}:${time % 60}")

// When expression
val timeText = when {
    time > 3600 -> "${time / 3600}h ${(time % 3600) / 60}m"
    time > 60 -> "${time / 60}m ${time % 60}s"
    else -> "${time}s"
}
```

### Compose UI Examples
```kotlin
@Composable
fun TimerDisplay(seconds: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(seconds),
            style = MaterialTheme.typography.displayLarge
        )
    }
}
```

## 📖 Useful Resources
- [Kotlin Official Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Koans (Interactive Exercises)](https://play.kotlinlang.org/koans/overview)

### 2. Branch Naming