var latitude = 42.33141;
var longitude = -71.099396;
var key = "f0aa9402-a45c-4e90-86b2-acaab8076bc3";

function requestJson(lat, lng, map){
	var quest = "http://api.tripadvisor.com/api/partner/2.0/map/"+lat+","+lng+"?key="+key;

	var xhr = new XMLHttpRequest();

	xhr.open("GET", quest, false);

	xhr.send();

	var json = JSON.parse(xhr.responseText);

	//alert(json.data[0].address_obj.street1);

	var stopLat = parseFloat(json.data[0].latitude);
	var stopLng = parseFloat(json.data[0].longitude) ;

	//alert(json.data[0].web_url+" "+stopLat+", "+stopLng);

	var stopMarker = new google.maps.Marker({
		position: {lat: stopLat, lng: stopLng},
		map: map,
		title: json.data[0].name,
		label: json.data[0].name
	});

	stopMarker.addListener('click', function(){
		window.open(json.data[0].web_url);
	});
}

