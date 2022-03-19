var map = L.map('app');

cpRPA.setDistanceFn(distance);
cpRPA.setLatlng2PxFn(latlng2Px);
cpRPA.setPx2LatlngFn(px2Latlng);

function distance(p1, p2) {
    return L.latLng(p1.lat, p1.lng).distanceTo(L.latLng(p2.lat, p2.lng))
}

function latlng2Px(latlng) {
    return map.latLngToLayerPoint(L.latLng(latlng.lat, latlng.lng))
}

function px2Latlng(px) {
    return map.layerPointToLatLng(L.point(px[0], px[1]))
}

function calcArea(latlngs) {
    var S = 0;
    for (var i = 0; i < latlngs.length; i++) {
        S += X(latlngs[i]) * Y(latlngs[si(i + 1, latlngs.length)]) - Y(latlngs[i]) * X(latlngs[si(i + 1, latlngs.length)])
    }
    return Math.abs(S) / 2

    function X(latlng) {
        return latlng.lng * lng2m(latlng);
    }

    function Y(latlng) {
        return latlng.lat * lat2m(latlng);
    }
}

function calcLineArea(latlngs, space) {
    var S = 0;
    for (var i = 0; i < latlngs.length; i += 2) {
        var j = si(i + 1, latlngs.length);
        S += distance(latlngs[i], latlngs[j]);
    }
    return S * space * 2
}

function lat2m(latlng) {
    return distance(latlng, {
        lat: latlng.lat + 1,
        lng: latlng.lng
    })
}

function lng2m(latlng) {
    return distance(latlng, {
        lat: latlng.lat,
        lng: latlng.lng + 1
    });
}

function si(i, l) {
    if (i > l - 1) {
        return i - l;
    }
    if (i < 0) {
        return l + i;
    }
    return i;
}