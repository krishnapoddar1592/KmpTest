//
//  ReflectApp.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 21/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


//// iOSApp.swift
//import SwiftUI
//import Firebase
//import shared
//
//@main
//struct ReflectApp: App {
//    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
//    
//    var body: some Scene {
//        WindowGroup {
//            ContentView()
//        }
//    }
//}

// ios/KaMPKitiOS/ReflectApp.swift
import SwiftUI
import Firebase
import shared
import FirebaseCore       // For core Firebase functionality
import FirebaseAuth       // For Auth.auth() and authentication functions
import GoogleSignIn       // For GoogleSignIn
import AuthenticationServices

@main
struct ReflectApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var sessionManager = SessionManager()
    @StateObject private var themeManager = EmotionThemeManager()
    
    var body: some Scene {
        WindowGroup {
            NavigationView {
                ContentView()
                    .environmentObject(sessionManager)
                    .environmentObject(themeManager)
                    .emotionTheme(themeManager)
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .onAppear {
                // Configure app appearance
                configureAppAppearance()
                
                // Register custom fonts
                registerFonts()
            }
        }
    }
    
    private func configureAppAppearance() {
        // Configure the app's appearance based on current theme
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        
        if themeManager.currentTheme == .COSMIC {
            appearance.backgroundColor = UIColor(Color.Cosmic.backgroundPrimary)
            appearance.titleTextAttributes = [.foregroundColor: UIColor(Color.Cosmic.textPrimary)]
            appearance.largeTitleTextAttributes = [.foregroundColor: UIColor(Color.Cosmic.textPrimary)]
        } else {
            appearance.backgroundColor = UIColor(Color.Serene.backgroundPrimary)
            appearance.titleTextAttributes = [.foregroundColor: UIColor(Color.Serene.textPrimary)]
            appearance.largeTitleTextAttributes = [.foregroundColor: UIColor(Color.Serene.textPrimary)]
        }
        
        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().compactAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
        
        // Tab bar appearance
        let tabBarAppearance = UITabBarAppearance()
        tabBarAppearance.configureWithOpaqueBackground()
        
        if themeManager.currentTheme == .COSMIC {
            tabBarAppearance.backgroundColor = UIColor(Color.Cosmic.backgroundPrimary)
        } else {
            tabBarAppearance.backgroundColor = UIColor(Color.Serene.backgroundPrimary)
        }
        
        UITabBar.appearance().standardAppearance = tabBarAppearance
        UITabBar.appearance().scrollEdgeAppearance = tabBarAppearance
    }
    
    private func registerFonts() {
        // Register custom fonts if we have them bundled
        // This would require adding the font files to your project
        
        // Example:
        // if let url = Bundle.main.url(forResource: "CabinetGrotesk-Bold", withExtension: "ttf") {
        //     CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
        // }
        //
        // if let url = Bundle.main.url(forResource: "GeneralSans-Semibold", withExtension: "ttf") {
        //     CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
        // }
    }
}

// MARK: - Session Manager
class SessionManager: ObservableObject {
    @Published var isAuthenticated: Bool = false
    @Published var currentUser: shared.User? = nil
    
    init() {
        // Check if user is already authenticated
        setupAuthStateListener()
    }
    
    private func setupAuthStateListener() {
        Auth.auth().addStateDidChangeListener { (auth, user) in
            if let firebaseUser = user {
                self.isAuthenticated = true
                self.currentUser = User(
                    id: firebaseUser.uid,
                    email: firebaseUser.email ?? "",
                    displayName: firebaseUser.displayName,
                    isEmailVerified: firebaseUser.isEmailVerified,
                    subscriptionType: SubscriptionType.free,
                    createdAt: Int64(firebaseUser.metadata.creationDate?.timeIntervalSince1970 ?? 0),
                    lastLoginAt: Int64(firebaseUser.metadata.lastSignInDate?.timeIntervalSince1970 ?? 0)
                )
            } else {
                self.isAuthenticated = false
                self.currentUser = nil
            }
        }
    }
    
    func signOut() {
        do {
            try Auth.auth().signOut()
            self.isAuthenticated = false
            self.currentUser = nil
        } catch {
            print("Error signing out: \(error.localizedDescription)")
        }
    }
}

// MARK: - App Delegate Extensions
extension AppDelegate {
    // This method helps with token refresh and maintaining session
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // If you need to process notifications, handle them here
        completionHandler(.noData)
    }
}
