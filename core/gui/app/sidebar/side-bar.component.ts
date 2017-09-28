import {Component, ViewChild, OnInit} from '@angular/core';
import { Response, Http } from '@angular/http';
import { CurrentDataService } from '../services/current-data-service';
import { TableMetadata } from "../services/table-metadata";
import {log} from "util";

declare var jQuery: any;
declare var Backbone: any;

declare var PrettyJSON: any;
declare let JSONEditor: any;

@Component({
    moduleId: module.id,
    selector: 'side-bar-container',
    templateUrl: './side-bar.component.html',
    styleUrls: ['../style.css']
})

export class SideBarComponent {
    data: any;
    attributes: string[] = [];

    OperatorSchemaData: any[] = [];
    JsonSchemaEditor: any;
    currentSchema: any;

    operatorId: number;
    operatorTitle: string;

    advancedPressed: boolean = false;

    hiddenList: string[] = ["operatorType", "luceneAnalyzer", "matchingType"];

    selectorList: string[] = ["dictionaryEntries", "password", "nlpEntityType",
        "splitType", "splitOption", "sampleType", "comparisonType",
        "aggregationType", "attributes", "tableName", "attribute", "keywordList",
        "languageList", "locationList","customerKey","customerSecret","token","tokenSecret"];

    matcherList: string[] = ["conjunction", "phrase", "substring"];
    nlpEntityList: string[] = ["noun", "verb", "adjective", "adverb", "ne_all",
        "number", "location", "person", "organization", "money", "percent", "date", "time"];
    regexSplitList: string[] = ["left", "right", "standalone"];
    nlpSplitList: string[] = ["oneToOne", "oneToMany"];
    samplerList: string[] = ["random", "firstk"];

    compareList: string[] = ["=", ">", ">=", "<", "<=", "≠"];
    aggregationList: string[] = ["min", "max", "count", "sum", "average"];

    twitterLanguageList: string[] = ["English","Arabic","Danish","Dutch","English UK","Farsi","Filipino","Finnish","French","German","Hebrew","Hindi","Hungarian","Indonesian","Italian","Japanese","Korean",
        "Malay","Norwegian","Polish","Portuguese","Russian","Simplified Chinese","Spanish","Swedish","Thai", "Traditional Chinese","Turkish","Urdu"];

    twitterLanguageShortenList: string[] = ["en","ar","da","nl","en-gb","fa","fil","fi","fr","de","he","hi","hu","id","it","ja","ko","msa","no","pl","pt","ru","zh-cn","es","sv","th","zh-tw","tr","ur"];


    twitterLanguageMapping: {[key:string]:string;} = {};

    attributeItems:Array<string> = [];
    tableNameItems:Array<string> = [];
    selectedAttributesList:Array<string> = [];
    selectedAttributeMulti:string = "";
    selectedAttributeSingle:string = "";
    metadataList:Array<TableMetadata> = [];

    dictionaryNames: Array<string> = [];
    dictionaryContent: Array<string> = [];
    selectedDictionary:string = "";
    twitterQuery: Array<string> = [];
    twitterLanguage: Array<string> = [];
    locationString:string = "";

    optionalTwitterList: Array<string> = ["customerKey","customerSecret","token","tokenSecret"];

    checkInHidden(name: string) {
      if (this.advancedPressed){
        var hideItem = ["operatorType"];
        return jQuery.inArray(name, hideItem);
      }
      return jQuery.inArray(name, this.hiddenList);
    }
    checkInSelector(name: string) {
      if (this.advancedPressed){
        var selectList = this.selectorList.concat(["operatorType"]);
        return jQuery.inArray(name, selectList);
      }
      var selectList = this.selectorList.concat(this.hiddenList);
      return jQuery.inArray(name, selectList);
    }

    checkInOptionalTwitter(name: string){
      return jQuery.inArray(name,this.optionalTwitterList);
    }

    checkIsDictionary(){
      return this.operatorTitle === "Dictionary Search" || this.operatorTitle === "Source: Dictionary";
    }

    fileChange(event) {
      let fileList: FileList = event.target.files;
      this.currentDataService.uploadDictionary(fileList[0]);
    }

    constructor(private currentDataService: CurrentDataService) {
        currentDataService.newAddition$.subscribe(
            data => {
                this.data = data.operatorData;
                this.operatorId = data.operatorNum;
                this.operatorTitle = data.operatorData.properties.title;
                this.attributes = [];
                for (var attribute in data.operatorData.properties.attributes) {
                    this.attributes.push(attribute);
                }
                // initialize the advanced button
                this.advancedPressed = false;
                // initialize selected attributes
                this.selectedAttributeMulti = "";
                this.selectedAttributeSingle = "";
                this.selectedDictionary = "";
                this.locationString = "";

                // initialize the twitter language options
                for (var i = 0; i < this.twitterLanguageList.length; ++i){
                  this.twitterLanguageMapping[this.twitterLanguageList[i]] = this.twitterLanguageShortenList[i];
                }

                if (data.operatorData.properties.attributes.attributes) {
                    this.selectedAttributesList = data.operatorData.properties.attributes.attributes;
                } else if (data.operatorData.properties.attributes.attribute) {
                    this.selectedAttributesList = [data.operatorData.properties.attributes.attribute]
                }
                if (data.operatorData.properties.attributes.tableName) {
                    this.getAttributesForTable(data.operatorData.properties.attributes.tableName);
                }
                if (data.operatorData.properties.attributes.dictionaryEntries) {
                    this.dictionaryContent = data.operatorData.properties.attributes.dictionaryEntries;
                }
                if (data.operatorData.properties.attributes.keywordList) {
                    this.twitterQuery = data.operatorData.properties.attributes.keywordList;
                }
                if (data.operatorData.properties.attributes.languageList) {
                    this.twitterLanguage = data.operatorData.properties.attributes.languageList;
                }

            });

        currentDataService.metadataRetrieved$.subscribe(
            data => {
                this.metadataList = data;
                let metadata: (Array<TableMetadata>) = data;
                metadata.forEach(x => {
                    this.tableNameItems.push((x.tableName));
                });
            }
        );

        currentDataService.dictionaryNames$.subscribe(
            data => {
                console.log("dict data is: ");
                console.log(data);
                this.dictionaryNames = data;
            }
        );

        currentDataService.dictionaryContent$.subscribe(
            data => {
                this.dictionaryContent = [];
                for(let entry of data){
                    this.dictionaryContent.push(entry.trim());
                }
                this.data.properties.attributes.dictionaryEntries = this.dictionaryContent;
                this.onFormChange("dictionary");
            }
        );

        currentDataService.getJsonSchema$.subscribe(
          data => {
            this.OperatorSchemaData = data;
          },
          error => {
            console.log(error);
          }
        );


        currentDataService.testing$.subscribe(
          data => {
            this.currentSchema = {};

            // clearData or there is existing
            if (data.operatorData.properties.title === "Operator"){
              this.JsonSchemaEditor.destroy();
              return;
            }
            if (this.JsonSchemaEditor){
              this.JsonSchemaEditor.destroy();
            }

            // get the schema of current selected operator
            for (let eachSchema of this.OperatorSchemaData){
              if (eachSchema.userFriendlyName === data.operatorData.properties.title) {
                this.currentSchema = eachSchema;
                break;
              }
            }
            console.log("Current = ");
            console.log(this.currentSchema);

            // generate the schema format
            var operator_attributes = {
              "type" : "object",
              "properties" : this.currentSchema.properties,
              "required" : this.currentSchema.required
            }

            // this hides the non-required fields, some fields are still not added
            // to the required field, so comment this out first
            // for (var eachProperty in this.currentSchema.properties){
            //   if (jQuery.inArray(eachProperty,this.currentSchema.required) === -1){
            //     this.currentSchema.properties[eachProperty]["options"] = {
            //       "hidden" : true
            //     };
            //   }
            // }

            //create the table according to the schema provided
            this.JsonSchemaEditor = new JSONEditor(document.getElementById("editor_holder"),{
              theme : "bootstrap3",
              schema : operator_attributes,
              disable_edit_json : true,
              disable_properties: true,
              disable_collapse : true,
            });

            // this is the value of the schema
            var schema_value = this.JsonSchemaEditor.getValue();
            console.log(schema_value);
          }
        );


    }

    humanize(name: string): string {
        // TODO: rewrite this function to handle camel case
        // e.g.: humanize camelCase -> camel case
        var frags = name.split('_');
        for (var i = 0; i < frags.length; i++) {
            frags[i] = frags[i].charAt(0).toUpperCase() + frags[i].slice(1);
        }
        return frags.join(' ');
    }

    attributeAdded (type: string) {
        if (type === "multi") {
            this.selectedAttributesList.push(this.selectedAttributeMulti);
            this.data.properties.attributes.attributes = this.selectedAttributesList;
            this.onFormChange("attributes");
        } else if (type === "single") {
            this.data.properties.attributes.attribute = this.selectedAttributeSingle;
            this.onFormChange("attribute");
        }
    }

    locationAdded(event:string){
        this.locationString = event;
        this.data.properties.attributes.locationList = this.locationString;
        this.onFormChange("locationList");
    }

    manuallyAdded (event:string) {
        if (event.length === 0) {
            // removed all attributes
            this.selectedAttributesList = [];
        } else {
            this.selectedAttributesList = event.split(",");
        }

        this.data.properties.attributes.attributes = this.selectedAttributesList;
        this.onFormChange("attributes");
    }

    getAttributesForTable (event:string) {
        if (!event) {
            return;
        }

        this.attributeItems = [];

        this.metadataList.forEach(x => {
            if (x.tableName === event) {
                x.attributes.forEach(
                    y => {
                        if (!y.attributeName.startsWith("_")) {
                            this.attributeItems.push(y.attributeName);
                        }
                    });
            }
        });

        this.onFormChange("tableName");
    }

    dictionarySelected() {
        this.currentDataService.getDictionaryContent(this.selectedDictionary);
    }

    dictionaryManuallyAdded(event: string) {
        if (event.length === 0) {
            this.dictionaryContent = [];
        } else {
            this.dictionaryContent = event.split(",");
        }
        this.data.properties.attributes.dictionaryEntries = this.dictionaryContent;
        this.onFormChange("dictionary");
    }

  onFormChange (attribute: string) {
    var currentData = jQuery("#the-flowchart").flowchart("getOperatorData", this.operatorId);
    // update the position of the operator if it is moved before the value is changed
    this.data.left = currentData.left;
    this.data.top = currentData.top;
    jQuery("#the-flowchart").flowchart("setOperatorData", this.operatorId, this.data);
  }

  onDelete() {
    this.operatorTitle = "Operator";
    this.attributes = [];
    this.dictionaryContent = [];
    jQuery("#the-flowchart").flowchart("deleteOperator", this.operatorId);
    this.currentDataService.clearData();
    this.currentDataService.setAllOperatorData(jQuery('#the-flowchart').flowchart('getData'));
  }

  onAdvanced() {
    this.advancedPressed = true;
  }

  hideAdvance() {
    this.advancedPressed = false;
  }



  twitterQueryManuallyAdded(event: string) {
      if (event.length === 0) {
          this.twitterQuery = [];
      } else {
          this.twitterQuery = event.split(",");
      }
      this.data.properties.attributes.keywordList = this.twitterQuery;
      this.onFormChange("keywordList");
  }

  twitterLanguageManuallyAdded(shortenFormLanguage: string){
    var languageCheckBox = jQuery("#" + shortenFormLanguage);
    if (languageCheckBox[0].checked){
      this.twitterLanguage.push(shortenFormLanguage);
    } else {
      var index = this.twitterLanguage.indexOf(shortenFormLanguage);
        // remove 1 element starting from that index
      this.twitterLanguage.splice(index, 1);
    }
    console.log(this.twitterLanguage);
    this.data.properties.attributes.languageList = this.twitterLanguage;
    this.onFormChange("languageList");
  }

  languageTextClicked(language: string){
    var shortenFormLanguage = this.twitterLanguageMapping[language];
    var languageCheckBox = jQuery("#" + shortenFormLanguage);
    // check if the checkbox for particular language is checked
    if (languageCheckBox[0].checked){
      // if is checked now, uncheck it
      languageCheckBox[0].checked = false;
    } else {
      // if not checked, check it
      languageCheckBox[0].checked = true;
    }
    this.twitterLanguageManuallyAdded(shortenFormLanguage);
  }

}
