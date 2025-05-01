//
//  MainAppView.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 28/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/MainAppView.swift
import SwiftUI
import shared

struct MainAppView: View {
    @ObservedObject var authViewModel: AuthViewModelWrapper
    @State private var selectedTab: String = "Home"
    @State private var showEmotionDetection = false
    
    @EnvironmentObject var themeManager: EmotionThemeManager
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()
            
            VStack {
                // Content area
                ZStack {
                    // Home screen
                    if selectedTab == "Home" {
                        HomeScreen(
                            onEmotionDetectionTap: { 
                                showEmotionDetection = true 
                            }
                        )
                    }
                    
                    // Stats screen
                    else if selectedTab == "Stats" {
                        StatsScreen()
                    }
                    
                    // Calendar screen
                    else if selectedTab == "Calendar" {
                        CalendarScreen()
                    }
                    
                    // Breathing screen
                    else if selectedTab == "Breathing" {
                        BreathingScreen()
                    }
                    
                    // Settings screen
                    else if selectedTab == "Settings" {
                        SettingsScreen(
                            onSignOut: {
                                authViewModel.logout()
                            },
                            onToggleTheme: {
                                themeManager.toggleTheme()
                            },
                            currentTheme: themeManager.currentTheme
                        )
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                
                // Bottom navigation bar
                BottomNavigationBar(selectedTab: $selectedTab)
            }
        }
        .sheet(isPresented: $showEmotionDetection) {
            EmotionDetectionView()
                .environmentObject(themeManager)
                .emotionTheme(themeManager)
        }
    }
}

// MARK: - Home Screen
struct HomeScreen: View {
    var onEmotionDetectionTap: () -> Void
    
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        VStack(spacing: 24) {
            Text("Every Emotion Matters")
                .font(typography.displayMedium)
                .foregroundColor(colors.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 32)
            
            Text("A journey to self-awareness starts here.")
                .font(typography.bodyLarge)
                .foregroundColor(colors.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Spacer()
            
            // Features Cards
            VStack(spacing: 16) {
                FeatureCard(
                    title: "Detect Emotion",
                    description: "Use your camera to detect your current emotion",
                    iconName: "face.smiling",
                    action: onEmotionDetectionTap
                )
                
                FeatureCard(
                    title: "Emotion Journal",
                    description: "Track your emotions over time",
                    iconName: "book.fill",
                    action: {}
                )
                
                FeatureCard(
                    title: "Guided Breathing",
                    description: "Relax with guided breathing exercises",
                    iconName: "wind",
                    action: {}
                )
            }
            .padding(.horizontal)
            
            Spacer()
        }
    }
}

struct FeatureCard: View {
    var title: String
    var description: String
    var iconName: String
    var action: () -> Void
    
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                // Icon in circle
                ZStack {
                    Circle()
                        .fill(colors.interactive.opacity(0.1))
                        .frame(width: 60, height: 60)
                    
                    Image(systemName: iconName)
                        .font(.system(size: 24))
                        .foregroundColor(colors.interactive)
                }
                
                // Text content
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(typography.titleMedium)
                        .foregroundColor(colors.textPrimary)
                    
                    Text(description)
                        .font(typography.bodyMedium)
                        .foregroundColor(colors.textSecondary)
                        .lineLimit(2)
                }
                
                Spacer()
                
                // Chevron
                Image(systemName: "chevron.right")
                    .foregroundColor(colors.textSecondary)
            }
            .padding()
            .background(colors.backgroundSecondary)
            .cornerRadius(12)
        }
    }
}

// MARK: - Stats Screen
struct StatsScreen: View {
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        VStack {
            Text("Stats")
                .font(typography.headlineLarge)
                .foregroundColor(colors.textPrimary)
                .padding()
            
            Text("Your emotion statistics will appear here")
                .font(typography.bodyLarge)
                .foregroundColor(colors.textSecondary)
            
            Spacer()
        }
    }
}

// MARK: - Calendar Screen
struct CalendarScreen: View {
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        VStack {
            Text("Calendar")
                .font(typography.headlineLarge)
                .foregroundColor(colors.textPrimary)
                .padding()
            
            Text("Your emotion calendar will appear here")
                .font(typography.bodyLarge)
                .foregroundColor(colors.textSecondary)
            
            Spacer()
        }
    }
}

// MARK: - Breathing Screen
struct BreathingScreen: View {
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        VStack {
            Text("Breathing Exercises")
                .font(typography.headlineLarge)
                .foregroundColor(colors.textPrimary)
                .padding()
            
            Text("Guided breathing exercises will appear here")
                .font(typography.bodyLarge)
                .foregroundColor(colors.textSecondary)
            
            Spacer()
        }
    }
}

// MARK: - Settings Screen
struct SettingsScreen: View {
    var onSignOut: () -> Void
    var onToggleTheme: () -> Void
    var currentTheme: EmotionAppTheme
    
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        VStack {
            Text("Settings")
                .font(typography.headlineLarge)
                .foregroundColor(colors.textPrimary)
                .padding()
            
            // Theme toggle
            VStack(spacing: 10) {
                Text("Appearance")
                    .font(typography.titleMedium)
                    .foregroundColor(colors.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                HStack {
                    Text("Theme")
                        .font(typography.bodyLarge)
                        .foregroundColor(colors.textSecondary)
                    
                    Spacer()
                    
                    Button(action: onToggleTheme) {
                        HStack(spacing: 8) {
                            Image(systemName: currentTheme == .COSMIC ? "moon.fill" : "sun.max.fill")
                                .foregroundColor(colors.interactive)
                            
                            Text(currentTheme == .COSMIC ? "Cosmic" : "Serene")
                                .font(typography.bodyMedium)
                                .foregroundColor(colors.textPrimary)
                        }
                        .padding(.vertical, 8)
                        .padding(.horizontal, 12)
                        .background(colors.backgroundSecondary)
                        .cornerRadius(8)
                    }
                }
            }
            .padding()
            .background(colors.backgroundSecondary.opacity(0.5))
            .cornerRadius(12)
            .padding(.horizontal)
            
            Spacer()
            
            // Sign out button
            Button(action: onSignOut) {
                Text("Sign Out")
                    .font(typography.titleMedium)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.red)
                    .cornerRadius(8)
                    .padding(.horizontal, 24)
            }
            .padding(.bottom, 32)
        }
    }
}

// MARK: - Bottom Navigation Bar
struct BottomNavigationBar: View {
    @Binding var selectedTab: String
    
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        HStack {
            NavBarItem(
                iconName: "chart.bar",
                label: "Stats",
                isSelected: selectedTab == "Stats",
                onTap: { selectedTab = "Stats" }
            )
            
            NavBarItem(
                iconName: "calendar",
                label: "Calendar",
                isSelected: selectedTab == "Calendar",
                onTap: { selectedTab = "Calendar" }
            )
            
            NavBarItem(
                iconName: "house.fill",
                label: "Home",
                isSelected: selectedTab == "Home",
                onTap: { selectedTab = "Home" }
            )
            
            NavBarItem(
                iconName: "wind",
                label: "Breathing",
                isSelected: selectedTab == "Breathing",
                onTap: { selectedTab = "Breathing" }
            )
            
            NavBarItem(
                iconName: "gearshape",
                label: "Settings",
                isSelected: selectedTab == "Settings",
                onTap: { selectedTab = "Settings" }
            )
        }
        .padding(.top, 8)
        .padding(.bottom, 16)
        .background(colors.background)
        .overlay(
            Rectangle()
                .frame(height: 1)
                .foregroundColor(colors.textSecondary.opacity(0.2)),
            alignment: .top
        )
    }
}

struct NavBarItem: View {
    var iconName: String
    var label: String
    var isSelected: Bool
    var onTap: () -> Void
    
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                Image(systemName: iconName)
                    .font(.system(size: 24))
                    .foregroundColor(isSelected ? colors.interactive : colors.textSecondary)
                
                Text(label)
                    .font(typography.bodyMedium)
                    .foregroundColor(isSelected ? colors.interactive : colors.textSecondary)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

//struct MainAppView_Previews: PreviewProvider {
//    static var previews: some View {
//        Group {
//            // Preview COSMIC theme
//            let cosmicManager = EmotionThemeManager()
//            cosmicManager.currentTheme = .COSMIC
//            
//            MainAppView(authViewModel: AuthViewModelWrapper())
//                .environmentObject(cosmicManager)
//                .modifier(EmotionThemeModifier(themeManager: cosmicManager))
//                .previewDisplayName("COSMIC Theme")
//            
//            // Preview SERENE theme
//            let sereneManager = EmotionThemeManager()
//            sereneManager.currentTheme = .SERENE
//            
//            MainAppView(authViewModel: AuthViewModelWrapper())
//                .environmentObject(sereneManager)
//                .modifier(EmotionThemeModifier(themeManager: sereneManager))
//                .previewDisplayName("SERENE Theme")
//        }
//    }
//}
