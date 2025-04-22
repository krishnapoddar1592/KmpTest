//
//  LoginView.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 21/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/LoginView.swift
import SwiftUI
import Firebase
import GoogleSignIn
import shared

struct LoginView: View {
    @ObservedObject var authViewModel: AuthViewModelWrapper
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var isShowingAlert: Bool = false
    @State private var alertMessage: String = ""
    
    var body: some View {
        VStack(spacing: 20) {
            // App logo or header image
            Image(systemName: "lock.shield")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 100, height: 100)
                .foregroundColor(.blue)
                .padding(.bottom, 20)
            
            Text("Sign In")
                .font(.largeTitle)
                .fontWeight(.bold)
                .padding(.bottom, 20)
            
            // Email field
            TextField("Email", text: $email)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)
                .padding(.horizontal)
            
            // Password field
            SecureField("Password", text: $password)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)
                .padding(.horizontal)
            
            // Login button
            Button(action: {
                authViewModel.loginWithEmail(email: email, password: password)
            }) {
                if authViewModel.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                        .padding(.horizontal)
                } else {
                    Text("Sign In")
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                        .padding(.horizontal)
                }
            }
            
            Divider()
                .padding(.vertical)
            
            // Social sign-in options
            HStack(spacing: 20) {
                Button(action: {
                    // Google sign-in
                    signInWithGoogle()
                }) {
                    HStack {
                        Image(systemName: "g.circle.fill")
                            .resizable()
                            .frame(width: 24, height: 24)
                        Text("Sign in with Google")
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                }
                
                Button(action: {
                    // Apple sign-in
                    signInWithApple()
                }) {
                    HStack {
                        Image(systemName: "apple.logo")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 24, height: 24)
                        Text("Sign in with Apple")
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                }
            }
            .padding(.horizontal)
            
            Spacer()
            
            // Register option
            HStack {
                Text("Don't have an account?")
                    .foregroundColor(.gray)
                Button("Register") {
                    // Navigate to registration
                }
                .foregroundColor(.blue)
            }
            .padding(.bottom, 20)
        }
        .padding()
        .alert(isPresented: $isShowingAlert) {
            Alert(
                title: Text("Error"),
                message: Text(alertMessage),
                dismissButton: .default(Text("OK"))
            )
        }
        .onChange(of: authViewModel.authState) { state in
            if case let .error(message) = state {
                isShowingAlert = true
                alertMessage = message
            }
        }
    }
    
    func signInWithGoogle() {
        guard let clientID = FirebaseApp.app()?.options.clientID else { return }
        
        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config
        
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootViewController = windowScene.windows.first?.rootViewController else {
            return
        }
        
        GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
            if let error = error {
                isShowingAlert = true
                alertMessage = error.localizedDescription
                return
            }
            
            guard let user = result?.user,
                  let idToken = user.idToken?.tokenString else {
                isShowingAlert = true
                alertMessage = "Failed to get ID token"
                return
            }
            
            authViewModel.loginWithGoogle(idToken: idToken)
        }
    }
    
    func signInWithApple() {
        // Implement Apple Sign In flow
    }
}
