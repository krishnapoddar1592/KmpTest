// ios/KaMPKitiOS/Theme/EmotionThemeModifier.swift
import SwiftUI

// Theme modifier to apply consistent styling throughout the app
struct EmotionThemeModifier: ViewModifier {
    @ObservedObject var themeManager: EmotionThemeManager
    
    func body(content: Content) -> some View {
        content
            .environment(\.emotionColors, themeManager.colors)
            .environment(\.emotionTypography, EmotionTypographyProvider.typography)
            .preferredColorScheme(themeManager.colors.isLight ? .light : .dark)
    }
}

// Environment keys for theme access
private struct EmotionColorsKey: EnvironmentKey {
    static let defaultValue = EmotionColorScheme(
        background: .black,
        backgroundSecondary: .gray,
        interactive: .blue,
        interactiveSecondary: .green,
        interactiveDisabled: .gray,
        textPrimary: .white,
        textSecondary: .gray,
        textDisabled: .gray.opacity(0.5),
        isLight: false
    )
}

private struct EmotionTypographyKey: EnvironmentKey {
    static let defaultValue = EmotionTypography()
}

// Environment value extensions
extension EnvironmentValues {
    var emotionColors: EmotionColorScheme {
        get { self[EmotionColorsKey.self] }
        set { self[EmotionColorsKey.self] = newValue }
    }
    
    var emotionTypography: EmotionTypography {
        get { self[EmotionTypographyKey.self] }
        set { self[EmotionTypographyKey.self] = newValue }
    }
}

// View extension for easy theme application
extension View {
    func emotionTheme(_ themeManager: EmotionThemeManager) -> some View {
        self.modifier(EmotionThemeModifier(themeManager: themeManager))
    }
    
    // Themed foreground color
    func foregroundColor(from keyPath: KeyPath<EmotionColorScheme, Color>) -> some View {
        self.modifier(EmotionForegroundColorModifier(keyPath: keyPath))
    }
    
    // Themed background
    func backgroundColor(from keyPath: KeyPath<EmotionColorScheme, Color>) -> some View {
        self.modifier(EmotionBackgroundColorModifier(keyPath: keyPath))
    }
    
    // Themed font
    func font(from keyPath: KeyPath<EmotionTypography, Font>) -> some View {
        self.modifier(EmotionFontModifier(keyPath: keyPath))
    }
}

// Modifiers for applying themed styles
struct EmotionForegroundColorModifier: ViewModifier {
    @Environment(\.emotionColors) private var colors
    let keyPath: KeyPath<EmotionColorScheme, Color>
    
    func body(content: Content) -> some View {
        content.foregroundColor(colors[keyPath: keyPath])
    }
}

struct EmotionBackgroundColorModifier: ViewModifier {
    @Environment(\.emotionColors) private var colors
    let keyPath: KeyPath<EmotionColorScheme, Color>
    
    func body(content: Content) -> some View {
        content.background(colors[keyPath: keyPath])
    }
}

struct EmotionFontModifier: ViewModifier {
    @Environment(\.emotionTypography) private var typography
    let keyPath: KeyPath<EmotionTypography, Font>
    
    func body(content: Content) -> some View {
        content.font(typography[keyPath: keyPath])
    }
}
