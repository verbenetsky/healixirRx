package com.example.pharmacystore.ui.pharmacies

import android.annotation.SuppressLint
import android.graphics.Color.alpha
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.common.truncateTo2Decimals
import com.example.pharmacystore.data.remote.MedStockDrug
import com.example.pharmacystore.data.remote.PharmacyShort
import com.example.pharmacystore.domain.model.displayName
import com.example.pharmacystore.ui.drug.DecreaseIncreaseQt
import com.example.pharmacystore.ui.drug.DrugViewModel
import com.example.pharmacystore.ui.drug.LabeledImage
import com.example.pharmacystore.ui.shoppingcart.ShoppingCartViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.max

@Composable
fun PharmacyStockScreen(
    event: SharedFlow<ShoppingCartViewModel.ShoppingCartEvent>,
    cartSize: Int,
    drugViewModel: DrugViewModel,
    pharmacyViewModel: PharmacyViewModel,
    navigateToPharmacyScreen: () -> Unit,
    navigateToShoppingCartScreen: () -> Unit,
    onAddToCartClick: (MedStockDrug, PharmacyShort, Int) -> Unit,
) {

    val context = LocalContext.current

    val state by drugViewModel.pharmacyStockState.collectAsStateWithLifecycle()

    val pharmacyInfo by pharmacyViewModel.pharmacyInfoPharmacyStock.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        event.collect { value ->
            when(value) {
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastAdded -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastCoundNotVerifyStock -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastQtIsTooBig -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }
                is ShoppingCartViewModel.ShoppingCartEvent.ShowToastThereIsNotEnoughItemsInStock -> {
                    Toast.makeText(context, value.msg, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }


    Box(Modifier.fillMaxSize()) {
        when (state) {
            DrugViewModel.PharmacyStockState.Idle -> {

            }

            is DrugViewModel.PharmacyStockState.Error -> {

                val err = state as DrugViewModel.MedStockState.Error

                Text(
                    text = "Error: ${err.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            DrugViewModel.PharmacyStockState.Loading -> {

            }

            is DrugViewModel.PharmacyStockState.Success -> {

                val items = state as DrugViewModel.PharmacyStockState.Success

                LazyColumn(Modifier.padding(12.dp)) {
                    item {
                        SectionCard(isLoading = false) {
                            Text(
                                text = pharmacyInfo?.name ?: "Apteka",
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(6.dp))

                            TwoLineRow("City", pharmacyInfo?.city.orDash())
                            TwoLineRow("Adress", pharmacyInfo?.address.orDash())

                            TwoLineRow(
                                "Distance from your location",
                                pharmacyInfo?.distanceKms
                                    ?.let { truncateTo2Decimals(it) }
                                    ?.let { "$it km" }
                                    .orDash()
                            )

                            Spacer(Modifier.height(6.dp))


                            OutlinedButton(
                                onClick = {
                                    if (pharmacyInfo?.id != null) {
                                        navigateToPharmacyScreen()
                                        pharmacyViewModel.getPharmacyDetails(pharmacyInfo!!.id)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View pharmacy details")
                            }
                        }
                    }

                    if (items.listOfMedStockDrugs.isNotEmpty() && pharmacyInfo != null) {
                        items(items.listOfMedStockDrugs, key = { it.packageNdc }) { medStockItem ->
                            SingleRecordOfDrug(
                                medStockItem,
                                onCartClick = { usersQt ->
                                    onAddToCartClick(
                                        medStockItem,
                                        pharmacyInfo!!,
                                        usersQt
                                    )
                                })
                        }
                    }
                }
            }

        }

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
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun SingleRecordOfDrug(
    medStockDrug: MedStockDrug,
    onCartClick: (qty: Int) -> Unit,
) {
    var qty by remember { mutableIntStateOf(1) }
    LaunchedEffect(qty) {
        println(qty)
    }

    val displayName = medStockDrug.brandName
        ?: medStockDrug.genericName
        ?: medStockDrug.drugNdc

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(returnGradientBackGround())
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1) Obrazek / mock zdjęcia leku
                LabeledImage(medStockDrug = medStockDrug)

                // 2) Nagłówek: nazwa + cena
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = White,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Transparent)
                            .drawBehind {
                                drawRoundRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.08f),
                                            Color.Black.copy(alpha = 0.05f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = max(size.width, size.height) * 5f
                                    ),
                                    cornerRadius = CornerRadius(32.dp.toPx())
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = String.format("%.2f zł", medStockDrug.price),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                    )
                }

                // 3) Panel z detalami
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Transparent)
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.07f),
                                        Color.Black.copy(alpha = 0.04f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = max(size.width, size.height) * 5.5f
                                ),
                                cornerRadius = CornerRadius(36.dp.toPx())
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Drug NDC: ${medStockDrug.drugNdc}",
                            color = White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Package NDC: ${medStockDrug.packageNdc}",
                            color = White,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        medStockDrug.labelerName?.let {
                            Text(
                                text = "Labeler: $it",
                                color = White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DecreaseIncreaseQtPharmacyScreen(
                        value = qty,
                        canIncrease = qty < medStockDrug.qt,
                        canDecrease = qty > 1,
                        onIncreaseQuantity = {
                            if (qty < medStockDrug.qt) qty++
                        },
                        onDecreaseQuantity = {
                            if (qty > 1) qty--
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)      // 🔹 ta sama wysokość co przycisk
                    )

                    Spacer(Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = { onCartClick(qty) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),     // 🔹 ta sama wysokość co kontrolka z ilością
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to cart"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add to cart",
                            fontSize = 13.sp
                        )
                    }
                }


                // 4) Ilość + info

                Text(
                    text = "$qty / ${medStockDrug.qt} pcs",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // 5) CTA – dodanie do koszyka

            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun DecreaseIncreaseQtPharmacyScreen(
    value: Int,
    canIncrease: Boolean,
    canDecrease: Boolean,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(40.dp)
            .padding(1.dp),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onDecreaseQuantity,
                enabled = canDecrease
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease quantity",
                    modifier = Modifier.padding(1.dp)
                )
            }

            Text(
                value.toString(),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = onIncreaseQuantity,
                enabled = canIncrease
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


