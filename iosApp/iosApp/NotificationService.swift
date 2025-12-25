import UserNotifications
import composeApp // Your KMP Library

class NotificationService: UNNotificationServiceExtension {
    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        let content = request.content.body

        // 1. Call your KMP Regex Parser
        let parser = RegexIngestor()
        if let transaction = parser.parseSms(content: content) {
            // 2. Save directly to the Encrypted Vault
            VaultDatabase.shared.insertTransaction(transaction)
        }

        // 3. Let the notification show normally
        contentHandler(request.content)
    }
}