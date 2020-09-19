package com.samriddha.letschartapp.others

object Constants {

    const val DATABASE_PATH_NAME_ALL_USERS = "All_Users" //Key used for saving users information inside firebaseDb
    const val DATABASE_PATH_GROUPS = "Groups" //Key used for saving information of all the groups that are created inside firebaseDb
    const val DATABASE_PATH_ALL_REQUESTS = "All_Requests" //For saving all the chat/friend request that are send or received
    const val USER_DP_STORAGE_REF_PATH = "ProfileImages" //Folder Name used for saving all the profile images of all the users inside firebaseStorage.
    const val DATABASE_PATH_ALL_CONTACTS = "All_Contacts"
    const val DATABASE_PATH_ALL_MESSAGES = "All_Messages"
    const val DATABASE_PATH_NOTIFICATIONS = "Notifications"

    const val ALL_USER_KEY_USER_NAME = "name" //Key For saving user's name.
    const val ALL_USER_KEY_USER_UID = "uid" //Key For saving user's uid .
    const val ALL_USER_KEY_USER_ABOUT = "about" //Key For saving user's about section.
    const val ALL_USER_KEY_USER_DP = "profile_pic" // Key For saving user's profile dp

    const val DATABASE_KEY_MESSAGE = "message" //Key for saving the message that us send by user.
    const val DATABASE_KEY_MSG_DATE = "date" //Key for recording sending date of the message.
    const val DATABASE_KEY_MSG_TIME = "time" //Key for recording sending time of the message.


    const val KEY_MY_GROUPS_TO_GROUP_CHAT_ACTIVITY = "KEY_MY_GROUPS_TO_GROUP_CHAT_ACTIVITY"
    const val KEY_FIND_FRIENDS_TO_USER_PROFILE_ACTIVITY = "KEY_FIND_FRIENDS_TO_USER_PROFILE_ACTIVITY"
    const val KEY_USER_ID_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY = "KEY_USER_ID_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY"
    const val KEY_USER_NAME_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY = "KEY_USER_NAME_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY"
    const val KEY_USER_IMAGE_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY = "KEY_USER_IMAGE_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY"

    const val PICK_IMAGE_FROM_GALLERY = 1


    /*We use these constants for saving request status.
    * new -> is for user's that are new to each other.When a user sends request to another user for the first time.
    * msg_sent -> is for identifying users whom request is already send. This is used on senders end.
    * msg_received -> is for identifying user's who have send request to me.
    * request_type ->This is key that is used for identifying the request type.
    * If user is sending request to someone then value of "request_type" will be "msg_sent".
    * And if user has new message request from some other user then "request_type" will be "msg_received".
    * */
    const val SEND_REQUEST_KEY_REQ_TYPE = "request_type"
    const val SEND_REQUEST_VALUE_NEW = "new"
    const val SEND_REQUEST_VALUE_SENT = "req_sent"
    const val SEND_REQUEST_VALUE_RECEIVED = "req_received"
    const val SEND_REQUEST_VALUE_FRIENDS = "friends"


    const val ALL_CONTACTS_KEY_CONTACTS = "contacts"
    const val CONTACTS_VALUE_SAVED = "saved"

    const val ALL_MESSAGES_KEY_MSG_TYPE = "type"
    const val ALL_MESSAGES_KEY_FROM = "from"
    const val MSG_TYPE_VALUE_TEXT_MESSAGE = "text"

    const val NOTIFICATION_VALUE_NOTIFICATION_TYPE = "request"


    //Shared Pref Constants
    const val SHARED_PREF_NAME = "SHARED_PREF_NAME"
    const val KEY_DEVICE_TOKEN = "device_token"
}