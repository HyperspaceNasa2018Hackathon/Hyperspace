package team.hyperspace.firewatcher

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import team.hyperspace.firewatcher.firemonitor.FireMonitorFragment
import team.hyperspace.firewatcher.firereporter.FireReportFragment
import team.hyperspace.firewatcher.firhistory.FireHistoryFragment
import team.hyperspace.firewatcher.outsourcing.OutsourcingFrgament

class FireWatcherActivity : AppCompatActivity() {

    private lateinit var viewPager : ViewPager
    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire_watcher)
        viewPager = initViewPager()
        viewPager.offscreenPageLimit = 1
        initTabLayout()
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        toolbar.setLogo(R.drawable.toolbar_icon)
        if (menu != null) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_map_type, menu)
            initSearchView(menu)
            return true
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            when(viewPager.currentItem) {
                0 -> {
                    menu.findItem(R.id.action_search).isVisible = true
                    menu.findItem(R.id.action_map_type).isVisible = true
                    menu.findItem(R.id.action_pick_date).isVisible = false
                    toolbar.setLogo(R.drawable.fire_icon)
                    toolbar.setTitle(R.string.title_activity_fire_watcher)
                }
                1 -> {
                    menu.findItem(R.id.action_search).isVisible = false
                    menu.findItem(R.id.action_map_type).isVisible = false
                    menu.findItem(R.id.action_pick_date).isVisible = false
                    toolbar.setLogo(R.drawable.toolbar_icon)
                    toolbar.setTitle(R.string.account_name)
                }
                2 -> {
                    menu.findItem(R.id.action_search).isVisible = true
                    menu.findItem(R.id.action_map_type).isVisible = true
                    menu.findItem(R.id.action_pick_date).isVisible = true
                    toolbar.setLogo(R.drawable.fire_icon)
                    toolbar.setTitle(R.string.title_activity_fire_watcher)
                }
                3 -> {
                    menu.findItem(R.id.action_search).isVisible = false
                    menu.findItem(R.id.action_map_type).isVisible = false
                    menu.findItem(R.id.action_pick_date).isVisible = false
                    toolbar.setLogo(R.drawable.toolbar_icon)
                    toolbar.setTitle(R.string.account_name)
                }
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun initSearchView(menu: Menu) {
        val searchItem : MenuItem = menu.findItem(R.id.action_search)
        val searchView : SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(str: String?): Boolean {
                Log.d(FireMonitorFragment.TAG, "onQueryTextSubmit $str")
                passSearchStringToFragment(str)
                searchView.onActionViewCollapsed()
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(str: String?): Boolean {
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item : MenuItem?) : Boolean {
        val fragment : Fragment? = getSelectedFragment()
        if (fragment is FireMonitorFragment) {
            return fragment.onOptionsItemSelected(item)
        } else if (fragment is FireHistoryFragment) {
            return fragment.onOptionsItemSelected(item)
        }
        return false
    }

    private fun passSearchStringToFragment(str: String?) {
        val fragment : Fragment? = getSelectedFragment()
        if (fragment is FireMonitorFragment) {
            fragment.onSearchStringUpdate(str)
        } else if (fragment is FireHistoryFragment) {
            fragment.onSearchStringUpdate(str)
        }
    }

    private fun getSelectedFragment() : Fragment? {
        val adapter : ViewPagerAdapter = viewPager.adapter as ViewPagerAdapter
        return adapter.getItem(viewPager.currentItem)
    }

    private fun initViewPager() : ViewPager {
        val viewPager : ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.requestTransparentRegion(viewPager)
        return viewPager
    }

    private fun initTabLayout() {
        val tabLayout : TabLayout = findViewById(R.id.tabs)
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.fire_monitor_tab))
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.fire_report_tab))
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.fire_history_tab))
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.fire_head_tab))
        tabLayout.addOnTabSelectedListener(object : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                setTabSelected(tab, false)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager.setCurrentItem(tab.position)
                    invalidateOptionsMenu()
                    setTabSelected(tab, true)
                    val fragment = getSelectedFragment()
                    if (fragment is FireReportFragment) {
                        fragment.onPageSelected()
                    }
                }
            }

            private fun setTabSelected(tab: TabLayout.Tab?, selected : Boolean) {
                if (tab != null) {
                    val view : View? = tab.customView
                    if (view != null) {
                        view.isSelected = selected
                    }
                }
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }
        })
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
    }

    class ViewPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments : MutableMap<Int, Fragment> = mutableMapOf()

        override fun getCount(): Int {
            return 4
        }

        override fun getItem(index: Int): Fragment {
            val cached : Fragment? = fragments.get(index)
            if (cached != null) {
                return cached
            }

            var fragment : Fragment? = null
            when (index) {
                0 -> fragment =  FireMonitorFragment()
                1 -> fragment =  FireReportFragment()
                2 -> fragment = FireHistoryFragment()
                3 -> fragment = OutsourcingFrgament()
            }
            if (fragment != null) {
                fragments.put(index, fragment)
                return fragment
            } else {
                throw IllegalArgumentException()
            }
        }

        public override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            super.destroyItem(collection, position, view)
            if (fragments.contains(position)) {
                fragments.remove(position)
            }
        }
    }
}
