// ios/KaMPKitiOS/ContentView.swift
import SwiftUI
import shared
import Firebase
import Foundation
import Firebase
import Combine
import Foundation
import FirebaseCore       // For core Firebase functionality
import FirebaseAuth       // For Auth.auth() and authentication functions
import GoogleSignIn       // For GoogleSignIn
import AuthenticationServices  // For Apple authentication
import shared

// ios/KaMPKitiOS/ContentView.swift
import SwiftUI
import shared
import Firebase

struct ContentView: View {
    @StateObject private var authViewModel = AuthViewModelWrapper()
    @State private var isShowingRegister = false
    @EnvironmentObject var themeManager: EmotionThemeManager
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        ZStack {
            // Background color based on theme
            colors.background.edgesIgnoringSafeArea(.all)
            
            if case .authenticated = authViewModel.authState {
                
                // User is logged in, show main app content
                MainAppView(authViewModel: authViewModel)
            } else {
                // User is not logged in, show login screen
                LoginView(authViewModel: authViewModel)
                    .sheet(isPresented: $isShowingRegister) {
                        RegisterView(authViewModel: authViewModel)
                    }
                    .onChange(of: authViewModel.authState) { newState in
                        // If we just registered successfully, dismiss the register sheet
                        if case .authenticated = newState {
                            isShowingRegister = false
                        }
                    }
                    .toolbar {
                        ToolbarItem(placement: .navigationBarTrailing) {
                            Button("Register") {
                                isShowingRegister = true
                            }
                            .foregroundColor(colors.interactive)
                            .font(typography.bodyMedium)
                        }
                        
                        // Theme toggle button
                        ToolbarItem(placement: .navigationBarLeading) {
                            Button(action: {
                                themeManager.toggleTheme()
                            }) {
                                Image(systemName: themeManager.currentTheme == .COSMIC ? "sun.max.fill" : "moon.fill")
                                    .foregroundColor(colors.interactive)
                            }
                        }
                    }
            }
        }
        .onAppear {
            // Check if user is already logged in when the app starts
            checkCurrentUser()
        }
    }
    
    private func checkCurrentUser() {
        // Firebase Auth already handles persisting the user session,
        // so we just need to check if there's a current user and update our state
        if let user = Auth.auth().currentUser {
            // User is already signed in, create Kotlin User object and update the ViewModel
            let kotlinUser = User(
                id: user.uid,
                email: user.email ?? "",
                displayName: user.displayName,
                isEmailVerified: user.isEmailVerified,
                subscriptionType: SubscriptionType.free,
                createdAt: Int64(user.metadata.creationDate?.timeIntervalSince1970 ?? 0),
                lastLoginAt: Int64(user.metadata.lastSignInDate?.timeIntervalSince1970 ?? 0)
            )
            print(user.email)
            
            // Update the view model with the authenticated user (correct way to call the method)
            authViewModel.setAuthenticatedUser(user: kotlinUser)
        }
    }
}

// MARK: - AppDelegate Extensions
extension AppDelegate {
    // Add a method to refresh auth token if needed
    func refreshAuthTokenIfNeeded(completion: @escaping (Bool) -> Void) {
        if let user = Auth.auth().currentUser {
            user.getIDTokenForcingRefresh(true) { token, error in
                if let error = error {
                    print("Error refreshing auth token: \(error.localizedDescription)")
                    completion(false)
                    return
                }
                
                if let token = token {
                    // Store token in secure storage or use it for API calls
                    print("Token refreshed successfully")
                    completion(true)
                    return
                }
                
                completion(false)
            }
        } else {
            completion(false)
        }
    }
}

// MARK: - Preview
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let themeManager = EmotionThemeManager()
        
        Group {
            ContentView()
                .environmentObject(themeManager)
                .modifier(EmotionThemeModifier(themeManager: themeManager))
            
            ContentView()
                .environmentObject({
                    let manager = EmotionThemeManager()
                    manager.currentTheme = .SERENE
                    return manager
                }())
                .modifier(EmotionThemeModifier(themeManager: {
                    let manager = EmotionThemeManager()
                    manager.currentTheme = .SERENE
                    return manager
                }()))
        }
    }
}
