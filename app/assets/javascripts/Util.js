import React from "react";

const NotSpecified = <i>not specified</i>;

const getRequestParam = paramName => {
  paramName = paramName.replace(/[\[\]]/g, "\\$&");
  let regex = new RegExp("[?&]" + paramName + "(=([^&#]*)|&|#|$)"),
    results = regex.exec(window.location.href);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, " "));
};

const checkStatus = response => {
  if (response.status !== 200) {
    throw new Error(response.statusText)
  }
};

const checkStatusAndParseJson = response => {
  checkStatus(response);
  return response.json();
};

const getCSRFTokenFromCookie = () => {
  let docCookie = "; " + document.cookie;
  let parts = docCookie.split("; csrfToken=");
  if (parts.length === 2) return parts.pop().split(";").shift();
};

const fetchAvalanche = extId => {
  let url = `/api/avalanche/${extId}`;
  let editKey = getRequestParam('edit');
  if (editKey) url += `?edit=${editKey}`;
  return fetch(url).then(response => {
    return checkStatusAndParseJson(response);
  });
};

/**
 * parse a date string in the form MM-dd-yyyy to a string in the format "day, Month date, year"
 * e.g. "2017-04-12" parses to "Wednesday, April 12, 2017"
 */
const parseApiDateString = dateString => {
  const options = { timeZone: 'UTC', weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  return new Date(dateString).toLocaleDateString('en-US', options);
};

const avalancheUrl = (extId, editKey) => {
  let lastSlashIndex = window.location.href.lastIndexOf('/');
  let url = `${window.location.href.substring(0, lastSlashIndex)}/${extId}`;
  if (editKey) url = `${url}?edit=${editKey}`;
  return url;
};

const constructImageUrl = (s3Bucket, avalanche, image) => {
  return `//${s3Bucket}.s3.amazonaws.com/avalanches/${avalanche.extId}/images/${image.filename}`;
};

/**
 * Returns the first found object in the array with a value field equal to code.
 * Returns empty string if no matching object is found.
 */
const labelForDataCode = (array, code) => {
  let match = array.find(obj => { return obj.value === code });
  return match ? match.label : NotSpecified;
};

const compositeLabelForDataCode = (array, code) => {
  let match = array.find(obj => { return obj.value === code });
  return match ? `${match.value} - ${match.label}` : NotSpecified;
};

const metersToFeet = (meters) => {
  return Math.round(meters * 3.28084);
};

module.exports = {
  NotSpecified,
  fetchAvalanche,
  getRequestParam,
  checkStatus,
  checkStatusAndParseJson,
  getCSRFTokenFromCookie,
  parseApiDateString,
  avalancheUrl,
  constructImageUrl,
  labelForDataCode,
  compositeLabelForDataCode,
  metersToFeet,
};