//
//  ContentView.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 21/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/ContentView.swift
import SwiftUI
import shared
import Firebase

struct ContentView: View {
    @StateObject private var authViewModel = AuthViewModelWrapper()
    
    var body: some View {
        ZStack {
            if case .authenticated = authViewModel.authState {
                // Main app content - show your app's main screen here
                MainAppView(authViewModel: authViewModel)
            } else {
                // Login view
                LoginView(authViewModel: authViewModel)
            }
        }
    }
}

struct MainAppView: View {
    @ObservedObject var authViewModel: AuthViewModelWrapper
    
    var body: some View {
        VStack {
            Text("You are logged in!")
                .font(.title)
                .padding()
            
            Button("Sign Out") {
                authViewModel.logout()
            }
            .padding()
            .background(Color.red)
            .foregroundColor(.white)
            .cornerRadius(8)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
