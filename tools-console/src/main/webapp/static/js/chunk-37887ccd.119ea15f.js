(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-37887ccd"],{"68fb":function(e,t,l){"use strict";l.r(t);var s=function(){var e=this,t=e.$createElement,l=e._self._c||t;return l("div",{staticClass:"app-container"},[l("el-row",[l("el-col",{attrs:{span:6}},[l("div",{staticClass:"input-container"},[l("div",{staticClass:"text-bold "},[l("el-button",{staticClass:"text-forestgreen",attrs:{type:"text",icon:"el-icon-document-copy"},on:{click:function(t){return e.copyText(e.left,t)}}},[e._v("复制")]),e._v(" 原始数据:"+e._s(e.handle.left.length)+" ")],1),l("el-input",{attrs:{type:"textarea",rows:18,placeholder:"原始数据"},on:{blur:function(t){return e.parseColumns(t)}},model:{value:e.left,callback:function(t){e.left=t},expression:"left"}})],1)]),l("el-col",{staticClass:"margin-left",attrs:{span:6}},[l("div",{staticClass:"input-container"},[l("div",{staticClass:"text-bold margin-little-bottom"},[l("el-button",{staticClass:"text-forestgreen",attrs:{type:"text",icon:"el-icon-document-copy"},on:{click:function(t){return e.copyText(e.right,t)}}},[e._v("复制")]),e._v(" 处理后数据:"+e._s(e.handle.right.length)+" ")],1),l("el-input",{attrs:{type:"textarea",rows:18,placeholder:"处理后数据"},model:{value:e.right,callback:function(t){e.right=t},expression:"right"}})],1)]),l("el-col",{staticClass:"margin-left",attrs:{span:9}},[l("el-form",{attrs:{size:"mini",inline:""}},[l("el-form-item",[l("el-checkbox",{model:{value:e.setting.delRepeat,callback:function(t){e.$set(e.setting,"delRepeat",t)},expression:"setting.delRepeat"}},[e._v("去除重复数据")])],1),l("el-form-item",[l("el-checkbox",{model:{value:e.setting.trim,callback:function(t){e.$set(e.setting,"trim",t)},expression:"setting.trim"}},[e._v("去空格")])],1),l("el-form-item",[l("el-checkbox",{attrs:{title:"如果去空格选上了的话, 会先去空格再去空行"},model:{value:e.setting.delBlank,callback:function(t){e.$set(e.setting,"delBlank",t)},expression:"setting.delBlank"}},[e._v("去空行")])],1)],1),l("p",{staticClass:"text-bold"},[e._v("数据排序")]),l("el-form",{attrs:{size:"mini",inline:""}},[l("el-form-item",[l("el-checkbox",{model:{value:e.setting.allIsNumber,callback:function(t){e.$set(e.setting,"allIsNumber",t)},expression:"setting.allIsNumber"}},[e._v("纯数字")])],1),l("el-form-item",[l("el-button",{staticClass:"text-forestgreen margin-little-right",attrs:{type:"text",icon:"el-icon-sort-up"},on:{click:function(t){return e.sort(!0)}}},[e._v("升序")]),l("el-button",{staticClass:"text-forestgreen margin-little-right",attrs:{type:"text",icon:"el-icon-sort-down"},on:{click:function(t){return e.sort(!1)}}},[e._v("降序")])],1)],1),l("el-button",{staticClass:"text-forestgreen margin-little-right",attrs:{type:"text",icon:"el-icon-top"},on:{click:e.capitalize}},[e._v("首字母大写")]),l("el-button",{staticClass:"text-forestgreen margin-little-right",attrs:{type:"text",icon:"el-icon-phone",title:"只有当后台翻译模块可用时才有用"},on:{click:e.translate}},[e._v("翻译")]),l("p",{staticClass:"text-bold"},[l("el-button",{staticClass:"text-forestgreen",attrs:{size:"small",type:"text",icon:"el-icon-thumb"},on:{click:e.columnPad}},[e._v("行处理")]),e._v(" 前后缀 ")],1),l("el-form",{attrs:{inline:!0}},[l("el-form-item",[l("el-input",{attrs:{placeholder:"前缀",size:"small"},model:{value:e.setting.prefix,callback:function(t){e.$set(e.setting,"prefix",t)},expression:"setting.prefix"}})],1),l("el-form-item",[l("el-input",{attrs:{placeholder:"后缀",size:"small"},model:{value:e.setting.suffix,callback:function(t){e.$set(e.setting,"suffix",t)},expression:"setting.suffix"}})],1)],1),l("p",{staticClass:"text-bold"},[l("el-button",{staticClass:"text-forestgreen",attrs:{size:"small",type:"text",icon:"el-icon-thumb"},on:{click:e.findAndReplace}},[e._v("行处理")]),e._v(" 查找与替换 ")],1),l("el-form",{attrs:{inline:""}},[l("el-form-item",{staticStyle:{display:"block"}},[l("el-checkbox",{model:{value:e.setting.replace.find,callback:function(t){e.$set(e.setting.replace,"find",t)},expression:"setting.replace.find"}},[e._v("仅查找")]),l("el-checkbox",{model:{value:e.setting.replace.regex,callback:function(t){e.$set(e.setting.replace,"regex",t)},expression:"setting.replace.regex"}},[e._v("正则表达式")])],1),l("el-form-item",[l("el-input",{attrs:{placeholder:"查找行",size:"small"},model:{value:e.setting.replace.before,callback:function(t){e.$set(e.setting.replace,"before",t)},expression:"setting.replace.before"}})],1),l("el-form-item",[l("el-input",{attrs:{placeholder:"替换",disabled:e.setting.replace.find,size:"small"},model:{value:e.setting.replace.after,callback:function(t){e.$set(e.setting.replace,"after",t)},expression:"setting.replace.after"}})],1)],1),l("el-form",[l("p",{staticClass:"text-bold"},[l("el-button",{staticClass:"text-forestgreen",attrs:{size:"small",type:"text",icon:"el-icon-thumb"},on:{click:e.dataCollect}},[e._v("行提取")]),e._v(" 数据提取 ")],1),l("el-form-item",[l("el-input",{staticClass:"input-with-select",attrs:{size:"small",placeholder:"提取正则"},model:{value:e.setting.dataCollect.select.regex,callback:function(t){e.$set(e.setting.dataCollect.select,"regex",t)},expression:"setting.dataCollect.select.regex"}},[l("el-select",{staticStyle:{width:"100px"},attrs:{slot:"prepend",placeholder:"请选择"},on:{change:e.choseSample},slot:"prepend",model:{value:e.setting.dataCollect.select.label,callback:function(t){e.$set(e.setting.dataCollect.select,"label",t)},expression:"setting.dataCollect.select.label"}},e._l(e.samples,(function(e){return l("el-option",{key:e.label,attrs:{label:e.label,value:e.label}})})),1),l("el-select",{staticClass:"small",staticStyle:{width:"60px"},attrs:{slot:"append",placeholder:"正则分组"},slot:"append",model:{value:e.setting.dataCollect.select.index,callback:function(t){e.$set(e.setting.dataCollect.select,"index",t)},expression:"setting.dataCollect.select.index"}},e._l([0,1,2,3,4,5,6,7,8,9],(function(e){return l("el-option",{key:"group"+e,attrs:{label:e,value:e}})})),1)],1)],1)],1)],1)],1)],1)},n=[],i=(l("ac1f"),l("1276"),l("498a"),l("a15b"),l("5319"),l("7db0"),l("d3b7"),l("4d63"),l("2c3e"),l("25f0"),l("00b4"),l("ad9d"),l("4e82"),l("f71e")),a=l("ed08"),r={name:"Column",data:function(){return{left:null,right:null,handle:{left:[],right:[]},setting:{trim:!0,delBlank:!0,allIsNumber:!1,prefix:"",suffix:"",delRepeat:!0,replace:{before:null,after:null,regex:!0,find:!0},dataCollect:{select:{regex:null,label:null,index:0},samples:{"Java属性":{pattern:/(private|protected)\s+\w+\s+(\w+);/,index:2},"Java 属性值":{pattern:/(private|protected|public).*?=\s*(.+?);/,index:2},"Java 公共属性":{pattern:/public.*?\s(\w+)\s=/,index:1},"文档注释":{pattern:/\* (.*?)\n/,index:1},"末尾注释":{pattern:/\/\/(.*)/,index:1},"末尾注释前面的":{pattern:/(.*)\/\//,index:1},"属性列":{pattern:/property="(\w+)"/,index:1},"数据库列":{pattern:/column="(\w+)"/,index:1},temp:{pattern:/\w\.(\w+),/,index:1},"表列":{pattern:/"(\w+)"\s.*/,index:1},"表列类型":{pattern:/"(\w+)"\s(\w+).*/,index:2},"表列注释":{pattern:/\'(.+)\';/,index:1},hibernatesql:{pattern:/"(.+)"/,index:1},aliassql:{pattern:/(\w+),/,index:1},"注释提取":{pattern:/value\s+=\s+"(.+)"/,index:1},"类型提取":{pattern:/(private|protected)\s+(\w+)/,index:2}}}}}},computed:{samples:function(){var e=[];for(var t in this.setting.dataCollect.samples)e.push({label:t,value:this.setting.dataCollect.samples[t].pattern});return e}},methods:{copyText:function(e,t){Object(i["a"])(e,t)},choseSample:function(e){var t=this.setting.dataCollect;t.samples[e]&&(t.select.regex=t.samples[e].pattern.source,t.select.index=t.samples[e].index)},parseColumns:function(e){for(var t=e.target.value.trim().split("\n"),l=[],s=0;s<t.length;s++){var n=t[s];this.setting.trim&&(n=n.trim()),n&&this.setting.delBlank,l.push(n)}this.handle["left"]=l},handleRepeat:function(){var e=this.handle.left;if(this.setting.delRepeat){for(var t=[],l=0;l<this.handle.left.length;l++){var s=this.handle.left[l];-1===Object(a["g"])(s,t)&&t.push(s)}e=t}return e},columnPad:function(){var e=this.handleRepeat();if(this.setting.prefix||this.setting.suffix)for(var t=0;t<e.length;t++)e[t]=this.setting.prefix+e[t]+this.setting.suffix;this.handle.right=e,this.right=e.join("\n")},findAndReplace:function(){var e=this.handleRepeat();if(this.setting.replace.before){var t=[];if(this.setting.replace.find)for(var l=0;l<e.length;l++){var s=e[l];if(!this.setting.replace.regex&&s.contains(this.setting.replace.before))t.push(s);else if(this.setting.replace.regex){var n=new RegExp(this.setting.replace.before);n.test(s)&&t.push(s)}}else for(var i=0;i<e.length;i++){var a=e[i];!this.setting.replace.regex&&a.contains(this.setting.replace.before)?t.push(a.replaceAll(this.setting.replace.before,this.setting.replace.after)):this.setting.replace.regex&&new RegExp(this.setting.replace.before).test(a)&&this.$message("正则替换暂不支持")}e=t}this.handle.right=e,this.right=e.join("\n")},dataCollect:function(){for(var e=this.handleRepeat(),t=[],l=0;l<e.length;l++){var s=e[l],n=new RegExp(this.setting.dataCollect.select.regex).exec(s);if(n&&n.length>this.setting.dataCollect.select.index){var i=n[this.setting.dataCollect.select.index];t.push(i)}}this.handle.right=t,this.right=t.join("\n")},sort:function(e){var t=this.handleRepeat();this.setting.allIsNumber?t.sort((function(t,l){return e?parseInt(t)-parseInt(l):parseInt(l)-parseInt(t)})):t.sort((function(t,l){return e?t.localeCompare(l):l.localeCompare(t)})),this.handle.right=t,this.right=t.join("\n")},capitalize:function(){for(var e=this.handleRepeat(),t=[],l=0;l<e.length;l++){var s=e[l];if(s){var n=s[0].toUpperCase()+s.substr(1);t.push(n)}}this.handle.right=t,this.right=t.join("\n")},translate:function(){this.$message("暂不支持")}}},c=r,o=(l("7b37"),l("2877")),p=Object(o["a"])(c,s,n,!1,null,"7bec2d7c",null);t["default"]=p.exports},"7b37":function(e,t,l){"use strict";l("bce1")},bce1:function(e,t,l){},f71e:function(e,t,l){"use strict";l.d(t,"a",(function(){return c}));var s=l("2b0e"),n=l("b311"),i=l.n(n);function a(){s["default"].prototype.$message({message:"Copy successfully",type:"success",duration:1500})}function r(){s["default"].prototype.$message({message:"Copy failed",type:"error"})}function c(e,t){var l=new i.a(t.target,{text:function(){return e}});l.on("success",(function(){a(),l.destroy()})),l.on("error",(function(){r(),l.destroy()})),l.onClick(t)}}}]);