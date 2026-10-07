package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.moneydiary.mndytr.ui.theme.ExpenseRed

@Composable
fun ReverseTransactionDialog(
    transactionId: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reasonInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "লেনদেন রিভার্স বা বাতিল",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "সতর্কতা: নিশ্চিত লেনদেন সরাসরি ডিলিট করা যায় না। এটি রিভার্স করলে একটি বিপরীত এন্ট্রি (TX_17_REVERSAL) তৈরি হয়ে অ্যাকাউন্টের ব্যালেন্স পূর্বাবস্থায় ফিরিয়ে আনবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Text(
                    text = "লেনদেন: $description",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = {
                        reasonInput = it
                        errorMessage = null
                    },
                    label = { Text("রিভার্স করার কারণ (বাধ্যতামূলক)") },
                    placeholder = { Text("যেমন: ভুলে দুইবার এন্ট্রি দেওয়া হয়েছিল") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reversal_reason_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reasonInput.trim().isEmpty()) {
                        errorMessage = "রিভার্স করার কারণ উল্লেখ করুন"
                        return@Button
                    }
                    onConfirm(reasonInput.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                modifier = Modifier.testTag("confirm_reversal_button")
            ) {
                Text("রিভার্স সম্পন্ন করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
