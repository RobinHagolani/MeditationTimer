# 🏗 Project Structure

## Overview
This document describes the architecture and structure of the MeditationTimer app, serving as the foundation for all development decisions.

## Directory Structure
```kotlin
app/src/main/java/com/robinhagolani/meditationtimer/
├── ui/                  // UI Components and Screens
│   ├── components/      // Reusable UI components
│   │   ├── Timer.kt    // Timer display and controls
│   │   └── Breathing.kt // Breathing animation
│   ├── screens/         // Main app screens
│   │   └── MainScreen.kt // Primary meditation screen
│   └── theme/          // App theming
│       ├── Color.kt    // Color definitions
│       ├── Theme.kt    // Theme configuration
│       └── Type.kt     // Typography
├── data/               // Data Layer
│   ├── models/         // Data models
│   │   └── Session.kt  // Meditation session data
│   └── database/       // Database access
│       └── SessionDao.kt // Database operations
└── viewmodels/        // ViewModels (MVVM pattern)
    └── MainViewModel.kt // Main screen logic

docs/                   // Documentation
├── architecture/      // Architectural decisions
├── features/         // Feature specifications
├── learning/         // Learning resources
├── development/      // Development guidelines
└── context/         // RAG context for AI assistance
```

## Architecture Principles

### 1. Clean Architecture
We follow Clean Architecture principles to ensure:
- Separation of concerns
- Testability
- Maintainability
- Scalability

#### Layers:
1. **UI Layer** (Presentation)
    - Composable functions
    - UI state handling
    - User interactions

2. **Domain Layer** (Business Logic)
    - ViewModels
    - Use Cases
    - Business rules

3. **Data Layer** (Data Access)
    - Repositories
    - Local database
    - Data models

### 2. MVVM Pattern
We use the Model-View-ViewModel pattern because:
- It's recommended by Google
- Works well with Jetpack Compose
- Provides clear separation of concerns

Components:
- **Model**: Data classes in `data/models`
- **View**: Composables in `ui/`
- **ViewModel**: Classes in `viewmodels/`

### 3. Dependency Injection
Using Hilt for DI to:
- Manage dependencies
- Facilitate testing
- Reduce boilerplate

### 4. State Management
Using Kotlin Flow and StateFlow for:
- Reactive UI updates
- Predictable state changes
- Easy testing

## Development Workflow

### 1. Feature Development
1. Create feature specification in `docs/features`
2. Implement data models
3. Create ViewModel
4. Build UI components
5. Add tests

### 2. Testing Strategy
- Unit tests for ViewModels and business logic
- UI tests for Composables
- Integration tests for data layer

### 3. Documentation
Each new feature requires:
- Feature specification
- API documentation
- Usage examples
- Test coverage

## Technology Stack

### Core Technologies
- Kotlin 1.8.x
- Jetpack Compose
- Material3
- Coroutines & Flow

### Development Tools
- Android Studio
- Git
- Gradle

## Getting Started
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run the app

## Next Steps
- [ ] Implement basic timer functionality
- [ ] Add breathing animation
- [ ] Create statistics tracking
- [ ] Implement settings

### Recent Updates (05 April 2025)

#### Project Setup
- Upgraded AGP to 8.5.0
- Configured Hilt 2.50 for dependency injection
- Set up Java 17 compatibility
- Created MeditationTimerApp.kt as Hilt entry point

#### Directory Structure Updates
Added:
- `MeditationTimerApp.kt` - Hilt Application class
- `di/` - Dependency Injection modules (planned)
- `domain/` - Business logic layer (planned)
   - `usecases/` - Business logic implementations
   - `repositories/` - Repository interfaces
   - `models/` - Domain models
