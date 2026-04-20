package com.example.data.repository

import com.example.data.repository.local.LocalDataSource
import com.example.domain.repository.TagRepository
import javax.inject.Inject

class TagRepositoryImpl  @Inject constructor(
    localDataSource: LocalDataSource
) : TagRepository{


}