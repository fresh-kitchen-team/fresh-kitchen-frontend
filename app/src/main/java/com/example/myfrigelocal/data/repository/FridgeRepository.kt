package com.example.myfrigelocal.data.repository

import com.example.myfrigelocal.data.model.Ingredient

interface FridgeRepository {
    suspend fun observeIngredients(): List<Ingredient>
}

class FridgeRepositoryImpl : FridgeRepository {
    override suspend fun observeIngredients(): List<Ingredient> = emptyList()
}
