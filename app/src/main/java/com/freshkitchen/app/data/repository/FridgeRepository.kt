package com.freshkitchen.app.data.repository

import com.freshkitchen.app.data.model.Ingredient

interface FridgeRepository {
    suspend fun observeIngredients(): List<Ingredient>
}

class FridgeRepositoryImpl : FridgeRepository {
    override suspend fun observeIngredients(): List<Ingredient> = emptyList()
}
