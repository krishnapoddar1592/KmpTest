//
//  to.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 28/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/Theme/EmotionAppTheme.swift
import SwiftUI

// Theme enum to match Android
enum EmotionAppTheme: String, CaseIterable, Identifiable {
    case COSMIC
    case SERENE
    
    var id: String { self.rawValue }
}

// Color extensions to match Android color definitions
extension Color {
    // Neutral colors
    static let neutral = Color(hex: "F3F3F3")
    
    // COSMIC theme colors
    struct Cosmic {
        static let accent = Color(hex: "64B4F6")
        static let backgroundPrimary = Color(hex: "0D0F1A")
        static let backgroundSecondary = Color(hex: "1A1F2F")
        static let textPrimary = Color.white
        static let textSecondary = Color(hex: "E0E0E0")
        
        // Gradients
        static let gradient1Start = Color(hex: "1E2761")
        static let gradient1End = Color(hex: "1A1F2F")
    }
    
    // SERENE theme colors
    struct Serene {
        static let backgroundPrimary = Color(hex: "CBD7D7")
        static let backgroundSecondary = Color(hex: "E6F0EF")
        static let backgroundContrast = Color(hex: "96C6C6")
        static let interactivePrimary = Color(hex: "26A699")
        static let interactiveSecondary = Color(hex: "4CAF4F")
        static let interactiveDisabled = Color(hex: "8A9199")
        static let textPrimary = Color(hex: "2A2D32")
        static let textSecondary = Color(hex: "5A5F66")
        static let textDisabled = Color(hex: "8A9199")
        
        // Gradients
        static let gradient1 = Color(hex: "CBDDE0")
    }
    
    // Emotion-specific colors
    struct Emotion {
        // Joy
        static let joy1 = Color(hex: "FFE45C")
        static let joy2 = Color(hex: "FFD900")
        static let joy3 = Color(hex: "FFFEE0")
        
        // Sadness
        static let sadness1 = Color(hex: "1E2761")
        static let sadness2 = Color(hex: "400082")
        
        // Anger
        static let anger1 = Color(hex: "FF8347")
        static let anger2 = Color(hex: "FF0000")
        
        // Stress
        static let stress1 = Color(hex: "FFD980")
        static let stress2 = Color(hex: "FA9400")
        static let stress3 = Color(hex: "FF4400")
    }
    
    // Helper initializer for hex colors
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// Theme-specific color scheme
struct EmotionColorScheme {
    let background: Color
    let backgroundSecondary: Color
    let interactive: Color
    let interactiveSecondary: Color
    let interactiveDisabled: Color
    let textPrimary: Color
    let textSecondary: Color
    let textDisabled: Color
    let isLight: Bool
}

// Theme environment object to be used throughout the app
class EmotionThemeManager: ObservableObject {
    @Published var currentTheme: EmotionAppTheme = .COSMIC
    
    var colors: EmotionColorScheme {
        switch currentTheme {
        case .COSMIC:
            return EmotionColorScheme(
                background: Color.Cosmic.backgroundPrimary,
                backgroundSecondary: Color.Cosmic.backgroundSecondary,
                interactive: Color.Cosmic.accent,
                interactiveSecondary: Color.Cosmic.accent,
                interactiveDisabled: Color.Cosmic.backgroundSecondary,
                textPrimary: Color.Cosmic.textPrimary,
                textSecondary: Color.Cosmic.textSecondary,
                textDisabled: Color.Cosmic.textSecondary.opacity(0.5),
                isLight: false
            )
        case .SERENE:
            return EmotionColorScheme(
                background: Color.Serene.backgroundPrimary,
                backgroundSecondary: Color.Serene.backgroundSecondary,
                interactive: Color.Serene.interactivePrimary,
                interactiveSecondary: Color.Serene.interactiveSecondary,
                interactiveDisabled: Color.Serene.interactiveDisabled,
                textPrimary: Color.Serene.textPrimary,
                textSecondary: Color.Serene.textSecondary,
                textDisabled: Color.Serene.textDisabled,
                isLight: true
            )
        }
    }
    
    func  toggleTheme() {
        withAnimation {
            currentTheme = currentTheme == .COSMIC ? .SERENE : .COSMIC
        }
    }
}
