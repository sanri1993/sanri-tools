(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-32da3cd0"],{"11c1":function(a,e,t){var n=t("c437"),s=t("c64e"),c=s;c.v1=n,c.v4=s,a.exports=c},2366:function(a,e){for(var t=[],n=0;n<256;++n)t[n]=(n+256).toString(16).substr(1);function s(a,e){var n=e||0,s=t;return[s[a[n++]],s[a[n++]],s[a[n++]],s[a[n++]],"-",s[a[n++]],s[a[n++]],"-",s[a[n++]],s[a[n++]],"-",s[a[n++]],s[a[n++]],"-",s[a[n++]],s[a[n++]],s[a[n++]],s[a[n++]],s[a[n++]],s[a[n++]]].join("")}a.exports=s},"6caf":function(a,e,t){"use strict";var n=function(){var a=this,e=a.$createElement,t=a._self._c||e;return t("div",[t("span",{staticClass:"append-button-group margin-right"},[t("el-button",{staticClass:"text-danger",attrs:{type:"text",size:"small",icon:"el-icon-refresh"},on:{click:a.refreshConnections}})],1),t("el-select",{attrs:{filterable:"",clearable:"",placeholder:"选择连接",size:"small"},on:{change:a.switchConnection},model:{value:a.connName,callback:function(e){a.connName=e},expression:"connName"}},a._l(a.connections,(function(a){return t("el-option",{key:a,attrs:{label:a,value:a}})})),1),a.catalogs.length>0?[t("el-select",{staticClass:"margin-left",attrs:{filterable:"",clearable:"",placeholder:"选择 catalog",size:"small"},on:{change:function(e){return a.switchNamespace("catalog")}},model:{value:a.namespace.catalog,callback:function(e){a.$set(a.namespace,"catalog",e)},expression:"namespace.catalog"}},a._l(a.catalogs,(function(a){return t("el-option",{key:a,attrs:{label:a,value:a}})})),1)]:a._e(),a.schemas.length>0?[t("el-select",{staticClass:"margin-left",attrs:{filterable:"",clearable:"",placeholder:"选择 schema",size:"small"},on:{change:function(e){return a.switchNamespace("schema")}},model:{value:a.namespace.schema,callback:function(e){a.$set(a.namespace,"schema",e)},expression:"namespace.schema"}},a._l(a.schemas,(function(a){return t("el-option",{key:a.schema,attrs:{label:a.schema,value:a.schema}})})),1)]:a._e()],2)},s=[],c=t("c48b"),o=t("b1a9"),l={name:"DataSourceChose",data:function(){return{connName:null,namespace:{catalog:null,schema:null},connections:[],catalogs:[],schemas:[]}},methods:{switchConnection:function(a){var e=this;this.connName=a,this.$emit("changeConnName",a),this.catalogs.length=0,this.schemas.length=0,this.connName&&c["a"].catalogs(this.connName).then((function(a){e.catalogs=a.data,e.catalogs&&e.catalogs.length>0?(e.namespace.catalog=e.catalogs[0],e.$emit("changeNamespace",e.namespace),e.switchNamespace("catalog")):c["a"].schemas(e.connName).then((function(a){e.schemas=a.data,e.schemas&&e.schemas.length>0&&(e.namespace.schema=e.schemas[0].schema,e.$emit("changeNamespace",e.namespace))}))}))},refreshConnections:function(){var a=this;o["a"].security.moduleConnectNames("database").then((function(e){a.connections=e.data}))},switchNamespace:function(a){var e=this;"catalog"===a?(this.$emit("changeNamespace",this.namespace),this.schemas.length=0,c["a"].catalogSchemas(this.connName,this.namespace.catalog).then((function(a){e.schemas=a.data,e.schemas&&e.schemas.length>0&&(e.namespace.schema=e.schemas[0].schema)}))):"schema"===a&&this.$emit("changeNamespace",this.namespace)},getConnName:function(){return this.connName},getNamespace:function(){return this.namespace}},mounted:function(){this.refreshConnections()}},r=l,i=t("2877"),m=Object(i["a"])(r,n,s,!1,null,"6ba25e74",null);e["a"]=m.exports},a0b7:function(a,e,t){"use strict";t.r(e);var n=function(){var a=this,e=a.$createElement,t=a._self._c||e;return t("div",{staticClass:"app-container"},[t("el-row",[t("data-source-chose",{on:{changeConnName:a.changeConn,changeNamespace:a.changeNamespace}})],1),t("el-row",[t("el-col",{attrs:{span:10}},[t("p",{staticClass:"text-forestgreen"},[a._v("搜索到数据表后, 点击表格行可以获取数据表详细信息")]),t("TableSearch",{attrs:{"conn-name":a.connName,namespace:a.namespace},on:{"click-row":a.showTableProperties}})],1),a.connName&&a.tableMeta&&a.tableMeta.table?t("el-col",{attrs:{span:14}},[t("TableProperties",{ref:"tableProperties",attrs:{tableMeta:a.tableMeta,"conn-name":a.connName}})],1):a._e()],1)],1)},s=[],c=t("c068"),o=function(){var a=this,e=a.$createElement,t=a._self._c||e;return t("div",{staticClass:"padding"},[t("div",[t("p",[t("b",[a._v("连接: ")]),a._v(a._s(a.connName))]),t("p",[t("b",[a._v("数据表: ")]),a._v(a._s(a.tableMeta&&a.tableMeta.table?a.tableMeta.table.actualTableName.fullName:"未选中表, 请点击左边表格行后操作")+" "),-1!==a.tableOperator.dataCount?t("span",[t("b",[a._v("数据量:")]),a._v(" "+a._s(a.tableOperator.dataCount))]):a._e()])]),t("el-tabs",{attrs:{"tab-position":"top","active-name":a.tabs.activeTabName},on:{"tab-click":a.switchTab}},[t("el-tab-pane",{attrs:{label:"字段",name:"columns"}},[a.tableMeta?[t("el-table",{staticStyle:{width:"100%"},attrs:{data:a.tableMeta.columns,border:"",stripe:"",height:"500",size:"mini"}},[t("el-table-column",{attrs:{type:"index",width:"50"}}),t("el-table-column",{attrs:{prop:"columnName",label:"列名",width:"200"}}),t("el-table-column",{attrs:{label:"必填",width:"50"},scopedSlots:a._u([{key:"default",fn:function(e){return[t("span",[a._v(a._s(e.row.nullable?"N":"Y"))])]}}],null,!1,3400424101)}),t("el-table-column",{attrs:{label:"类型",width:"170"},scopedSlots:a._u([{key:"default",fn:function(e){return[t("span",[a._v(a._s(a.showTableColumnType(e.row)))])]}}],null,!1,1421780858)}),t("el-table-column",{attrs:{prop:"remark",label:"注释"}})],1)]:a._e()],2),t("el-tab-pane",{attrs:{label:"索引",name:"indices"}},[t("el-table",{staticStyle:{width:"100%"},attrs:{data:a.indices,border:"",stripe:"",size:"mini"}},[t("el-table-column",{attrs:{type:"index",width:"50"}}),t("el-table-column",{attrs:{prop:"indexName",label:"索引名称",width:"200"}}),t("el-table-column",{attrs:{prop:"columnName",label:"列名",width:"150"}}),t("el-table-column",{attrs:{label:"索引类型"},scopedSlots:a._u([{key:"default",fn:function(e){return[t("span",[a._v(a._s(a.showIndexType(e.row)))])]}}])}),t("el-table-column",{attrs:{prop:"ordinalPosition",label:"顺序"}}),t("el-table-column",{attrs:{label:"unique"},scopedSlots:a._u([{key:"default",fn:function(e){return[t("span",[a._v(a._s(e.row.unique))])]}}])})],1)],1),t("el-tab-pane",{attrs:{label:"表标记",name:"tag"}},[t("el-button",{attrs:{disabled:!a.tableMeta||!a.connName,size:"small",type:"primary"},on:{click:function(e){a.tableMarkDialog.visible=!0}}},[a._v("配置")]),t("el-row",{staticClass:"margin-top"},[t("el-col",a._l(a.tableMarkDialog.tableTags,(function(e){return t("el-tag",{key:e,staticClass:"margin-left",attrs:{size:"medium",type:""},on:{close:a.dropTag}},[a._v(a._s(e))])})),1)],1)],1),t("el-tab-pane",{attrs:{label:"表关系",name:"relation"}},[t("el-button-group",[t("el-button",{attrs:{size:"small",type:"text",icon:"el-icon-bottom",disabled:"hierarchy"===a.relationType},on:{click:a.hierarchy}},[a._v("hierarchy")]),t("el-button",{attrs:{size:"small",type:"text",icon:"el-icon-top",disabled:"superTypes"===a.relationType},on:{click:a.superTypes}},[a._v("superTypes")])],1),t("el-tree",{ref:"relation",attrs:{data:a.relations,"default-expand-all":!0}})],1),t("el-tab-pane",{attrs:{label:"表操作",name:"operator"}},[t("el-button-group",[t("el-button",{attrs:{disabled:!a.tableMeta||!a.connName,type:"white",size:"small",icon:"el-icon-view"},on:{click:a.loadTableDataTotal}},[a._v("表数据量")]),t("el-button",{attrs:{disabled:!a.tableMeta||!a.connName,type:"white",size:"small",icon:"el-icon-delete"},on:{click:a.emptyTable}},[a._v("清空表")]),t("el-button",{attrs:{disabled:!a.tableMeta||!a.connName,type:"white",size:"small",icon:"el-icon-refresh"},on:{click:a.refreshMeta}},[a._v("更新元数据")])],1)],1)],1),t("el-dialog",{attrs:{visible:a.tableMarkDialog.visible,title:a.tableMarkDialog.title},on:{"update:visible":function(e){return a.$set(a.tableMarkDialog,"visible",e)}}},[t("el-row",[t("el-col",{attrs:{span:24}},[t("el-transfer",{attrs:{data:a.tagsComputed,titles:["标签库","选中的标签"]},model:{value:a.tableMarkDialog.tableTags,callback:function(e){a.$set(a.tableMarkDialog,"tableTags",e)},expression:"tableMarkDialog.tableTags"}})],1)],1),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{attrs:{type:"primary"},on:{click:a.storeTableTags}},[a._v("确 定")])],1)],1)],1)},l=[],r=(t("b0c0"),t("d81d"),t("c48b")),i={name:"TableProperties",props:{tableMeta:{type:Object},connName:String},data:function(){return{indices:[],primaryKeys:[],relations:[],relationType:"hierarchy",tabs:{activeTabName:"columns"},tableMarkDialog:{visible:!1,title:"表标记",tags:[],tableTags:[]},tableOperator:{dataCount:-1}}},methods:{refreshMeta:function(){var a=this;r["a"].refreshTable(this.connName,this.tableMeta.table.actualTableName).then((function(e){a.tableMeta=e.data}))},loadTableDataTotal:function(){var a=this,e=this.tableMeta.table.actualTableName;r["a"].executeQuery(this.connName,e.namespace,"select count(*) from "+e.tableName).then((function(e){if(a.tableOperator.dataCount=-1,e.data&&e.data.length>0){var t=e.data[0],n=t.headers[0].columnName;a.tableOperator.dataCount=t.rows[0][n]+""}}))},emptyTable:function(){var a=this,e=this.tableMeta.table.actualTableName;this.$confirm("确定清空数据表 "+e.tableName+" 此操作不可逆?","警告",{type:"warning"}).then((function(){r["a"].emptyTable(a.connName,e).then((function(e){a.$message("清除成功")}))})).catch((function(){}))},storeTableTags:function(){var a=this,e=this.tableMeta.table.actualTableName,t={actualTableName:e,tags:this.tableMarkDialog.tableTags};r["a"].markTag(this.connName,[t]).then((function(e){a.tableMarkDialog.visible=!1,a.tags=a.tableMarkDialog.tableTags}))},dropTag:function(a){},showTableColumnType:function(a){var e="("+a.columnSize+")";return a.decimalDigits&&(e="("+a.columnSize+","+a.decimalDigits+")"),a.typeName+e},switchTab:function(a,e){var t=this;if(this.tabs.activeTabName=a.name,this.tableMeta&&this.tableMeta.table)switch(a.name){case"indices":r["a"].indices(this.connName,this.tableMeta.table.actualTableName).then((function(a){t.indices=a.data}));break;case"tag":r["a"].tableTags(this.connName,this.tableMeta.table.actualTableName).then((function(a){t.tags=a.data,t.tableMarkDialog.tableTags=a.data}));break;case"relation":this.hierarchy();break}},hierarchy:function(){var a=this;this.relationType="hierarchy",r["a"].hierarchy(this.connName,this.tableMeta.table.actualTableName).then((function(e){a.relations=[e.data]}))},superTypes:function(){var a=this;this.relationType="superTypes",r["a"].superTypes(this.connName,this.tableMeta.table.actualTableName).then((function(e){a.relations=[e.data]}))},cleanLastTableData:function(){this.tableOperator.dataCount=-1,this.tableMeta=null},activeCurrentTab:function(){this.tableOperator.dataCount=-1,this.switchTab({name:this.tabs.activeTabName})},showIndexType:function(a){switch(a.indexType){case 0:return"tableIndexStatistic";case 1:return"tableIndexClustered";case 2:return"tableIndexHashed";case 3:return"BTree"}return"unknow"}},computed:{tagsComputed:function(){return this.tableMarkDialog.tags?this.tableMarkDialog.tags.map((function(a){return{key:a,label:a}})):[]}},mounted:function(){var a=this;r["a"].tags().then((function(e){a.tableMarkDialog.tags=e.data}))}},m=i,u=t("2877"),b=Object(u["a"])(m,o,l,!1,null,"39e2a6b8",null),d=b.exports,h=t("6caf"),p={name:"metadata",components:{TableSearch:c["a"],TableProperties:d,DataSourceChose:h["a"]},data:function(){return{tableMeta:{},connName:null,namespace:null}},methods:{changeConn:function(a){this.connName=a,this.tableMeta={}},changeNamespace:function(a){this.namespace=a,this.tableMeta={}},showTableProperties:function(a){var e=this;this.tableMeta=a,this.$nextTick((function(){e.$refs.tableProperties.activeCurrentTab()}))}},mounted:function(){},computed:{}},f=p,g=Object(u["a"])(f,n,s,!1,null,"61754fc8",null);e["default"]=g.exports},c068:function(a,e,t){"use strict";var n=function(){var a=this,e=a.$createElement,t=a._self._c||e;return t("div",[t("el-row",{staticClass:"margin-bottom margin-top"},[t("el-col",{attrs:{span:18}},[t("el-input",{attrs:{disabled:!a.connName,size:"small",placeholder:"输入表名/字段名/表注释/列注释/table:表名/column:列名/tag:表标记","suffix-icon":"el-icon-search"},nativeOn:{keyup:[function(e){return!e.type.indexOf("key")&&a._k(e.keyCode,"enter",13,e.key,"Enter")?null:a.searchTables(e)},function(e){return a.keyupSearch(e)}]},model:{value:a.search.keyword,callback:function(e){a.$set(a.search,"keyword",e)},expression:"search.keyword"}},[t("el-select",{staticStyle:{width:"100px"},attrs:{slot:"prepend",placeholder:"请选择"},slot:"prepend",model:{value:a.search.searchSchema,callback:function(e){a.$set(a.search,"searchSchema",e)},expression:"search.searchSchema"}},[t("el-option",{attrs:{label:"全局",value:""}}),t("el-option",{attrs:{label:"table",value:"table"}}),t("el-option",{attrs:{label:"column",value:"column"}}),t("el-option",{attrs:{label:"tag",value:"tag"}})],1)],1)],1),t("el-col",{attrs:{span:6}},[t("el-button-group",{staticClass:"margin-left"},[t("el-button",{attrs:{size:"small",type:"primary",disabled:!a.connName},on:{click:a.generateDoc}},[a._v("下载文档")])],1)],1)],1),t("el-row",[t("el-col",{attrs:{span:24}},[t("el-table",{directives:[{name:"loading",rawName:"v-loading",value:a.loading,expression:"loading"}],ref:"searchTable",attrs:{data:a.tables,border:"",stripe:"",height:"500","row-key":function(a){return"table:"+a.table.actualTableName.tableName+1e3*Math.random()},size:"mini","highlight-current-row":""},on:{"row-click":a.clickTableRow}},[t("el-table-column",{attrs:{type:"index",width:"50"}}),a.selection?[t("el-table-column",{attrs:{type:"selection",width:"55"}})]:a._e(),t("el-table-column",{attrs:{prop:"table.actualTableName.tableName",label:"表名称",width:"260"}}),a.showRemark?[t("el-table-column",{attrs:{prop:"table.remark",label:"表说明"}})]:a._e()],2)],1)],1)],1)},s=[],c=(t("498a"),t("ac1f"),t("841c"),t("c48b")),o=t("b1a9"),l={name:"TableSearch",props:{connName:String,namespace:Object,selection:{type:Boolean,default:!1},showRemark:{type:Boolean,default:!0}},data:function(){return{search:{searchSchema:null,keyword:null},tables:[],loading:!1}},methods:{keyupSearch:function(){this.keyword&&this.keyword.length>10&&this.searchTables()},searchTables:function(){var a=this;this.keyword&&(this.keyword=this.keyword.trim()),this.loading=!0,c["a"].searchTables(this.connName,this.namespace.catalog,[this.namespace.schema],this.search.searchSchema,this.search.keyword).then((function(e){a.tables=e.data,a.loading=!1})).catch((function(e){a.loading=!1}))},generateDoc:function(){c["a"].exportDoc(this.connName,this.namespace.catalog,[this.namespace.schema],this.search.searchSchema,this.search.keyword).then((function(a){o["a"].fileDownload(a.data)}))},getChoseTables:function(){return this.$refs.searchTable.selection},clickTableRow:function(a,e,t){this.$emit("click-row",a,e)}}},r=l,i=t("2877"),m=Object(i["a"])(r,n,s,!1,null,"e183210c",null);e["a"]=m.exports},c437:function(a,e,t){var n,s,c=t("e1f4"),o=t("2366"),l=0,r=0;function i(a,e,t){var i=e&&t||0,m=e||[];a=a||{};var u=a.node||n,b=void 0!==a.clockseq?a.clockseq:s;if(null==u||null==b){var d=c();null==u&&(u=n=[1|d[0],d[1],d[2],d[3],d[4],d[5]]),null==b&&(b=s=16383&(d[6]<<8|d[7]))}var h=void 0!==a.msecs?a.msecs:(new Date).getTime(),p=void 0!==a.nsecs?a.nsecs:r+1,f=h-l+(p-r)/1e4;if(f<0&&void 0===a.clockseq&&(b=b+1&16383),(f<0||h>l)&&void 0===a.nsecs&&(p=0),p>=1e4)throw new Error("uuid.v1(): Can't create more than 10M uuids/sec");l=h,r=p,s=b,h+=122192928e5;var g=(1e4*(268435455&h)+p)%4294967296;m[i++]=g>>>24&255,m[i++]=g>>>16&255,m[i++]=g>>>8&255,m[i++]=255&g;var v=h/4294967296*1e4&268435455;m[i++]=v>>>8&255,m[i++]=255&v,m[i++]=v>>>24&15|16,m[i++]=v>>>16&255,m[i++]=b>>>8|128,m[i++]=255&b;for(var y=0;y<6;++y)m[i+y]=u[y];return e||o(m)}a.exports=i},c48b:function(a,e,t){"use strict";var n=t("5530"),s=(t("a15b"),t("b775")),c=t("4328"),o=t.n(c),l=t("11c1");e["a"]={connections:function(){return s["a"].get("/security/connect/database/connectNames")},catalogs:function(a){return s["a"].get("/db/metadata/catalogs",{params:{connName:a}})},schemas:function(a){return s["a"].get("/db/metadata/schemas",{params:{connName:a}})},catalogSchemas:function(a,e){return s["a"].get("/db/metadata/filterSchemas",{params:{connName:a,catalog:e}})},indices:function(a,e){var t=e.namespace,n=e.tableName;return s["a"].get("/db/metadata/indices",{params:{connName:a,"namespace.catalog":t.catalog,"namespace.schema":t.schema,tableName:n}})},primaryKeys:function(a,e){var t=e.namespace,n=e.tableName;return s["a"].get("/db/metadata/primaryKeys",{params:{connName:a,"namespace.catalog":t.catalog,"namespace.schema":t.schema,tableName:n}})},tables:function(a,e,t){return s["a"].get("/db/metadata/tables",{params:{connName:a,catalog:e,schema:t}})},refreshTable:function(a,e){var t=e.namespace,n=e.tableName;return s["a"].get("/db/metadata/refreshTable",{params:{connName:a,"namespace.catalog":t.catalog,"namespace.schema":t.schema,tableName:n}})},searchTables:function(a,e,t,c,o){return s["a"].get("/db/metadata/searchTables",{params:Object(n["a"])(Object(n["a"])({connName:a,catalog:e},{schemas:t.join(",")}),{},{searchSchema:c,keyword:o})})},compareMeta:function(a){return s["a"].post("/db/metadata/compare",a,s["b"].json)},compareMetaChangeSqls:function(a){return s["a"].post("/db/metadata/compare/changeSqls",a,s["b"].json)},executeQuery:function(a,e,t){return s["a"].post("/db/data/executeQuery",{connName:a,namespace:e,sqls:[t]})},groups:function(a,e){return s["a"].get("/db/data/config/groups",{params:Object(n["a"])({connName:a},e)})},dataIds:function(a,e,t){return s["a"].get("/db/data/config/dataIds",{params:Object(n["a"])(Object(n["a"])({connName:a},e),{},{groupId:t})})},content:function(a,e,t,c){return s["a"].get("/db/data/config/content",{params:Object(n["a"])(Object(n["a"])({connName:a},e),{},{groupId:t,dataId:c})})},emptyTable:function(a,e){var t=e.namespace,n=e.tableName;return s["a"].post("/db/data/emptyTable",o.a.stringify({connName:a,"namespace.catalog":t.catalog,"namespace.schema":t.schema,tableName:n}))},dataChangeSqls:function(a){return s["a"].post("/db/data/dataChangeSqls",a,s["b"].json)},loadRandomMethods:function(){return s["a"].get("/db/data/loadRandomMethods")},tableRandomData:function(a,e,t,n){var c={connName:a,actualTableName:e,columnMappers:t,size:n};return s["a"].post("/db/data/singleTableRandomData",c,s["b"].json)},checkDirtyData:function(a,e){return s["a"].get("/db/data/checkDirtyData",{params:Object(n["a"])({connName:a},e)})},renameStrategies:function(){return s["a"].get("/db/code/renameStrategies")},buildJavaBean:function(a){return s["a"].post("/db/code/build/javaBean",a,s["b"].json)},buildMapper:function(a){return s["a"].post("/db/code/build/mapper",a,s["b"].json)},templates:function(){return s["a"].get("/db/code/templates")},uploadTemplate:function(a){return s["a"].post("/db/code/template/upload",a)},templateContent:function(a){return s["a"].get("/db/code/".concat(a,"/content"))},override:function(a){return s["a"].post("/db/code/override",a,s["b"].json)},codeSchemas:function(){return s["a"].get("/db/code/schemas")},codeSchemaTemplates:function(a){return s["a"].get("/db/code/".concat(a,"/templates"))},codePreview:function(a){return s["a"].post("/db/code/template/code/preview",a)},codeGenerate:function(a){return s["a"].post("/db/code/template/code/generator",a)},tags:function(){return s["a"].get("/db/metadata/extend/mark/tags")},markTag:function(a,e){return s["a"].post("/db/metadata/extend/mark/config/".concat(a,"/tableMark"),e,s["b"].json)},tableTags:function(a,e){var t=e.namespace,n=e.tableName;return s["a"].get("/db/metadata/extend/mark/tableTags",{params:{connName:a,"namespace.catalog":t.catalog,"namespace.schema":t.schema,tableName:n}})},tagTables:function(a,e,t,n){return s["a"].get("/db/metadata/extend/mark/tagTables",{params:{connName:a,catalog:e,schema:t,tag:n}})},configRelation:function(a){return s["a"].post("/db/metadata/extend/relation/config",a,s["b"].json)},configRelationFromSql:function(a){return s["a"].post("/db/metadata/extend/relation/config/fromSql",a,s["b"].json)},parentRelations:function(a,e){return s["a"].get("/db/metadata/extend/relation/parents",{params:Object(n["a"])({connName:a},e)})},childRelations:function(a,e){return s["a"].get("/db/metadata/extend/relation/childs",{params:Object(n["a"])({connName:a},e)})},hierarchy:function(a,e){var t=e.namespace,n=e.tableName;return s["a"].get("/db/metadata/extend/relation/hierarchy",{params:{connName:a,"namespace.catalog":t.catalog,"namespace.schema":t.schema,tableName:n}})},superTypes:function(a,e){var t=e.namespace,n=e.tableName;return s["a"].get("/db/metadata/extend/relation/superTypes",{params:{connName:a,"namespace.catalog":t.catalog,"namespace.schema":t.schema,tableName:n}})},exportPreview:function(a,e,t){var n=l.v1();return s["a"].post("/db/data/exportPreview",{connName:a,namespace:e,sqls:[t],traceId:n},s["b"].json)},exportData:function(a,e,t){var n=l.v1();return s["a"].post("/db/data/exportData",{connName:a,namespace:e,sqls:[t],traceId:n},s["b"].json)},exportDoc:function(a,e,t,c,o){return s["a"].get("/db/doc/export",{params:Object(n["a"])(Object(n["a"])({connName:a,catalog:e,searchSchema:c},{schemas:t.join(",")}),{},{keyword:o})})}}},c64e:function(a,e,t){var n=t("e1f4"),s=t("2366");function c(a,e,t){var c=e&&t||0;"string"==typeof a&&(e="binary"===a?new Array(16):null,a=null),a=a||{};var o=a.random||(a.rng||n)();if(o[6]=15&o[6]|64,o[8]=63&o[8]|128,e)for(var l=0;l<16;++l)e[c+l]=o[l];return e||s(o)}a.exports=c},e1f4:function(a,e){var t="undefined"!=typeof crypto&&crypto.getRandomValues&&crypto.getRandomValues.bind(crypto)||"undefined"!=typeof msCrypto&&"function"==typeof window.msCrypto.getRandomValues&&msCrypto.getRandomValues.bind(msCrypto);if(t){var n=new Uint8Array(16);a.exports=function(){return t(n),n}}else{var s=new Array(16);a.exports=function(){for(var a,e=0;e<16;e++)0===(3&e)&&(a=4294967296*Math.random()),s[e]=a>>>((3&e)<<3)&255;return s}}}}]);