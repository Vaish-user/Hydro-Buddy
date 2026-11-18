package com.hydrobuddy.app.ui.mascot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.ui.theme.MintTealPrimary
import com.hydrobuddy.app.ui.theme.MintPastelSecondary
import com.hydrobuddy.app.ui.theme.MintSoftBackground
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SageTextSecondary
import com.hydrobuddy.app.ui.theme.SoftMintContainer
import com.hydrobuddy.app.ui.theme.SpruceTextPrimary
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

/**
 * Dedicated 3D hardware-accelerated prototype viewer for the custom Stage 1 Blender model
 * (assets/models/pip_stage1.glb).
 *
 * Characteristics:
 * - Direct SceneView 2.2.1 / Filament rendering
 * - Non-opaque / transparent background
 * - Scaled to units and centered for idle presentation
 * - 9,380 triangles, 12 PBR glTF-compatible materials
 */
@Composable
fun PipStage1ModelView(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    characterSize: CharacterSize = CharacterSize.MEDIUM,
    onError: (() -> Unit)? = null
) {
    val actualSize = size * characterSize.scaleFactor
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine).apply {
        // Front-facing, slightly elevated, framing Pip's 1.12m height
        position = Float3(x = 0f, y = 0.50f, z = 2.3f)
        lookAt(Float3(x = 0f, y = 0.45f, z = 0f))
    }

    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(engine, modelLoader) {
        try {
            val instance = modelLoader.createModelInstance("models/pip_stage1.glb")
            val node = ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.0f
            ).apply {
                position = Float3(x = 0f, y = 0f, z = 0f)
            }
            modelNode = node
        } catch (e: Exception) {
            loadFailed = true
            onError?.invoke()
        }
    }

    Box(
        modifier = modifier.size(actualSize),
        contentAlignment = Alignment.Center
    ) {
        val node = modelNode
        if (node != null && !loadFailed) {
            Scene(
                modifier = Modifier.size(actualSize),
                engine = engine,
                modelLoader = modelLoader,
                cameraNode = cameraNode,
                isOpaque = false,
                childNodes = rememberNodes {
                    add(node)
                }
            )
        } else if (loadFailed) {
            // Graceful fallback display if device GL context fails
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Pip Stage 1 (3D)",
                    style = MaterialTheme.typography.labelSmall.copy(color = SageTextSecondary)
                )
            }
        }
    }
}

/**
 * Interactive developer comparison component to switch and compare
 * the existing Production Pip and the new Blender Stage 1 model.
 */
enum class ComparisonMode(val label: String) {
    PRODUCTION("Production Pip"),
    STAGE1("Blender Stage 1"),
    SIDE_BY_SIDE("Side-by-Side")
}

@Composable
fun PipModelComparisonCard(
    modifier: Modifier = Modifier,
    characterSize: CharacterSize = CharacterSize.MEDIUM
) {
    var mode by remember { mutableStateOf(ComparisonMode.SIDE_BY_SIDE) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SoftMintContainer)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MintSoftBackground)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ComparisonMode.values().forEach { compMode ->
                val isSelected = mode == compMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) PureWhiteSurface else Color.Transparent)
                        .clickable { mode = compMode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = compMode.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SpruceTextPrimary else SageTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Display area based on selection
        when (mode) {
            ComparisonMode.PRODUCTION -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PipCharacterView(
                        size = 110.dp,
                        characterSize = characterSize,
                        prefer3D = true,
                        modelAssetPath = "models/pip.glb"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Production Model (Rigged • 8 Actions • 586 KB)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SpruceTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            ComparisonMode.STAGE1 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PipStage1ModelView(
                        size = 110.dp,
                        characterSize = characterSize
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Blender Stage 1 (Custom Model • 9,380 tris • 385 KB)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MintTealPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            ComparisonMode.SIDE_BY_SIDE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Production Pip
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PureWhiteSurface)
                                .border(1.dp, MintPastelSecondary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            PipCharacterView(
                                size = 80.dp,
                                characterSize = CharacterSize.SMALL,
                                prefer3D = true,
                                modelAssetPath = "models/pip.glb"
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Production",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpruceTextPrimary,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "Rigged 8-Anim",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right: Blender Stage 1
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PureWhiteSurface)
                                .border(1.5.dp, MintTealPrimary, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            PipStage1ModelView(
                                size = 80.dp,
                                characterSize = CharacterSize.SMALL
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Blender Stage 1",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MintTealPrimary,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "Static Geometry",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
