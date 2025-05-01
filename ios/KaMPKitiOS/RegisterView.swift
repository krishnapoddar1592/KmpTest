//
//  RegisterView.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 28/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/RegisterView.swift
import SwiftUI
import shared

struct RegisterView: View {
    @ObservedObject var authViewModel: AuthViewModelWrapper
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var displayName: String = ""
    @State private var isShowingAlert: Bool = false
    @State private var alertMessage: String = ""
    @Environment(\.presentationMode) var presentationMode
    
    @EnvironmentObject var themeManager: EmotionThemeManager
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Progress indicator
                ProgressIndicator(currentStep: 2, totalSteps: 3)
                    .padding(.top, 24)
                
                // Logo/Illustration placeholder
                PlaceholderImage()
                    .padding(.top, 24)
                    .padding(.bottom, 24)
                
                // Registration form
                VStack(spacing: 16) {
                    // Display name field
                    CustomTextField(
                        text: $displayName,
                        placeholder: "Full Name",
                        keyboardType: .default,
                        isPassword: false
                    )
                    
                    // Email field
                    CustomTextField(
                        text: $email,
                        placeholder: "Email",
                        keyboardType: .emailAddress,
                        isPassword: false
                    )
                    
                    // Password field
                    CustomTextField(
                        text: $password,
                        placeholder: "Password",
                        keyboardType: .default,
                        isPassword: true
                    )
                    
                    // Register button
                    PrimaryButton(
                        text: "Register",
                        isLoading: authViewModel.isLoading,
                        action: {
                            authViewModel.register(email: email, password: password, displayName: displayName)
                        }
                    )
                    .padding(.top, 24)
                    
                    // Divider with text
                    DividerWithText(text: "Or continue with")
                        .padding(.vertical, 24)
                    
                    // Social registration options
                    HStack(spacing: 24) {
                        SocialButton(
                            iconName: "g.circle.fill",
                            action: {
                                // Google sign up
                            }
                        )
                        
                        SocialButton(
                            iconName: "apple.logo",
                            action: {
                                // Apple sign up
                            }
                        )
                    }
                    
                    // Login option
                    HStack(spacing: 4) {
                        Text("Already have an account?")
                            .foregroundColor(colors.textSecondary)
                            .font(typography.bodyMedium)
                        
                        Button("Sign in") {
                            presentationMode.wrappedValue.dismiss()
                        }
                        .foregroundColor(colors.interactive)
                        .font(typography.bodyMedium)
                    }
                    .padding(.top, 24)
                    
                    Spacer()
                }
                .padding(.horizontal, 24)
            }
        }
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
        .navigationBarTitle("Register", displayMode: .inline)
        .navigationBarItems(leading: Button(action: {
            presentationMode.wrappedValue.dismiss()
        }) {
            Image(systemName: "chevron.left")
                .foregroundColor(colors.interactive)
        })
    }
}

//struct RegisterView_Previews: PreviewProvider {
//    static var previews: some View {
//        Group {
//            // Preview COSMIC theme
//            let cosmicThemeManager = EmotionThemeManager()
//            cosmicThemeManager.currentTheme = .COSMIC
//            
//            RegisterView(authViewModel: AuthViewModelWrapper())
//                .environmentObject(cosmicThemeManager)
//                .emotionTheme(cosmicThemeManager)
//                .previewDisplayName("COSMIC Theme")
//            
//            // Preview SERENE theme
//            let sereneThemeManager = EmotionThemeManager()
//            sereneThemeManager.currentTheme = .SERENE
//            
//            RegisterView(authViewModel: AuthViewModelWrapper())
//                .environmentObject(sereneThemeManager)
//                .emotionTheme(sereneThemeManager)
//                .previewDisplayName("SERENE Theme")
//        }
//    }
//}
