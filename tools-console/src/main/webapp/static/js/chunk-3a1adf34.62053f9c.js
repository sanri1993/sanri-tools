(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-3a1adf34"],{"0c2e":function(t,e,a){"use strict";a("18e5")},"18e5":function(t,e,a){},9406:function(t,e,a){"use strict";a.r(e);var l=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"app-container"},[a("github-corner",{staticClass:"github-corner"}),a("el-table",{staticStyle:{width:"100%"},attrs:{data:t.plugins,size:"mini",border:"",stripe:"","highlight-current-row":""}},[a("el-table-column",{attrs:{type:"index",width:"55"}}),a("el-table-column",{attrs:{prop:"pluginRegister.name",label:"插件名称",width:"150",sortable:""}}),a("el-table-column",{attrs:{prop:"pluginRegister.author",label:"作者",width:"80"}}),a("el-table-column",{attrs:{prop:"pluginRegister.versionString",label:"版本",width:"80"}}),a("el-table-column",{attrs:{prop:"pluginCallInfo.lastCallTime",label:"上次调用",formatter:t.formatTime,sortable:"",width:"150"}}),a("el-table-column",{attrs:{prop:"pluginCallInfo.totalCalls",label:"次数",sortable:"",width:"80"}}),a("el-table-column",{attrs:{prop:"pluginRegister.desc",label:"简单描述"}}),a("el-table-column",{attrs:{fixed:"right",label:"操作",width:"200"},scopedSlots:t._u([{key:"default",fn:function(e){return[a("el-button",{attrs:{type:"text",size:"small"},on:{click:function(a){return t.introduce(e.row.pluginRegister.id)}}},[t._v("插件介绍")])]}}])})],1),a("el-drawer",{attrs:{title:t.drawer.title,visible:t.drawer.visible,size:"40%",direction:"rtl"},on:{"update:visible":function(e){return t.$set(t.drawer,"visible",e)}}},[a("mavon-editor",{staticStyle:{height:"100%",width:"100%"},attrs:{editable:!1,subfield:!1,"toolbars-flag":!1,"default-open":"preview"},model:{value:t.drawer.detail.helpContent,callback:function(e){t.$set(t.drawer.detail,"helpContent",e)},expression:"drawer.detail.helpContent"}})],1)],1)},i=[],n=(a("b0c0"),a("b1a9")),r=a("ed08"),o=a("b2d8"),s=(a("64e1"),function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("a",{staticClass:"github-corner",attrs:{href:"https://gitee.com/sanri/sanri-tools-maven",target:"_blank","aria-label":"View source on Github"}},[a("svg",{staticStyle:{fill:"#40c9c6",color:"#fff"},attrs:{width:"80",height:"80",viewBox:"0 0 250 250","aria-hidden":"true"}},[a("path",{attrs:{d:"M0,0 L115,115 L130,115 L142,142 L250,250 L250,0 Z"}}),a("path",{staticClass:"octo-arm",staticStyle:{"transform-origin":"130px 106px"},attrs:{d:"M128.3,109.0 C113.8,99.7 119.0,89.6 119.0,89.6 C122.0,82.7 120.5,78.6 120.5,78.6 C119.2,72.0 123.4,76.3 123.4,76.3 C127.3,80.9 125.5,87.3 125.5,87.3 C122.9,97.6 130.6,101.9 134.4,103.2",fill:"currentColor"}}),a("path",{staticClass:"octo-body",attrs:{d:"M115.0,115.0 C114.9,115.1 118.7,116.5 119.8,115.4 L133.7,101.6 C136.9,99.2 139.9,98.4 142.2,98.6 C133.8,88.0 127.5,74.4 143.8,58.0 C148.5,53.4 154.0,51.2 159.7,51.0 C160.3,49.4 163.2,43.6 171.4,40.1 C171.4,40.1 176.1,42.5 178.8,56.2 C183.1,58.6 187.2,61.8 190.9,65.4 C194.5,69.0 197.7,73.2 200.1,77.6 C213.8,80.2 216.3,84.9 216.3,84.9 C212.7,93.1 206.9,96.0 205.4,96.6 C205.1,102.4 203.0,107.8 198.3,112.5 C181.9,128.9 168.3,122.5 157.7,114.1 C157.9,116.9 156.7,120.9 152.7,124.9 L141.0,136.5 C139.8,137.7 141.6,141.9 141.8,141.8 Z",fill:"currentColor"}})])])}),c=[],u=(a("0c2e"),a("2877")),d={},p=Object(u["a"])(d,s,c,!1,null,"8d1fd626",null),b=p.exports,h={name:"Index",components:{mavonEditor:o["mavonEditor"],GithubCorner:b},data:function(){return{plugins:[],drawer:{visible:!1,title:"",detail:{enhancePlugin:{},helpContent:""}}}},created:function(){var t="/";"/"===t&&(t=window.location.origin),console.log(t,"linkWs"),this.$websocket.dispatch("WEBSOCKET_INIT",t+"/ws")},mounted:function(){var t=this;n["a"].plugins().then((function(e){t.plugins=e.data}))},methods:{formatTime:function(t,e){return Object(r["h"])(t.pluginCallInfo.lastCallTime,void 0)},introduce:function(t){var e=this;n["a"].pluginDetail(t).then((function(t){e.drawer.detail=t.data,e.drawer.title="插件 "+t.data.enhancePlugin.pluginRegister.name+" 详细介绍",e.drawer.detail.helpContent=e.drawer.detail.helpContent||"暂无介绍",e.drawer.visible=!0}))}}},f=h,C=(a("b4da"),Object(u["a"])(f,l,i,!1,null,"adb08a0e",null));e["default"]=C.exports},b4da:function(t,e,a){"use strict";a("c394")},c394:function(t,e,a){}}]);