import UIKit
import Shared

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(
      _ application: UIApplication,
      didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {

        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { _, _ in }

        let repo = ReminderRepository()
        let scheduler = ReminderScheduler()
        let manager = ReminderManager(repo: repo, scheduler: scheduler)

        let controller = ComposeAppKt.ComposeViewController(manager: manager)

        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = controller
        window?.makeKeyAndVisible()

        return true
    }
}
