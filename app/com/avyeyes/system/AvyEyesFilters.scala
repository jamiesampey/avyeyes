package com.avyeyes.system

import javax.inject.Inject

import play.api.http.DefaultHttpFilters
import play.filters.csrf.CSRFFilter

class AvyEyesFilters @Inject() (csrfFilter: CSRFFilter) extends DefaultHttpFilters(csrfFilter)
