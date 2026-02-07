package eu.kanade.presentation.more.settings.screen

import HomeTabs
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import tachiyomi.presentation.core.util.plus
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object SettingsBottomNavScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val uiPreferences = remember { Injekt.get<UiPreferences>() }
        val scope = rememberCoroutineScope()

        val defaultOrder = remember {
            HomeTabs.Default.map { tab ->
                HomeTabs.Map.entries.first { it.value.tab == tab }.key
            }
        }

        val storedTabsPref = remember { uiPreferences.bottomBarTabs(defaultOrder) }
        val storedTabs by storedTabsPref.collectAsState()

        val currentKeys = remember(storedTabs) {
            (storedTabs + (defaultOrder - storedTabs.toSet())).toMutableStateList()
        }

        val enabledState = remember(storedTabs) {
            mutableStateMapOf<String, Boolean>().apply {
                defaultOrder.forEach { key ->
                    put(key, key in storedTabs || key == "more")
                }
            }
        }

        fun save() {
            val newEnabledTabs = currentKeys.filter { enabledState[it] == true }

            scope.launch {
                storedTabsPref.set(newEnabledTabs)
            }
        }

        val lazyListState = rememberLazyListState()
        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
            // Indices are offset by 1 due to the header item
            val fromIndex = from.index - 1
            val toIndex = to.index - 1
            
            if (fromIndex >= 0 && toIndex >= 0 && fromIndex < currentKeys.size && toIndex < currentKeys.size) {
                 val item = currentKeys.removeAt(fromIndex)
                 currentKeys.add(toIndex, item)
                 save()
            }
        }

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.pref_bottom_nav_tabs),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                contentPadding = paddingValues + topSmallPaddingValues + PaddingValues(horizontal = MaterialTheme.padding.medium),
            ) {
                item {
                    val startScreenPref = uiPreferences.startScreen()
                    var showDialog by remember { mutableStateOf(false) }
                    val currentStartScreen by startScreenPref.collectAsState()
                    val enabledTabs = currentKeys.filter { enabledState[it] == true }

                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text(text = stringResource(MR.strings.pref_bottom_nav_start_screen)) },
                            text = {
                                Column {
                                    enabledTabs.forEach { key ->
                                        val tab = HomeTabs.Map[key]!!
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    scope.launch { startScreenPref.set(key) }
                                                    showDialog = false
                                                }
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            RadioButton(
                                                selected = (key == currentStartScreen),
                                                onClick = null,
                                            )
                                            Text(
                                                text = stringResource(tab.nameRes),
                                                modifier = Modifier.padding(start = 8.dp),
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text(stringResource(MR.strings.action_cancel))
                                }
                            },
                        )
                    }

                    TextPreferenceWidget(
                        title = stringResource(MR.strings.pref_bottom_nav_start_screen),
                        subtitle = HomeTabs.Map[currentStartScreen]?.let { stringResource(it.nameRes) },
                        onPreferenceClick = { showDialog = true },
                    )
                }

                items(items = currentKeys, key = { it }) { key ->
                    ReorderableItem(reorderableState, key = key) { _ ->
                        val tab = HomeTabs.Map[key]!!
                        val enabled = enabledState[key] ?: false
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .padding(vertical = MaterialTheme.padding.small)
                                .padding(end = MaterialTheme.padding.medium),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DragHandle,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(MaterialTheme.padding.medium)
                                    .draggableHandle(),
                            )

                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.padding(end = MaterialTheme.padding.medium),
                            )

                            Text(
                                text = stringResource(tab.nameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )

                            Checkbox(
                                checked = enabled,
                                onCheckedChange = { isChecked ->
                                    enabledState[key] = isChecked
                                    save()
                                },
                                enabled = key != "more",
                            )
                        }
                    }
                }
            }
        }
    }
}
