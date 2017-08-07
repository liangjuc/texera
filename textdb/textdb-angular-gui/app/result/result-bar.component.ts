import { Component,ViewChild, OnInit } from '@angular/core';
import { CurrentDataService } from '../services/current-data-service';
import { ModalComponent } from 'ng2-bs3-modal/ng2-bs3-modal';

declare var jQuery: any;
declare var Backbone: any;
declare var PrettyJSON: any;

@Component({
    moduleId: module.id,
    selector: 'result-container',
    templateUrl: './result-bar.component.html',
    styleUrls: ['../style.css'],
})
export class ResultBarComponent {
  result: any;
  attribute: string[] = [];
  resultBarStatus: string = "closed";
  previousResultHandleTop: number = -10;


  @ViewChild('ResultModal')
  modal: ModalComponent;

  ModalOpen() {
    this.modal.open();
  }
  ModalClose() {
    this.modal.close();
  }

  constructor (private currentDataService: CurrentDataService){
    currentDataService.checkPressed$.subscribe(
      data => {
        // stop the loading animation of the run button
        jQuery('.navigation-btn').button('reset');
        this.attribute = [];
        // check if the result is valid
        if (data.code === 0) {
          this.result = JSON.parse(data.message);
          console.log(this.result);
          for (var each in this.result[0]){
            this.attribute.push(each);
          }
          // open the result bar automatically
          this.openResultBar();
          var newThing = jQuery('#the-flowchart').flowchart('getData');
          for (var each in newThing.operators){
            jQuery("#the-flowchart").flowchart("getHenryData",each);
          }
        } else {
          // pop the modal when not valid
            var node = new PrettyJSON.view.Node({
              el: jQuery("#ResultElem"),
              data: {"message": data.message}
            });
            this.ModalOpen();
        }
      }
    );
  }

  resultBarClicked(){
    // check if the result bar is opened or closed
    var currentResultBarStatus = jQuery('#result-table-bar').css('display');
    if (currentResultBarStatus === "none"){
      this.openResultBar()
    } else {
      this.closeResultBar()
    }
  }

  openResultBar(){
    jQuery("#result-table-bar").css({
      "display":"block",
      "height" : "300px",
    });
    jQuery('#ngrip').css({"top":"-10px"});
    jQuery("#flow-chart-container").css({"height":"calc(100% - 340px);"});
    this.redrawDraggable();
  }

  closeResultBar(){
    jQuery("#result-table-bar").css({
      "display":"none",
      "height" : "0px",
    });
    jQuery('#ngrip').css({"top":"-10px"});
    jQuery("#flow-chart-container").css({"height":"calc(100% - 40px);"});
    this.redrawDraggable();
  }

  redrawDraggable(){
    this.previousResultHandleTop = -parseInt(jQuery('#result-table-bar').css('height'), 10) - 10;
    jQuery("#ngrip").draggable( "destroy" );
    this.initializeResizing(this.previousResultHandleTop);
  }

  // initialized the default draggable / resizable result bar
  initializing(){
    this.initializeResizing(this.previousResultHandleTop);
  }

  initializeResizing(previousHeight: number){
    jQuery("#ngrip").draggable({
      axis:"y",
      containment: "window",
      drag: function( event, ui ) {
        // calculate the position
        var endPosition = previousHeight + 10 + ui.position.top;
        // if endPosition exceeds the maximum
        if (endPosition < -310){
          ui.position.top = -310;
          jQuery("#result-table-bar").css({
            "display":"block",
            "height" : "300px",
          });
          jQuery("#flow-chart-container").css({"height":"calc(100% - 340px);"});
        } else if (endPosition > -10){
          // if endPosition is lower than the minimum
          ui.position.top = -10;
          jQuery("#result-table-bar").css({
            "display":"none",
            "height" : "0px",
          });
          jQuery("#flow-chart-container").css({"height":"calc(100% - 40px);"});
        } else {
          var new_height = -endPosition - 10; // include the drag button
          var new_height2 = new_height + 40; // include the drag button and the title bar

          // redraw 2 fields to resize
          jQuery("#flow-chart-container").css({"height":"calc(100% - " + new_height2 + "px);"});
          jQuery("#result-table-bar").css({
            "display":"block",
            "height" : new_height + "px",
          });
        }
      },
      start: function( event, ui ) {
        // hide the drag button when begin dragging (fix some error)
        jQuery("#ngrip").css({
          "display" : "none"
        });

      },
      stop: function( event, ui ) {
        // get the current result bar height
        var newResultBarHeight = parseInt(jQuery('#result-table-bar').css('height'), 10);
        // if at minimum
        if (newResultBarHeight === 0){
          previousHeight = -10;
        } else if (newResultBarHeight === 300){
          previousHeight = -310
        } else {
          // previous height is used for calculating the movement of the result bar and flowchart
          previousHeight = previousHeight + 10 + parseInt(jQuery('#ngrip').css('top'), 10);
        }

        // make sure drag button is directly above the result bar
        jQuery('#ngrip').css({"top":"-10px"});

        // make the drag button visible again
        jQuery("#ngrip").css({
          "display" : "block"
        });
      }
    });

  }

}
