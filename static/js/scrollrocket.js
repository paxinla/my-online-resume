/*
var keyStr = "ABCDEFGHIJKLMNOP" +
             "QRSTUVWXYZabcdef" +
             "ghijklmnopqrstuv" +
             "wxyz0123456789+/" +
             "=";

function decode64(input) {
    var output = "";
    var chr1, chr2, chr3 = "";
    var enc1, enc2, enc3, enc4 = "";
    var i = 0;

    input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

    do {
       enc1 = keyStr.indexOf(input.charAt(i++));
       enc2 = keyStr.indexOf(input.charAt(i++));
       enc3 = keyStr.indexOf(input.charAt(i++));
       enc4 = keyStr.indexOf(input.charAt(i++));

       chr1 = (enc1 << 2) | (enc2 >> 4);
       chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
       chr3 = ((enc3 & 3) << 6) | enc4;

       output = output + String.fromCharCode(chr1);

       if (enc3 != 64) {
          output = output + String.fromCharCode(chr2);
       }
       if (enc4 != 64) {
          output = output + String.fromCharCode(chr3);
       }

       chr1 = chr2 = chr3 = "";
       enc1 = enc2 = enc3 = enc4 = "";

    } while (i < input.length);

    return unescape(output);
}


function GetHttpRequest() {
    var xmlhttp;

    if(window.XMLHttpRequest) {
        xmlhttp=new XMLHttpRequest();
    } else {
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }

    return xmlhttp; 
}


function LoadFileData(tagId, fileName) {
    var xmlhttp=GetHttpRequest();

    if(!xmlhttp){
        alert("Problem getting XHR!");
    }

    xmlhttp.open("GET",fileName,true);
    xmlhttp.onreadystatechange=function() { 
        OnReadyStateChg(xmlhttp, tagId); 
    };
    xmlhttp.send(null); 
}


function OnReadyStateChg(xmlHttp, tagId) {
    if (xmlHttp.readyState==4) {
        if (xmlHttp.status==200||xmlHttp.status==0) {
            var plainText = decode64(xmlHttp.responseText);
            document.getElementById(tagId).innerHTML=plainText;
            var input = document.getElementById(tagId); 
            input.innerHTML=marked(input.innerHTML); 
        } else {
            alert("Problem retrieving "+tagId+" data! Status="+xmlHttp.status);
        }
    }
}

function getScrollTop() {
    //解决多浏览器支持,chrome等浏览器下document.documentElement.scrollTop的值是0
    return document.documentElement.scrollTop + document.body.scrollTop;
}

function LoadContent() {
    var scrollHeight = 600;
    var obj = document.getElementById("gotoTop");
    obj.style.display = "none"; 

    LoadFileData("banner","output/banner.mdx");
    LoadFileData("announce","output/announce.mdx");
    LoadFileData("intro","output/intro.mdx");
    LoadFileData("info","output/info.mdx");
    LoadFileData("stack","output/stack.mdx");
    LoadFileData("cert","output/cert.mdx");
    LoadFileData("cv","output/cv.mdx");
    LoadFileData("end","output/end.mdx");

    window.onscroll = function () { 
        getScrollTop() > scrollHeight ? obj.style.display = "" : obj.style.display = "none"; 
    }
}
*/

function pageScroll(acceleration,stime) {
   acceleration = acceleration || 0.1;
   stime = stime || 10;
   var x1 = 0;
   var y1 = 0;
   var x2 = 0;
   var y2 = 0;
   var x3 = 0;
   var y3 = 0; 
   if (document.documentElement) {
       x1 = document.documentElement.scrollLeft || 0;
       y1 = document.documentElement.scrollTop || 0;
   }
   if (document.body) {
       x2 = document.body.scrollLeft || 0;
       y2 = document.body.scrollTop || 0;
   }
   var x3 = window.scrollX || 0;
   var y3 = window.scrollY || 0;
 
   // 滚动条到页面顶部的水平距离
   var x = Math.max(x1, Math.max(x2, x3));
   // 滚动条到页面顶部的垂直距离
   var y = Math.max(y1, Math.max(y2, y3));
 
   // 滚动距离 = 目前距离 / 速度, 因为距离原来越小, 速度是大于 1 的数, 所以滚动距离会越来越小
   var speeding = 1 + acceleration;
   window.scrollTo(Math.floor(x / speeding), Math.floor(y / speeding));
 
   // 如果距离不为零, 继续调用函数
   if(x > 0 || y > 0) {
       var run = "pageScroll(" + acceleration + ", " + stime + ")";
       window.setTimeout(run, stime);
   }
}

