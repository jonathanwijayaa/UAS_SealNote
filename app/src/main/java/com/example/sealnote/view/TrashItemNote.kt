package com.example.sealnote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sealnote.ui.theme.ItemNoteCardBackground
import com.example.sealnote.ui.theme.ItemNoteContentColor
import com.example.sealnote.ui.theme.ItemNoteDateColor
import com.example.sealnote.ui.theme.ItemNoteRestoreButtonBackground
import com.example.sealnote.ui.theme.ItemNoteRestoreButtonTextColor
import com.example.sealnote.ui.theme.ItemNoteTitleColor
import com.example.sealnote.ui.theme.TrashScreenBackground
import com.example.sealnote.model.DeletedNote // <-- ADD THIS IMPORT

// REMOVE THE data class DeletedNote DEFINITION FROM HERE
// data class DeletedNote(
//    val id: String,
//    val title: String,
//    val contentSnippet: String,
//    val deletionDate: String // Harus berisi teks lengkap seperti "Deleted date : 22 Mei 2022"
// )

@Composable
fun TrashNoteItem( // Ini akan menggantikan placeholder TrashNoteItem di TrashScreen.kt
    note: DeletedNote,
    onRestoreClick: (noteId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(), // Mengisi lebar sel grid
        shape = RoundedCornerShape(8.dp), // app:cardCornerRadius="8dp"
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // app:cardElevation="4dp"
        colors = CardDefaults.cardColors(containerColor = ItemNoteCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // padding="12dp" pada LinearLayout horizontal
            verticalAlignment = Alignment.CenterVertically // Untuk menengahkan tombol Restore secara vertikal
        ) {
            // Bagian Kiri: Info Catatan
            Column(
                modifier = Modifier.weight(1f) // Mengambil sisa ruang, mendorong tombol ke kanan
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ItemNoteTitleColor,
                    maxLines = 2, // Tambahkan untuk konsistensi jika judul bisa panjang
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp)) // paddingTop="4dp" untuk tvContent

                Text(
                    text = note.contentSnippet,
                    fontSize = 14.sp,
                    color = ItemNoteContentColor,
                    maxLines = 3, // android:maxLines="3"
                    overflow = TextOverflow.Ellipsis // android:ellipsize="end"
                )

                Spacer(modifier = Modifier.height(6.dp)) // paddingTop="6dp" untuk tvDate

                Text(
                    // Asumsi field note.deletionDate sudah berisi string lengkap seperti "Deleted date : ..."
                    text = note.deletionDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, // android:textStyle="bold"
                    color = ItemNoteDateColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp)) // Memberi sedikit jarak sebelum tombol

            // Bagian Kanan: Tombol Restore Note
            // Menggunakan Box untuk membuat tampilan tombol kustom
            Box(
                modifier = Modifier
                    .background(ItemNoteRestoreButtonBackground, shape = RoundedCornerShape(4.dp)) // Meniru @drawable/btn_restore
                    .clickable { onRestoreClick(note.id) }
                    // Padding XML adalah 5dp, bisa disesuaikan untuk touch target yang lebih baik
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center // gravity="center" untuk teks di dalam tombol
            ) {
                Text(
                    text = "Restore Note",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ItemNoteRestoreButtonTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TrashNoteItemPreviewDark() {
    // Pastikan Anda memiliki data class DeletedNote yang terdefinisi
    val sampleNote = DeletedNote(
        id = "1",
        title = "Contoh Judul Catatan yang Panjang Sekali Sehingga Mungkin Perlu Beberapa Baris",
        contentSnippet = "Ini adalah cuplikan konten catatan yang telah dihapus. Cuplikan ini bisa cukup panjang hingga mencapai tiga baris maksimum sebelum akhirnya terpotong.",
        deletionDate = "Deleted date : 2 Juni 2025"
    )
    MaterialTheme { // Atau tema kustom aplikasi Anda
        Surface(color = TrashScreenBackground) { // Untuk mensimulasikan latar belakang screen
            Box(modifier = Modifier.padding(8.dp)) { // Mensimulasikan margin yang mungkin ada di grid item
                TrashNoteItem(note = sampleNote, onRestoreClick = {})
            }
        }
    }
}