import requests, json

class Geocoder:
  def geocode(self, address):
    url = "http://where.yahooapis.com/geocode?q=" + address.replace(' ', '%20') + \
      "&flags=J&appid=UmMtXR7c"
    res = requests.get(url)
    data = json.loads(res.text)
    return data['ResultSet']['Results'][0]

if __name__ == "__main__":
  geocoder = Geocoder()
  latlon = geocoder.geocode('Irvine, California')
  print latlon['latitude'], latlon['longitude']
