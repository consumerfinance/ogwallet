package dev.consumerfinance.ogwallet.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PINSetupScreen(
    onPINSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Set Up Master PIN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Create a secure PIN to protect your financial data",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN Input
        OutlinedTextField(
            value = pin,
            onValueChange = { 
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    pin = it
                    errorMessage = null
                }
            },
            label = { Text("Enter PIN (6 digits)") },
            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            trailingIcon = {
                IconButton(onClick = { showPin = !showPin }) {
                    Icon(
                        imageVector = if (showPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPin) "Hide PIN" else "Show PIN"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm PIN Input
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { 
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    confirmPin = it
                    errorMessage = null
                }
            },
            label = { Text("Confirm PIN") },
            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null
        )
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                when {
                    pin.length != 6 -> {
                        errorMessage = "PIN must be exactly 6 digits"
                    }
                    pin != confirmPin -> {
                        errorMessage = "PINs do not match"
                    }
                    else -> {
                        onPINSet(pin)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = pin.length == 6 && confirmPin.length == 6
        ) {
            Text("Set PIN", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Security Tips:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Use a unique PIN you haven't used elsewhere",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Avoid sequential numbers (123456)",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Don't use your birthday or phone number",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

