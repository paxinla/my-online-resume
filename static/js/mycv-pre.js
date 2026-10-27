/* antiband.js - 防拷贝行为阻断（ES5，IE8+ 兼容） */
(function () {
var doc = document;
function prevent(e) {
e = e || window.event;
if (e.preventDefault) { e.preventDefault(); } else { e.returnValue = false; }
return false;
}
/* 破解 iframe 嵌套 */
try {
if (window.top !== window.self) { window.top.location = window.self.location; }
} catch (err) {}
/* 右键菜单 */
doc.oncontextmenu = prevent;
/* 事件绑定：IE8 用 attachEvent，其余用 addEventListener */
function on(el, ev, fn) {
if (el.addEventListener) { el.addEventListener(ev, fn, false); }
else if (el.attachEvent) { el.attachEvent('on' + ev, fn); }
}
on(doc.body || doc, 'copy', prevent);
on(doc.body || doc, 'cut', prevent);
on(doc.body || doc, 'dragstart', prevent);
/* 快捷键威慑：Ctrl+C/X/S/U 与 F12 */
on(doc, 'keydown', function (e) {
e = e || window.event;
var k = e.keyCode || e.which;
if (k === 123) { return prevent(e); }
if (e.ctrlKey && e.shiftKey && (k === 73 || k === 74)) { return prevent(e); }
if (e.ctrlKey && (k === 67 || k === 88 || k === 83 || k === 85)) { return prevent(e); }
return true;
});
}());
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