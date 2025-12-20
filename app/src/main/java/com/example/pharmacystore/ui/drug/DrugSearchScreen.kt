package com.example.pharmacystore.ui.drug

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.pharmacystore.R
import com.example.pharmacystore.domain.model.Drug
import com.example.pharmacystore.domain.model.displayName
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.ui.shoppingcart.ShoppingCartViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.example.pharmacystore.common.findActivity
import com.example.pharmacystore.common.utlis.MyCaptureActivity
import com.example.pharmacystore.common.utlis.generateBarcodeBitmapFrom13Digits
import com.example.pharmacystore.data.remote.MedStockDrug
import com.example.pharmacystore.data.remote.displayName
import com.example.pharmacystore.ui.pharmacies.PharmacyViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun DrugSearchScreen(
    drugNdcFromShoppingCartScreen: String?,
    drugViewModel: DrugViewModel,
    shoppingCartViewModel: ShoppingCartViewModel,
    pharmacyViewModel: PharmacyViewModel,
    address: String,
    navigateToPickPackage: () -> Unit,
    navigateToShoppingCartScreen: () -> Unit,
) {

    val context = LocalContext.current

    var showDialogPermissionRejected by remember { mutableStateOf(false) }

    // Na nowszych Androidach nie ma już widocznego "Don't ask again",
    // ale system zachowuje się podobnie:
    //
    // 1) Pierwsze tam "Don't allow":
    //    - isGranted == false
    //    - shouldShowRequestPermissionRationale(...) == true
    //    → można jeszcze raz poprosić o pozwolenie (np. po pokazaniu własnego dialogu z wyjaśnieniem).
    //
    // 2) Po kilku odmowach(okolo 2) system przestaje pokazywać dialog:
    //    - isGranted == false
    //    - shouldShowRequestPermissionRationale(...) == false
    //    → od tego momentu jedyna opcja to wysłać użytkownika do ustawień aplikacji
    //      i poprosić, żeby ręcznie włączył uprawnienie.

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("msg", "CAM ACCESS GRANTED")
                println("cam access granted")
            } else {
                ActivityResultContracts.RequestPermission()
                println("access rejected")
                showDialogPermissionRejected = true
            }
        }

    val scope = rememberCoroutineScope()
    var upcResult by remember { mutableStateOf<DrugViewModel.UpcCodesResult?>(null) }
    var isLoading by remember { mutableStateOf<Map<String, Boolean>?>(null) }
    var pickedDrugNdc by remember { mutableStateOf("") }

    LaunchedEffect(drugNdcFromShoppingCartScreen) {
        if (!drugNdcFromShoppingCartScreen.isNullOrBlank()) {
            drugViewModel.changeSearchBarState(drugNdcFromShoppingCartScreen)
        }
    }

    val items = drugViewModel.drugsPagingFlow.collectAsLazyPagingItems()

    val searchBarState by drugViewModel.searchBarState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    val cartSizeState by shoppingCartViewModel.cartSize.collectAsStateWithLifecycle()

    LaunchedEffect(address) { pharmacyViewModel.ensureLocationFromAddressIfNeeded(address) }

    // kazda activity to jednoczesnie context ale nie kazdy contex to activity
    // val activity = context as Activity // - to teoretycznie moze sie wywalic jesli napotkamy sytuacje
    // kiedy ten context nie jest activity. Czym jest ten context zalezy gdzie sie znajdujemy

    val activity = LocalContext.current.findActivity()

    // Launcher do odpalenia aktywności skanera ZXing
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intentResult = IntentIntegrator.parseActivityResult(
            result.resultCode,
            result.data
        )
        val contents = intentResult?.contents
        if (!contents.isNullOrEmpty()) {
            if (contents.length == 12) {
                drugViewModel.changeSearchBarState("0${contents}")
            } else {
                drugViewModel.changeSearchBarState(contents)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchBarState,
                onValueChange = { drugViewModel.changeSearchBarState(it) },
                label = { Text("Enter UPC (barcode), NDC or name of drug to search") },
                singleLine = true,
                trailingIcon = {

                    // search
                    Row() {
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                // aktualizujemy state w VM → odpali się nowy Pager
                                drugViewModel.changeSearchBarState(searchBarState)
                            }, enabled = searchBarState.isNotEmpty()
                        ) { Icon(Icons.Default.Search, contentDescription = "search") }

                        // open camera
                        IconButton(
                            onClick = {
                                keyboardController?.hide()

                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (granted) {
                                    // już mamy dostęp do kamerki

                                    val integrator = IntentIntegrator(activity).apply {
                                        setCaptureActivity(MyCaptureActivity::class.java)
                                        setOrientationLocked(false)
                                    }

                                    val scanIntent = integrator.createScanIntent()
                                    scanLauncher.launch(scanIntent)
                                } else {
                                    launcher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        ) { Icon(Icons.Default.CameraAlt, contentDescription = "open camera") }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = items.loadState.refresh) {
                is LoadState.Loading -> {
                    // pierwszy load
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadState.Error -> { // error przy pierwszym ładowaniu
                    val error = state.error
                    println("error: ${error.localizedMessage}")
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Something went wrong:\n${error.localizedMessage ?: "Unknown error"}")
                    }
                }

                is LoadState.NotLoading -> {
                    if (items.itemCount == 0 && searchBarState.isNotEmpty()) {
                        // nic nie znaleziono
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) { Text("No items found") }
                    } else {
                        // lista wyników
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                count = items.itemCount,
                                key = items.itemKey { it.drugNDC },
                                contentType = items.itemContentType { "DrugItem" },
                            ) { index ->
                                val drug = items[index]
                                if (drug != null) {
                                    SingleRecordOfDrug(
                                        drug = drug,
                                        onCartClick = {
                                            drugViewModel.pickPackage(drug)
                                            navigateToPickPackage()
                                        },
                                        onShowBarCodeClick = {
                                            scope.launch {
                                                isLoading = isLoading?.plus((drug.drugNDC to true))
                                                upcResult = drugViewModel.getUpcCodes(drug.drugNDC)
                                                pickedDrugNdc = drug.drugNDC
                                                isLoading = isLoading?.plus((drug.drugNDC to false))
                                            }
                                        },
                                        pickedDrugNdc = pickedDrugNdc,
                                        isLoading = isLoading?.get(drug.drugNDC) == true,
                                        upcState = upcResult,
                                    )
                                }
                            }

                            // loader / error przy doładowywaniu kolejnych stron
                            item {
                                when (val appendState = items.loadState.append) {
                                    is LoadState.Loading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    is LoadState.Error -> {
                                        Text(
                                            text = "Error loading more: ${appendState.error.localizedMessage}",
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    else -> Unit
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
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
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart"
                )
            }

            if (cartSizeState > 0) {
                val scale = remember { Animatable(1f) }
                var previousCartSize by remember { mutableIntStateOf(cartSizeState) }

                LaunchedEffect(cartSizeState) {
                    if (cartSizeState > previousCartSize) {
                        previousCartSize = cartSizeState

                        scale.snapTo(1.3f)
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    } else {
                        previousCartSize = cartSizeState
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(22.dp)
                        .scale(scale.value)
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cartSizeState <= 99) cartSizeState.toString() else "99",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 11.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDialogPermissionRejected) {
        RejectCameraPermissionDialog(
            onDismiss = { showDialogPermissionRejected = false },
            onOpenSettingsClick = {
                openAppSettings(context)
            }
        )
    }
}

@Composable
fun SingleRecordOfDrug(
    drug: Drug,
    pickedDrugNdc: String,
    onCartClick: () -> Unit,
    onShowBarCodeClick: () -> Unit,
    isLoading: Boolean,
    upcState: DrugViewModel.UpcCodesResult?,
) {

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
                // 1) Obrazek
                LabeledImage(drug)

                // 3) Nazwa leku
                Text(
                    text = drug.displayName.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Transparent)

                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = max(
                                        size.width,
                                        size.height
                                    ) * 10f // większe, bardziej miękkie rozmycie
                                ),
                                cornerRadius = CornerRadius(40.dp.toPx())
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )

                // 4) Miękko przyciemniony panel z detalami
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Transparent)
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = max(
                                        size.width,
                                        size.height
                                    ) * 10f // większe, bardziej miękkie rozmycie
                                ),
                                cornerRadius = CornerRadius(40.dp.toPx())
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "NDC: ${drug.drugNDC}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Labeler: ${drug.labelerName}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Active ingredients",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        drug.activeIngredients.forEach { ing ->
                            Text(
                                text = "• ${ing.name}: ${ing.strength}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Packaging",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        drug.packaging.forEach { pkg ->
                            Text(
                                text = "• ${pkg.packageNdc} – ${pkg.desc}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // 2) Przyciski akcji
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onCartClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "Add to cart or check availability",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                }

                // przycisk
                OutlinedButton(
                    onClick = { onShowBarCodeClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show barcode (UPC)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (upcState != null && pickedDrugNdc == drug.drugNDC) {
                    when (upcState) {
                        is DrugViewModel.UpcCodesResult.Error -> {
                            Text("Something went wrong: ${upcState.message}")
                        }

                        DrugViewModel.UpcCodesResult.NoData -> {
                            Text("There is no UPC data for this product")
                        }

                        is DrugViewModel.UpcCodesResult.Success -> {
                            upcState.codes.forEach { code ->
                                if (code.length == 13) {
                                    BarcodeFrom13Digits(code)
                                    Text(
                                        code,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}


@Composable
fun LabeledImage(drug: Drug? = null, medStockDrug: MedStockDrug? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.drug_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Text(
            text = medStockDrug?.displayName ?: drug?.displayName ?: "",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        )
    }
}

@Composable
fun BarcodeFrom13Digits(
    code: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(code) {
        generateBarcodeBitmapFrom13Digits(code)
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Barcode for $code",
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RejectCameraPermissionDialog(onDismiss: () -> Unit, onOpenSettingsClick: () -> Unit) {

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Camera permission required",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "To scan barcodes, the app needs access to your camera. " +
                            "Please allow camera permission in the system dialog when asked. " +
                            "If the dialog no longer appears or you denied it before, go to your phone’s app settings, " +
                            "open this app and enable the Camera permission manually.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {onOpenSettingsClick()}) {
                        Text("Open Settings")
                    }
                }

            }
        }
    }
}



private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

