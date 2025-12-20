package com.example.pharmacystore.ui.pharmacies

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.pharmacystore.common.toMessage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.math.round
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ekran odpowiadajacy za wyswietlenie listy aptek
// bedzie mozliwosc odfiltorowania po odleglosci od miejsca zamieszkania


enum class SortOption { Distance, Name }

enum class SortOrder { ASC, DESC }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaciesScreen(
    viewModel: PharmacyViewModel,
    navigateToPharmacyScreen: () -> Unit,
    address: String,
) {
    val context = LocalContext.current

    val radius by viewModel.radius.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val sortedBy by viewModel.sortedBy.collectAsStateWithLifecycle()
    val isOpen by viewModel.isOpen.collectAsStateWithLifecycle()

    val hasUserSearched by viewModel.hasUserSearched.collectAsStateWithLifecycle()

    // ----------------------- Przewijalnosc listy -------------------------------------------------
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topBarState)
    val listState = rememberLazyListState()
    // ---------------------------------------------------------------------------------------------

    val items = viewModel.pharmaciesPagingFlow.collectAsLazyPagingItems()

    if (hasUserSearched) {
        LaunchedEffect(sortedBy, sortOrder) {
            snapshotFlow { items.loadState.refresh }
                .filter { it is LoadState.NotLoading }
                .first()
            listState.scrollToItem(0)
        }

        LaunchedEffect(items.loadState) {
            if (items.loadState.refresh is LoadState.Error) {
                val error = (items.loadState.refresh as LoadState.Error).error
                Toast.makeText(
                    context,
                    error.toMessage(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // scroll listy
    LaunchedEffect(Unit) {
        listState.scrollToItem(
            index = viewModel.savedIndex,
            scrollOffset = viewModel.savedOffset
        )
    }

    LaunchedEffect(address) {
        println(address)
        viewModel.ensureLocationFromAddressIfNeeded(address)
    }

    PharmacyScreenContent(
        sortedBy = sortedBy,
        sortOrder = sortOrder,
        onSortedByChanged = { sortedBy -> viewModel.changeSortedBy(sortedBy) },

        radius = radius,

        isOpen = isOpen,

        hasUserSearched = hasUserSearched,

        onToggleOpenNow = { state ->
            viewModel.changeIsOpen(state)
        },

        onSortOrderChanged = { order -> viewModel.changeSortOrder(order) },

        onRadiusChanged = { rad -> viewModel.changeRadius(rad) },

        pharmacies = items,
        onSearchClick = {
            // najpierw
            viewModel.applyFilters()
            items.refresh()
        },
        scrollBehavior = scrollBehavior,
        listState = listState,
        navigateToPharmacyScreen = { id ->
            viewModel.getPharmacyDetails(id)
            navigateToPharmacyScreen()
        },
        saveListIndexAndOffset = { index, offset -> viewModel.saveIndex(index, offset) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyScreenContent(
    hasUserSearched: Boolean,
    radius: Int,
    isOpen: Boolean,
    navigateToPharmacyScreen: (Int) -> Unit,
    sortedBy: SortOption,
    sortOrder: SortOrder,
    onSortedByChanged: (SortOption) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit,
    onRadiusChanged: (Int) -> Unit,
    onSearchClick: (radius: Int) -> Unit,
    pharmacies: LazyPagingItems<PharmacyLight>?,
    onToggleOpenNow: (Boolean) -> Unit = {},

    // przewijalnosc listy & index
    scrollBehavior: TopAppBarScrollBehavior,
    listState: LazyListState,
    saveListIndexAndOffset: (Int, Int) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var sortingTypeExpanded by remember { mutableStateOf(false) }

    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            val fraction = scrollBehavior.state.overlappedFraction
            val base = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)
            val container = base.copy(alpha = 1f - fraction)

            TopAppBar(
                colors = topAppBarColors(
                    containerColor = container,
                    scrolledContainerColor = container
                ),
                title = {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    ) {
                        Text("Pharmacies nearby")
                        Spacer(Modifier.height(8.dp))

                        FilterBar(
                            rangeKm = radius,
                            sortOrder = sortOrder,
                            onRangeChange = {
                                onRadiusChanged(it)
                            },
                            sort = sortedBy,
                            sortMenuExpanded = sortMenuExpanded,
                            onClickSort = { sortMenuExpanded = true },
                            onDismissSortMenu = { sortMenuExpanded = false },
                            onPickSort = {
                                onSortedByChanged(it)
                                sortMenuExpanded = false
                            },
                            sortingTypeExpanded = sortingTypeExpanded,
                            onClickSortTypeButton = { sortingTypeExpanded = true },
                            onDismissSortType = { sortingTypeExpanded = false },

                            isOpen = isOpen,
                            onToggleOpenNow = {
                                onToggleOpenNow(it)
                            },
                            onSearchClick = {
                                onSearchClick(radius)
                            },
                            onSortChanged = { order ->
                                onSortOrderChanged(order)
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { inner ->
        if (pharmacies != null && hasUserSearched) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pharmacies.loadState.refresh is LoadState.Loading) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(
                        count = pharmacies.itemCount,
                        key = pharmacies.itemKey { it.identyfikator_apteki },
                        contentType = pharmacies.itemContentType { "MyPagingItems" },
                    ) { index ->
                        val item = pharmacies[index]
                        if (item != null) {
                            PharmacyCard(
                                item = item,
                                onClick = {
                                    navigateToPharmacyScreen(item.identyfikator_apteki)
                                    saveListIndexAndOffset(
                                        listState.firstVisibleItemIndex,
                                        listState.firstVisibleItemScrollOffset
                                    )
                                },
                                onNavigate = {
                                    mapIntent(
                                        item.lat,
                                        item.lon,
                                        "${item.ulica_znormalizowana} Apteka", ctx
                                    )
                                })
                        }
                    }

                    item {
                        if (pharmacies.loadState.append is LoadState.Loading) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }


            }
        }
    }
}


@Composable
private fun FilterBar(
    rangeKm: Int,
    onRangeChange: (Int) -> Unit,
    sort: SortOption,
    sortOrder: SortOrder,
    onClickSortTypeButton: () -> Unit,
    onClickSort: () -> Unit,
    onSearchClick: () -> Unit,
    sortMenuExpanded: Boolean,
    sortingTypeExpanded: Boolean,
    onDismissSortType: () -> Unit,
    onDismissSortMenu: () -> Unit,
    onPickSort: (SortOption) -> Unit,
    isOpen: Boolean,
    onToggleOpenNow: (Boolean) -> Unit,
    onSortChanged: (SortOrder) -> Unit,
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Range: $rangeKm km", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))

                Box {
                    AssistChip(
                        onClick = onClickSort,
                        label = { Text("Sort: ${sort.name}") }
                    )
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = onDismissSortMenu
                    ) {
                        SortOption.entries.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.name) },
                                onClick = { onPickSort(opt) }
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { onClickSortTypeButton() }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "More filters")
                    }
                    DropdownMenu(
                        expanded = sortingTypeExpanded,
                        onDismissRequest = onDismissSortType
                    ) {
                        SortOrder.entries.forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    when (opt) {
                                        SortOrder.ASC -> Text("Low to High")
                                        SortOrder.DESC -> Text("High to Low")
                                    }

                                },
                                onClick = {
                                    onSortChanged(opt)
                                    onDismissSortType()
                                }
                            )
                        }
                    }
                }
                Icon(
                    if (sortOrder == SortOrder.ASC) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                    contentDescription = ""
                )
            }

            Slider(
                value = rangeKm.toFloat(),
                onValueChange = { onRangeChange(it.toInt().coerceIn(0, 30)) },
                valueRange = 1f..30f,
                steps = 28 // co 1 km
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                FilterChip(
                    selected = isOpen,
                    onClick = { onToggleOpenNow(!isOpen) },
                    label = { Text("Open now") }
                )
            }
            Button(
                onClick = { onSearchClick() },
                Modifier
                    .fillMaxWidth()
            ) { Text("Search") }

        }

    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PharmacyCard(
    item: PharmacyLight,
    onClick: () -> Unit = {},
    onNavigate: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.nazwa_apteki ?: "Apteka",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )


                val km = item.distance
                Text(
                    text = if (km >= 1) String.format("%.1f km", km) else String.format(
                        "%.1f m",
                        round(km * 1000)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    modifier = Modifier.widthIn(min = 56.dp)
                )
            }

            if (item.ulica_znormalizowana.isNotBlank()) {
                Spacer(Modifier.height(4.dp))

                val streetType =
                    if (item.typ_ulicy == null) "ul. " else StringBuilder("${item.typ_ulicy} ")
                Text(
                    text = "$streetType${item.ulica_znormalizowana}, ${item.miejscowosc}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.rodzaj_apteki,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = onNavigate) {
                    Icon(Icons.Outlined.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Navigate")
                }
            }
        }
    }
}

fun mapIntent(lat: Double?, lon: Double?, name: String?, ctx: Context) {
    val gmmIntentUri =
        "geo:$lat,$lon?q=${name ?: "Apteka"}".toUri()
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    ctx.startActivity(mapIntent)
}
