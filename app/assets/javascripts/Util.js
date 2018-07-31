module.exports = {

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
   * e.g. "04-12-2017" parses to "Wednesday, April 12, 2017"
   */
  parseApiDateString: (dateString) => {
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
  },
};