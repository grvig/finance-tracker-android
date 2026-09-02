package com.grvig.financetracker

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.grvig.financetracker.viewmodel.HouseholdViewModel

@Composable
fun NotificationSettingsScreen(
    householdViewModel: HouseholdViewModel,
    currentUserId: String,
    onBack: () -> Unit,
    onMasterEnabled: () -> Unit
) {

    val context = LocalContext.current

    var enabled by remember {
        mutableStateOf(NotificationPreferences.isEnabled(context, currentUserId))
    }

    var followed by remember {
        mutableStateOf(
            NotificationPreferences.followedUsers(context, currentUserId)
        )
    }

    // The in app switches mean nothing if Android itself is blocking us, and
    // the system dialog only ever asks once.
    var systemAllowed by remember {
        mutableStateOf(
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        )
    }

    val systemSettings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        systemAllowed = NotificationManagerCompat.from(context)
            .areNotificationsEnabled()
    }

    var members by remember {
        mutableStateOf<List<Pair<String, String>>>(emptyList())
    }

    LaunchedEffect(Unit) {
        members = householdViewModel
            .getMemberEmails(SessionManager.currentHouseholdId)
            .filterKeys { it != currentUserId }
            .toList()
            .sortedBy { it.second }
    }

    AppScaffold(
        title = "Notifications",
        onBack = onBack
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            if (!systemAllowed) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Notifications are turned off for this app",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Nothing below will reach you until you allow " +
                                "them in Android settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(
                            onClick = {
                                systemSettings.launch(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(
                                            Settings.EXTRA_APP_PACKAGE,
                                            context.packageName
                                        )
                                )
                            }
                        ) {
                            Text("Open settings")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expense notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Get notified when someone you pick adds an expense",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { on ->
                        enabled = on
                        NotificationPreferences.setEnabled(context, currentUserId, on)
                        if (on) {
                            ExpenseNotificationScheduler.markCaughtUp(context)
                            onMasterEnabled()
                        }
                        ExpenseNotificationScheduler.refresh(context)
                        systemAllowed = NotificationManagerCompat.from(context)
                            .areNotificationsEnabled()
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Notify me about",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Polling is skipped entirely while nobody is followed, so the
            // master switch on its own looks like it does nothing.
            if (enabled && followed.isEmpty() && members.isNotEmpty()) {
                Text(
                    text = "Pick at least one person. Nothing is checked while " +
                        "this list is empty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (members.isEmpty()) {
                Text(
                    text = "No other members in your household yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            members.forEach { (uid, email) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uid in followed,
                        enabled = enabled,
                        onCheckedChange = { on ->
                            NotificationPreferences.setFollowing(
                                context, currentUserId, uid, on
                            )
                            followed = NotificationPreferences
                                .followedUsers(context, currentUserId)
                            ExpenseNotificationScheduler.refresh(context)
                        }
                    )
                }
            }

            Text(
                text = "New expenses are checked every few minutes, so a " +
                    "notification can take a short while to arrive.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
