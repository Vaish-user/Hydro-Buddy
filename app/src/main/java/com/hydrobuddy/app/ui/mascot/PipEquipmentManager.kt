package com.hydrobuddy.app.ui.mascot

import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.RenderableManager
import com.google.android.filament.gltfio.FilamentAsset
import io.github.sceneview.model.ModelInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Categorization of equipment attachment zones to enforce mutual exclusivity where appropriate.
 */
enum class EquipmentCategory {
    HEAD,   // Mutually exclusive: Chef Hat vs Party Hat
    FACE,   // Can be worn simultaneously with HEAD gear (Sunglasses)
    NECK,   // Independent (Scarf)
    HAND_L, // Left Wing (Water Glass)
    HAND_R  // Right Wing (Cartoon Knife)
}

/**
 * The 6 modular 3D equipment accessories for Pip V2.
 */
enum class PipAccessory(
    val displayName: String,
    val category: EquipmentCategory,
    val nodeNames: List<String>
) {
    WATER_GLASS("Water Glass", EquipmentCategory.HAND_L, listOf("Prop_Glass", "Prop_Water")),
    CHEF_HAT("Chef Hat", EquipmentCategory.HEAD, listOf("Equip_ChefHat")),
    SUNGLASSES("Sunglasses", EquipmentCategory.FACE, listOf("Equip_Sunglasses")),
    SCARF("Cozy Scarf", EquipmentCategory.NECK, listOf("Equip_Scarf")),
    CARTOON_KNIFE("Cartoon Knife", EquipmentCategory.HAND_R, listOf("Prop_Knife")),
    PARTY_HAT("Party Hat", EquipmentCategory.HEAD, listOf("Equip_PartyHat"))
}

/**
 * Manages modular accessory toggling, bone socket preservation, and mutual exclusivity.
 */
class PipEquipmentManager(
    private val engine: Engine,
    private val modelInstance: ModelInstance,
    initialEquipped: Set<PipAccessory> = emptySet()
) {
    companion object {
        private const val TAG = "PipEquipmentManager"
    }

    private val rm: RenderableManager = engine.renderableManager
    // Maps accessory to list of its renderable entity IDs
    private val accessoryEntities = mutableMapOf<PipAccessory, List<Int>>()

    private val _equippedAccessories = MutableStateFlow<Set<PipAccessory>>(emptySet())
    val equippedAccessories: StateFlow<Set<PipAccessory>> = _equippedAccessories.asStateFlow()

    init {
        discoverEquipmentEntities()
        setEquippedAccessories(initialEquipped)
    }

    /**
     * Resolves verified GLB node names to actual Filament entities.
     */
    private fun discoverEquipmentEntities() {
        val asset: FilamentAsset = modelInstance.asset
        for (acc in PipAccessory.values()) {
            val entitiesForAcc = mutableListOf<Int>()
            for (nodeName in acc.nodeNames) {
                val entity = asset.getFirstEntityByName(nodeName)
                if (entity != 0 && rm.hasComponent(entity)) {
                    entitiesForAcc.add(entity)
                    Log.d(TAG, "Mapped '${acc.displayName}' node '$nodeName' -> entity $entity")
                } else {
                    Log.w(TAG, "Node '$nodeName' for '${acc.displayName}' not found in FilamentAsset")
                }
            }
            accessoryEntities[acc] = entitiesForAcc
        }
    }

    /**
     * Equips an accessory. Enforces mutual exclusivity:
     * e.g., equipping CHEF_HAT un-equips PARTY_HAT, and vice versa.
     */
    fun equip(accessory: PipAccessory) {
        val current = _equippedAccessories.value.toMutableSet()
        // Remove any item sharing the same category (e.g. HEAD: Chef Hat vs Party Hat)
        current.removeAll { it.category == accessory.category }
        current.add(accessory)
        applyEquippedSet(current)
    }

    /**
     * Un-equips an accessory.
     */
    fun unequip(accessory: PipAccessory) {
        val current = _equippedAccessories.value.toMutableSet()
        if (current.remove(accessory)) {
            applyEquippedSet(current)
        }
    }

    /**
     * Toggles an accessory on or off.
     */
    fun toggle(accessory: PipAccessory) {
        if (isEquipped(accessory)) {
            unequip(accessory)
        } else {
            equip(accessory)
        }
    }

    /**
     * Sets the complete active equipment configuration.
     */
    fun setEquippedAccessories(accessories: Set<PipAccessory>) {
        val sanitized = mutableSetOf<PipAccessory>()
        for (acc in accessories) {
            // Keep at most one item per category (last one wins)
            sanitized.removeAll { it.category == acc.category }
            sanitized.add(acc)
        }
        applyEquippedSet(sanitized)
    }

    /**
     * Checks if an accessory is currently equipped and visible.
     */
    fun isEquipped(accessory: PipAccessory): Boolean {
        return _equippedAccessories.value.contains(accessory)
    }

    /**
     * Un-equips all accessories, rendering Pip in clean baseline form.
     */
    fun unequipAll() {
        applyEquippedSet(emptySet())
    }

    private fun applyEquippedSet(newSet: Set<PipAccessory>) {
        _equippedAccessories.value = newSet
        for ((acc, entities) in accessoryEntities) {
            val isVisible = newSet.contains(acc)
            for (entity in entities) {
                if (rm.hasComponent(entity)) {
                    val rInstance = rm.getInstance(entity)
                    // Filament layerMask: bit 0 controls main camera visibility (0x1 = visible, 0x0 = hidden)
                    rm.setLayerMask(rInstance, 0xFF, if (isVisible) 0x1 else 0x0)
                }
            }
        }
    }

    fun dispose() {
        accessoryEntities.clear()
    }
}
