//
//  AuthStateWrapper.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 21/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/AuthViewModelWrapper.swift
import Foundation
import shared
import Combine

enum AuthStateWrapper {
    case initial
    case loading
    case authenticated(user: User)
    case error(message: String)
}

class AuthViewModelWrapper: ObservableObject {
    private let viewModel: AuthViewModel
    
    @Published var authState: AuthStateWrapper = .initial
    @Published var isLoading = false
    
    init() {
        self.viewModel = KotlinDependencies().getAuthViewModel()
        
        // Observe auth state changes
        observeAuthState()
    }
    
    private func observeAuthState() {
//        viewModel.authState.collect(collector: { state in
//            DispatchQueue.main.async {
//                if let state = state as? AuthState {
//                    switch state {
//                    case is AuthState.Initial:
//                        self.authState = .initial
//                        self.isLoading = false
//                    case is AuthState.Loading:
//                        self.authState = .loading
//                        self.isLoading = true
//                    case let authenticated as AuthState.Authenticated:
//                        self.authState = .authenticated(user: authenticated.user)
//                        self.isLoading = false
//                    case let error as AuthState.Error:
//                        self.authState = .error(message: error.message)
//                        self.isLoading = false
//                    default:
//                        break
//                    }
//                }
//            }
//        }, completionHandler: { error in
//            print("Error collecting auth state: \(error?.localizedDescription ?? "unknown error")")
//        })\
        // With this async approach:
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
                                   self.isLoading = false
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
    
    func loginWithEmail(email: String, password: String) {
        viewModel.loginWithEmail(email: email, password: password)
    }
    
    func loginWithGoogle(idToken: String) {
        viewModel.loginWithGoogle(idToken: idToken)
    }
    
    func loginWithApple(idToken: String, nonce: String?) {
        viewModel.loginWithApple(idToken: idToken, nonce: nonce)
    }
    
    func register(email: String, password: String, displayName: String?) {
        viewModel.register(email: email, password: password, displayName: displayName)
    }
    
    func logout() {
//        viewModel.
        print("logged out")
    }
}
extension AuthStateWrapper: Equatable {
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
