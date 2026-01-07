package com.onlycare.app.presentation.screens.female

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.OnlyCareSoftShadowContainer
import com.onlycare.app.presentation.components.OnlyCareTextField
import com.onlycare.app.presentation.components.OnlyCareTopAppBar
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.presentation.navigation.Screen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    navController: NavController,
    viewModel: WithdrawViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh KYC/UPI status whenever we return to this screen (after submitting PAN / saving UPI)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshFast()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    // Show success dialog
    if (state.withdrawalSuccess) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearWithdrawalSuccess()
                navController.navigateUp()
            },
            containerColor = ThemeSurface,
            title = { Text("Withdrawal Requested", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(state.successMessage ?: "Your withdrawal request has been submitted successfully!", color = TextSecondary) },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearWithdrawalSuccess()
                    navController.navigateUp()
                }, colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    
    // Show error state
    if (state.error != null && !state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "Failed to load",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.error ?: "Unknown error",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.retry() },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Refresh, "Retry", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            OnlyCareTopAppBar(
                title = "Withdraw",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input Section
            item {
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = Border.copy(alpha = 0.28f)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PrimaryLight, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Enter amount to withdraw",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OnlyCareTextField(
                                value = state.amount,
                                onValueChange = { viewModel.onAmountChange(it) },
                                label = "Amount",
                                placeholder = "0",
                                leadingIcon = Icons.Default.CurrencyRupee,
                                keyboardType = KeyboardType.Number,
                                enabled = !state.isLoading
                            )
                        }
                    }
                }
            }
            
            // Payment Method Section
            item {
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = Border.copy(alpha = 0.28f)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PrimaryLight, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Add payment method",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Complete KYC Option
                            PaymentMethodItem(
                                icon = Icons.Default.CreditCard,
                                title = "Complete KYC",
                                subtitle = null,
                                isCompleted = state.hasKycData,
                                onClick = { navController.navigate(Screen.KYC.route) }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // UPI ID Option
                            PaymentMethodItem(
                                icon = Icons.Default.Payment,
                                title = "UPI ID",
                                subtitle = "For instant money credit",
                                isCompleted = state.hasUpiData,
                                onClick = { navController.navigate(Screen.BankDetails.route) }
                            )
                        }
                    }
                }
            }
            
            // Withdrawal Summary
            item {
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = Border.copy(alpha = 0.28f)
                ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryLight, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Withdrawal Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val withdrawAmount = state.amount.toDoubleOrNull() ?: 0.0
                        SummaryRow("Total withdrawal amount", "₹${if (withdrawAmount > 0) String.format(Locale.US, "%.0f", withdrawAmount) else "0"}")
                        Spacer(modifier = Modifier.height(10.dp))
                        SummaryRow("Transaction fee", "= ₹${String.format(Locale.US, "%.0f", viewModel.getTransactionFee())}")
                        Spacer(modifier = Modifier.height(10.dp))
                        SummaryRow("TDS deduction (1%)", "= ₹${String.format(Locale.US, "%.0f", viewModel.getTdsDeduction())}")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = PrimaryLight.copy(alpha = 0.6f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Amount to Receive
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.border(1.dp, PrimaryLight, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Amount you will receive",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                val amountToReceive = viewModel.getAmountToReceive()
                                Text(
                                    text = "= ₹${if (amountToReceive > 0) String.format(Locale.US, "%.0f", amountToReceive) else "0"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                }
            }
            
            // Withdraw Button
            item {
                // Show error if any
                val withdrawalError = state.withdrawalError
                if (withdrawalError != null) {
                    Text(
                        text = withdrawalError,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                val currentAmount = state.amount.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = { viewModel.requestWithdrawal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    ),
                    enabled = !state.isProcessing && currentAmount > 0 && !state.isLoading
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Withdraw",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    isCompleted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.border(1.dp, PrimaryLight, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryLight.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) SuccessGreen else PrimaryLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Add,
                    contentDescription = if (isCompleted) "Completed" else "Add",
                    tint = if (isCompleted) Color.White else Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 14.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}



