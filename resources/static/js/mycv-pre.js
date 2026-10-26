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
/* mobile-guard.js - 移动端访问降级（沿用旧实现 UA 检测逻辑，ES5/IE8 兼容） */
window.mobileCheck = function() {
var check = false;
(function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor||window.opera);
return check;
};
if (window.mobileCheck()) {
var warnDialog = '';
warnDialog = '<div class="not-support-mobile-access">'
+ '<h3 style="color:yellow;letter-spacing:0.3px;">本网页仅支持在PC端浏览</h3>'
+ '</div>' ;
document.getElementById("whole-page").innerHTML += warnDialog ;
document.getElementById("content-area").className += ' blur';
}