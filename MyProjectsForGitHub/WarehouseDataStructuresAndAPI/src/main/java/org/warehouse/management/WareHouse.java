package org.warehouse.management;

import org.warehouse.exceptions.*;
import org.warehouse.model.MaterialType;

import java.util.Map;

public class WareHouse implements Inventory {
    private final Map<MaterialType, Integer> warehouseMaterials;

    public WareHouse(Map<MaterialType, Integer> warehouseMaterials) {
        this.warehouseMaterials = warehouseMaterials;
    }

    /**
     * Adds a new material to the warehouse if it doesn't already exist.
     * @param type The type of material to add.
     * @param quantity The quantity of the material to add.
     * @return The type of material added.
     * @throws ExceedingCapacity If the quantity exceeds the material's maximum capacity.
     * @throws InvalidQuantity If the specified quantity is less than or equal to zero.
     * @throws MaterialAlreadyExists If the material already exists in the inventory.
     */
    @Override
    public MaterialType addMaterial(MaterialType type, int quantity) throws ExceedingCapacity, InvalidQuantity, MaterialAlreadyExists {
        checkInvalidQuantity(type, quantity);
        if (warehouseMaterials.containsKey(type)) {
            throw new MaterialAlreadyExists("The material you want to add already exists");
        } else {
            warehouseMaterials.put(type, quantity);
        }

        return type;
    }

    /**
     * Updates the quantity of an existing material in the warehouse.
     * @param type The type of material to update.
     * @param quantity The quantity to add to the existing material quantity.
     * @throws ExceedingCapacity If the update causes the quantity to exceed the maximum capacity.
     * @throws InvalidQuantity If the specified quantity is invalid.
     * @throws MaterialNotFound If the material is not found in the inventory.
     */
    @Override
    public void updateMaterialQuantity(MaterialType type, int quantity) throws ExceedingCapacity, InvalidQuantity, MaterialNotFound {
        checkInvalidQuantity(type, quantity);

        if (!warehouseMaterials.containsKey(type)) {
            throw new MaterialNotFound("The material you want to update is not found");
        } else {
            warehouseMaterials.put(type, warehouseMaterials.get(type) + quantity);
        }
    }

    /**
     * Helper method to check if the quantity is valid.
     * @param type The type of material.
     * @param quantity The quantity to check.
     * @throws InvalidQuantity If the quantity is less than or equal to zero.
     * @throws ExceedingCapacity If the quantity exceeds the maximum capacity of the material.
     */
    private void checkInvalidQuantity(MaterialType type, int quantity) throws InvalidQuantity, ExceedingCapacity {
        if (quantity <= 0) {
            throw new InvalidQuantity("The quantity must be greater than 0");
        }

        if (quantity > type.getMaximumCapacity()) {
            throw new ExceedingCapacity("Adding " + quantity + " units of " + type.getName() + " would exceed the max capacity of " + type.getMaximumCapacity());
        }
    }

    /**
     * Removes a material completely from the warehouse.
     * @param type The type of material to remove.
     * @return The type of the material removed.
     * @throws MaterialNotFound If the material is not found in the inventory.
     */
    @Override
    public MaterialType removeMaterial(MaterialType type) throws MaterialNotFound {
        MaterialType typeToBeRemoved;
        if (!warehouseMaterials.containsKey(type)) {
            throw new MaterialNotFound("The material you want to delete is not found");
        } else {
            typeToBeRemoved = type;
            warehouseMaterials.remove(typeToBeRemoved);
        }
        return typeToBeRemoved;
    }

    /**
     * Reduces the quantity of a specified material in the warehouse by a given amount.
     * @param type The type of material whose quantity is to be reduced.
     * @param quantity The amount by which the material's quantity is to be reduced.
     * @return The amount by which the quantity was reduced.
     * @throws ExceedingCapacity If reducing the quantity would leave a negative inventory.
     * @throws InvalidQuantity If the specified quantity for reduction is invalid or exceeds the current stock.
     * @throws MaterialNotFound If the material is not found in the warehouse's inventory.
     */
    @Override
    public int dropSomeQuantity(MaterialType type, int quantity) throws ExceedingCapacity, InvalidQuantity, MaterialNotFound {
        checkInvalidQuantity(type, quantity);
        if (!warehouseMaterials.containsKey(type)) {
            throw new MaterialNotFound("The material you want to transfer is not found");
        } else {
            warehouseMaterials.put(type, warehouseMaterials.get(type) - quantity);
        }
        return quantity;
    }

    /**
     * Transfers all quantity of a specified material to another warehouse.
     * The entire quantity of the material is removed from this warehouse and added to the destination warehouse.
     * @param toWarehouse The destination warehouse to which the material is to be transferred.
     * @param type The type of material to be transferred.
     * @throws ExceedingCapacity If the destination warehouse cannot accommodate the transferred quantity.
     * @throws MaterialAlreadyExists If the material already exists in the destination warehouse.
     * @throws InvalidQuantity If the quantity of the material is invalid.
     * @throws MaterialNotFound If the material is not found in this warehouse.
     */
    @Override
    public void transferFullMaterial(Inventory toWarehouse, MaterialType type) throws ExceedingCapacity, MaterialAlreadyExists, InvalidQuantity, MaterialNotFound {
        if (!warehouseMaterials.containsKey(type)) {
            toWarehouse.addMaterial(type, warehouseMaterials.get(type));
        } else {
            toWarehouse.updateMaterialQuantity(type, warehouseMaterials.get(type));
        }
        this.removeMaterial(type);
    }

    /**
     * Transfers a specified quantity of a material to another warehouse.
     * If the specified quantity is valid and available, it is deducted from this warehouse and added to the destination warehouse.
     * @param toWarehouse The destination warehouse to which the material is to be transferred.
     * @param type The type of material to be transferred.
     * @param quantity The quantity of the material to be transferred.
     * @return The quantity that was transferred.
     * @throws ExceedingCapacity If the transfer exceeds the capacity limits of the destination warehouse.
     * @throws InvalidQuantity If the specified quantity is invalid or not available.
     * @throws MaterialAlreadyExists If the material already exists in the destination warehouse and cannot be added.
     * @throws MaterialNotFound If the material is not found in this warehouse.
     */
    @Override
    public int transferSomeQuantityOfMaterial(Inventory toWarehouse, MaterialType type, int quantity) throws ExceedingCapacity, InvalidQuantity, MaterialAlreadyExists, MaterialNotFound {
        checkInvalidQuantity(type, quantity);
        if (!warehouseMaterials.containsKey(type)) {
            toWarehouse.addMaterial(type, quantity);
        } else {
            toWarehouse.updateMaterialQuantity(type, quantity);
        }
        this.dropSomeQuantity(type, quantity);
        return quantity;
    }

    /**
     * Retrieves the current quantity of a specified material in the warehouse.
     * @param type The material type whose quantity is to be retrieved.
     * @return The current quantity of the material.
     * @throws MaterialNotFound If the material is not found in the warehouse's inventory.
     */
    @Override
    public int getMaterialQuantity(MaterialType type) throws MaterialNotFound {
        if (!warehouseMaterials.containsKey(type)) {
            throw new MaterialNotFound("The material's quantity you want to see is not found");
        }
        return type.getMaximumCapacity();
    }

    /**
     * Lists all materials and their quantities in the warehouse.
     * @return A map of all materials and their respective quantities.
     * @throws WarehouseIsEmpty If there are no materials in the warehouse.
     */
    @Override
    public Map<MaterialType, Integer> listAllMaterials() throws WarehouseIsEmpty {
        if (warehouseMaterials.isEmpty()) {
            throw new WarehouseIsEmpty("Warehouse is empty, nothing to show");
        }
        for (MaterialType materialType : warehouseMaterials.keySet()) {
            System.out.println(materialType);
        }
        return  warehouseMaterials;
    }
}
