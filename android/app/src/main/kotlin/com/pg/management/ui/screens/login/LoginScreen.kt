package com.pg.management.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.R
import com.pg.management.domain.model.UserRole
import com.pg.management.ui.components.GlassCard
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.components.PrimaryButton
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.Slate200
import com.pg.management.ui.theme.Slate400

@Composable
fun LoginScreen(
    onLoggedInAsAdmin: () -> Unit,
    onLoggedInAsTenant: () -> Unit,
    onForgotPassword: () -> Unit,
    vm: LoginViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(s.success) {
        when (s.success) {
            UserRole.Admin -> { vm.consumeSuccess(); onLoggedInAsAdmin() }
            UserRole.Tenant -> { vm.consumeSuccess(); onLoggedInAsTenant() }
            null -> {}
        }
    }

    GradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(20.dp))
                BrandHeader()
                Spacer(Modifier.height(28.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28,
                    padding = PaddingValues(24.dp),
                ) {
                    Text(
                        text = stringResource(R.string.login_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.login_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate200,
                    )
                    Spacer(Modifier.height(22.dp))

                    IdentifierField(
                        value = s.identifier,
                        onValueChange = vm::onIdentifierChange,
                    )
                    Spacer(Modifier.height(14.dp))
                    PasswordField(
                        value = s.password,
                        onValueChange = vm::onPasswordChange,
                        onSubmit = {
                            keyboard?.hide()
                            vm.submit()
                        },
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = s.remember,
                                onCheckedChange = vm::onRememberToggle,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BrandCyan,
                                    uncheckedColor = Slate400,
                                    checkmarkColor = MaterialTheme.colorScheme.background,
                                ),
                            )
                            Text(
                                text = stringResource(R.string.login_remember),
                                color = Slate200,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        TextButton(onClick = onForgotPassword) {
                            Text(
                                text = stringResource(R.string.login_forgot),
                                color = BrandCyan,
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = s.error != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Text(
                            text = s.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(
                        text = stringResource(R.string.login_button),
                        onClick = {
                            keyboard?.hide()
                            vm.submit()
                        },
                        loading = s.loading,
                        enabled = s.canSubmit,
                    )
                }

                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.app_tagline),
                    color = Slate400,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_splash_logo),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_name),
            color = Color.White,
            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        )
    }
}

@Composable
private fun IdentifierField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = Slate400) },
        label = { Text(stringResource(R.string.login_id_label)) },
        placeholder = { Text(stringResource(R.string.login_id_placeholder), color = Slate400) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(14.dp),
        colors = textFieldColors(),
    )
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, onSubmit: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Slate400) },
        trailingIcon = {
            val icon = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility
            androidx.compose.material3.IconButton(onClick = { visible = !visible }) {
                Icon(icon, contentDescription = null, tint = Slate400)
            }
        },
        label = { Text(stringResource(R.string.login_password_label)) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        shape = RoundedCornerShape(14.dp),
        colors = textFieldColors(),
    )
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = BrandCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
    focusedLabelColor = BrandCyan,
    unfocusedLabelColor = Slate400,
    cursorColor = BrandCyan,
    focusedContainerColor = Color.White.copy(alpha = 0.05f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
)
