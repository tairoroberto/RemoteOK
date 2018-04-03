package com.remoteok.io.app.data.highestpaid

import com.remoteok.io.app.data.dao.HighestPaidDao
import com.remoteok.io.app.data.dao.JobsDao
import com.remoteok.io.app.domain.highestpaid.HighestPaidLocalRepository
import com.remoteok.io.app.model.HighestPaid
import com.remoteok.io.app.model.Job
import io.reactivex.Flowable

/**
 * Created by tairo on 1/6/18 10:58 PM.
 */
class HighestPaidLocalDataStore(private val highestPaidDao: HighestPaidDao, private val jobsDao: JobsDao) : HighestPaidLocalRepository {
    override fun add(highestPaid: HighestPaid): Long {
        return highestPaidDao.add(highestPaid)
    }

    override fun addAll(highestPaidSalaries: List<HighestPaid>?) {
        highestPaidDao.addAll(highestPaidSalaries)
    }

    override fun update(highestPaid: HighestPaid) {
        highestPaidDao.update(highestPaid)
    }

    override fun delete(highestPaid: HighestPaid) {
        highestPaidDao.delete(highestPaid)
    }

    override fun deleteAll() {
        highestPaidDao.deleteAll()
    }

    override fun getAll(): Flowable<List<HighestPaid>> {
        return highestPaidDao.getAll()
    }

    override fun getByID(id: Int): Flowable<HighestPaid> {
        return highestPaidDao.getByID(id)
    }

    override fun getAllByTag(tag: String): Flowable<List<Job>> {
        return jobsDao.getAllByTag(tag)
    }
}