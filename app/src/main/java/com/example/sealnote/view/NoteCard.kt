package com.example.sealnote.view

// Pastikan Anda memiliki impor untuk warna kustom Anda
// import com.example.sealnote.ui.theme.CardBackgroundColor
// import com.example.sealnote.ui.theme.PrimaryTextColor
// import com.example.sealnote.ui.theme.SecondaryTextColor
// import com.example.sealnote.ui.theme.TertiaryTextColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sealnote.model.Notes
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Notes,
    onBookmarkClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleSecretClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        // Membuat seluruh kartu bisa diklik untuk mengedit
        onClick = onEditClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Box digunakan untuk menampung konten dan tombol menu yang menumpang (overlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    // Beri sedikit ruang di kanan agar tidak tertimpa menu
                    modifier = Modifier.padding(end = 32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = note.updatedAt.formatToReadableDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Box untuk tombol menu, diposisikan di pojok kanan atas
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            val bookmarkText = if (note.bookmarked) "Remove from Bookmark" else "Add to Bookmark"
                            Text(bookmarkText)
                        },
                        onClick = {
                            onBookmarkClick() // Panggil lambda onBookmarkClick
                            isMenuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (note.bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (note.bookmarked) "Remove from Bookmark" else "Add to Bookmark"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEditClick()
                            isMenuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, "Edit") }
                    )
                    DropdownMenuItem(
                        text = {
                            val text = if (note.secret) "Remove from Secret" else "Add to Secret"
                            Text(text)
                        },
                        onClick = {
                            onToggleSecretClick()
                            isMenuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                if (note.secret) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                                contentDescription = "Toggle Secret"
                            )
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Move to Trash") },
                        onClick = {
                            onDeleteClick()
                            isMenuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.Delete, "Move to Trash") }
                    )
                }
            }
        }
    }
}

// Fungsi helper untuk mengubah format tanggal (bisa dipindah ke file util)
private fun Date?.formatToReadableDate(): String {
    if (this == null) return ""
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(this)
}