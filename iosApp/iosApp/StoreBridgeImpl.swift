import Shared
import StoreKit

/// Einmaliger Premium-Kauf über StoreKit 2.
/// StoreKit verifiziert jede Transaktion kryptografisch (VerificationResult);
/// nur verifizierte, nicht widerrufene Käufe schalten Premium frei.
final class StoreBridgeImpl: NSObject, IosStoreBridge {
    private var products: [String: Product] = [:]
    private var updatesTask: Task<Void, Never>?

    func fetchProduct(productId: String, onResult: @escaping (String?) -> Void) {
        Task { @MainActor in
            do {
                let result = try await Product.products(for: [productId])
                if let product = result.first {
                    products[productId] = product
                    onResult(product.displayPrice)
                } else {
                    onResult(nil)
                }
            } catch {
                print("StoreKit products: \(error.localizedDescription)")
                onResult(nil)
            }
        }
    }

    func isEntitled(productId: String, onResult: @escaping (KotlinBoolean) -> Void) {
        Task {
            var entitled = false
            for await result in Transaction.currentEntitlements {
                if case .verified(let transaction) = result,
                   transaction.productID == productId,
                   transaction.revocationDate == nil {
                    entitled = true
                }
            }
            onResult(KotlinBoolean(bool: entitled))
        }
    }

    func purchase(productId: String, onResult: @escaping (String) -> Void) {
        Task { @MainActor in
            do {
                var product = products[productId]
                if product == nil {
                    product = try await Product.products(for: [productId]).first
                }
                guard let product else { onResult("failed"); return }
                switch try await product.purchase() {
                case .success(let verification):
                    switch verification {
                    case .verified(let transaction):
                        await transaction.finish()
                        onResult("success")
                    case .unverified:
                        onResult("unverified")
                    }
                case .pending:
                    onResult("pending")
                case .userCancelled:
                    onResult("cancelled")
                @unknown default:
                    onResult("failed")
                }
            } catch {
                print("StoreKit purchase: \(error.localizedDescription)")
                onResult("failed")
            }
        }
    }

    func restore(onDone: @escaping (KotlinBoolean) -> Void) {
        Task {
            do {
                try await AppStore.sync()
                onDone(KotlinBoolean(bool: true))
            } catch {
                onDone(KotlinBoolean(bool: false))
            }
        }
    }

    func observeUpdates(onChanged: @escaping () -> Void) {
        updatesTask?.cancel()
        updatesTask = Task.detached {
            for await update in Transaction.updates {
                if case .verified(let transaction) = update {
                    await transaction.finish()
                }
                onChanged()
            }
        }
    }
}
