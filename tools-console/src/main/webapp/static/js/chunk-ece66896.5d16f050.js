(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-ece66896"],{6835:function(t,e,n){},"84c5":function(t,e,n){"use strict";n("6835")},b311:function(t,e,n){
/*!
 * clipboard.js v2.0.4
 * https://zenorocha.github.io/clipboard.js
 * 
 * Licensed MIT © Zeno Rocha
 */
(function(e,n){t.exports=n()})(0,(function(){return function(t){var e={};function n(i){if(e[i])return e[i].exports;var r=e[i]={i:i,l:!1,exports:{}};return t[i].call(r.exports,r,r.exports,n),r.l=!0,r.exports}return n.m=t,n.c=e,n.d=function(t,e,i){n.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:i})},n.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},n.t=function(t,e){if(1&e&&(t=n(t)),8&e)return t;if(4&e&&"object"===typeof t&&t&&t.__esModule)return t;var i=Object.create(null);if(n.r(i),Object.defineProperty(i,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var r in t)n.d(i,r,function(e){return t[e]}.bind(null,r));return i},n.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return n.d(e,"a",e),e},n.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},n.p="",n(n.s=0)}([function(t,e,n){"use strict";var i="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(t){return typeof t}:function(t){return t&&"function"===typeof Symbol&&t.constructor===Symbol&&t!==Symbol.prototype?"symbol":typeof t},r=function(){function t(t,e){for(var n=0;n<e.length;n++){var i=e[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(t,i.key,i)}}return function(e,n,i){return n&&t(e.prototype,n),i&&t(e,i),e}}(),o=n(1),a=h(o),l=n(3),s=h(l),c=n(4),u=h(c);function h(t){return t&&t.__esModule?t:{default:t}}function f(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}function d(t,e){if(!t)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return!e||"object"!==typeof e&&"function"!==typeof e?t:e}function g(t,e){if("function"!==typeof e&&null!==e)throw new TypeError("Super expression must either be null or a function, not "+typeof e);t.prototype=Object.create(e&&e.prototype,{constructor:{value:t,enumerable:!1,writable:!0,configurable:!0}}),e&&(Object.setPrototypeOf?Object.setPrototypeOf(t,e):t.__proto__=e)}var p=function(t){function e(t,n){f(this,e);var i=d(this,(e.__proto__||Object.getPrototypeOf(e)).call(this));return i.resolveOptions(n),i.listenClick(t),i}return g(e,t),r(e,[{key:"resolveOptions",value:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};this.action="function"===typeof t.action?t.action:this.defaultAction,this.target="function"===typeof t.target?t.target:this.defaultTarget,this.text="function"===typeof t.text?t.text:this.defaultText,this.container="object"===i(t.container)?t.container:document.body}},{key:"listenClick",value:function(t){var e=this;this.listener=(0,u.default)(t,"click",(function(t){return e.onClick(t)}))}},{key:"onClick",value:function(t){var e=t.delegateTarget||t.currentTarget;this.clipboardAction&&(this.clipboardAction=null),this.clipboardAction=new a.default({action:this.action(e),target:this.target(e),text:this.text(e),container:this.container,trigger:e,emitter:this})}},{key:"defaultAction",value:function(t){return y("action",t)}},{key:"defaultTarget",value:function(t){var e=y("target",t);if(e)return document.querySelector(e)}},{key:"defaultText",value:function(t){return y("text",t)}},{key:"destroy",value:function(){this.listener.destroy(),this.clipboardAction&&(this.clipboardAction.destroy(),this.clipboardAction=null)}}],[{key:"isSupported",value:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:["copy","cut"],e="string"===typeof t?[t]:t,n=!!document.queryCommandSupported;return e.forEach((function(t){n=n&&!!document.queryCommandSupported(t)})),n}}]),e}(s.default);function y(t,e){var n="data-clipboard-"+t;if(e.hasAttribute(n))return e.getAttribute(n)}t.exports=p},function(t,e,n){"use strict";var i="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(t){return typeof t}:function(t){return t&&"function"===typeof Symbol&&t.constructor===Symbol&&t!==Symbol.prototype?"symbol":typeof t},r=function(){function t(t,e){for(var n=0;n<e.length;n++){var i=e[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(t,i.key,i)}}return function(e,n,i){return n&&t(e.prototype,n),i&&t(e,i),e}}(),o=n(2),a=l(o);function l(t){return t&&t.__esModule?t:{default:t}}function s(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}var c=function(){function t(e){s(this,t),this.resolveOptions(e),this.initSelection()}return r(t,[{key:"resolveOptions",value:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};this.action=t.action,this.container=t.container,this.emitter=t.emitter,this.target=t.target,this.text=t.text,this.trigger=t.trigger,this.selectedText=""}},{key:"initSelection",value:function(){this.text?this.selectFake():this.target&&this.selectTarget()}},{key:"selectFake",value:function(){var t=this,e="rtl"==document.documentElement.getAttribute("dir");this.removeFake(),this.fakeHandlerCallback=function(){return t.removeFake()},this.fakeHandler=this.container.addEventListener("click",this.fakeHandlerCallback)||!0,this.fakeElem=document.createElement("textarea"),this.fakeElem.style.fontSize="12pt",this.fakeElem.style.border="0",this.fakeElem.style.padding="0",this.fakeElem.style.margin="0",this.fakeElem.style.position="absolute",this.fakeElem.style[e?"right":"left"]="-9999px";var n=window.pageYOffset||document.documentElement.scrollTop;this.fakeElem.style.top=n+"px",this.fakeElem.setAttribute("readonly",""),this.fakeElem.value=this.text,this.container.appendChild(this.fakeElem),this.selectedText=(0,a.default)(this.fakeElem),this.copyText()}},{key:"removeFake",value:function(){this.fakeHandler&&(this.container.removeEventListener("click",this.fakeHandlerCallback),this.fakeHandler=null,this.fakeHandlerCallback=null),this.fakeElem&&(this.container.removeChild(this.fakeElem),this.fakeElem=null)}},{key:"selectTarget",value:function(){this.selectedText=(0,a.default)(this.target),this.copyText()}},{key:"copyText",value:function(){var t=void 0;try{t=document.execCommand(this.action)}catch(e){t=!1}this.handleResult(t)}},{key:"handleResult",value:function(t){this.emitter.emit(t?"success":"error",{action:this.action,text:this.selectedText,trigger:this.trigger,clearSelection:this.clearSelection.bind(this)})}},{key:"clearSelection",value:function(){this.trigger&&this.trigger.focus(),window.getSelection().removeAllRanges()}},{key:"destroy",value:function(){this.removeFake()}},{key:"action",set:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"copy";if(this._action=t,"copy"!==this._action&&"cut"!==this._action)throw new Error('Invalid "action" value, use either "copy" or "cut"')},get:function(){return this._action}},{key:"target",set:function(t){if(void 0!==t){if(!t||"object"!==("undefined"===typeof t?"undefined":i(t))||1!==t.nodeType)throw new Error('Invalid "target" value, use a valid Element');if("copy"===this.action&&t.hasAttribute("disabled"))throw new Error('Invalid "target" attribute. Please use "readonly" instead of "disabled" attribute');if("cut"===this.action&&(t.hasAttribute("readonly")||t.hasAttribute("disabled")))throw new Error('Invalid "target" attribute. You can\'t cut text from elements with "readonly" or "disabled" attributes');this._target=t}},get:function(){return this._target}}]),t}();t.exports=c},function(t,e){function n(t){var e;if("SELECT"===t.nodeName)t.focus(),e=t.value;else if("INPUT"===t.nodeName||"TEXTAREA"===t.nodeName){var n=t.hasAttribute("readonly");n||t.setAttribute("readonly",""),t.select(),t.setSelectionRange(0,t.value.length),n||t.removeAttribute("readonly"),e=t.value}else{t.hasAttribute("contenteditable")&&t.focus();var i=window.getSelection(),r=document.createRange();r.selectNodeContents(t),i.removeAllRanges(),i.addRange(r),e=i.toString()}return e}t.exports=n},function(t,e){function n(){}n.prototype={on:function(t,e,n){var i=this.e||(this.e={});return(i[t]||(i[t]=[])).push({fn:e,ctx:n}),this},once:function(t,e,n){var i=this;function r(){i.off(t,r),e.apply(n,arguments)}return r._=e,this.on(t,r,n)},emit:function(t){var e=[].slice.call(arguments,1),n=((this.e||(this.e={}))[t]||[]).slice(),i=0,r=n.length;for(i;i<r;i++)n[i].fn.apply(n[i].ctx,e);return this},off:function(t,e){var n=this.e||(this.e={}),i=n[t],r=[];if(i&&e)for(var o=0,a=i.length;o<a;o++)i[o].fn!==e&&i[o].fn._!==e&&r.push(i[o]);return r.length?n[t]=r:delete n[t],this}},t.exports=n},function(t,e,n){var i=n(5),r=n(6);function o(t,e,n){if(!t&&!e&&!n)throw new Error("Missing required arguments");if(!i.string(e))throw new TypeError("Second argument must be a String");if(!i.fn(n))throw new TypeError("Third argument must be a Function");if(i.node(t))return a(t,e,n);if(i.nodeList(t))return l(t,e,n);if(i.string(t))return s(t,e,n);throw new TypeError("First argument must be a String, HTMLElement, HTMLCollection, or NodeList")}function a(t,e,n){return t.addEventListener(e,n),{destroy:function(){t.removeEventListener(e,n)}}}function l(t,e,n){return Array.prototype.forEach.call(t,(function(t){t.addEventListener(e,n)})),{destroy:function(){Array.prototype.forEach.call(t,(function(t){t.removeEventListener(e,n)}))}}}function s(t,e,n){return r(document.body,t,e,n)}t.exports=o},function(t,e){e.node=function(t){return void 0!==t&&t instanceof HTMLElement&&1===t.nodeType},e.nodeList=function(t){var n=Object.prototype.toString.call(t);return void 0!==t&&("[object NodeList]"===n||"[object HTMLCollection]"===n)&&"length"in t&&(0===t.length||e.node(t[0]))},e.string=function(t){return"string"===typeof t||t instanceof String},e.fn=function(t){var e=Object.prototype.toString.call(t);return"[object Function]"===e}},function(t,e,n){var i=n(7);function r(t,e,n,i,r){var o=a.apply(this,arguments);return t.addEventListener(n,o,r),{destroy:function(){t.removeEventListener(n,o,r)}}}function o(t,e,n,i,o){return"function"===typeof t.addEventListener?r.apply(null,arguments):"function"===typeof n?r.bind(null,document).apply(null,arguments):("string"===typeof t&&(t=document.querySelectorAll(t)),Array.prototype.map.call(t,(function(t){return r(t,e,n,i,o)})))}function a(t,e,n,r){return function(n){n.delegateTarget=i(n.target,e),n.delegateTarget&&r.call(t,n)}}t.exports=o},function(t,e){var n=9;if("undefined"!==typeof Element&&!Element.prototype.matches){var i=Element.prototype;i.matches=i.matchesSelector||i.mozMatchesSelector||i.msMatchesSelector||i.oMatchesSelector||i.webkitMatchesSelector}function r(t,e){while(t&&t.nodeType!==n){if("function"===typeof t.matches&&t.matches(e))return t;t=t.parentNode}}t.exports=r}])}))},de41:function(t,e,n){"use strict";n.r(e);var i=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"app-container"},[n("el-row",{directives:[{name:"loading",rawName:"v-loading",value:t.loading,expression:"loading"}]},[n("el-col",{attrs:{span:20}},[n("el-row",{},[n("el-col",{attrs:{span:7}},[n("div",{staticClass:"input-container"},[n("div",{staticClass:"text-bold margin-little-bottom"},[t._v("左边列:"+t._s(t.handle.left.length))]),n("el-input",{attrs:{type:"textarea",rows:9,placeholder:"左边列"},on:{blur:function(e){return t.parseColumns("left")}},model:{value:t.left,callback:function(e){t.left=e},expression:"left"}})],1)]),n("el-col",{staticClass:"margin-small-left",attrs:{span:7}},[n("div",{staticClass:"input-container"},[n("div",{staticClass:"text-bold margin-little-bottom"},[t._v("右边列:"+t._s(t.handle.right.length))]),n("el-input",{attrs:{type:"textarea",rows:9,placeholder:"右边列"},on:{blur:function(e){return t.parseColumns("right")}},model:{value:t.right,callback:function(e){t.right=e},expression:"right"}})],1)]),n("el-col",{staticClass:"margin-small-left",attrs:{span:7}},[n("div",{staticClass:"input-container"},[n("div",{staticClass:"text-bold margin-little-bottom"},[t._v("忽略列:"+t._s(t.handle.ignore.length))]),n("el-input",{attrs:{type:"textarea",rows:9,placeholder:"忽略列"},on:{blur:function(e){return t.parseColumns("ignore")}},model:{value:t.ignore,callback:function(e){t.ignore=e},expression:"ignore"}})],1)])],1),n("el-row",[n("el-col",{attrs:{span:7}},[n("div",{staticClass:"input-container"},[n("div",{staticClass:"text-bold "},[n("el-button",{staticClass:"text-forestgreen",attrs:{type:"text",icon:"el-icon-document-copy"},on:{click:function(e){return t.copyText(t.leftMuti,e)}}},[t._v("复制")]),t._v(" 左边比右边多出列:"+t._s(t.handle.leftMuti.length)+" ")],1),n("el-input",{attrs:{type:"textarea",rows:9,placeholder:"左边多于右边"},model:{value:t.leftMuti,callback:function(e){t.leftMuti=e},expression:"leftMuti"}})],1)]),n("el-col",{staticClass:"margin-small-left",attrs:{span:7}},[n("div",{staticClass:"input-container"},[n("div",{staticClass:"text-bold "},[n("el-button",{staticClass:"text-forestgreen",attrs:{type:"text",icon:"el-icon-document-copy"},on:{click:function(e){return t.copyText(t.rightMuti,e)}}},[t._v("复制")]),t._v(" 右边比左边多出列:"+t._s(t.handle.rightMuti.length)+" ")],1),n("el-input",{attrs:{type:"textarea",rows:9,placeholder:"右边多余左边"},model:{value:t.rightMuti,callback:function(e){t.rightMuti=e},expression:"rightMuti"}})],1)]),n("el-col",{staticClass:"margin-small-left",attrs:{span:7}},[n("div",{staticClass:"input-container"},[n("div",{staticClass:"text-bold "},[n("el-button",{staticClass:"text-forestgreen",attrs:{type:"text",icon:"el-icon-document-copy"},on:{click:function(e){return t.copyText(t.equal,e)}}},[t._v("复制")]),t._v(" 相等的列:"+t._s(t.handle.equal.length)+" "),n("i",{staticClass:"el-icon-microphone",attrs:{title:"当两边相等时, 使用左边列原始值"}})],1),n("el-input",{attrs:{type:"textarea",rows:9,placeholder:"相等列"},model:{value:t.equal,callback:function(e){t.equal=e},expression:"equal"}})],1)])],1)],1),n("el-col",{attrs:{span:4}},[n("el-checkbox",{attrs:{label:"忽略大小写"},on:{change:t.changeSetting},model:{value:t.setting.ignoreAb,callback:function(e){t.$set(t.setting,"ignoreAb",e)},expression:"setting.ignoreAb"}}),n("el-checkbox",{attrs:{label:"忽略下划线"},on:{change:t.changeSetting},model:{value:t.setting.ignoreUnderline,callback:function(e){t.$set(t.setting,"ignoreUnderline",e)},expression:"setting.ignoreUnderline"}}),n("el-checkbox",{attrs:{checked:"",label:"忽略空白"},on:{change:t.changeSetting},model:{value:t.setting.ignoreSpace,callback:function(e){t.$set(t.setting,"ignoreSpace",e)},expression:"setting.ignoreSpace"}}),n("div",{staticClass:"margin-top"},[n("el-button",{attrs:{type:"primary",size:"small"},on:{click:t.compare}},[t._v("开始比较")])],1)],1)],1)],1)},r=[],o=(n("ac1f"),n("1276"),n("498a"),n("5319"),n("d81d"),n("a15b"),n("f71e")),a=n("ed08"),l={name:"Compare",data:function(){return{loading:!1,left:null,right:null,ignore:null,rightMuti:null,leftMuti:null,equal:null,handle:{left:[],right:[],ignore:[],rightMuti:[],leftMuti:[],equal:[]},setting:{ignoreAb:!1,ignoreUnderline:!1,ignoreSpace:!0}}},methods:{copyText:function(t,e){Object(o["a"])(t,e)},changeSetting:function(){this.parseColumns("left"),this.parseColumns("right")},parseColumns:function(t){if(this[t]){for(var e=this[t].trim().split("\n"),n=[],i=0;i<e.length;i++){var r=e[i];this.setting.ignoreSpace&&(r=r.trim()),this.setting.ignoreAb&&(r=r.toLowerCase()),this.setting.ignoreUnderline&&(r=r.replace(/_/g,"")),r&&n.push({origin:e[i],handle:r})}this.handle[t]=n}},compare:function(){this.loading=!0;var t=!0;if(this.handle.rightMuti=[],this.handle.leftMuti=[],this.handle.equal=[],0===this.handle.ignore.length)this.handle.right.length>0&&0===this.handle.left.length?(this.handle.rightMuti=this.handle.right.map((function(t){return t.origin})),t=!1):0===this.handle.right.length&&this.handle.left.length>0&&(this.handle.leftMuti=this.handle.left.map((function(t){return t.origin})),t=!1);else if(this.handle.right.length>0&&0===this.handle.left.length){for(var e=0;e<this.handle.right.length;e++)-1===Object(a["g"])(this.handle.right[e].origin,this.handle.ignore)&&this.handle.rightMuti.push(this.handle.right[e].origin);t=!1}else if(0===this.handle.right.length&&this.handle.left.length>0){for(var n=0;n<this.handle.left.length;n++)-1===Object(a["g"])(this.handle.left[n].origin,this.handle.ignore)&&this.handle.leftMuti.push(this.handle.left[n].origin);t=!1}if(t){for(var i=0;i<this.handle.left.length;i++)if(-1===Object(a["g"])(this.handle.left[i].origin,this.handle.ignore))for(var r=0;r<this.handle.right.length;r++)if(this.handle.left[i].handle===this.handle.right[r].handle){this.handle.equal.push(this.handle.left[i]);break}for(var o=";"+this.handle.equal.map((function(t){return t.handle})).join(";")+";",l=0;l<this.handle.left.length;l++)-1===Object(a["g"])(this.handle.left[l].origin,this.handle.ignore)&&-1===o.indexOf(";"+this.handle.left[l].handle+";")&&this.handle.leftMuti.push(this.handle.left[l].origin);for(var s=0;s<this.handle.right.length;s++)-1===Object(a["g"])(this.handle.right[s].origin,this.handle.ignore)&&-1===o.indexOf(";"+this.handle.right[s].handle+";")&&this.handle.rightMuti.push(this.handle.right[s].origin)}this.loading=!1,this.leftMuti=this.handle.leftMuti.join("\n"),this.rightMuti=this.handle.rightMuti.join("\n"),this.equal=this.handle.equal.map((function(t){return t.origin})).join("\n")}}},s=l,c=(n("84c5"),n("2877")),u=Object(c["a"])(s,i,r,!1,null,"9544f6c2",null);e["default"]=u.exports},f71e:function(t,e,n){"use strict";n.d(e,"a",(function(){return s}));var i=n("2b0e"),r=n("b311"),o=n.n(r);function a(){i["default"].prototype.$message({message:"Copy successfully",type:"success",duration:1500})}function l(){i["default"].prototype.$message({message:"Copy failed",type:"error"})}function s(t,e){var n=new o.a(e.target,{text:function(){return t}});n.on("success",(function(){a(),n.destroy()})),n.on("error",(function(){l(),n.destroy()})),n.onClick(e)}}}]);