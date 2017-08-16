/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */


var dgconverter = new Object();

dgconverter.getNodeToXml = function (inputNodeSet) {
    var exportableNodeSet = JSON.parse(inputNodeSet);
    //uses inputNodeSet if passed otherwise build the latest nodeSet

    //$("#btn-deploy").removeClass("disabled");

    function getDgStartNode(nodeList) {
        for (var i = 0; i < nodeList.length; i++) {
            if (nodeList[i].type == 'dgstart' && nodeList[i].wires != null && nodeList[i].wires != undefined) {
                return nodeList[i];
            }
        }
        return null;
    }


    function getNode(id) {
        for (var i = 0; i < exportableNodeSet.length; i++) {
            if (exportableNodeSet[i].id == id) {
                return exportableNodeSet[i];
            }
        }
        return null;
    }

    function getStartTag(node) {
        var startTag = "";
        var xmlStr = "";
        if (node != null && node.type != 'dgstart') {
            xmlStr = node.xml;
            var regex = /(<)([\w-]+)(.*)?/;
            var match = regex.exec(xmlStr);
            if (match != null) {
                if (match[1] != undefined && match[2] != undefined) {
                    startTag = match[2];
                }
            } else {
                console.log("startTag not found.");
            }
        }
        return startTag;
    }

    // if (inputNodeSet == null || inputNodeSet == undefined) {
    //     exportableNodeSet = getCurrentFlowNodeSet();
    // } else {
    //     exportableNodeSet = JSON.parse(inputNodeSet);
    // }
    var dgstartNode = getDgStartNode(exportableNodeSet);

    var level = 0;
    var fullXmlStr = "";

    printXml(dgstartNode);


    function printXml(node) {
        var xmlStr = "";
        var startTag = "";
        if (node != null && node.type != 'dgstart') {
            var comments = node.comments;
            if (comments != null && comments != "") {
                //if xml comments field already has the <!-- and --> remove them
                comments = comments.replace("<!--", "");
                comments = comments.replace("-->", "");
                xmlStr = "<!--" + comments + "-->";
            }
            xmlStr += node.xml;
            startTag = getStartTag(node);
          //special handling for break node
			if(xmlStr != undefined && xmlStr != null && xmlStr.trim() == "<break>"){
				fullXmlStr += "<break/>";
			}else{	
				fullXmlStr +=xmlStr;
			}
            /*
            if(level > 0){
                var spacing = Array(level).join("  ");
                xmlStr=xmlStr.replace(/\n/g,spacing);
                fullXmlStr +=xmlStr;

                console.log(xmlStr);
            }else{
                fullXmlStr +=xmlStr;
                console.log(xmlStr);
            }
            */
        }

        //console.log("startTag:" + startTag);

        var wiredNodes = [];
        var wiredNodesArr = [];
        if (node != null && node.wires != null && node.wires[0] != null && node.wires[0] != undefined && node.wires[0].length > 0) {
            wiredNodes = node.wires[0];
            //console.log("Before sort");
            for (var k = 0; wiredNodes != undefined && wiredNodes != null && k < wiredNodes.length; k++) {
                wiredNodesArr.push(getNode(wiredNodes[k]));
            }
            //console.dir(wiredNodesArr);
            //sort based on y position
            wiredNodesArr.sort(function (a, b) {
                return a.y - b.y;
            });
            //console.log("After sort");
            //console.dir(wiredNodesArr);
        }

        for (var k = 0; wiredNodesArr != null && k < wiredNodesArr.length; k++) {
            level++;
            var nd = wiredNodesArr[k];
            printXml(nd);
        }

        //append end tag
        if (startTag != "") {
        	if(startTag != "break"){
				fullXmlStr += "</" + startTag + ">";
			}
            /*
            if(level >0){
                var spacing = Array(level).join("  ");
                fullXmlStr += spacing + "</" + startTag + ">";
                console.log(spacing + "</" + startTag + ">");
            }else{
                fullXmlStr += "</" + startTag + ">";
                console.log("</" + startTag + ">");
            }
            */
        }

        /*if(level>0){
            level=level-1;
        }
        */
        //console.log("endTag:" + startTag);
        //console.log("xml:" + fullXmlStr);
    }
    //console.log("fullXmlStr:" + fullXmlStr);
    return fullXmlStr;
};

