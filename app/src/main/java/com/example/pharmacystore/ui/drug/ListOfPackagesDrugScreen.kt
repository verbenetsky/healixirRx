package com.example.pharmacystore.ui.drug

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pharmacystore.common.GoToTop
import com.example.pharmacystore.common.isScrollingUp
import com.example.pharmacystore.common.truncateTo2Decimals
import com.example.pharmacystore.data.remote.MedStock
import com.example.pharmacystore.data.remote.PharmacyShort
import com.example.pharmacystore.domain.model.CartItem
import com.example.pharmacystore.domain.model.Drug
import com.example.pharmacystore.domain.model.Packaging
import com.example.pharmacystore.domain.model.displayName
import com.example.pharmacystore.ui.pharmacies.PharmacyViewModel
import com.example.pharmacystore.ui.pharmacies.SortOption
import com.example.pharmacystore.ui.pharmacies.SortOrder
import com.example.pharmacystore.ui.shoppingcart.CartKey
import com.example.pharmacystore.ui.shoppingcart.ShoppingCartViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@SuppressLint("FrequentlyChangingValue")
@Composable
fun PickPackageDrugScreen(
    shoppingCartEvent: SharedFlow<ShoppingCartViewModel.ShoppingCartEvent>,
    drugViewModel: DrugViewModel,
    pharmacyViewModel: PharmacyViewModel,
    drug: Drug?,
    cartSize: Int,
    navigateToShoppingCartScreen: () -> Unit,
    navigateToPharmacyStock: () -> Unit,
    popBackStack: () -> Unit,
    onAddToCartClick: (med: CartItem, stockSize: Int) -> Unit,

    checkShoppingCartSize: (packageNdc: String, pharmacyId: Int, usersQt: Int, stockSize: Int) -> Unit,
    canIncrease: Map<CartKey, Boolean>,
    onDecreaseQuantity: (CartKey) -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        shoppingCartEvent.collect { event ->
            when (event) {
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastAdded -> {
                    Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()
                }
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastQtIsTooBig -> {
                    Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()
                }
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastCoundNotVerifyStock -> {
                    Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()
                }
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastThereIsNotEnoughItemsInStock -> {
                    Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    var pickedPackage by remember { mutableStateOf("") }
    var selectedPackageNdc by rememberSaveable { mutableStateOf<String?>(null) }

    // czysci liste wyszukanych lekow, bez tego to jesli wybierzemy jakis lek, klikniemy check availability nearby
    // potem wyjdziemy i wybierzemy inny lek to bedzie caly czas stara lista z poprzednimi lekami
    BackHandler {
        drugViewModel.changeState(DrugViewModel.MedStockState.Idle)
        pharmacyViewModel.pickPackage(null)
        popBackStack()
    }

    val listState = rememberLazyListState()

    if (drug == null) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val medStockState by drugViewModel.medStockState.collectAsStateWithLifecycle()
    val pickedPackageFromAvailablePackage by pharmacyViewModel.pickedPackage.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp
            ), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // info o aptece
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "General information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = drug.displayName, style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "NDC: ${drug.drugNDC}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Labeler: ${drug.labelerName}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (drug.activeIngredients.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Active ingredients:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            drug.activeIngredients.forEach { ing ->
                                Text(
                                    text = "• ${ing.name}: ${ing.strength}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // ---- Tytuł sekcji opakowań ----
            item {
                Column {
                    Text(
                        text = "Available packages", style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // ---- LISTA OPAKOWAŃ ----
            items(drug.packaging) { pkg ->
                val isSelected = pickedPackageFromAvailablePackage?.packageNdc == pkg.packageNdc

                SinglePackageRecord(pkg = pkg, isSelected = isSelected, onClick = {
                    //selectedPackageNdc = pkg.packageNdc
                    pharmacyViewModel.pickPackage(pkg)
                }, onCheckAvailabilityClick = {
                    selectedPackageNdc = pkg.packageNdc
                    drugViewModel.getMedStock(pkg.packageNdc, null, null)
                    pickedPackage = pkg.desc
                })
            }

            // ---- SEKCJA: lista aptek z VM ----
            item {
                MedStockSection(
                    state = medStockState,
                    pkg = pickedPackage,
                    onSortChange = { opt, order ->
                        if (selectedPackageNdc != null) {
                            drugViewModel.getMedStock(
                                packageNdc = selectedPackageNdc!!,
                                sortOption = opt,
                                sortOrder = order
                            )
                        }
                    },
                    onGoToPharmacyStockClick = { id -> drugViewModel.getPharmacyStock(id) },
                    navigateToPharmacyStock = { navigateToPharmacyStock() },
                    savePharmacyStock = { info -> pharmacyViewModel.savePharmacyInfo(info) },
                    addToCart = { qt, item -> // qt - ilosc wybrana przez usera
                        onAddToCartClick(
                            CartItem(
                                packageNdc = item.packageNdc,
                                drugNDC = drug.drugNDC,
                                name = drug.displayName,
                                labelerName = drug.labelerName,
                                brandName = drug.brandName,
                                quantity = qt,
                                drugPackageDesc = "",
                                pharmacyId = item.pharmacy.id,
                                pharmacyName = item.pharmacy.name,
                                address = item.pharmacy.address,
                                city = item.pharmacy.city,
                                distanceKms = item.pharmacy.distanceKms,
                                price = item.drugPrice
                            ),
                            item.quantity // item.qt - ilosc dostepnego towaru w jednej aptece
                        )
                    },
                    checkShoppingCartSize = { packageNdc, pharmacyId, usersQt, stockSize ->
                        checkShoppingCartSize(
                            packageNdc,
                            pharmacyId,
                            usersQt,
                            stockSize
                        )
                    },
                    canIncrease = canIncrease,
                    onDecreaseQuantity = { key ->
                        onDecreaseQuantity(key)
                    }
                )
            }
        }


        // ---- FAB z koszykiem ----
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { navigateToShoppingCartScreen() },
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart"
                )
            }

            if (cartSize > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(22.dp)
                        .background(
                            color = Color.Red, shape = CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cartSize <= 99) cartSize.toString() else "99",
                        color = White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp, lineHeight = 11.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = listState.firstVisibleItemIndex > 0 && listState.isScrollingUp(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GoToTop { scope.launch { listState.scrollToItem(0) } }
        }
    }
}

@Composable
fun SinglePackageRecord(
    pkg: Packaging, isSelected: Boolean, onClick: () -> Unit, onCheckAvailabilityClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {
                onClick()
                onCheckAvailabilityClick()
            }, border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }, colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected, onClick = onClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(pkg.desc, style = MaterialTheme.typography.bodySmall)
            }

        }
    }
}


@Composable
fun MedStockSection(
    canIncrease: Map<CartKey, Boolean>,
    state: DrugViewModel.MedStockState,
    onSortChange: (SortOption, SortOrder) -> Unit,
    onGoToPharmacyStockClick: (Int) -> Unit,
    navigateToPharmacyStock: () -> Unit,
    addToCart: (qt: Int, stock: MedStock) -> Unit, // qt to ilosc jaka wprowadzil user, stock - to
    savePharmacyStock: (PharmacyShort) -> Unit,
    pkg: String,
    onDecreaseQuantity: (CartKey) -> Unit,
    checkShoppingCartSize: (packageNdc: String, pharmacyId: Int, usersQt: Int, stockSize: Int) -> Unit
) {
    var sortOption by remember { mutableStateOf(SortOption.Name) }

    when (state) {
        is DrugViewModel.MedStockState.Idle -> {}

        is DrugViewModel.MedStockState.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp), strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Checking availability in nearby pharmacies...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        is DrugViewModel.MedStockState.Error -> {
            Text(
                text = "Error: ${state.message}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        is DrugViewModel.MedStockState.Success -> {
            val items = state.listOfMedStock
            if (items.isEmpty()) {
                Text(
                    text = "No pharmacies found with this package nearby.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Sort(
                    onSortChange = { option, order ->
                        onSortChange(option, order)
                    },
                    sortOption = sortOption,
                    onSortOptionChange = { opt ->
                        sortOption = opt
                    },
                )

                Text(
                    text = "Nearby pharmacies with \n$pkg package",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { stock ->
                        PharmacyStockCard(
                            canIncrease = canIncrease,
                            stock = stock,
                            onAddToCartClick = { qt ->
                                addToCart(qt, stock) // qt - ilosc wybrana przez usera
                            },
                            onGoToPharmacyStockClick = { id ->
                                onGoToPharmacyStockClick(id)
                                savePharmacyStock(stock.pharmacy)
                            },
                            navigateToPharmacyStock = { navigateToPharmacyStock() },
                            checkShoppingCartSize = { packageNdc, usersQt, stockSize ->
                                checkShoppingCartSize(
                                    packageNdc,
                                    stock.pharmacy.id,
                                    usersQt,
                                    stockSize
                                )
                            },
                            onDecreaseQuantity = { key ->
                                onDecreaseQuantity(key)
                            }
                        )
                    }
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun PharmacyStockCard(
    canIncrease: Map<CartKey, Boolean>,
    stock: MedStock,
    onAddToCartClick: (qt: Int) -> Unit,
    onGoToPharmacyStockClick: (Int) -> Unit,
    navigateToPharmacyStock: () -> Unit,
    onDecreaseQuantity: (CartKey) -> Unit,
    checkShoppingCartSize: (packageNdc: String, usersQt: Int, stockSize: Int) -> Unit,
) {
    var qty by remember { mutableIntStateOf(0) }
    val key = CartKey(stock.packageNdc, stock.pharmacy.id)
    val canIncreaseForThis = canIncrease[key] ?: true

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stock.pharmacy.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )

            Text(
                text = "${stock.pharmacy.address ?: ""}, ${stock.pharmacy.city}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 1
            )

            Text(
                text = "Distance: ${truncateTo2Decimals(stock.pharmacy.distanceKms)} km",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format("Price: %.2f", stock.drugPrice),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = { onAddToCartClick(qty) }, // qt - ilosc wybrana przez usera
                enabled = qty > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart, contentDescription = "Add to cart"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to cart")
            }

            Spacer(modifier = Modifier.height(8.dp))

            DecreaseIncreaseQt(
                value = qty,
                canIncrease = qty < stock.quantity && canIncreaseForThis,
                canDecrease = qty > 0,
                onIncreaseQuantity = {
                    // za kazdym razem jak user klika na plus to trzeba przekazac id apteki z ktorej user dodaje do koszyka, package ndc, qt jakie wpisal user oraz stock.size
                    // potem majac te dane sprawdzic czy ilosc w koszuku + ilosc w DecreaseIncreaseQt nie jest wieksza od stock.size
                    if (qty < stock.quantity) qty++

                    checkShoppingCartSize(stock.packageNdc, qty, stock.quantity)
                },
                onDecreaseQuantity = {
                    if (qty > 0) qty--
                    onDecreaseQuantity(key)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$qty out of ${stock.quantity}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))


            OutlinedButton(
                onClick = {
                    onGoToPharmacyStockClick(stock.pharmacy.id)
                    navigateToPharmacyStock()
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = "view pharmacy stock"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go to pharmacy stock")
            }
        }
    }
}

@Composable
fun DecreaseIncreaseQt(
    value: Int,
    canIncrease: Boolean,
    canDecrease: Boolean,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(1.dp),
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onDecreaseQuantity, enabled = canDecrease
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease quantity",
                    modifier = Modifier.padding(1.dp)
                )
            }

            Text(
                value.toString(), modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = onIncreaseQuantity, enabled = canIncrease
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase quantity",
                    modifier = Modifier.padding(1.dp)
                )
            }
        }
    }
}


@Composable
fun Sort(
    onSortChange: (SortOption, SortOrder) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit
) {

    var sortingOptionExpanded by remember { mutableStateOf(false) }
    var sortingOrderExpanded by remember { mutableStateOf(false) }

    var sortOrder by remember { mutableStateOf(SortOrder.ASC) }

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
    ) {

        Spacer(Modifier.weight(1f))

        Box {
            AssistChip(
                onClick = { sortingOptionExpanded = true },
                label = { Text("Sort: $sortOption") })
            DropdownMenu(
                expanded = sortingOptionExpanded,
                onDismissRequest = { sortingOptionExpanded = false }) {
                SortOption.entries.forEach { opt ->
                    DropdownMenuItem(text = { Text(opt.name) }, onClick = {
                        onSortOptionChange(opt)
                        sortingOptionExpanded = false
                        onSortChange(opt, sortOrder)
                    })
                }
            }
        }

        Box {
            IconButton(onClick = { sortingOrderExpanded = true }) {
                Icon(Icons.Outlined.FilterList, contentDescription = "More filters")
            }
            DropdownMenu(
                expanded = sortingOrderExpanded,
                onDismissRequest = { sortingOrderExpanded = false }) {
                SortOrder.entries.forEach { opt ->
                    DropdownMenuItem(text = {
                        when (opt) {
                            SortOrder.ASC -> Text("Low to High")
                            SortOrder.DESC -> Text("High to Low")
                        }

                    }, onClick = {
                        sortOrder = opt
                        sortingOrderExpanded = false
                        onSortChange(sortOption, opt)
                    })
                }
            }
        }
    }
}

