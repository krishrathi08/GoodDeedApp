package com.yourpackage.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.gooddeedproject.ui.theme.AppBackgroundColor
import eu.tutorials.gooddeedproject.ui.theme.BlueButtonColor
import eu.tutorials.gooddeedproject.ui.theme.ButtonTextColor
import eu.tutorials.gooddeedproject.ui.theme.OrangeButtonColor
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.ui.theme.poppinsFontFamily


@Composable
fun SignUpScreen(
    onUserSignUpClick: () -> Unit,
    onOrganizerSignUpClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the single, complete logo image
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(220.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Sign Up as User Button
            SignUpButton(
                text = "Sign Up As User",
                onClick = onUserSignUpClick,
                backgroundColor = BlueButtonColor,
                {
                    Image(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "User Logo",
                        modifier = Modifier.width(28.dp) // Adjust width as needed
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up as Organizer Button
            SignUpButton(
                text = "Sign Up As Organizer",
                onClick = onOrganizerSignUpClick,
                backgroundColor = OrangeButtonColor,
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_organizer),
                        contentDescription = "Organizer Logo",
                        modifier = Modifier.width(28.dp) // Adjust width as needed
                    )
                }
            )
        }
    }
}

@Composable
fun SignUpButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    icon: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = ButtonTextColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(onUserSignUpClick = {}, onOrganizerSignUpClick = {})
}