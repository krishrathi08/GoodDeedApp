package eu.tutorials.gooddeedproject.signin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.tutorials.gooddeedproject.R

@Composable
fun DividerWithText() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = " or continue with ",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SocialLoginButtons(
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onGitHubClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onGoogleClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(painterResource(id = R.drawable.ic_google), contentDescription = "Google Sign In", modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onBackground)
        }
        OutlinedButton(
            onClick = onAppleClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(painterResource(id = R.drawable.ic_apple), contentDescription = "Apple Sign In", modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onBackground)
        }
        OutlinedButton(
            onClick = onGitHubClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(painterResource(id = R.drawable.ic_github), contentDescription = "GitHub Sign In", modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}