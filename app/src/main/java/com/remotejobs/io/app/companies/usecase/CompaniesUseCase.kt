package com.remotejobs.io.app.companies.usecase

import com.remotejobs.io.app.interfaces.CompaniesLocalRepository
import com.remotejobs.io.app.interfaces.CompaniesRemoteRepository
import com.remotejobs.io.app.model.CompaniesResponse
import com.remotejobs.io.app.model.Company
import com.remotejobs.io.app.model.Job
import com.remotejobs.io.app.model.JobsResponse
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by tairo on 12/11/17.
 */
class CompaniesUseCase(private val companiesLocalRepository: CompaniesLocalRepository,
                       private val companiesRemoteRepository: CompaniesRemoteRepository
) {

    fun listAllCompanies(): Single<CompaniesResponse> {
        return companiesRemoteRepository.getCompanies()
    }

    fun listCompaniesJobs(company: String, lastItem: String?): Single<JobsResponse> {
        return companiesRemoteRepository.getCompaniesJobs(company, lastItem)
    }

    fun listCompaniesFromBD(): Flowable<List<Company>> {
        return companiesLocalRepository.getAll()
    }

    fun listCompaniesJobsFromBD(company: String): Flowable<List<Job>> {
        return companiesLocalRepository.getAllByCompany(company)
    }

    fun addAllCompanies(companies: List<Company>?) {
        companiesLocalRepository.addAll(companies)
    }

    fun deleteAllCompanies() {
        companiesLocalRepository.deleteAll()
    }
}
