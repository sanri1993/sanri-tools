(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-0e95ff0e"],{"0c26":function(t,e,n){"use strict";n("a1c6")},"91d0":function(t,e,n){"use strict";n.r(e);var i=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"app-container"},[n("div",{staticClass:"panel panel-default"},[n("div",{staticClass:"panel-heading"},[t._v("设置分词工具和翻译工具")]),n("div",{staticClass:"panel-body"},[n("el-radio-group",{model:{value:t.input.tokenizer,callback:function(e){t.$set(t.input,"tokenizer",e)},expression:"input.tokenizer"}},t._l(t.tokenizers,(function(t){return n("el-radio",{key:t,attrs:{label:t}})})),1),n("el-checkbox-group",{staticClass:"margin-top margin-bottom",model:{value:t.input.englishs,callback:function(e){t.$set(t.input,"englishs",e)},expression:"input.englishs"}},t._l(t.englishs,(function(t){return n("el-checkbox",{key:t,attrs:{label:t}})})),1)],1)]),n("el-row",[n("el-col",{attrs:{span:8}},[n("div",{staticClass:"panel panel-default"},[n("div",{staticClass:"panel-heading"},[t._v("业务词翻译")]),n("div",{staticClass:"panel-body"},[n("el-input",{attrs:{size:"small",placeholder:"输入业务名来获取英语名称","suffix-icon":"el-icon-search"},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.queryName(e)}},model:{value:t.input.keyword,callback:function(e){t.$set(t.input,"keyword",e)},expression:"input.keyword"}}),t.translates&&t.translates.length>0?t._l(t.translates,(function(e){return n("p",{key:e},[t._v(t._s(e))])})):[t._v("暂无结果")]],2)])]),n("el-col",{staticClass:"margin-left",attrs:{span:15}},[n("div",{staticClass:"panel panel-default"},[n("div",{staticClass:"panel-heading"},[t._v("业务词配置")]),n("div",{staticClass:"panel-body"},[n("el-button",{attrs:{type:"text"},on:{click:function(e){t.dialog.visible=!0}}},[n("i",{staticClass:"fa fa-plus"}),t._v(" 新业务")]),n("small",{staticClass:"text-forestgreen "},[t._v("同名直接覆盖")]),n("el-row",[n("el-col",{attrs:{span:8}},[n("el-table",{staticStyle:{width:"100%"},attrs:{data:t.bizTableData,border:"",stripe:"",size:"mini"},on:{"selection-change":t.choseBizs}},[n("el-table-column",{attrs:{type:"selection",width:"55"}}),n("el-table-column",{attrs:{prop:"biz",label:"biz"}})],1)],1),n("el-col",{staticClass:"margin-left",attrs:{span:15}},[n("el-input",{attrs:{type:"textarea",autosize:{minRows:5,maxRows:10},placeholder:"当前业务词"},model:{value:t.contentMerge,callback:function(e){t.contentMerge=e},expression:"contentMerge"}})],1)],1)],1)])])],1),n("el-dialog",{attrs:{visible:t.dialog.visible,title:"新业务"},on:{"update:visible":function(e){return t.$set(t.dialog,"visible",e)}}},[n("el-button",{attrs:{type:"primary",size:"small"},on:{click:t.commitBiz}},[t._v("保存")]),n("el-input",{staticClass:"margin-top margin-bottom",attrs:{size:"small",placeholder:"当前业务名称"},model:{value:t.dialog.input.biz,callback:function(e){t.$set(t.dialog.input,"biz",e)},expression:"dialog.input.biz"}}),n("el-input",{attrs:{type:"textarea",autosize:{minRows:5,maxRows:10},placeholder:"业务词映射"},model:{value:t.dialog.input.content,callback:function(e){t.$set(t.dialog.input,"content",e)},expression:"dialog.input.content"}})],1)],1)},a=[],s=(n("d81d"),n("a15b"),n("b775")),l={englishs:function(){return s["a"].get("/name/englishs")},tokenizers:function(){return s["a"].get("/name/tokenizers")},bizs:function(){return s["a"].get("/name/bizs")},content:function(t){return s["a"].get("/name/detail/".concat(t))},writeBizContent:function(t,e){return s["a"].post("/name/mirror/write/".concat(t),e)},translate:function(t,e,n,i){var a=n.join(","),l=i.join(",");return s["a"].get("/name/translate",{params:{orginChars:t,tokenizer:e,tranlates:a,bizs:l}})},contentMerge:function(t){return s["a"].get("/name/content/bizs",{params:{bizs:t.join(",")}})}},o={name:"translate",data:function(){return{input:{tokenizer:null,englishs:[],keyword:null,selectedBizs:[]},bizs:[],englishs:[],tokenizers:[],translates:[],contentMerge:null,dialog:{visible:!1,input:{biz:null,content:null}}}},computed:{bizTableData:function(){return this.bizs.map((function(t){return{biz:t}}))}},mounted:function(){var t=this;this.reloadBizs(),l.tokenizers().then((function(e){t.tokenizers=e.data,t.tokenizers.length>0&&(t.input.tokenizer=t.tokenizers[0])})),l.englishs().then((function(e){t.englishs=e.data,t.englishs.length>0&&(t.input.englishs=[t.englishs[0]])}))},methods:{queryName:function(){var t=this;this.input.keyword&&l.translate(this.input.keyword,this.input.tokenizer,this.input.englishs,this.input.selectedBizs).then((function(e){t.translates=e.data}))},reloadBizs:function(){var t=this;l.bizs().then((function(e){t.bizs=e.data}))},commitBiz:function(){var t=this;this.dialog.input.biz&&this.dialog.input.content?l.writeBizContent(this.dialog.input.biz,this.dialog.input.content).then((function(e){t.dialog.visible=!1,t.reloadBizs()})):this.$message("输入业务标题和内容")},choseBizs:function(t){var e=this,n=t.map((function(t){return t.biz}));this.input.selectedBizs=n,l.contentMerge(n).then((function(t){e.contentMerge=t.data.join("\n")}))}}},r=o,c=(n("0c26"),n("2877")),u=Object(c["a"])(r,i,a,!1,null,"0a2ac053",null);e["default"]=u.exports},a1c6:function(t,e,n){}}]);