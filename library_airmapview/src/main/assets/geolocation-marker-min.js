(function(){
/*
 geolocation-marker version 2.0.5
 @copyright 2012, 2015 Chad Killingsworth
 @see https://github.com/ChadKillingsworth/geolocation-marker/blob/master/LICENSE.txt
*/
'use strict';var b,e="function"==typeof Object.create?Object.create:function(a){function c(){}c.prototype=a;return new c},g;if("function"==typeof Object.setPrototypeOf)g=Object.setPrototypeOf;else{var h;a:{var k={u:!0},l={};try{l.__proto__=k;h=l.u;break a}catch(a){}h=!1}g=h?function(a,c){a.__proto__=c;if(a.__proto__!==c)throw new TypeError(a+" is not extensible");return a}:null}var m=g;
function n(a,c,f,v){var d=google.maps.MVCObject.call(this)||this;d.c=null;d.o=null;d.D=null;d.b=null;d.a=null;d.i=-1;var p={clickable:!1,cursor:"pointer",draggable:!1,flat:!0,icon:{path:google.maps.SymbolPath.CIRCLE,fillColor:"#C8D6EC",fillOpacity:.7,scale:12,strokeWeight:0},position:new google.maps.LatLng(0,0),title:"Current location",zIndex:2},q={clickable:!1,cursor:"pointer",draggable:!1,flat:!0,icon:{path:google.maps.SymbolPath.CIRCLE,fillColor:"#4285F4",fillOpacity:1,scale:6,strokeColor:"white",
strokeWeight:2},optimized:!1,position:new google.maps.LatLng(0,0),title:"Current location",zIndex:3};c&&(p=r(p,c));f&&(q=r(q,f));c={clickable:!1,radius:0,strokeColor:"1bb6ff",strokeOpacity:.4,fillColor:"61a0bf",fillOpacity:.4,strokeWeight:1,zIndex:1};v&&(c=r(c,v));d.c=new google.maps.Marker(p);d.b=new google.maps.Marker(q);d.a=new google.maps.Circle(c);google.maps.MVCObject.prototype.set.call(d,"accuracy",null);google.maps.MVCObject.prototype.set.call(d,"position",null);google.maps.MVCObject.prototype.set.call(d,
"map",null);d.set("minimum_accuracy",null);d.set("position_options",{enableHighAccuracy:!1,maximumAge:Infinity,timeout:6E4});d.a.bindTo("map",d.c);d.a.bindTo("map",d.b);a&&d.h(a);return d}var t=google.maps.MVCObject;n.prototype=e(t.prototype);n.prototype.constructor=n;if(m)m(n,t);else for(var u in t)if("prototype"!=u)if(Object.defineProperties){var w=Object.getOwnPropertyDescriptor(t,u);w&&Object.defineProperty(n,u,w)}else n[u]=t[u];n.a=t.prototype;b=n.prototype;
b.set=function(a,c){if(x.test(a))throw"'"+a+"' is a read-only property.";"map"===a?this.h(c):google.maps.MVCObject.prototype.set.call(this,a,c)};b.f=function(){return this.get("map")};b.m=function(){return this.get("position_options")};b.B=function(a){this.set("position_options",a)};b.g=function(){return this.get("position")};b.s=function(){return this.get("position")?this.a.getBounds():null};b.l=function(){return this.get("accuracy")};b.j=function(){return this.get("minimum_accuracy")};
b.A=function(a){this.set("minimum_accuracy",a)};
b.h=function(a){google.maps.MVCObject.prototype.set.call(this,"map",a);a?navigator.geolocation&&(console.log("navigator.geolocation"),this.i=navigator.geolocation.watchPosition(this.C.bind(this),this.error,this.m())):(this.c.unbind("position"),this.b.unbind("position"),this.a.unbind("center"),this.a.unbind("radius"),google.maps.MVCObject.prototype.set.call(this,"accuracy",null),google.maps.MVCObject.prototype.set.call(this,"position",null),navigator.geolocation.clearWatch(this.i),this.i=-1,this.c.setMap(a),
this.b.setMap(a))};b.w=function(a){this.b.setOptions(r({},a))};b.v=function(a){this.a.setOptions(r({},a))};
b.C=function(a){console.log("updatePosition_");var c=new google.maps.LatLng(a.coords.latitude,a.coords.longitude),f=null==this.b.getMap();if(f){if(null!=this.j()&&a.coords.accuracy>this.j())return;this.c.setMap(this.f());this.b.setMap(this.f());this.c.bindTo("position",this);this.b.bindTo("position",this);this.a.bindTo("center",this,"position");this.a.bindTo("radius",this,"accuracy")}this.l()!=a.coords.accuracy&&google.maps.MVCObject.prototype.set.call(this,"accuracy",a.coords.accuracy);!f&&null!=
this.g()&&this.g().equals(c)||google.maps.MVCObject.prototype.set.call(this,"position",c);this.o&&this.o(c)};b.error=function(a){console.warn("ERROR("+a.code+"): "+a.message)};function r(a,c){for(var f in c)!0!==y[f]&&(a[f]=c[f]);return a}var y={map:!0,position:!0,radius:!0},x=/^(?:position|accuracy)$/i;var z=window;function A(){n.prototype.getAccuracy=n.prototype.l;n.prototype.getBounds=n.prototype.s;n.prototype.getMap=n.prototype.f;n.prototype.getMinimumAccuracy=n.prototype.j;n.prototype.getPosition=n.prototype.g;n.prototype.getPositionOptions=n.prototype.m;n.prototype.setCircleOptions=n.prototype.v;n.prototype.setMap=n.prototype.h;n.prototype.setMarkerOptions=n.prototype.w;n.prototype.setMinimumAccuracy=n.prototype.A;n.prototype.setPositionOptions=n.prototype.B;return n}
"function"===typeof z.define&&z.define.amd?z.define([],A):"object"===typeof z.exports?z.module.exports=A():z.GeolocationMarker=A();
}).call(this)

//# sourceMappingURL=geolocation-marker-min.js.map