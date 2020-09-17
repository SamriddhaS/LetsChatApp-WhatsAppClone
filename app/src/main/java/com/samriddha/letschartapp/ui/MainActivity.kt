package com.samriddha.letschartapp.ui

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_NAME
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.adapters.ViewPagerAdapter
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_GROUPS
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity() {

    private var currentUser:FirebaseUser? = null
    private var firebaseAuth:FirebaseAuth?  = null
    private var firebaseDbRef:DatabaseReference? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setting our toolbar as default action bar.
        setSupportActionBar(toolbar)

        firebaseAuth=  FirebaseAuth.getInstance()
        currentUser = firebaseAuth?.currentUser
        firebaseDbRef = FirebaseDatabase.getInstance().reference

        initViewPager()
    }

    override fun onStart() {
        super.onStart()

        if (currentUser==null)
            sendUserToLoginActivity()
        else{
            verifyUser()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when(item.itemId){
            R.id.settingsMenu ->{

                sendUserToSettingsActivity(false)
                return true
            }
            R.id.logoutMenu -> {

                //Sign Out User
                firebaseAuth?.signOut()
                sendUserToLoginActivity()
                return true
            }
            R.id.findFriendsMenu ->{

                startActivity(Intent(this,FindFriendsActivity::class.java))
                return true
            }
            R.id.createGroupMenu ->{

                createNewGroup()
                return true
            }

        }

        return false

    }

    private fun createNewGroup() {

        val dialogBuilder = AlertDialog.Builder(this,R.style.AlertDialogStyle)
        dialogBuilder.setTitle("Enter Group Name")

        val etGroupName = EditText(this)
        etGroupName.hint = "e.g: School Friends"

        dialogBuilder.setView(etGroupName)
        dialogBuilder
            .setPositiveButton("Create", DialogInterface.OnClickListener { _,_ ->

            val groupName = etGroupName.text.toString()
            if (groupName.isEmpty())
                Toast.makeText(this,"Enter Group Name",Toast.LENGTH_SHORT).show()
            else{
                createGroup(groupName)
            }
        })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog , _ ->

                dialog.cancel()
            })

        dialogBuilder.show()

    }

    private fun createGroup(groupName:String) {

        firebaseDbRef
            ?.child(DATABASE_PATH_GROUPS)
            ?.child(groupName)
            ?.setValue("")
            ?.addOnSuccessListener {
                Toast.makeText(this,"$groupName Is Created",Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {
                Toast.makeText(this,"Error: ${it.message}",Toast.LENGTH_SHORT).show()

            }

    }

    private fun verifyUser() {

        val currentUserId = firebaseAuth?.currentUser?.uid

        firebaseDbRef
            ?.child(DATABASE_PATH_NAME_ALL_USERS)
            ?.child(currentUserId!!)
            ?.addValueEventListener(object : ValueEventListener{

                override fun onCancelled(error: DatabaseError) {


            }
                override fun onDataChange(snapshot: DataSnapshot) {

                if (!(snapshot.child(ALL_USER_KEY_USER_NAME).exists())){
                    sendUserToSettingsActivity(true)
                }

            }
        })


    }

    private fun sendUserToLoginActivity() {

        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))

        finish()
    }

    private fun sendUserToSettingsActivity(finishMainActivity:Boolean) {

        if (finishMainActivity){
            startActivity(Intent(this,
                SettingsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }else{
            startActivity(Intent(this,
                SettingsActivity::class.java))
        }


    }

    private fun initViewPager(){
        tabLayout.setupWithViewPager(viewPager)

        val contactsFragment = ContactsFragment()
        val chatsFragment = ChatsFragment()
        val myGroupsFragment = MyGroupsFragment()

        val viewPagerAdapter =
            ViewPagerAdapter(
                supportFragmentManager,
                0
            )
        viewPagerAdapter.addFragment(chatsFragment,"Chats")
        viewPagerAdapter.addFragment(myGroupsFragment,"MyGroups")
        viewPagerAdapter.addFragment(contactsFragment,"Contacts")

        viewPager.adapter = viewPagerAdapter
//
//        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_chat_24)
//        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_groups)
//        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_contacts)

    }


}