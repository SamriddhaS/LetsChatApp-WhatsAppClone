package com.samriddha.letschartapp.models

data class Messages(
    val message:String,
    val from:String,
    val type:String
)
{
    //Need This Empty Constructor To Work With FirebaseRecyclerView Adapter
    constructor():this("","","")
}
