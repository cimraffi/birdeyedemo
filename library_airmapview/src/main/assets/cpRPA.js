(function(root, factory) {
    if (typeof define === 'function' && define.amd) {
        define([], factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        root.cpRPA = factory();
    }
}(this, function() {
    function Util() {}

    function setOptions(opt) {
        if (!(opt.polygon instanceof Array)) {
            throw new Error('cpRPA: the "polygon" of options must be a Array like [{lat:Number,lng:Number}]')
            return
        }
        if (opt.rotate && typeof opt.rotate !== 'number') {
            throw new Error('cpRPA: the "rotate" of options must be a number!');
            return
        }
        if (opt.space && typeof opt.space !== 'number') {
            throw new Error('cpRPA: the "space" of options must be a number!');
            return
        }

        res = [];
        res.push(opt.polygon[0]);
        res.push(opt.polygon[1]);

        starti = 0
        var U = new Util();
        p_1 = U.latlng2Px(opt.polygon[starti]);
        p_2 = U.latlng2Px(opt.polygon[starti+1]);

        l = genLineSlope2(p_1, p_2);

        var bounds = createPolygonBounds(opt.polygon);
        r = l?Math.atan(l)*180/Math.PI:90;

        lats = []
        var tmp_rPolygon = createRotatePolygon(opt.polygon, bounds, -r);
        //return tmp_rPolygon;

        for (var i = 0; i < tmp_rPolygon.length; i++) {
            lats.push(tmp_rPolygon[i].lat);
        }
        var maxLat = Math.max.apply(Math, lats);
        var rrr = 0;
        if (maxLat > tmp_rPolygon[starti].lat){
            rrr=180
        }

        var rPolygon = createRotatePolygon(opt.polygon, bounds, rrr-r);
        if (!rPolygon) {
            throw new Error('cpRPA: You must call ".setLatlng2PxFn" and ".setPx2LatlngFn" methods before setOptions ');
            return
        }

        var rBounds = createPolygonBounds(rPolygon);
        var latline = createLats(rBounds, opt.space || 5);

        var line = [];
        var polyline = [];
        var check = null;
        var pointIndex = 0;
        for (var i = 0; i < latline.len; i++) {
            line = [];
            for (var j = 0; j < rPolygon.length; j++) {
                var nt = si(j + 1, rPolygon.length)
                check = createInlinePoint(
                    [rPolygon[j].lng, rPolygon[j].lat], [rPolygon[nt].lng, rPolygon[nt].lat],
                    rBounds.northLat - i * latline.lat
                );
                if (check) {
                    line.push(check)
                }
            }

            if (line.length < 2) {
                continue;
            }

            if (line[0][0] === line[1][0]) {
                continue;
            }
            
            //if(i == 0) {
                //console.log([rPolygon[j].lng, rPolygon[j].lat]);
                //console.log([rPolygon[nt].lng, rPolygon[nt].lat]);
                //console.log(line);
            //}
            //console.log(line);

            // 规划航线
            if (pointIndex % 2 == 1) {
                polyline.push({ lat: line[0][1],
                                lng: line[0][0] },
                              { lat: line[1][1],
                                lng: line[1][0] })
            } else {
                polyline.push({ lat: line[1][1],
                                lng: line[1][0] },
                              { lat: line[0][1],
                                lng: line[0][0] })
            }
            pointIndex ++;
        }
        //console.log('polyline :');
        //console.log(polyline);
        return createRotatePolygon(polyline, bounds, rrr+r)
        
        /*
        var bounds = createPolygonBounds(opt.polygon);
        console.log('bounds :');
        console.log(bounds);
        var rPolygon = createRotatePolygon(opt.polygon, bounds, -opt.rotate || 0);
        console.log('rPolygon :');
        console.log(rPolygon);

        if (!rPolygon) {
            throw new Error('cpRPA: You must call ".setLatlng2PxFn" and ".setPx2LatlngFn" methods before setOptions ');
            return
        }

        var rBounds = createPolygonBounds(rPolygon);
        console.log('rBounds :');
        console.log(rBounds);
        var latline = createLats(rBounds, opt.space || 5);
        console.log('latline :');
        console.log(latline);

        var line = [];
        var polyline = [];
        var check = null;
        for (var i = 0; i < latline.len; i++) {
            line = [];
            for (var j = 0; j < rPolygon.length; j++) {
                var nt = si(j + 1, rPolygon.length)
                check = createInlinePoint(
                    [rPolygon[j].lng, rPolygon[j].lat], [rPolygon[nt].lng, rPolygon[nt].lat],
                    rBounds.northLat - i * latline.lat
                );
                if (check) {
                    line.push(check)
                }
            }

            if (line.length < 2) {
                continue;
            }

            if (line[0][0] === line[1][0]) {
                continue;
            }

            if (i % 2) {
                polyline.push({
                    lat: line[0][1],
                    lng: Math.max(line[0][0], line[1][0]),
                }, {
                    lat: line[0][1],
                    lng: Math.min(line[0][0], line[1][0])
                })
            } else {
                polyline.push({
                    lat: line[0][1],
                    lng: Math.min(line[0][0], line[1][0]),
                }, {
                    lat: line[0][1],
                    lng: Math.max(line[0][0], line[1][0])
                })
            }
        }
        console.log('polyline :');
        console.log(polyline);
        return createRotatePolygon(polyline, bounds, opt.rotate || 0)
*/
    }

    function setDistanceFn(fn) {
        if (typeof fn !== 'function') {
            throw new Error('setDistanceFn\'s argument must be a function');
            return
        }
        Util.prototype.distance = fn;
    }

    function setLatlng2PxFn(fn) {
        if (typeof fn !== 'function') {
            throw new Error('setLatlng2PxFn\'s argument must be a function');
            return
        }
        Util.prototype.latlng2Px = fn;
    }

    function setPx2LatlngFn(fn) {
        if (typeof fn !== 'function') {
            throw new Error('setPx2LatlngFn\'s argument must be a function');
            return
        }
        Util.prototype.px2Latlng = fn;
    }

    function transform(x, y, tx, ty, deg, sx, sy) {
        var deg = deg * Math.PI / 180;
        if (!sy) sy = 1;
        if (!sx) sx = 1;
        return [
            sx * ((x - tx) * Math.cos(deg) - (y - ty) * Math.sin(deg)) + tx,
            sy * ((x - tx) * Math.sin(deg) + (y - ty) * Math.cos(deg)) + ty
        ]
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

    function createInlinePoint(p1, p2, y) {
        var s = p1[1] - p2[1];
        var x;
        if (s) {
            x = (y - p1[1]) * (p1[0] - p2[0]) / s + p1[0]
        } else {
            return false
        }
        if (x > p1[0] && x > p2[0]) {
            return false
        }
        if (x < p1[0] && x < p2[0]) {
            return false
        }
        return [x, y]
    }
    
    function createInlinePoint2(p1, p2, l) {
        var s = p1[1] - p2[1];
        var x;
        if (s) {
            x = (y - p1[1]) * (p1[0] - p2[0]) / s + p1[0]
        } else {
            return false
        }
        if (x > p1[0] && x > p2[0]) {
            return false
        }
        if (x < p1[0] && x < p2[0]) {
            return false
        }
        return [x, y]
    }

    function createPolygonBounds(latlngs) {
        var lats = [];
        var lngs = [];
        for (var i = 0; i < latlngs.length; i++) {
            lats.push(latlngs[i].lat);
            lngs.push(latlngs[i].lng);
        }
        var maxLat = Math.max.apply(Math, lats);
        var maxLng = Math.max.apply(Math, lngs);
        var minLat = Math.min.apply(Math, lats);
        var minLng = Math.min.apply(Math, lngs);

        return {
            center: {
                lat: (maxLat + minLat) / 2,
                lng: (maxLng + minLng) / 2
            },
            latlngs: [{
                lat: maxLat,
                lng: minLng
            }, {
                lat: maxLat,
                lng: maxLng
            }, {
                lat: minLat,
                lng: maxLng
            }, {
                lat: minLat,
                lng: minLng
            }],
            northLat: maxLat
        }
    }

    function createRotatePolygon(latlngs, bounds, rotate) {
        var U = new Util();
        if (typeof U.latlng2Px !== 'function' && typeof U.px2Latlng !== 'function') {
            return false
        }
        var res = [],
            a, b;
        var c = U.latlng2Px(bounds.center);
        for (var i = 0; i < latlngs.length; i++) {
            a = U.latlng2Px(latlngs[i]);
            b = transform(a.x, a.y, c.x, c.y, rotate);
            d = U.px2Latlng(b);
            res.push(U.px2Latlng(b));
        }
        return res;
    }

    function createLats(bounds, space) {
        var nw = bounds.latlngs[0];
        var sw = bounds.latlngs[3];
        var U = new Util();
        if (typeof U.distance !== 'function') {
            throw new Error('You must call the ".setDistanceFn" method and set a function to calculate the distance!');
            return false
        }

        var distance = U.distance({
            lat: nw.lat,
            lng: nw.lng
        }, {
            lat: sw.lat,
            lng: sw.lng
        });
        var steps = parseInt(distance / space );
        var lats = (nw.lat - sw.lat) / steps;
        return {
            len: steps,
            lat: lats
        }
    }

    function getPolygonArea(polygon) {
        var S = 0;
        for (var i = 0; i < polygon.length; i++) {
            S += X(polygon[i]) * Y(polygon[si(i + 1, polygon.length)]) - Y(polygon[i]) * X(polygon[si(i + 1, polygon.length)])
        }
        return Math.abs(S) / 2

        function X(latlng) {
            return latlng.lng * lng2m(latlng);
        }

        function Y(latlng) {
            return latlng.lat * lat2m(latlng);
        }
    }

    function getPolylineArea(polyline, space) {
        var S = 0;
        space = space || 5
        var U = new Util();
        for (var i = 0; i < polyline.length; i += 2) {
            var j = si(i + 1, polyline.length);
            S += U.distance(polyline[i], polyline[j]);
        }
        return S * space * 2
    }

    function lat2m(latlng) {
        var U = new Util();
        return U.distance(latlng, {
            lat: latlng.lat + 1,
            lng: latlng.lng
        })
    }

    function lng2m(latlng) {
        var U = new Util();
        return U.distance(latlng, {
            lat: latlng.lat,
            lng: latlng.lng + 1
        });
    }

    function genLineSlope(p1, p2) {
        var slope = 0;
        var intercept = 0;
        var p_l, p_r;
        if (p2.lng == p1.lng) {
            return {};
        }
        if (p2.lng > p1.lng) {
            p_l = p1;
            p_r = p2;
        } else {
            p_l = p2;
            p_r = p1;
        }
        slope = (p_r.lat - p_l.lat) / (p_r.lng - p_l.lng);
        return slope;
    }
    
    function genLineSlope2(p1, p2) {
        var slope = 0;
        var p_l, p_r;
        if (p2.x == p1.x) {
            return;
        }
        if (p2.x > p1.x) {
            p_l = p1;
            p_r = p2;
        } else {
            p_l = p2;
            p_r = p1;
        }
        slope = (p_r.y - p_l.y) / (p_r.x - p_l.x);
        return slope;
    }
    
    function moveLine(l, d) {
        return {
            slope: l.slope, 
            intercept: l.intercept + d
        }
    }

    return {
        setOptions: setOptions,
        setDistanceFn: setDistanceFn,
        setLatlng2PxFn: setLatlng2PxFn,
        setPx2LatlngFn: setPx2LatlngFn,
        getPolygonArea: getPolygonArea,
        getPolylineArea: getPolylineArea
    }
}));