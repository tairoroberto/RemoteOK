package com.remotejobs.io.app.home.view

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.instantapps.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import com.remotejobs.io.app.data.dao.FavoritesDao
import com.remotejobs.io.app.home.R
import com.remotejobs.io.app.home.di.HomeModuleInjector
import com.remotejobs.io.app.home.viewmodel.HomeViewModel
import com.remotejobs.io.app.home.viewmodel.HomeViewModelFactory
import com.remotejobs.io.app.model.Job
import com.remotejobs.io.app.utils.extension.*
import com.remotejobs.io.app.view.detail.DetailActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : androidx.fragment.app.Fragment() {
    @Inject
    lateinit var homeViewModelFactory: HomeViewModelFactory

    @Inject
    lateinit var favoritesDao: FavoritesDao

    companion object {
        const val SEARCH_PARAM = "search"
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, homeViewModelFactory).get(HomeViewModel::class.java)
    }

    private val list: MutableList<Job> = ArrayList()

    private lateinit var adapter: HomeRecyclerAdapter

    private lateinit var adapterSearch: SearchAdapter

    private val suggestions: MutableList<String> = ArrayList()

    private val filteredValues = ArrayList<String>()

    private lateinit var tracker: FirebaseAnalytics

    override fun onAttach(context: Context?) {
        HomeModuleInjector.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resources.getStringArray(R.array.suggestions).forEach { suggestions.add(it) }
        adapterSearch = SearchAdapter(activity, suggestions, favoritesDao)
        adapter = HomeRecyclerAdapter(activity, list, this::onItemClick)
        setHasOptionsMenu(true)
        retainInstance = true

        observeLoadingStatus()
        //observeErrorStatus()
        observeResponse()
        getAllJobs()
        tracker = FirebaseAnalytics.getInstance(context as Context)

        if (InstantApps.isInstantApp(context as Context)) {
            tracker.logEvent("home_intantapp", null)
        } else {
            tracker.logEvent("home", null)
        }

        if (arguments?.get(SEARCH_PARAM) != null) {
            showProgress(true)
            viewModel.search("remote-${arguments?.get(SEARCH_PARAM)}-jobs")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setAnimation()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerViewListJobs()

        listSearch.setOnItemClickListener { _, _, position, _ ->
            showProgress(true)
            viewModel.search("remote-${filteredValues[position]}-jobs")
            listSearch.visibility = GONE
            activity?.hideSoftKeyboard()
        }

        if (!InstantApps.isInstantApp(context as Context)) {
            showAlertDialogHateUs()
        }
    }

    private fun setRecyclerViewListJobs() {
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener { getAllJobs() }
    }

    private fun getAllJobs() {
        viewModel.getAllJobs()
    }

    private fun observeLoadingStatus() {
        viewModel.getLoadingStatus().observe(this, Observer { isLoading -> showProgress(isLoading) })
    }

    private fun observeErrorStatus() {
        viewModel.getErrorStatus().observe(this, Observer { msg -> showSnackBarError(msg.toString()) })
    }

    private fun observeResponse() {
        viewModel.getResponse().observe(this, androidx.lifecycle.Observer { response -> showJobsList(response) })
    }

    private fun setAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val changeBounds = ChangeBounds()
            changeBounds.duration = 2000
            activity?.window?.sharedElementExitTransition = changeBounds
        }
    }

    private fun showProgress(b: Boolean?) {
        activity?.showProgress(recyclerView, progress, b == true)
        swipeRefreshLayout?.isRefreshing = false
    }

    private fun showSnackBarError(str: String) {
        activity?.showSnackBarError(recyclerView, str)
        swipeRefreshLayout?.isRefreshing = false
    }

    private fun showJobsList(jobs: List<Job>?) {

        withoutData?.visibility = GONE
        if (jobs == null || jobs.isEmpty()) {
            withoutData?.visibility = VISIBLE
        }

        adapter.update(jobs)
        swipeRefreshLayout?.isRefreshing = false
        showProgress(false)
    }

    private fun onItemClick(job: Job, imageView: ImageView) {

        val options: ActivityOptionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(context as Activity, Pair.create(imageView, "image"))

        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra("job", job)
        activity?.startActivity(intent, options.toBundle())
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu)
        setUpSearchView(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setUpSearchView(menu: Menu?) {

        val searchView = MenuItemCompat.getActionView(menu?.findItem(R.id.search)) as SearchView
        searchView.setIconifiedByDefault(false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.search("remote-$query-jobs")
                activity?.hideSoftKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {

                if (newText.isBlank() || newText.length < 2) {
                    listSearch?.visibility = GONE
                    return false
                }

                filteredValues.clear()
                resources.getStringArray(R.array.suggestions).forEach {
                    if (it.toLowerCase().startsWith(newText.toLowerCase())) {
                        filteredValues.add(it)
                    }
                }

                if (filteredValues.size == 0) {
                    listSearch?.visibility = GONE
                }

                adapterSearch.updateItems(filteredValues)
                listSearch?.visibility = VISIBLE
                listSearch?.adapter = adapterSearch

                return true
            }
        })
        MenuItemCompat.setOnActionExpandListener(menu?.findItem(R.id.search),
                object : MenuItemCompat.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                        searchView.requestFocus()
                        activity?.showSoftKeyboard()
                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                        activity?.hideSoftKeyboard()
                        return true
                    }
                })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search) {
            return super.onOptionsItemSelected(item)
        }

        if (item.itemId == R.id.favorites) {
            startActivity(Intent(context, FavoritesActivity::class.java))
        }


        if (item.itemId == R.id.donate) {
            //activity?.browse("https://www.paypal.com/your_paypal")
            activity?.alert {
                title = "Donate"
                message = "Option is disable for now"
                yesButton { }
            }?.show()
        }

        return true
    }

    private fun showAlertDialogHateUs() {
        val rated = activity?.getSharedPreferences("Home", MODE_PRIVATE)?.getBoolean("rated", false)
        if (rated != true) {
            activity?.alert {
                title = "Rate us"
                message = "Rate us and help us improve with new features for the community, give us 5 stars to help in the disclosure, but leave a comment to improve the app :)"

                noButton {}
                yesButton {
                    activity?.launchPlayStore()
                    activity?.getSharedPreferences("Home", MODE_PRIVATE)?.edit()?.putBoolean("rated", true)?.apply()
                }

            }?.show()
        }
    }
}
