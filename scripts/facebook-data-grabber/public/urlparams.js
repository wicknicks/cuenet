function getURLParams () {
  if (window.location.href.indexOf('?') == -1 && window.location.href.indexOf('&') == -1)
    return [];
  
    return getParams(window.location.href);
}

function getParams(url) {
  var params = [], hash;
  var hashes = url.slice(url.indexOf('?') + 1).split('&');
  for (var i=0; i < hashes.length; i++) {
    hash = hashes[i].split('=');
    params.push(hash[0]);
    params[hash[0]]=hash[1];
  }
  
  return params;
}
