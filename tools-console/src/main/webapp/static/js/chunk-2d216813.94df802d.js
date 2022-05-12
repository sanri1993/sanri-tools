(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-2d216813"],{c36c:function(t,o,n){"use strict";n.r(o);var i=function(){var t=this,o=t.$createElement,n=t._self._c||o;return n("div",{staticClass:"app-container"},[n("el-row",[n("el-col",[n("el-button",{staticClass:"margin-right",attrs:{type:"text",icon:"el-icon-refresh"},on:{click:t.reloadAllConnects}}),n("el-select",{attrs:{size:"small"},on:{change:t.loadGroups},model:{value:t.input.connect,callback:function(o){t.$set(t.input,"connect",o)},expression:"input.connect"}},t._l(t.connects,(function(t){return n("el-option",{key:t,attrs:{value:t,label:t}})})),1)],1)],1),n("el-row",{staticClass:"margin-top margin-bottom"},[n("p",[t._v("brokers("+t._s(t.brokers.length)+"): "+t._s(t.brokers.join(",")))])]),n("el-row",{},[n("el-col",{attrs:{span:6}},[n("list-group",{directives:[{name:"loading",rawName:"v-loading",value:t.view.groupsLoading,expression:"view.groupsLoading"}],attrs:{list:t.groups},on:{"click-item":t.choseGroup}})],1),n("el-col",{staticClass:"margin-left",attrs:{span:17}},[n("p",[t._v("当前选中分组: "+t._s(t.input.group)+" , 消费主题数: "+t._s(t.currentGroupInfo.topics.length)+" ")]),n("p",[t._v("组协调器: "+t._s(t.currentGroupInfo.subscribes.coordinator)+" ")]),n("p",[t._v("分区策略: "+t._s(t.currentGroupInfo.subscribes.partitionAssignor)+" ")]),n("p",[n("el-button-group",[n("el-button",{attrs:{type:"danger",size:"small",icon:"el-icon-delete"},on:{click:t.deleteGroup}},[t._v("删除 "+t._s(t.input.group))]),n("el-button",{attrs:{size:"small",icon:"el-icon-search"}},[t._v(t._s(t.input.group)+" 消费情况预览")])],1)],1),n("el-row",[n("el-col",{attrs:{span:10}},[n("list-group",{attrs:{list:t.currentGroupInfo.topics},on:{"click-item":t.showGroupTopicSubscribe}})],1),n("el-col",{staticClass:"margin-left",attrs:{span:12}},[n("p",[t._v("当前选中主题: "+t._s(t.currentGroupInfo.currentTopicInfo.topic)+", 分区数: "+t._s(t.subscribes.length))]),n("p",[t._v("logSize: "+t._s(t.currentGroupInfo.currentTopicInfo.logSize)+" offset: "+t._s(t.currentGroupInfo.currentTopicInfo.offset)+" lag: "+t._s(t.currentGroupInfo.currentTopicInfo.lag))]),n("el-table",{directives:[{name:"loading",rawName:"v-loading",value:t.view.loading,expression:"view.loading"}],attrs:{data:t.subscribes,border:"",stripe:"",size:"mini"}},[n("el-table-column",{attrs:{prop:"partition",label:"分区",width:"45"}}),n("el-table-column",{attrs:{prop:"host",label:"消费主机",width:"105"}}),n("el-table-column",{attrs:{label:"minOffset"},scopedSlots:t._u([{key:"default",fn:function(o){return[n("span",[t._v(t._s(t.showMinOffset(o.row)))])]}}])}),n("el-table-column",{attrs:{prop:"logSize",label:"logSize"}}),n("el-table-column",{attrs:{prop:"offset",label:"offset"}}),n("el-table-column",{attrs:{prop:"lag",label:"lag"}}),n("el-table-column",{attrs:{fixed:"right",label:"操作",width:"140"},scopedSlots:t._u([{key:"default",fn:function(o){return[n("el-button",{attrs:{type:"text",disabled:o.row.offset===o.row.logSize,size:"small"},on:{click:function(n){return t.showDataDialog(o.row,"topicDataNearby","附近数据")}}},[t._v("附近数据")]),n("el-button",{attrs:{type:"text",disabled:o.row.minOffset===o.row.logSize,size:"small"},on:{click:function(n){return t.showDataDialog(o.row,"topicDataLast","尾部数据")}}},[t._v("尾部数据")])]}}])})],1)],1)],1)],1)],1),n("el-dialog",{attrs:{visible:t.view.dialog,title:t.dialogTips,width:"90%"},on:{"update:visible":function(o){return t.$set(t.view,"dialog",o)}}},[t._v(" 选择分区 "),n("el-input-number",{attrs:{min:0,max:10,label:"选择分区",size:"small"},model:{value:t.dialog.configs.partition,callback:function(o){t.$set(t.dialog.configs,"partition",o)},expression:"dialog.configs.partition"}}),n("kafka-data-view",{attrs:{datas:t.dialog.datas,loading:t.view.dialogLoading},on:{"update-data-show":t.reloadData,"next-partition":t.loadNextPartition}})],1)],1)},e=[],r=(n("d3b7"),n("159b"),n("4e82"),n("e4bc")),s=n("b1a9"),a=n("f3cc"),c=n("0964"),l={name:"KafkaGroup",components:{ListGroup:a["a"],KafkaDataView:c["a"]},data:function(){return{input:{connect:null,group:null},view:{loading:!1,dialog:!1,dialogLoading:!1,groupsLoading:!1},currentGroupInfo:{topics:[],subscribes:{},currentTopicInfo:{topic:null,logSize:0,offset:0,lag:0}},connects:[],groups:[],subscribes:[],brokers:[],dialog:{input:{},datas:[],configs:{partition:null,method:null,chinese:null,max:0}}}},computed:{dialogTips:function(){return this.currentGroupInfo.currentTopicInfo.topic+"_"+this.dialog.configs.partition+"_"+this.dialog.configs.chinese+" 的数据"}},mounted:function(){this.reloadAllConnects()},methods:{showDataDialog:function(t,o,n){this.dialog.configs.partition=t.partition,this.dialog.configs.method=o,this.dialog.configs.chinese=n,this.dialog.configs.max=this.subscribes.length-1,this.view.dialog=!0,this.dialog.datas=[]},loadNextPartition:function(t,o,n){this.dialog.configs.partition++,this.dialog.configs.partition>this.dialog.configs.max&&(this.dialog.configs.partition=0),this.reloadData(t,o,n)},reloadData:function(t,o,n){var i=this,e=this.dialog.configs;this.view.dialogLoading=!0,r["a"][e.method](this.input.connect,this.currentGroupInfo.currentTopicInfo.topic,e.partition,n,t,o).then((function(t){i.dialog.datas=t.data,t.data.forEach((function(t){return t.partition?t.partition:t.partition=i.dialog.configs.partition})),i.view.dialogLoading=!1}))},deleteGroup:function(){var t=this;this.$confirm("确定删除组 "+this.input.group+" 此操作不可逆?","警告",{type:"warning"}).then((function(){r["a"].deleteGroup(t.input.connect,t.input.group).then((function(o){t.loadGroups(t.input.connect)}))})).catch((function(){}))},loadGroups:function(t){var o=this;this.view.groupsLoading=!0,r["a"].groups(t).then((function(t){o.view.groupsLoading=!1,o.groups=t.data,o.groups&&o.groups.length>0&&o.choseGroup(o.groups[0])})),r["a"].brokers(t).then((function(t){o.brokers=t.data}))},choseGroup:function(t){var o=this;this.input.group=t,this.subscribes=[],r["a"].subscribeTopics(this.input.connect,t).then((function(n){o.currentGroupInfo.topics=n.data,o.currentGroupInfo.topics&&o.currentGroupInfo.topics.length>0&&r["a"].subscribes(o.input.connect,t).then((function(t){o.currentGroupInfo.subscribes=t.data,o.showGroupTopicSubscribe(o.currentGroupInfo.topics[0])}))}))},showMinOffset:function(t){return t.minOffset+" ("+(t.logSize-t.minOffset)+")"},showGroupTopicSubscribe:function(t){var o=this;this.view.loading=!0,this.currentGroupInfo.currentTopicInfo.topic=t;for(var n=[],i=this.currentGroupInfo.subscribes.memberInfos,e=0;e<i.length;e++)for(var s=i[e].topicPartitions,a=0;a<s.length;a++)s[a].topic===t&&(s[a].host=i[e].host,n.push(s[a]));n.sort((function(t,o){return t.partition-o.partition})),this.currentGroupInfo.currentTopicInfo.logSize=0,this.currentGroupInfo.currentTopicInfo.offset=0,this.currentGroupInfo.currentTopicInfo.lag=0,r["a"].subscribeTopicOffset(this.input.connect,this.input.group,t).then((function(t){var i=t.data;i.sort((function(t,o){return t.partition-o.partition}));for(var e=0;e<n.length;e++){var r=n[e],s=i[e];s&&(Object.assign(r,s),o.currentGroupInfo.currentTopicInfo.logSize+=s.logSize,o.currentGroupInfo.currentTopicInfo.offset+=s.offset,o.currentGroupInfo.currentTopicInfo.lag+=s.lag)}o.subscribes=n,o.view.loading=!1}))},reloadAllConnects:function(){var t=this;s["a"].security.moduleConnectNames("kafka").then((function(o){t.connects=o.data,t.connects&&t.connects.length>0&&(t.input.connect=t.connects[0],t.loadGroups(t.input.connect))}))}}},u=l,p=n("2877"),f=Object(p["a"])(u,i,e,!1,null,null,null);o["default"]=f.exports}}]);