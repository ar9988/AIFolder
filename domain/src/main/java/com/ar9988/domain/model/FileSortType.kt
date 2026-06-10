package com.ar9988.domain.model

enum class FileSortType {
    Recent,Name,Size
}

fun FileSortType.toName() : String{
    return when(this){
        FileSortType.Recent -> "최신순"
        FileSortType.Name -> "이름순"
        FileSortType.Size -> "크기순"
    }
}