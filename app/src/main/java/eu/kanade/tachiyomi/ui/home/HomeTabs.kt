import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NewReleases
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import kotlinx.collections.immutable.persistentMapOf
import tachiyomi.i18n.MR

object HomeTabs {
    val Default = listOf(
        LibraryTab,
        UpdatesTab,
        HistoryTab,
        BrowseTab,
        MoreTab,
    )

    val Map = persistentMapOf(
        "library" to TabItem(LibraryTab, MR.strings.label_library, Icons.Outlined.CollectionsBookmark),
        "updates" to TabItem(UpdatesTab, MR.strings.label_recent_updates, Icons.Outlined.NewReleases),
        "history" to TabItem(HistoryTab, MR.strings.label_recent_manga, Icons.Outlined.History),
        "browse" to TabItem(BrowseTab, MR.strings.browse, Icons.Outlined.Explore),
        "more" to TabItem(MoreTab, MR.strings.label_more, Icons.Outlined.MoreHoriz),
    )
}

data class TabItem(
    val tab: Tab,
    val nameRes: dev.icerock.moko.resources.StringResource,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
