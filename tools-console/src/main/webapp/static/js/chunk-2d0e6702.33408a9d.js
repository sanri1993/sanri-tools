(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-2d0e6702"],{"996b":function(e,t,l){"use strict";l.r(t);var a=function(){var e=this,t=e.$createElement,l=e._self._c||t;return l("div",{staticClass:"app-container"},[l("div",[e._v("临时文件目录占用大小: "+e._s(e.formatSizeHuman(e.tempFileSize)))]),l("el-table",{staticStyle:{width:"100%"},attrs:{data:e.files,border:"",stripe:"",size:"mini",lazy:"",load:e.loadChild,"row-key":"path","tree-props":{children:"children",hasChildren:"directory"}}},[l("el-table-column",{attrs:{prop:"name",label:"文件名",sortable:""}}),l("el-table-column",{attrs:{prop:"path",label:"路径"}}),l("el-table-column",{attrs:{prop:"size",label:"大小",sortable:""},scopedSlots:e._u([{key:"default",fn:function(t){return[l("span",[e._v(e._s(e.formatSizeHuman(t.row.size)))])]}}])}),l("el-table-column",{attrs:{prop:"lastUpdateTime",label:"上次修改时间",sortable:"",formatter:e.formatTime}}),l("el-table-column",{attrs:{fixed:"right",label:"操作",width:"100"},scopedSlots:e._u([{key:"default",fn:function(t){return[t.row.delete?l("div",[e._v("已删除")]):l("div",[l("el-button",{attrs:{type:"text",size:"small"},on:{click:function(l){return e.deleteFiles(t.row,t.$index)}}},[e._v("删除")]),l("el-button",{attrs:{type:"text",size:"small"},on:{click:function(l){return e.downloadFile(t.row.path)}}},[e._v("下载")])],1)]}}])})],1)],1)},i=[],n=l("b1a9"),o=l("ed08"),r={name:"file",data:function(){return{files:[],tempFileSize:0}},mounted:function(){this.loadRoot()},methods:{formatSizeHuman:o["e"],deleteFiles:function(e,t){var l=this;n["a"].deleteFiles(e.path).then((function(t){l.setRowDelete(e)}))},setRowDelete:function(e){if(this.$set(e,"delete",!0),e.children)for(var t=0;t<e.children.length;t++)this.setRowDelete(e.children[t])},downloadFile:function(e){n["a"].fileDownload(e)},calcDirectorySize:function(e){var t=this;n["a"].calcDirectorySize(e.path).then((function(l){t.$set(e,"size",l.data)}))},formatTime:function(e,t){return Object(o["h"])(e.lastUpdateTime,void 0)},formatSize:function(e,t){return Object(o["e"])(e.size)},loadRoot:function(){var e=this;n["a"].childNames("").then((function(t){e.files=t.data;for(var l=0,a=0;a<e.files.length;a++)l+=e.files[a].size;e.tempFileSize=l}))},loadChild:function(e,t,l){var a=0===e.level?"":e.path;n["a"].childNames(a).then((function(e){l(e.data)}))}}},s=r,c=l("2877"),d=Object(c["a"])(s,a,i,!1,null,"1ccd6a0a",null);t["default"]=d.exports}}]);