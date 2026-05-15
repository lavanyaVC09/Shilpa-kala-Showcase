package com.example.shilpakalashowcase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.shilpakalashowcase.ui.theme.ShilpaKalaShowcaseTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.net.URLEncoder

// 🔹 Model with status support
data class Sculpture(
    val id: String = "",
    val name: String = "",
    val price: Long = 0,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val status: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShilpaKalaShowcaseTheme { HomeScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val db = FirebaseFirestore.getInstance()
    var sculptures by remember { mutableStateOf<List<Sculpture>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        db.collection("sculptures").addSnapshotListener { value, error ->
            if (error != null) {
                isLoading = false
                return@addSnapshotListener
            }
            if (value != null) {
                sculptures = value.documents.map { doc ->
                    val s = doc.toObject(Sculpture::class.java) ?: Sculpture()
                    s.copy(
                        id = doc.id,
                        description = s.description.ifEmpty { doc.getString("desciption") ?: "" },
                        ratingCount = if (s.ratingCount == 0) (doc.getLong("ratingcount") ?: doc.getLong("rating count") ?: 0).toInt() else s.ratingCount,
                        category = s.category.ifEmpty { doc.getString("Category") ?: "" },
                        status = doc.getString("status") ?: doc.getString("Status") ?: doc.getString("availability") ?: ""
                    )
                }
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ShilpaKala Showcase", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val previousIndex = (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                            listState.animateScrollToItem(previousIndex)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, "Scroll Up")
                }

                Spacer(Modifier.height(12.dp))

                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val nextIndex = (listState.firstVisibleItemIndex + 1).coerceAtMost(sculptures.size - 1)
                            listState.animateScrollToItem(nextIndex)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, "Scroll Down")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(sculptures, key = { it.id }) { sculpture ->
                    SculptureCard(sculpture)
                }
                item { ProjectRoadmapCard() }
            }
        }
    }
}

@Composable
fun SculptureCard(sculpture: Sculpture) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var showFullView by remember { mutableStateOf(false) }

    // 🔹 Normalize status to prevent matching errors (lowercase + trim spaces)
    val displayStatus = sculpture.status.lowercase().trim()

    if (showFullView) {
        FullScreenDialog(imageUrl = sculpture.imageUrl) { showFullView = false }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFEEEEEE))
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { showFullView = true })
                    }
            ) {
                var isImageLoading by remember { mutableStateOf(true) }

                AsyncImage(
                    model = sculpture.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    onState = { isImageLoading = it is AsyncImagePainter.State.Loading }
                )

                if (isImageLoading) {
                    Box(Modifier.fillMaxSize().background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }

                Text(
                    "Double Tap for Full Screen",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                        Text(sculpture.category, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }

                    // 🔹 Dynamic Badge Logic: Shows specific colors for known statuses, and a generic one for others
                    if (displayStatus.isNotEmpty()) {
                        when (displayStatus) {
                            "wip" -> StatusBadge("WIP", Color.Red, Color(0xFFFFEBEE))
                            "for sale", "available", "in stock" -> StatusBadge("FOR SALE", Color(0xFF1976D2), Color(0xFFE3F2FD))
                            "limited", "limited stock" -> StatusBadge("LIMITED STOCK", Color(0xFFE65100), Color(0xFFFFF3E0))
                            "sold out", "out of stock" -> StatusBadge("OUT OF STOCK", Color.DarkGray, Color(0xFFF5F5F5))
                            else -> StatusBadge(sculpture.status.uppercase(), MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                        Text(" ${sculpture.rating} (${sculpture.ratingCount})", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(sculpture.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("₹${sculpture.price}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))
                Text(
                    text = sculpture.description,
                    maxLines = if (isExpanded) 100 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize().clickable { isExpanded = !isExpanded },
                    color = Color.DarkGray
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val encodedMsg = URLEncoder.encode("Inquiry for ${sculpture.name}", "UTF-8")
                        val uri = Uri.parse("https://wa.me/911234567890?text=$encodedMsg")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = displayStatus != "sold out" && displayStatus != "out of stock",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (displayStatus == "sold out" || displayStatus == "out of stock") Color.LightGray else Color(0xFF25D366)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (displayStatus == "sold out" || displayStatus == "out of stock") "OUT OF STOCK" else "WhatsApp Inquiry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProjectRoadmapCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Development Roadmap", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("• Live Firebase Sync: Active ✅", fontSize = 12.sp)
            Text("• High-Res Image Gallery: Active ✅", fontSize = 12.sp)
            Text("• AR View for Sculptures: Development 🚧", fontSize = 12.sp)
            Text("• Integrated Checkout: Planned 📅", fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(Modifier.height(8.dp))
            Text(
                "Note: All images used in this project are for display purposes; all credits go to their respective original owners.",
                fontSize = 13.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun FullScreenDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) offset += pan else offset = Offset.Zero
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            scale = if (scale > 1f) 1f else 3f
                            offset = Offset.Zero
                        })
                    }
            ) {
                var isImageLoading by remember { mutableStateOf(true) }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale, scaleY = scale,
                            translationX = offset.x, translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit,
                    onState = { isImageLoading = it is AsyncImagePainter.State.Loading }
                )
                if (isImageLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color.White)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}