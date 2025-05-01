////
////  LoginView.swift
////  KaMPKitiOS
////
////  Created by Krishna Poddar on 21/04/25.
////  Copyright © 2025 Touchlab. All rights reserved.
////
//
//
//// ios/KaMPKitiOS/LoginView.swift
//import SwiftUI
//import Firebase
//import GoogleSignIn
//import shared
//
//struct LoginView: View {
//    @ObservedObject var authViewModel: AuthViewModelWrapper
//    @State private var email: String = ""
//    @State private var password: String = ""
//    @State private var isShowingAlert: Bool = false
//    @State private var alertMessage: String = ""
//    
//    var body: some View {
//        VStack(spacing: 20) {
//            // App logo or header image
//            Image(systemName: "lock.shield")
//                .resizable()
//                .aspectRatio(contentMode: .fit)
//                .frame(width: 100, height: 100)
//                .foregroundColor(.blue)
//                .padding(.bottom, 20)
//            
//            Text("Sign In")
//                .font(.largeTitle)
//                .fontWeight(.bold)
//                .padding(.bottom, 20)
//            
//            // Email field
//            TextField("Email", text: $email)
//                .keyboardType(.emailAddress)
//                .autocapitalization(.none)
//                .padding()
//                .background(Color(.systemGray6))
//                .cornerRadius(8)
//                .padding(.horizontal)
//            
//            // Password field
//            SecureField("Password", text: $password)
//                .padding()
//                .background(Color(.systemGray6))
//                .cornerRadius(8)
//                .padding(.horizontal)
//            
//            // Login button
//            Button(action: {
//                authViewModel.loginWithEmail(email: email, password: password)
//            }) {
//                if authViewModel.isLoading {
//                    ProgressView()
//                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
//                        .frame(maxWidth: .infinity)
//                        .padding()
//                        .background(Color.blue)
//                        .foregroundColor(.white)
//                        .cornerRadius(8)
//                        .padding(.horizontal)
//                } else {
//                    Text("Sign In")
//                        .fontWeight(.bold)
//                        .frame(maxWidth: .infinity)
//                        .padding()
//                        .background(Color.blue)
//                        .foregroundColor(.white)
//                        .cornerRadius(8)
//                        .padding(.horizontal)
//                }
//            }
//            
//            Divider()
//                .padding(.vertical)
//            
//            // Social sign-in options
//            HStack(spacing: 20) {
//                Button(action: {
//                    // Google sign-in
//                    signInWithGoogle()
//                }) {
//                    HStack {
//                        Image(systemName: "g.circle.fill")
//                            .resizable()
//                            .frame(width: 24, height: 24)
//                        Text("Sign in with Google")
//                    }
//                    .padding()
//                    .background(Color(.systemGray6))
//                    .cornerRadius(8)
//                }
//                
//                Button(action: {
//                    // Apple sign-in
//                    signInWithApple()
//                }) {
//                    HStack {
//                        Image(systemName: "apple.logo")
//                            .resizable()
//                            .aspectRatio(contentMode: .fit)
//                            .frame(width: 24, height: 24)
//                        Text("Sign in with Apple")
//                    }
//                    .padding()
//                    .background(Color(.systemGray6))
//                    .cornerRadius(8)
//                }
//            }
//            .padding(.horizontal)
//            
//            Spacer()
//            
//            // Register option
//            HStack {
//                Text("Don't have an account?")
//                    .foregroundColor(.gray)
//                Button("Register") {
//                    // Navigate to registration
//                }
//                .foregroundColor(.blue)
//            }
//            .padding(.bottom, 20)
//        }
//        .padding()
//        .alert(isPresented: $isShowingAlert) {
//            Alert(
//                title: Text("Error"),
//                message: Text(alertMessage),
//                dismissButton: .default(Text("OK"))
//            )
//        }
//        .onChange(of: authViewModel.authState) { state in
//            if case let .error(message) = state {
//                isShowingAlert = true
//                alertMessage = message
//            }
//        }
//    }
//    
//    func signInWithGoogle() {
//        guard let clientID = FirebaseApp.app()?.options.clientID else { return }
//        
//        let config = GIDConfiguration(clientID: clientID)
//        GIDSignIn.sharedInstance.configuration = config
//        
//        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
//              let rootViewController = windowScene.windows.first?.rootViewController else {
//            return
//        }
//        
//        GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
//            if let error = error {
//                isShowingAlert = true
//                alertMessage = error.localizedDescription
//                return
//            }
//            
//            guard let user = result?.user,
//                  let idToken = user.idToken?.tokenString else {
//                isShowingAlert = true
//                alertMessage = "Failed to get ID token"
//                return
//            }
//            
//            authViewModel.loginWithGoogle(idToken: idToken)
//        }
//    }
//    
//    func signInWithApple() {
//        // Implement Apple Sign In flow
//    }
//}


// ios/KaMPKitiOS/LoginView.swift
//import SwiftUI
//import Firebase
//import GoogleSignIn
//import shared
//
//struct LoginView: View {
//    @ObservedObject var authViewModel: AuthViewModelWrapper
//    @State private var email: String = ""
//    @State private var password: String = ""
//    @State private var isShowingAlert: Bool = false
//    @State private var alertMessage: String = ""
//    
//    var body: some View {
//        ZStack {
//            Color(UIColor.systemBackground)
//                .ignoresSafeArea()
//            
//            VStack(spacing: 0) {
//                // Progress indicator
//                ProgressIndicator(currentStep: 1, totalSteps: 3)
//                    .padding(.top, 24)
//                
//                // Logo/Illustration placeholder
//                PlaceholderImage()
//                    .padding(.top, 24)
//                    .padding(.bottom, 24)
//                
//                // Login form
//                VStack(spacing: 16) {
//                    // Email field
//                    CustomTextField(
//                        text: $email,
//                        placeholder: "Email",
//                        keyboardType: .emailAddress,
//                        isPassword: false
//                    )
//                    
//                    // Password field
//                    CustomTextField(
//                        text: $password,
//                        placeholder: "Password",
//                        keyboardType: .default,
//                        isPassword: true
//                    )
//                    
//                    // Password recovery
//                    HStack {
//                        Spacer()
//                        Button("Forgot Password") {
//                            // Handle forgot password
//                        }
//                        .foregroundColor(.secondary)
//                        .font(.subheadline)
//                    }
//                    .padding(.top, 8)
//                    
//                    // Sign in button
//                    PrimaryButton(
//                        text: "Sign in",
//                        isLoading: authViewModel.isLoading,
//                        action: {
//                            authViewModel.loginWithEmail(email: email, password: password)
//                        }
//                    )
//                    .padding(.top, 24)
//                    
//                    // Divider with text
//                    DividerWithText(text: "Or continue with")
//                        .padding(.vertical, 24)
//                    
//                    // Social login options
//                    HStack(spacing: 24) {
//                        SocialButton(
//                            iconName: "g.circle.fill",
//                            action: signInWithGoogle
//                        )
//                        
//                        SocialButton(
//                            iconName: "apple.logo",
//                            action: signInWithApple
//                        )
//                    }
//                    
//                    // Register option
//                    HStack(spacing: 4) {
//                        Text("Not a member?")
//                            .foregroundColor(.secondary)
//                        Button("Register now") {
//                            // Navigate to registration
//                        }
//                        .foregroundColor(.blue)
//                    }
//                    .padding(.top, 24)
//                    
//                    Spacer()
//                }
//                .padding(.horizontal, 24)
//            }
//        }
//        .alert(isPresented: $isShowingAlert) {
//            Alert(
//                title: Text("Error"),
//                message: Text(alertMessage),
//                dismissButton: .default(Text("OK"))
//            )
//        }
//        .onChange(of: authViewModel.authState) { state in
//            if case let .error(message) = state {
//                isShowingAlert = true
//                alertMessage = message
//            }
//        }
//    }
//    
//    func signInWithGoogle() {
//        guard let clientID = FirebaseApp.app()?.options.clientID else { return }
//        
//        let config = GIDConfiguration(clientID: clientID)
//        GIDSignIn.sharedInstance.configuration = config
//        
//        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
//              let rootViewController = windowScene.windows.first?.rootViewController else {
//            return
//        }
//        
//        GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
//            if let error = error {
//                isShowingAlert = true
//                alertMessage = error.localizedDescription
//                return
//            }
//            
//            guard let user = result?.user,
//                  let idToken = user.idToken?.tokenString else {
//                isShowingAlert = true
//                alertMessage = "Failed to get ID token"
//                return
//            }
//            
//            authViewModel.loginWithGoogle(idToken: idToken)
//        }
//    }
//    
//    func signInWithApple() {
//        // Implement Apple Sign In flow
//    }
//}
//
//// MARK: - Supporting Components
//
//struct ProgressIndicator: View {
//    let currentStep: Int
//    let totalSteps: Int
//    
//    var body: some View {
//        HStack(spacing: 8) {
//            ForEach(1...totalSteps, id: \.self) { step in
//                Rectangle()
//                    .foregroundColor(step <= currentStep ? .blue : Color.secondary.opacity(0.3))
//                    .frame(height: 4)
//                    .cornerRadius(2)
//            }
//        }
//        .padding(.horizontal, 24)
//    }
//}
//
//struct PlaceholderImage: View {
//    var body: some View {
//        RoundedRectangle(cornerRadius: 24)
//            .fill(Color.secondary.opacity(0.1))
//            .frame(height: 200)
//            .overlay(
//                Image(systemName: "heart.fill")
//                    .resizable()
//                    .aspectRatio(contentMode: .fit)
//                    .frame(width: 80, height: 80)
//                    .foregroundColor(.blue)
//            )
//    }
//}
//
//struct CustomTextField: View {
//    @Binding var text: String
//    var placeholder: String
//    var keyboardType: UIKeyboardType
//    var isPassword: Bool
//    
//    @State private var isPasswordVisible = false
//    
//    var body: some View {
//        VStack(alignment: .leading) {
//            if isPassword {
//                HStack {
//                    if isPasswordVisible {
//                        TextField(placeholder, text: $text)
//                            .keyboardType(keyboardType)
//                            .autocapitalization(.none)
//                            .disableAutocorrection(true)
//                    } else {
//                        SecureField(placeholder, text: $text)
//                            .keyboardType(keyboardType)
//                            .autocapitalization(.none)
//                            .disableAutocorrection(true)
//                    }
//                    
//                    Button(action: {
//                        isPasswordVisible.toggle()
//                    }) {
//                        Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
//                            .foregroundColor(.secondary)
//                    }
//                }
//            } else {
//                TextField(placeholder, text: $text)
//                    .keyboardType(keyboardType)
//                    .autocapitalization(.none)
//                    .disableAutocorrection(true)
//            }
//        }
//        .padding()
//        .background(Color.secondary.opacity(0.1))
//        .cornerRadius(16)
//    }
//}
//
//struct PrimaryButton: View {
//    var text: String
//    var isLoading: Bool
//    var action: () -> Void
//    
//    var body: some View {
//        Button(action: action) {
//            ZStack {
//                Rectangle()
//                    .foregroundColor(.blue)
//                    .cornerRadius(8)
//                    .frame(height: 44)
//                
//                if isLoading {
//                    ProgressView()
//                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
//                } else {
//                    Text(text)
//                        .fontWeight(.bold)
//                        .foregroundColor(.white)
//                }
//            }
//        }
//        .disabled(isLoading)
//    }
//}
//
//struct DividerWithText: View {
//    var text: String
//    
//    var body: some View {
//        HStack {
//            Line()
//            Text(text)
//                .foregroundColor(.secondary)
//                .padding(.horizontal, 16)
//            Line()
//        }
//    }
//    
//    struct Line: View {
//        var body: some View {
//            Rectangle()
//                .fill(Color.secondary.opacity(0.3))
//                .frame(height: 1)
//        }
//    }
//}
//
//struct SocialButton: View {
//    var iconName: String
//    var action: () -> Void
//    
//    var body: some View {
//        Button(action: action) {
//            Circle()
//                .fill(Color.secondary.opacity(0.1))
//                .frame(width: 48, height: 48)
//                .overlay(
//                    Image(systemName: iconName)
//                        .resizable()
//                        .aspectRatio(contentMode: .fit)
//                        .frame(width: 24, height: 24)
//                        .foregroundColor(.primary)
//                )
//        }
//    }
//}

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
    
    @EnvironmentObject var themeManager: EmotionThemeManager
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        ZStack {
            colors.background.edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 0) {
                // Progress indicator
                ProgressIndicator(currentStep: 1, totalSteps: 3)
                    .padding(.top, 24)
                
                // Logo/Illustration placeholder
                PlaceholderImage()
                    .padding(.top, 24)
                    .padding(.bottom, 24)
                
                // Login form
                VStack(spacing: 16) {
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
                    
                    // Password recovery
                    HStack {
                        Spacer()
                        Button("Forgot Password") {
                            // Handle forgot password
                        }
                        .foregroundColor(colors.textSecondary)
                        .font(typography.bodyMedium)
                    }
                    .padding(.top, 8)
                    
                    // Sign in button
                    PrimaryButton(
                        text: "Sign in",
                        isLoading: authViewModel.isLoading,
                        action: {
                            authViewModel.loginWithEmail(email: email, password: password)
                        }
                    )
                    .padding(.top, 24)
                    
                    // Divider with text
                    DividerWithText(text: "Or continue with")
                        .padding(.vertical, 24)
                    
                    // Social login options
                    HStack(spacing: 24) {
                        SocialButton(
                            iconName: "g.circle.fill",
                            action: signInWithGoogle
                        )
                        
                        SocialButton(
                            iconName: "apple.logo",
                            action: signInWithApple
                        )
                    }
                    
                    // Register option
                    HStack(spacing: 4) {
                        Text("Not a member?")
                            .foregroundColor(colors.textSecondary)
                            .font(typography.bodyMedium)
                        
                        Button("Register now") {
                            // Navigate to registration
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

// MARK: - Supporting Components

struct ProgressIndicator: View {
    let currentStep: Int
    let totalSteps: Int
    
    @Environment(\.emotionColors) private var colors
    
    var body: some View {
        HStack(spacing: 8) {
            ForEach(1...totalSteps, id: \.self) { step in
                Rectangle()
                    .foregroundColor(step <= currentStep ? colors.interactive : colors.textSecondary.opacity(0.3))
                    .frame(height: 4)
                    .cornerRadius(2)
            }
        }
        .padding(.horizontal, 24)
    }
}

struct PlaceholderImage: View {
    @Environment(\.emotionColors) private var colors
    
    var body: some View {
        RoundedRectangle(cornerRadius: 24)
            .fill(colors.backgroundSecondary)
            .frame(height: 200)
            .overlay(
                Image(systemName: "heart.fill")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 80, height: 80)
                    .foregroundColor(colors.interactive)
            )
    }
}

struct CustomTextField: View {
    @Binding var text: String
    var placeholder: String
    var keyboardType: UIKeyboardType
    var isPassword: Bool
    
    @State private var isPasswordVisible = false
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        VStack(alignment: .leading) {
            if isPassword {
                HStack {
                    if isPasswordVisible {
                        TextField(placeholder, text: $text)
                            .keyboardType(keyboardType)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .foregroundColor(colors.textPrimary)
                            .font(typography.bodyLarge)
                    } else {
                        SecureField(placeholder, text: $text)
                            .keyboardType(keyboardType)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .foregroundColor(colors.textPrimary)
                            .font(typography.bodyLarge)
                    }
                    
                    Button(action: {
                        isPasswordVisible.toggle()
                    }) {
                        Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                            .foregroundColor(colors.textSecondary)
                    }
                }
            } else {
                TextField(placeholder, text: $text)
                    .keyboardType(keyboardType)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                    .foregroundColor(colors.textPrimary)
                    .font(typography.bodyLarge)
            }
        }
        .padding()
        .background(colors.backgroundSecondary.opacity(0.5))
        .cornerRadius(16)
    }
}

struct PrimaryButton: View {
    var text: String
    var isLoading: Bool
    var action: () -> Void
    
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        Button(action: action) {
            ZStack {
                Rectangle()
                    .foregroundColor(colors.interactive)
                    .cornerRadius(8)
                    .frame(height: 44)
                
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text(text)
                        .font(typography.titleMedium)
                        .foregroundColor(Color.white)
                }
            }
        }
        .disabled(isLoading)
    }
}

struct DividerWithText: View {
    var text: String
    
    @Environment(\.emotionColors) private var colors
    @Environment(\.emotionTypography) private var typography
    
    var body: some View {
        HStack {
            Line()
            Text(text)
                .foregroundColor(colors.textSecondary)
                .font(typography.bodyMedium)
                .padding(.horizontal, 16)
            Line()
        }
    }
    
    struct Line: View {
        @Environment(\.emotionColors) private var colors
        
        var body: some View {
            Rectangle()
                .fill(colors.textSecondary.opacity(0.3))
                .frame(height: 1)
        }
    }
}

struct SocialButton: View {
    var iconName: String
    var action: () -> Void
    
    @Environment(\.emotionColors) private var colors
    
    var body: some View {
        Button(action: action) {
            Circle()
                .fill(colors.backgroundSecondary)
                .frame(width: 48, height: 48)
                .overlay(
                    Image(systemName: iconName)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 24, height: 24)
                        .foregroundColor(colors.textPrimary)
                )
        }
    }
}

//struct LoginView_Previews: PreviewProvider {
//    static var previews: some View {
//        Group {
//            // Preview COSMIC theme
//            let cosmicThemeManager = EmotionThemeManager()
//            cosmicThemeManager.currentTheme = .COSMIC
//            
//            LoginView(authViewModel: AuthViewModelWrapper())
//                .environmentObject(cosmicThemeManager)
//                .emotionTheme(cosmicThemeManager)
//                .previewDisplayName("COSMIC Theme")
//            
//            // Preview SERENE theme
//            let sereneThemeManager = EmotionThemeManager()
//            sereneThemeManager.currentTheme = .SERENE
//            
//            LoginView(authViewModel: AuthViewModelWrapper())
//                .environmentObject(sereneThemeManager)
//                .emotionTheme(sereneThemeManager)
//                .previewDisplayName("SERENE Theme")
//        }
//    }
//}
