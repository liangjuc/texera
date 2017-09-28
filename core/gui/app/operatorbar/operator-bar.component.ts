import { Component } from '@angular/core';

import { MockDataService } from '../services/mock-data-service';
import { CurrentDataService } from '../services/current-data-service';

declare var jQuery: any;

@Component({
  moduleId: module.id,
  selector: '[operator-bar]',
  templateUrl: './operator-bar.component.html',
  styleUrls: ['../style.css']
})
export class OperatorBarComponent {

  theDropDownNow : string;
  OperatorSchemaData : any[] = [];

  sourceOperators: any[] = [];


  constructor(private mockDataService: MockDataService, private currentDataService: CurrentDataService) {

    currentDataService.getJsonSchema$.subscribe(
      data => {
        this.OperatorSchemaData = data;
        console.log('In operatorbar now!!');
        console.log(this.OperatorSchemaData);

      },
      error => {
        console.log(error);
      }
    );

  }

  initialize() {
    var container = jQuery('#the-flowchart').parent();
    this.initializeOperators(container);
    this.initializeDrop(this.theDropDownNow);
    this.initializeHoverDetail();
  }

  initializeDrop (currentDrop : string) {
    jQuery('html').mouseup(function(e){
      jQuery('.dropdown-content').css({
        "display" : "none",
      });
      var checkOnClickIsDropDown = jQuery('.dropdown');
      var checkOnIcon = jQuery('.group-icon');
      if (!checkOnClickIsDropDown.is(e.target) && !checkOnIcon.is(e.target)){
        currentDrop = "";
      } else {
        if (checkOnIcon.is(e.target)){
          var currentDropType = jQuery(e.target).parent().data('dropdown-type');
        } else {
          var currentDropType = jQuery(e.target).data('dropdown-type');
        }
        if (currentDropType !== currentDrop){
          var dropdownID = "#" + currentDropType;
          jQuery(dropdownID).css({
            "display" : "block",
          });
          currentDrop = currentDropType;
        } else {
          currentDrop = "";
        }
      }
    });
  }

  initializeHoverDetail(){

    var findSchemaDescription = function(operatorName: string, schemaList: any[]): any {
      for (let schema of schemaList){
        if (schema.userFriendlyName === operatorName){
          return schema.operatorDescription;
        }
      }
      return "Default Content";
    }

    var current = this;
    var currentDetailProcess;

    jQuery('.draggable_operator').hover(
      function(e){

        var openDetail = function (left, top, width, height, margin, name){
          jQuery('#operator-detail-div').css({
            "display" : "block",
            "left" : left + width,
            "top" : top - height - margin * 2,
          });
          jQuery('#operator-detail-name').html(name);
          var newDefinition = findSchemaDescription(name,current.OperatorSchemaData);
          jQuery('#operator-detail-content').html(newDefinition);
        }
        var targetOperator = jQuery(e.target);
        var operatorName = targetOperator.html();
        var offset = targetOperator.offset();
        var targetWidth = parseInt(targetOperator.css('width'),10);
        var targetHeight = parseInt(targetOperator.css('height'),10);
        var margin = parseInt(targetOperator.css('margin-top'),10);
        currentDetailProcess = setTimeout(openDetail , 700 , offset.left, offset.top, targetWidth, targetHeight, margin, operatorName);
      },
      function(e){
        clearTimeout(currentDetailProcess);
        jQuery('#operator-detail-div').css({
          "display" : "none"
        });
      }
    );
    jQuery('#operator-detail-div').hover(
      function(){
        jQuery('#operator-detail-div').css({
          "display" : "block",
        });
      },
      function () {
        jQuery('#operator-detail-div').css({
          "display" : "none"
        });
      }
    );
  }

  createOperator(schema: any) {
    var inputs = {};
    var outputs = {};
    var attributes = {};
    for (var i = 0; i < schema.inputNumber; ++i ) {
      inputs["input_" + (i+1)] = {
        "label" : "",
      }
    }
    for (var i = 0; i < schema.outputNumber; ++i ) {
      outputs["output_" + (i+1)] = {
        "label" : "",
      }
    }

    for (let eachProperty in schema.properties){
      if (schema.properties[eachProperty]["default"] != undefined){
        attributes[eachProperty] = schema.properties[eachProperty]["default"];
      } else {
        attributes[eachProperty] = "";
      }
    }

    attributes["operatorType"] = schema.operatorType;

    var operator = {
      "top" : 20,
      "left" : 20,
      "properties" : {
        "title" : schema.userFriendlyName,
        "inputs" : inputs,
        "outputs" : outputs,
        "descriptions" : schema.operatorDescription,
        "attributes" : attributes,
        // dummy data here
        // ||
        // v
        "image" : '../thirdparty/images/sql.jpg',
        "color" : '#ff8080'
      }
    }
    return operator;
  }

  findSchema(operatorName: string, schemaList: any[]){
    for (let schema of schemaList){
      if (schema.userFriendlyName === operatorName){
        return this.createOperator(schema);
      }
    }
    return null;
  }

  initializeOperators(container: any) {

    var current = this;


    var draggableOperators = jQuery('.draggable_operator');
    draggableOperators.draggable({
      cursor: "move",
      opacity: 0.7,

      appendTo: 'body',
      zIndex: 1000,

      helper: function(e) {
        var dragged = jQuery(this);
        var draggedName = dragged.html()
        var operatorData = current.findSchema(draggedName,current.OperatorSchemaData);
        return jQuery('#the-flowchart').flowchart('getOperatorElement', operatorData);
      },

      stop: function(e, ui) {
        var dragged = jQuery(this);
        var draggedName = dragged.html()
        var operatorData = current.findSchema(draggedName,current.OperatorSchemaData);



        var newData = {
          top: 0,
          left: 20,
          properties: operatorData.properties
        }

        var elOffset = ui.offset;
        var containerOffset = container.offset();

        var positionRatio = jQuery('#the-flowchart').flowchart('getPositionRatio');

        console.log("CONTAINoffset left " + containerOffset.left);
        console.log("CONTAINoffset top " + containerOffset.top);
        console.log("elOffset left = " + elOffset.left * positionRatio);
        console.log("elOffset top = " + elOffset.top * positionRatio);

        if (elOffset.left  > containerOffset.left &&
          elOffset.top > containerOffset.top &&
          elOffset.left  < containerOffset.left + container.width() &&
          elOffset.top < containerOffset.top + container.height()) {

          var flowchartOffset = jQuery('#the-flowchart').offset();

          var relativeLeft = elOffset.left - flowchartOffset.left;
          var relativeTop = elOffset.top - flowchartOffset.top;

          relativeLeft /= positionRatio;
          relativeTop /= positionRatio;

          newData.left = relativeLeft;
          newData.top = relativeTop;

          var operatorNum = jQuery('#the-flowchart').flowchart('addOperator', newData);

          jQuery('#the-flowchart').flowchart('selectOperator', operatorNum); // select the created operator
        }
      }
    });
  }
}
