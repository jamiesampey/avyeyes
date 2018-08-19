import React from "react";

const NotSpecified = <i>not specified</i>;

module.exports = {
  notSpecified: NotSpecified,

  getRequestParam: paramName => {
    paramName = paramName.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + paramName + "(=([^&#]*)|&|#|$)"),
    results = regex.exec(window.location.href);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
  },

  parseApiResponse: (response) => {
    if (response.status === 200) {
      return response.json();
    } else {
      throw new Error(response.statusText)
    }
  },

  /**
   * parse a date string in the form MM-dd-yyyy to a string in the format "day, Month date, year"
   * e.g. "2017-04-12" parses to "Wednesday, April 12, 2017"
   */
  parseApiDateString: (dateString) => {
    const options = { timeZone: 'UTC', weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
  },

  constructImageUrl(s3Bucket, avalanche, image) {
    return `//${s3Bucket}.s3.amazonaws.com/avalanches/${avalanche.extId}/images/${image.filename}`;
  },

  /**
   * Returns the first found object in the array with a value field equal to code.
   * Returns empty string if no matching object is found.
   */
  labelForDataCode: (array, code) => {
    let match = array.find(obj => { return obj.value === code });
    return match ? match.label : NotSpecified;
  },

  compositeLabelForDataCode: (array, code) => {
    let match = array.find(obj => { return obj.value === code });
    return match ? `${match.value} - ${match.label}` : NotSpecified;
  },

  metersToFeet: (meters) => {
    return Math.round(meters * 3.28084);
  },
};