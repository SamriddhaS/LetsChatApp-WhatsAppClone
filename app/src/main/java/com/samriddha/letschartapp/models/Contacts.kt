package com.samriddha.letschartapp.models

data class Contacts(
    val about:String,
    val name:String,
    val profile_pic:String
)
{
    //Need This Empty Constructor To Work With FirebaseRecyclerView Adapter
    constructor():this("","","")
}
