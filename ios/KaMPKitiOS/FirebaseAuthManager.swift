//
//  FirebaseAuthManager.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 21/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/FirebaseAuthManager.swift
import Foundation
import FirebaseCore       // For core Firebase functionality
import FirebaseAuth       // For Auth.auth() and authentication functions
import GoogleSignIn       // For GoogleSignIn
import AuthenticationServices  // For Apple authentication
import shared

class FirebaseAuthManager: NSObject {
    static let shared = FirebaseAuthManager()
    
    override init() {
        super.init()
        // Listen for auth state changes
        Auth.auth().addStateDidChangeListener { (auth, user) in
            // You can notify your Kotlin code about auth changes here if needed
        }
    }
    
    func loginWithEmail(email: String, password: String, completion: @escaping (String?, String?, String?, Bool, Error?) -> Void) {
        Auth.auth().signIn(withEmail: email, password: password) { (authResult, error) in
            if let error = error {
                completion(nil, nil, nil, false, error)
                return
            }
            
            guard let firebaseUser = authResult?.user else {
                completion(nil, nil, nil, false, NSError(domain: "FirebaseAuthManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Unknown error"]))
                return
            }
            
            completion(
                firebaseUser.uid,
                firebaseUser.email,
                firebaseUser.displayName,
                firebaseUser.isEmailVerified,
                nil
            )
        }
    }
    
    func loginWithGoogle(idToken: String, completion: @escaping (String?, String?, String?, Bool, Error?) -> Void) {
        let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: "")
        
        Auth.auth().signIn(with: credential) { (authResult, error) in
            if let error = error {
                completion(nil, nil, nil, false, error)
                return
            }
            
            guard let firebaseUser = authResult?.user else {
                completion(nil, nil, nil, false, NSError(domain: "FirebaseAuthManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Unknown error"]))
                return
            }
            
            completion(
                firebaseUser.uid,
                firebaseUser.email,
                firebaseUser.displayName,
                firebaseUser.isEmailVerified,
                nil
            )
        }
    }
    
    func loginWithApple(idToken: String, nonce: String?, completion: @escaping (String?, String?, String?, Bool, Error?) -> Void) {
        // Check if nonce is available
        guard let nonce = nonce else {
            // If nonce is not provided, complete with an error
            completion(nil, nil, nil, false, NSError(
                domain: "FirebaseAuthManager",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "Apple Sign In requires a nonce for security"]))
            return
        }
        
        // Now nonce is unwrapped and can be safely used
        let credential = OAuthProvider.credential(
            withProviderID: "apple.com",
            idToken: idToken,
            rawNonce: nonce
        )
        
        Auth.auth().signIn(with: credential) { (authResult, error) in
            if let error = error {
                completion(nil, nil, nil, false, error)
                return
            }
            
            guard let firebaseUser = authResult?.user else {
                completion(nil, nil, nil, false, NSError(domain: "FirebaseAuthManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Unknown error"]))
                return
            }
            
            completion(
                firebaseUser.uid,
                firebaseUser.email,
                firebaseUser.displayName,
                firebaseUser.isEmailVerified,
                nil
            )
        }
    }
    func register(email: String, password: String, displayName: String?, completion: @escaping (String?, String?, String?, Bool, Error?) -> Void) {
        Auth.auth().createUser(withEmail: email, password: password) { (authResult, error) in
            if let error = error {
                completion(nil, nil, nil, false, error)
                return
            }
            
            guard let firebaseUser = authResult?.user else {
                completion(nil, nil, nil, false, NSError(domain: "FirebaseAuthManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Unknown error"]))
                return
            }
            
            // Update display name if provided
            if let displayName = displayName, !displayName.isEmpty {
                let changeRequest = firebaseUser.createProfileChangeRequest()
                changeRequest.displayName = displayName
                
                changeRequest.commitChanges { (error) in
                    completion(
                        firebaseUser.uid,
                        firebaseUser.email,
                        displayName,
                        firebaseUser.isEmailVerified,
                        error
                    )
                }
            } else {
                completion(
                    firebaseUser.uid,
                    firebaseUser.email,
                    firebaseUser.displayName,
                    firebaseUser.isEmailVerified,
                    nil
                )
            }
        }
    }
    
    func signOut() -> Bool {
        do {
            try Auth.auth().signOut()
            return true
        } catch {
            print("Error signing out: \(error.localizedDescription)")
            return false
        }
    }
    
    func getCurrentUser() -> (String, String, String?, Bool)? {
        guard let firebaseUser = Auth.auth().currentUser else {
            return nil
        }
        
        return (
            firebaseUser.uid,
            firebaseUser.email ?? "",
            firebaseUser.displayName,
            firebaseUser.isEmailVerified
        )
    }
    
    func sendPasswordResetEmail(email: String, completion: @escaping (Bool) -> Void) {
        Auth.auth().sendPasswordReset(withEmail: email) { error in
            completion(error == nil)
        }
    }
}
