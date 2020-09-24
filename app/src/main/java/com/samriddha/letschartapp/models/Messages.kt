package com.samriddha.letschartapp.models

data class Messages(
    val message:String,
    val from:String,
    val type:String,
    val to:String,
    val message_id:String,
    val message_date:String,
    val message_time:String,
    val fileName:String
)
{
    //Need This Empty Constructor To Work With FirebaseRecyclerView Adapter
    constructor():this("","","","","","","","")

}
