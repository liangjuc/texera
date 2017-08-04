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
        // jQuery.hideLoading();
        jQuery('.navigation-btn').button('reset');
        this.attribute = [];
        // check if the result is valid
        if (data.code === 0) {
          this.result = JSON.parse(data.message);
          console.log(this.result);
          for (var each in this.result[0]){
            this.attribute.push(each);
          }
          this.openResultBar();
          var newThing = jQuery('#the-flowchart').flowchart('getData');
          for (var each in newThing.operators){
            jQuery("#the-flowchart").flowchart("getHenryData",each);
          }
        } else {
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
    if (this.resultBarStatus === "closed"){
      this.openResultBar()
    } else {
      this.closeResultBar()
    }
  }

  openResultBar(){
    jQuery("#result-table-bar").css({
      "display":"block",
    });
    jQuery("#flow-chart-container").css({"height":"calc(100% - 340px);"});
    this.resultBarStatus = "opened";
  }

  closeResultBar(){
    jQuery("#result-table-bar").css({"display":"none"});
    jQuery("#flow-chart-container").css({"height":"calc(100% - 40px);"});
    this.resultBarStatus = "closed";
  }

}
