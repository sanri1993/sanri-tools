(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-2d0aacb8"],{1311:function(t,e,i){"use strict";i.r(e);var a=function(){var t=this,e=t.$createElement,i=t._self._c||e;return i("div",{staticClass:"app-container"},[i("el-row",[i("el-col",[i("el-button",{staticClass:"margin-right",attrs:{type:"text",icon:"el-icon-refresh"},on:{click:t.reloadAllConnects}}),i("el-select",{attrs:{size:"small"},on:{change:t.loadTopics},model:{value:t.input.connect,callback:function(e){t.$set(t.input,"connect",e)},expression:"input.connect"}},t._l(t.connects,(function(t){return i("el-option",{key:t,attrs:{value:t,label:t}})})),1)],1)],1),i("el-row",{staticClass:"margin-top margin-bottom"},[i("p",[i("el-button",{attrs:{type:"text"},on:{click:t.refreshBrokerMonitor}},[t._v("JMX监控")]),t._v(" brokers("+t._s(t.brokers.length)+"): "+t._s(t.brokers.join(",")))],1)]),i("el-row",{},[i("el-col",{attrs:{span:10}},[i("list-group",{directives:[{name:"loading",rawName:"v-loading",value:t.view.topicsLoading,expression:"view.topicsLoading"}],attrs:{list:t.topicNames},on:{"click-item":t.choseTopic}})],1),i("el-col",{staticClass:"margin-left",attrs:{span:12}},[i("p",[t._v(t._s(t.input.topicName)+" 分区数: "+t._s(t.input.topic.partitions?t.input.topic.partitions.length:0))]),i("el-button-group",[i("el-button",{attrs:{size:"small",type:"danger",icon:"el-icon-delete"},on:{click:t.deleteTopic}},[t._v("删除 "+t._s(t.input.topicName))]),i("el-button",{attrs:{size:"small",plain:"",icon:"el-icon-plus"},on:{click:function(e){t.view.createTopicDialog=!0}}},[t._v("创建新主题")])],1),i("el-tabs",{on:{"tab-click":t.switchTab},model:{value:t.view.activeTabName,callback:function(e){t.$set(t.view,"activeTabName",e)},expression:"view.activeTabName"}},[i("el-tab-pane",{attrs:{label:"JMX监控",name:"zero"}},[t._v(" "+t._s(t.tab.zero.lastUpdateTime)+" "),i("el-button",{attrs:{type:"text",icon:"el-icon-refresh"},on:{click:t.refreshTopicMonitor}}),i("el-table",{directives:[{name:"loading",rawName:"v-loading",value:t.tab.zero.view.loading,expression:"tab.zero.view.loading"}],attrs:{data:t.tab.zero.tableData,border:"",stripe:"",size:"mini"}},[i("el-table-column",{attrs:{prop:"mBean",label:"监控属性",width:"450"}}),i("el-table-column",{attrs:{prop:"oneMinute",label:"1分钟",width:"100"}}),i("el-table-column",{attrs:{prop:"meanRate",label:"速率",width:"100"}}),i("el-table-column",{attrs:{prop:"fiveMinute",label:"5分钟",width:"100"}}),i("el-table-column",{attrs:{prop:"fifteenMinute",label:"15分钟"}})],1)],1),i("el-tab-pane",{attrs:{label:"基本属性",name:"first"}},[i("el-table",{staticStyle:{width:"100%"},attrs:{data:t.input.topic.partitions,border:"",stripe:"",size:"mini"}},[i("el-table-column",{attrs:{type:"expand"},scopedSlots:t._u([{key:"default",fn:function(e){return[i("div",{staticClass:"margin-bottom"},[t._v("Leader: "+t._s(e.row.leader))]),i("p",[t._v("isr: ")]),i("el-table",{staticStyle:{width:"100%"},attrs:{data:e.row.isr,border:"",stripe:"",size:"mini"}},[i("el-table-column",{attrs:{type:"index",width:"50",label:""}}),i("el-table-column",{attrs:{prop:"id",label:"id",width:"180"}}),i("el-table-column",{attrs:{prop:"host",label:"host",width:"180"}}),i("el-table-column",{attrs:{prop:"port",label:"port"}}),i("el-table-column",{attrs:{prop:"jmxPort",label:"jmxPort"}})],1),i("p",[t._v("replicas: ")]),i("el-table",{staticStyle:{width:"100%"},attrs:{data:e.row.replicas,border:"",stripe:"",size:"mini"}},[i("el-table-column",{attrs:{type:"index",width:"50",label:""}}),i("el-table-column",{attrs:{prop:"id",label:"id",width:"180"}}),i("el-table-column",{attrs:{prop:"host",label:"host",width:"180"}}),i("el-table-column",{attrs:{prop:"port",label:"port"}}),i("el-table-column",{attrs:{prop:"jmxPort",label:"jmxPort"}})],1)]}}])}),i("el-table-column",{attrs:{prop:"partition",label:"partition",width:"180"}}),i("el-table-column",{attrs:{prop:"leader",label:"leader",width:"240"},scopedSlots:t._u([{key:"default",fn:function(e){return[i("span",[t._v(t._s(e.row.leader?e.row.leader.host+":"+e.row.leader.port:null))])]}}])}),i("el-table-column",{attrs:{label:"isr",width:"180"},scopedSlots:t._u([{key:"default",fn:function(e){return[i("span",[t._v(t._s(e.row.isr.length))])]}}])}),i("el-table-column",{attrs:{label:"replicas"},scopedSlots:t._u([{key:"default",fn:function(e){return[i("span",[t._v(t._s(e.row.replicas.length))])]}}])})],1)],1),i("el-tab-pane",{attrs:{label:"数据查看",name:"second"}},[i("el-button",{attrs:{type:"text",icon:"el-icon-refresh"},on:{click:t.loadCurrentTopicLogSize}}),t._v(" "+t._s(t.tab.second.lastUpdateTime)+" "),i("el-button",{attrs:{type:"text",icon:"el-icon-search"},on:{click:function(e){t.topicData.view.visible=!0}}}),i("el-table",{directives:[{name:"loading",rawName:"v-loading",value:t.tab.second.view.loading,expression:"tab.second.view.loading"}],attrs:{data:t.tab.second.tableData,border:"",stripe:"",size:"mini"}},[i("el-table-column",{attrs:{prop:"topic",label:"topic",width:"350"}}),i("el-table-column",{attrs:{prop:"partition",label:"partition",width:"70",sortable:""}}),i("el-table-column",{attrs:{prop:"logSize",label:"logSize",width:"80"}}),i("el-table-column",{attrs:{label:"minOffset",width:"100"},scopedSlots:t._u([{key:"default",fn:function(e){return[i("span",[t._v(t._s(t.showMinOffset(e.row)))])]}}])}),i("el-table-column",{attrs:{prop:"time",label:"timestamp",sortable:""}}),i("el-table-column",{attrs:{fixed:"right",label:"操作",width:"80"},scopedSlots:t._u([{key:"default",fn:function(e){return[i("el-button",{attrs:{type:"text",disabled:e.row.minOffset===e.row.logSize,size:"small"},on:{click:function(i){return t.showDataDialog(e.row)}}},[t._v("尾部数据")])]}}])})],1)],1),i("el-tab-pane",{attrs:{label:"数据模拟(JSON)",name:"third"}},[i("el-button-group",[i("el-button",{attrs:{plain:"",size:"small",icon:"el-icon-ice-cream-round"},on:{click:t.lastOneData}},[t._v("随机选一条")]),i("el-button",{attrs:{type:"primary",size:"small",icon:"el-icon-position"},on:{click:t.sendData}},[t._v("发送")])],1),t._v(" 选择分区 "),i("el-input-number",{attrs:{size:"small",min:0,max:t.dialog.configs.max,label:"选择分区"},model:{value:t.input.partition,callback:function(e){t.$set(t.input,"partition",e)},expression:"input.partition"}}),i("json-editor",{directives:[{name:"loading",rawName:"v-loading",value:t.view.onlyOneDataLoading,expression:"view.onlyOneDataLoading"}],attrs:{json:t.input.json},on:{change:t.changeJson}})],1),i("el-tab-pane",{attrs:{label:"数据模拟(Object)",name:"four"}},[i("el-button-group",[i("el-button",{attrs:{type:"primary",size:"small",icon:"el-icon-send"}},[t._v("发送")])],1),t._v(" 选择分区 "),i("el-input-number",{attrs:{min:0,max:t.dialog.configs.max,label:"选择分区"},model:{value:t.input.partition,callback:function(e){t.$set(t.input,"partition",e)},expression:"input.partition"}}),i("json-editor",{attrs:{json:t.input.json},on:{change:t.changeJson}})],1)],1)],1)],1),i("el-dialog",{attrs:{visible:t.view.dialog,title:t.dialogTips,width:"90%"},on:{"update:visible":function(e){return t.$set(t.view,"dialog",e)}}},[t._v(" 选择分区 "),i("el-input-number",{attrs:{min:0,max:t.dialog.configs.max,label:"选择分区",size:"small"},model:{value:t.dialog.configs.partition,callback:function(e){t.$set(t.dialog.configs,"partition",e)},expression:"dialog.configs.partition"}}),i("kafka-data-view",{attrs:{datas:t.dialog.datas,loading:t.view.dialogLoading},on:{"update-data-show":t.reloadData,"next-partition":t.loadNextPartition}})],1),i("el-dialog",{attrs:{visible:t.view.createTopicDialog,title:"创建新主题",width:"30%"},on:{"update:visible":function(e){return t.$set(t.view,"createTopicDialog",e)}}},[i("el-form",{attrs:{size:"small","label-width":"100px"},model:{value:t.input.form,callback:function(e){t.$set(t.input,"form",e)},expression:"input.form"}},[i("el-form-item",{attrs:{label:"主题名称"}},[i("el-input",{model:{value:t.input.form.topic,callback:function(e){t.$set(t.input.form,"topic",e)},expression:"input.form.topic"}})],1),i("el-form-item",{attrs:{label:"分区数"}},[i("el-input-number",{model:{value:t.input.form.partitions,callback:function(e){t.$set(t.input.form,"partitions",e)},expression:"input.form.partitions"}})],1),i("el-form-item",{attrs:{label:"副本数"}},[i("el-input-number",{model:{value:t.input.form.replication,callback:function(e){t.$set(t.input.form,"replication",e)},expression:"input.form.replication"}})],1)],1),i("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[i("el-button",{attrs:{type:"primary"},on:{click:t.createTopic}},[t._v("确 定")])],1)],1),i("el-dialog",{attrs:{visible:t.broker.view.visible,title:t.broker.view.title,width:"50%"},on:{"update:visible":function(e){return t.$set(t.broker.view,"visible",e)}}},[t._v(" "+t._s(t.broker.lastUpdateTime)+" "),i("el-button",{attrs:{type:"text",icon:"el-icon-refresh"},on:{click:t.refreshBrokerMonitor}}),i("el-table",{directives:[{name:"loading",rawName:"v-loading",value:t.broker.view.loading,expression:"broker.view.loading"}],attrs:{data:t.broker.tableData,border:"",stripe:"",size:"mini"}},[i("el-table-column",{attrs:{prop:"mBean",label:"监控属性",width:"450"}}),i("el-table-column",{attrs:{prop:"oneMinute",label:"1分钟",width:"100"}}),i("el-table-column",{attrs:{prop:"meanRate",label:"速率",width:"100"}}),i("el-table-column",{attrs:{prop:"fiveMinute",label:"5分钟",width:"100"}}),i("el-table-column",{attrs:{prop:"fifteenMinute",label:"15分钟"}})],1)],1),i("el-dialog",{attrs:{width:"90%",visible:t.topicData.view.visible,title:t.topicData.view.title},on:{"update:visible":function(e){return t.$set(t.topicData.view,"visible",e)}}},[i("el-row",[i("el-button",{staticClass:"margin-right text-forestgreen",attrs:{type:"plain",size:"small"},on:{click:t.createIndex}},[i("i",{staticClass:"fa fa-play"})]),i("el-select",{attrs:{filterable:"",clearable:"",placeholder:"序列化",size:"small"},model:{value:t.topicData.input.serizlizer,callback:function(e){t.$set(t.topicData.input,"serizlizer",e)},expression:"topicData.input.serizlizer"}},t._l(t.topicData.serializers,(function(t){return i("el-option",{key:t,attrs:{label:t,value:t}})})),1),i("el-select",{attrs:{filterable:"",clearable:"",placeholder:"类加载器",size:"small"},model:{value:t.topicData.input.classloader,callback:function(e){t.$set(t.topicData.input,"classloader",e)},expression:"topicData.input.classloader"}},t._l(t.topicData.classloaders,(function(t){return i("el-option",{key:t,attrs:{label:t,value:t}})})),1),t._v(" 加载数量(每分区) "),i("el-input-number",{attrs:{size:"small"},model:{value:t.topicData.input.loadCount,callback:function(e){t.$set(t.topicData.input,"loadCount",e)},expression:"topicData.input.loadCount"}})],1),i("el-row",{staticClass:"margin-top"},[i("el-input",{attrs:{placeholder:"输入关键词搜索",size:"small"},on:{chage:t.indexSearch},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.indexSearch(e)}},model:{value:t.topicData.input.keyword,callback:function(e){t.$set(t.topicData.input,"keyword",e)},expression:"topicData.input.keyword"}}),i("el-table",{directives:[{name:"loading",rawName:"v-loading",value:t.topicData.view.loading,expression:"topicData.view.loading"}],attrs:{data:t.handleDatas,border:"",stripe:"",height:"500",size:"mini"}},[i("el-table-column",{attrs:{type:"index",width:"50"}}),i("el-table-column",{attrs:{prop:"partition",label:"partition",width:"70"}}),i("el-table-column",{attrs:{prop:"offset",label:"offset",width:"100"}}),i("el-table-column",{attrs:{prop:"time",label:"time",width:"150"}}),i("el-table-column",{attrs:{prop:"data",label:"data"}}),i("el-table-column",{attrs:{fixed:"right",label:"操作",width:"100"},scopedSlots:t._u([{key:"default",fn:function(e){return[i("el-button",{attrs:{type:"text",size:"small"},on:{click:function(i){return t.showJsonData(e.row)}}},[t._v("展示为 JSON")])]}}])})],1)],1)],1),i("el-drawer",{attrs:{visible:t.topicData.drawer.visible,"with-header":!1,direction:"rtl","append-to-body":""},on:{"update:visible":function(e){return t.$set(t.topicData.drawer,"visible",e)}}},[i("json-editor",{attrs:{json:t.topicData.drawer.data}})],1)],1)},o=[],n=(i("d81d"),i("b0c0"),i("e9c4"),i("d3b7"),i("159b"),i("b1a9")),l=i("e4bc"),s=i("ed08"),r=i("f3cc"),c=i("0964"),p=i("7dd6"),u={name:"KafkaTopic",components:{ListGroup:r["a"],KafkaDataView:c["a"],JsonEditor:p["a"]},data:function(){return{input:{topicName:null,topic:{},json:{},partition:0,form:{topic:null,replication:1,partitions:1}},view:{activeTabName:"third",dialog:!1,dialogLoading:!1,onlyOneDataLoading:!1,createTopicDialog:!1,topicsLoading:!1},topics:[],connects:[],brokers:[],dialog:{datas:[],configs:{partition:null,max:0}},tab:{zero:{view:{loading:!1},lastUpdateTime:null,tableData:[]},second:{view:{loading:!1},lastUpdateTime:null,tableData:[]}},broker:{view:{visible:!1,loading:!1,title:"broker 监控"},tableData:[],lastUpdateTime:null},topicData:{input:{keyword:null,serizlizer:"string",classloader:null,loadCount:10},view:{visible:!1,loading:!1,title:"数据查询(索引方式)"},drawer:{visible:!1,data:null},tableData:[],serializers:[],classloaders:[]}}},computed:{topicNames:function(){return this.topics.map((function(t){return t.name}))},dialogTips:function(){return this.input.topicName+"_"+this.dialog.configs.partition+" 尾部数据"},handleDatas:function(){return this.topicData.tableData.map((function(t){return Object.assign({time:Object(s["f"])(t.timestamp)},t)}))}},watch:{"input.topicName":{handler:function(t,e){"second"!==this.view.activeTabName?this.currentTopicLogSize=[]:this.loadCurrentTopicLogSize()}}},mounted:function(){this.reloadAllConnects()},methods:{createIndex:function(){var t=this;this.topicData.view.loading=!0,l["a"].createIndex(this.input.connect,this.input.topicName,this.topicData.input.loadCount,this.topicData.input.serizlizer,this.topicData.input.classloader).then((function(e){t.topicData.view.loading=!1,t.indexSearch()}))},indexSearch:function(){var t=this;l["a"].indexSearch(this.topicData.input.keyword).then((function(e){t.topicData.tableData=e.data}))},reloadAllConnects:function(){var t=this;n["a"].security.moduleConnectNames("kafka").then((function(e){t.connects=e.data,t.connects&&t.connects.length>0&&(t.input.connect=t.connects[0],t.loadTopics(t.input.connect))}))},refreshBrokerMonitor:function(){var t=this;this.broker.view.visible=!0,this.broker.view.loading=!0,this.broker.view.title=this.input.connect+" 监控",l["a"].monitorBroker(this.input.connect).then((function(e){t.broker.view.loading=!1,t.broker.tableData=e.data,t.broker.lastUpdateTime=Object(s["f"])((new Date).getTime())}))},refreshTopicMonitor:function(){var t=this;this.tab.zero.view.loading=!0,l["a"].monitorTopic(this.input.connect,this.input.topicName).then((function(e){t.tab.zero.view.loading=!1,t.tab.zero.tableData=e.data,t.tab.zero.lastUpdateTime=Object(s["f"])((new Date).getTime())}))},createTopic:function(){var t=this;l["a"].createTopic(Object.assign({clusterName:this.input.connect},this.input.form)).then((function(e){t.$message("创建 "+t.input.form.topic+" 成功"),t.view.createTopicDialog=!1,t.loadTopics(t.input.connect)}))},deleteTopic:function(){var t=this;this.$confirm("确定删除组 "+this.input.topicName+" 此操作不可逆?","警告",{type:"warning"}).then((function(){l["a"].deleteTopic(t.input.connect,t.input.topicName).then((function(e){t.loadTopics(t.input.connect)}))})).catch((function(){}))},sendData:function(){var t=this,e={clusterName:this.input.connect,topic:this.input.topicName,data:JSON.stringify(this.input.json)};l["a"].topicSendJsonData(e).then((function(e){t.$message("发送成功")}))},lastOneData:function(){var t=this;this.input.json={},this.view.onlyOneDataLoading=!0,l["a"].topicDataOne(this.input.connect,this.input.topicName,"string",null).then((function(e){t.view.onlyOneDataLoading=!1,e.data&&(t.input.json=JSON.parse(e.data.data))})).catch((function(e){t.view.onlyOneDataLoading=!1}))},changeJson:function(t){this.input.json=t},reloadData:function(t,e,i){var a=this,o=this.dialog.configs;this.view.dialogLoading=!0,l["a"].topicDataLast(this.input.connect,this.input.topicName,o.partition,i,t,e).then((function(t){a.dialog.datas=t.data,t.data.forEach((function(t){return t.partition?t.partition:t.partition=a.dialog.configs.partition})),a.view.dialogLoading=!1}))},loadNextPartition:function(t,e,i){this.dialog.configs.partition++,this.dialog.configs.partition>this.dialog.configs.max&&(this.dialog.configs.partition=0),this.reloadData(t,e,i)},showDataDialog:function(t){this.dialog.configs.partition=t.partition;var e=this.input.topic.partitions?this.input.topic.partitions.length:0;0!==e&&(this.dialog.configs.max=e-1),this.view.dialog=!0,this.dialog.datas=[]},switchTab:function(t){var e=this;"1"===t.index?this.loadCurrentTopicLogSize():"second"===t.name&&(this.topicData.classloaders&&0!==this.topicData.classloaders.length||(n["a"].serializers().then((function(t){e.topicData.serializers=t.data})),n["a"].classloaders().then((function(t){e.topicData.classloaders=t.data}))))},showMinOffset:function(t){return t.minOffset+" ("+(t.logSize-t.minOffset)+")"},loadTopics:function(t){var e=this;this.view.topicsLoading=!0,l["a"].topics(t).then((function(t){if(e.view.topicsLoading=!1,e.topics=t.data,e.topics&&e.topics.length>0){var i=e.topics[0].name;e.choseTopic(i,0)}})),l["a"].brokers(t).then((function(t){e.brokers=t.data}))},choseTopic:function(t,e){this.input.topicName=t,this.input.topic=this.topics[e],this.refreshTopicMonitor()},loadCurrentTopicLogSize:function(){var t=this;this.tab.second.view.loading=!0,l["a"].logSize(this.input.connect,this.input.topicName).then((function(e){t.tab.second.lastUpdateTime=Object(s["f"])((new Date).getTime()),t.tab.second.tableData=e.data.map((function(t){return Object.assign({time:Object(s["f"])(t.timestamp)},t)})),t.tab.second.view.loading=!1})).catch((function(e){t.tab.second.view.loading=!1}))},showJsonData:function(t){this.topicData.drawer.visible=!0,this.topicData.drawer.data=JSON.parse(t.data)}}},d=u,b=i("2877"),m=Object(b["a"])(d,a,o,!1,null,"f5ba44ea",null);e["default"]=m.exports}}]);