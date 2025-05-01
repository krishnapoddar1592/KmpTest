// In ios/KaMPKitiOS/AuthStateWrapper.swift

import Foundation
import Firebase
import Combine
import Foundation
import FirebaseCore       // For core Firebase functionality
import FirebaseAuth       // For Auth.auth() and authentication functions
import GoogleSignIn       // For GoogleSignIn
import AuthenticationServices  // For Apple authentication
import shared


enum AuthStateWrapper: Equatable {
    case initial
    case loading
    case authenticated(user: shared.User)
    case error(message: String)
    
    static func == (lhs: AuthStateWrapper, rhs: AuthStateWrapper) -> Bool {
        switch (lhs, rhs) {
        case (.initial, .initial):
            return true
        case (.loading, .loading):
            return true
        case (.authenticated(let user1), .authenticated(let user2)):
            return user1.id == user2.id
        case (.error(let message1), .error(let message2)):
            return message1 == message2
        default:
            return false
        }
    }
}

class AuthViewModelWrapper: ObservableObject {
    private let viewModel: AuthViewModel
    
    @Published var authState: AuthStateWrapper = .initial
    @Published var isLoading = false
    
    // Add a property to store the current user
    @Published var currentUser: shared.User? = nil
    
    // Create a UserDefaults key for storing the user session
    private let userSessionKey = "user_session"
    
    init() {
        self.viewModel = KotlinDependencies().getAuthViewModel()
        observeAuthState()
        checkCurrentUser()
    }
    
    private func checkCurrentUser() {
        if let user = Auth.auth().currentUser {
            // User is already signed in, update the Kotlin model with the current user
            let kotlinUser = User(
                id: user.uid,
                email: user.email ?? "",
                displayName: user.displayName,
                isEmailVerified: user.isEmailVerified,
                subscriptionType: SubscriptionType.free,
                createdAt: Int64(user.metadata.creationDate?.timeIntervalSince1970 ?? 0),
                lastLoginAt: Int64(user.metadata.lastSignInDate?.timeIntervalSince1970 ?? 0)
            )
            
            self.currentUser = kotlinUser
            viewModel.setAuthenticatedUser(user: kotlinUser)
        } else {
            // Try to restore from saved session
            restoreUserSession()
        }
    }
    
    private func observeAuthState() {
        Task {
            do {
                for await state in viewModel.authState {
                    DispatchQueue.main.async {
                        if let state = state as? AuthState {
                            switch state {
                            case is AuthState.Initial:
                                self.authState = .initial
                                self.isLoading = false
                            case is AuthState.Loading:
                                self.authState = .loading
                                self.isLoading = true
                            case let authenticated as AuthState.Authenticated:
                                self.authState = .authenticated(user: authenticated.user)
                                self.currentUser = authenticated.user
                                self.isLoading = false
                                self.saveUserSession(user: authenticated.user)
                            case let error as AuthState.Error:
                                self.authState = .error(message: error.message)
                                self.isLoading = false
                            default:
                                break
                            }
                        }
                    }
                }
            } catch {
                print("Error collecting auth state: \(error.localizedDescription)")
            }
        }
    }
    
    // MARK: - User Session Persistence
    
    private func saveUserSession(user: shared.User) {
        guard let userData = try? JSONEncoder().encode(UserSession(user: user)) else {
            print("Failed to encode user")
            return
        }
        
        UserDefaults.standard.set(userData, forKey: userSessionKey)
    }
    
    private func restoreUserSession() {
        guard let userData = UserDefaults.standard.data(forKey: userSessionKey),
              let userSession = try? JSONDecoder().decode(UserSession.self, from: userData) else {
            return
        }
        
        // Check if session is still valid
        if userSession.expiryDate > Date() {
            let user = userSession.toUser()
            self.currentUser = user
            viewModel.setAuthenticatedUser(user: user)
        } else {
            // Session expired, clear it
            UserDefaults.standard.removeObject(forKey: userSessionKey)
        }
    }
    
    // MARK: - Auth Methods
    
    func loginWithEmail(email: String, password: String) {
        viewModel.setLoadingState()
        
        FirebaseAuthManager.shared.loginWithEmail(email: email, password: password) { uid, email, displayName, isVerified, error in
            if let error = error {
                DispatchQueue.main.async {
                    self.viewModel.setAuthError(errorMessage: error.localizedDescription)
                }
            } else if let uid = uid {
                let user = User(
                    id: uid,
                    email: email ?? "",
                    displayName: displayName,
                    isEmailVerified: isVerified,
                    subscriptionType: SubscriptionType.free,
                    createdAt: Int64(Date().timeIntervalSince1970),
                    lastLoginAt: Int64(Date().timeIntervalSince1970)
                )
                
                DispatchQueue.main.async {
                    self.currentUser = user
                    self.viewModel.setAuthenticatedUser(user: user)
                }
            } else {
                DispatchQueue.main.async {
                    self.viewModel.setAuthError(errorMessage: "Unknown error during login")
                }
            }
        }
    }
    
    func loginWithGoogle(idToken: String) {
        viewModel.setLoadingState()
        
        let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: "")
        
        Auth.auth().signIn(with: credential) { authResult, error in
            if let error = error {
                DispatchQueue.main.async {
                    self.viewModel.setAuthError(errorMessage: error.localizedDescription)
                }
                return
            }
            
            guard let user = authResult?.user else {
                DispatchQueue.main.async {
                    self.viewModel.setAuthError(errorMessage: "Failed to get user from Google Sign In")
                }
                return
            }
            
            let kotlinUser = User(
                id: user.uid,
                email: user.email ?? "",
                displayName: user.displayName,
                isEmailVerified: user.isEmailVerified,
                subscriptionType: SubscriptionType.free,
                createdAt: Int64(user.metadata.creationDate?.timeIntervalSince1970 ?? 0),
                lastLoginAt: Int64(user.metadata.lastSignInDate?.timeIntervalSince1970 ?? 0)
            )
            
            DispatchQueue.main.async {
                self.currentUser = kotlinUser
                self.viewModel.setAuthenticatedUser(user: kotlinUser)
            }
        }
    }
    
    func register(email: String, password: String, displayName: String?) {
        viewModel.setLoadingState()
        
        Auth.auth().createUser(withEmail: email, password: password) { authResult, error in
            if let error = error {
                DispatchQueue.main.async {
                    self.viewModel.setAuthError(errorMessage: error.localizedDescription)
                }
                return
            }
            
            guard let user = authResult?.user else {
                DispatchQueue.main.async {
                    self.viewModel.setAuthError(errorMessage: "Failed to register user")
                }
                return
            }
            
            // If displayName is provided, update the user profile
            if let displayName = displayName, !displayName.isEmpty {
                let changeRequest = user.createProfileChangeRequest()
                changeRequest.displayName = displayName
                
                changeRequest.commitChanges { error in
                    let kotlinUser = User(
                        id: user.uid,
                        email: user.email ?? "",
                        displayName: displayName,
                        isEmailVerified: user.isEmailVerified,
                        subscriptionType: SubscriptionType.free,
                        createdAt: Int64(Date().timeIntervalSince1970),
                        lastLoginAt: Int64(Date().timeIntervalSince1970)
                    )
                    
                    DispatchQueue.main.async {
                        if let error = error {
                            self.viewModel.setAuthError(errorMessage: "Failed to set display name: \(error.localizedDescription)")
                        } else {
                            self.currentUser = kotlinUser
                            self.viewModel.setAuthenticatedUser(user: kotlinUser)
                        }
                    }
                }
            } else {
                // No display name to set, just update ViewModel with the user
                let kotlinUser = User(
                    id: user.uid,
                    email: user.email ?? "",
                    displayName: user.displayName,
                    isEmailVerified: user.isEmailVerified,
                    subscriptionType: SubscriptionType.free,
                    createdAt: Int64(Date().timeIntervalSince1970),
                    lastLoginAt: Int64(Date().timeIntervalSince1970)
                )
                
                DispatchQueue.main.async {
                    self.currentUser = kotlinUser
                    self.viewModel.setAuthenticatedUser(user: kotlinUser)
                }
            }
        }
    }
    
    func logout() {
        do {
            try Auth.auth().signOut()
            // Clear the user session
            UserDefaults.standard.removeObject(forKey: userSessionKey)
            self.currentUser = nil
            
            // Update the Kotlin ViewModel
            viewModel.setLoggedOut()
        } catch {
            print("Error signing out: \(error.localizedDescription)")
        }
    }
    
    // This is the method called from ContentView
    func setAuthenticatedUser(user: shared.User) {
        self.currentUser = user
        viewModel.setAuthenticatedUser(user: user)
        // Also save the user session for persistence
        saveUserSession(user: user)
    }
}

// MARK: - Helper Structs

// A struct for serializing user session data
struct UserSession: Codable {
    let id: String
    let email: String
    let displayName: String?
    let isEmailVerified: Bool
    let subscriptionType: String
    let createdAt: Int64
    let lastLoginAt: Int64
    let expiryDate: Date
    
    init(user: shared.User) {
        self.id = user.id
        self.email = user.email
        self.displayName = user.displayName
        self.isEmailVerified = user.isEmailVerified
        
        switch user.subscriptionType {
        case SubscriptionType.premium:
            self.subscriptionType = "PREMIUM"
        case SubscriptionType.trial:
            self.subscriptionType = "TRIAL"
        default:
            self.subscriptionType = "FREE"
        }
        
        self.createdAt = user.createdAt
        self.lastLoginAt = user.lastLoginAt
        
        // Set session expiry to 30 days from now
        self.expiryDate = Calendar.current.date(byAdding: .day, value: 30, to: Date()) ?? Date()
    }
    
    func toUser() -> shared.User {
        let subscriptionTypeEnum: SubscriptionType
        switch subscriptionType {
        case "PREMIUM":
            subscriptionTypeEnum = SubscriptionType.premium
        case "TRIAL":
            subscriptionTypeEnum = SubscriptionType.trial
        default:
            subscriptionTypeEnum = SubscriptionType.free
        }
        
        return User(
            id: id,
            email: email,
            displayName: displayName,
            isEmailVerified: isEmailVerified,
            subscriptionType: subscriptionTypeEnum,
            createdAt: createdAt,
            lastLoginAt: lastLoginAt
        )
    }
}
