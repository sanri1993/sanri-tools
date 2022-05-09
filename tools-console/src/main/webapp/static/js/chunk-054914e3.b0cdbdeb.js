(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-054914e3"],{cf64:function(e,t,n){"use strict";n("dd75")},dd75:function(e,t,n){},e7c4:function(e,t,n){"use strict";n.r(t);var a=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{staticClass:"app-container"},[n("el-row",[n("connect-chose",{attrs:{module:"fastdfs"},on:{change:e.changeConnName}})],1),n("el-row",[n("el-tabs",{directives:[{name:"loading",rawName:"v-loading",value:e.uploadLoading,expression:"uploadLoading"}],attrs:{"active-name":"preview"}},[n("el-tab-pane",{attrs:{label:"预览下载",name:"preview"}},[n("el-form",{attrs:{size:"small","label-width":"120px"}},[n("el-form-item",[n("el-button-group",[n("el-button",{attrs:{size:"small",type:"primary",icon:"el-icon-view",disabled:!e.dfsIds,title:"点击切换下一张图"},on:{click:e.preview}},[e._v("预览")]),n("el-button",{attrs:{size:"small",icon:"el-icon-download",disabled:!e.dfsIds},on:{click:e.download}},[e._v("下载")])],1)],1),n("el-form-item",{attrs:{label:"dfsId"}},[n("el-input",{attrs:{disabled:!e.connName,size:"small",clearable:"",placeholder:"填写 dfsId , 可以填写多个, 使用逗号或者换行进行分隔",type:"textarea",autosize:{minRows:1,maxRows:5}},nativeOn:{keyup:function(t){return!t.type.indexOf("key")&&e._k(t.keyCode,"enter",13,t.key,"Enter")?null:e.preview(t)}},model:{value:e.dfsIds,callback:function(t){e.dfsIds=t},expression:"dfsIds"}})],1)],1),e.image?[n("el-row",[n("el-col",{staticClass:"padding",attrs:{span:4}},[n("el-descriptions",{attrs:{column:1,size:"small",title:"文件信息",border:""}},[n("el-descriptions-item",{attrs:{label:"数据来源"}},[e._v(e._s(e.fileInfo.sourceIpAddr))]),n("el-descriptions-item",{attrs:{label:"文件类型"}},[e._v(e._s(1===e.fileInfo.fileType?"常规文件":e.fileInfo.fileType))]),n("el-descriptions-item",{attrs:{label:"文件大小"}},[e._v(e._s(e.formatSizeHuman(e.fileInfo.fileSize)))]),n("el-descriptions-item",{attrs:{label:"创建时间"}},[e._v(e._s(e.fileInfo.createTimestamp))]),n("el-descriptions-item",{attrs:{label:"CRC32"}},[e._v(e._s(e.fileInfo.crc32))])],1)],1),n("el-col",{attrs:{span:18}},[n("el-image",{staticStyle:{width:"100%"},attrs:{src:e.image,"preview-src-list":[e.image]}})],1)],1)]:[e._v("填写 dfsId 地址来预览图片")]],2),n("el-tab-pane",{attrs:{name:"upload",label:"上传"}},[n("el-upload",{ref:"upload",staticClass:"upload-demo",attrs:{action:"#","on-change":e.setUploadFiles,multiple:!0,"auto-upload":!1}},[n("el-button",{attrs:{slot:"trigger",size:"small",type:"primary"},slot:"trigger"},[e._v("选取文件")]),n("el-button",{staticStyle:{"margin-left":"10px"},attrs:{size:"small",type:"success"},on:{click:e.submitUpload}},[e._v("上传到服务器")]),n("div",{staticClass:"el-upload__tip",attrs:{slot:"tip"},slot:"tip"},[e._v("别上传太大的文件, 10M 以内吧")])],1),e.uploadResult.length>0?n("div",[n("el-alert",{attrs:{type:"success"}},[e._v("上传结果")]),n("ul",{staticClass:"list-group"},e._l(e.uploadResult,(function(t){return n("li",{staticClass:"list-group-item",staticStyle:{display:"flex","justify-content":"space-between"}},[n("b",[e._v(" "+e._s(t.key)+" ")]),n("span",[e._v(" "+e._s(t.value)+" ")])])})),0)],1):e._e()],1)],1)],1)],1)},s=[],o=(n("ac1f"),n("1276"),n("d3b7"),n("ace4"),n("5cc6"),n("9a8c"),n("a975"),n("735e"),n("c1ac"),n("d139"),n("3a7b"),n("d5d6"),n("82f8"),n("e91f"),n("60bd"),n("5f96"),n("3280"),n("3fcc"),n("ca91"),n("25a1"),n("cd26"),n("3c5d"),n("2954"),n("649e"),n("219c"),n("170b"),n("b39a"),n("72f7"),n("d81d"),n("b0c0"),function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",[n("el-select",{attrs:{size:"small",clearable:"",filterable:""},on:{change:e.switchConnect},model:{value:e.choseConnect,callback:function(t){e.choseConnect=t},expression:"choseConnect"}},e._l(e.connects,(function(e){return n("el-option",{key:e,attrs:{value:e,label:e}})})),1),n("el-button",{staticClass:"margin-left",attrs:{type:"text",icon:"el-icon-edit",size:"small"},on:{click:e.createConnect}},[e._v("新建")])],1)}),i=[],l=n("b1a9"),c={name:"ConnectChose",props:{module:String},data:function(){return{connects:[],choseConnect:null}},mounted:function(){this.refreshConnects()},methods:{createConnect:function(){window.location.hash="/core/connect"},refreshConnects:function(){var e=this;l["a"].security.moduleConnectNames(this.module).then((function(t){e.connects=t.data,e.connects&&e.connects.length>0&&e.switchConnect(e.connects[0])}))},switchConnect:function(e){this.choseConnect=e,this.$emit("change",e)}}},r=c,d=n("2877"),u=Object(d["a"])(r,o,i,!1,null,"2583b560",null),f=u.exports,p=n("b775"),m=n("ed08"),h={fileInfo:function(e,t){return p["a"].get("/fastdfs/fileInfo",{params:{connName:e,dfsId:t}})},preview:function(e,t){return p["a"].get("/fastdfs/preview",{params:{connName:e,dfsId:t},responseType:"arraybuffer"})},download:function(e,t){p["a"].get("/fastdfs/download",{params:{connName:e,dfsIds:t},responseType:"blob"}).then((function(e){Object(m["c"])(e,{})})).catch((function(e){console.log(e)}))},uploadFiles:function(e){return p["a"].post("/fastdfs/uploadFiles",e)}},b=n("6191"),v={name:"fastdfs",components:{TemplateExample:b["a"],ConnectChose:f},data:function(){return{connName:null,dfsIds:null,image:null,fileInfo:{},previewCount:0,uploadFiles:[],uploadResult:[],uploadLoading:!1}},created:function(){this.previewCount=0},methods:{formatSizeHuman:m["e"],formatTime:m["f"],changeConnName:function(e){this.connName=e},checkFileInfo:function(e){var t=this;if(this.connName&&this.dfsIds){var n=this.dfsIds.split(/[\n,]/),a=this.previewCount%n.length;h.fileInfo(this.connName,n[a]).then((function(s){t.fileInfo=s.data,e(n[a])}))}else this.$message("填写文件路径信息")},preview:function(){var e=this;this.checkFileInfo((function(t){e.previewCount++,h.preview(e.connName,t).then((function(e){return"data:image/png;base64,"+btoa(new Uint8Array(e.data).reduce((function(e,t){return e+String.fromCharCode(t)}),""))})).then((function(t){e.image=t}))}))},download:function(){var e=this;this.checkFileInfo((function(){h.download(e.connName,e.dfsIds)}))},setUploadFiles:function(e,t){this.uploadFiles=t.map((function(e){return e.raw}))},submitUpload:function(){var e=this;this.uploadLoading=!0;for(var t=new FormData,n=0;n<this.uploadFiles.length;n++)t.append("files",this.uploadFiles[n],this.uploadFiles[n].name);t.append("connName",this.connName),h.uploadFiles(t).then((function(t){e.uploadLoading=!1,e.uploadResult=t.data})).catch((function(t){e.uploadLoading=!1}))}}},g=v,w=(n("cf64"),Object(d["a"])(g,a,s,!1,null,"4a3b8c78",null));t["default"]=w.exports}}]);