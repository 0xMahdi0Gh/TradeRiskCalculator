package com.example.traderiskcalculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var balance by remember { mutableStateOf(prefs.getString("balance", "10000") ?: "10000") }
    var risk by remember { mutableStateOf("1") }
    var entry by remember { mutableStateOf("60000") }
    var stop by remember { mutableStateOf("59000") }
    var leverage by remember { mutableStateOf("10") }
    var isLong by remember { mutableStateOf(true) }

    val b = balance.toDoubleOrNull() ?: 0.0
    val r = risk.toDoubleOrNull() ?: 0.0
    val e = entry.toDoubleOrNull() ?: 0.0
    val s = stop.toDoubleOrNull() ?: 0.0
    val lev = leverage.toDoubleOrNull() ?: 0.0

    val validDirection = if (isLong) s < e else s > e
    val riskAmount = b * r / 100.0
    val stopDistance = abs(e - s)
    val positionSize = if (stopDistance > 0) riskAmount / stopDistance else 0.0
    val positionValue = positionSize * e
    val margin = if (lev > 0) positionValue / lev else 0.0
    val rrRows = listOf(1, 2, 3, 4, 5).map { rr ->
        val tp = if (isLong) e + stopDistance * rr else e - stopDistance * rr
        val profit = riskAmount * rr
        Triple(rr, tp, profit)
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF2563EB)
        )
    ) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "محاسبه‌گر ریسک ترید",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "حجم پوزیشن را بر اساس ریسک و استاپ‌لاس محاسبه کن",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = balance,
                    onValueChange = {
                        balance = it
                        prefs.edit().putString("balance", it).apply()
                    },
                    label = { Text("بالانس حساب") },
                    suffix = { Text("USDT") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = risk,
                    onValueChange = { risk = it },
                    label = { Text("ریسک هر معامله") },
                    suffix = { Text("%") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isLong = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("LONG") }
                    OutlinedButton(
                        onClick = { isLong = false },
                        modifier = Modifier.weight(1f)
                    ) { Text("SHORT") }
                }

                OutlinedTextField(
                    value = entry,
                    onValueChange = { entry = it },
                    label = { Text("قیمت ورود") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stop,
                    onValueChange = { stop = it },
                    label = { Text("Stop Loss") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = leverage,
                    onValueChange = { leverage = it },
                    label = { Text("Leverage") },
                    suffix = { Text("x") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (!validDirection && e > 0 && s > 0) {
                    Text(
                        if (isLong) "برای LONG، Stop Loss باید پایین‌تر از Entry باشد."
                        else "برای SHORT، Stop Loss باید بالاتر از Entry باشد.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider()

                Text("نتیجه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                ResultRow("مقدار ریسک", money(riskAmount))
                ResultRow("فاصله Entry تا SL", number(stopDistance))
                ResultRow("حجم پوزیشن", number(positionSize))
                ResultRow("ارزش پوزیشن", money(positionValue))
                ResultRow("مارجین موردنیاز", money(margin))

                HorizontalDivider()

                Text(
                    "سود بر اساس Risk / Reward",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text("Take Profit ورودی نیست؛ قیمت TP برای هر R:R خودکار محاسبه شده است.")

                rrRows.forEach { (rr, tp, profit) ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "1:$rr",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TP: ${number(tp)}")
                                Text(
                                    "+${money(profit)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    "اگر Stop Loss بخورد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "-${money(riskAmount)}  (${number(r)}% بالانس)",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    "نکته: این محاسبه‌گر کارمزد، funding و slippage را در نسخه فعلی لحاظ نمی‌کند.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

fun number(v: Double): String =
    String.format(Locale.US, "%,.6f", v).trimEnd('0').trimEnd('.')

fun money(v: Double): String =
    "$" + String.format(Locale.US, "%,.2f", v)
