package pe.edu.idat.dsi.dami.idatgram.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.edu.idat.dsi.dami.idatgram.ui.theme.IdatgramTheme

/**
 * Campo de texto estilo Instagram
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdatgramTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    maxLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { 
                { 
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                } 
            },
            trailingIcon = trailingIcon?.let { icon ->
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onAny = { onImeAction?.invoke() }
            ),
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Campo de contraseña con botón para mostrar/ocultar
 */
@Composable
fun IdatgramPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Contraseña",
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    IdatgramTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = Icons.Default.Lock,
        trailingIcon = if (passwordVisible) Icons.Default.Lock else Icons.Default.Lock,
        onTrailingIconClick = { passwordVisible = !passwordVisible },
        isError = isError,
        errorMessage = errorMessage,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        onImeAction = onImeAction,
        enabled = enabled
    )
}

/**
 * Campo de búsqueda
 */
@Composable
fun IdatgramSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar",
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    IdatgramTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        trailingIcon = if (value.isNotEmpty()) Icons.Default.Clear else null,
        onTrailingIconClick = { 
            if (value.isNotEmpty()) {
                onClear?.invoke()
                onValueChange("")
            }
        },
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search,
        onImeAction = onSearch,
        enabled = enabled
    )
}

/**
 * Campo para comentarios con botón de envío
 */
@Composable
fun IdatgramCommentField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Agregar un comentario...",
    enabled: Boolean = true,
    maxLines: Int = 3
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IdatgramTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = placeholder,
            maxLines = maxLines,
            imeAction = ImeAction.Send,
            onImeAction = if (value.isNotBlank()) onSend else null,
            enabled = enabled
        )
        
        IdatgramIconButton(
            icon = Icons.Default.Send,
            onClick = onSend,
            enabled = enabled && value.isNotBlank(),
            tint = if (value.isNotBlank()) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Enviar comentario"
        )
    }
}

/**
 * Campo de texto multilinea para captions
 */
@Composable
fun IdatgramCaptionField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Escribe un caption...",
    maxCharacters: Int = 2200,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        IdatgramTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= maxCharacters) {
                    onValueChange(newValue)
                }
            },
            placeholder = placeholder,
            maxLines = 5,
            imeAction = ImeAction.Default,
            enabled = enabled
        )
        
        Text(
            text = "${value.length}/$maxCharacters",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 16.dp)
        )
    }
}

// === Previews ===

@Preview(showBackground = true)
@Composable
private fun TextFieldsPreview() {
    IdatgramTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var search by remember { mutableStateOf("") }
            var comment by remember { mutableStateOf("") }
            var caption by remember { mutableStateOf("") }
            
            IdatgramTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "ejemplo@email.com",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )
            
            IdatgramPasswordField(
                value = password,
                onValueChange = { password = it }
            )
            
            IdatgramTextField(
                value = "Error example",
                onValueChange = { },
                label = "Con Error",
                isError = true,
                errorMessage = "Este campo tiene un error"
            )
            
            IdatgramSearchField(
                value = search,
                onValueChange = { search = it },
                placeholder = "Buscar usuarios..."
            )
            
            IdatgramCommentField(
                value = comment,
                onValueChange = { comment = it },
                onSend = { comment = "" }
            )
            
            IdatgramCaptionField(
                value = caption,
                onValueChange = { caption = it }
            )
        }
    }
}