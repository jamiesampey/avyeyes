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

};