package com.onlycare.app.presentation.screens.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(
    navController: NavController,
    packageId: String,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(packageId) {
        viewModel.loadPackage(packageId)
    }
    
    Scaffold(
        topBar = {
            OnlyCareTopAppBar(
                title = "Payment",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        if (isSuccess) {
            // Success State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Background),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            modifier = Modifier.size(82.dp),
                            tint = OnlineGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Payment Successful!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Coins have been added to your wallet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OnlyCarePrimaryButton(
                            text = "OK",
                            onClick = {
                                // Tell Wallet to refresh, then go back
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "wallet_refresh",
                                    true
                                )
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    state.error != null -> {
                        EmptyState(
                            icon = Icons.Default.Error,
                            title = "Unable to load package",
                            message = state.error ?: "Unknown error"
                        )
                    }

                    state.selectedPackage != null -> {
                        val pkg = state.selectedPackage!!

                        // Package Info (matches selected pack)
                        OnlyCareSoftShadowContainer(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            shadowOffsetY = 4.dp,
                            shadowColor = Border.copy(alpha = 0.28f)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Background),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Package Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Coins:", color = TextSecondary)
                                        Text(
                                            pkg.coins.toString(),
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Price:", color = TextSecondary)
                                        Text(
                                            "₹${pkg.price.toInt()}",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        OnlyCarePrimaryButton(
                            text = if (isProcessing) "Processing..." else "Pay ₹${pkg.price.toInt()}",
                            onClick = {
                                isProcessing = true
                                // Simulate payment (replace with actual purchase integration)
                                coroutineScope.launch {
                                    delay(2000)
                                    isProcessing = false
                                    isSuccess = true
                                }
                            },
                            loading = isProcessing
                        )

                        return@Scaffold
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                OnlyCarePrimaryButton(
                    text = if (isProcessing) "Processing..." else "Pay",
                    onClick = {
                        isProcessing = true
                        // Simulate payment
                        coroutineScope.launch {
                            delay(2000)
                            isProcessing = false
                            isSuccess = true
                        }
                    },
                    loading = isProcessing
                )
            }
        }
    }
}

