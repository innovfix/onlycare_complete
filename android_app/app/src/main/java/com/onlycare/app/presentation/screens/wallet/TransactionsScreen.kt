package com.onlycare.app.presentation.screens.wallet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.TransactionType
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val userGender = viewModel.getGender()
    
    Scaffold(
        topBar = {
            OnlyCareTopAppBar(
                title = "Transactions",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(8) {
                        OnlyCareSoftShadowContainer(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            shadowOffsetY = 4.dp,
                            shadowColor = Border.copy(alpha = 0.28f),
                            showSideShadows = true
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Background),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                OnlyCareListItemSkeleton()
                            }
                        }
                    }
                }
            }
            
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EmptyState(
                            icon = Icons.Default.Error,
                            title = "Error Loading Transactions",
                            message = state.error ?: "Unknown error"
                        )
                        OnlyCarePrimaryButton(
                            text = "Retry",
                            onClick = { viewModel.retry() }
                        )
                    }
                }
            }
            
            state.transactions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Receipt,
                        title = "No Transactions",
                        message = "Your transaction history will appear here"
                    )
                }
            }
            
            else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.transactions) { transaction ->
                    OnlyCareSoftShadowContainer(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        shadowOffsetY = 3.dp,
                        shadowColor = Border.copy(alpha = 0.24f)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Background),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (transaction.type) {
                                    TransactionType.PURCHASE -> Icons.Default.AddCircle
                                    TransactionType.CALL -> Icons.Default.Phone
                                    TransactionType.GIFT -> Icons.Default.CardGiftcard
                                    TransactionType.WITHDRAWAL -> Icons.Default.AccountBalance
                                    TransactionType.BONUS -> Icons.Default.Circle
                                },
                                contentDescription = null,
                                tint = Primary
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = transaction.title.ifEmpty { 
                                        transaction.type.name.lowercase().replaceFirstChar { it.uppercase() } 
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Text(
                                    text = formatTimestamp(transaction.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Circle,
                                        contentDescription = "Coins",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (transaction.isCredit) "+${transaction.coins}" else "-${transaction.coins}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (transaction.isCredit) OnlineGreen else ErrorRed
                                    )
                                }
                                // Hide currency amount for male users when type is CALL and it's a debit (call_spent)
                                val shouldShowAmount = transaction.amount > 0 && 
                                    !(userGender == Gender.MALE && 
                                      transaction.type == TransactionType.CALL && 
                                      !transaction.isCredit)
                                if (shouldShowAmount) {
                                    Text(
                                        text = "₹${String.format("%.2f", transaction.amount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}



