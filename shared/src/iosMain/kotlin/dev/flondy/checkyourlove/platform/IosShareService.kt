package dev.flondy.checkyourlove.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

/** Teilen über das iOS-Teilen-Menü (UIActivityViewController). */
internal class IosShareService : ShareService {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun shareImage(png: ByteArray, text: String, chooserTitle: String) {
        val data = png.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = png.size.toULong())
        }
        val image = UIImage.imageWithData(data) ?: return
        val controller = UIActivityViewController(activityItems = listOf(image, text), applicationActivities = null)
        val presenter = topViewController() ?: return
        // iPad: Popover braucht einen Ankerpunkt
        controller.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(controller, animated = true, completion = null)
    }

    private fun topViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
            .firstOrNull { it.isKeyWindow() }
        var top = window?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        return top
    }
}
