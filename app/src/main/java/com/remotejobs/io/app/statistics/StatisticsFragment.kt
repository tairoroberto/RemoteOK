package com.remotejobs.io.app.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.remotejobs.io.app.R

/**
 * A simple [Fragment] subclass.
 */
class StatisticsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

}// Required empty public constructor
