//
//  EmotionFontFamily.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 28/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/Theme/EmotionTypography.swift
import SwiftUI

// Define the font families to match Android Typography
struct EmotionFontFamily {
    static let cabinetGrotesk = "CabinetGrotesk"
    static let generalSans = "GeneralSans"
    
    // If the custom fonts are not available, use system fonts as fallbacks
    static func cabinetGroteskFont(size: CGFloat, weight: Font.Weight = .bold) -> Font {
        if UIFont.familyNames.contains(cabinetGrotesk) {
            return Font.custom(cabinetGrotesk, size: size).weight(weight)
        } else {
            return Font.system(size: size, weight: weight, design: .rounded)
        }
    }
    
    static func generalSansFont(size: CGFloat, weight: Font.Weight = .semibold) -> Font {
        if UIFont.familyNames.contains(generalSans) {
            return Font.custom(generalSans, size: size).weight(weight)
        } else {
            return Font.system(size: size, weight: weight, design: .default)
        }
    }
}

// Typography system based on the Android design
struct EmotionTypography {
    // Bold Heading - Cabinet Grotesk Bold 72sp
    let displayLarge: Font = EmotionFontFamily.cabinetGroteskFont(size: 72)
    
    // Heading1 - Cabinet Grotesk Bold 48sp
    let displayMedium: Font = EmotionFontFamily.cabinetGroteskFont(size: 48)
    
    // H1 - General Sans Semibold 32sp
    let headlineLarge: Font = EmotionFontFamily.generalSansFont(size: 32)
    
    // Semibold14 - General Sans Semibold 14sp
    let bodyMedium: Font = EmotionFontFamily.generalSansFont(size: 14)
    
    // Body text
    let bodyLarge: Font = EmotionFontFamily.generalSansFont(size: 16, weight: .regular)
    
    // Title for buttons, labels, etc.
    let titleMedium: Font = EmotionFontFamily.generalSansFont(size: 16)
}

// Extension to calculate line heights (rough approximation)
extension View {
    func lineSpacing(forFont font: Font, multiplier: CGFloat = 1.3) -> some View {
        let fontSize: CGFloat
        
        // Estimate the font size based on the font
        switch font {
        case EmotionFontFamily.cabinetGroteskFont(size: 72):
            fontSize = 72
        case EmotionFontFamily.cabinetGroteskFont(size: 48):
            fontSize = 48
        case EmotionFontFamily.generalSansFont(size: 32):
            fontSize = 32
        case EmotionFontFamily.generalSansFont(size: 16, weight: .semibold):
            fontSize = 16
        case EmotionFontFamily.generalSansFont(size: 16, weight: .regular):
            fontSize = 16
        case EmotionFontFamily.generalSansFont(size: 14):
            fontSize = 14
        default:
            fontSize = 16
        }
        
        return self.lineSpacing((fontSize * multiplier) - fontSize)
    }
}

// Global singleton for easy access
struct EmotionTypographyProvider {
    static let typography = EmotionTypography()
}