/* scrolltop.js - 火箭回顶（沿用旧 scrollrocket.js 逻辑，ES5） */
function pageScroll(acceleration, stime) {
    acceleration = acceleration || 0.1;
    stime = stime || 10;
    var x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0;
    if (document.documentElement) {
        x1 = document.documentElement.scrollLeft || 0;
        y1 = document.documentElement.scrollTop || 0;
    }
    if (document.body) {
        x2 = document.body.scrollLeft || 0;
        y2 = document.body.scrollTop || 0;
    }
    x3 = window.scrollX || 0;
    y3 = window.scrollY || 0;
    var x = Math.max(x1, Math.max(x2, x3));
    var y = Math.max(y1, Math.max(y2, y3));
    var speeding = 1 + acceleration;
    window.scrollTo(Math.floor(x / speeding), Math.floor(y / speeding));
    if (x > 0 || y > 0) {
        var run = "pageScroll(" + acceleration + ", " + stime + ")";
        window.setTimeout(run, stime);
    }
}
