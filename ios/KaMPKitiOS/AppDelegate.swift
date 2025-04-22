// ios/KaMPKitiOS/AppDelegate.swift
import UIKit
import Firebase
import GoogleSignIn
import shared

class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Configure Firebase
        FirebaseApp.configure()
        
        // Initialize Koin
        let userDefaults = UserDefaults.standard
        let appInfo = IosAppInfo() // You might need to create this class
        doInitKoinIos(userDefaults: userDefaults, appInfo: appInfo, doOnStartup: {
            print("Koin started from iOS")
        })
        return true
    }
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }
}
