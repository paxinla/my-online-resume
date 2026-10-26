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
