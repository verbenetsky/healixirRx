package com.example.pharmacystore.ui.auth.signIn

import android.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pharmacystore.R
import com.example.pharmacystore.common.returnGradientBackGround
import com.example.pharmacystore.ui.auth.OriginScreen


@Composable
fun SingInScreenOptions(
    navigateToSingInWithPhoneNumber: (OriginScreen) -> Unit,
    navigateToRegistrationOptions: () -> Unit,
    navigateToSingInWithEmail: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(returnGradientBackGround())
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.logo_apteka_4000_4000_try),
                contentDescription = "logo_pharmacy",
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.4.dp, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.10f)),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(returnGradientBackGround())
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Choose sign in method:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp
                            ),
                            color = androidx.compose.ui.graphics.Color.Black
                        )

                        Button(
                            onClick = { navigateToSingInWithPhoneNumber(OriginScreen.SIGN_IN) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 1.dp,
                                hoveredElevation = 8.dp,
                                focusedElevation = 8.dp,
                                disabledElevation = 0.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with phone #",
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { navigateToSingInWithEmail() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                width = 1.2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with email&password",
                                color = androidx.compose.ui.graphics.Color.Black,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f),
                border = BorderStroke(0.4.dp, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.10f)),
                tonalElevation = 2.dp
            ) {
                TextButton(
                    onClick = { navigateToRegistrationOptions() },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Don't have an account? Sign up",
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                }
            }
        }
    }
}
