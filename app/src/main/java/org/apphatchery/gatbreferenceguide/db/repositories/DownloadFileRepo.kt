package org.apphatchery.gatbreferenceguide.db.repositories

import org.apphatchery.gatbreferenceguide.api.API
import org.apphatchery.gatbreferenceguide.di.RetrofitDownloadClientAPI
import javax.inject.Inject


class DownloadFileRepo @Inject constructor(
    @RetrofitDownloadClientAPI val api: API
)