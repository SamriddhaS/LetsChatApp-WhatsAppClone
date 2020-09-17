package com.samriddha.letschartapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager, behaviour:Int) : FragmentPagerAdapter(fm,behaviour) {

    private var fragmentList:ArrayList<Fragment> = ArrayList()
    private var fragmentNames:ArrayList<String> = ArrayList()


    override fun getItem(position: Int): Fragment {

        return fragmentList[position]
    }

    override fun getCount(): Int {

        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentNames[position]
    }

    fun addFragment(fragment: Fragment,fragmentTitle:String){
        fragmentList.add(fragment)
        fragmentNames.add(fragmentTitle)
    }

}