package godau.fynn.usagedirectplus.browser

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import java.util.ArrayDeque

object BrowserWindowSnapshotFactory {
    private const val MAX_NODES_PER_WINDOW = 400

    fun create(
        applicationId: String,
        event: AccessibilityEvent?,
        activeRoot: AccessibilityNodeInfo?,
        windows: List<AccessibilityWindowInfo>
    ): BrowserObservationContext {
        val snapshots = mutableListOf<BrowserWindowSnapshot>()
        var activeWindowId: Int? = null

        for (window in windows) {
            val root = window.root ?: continue
            val snapshot = try {
                snapshotWindow(root, window)
            } finally {
                root.recycle()
            }

            if (snapshot.packageName == applicationId || snapshot.packageName == null) {
                snapshots += snapshot
                if (window.isActive) {
                    activeWindowId = snapshot.id
                }
            }
        }

        if (snapshots.isEmpty() && activeRoot != null) {
            val snapshot = try {
                snapshotWindow(activeRoot, null, fallbackId = -1, forceActive = true)
            } finally {
                activeRoot.recycle()
            }
            snapshots += snapshot
            activeWindowId = snapshot.id
        }

        return BrowserObservationContext(
            applicationId = applicationId,
            eventTexts = event?.text?.mapNotNull { it?.toString() } ?: emptyList(),
            eventContentDescription = event?.contentDescription?.toString(),
            eventClassName = event?.className?.toString(),
            activeWindowId = activeWindowId,
            windows = snapshots
        )
    }

    private fun snapshotWindow(
        root: AccessibilityNodeInfo,
        window: AccessibilityWindowInfo?,
        fallbackId: Int? = null,
        forceActive: Boolean = false
    ): BrowserWindowSnapshot {
        val nodes = mutableListOf<BrowserNodeSnapshot>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty() && nodes.size < MAX_NODES_PER_WINDOW) {
            val current = queue.removeFirst()
            try {
                nodes += BrowserNodeSnapshot(
                    viewIdResourceName = current.viewIdResourceName,
                    text = current.text?.toString(),
                    contentDescription = current.contentDescription?.toString(),
                    paneTitle = current.paneTitle?.toString(),
                    className = current.className?.toString(),
                    isEditable = current.isEditable,
                    isVisibleToUser = current.isVisibleToUser
                )

                for (index in 0 until current.childCount) {
                    current.getChild(index)?.let(queue::addLast)
                }
            } finally {
                if (current !== root) {
                    current.recycle()
                }
            }
        }

        return BrowserWindowSnapshot(
            id = window?.id ?: fallbackId,
            title = window?.title?.toString(),
            packageName = root.packageName?.toString(),
            isActive = forceActive || window?.isActive == true,
            isFocused = window?.isFocused == true,
            nodes = nodes
        )
    }
}
