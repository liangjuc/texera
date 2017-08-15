import { Injectable } from '@angular/core';
import { Subject }    from 'rxjs/Subject';
import { Response, Http } from '@angular/http';
import { Headers } from '@angular/http';

import { Data } from './data';
import { TableMetadata } from "./table-metadata";
import any = jasmine.any;


declare var jQuery: any;
declare var XLSX: any;
declare var saveAs: any;


const apiUrl = "http://localhost:8080/api";
const textdbUrl = apiUrl + '/newqueryplan/execute';
const metadataUrl = apiUrl + '/resources/metadata';
const uploadDictionaryUrl = apiUrl + "/upload/dictionary";
const getDictionariesUrl = apiUrl + "/resources/dictionaries";
const getDictionaryContentUrl = apiUrl + "/resources/dictionary?name=";
const downloadExcelUrl = apiUrl + "/download/result?resultID=";


const defaultData = {
    top: 20,
    left: 20,
    properties: {
        title: 'Operator',
        inputs: {},
        outputs: {},
        attributes : {},
    }
}

@Injectable()
export class CurrentDataService {
    allOperatorData : Data;

    private newAddition = new Subject<any>();
    newAddition$ = this.newAddition.asObservable();

    private checkPressed = new Subject<any>();
    checkPressed$ = this.checkPressed.asObservable();

    private metadataRetrieved = new Subject<any>();
    metadataRetrieved$ = this.metadataRetrieved.asObservable();

    private dictionaryNames= new Subject<any>();
    dictionaryNames$ = this.dictionaryNames.asObservable();

    private dictionaryContent = new Subject<any>();
    dictionaryContent$ = this.dictionaryContent.asObservable();

    constructor(private http: Http) { }

    setAllOperatorData(operatorData : any): void {
        this.allOperatorData = {id: 1, jsonData: operatorData};
    }

    selectData(operatorNum : number): void {
      var data_now = jQuery("#the-flowchart").flowchart("getOperatorData",operatorNum);
      this.newAddition.next({operatorNum: operatorNum, operatorData: data_now});
      this.setAllOperatorData(jQuery("#the-flowchart").flowchart("getData"));
    }

    clearData() : void {
      this.newAddition.next({operatorNum : null, operatorData: defaultData});
    }

    processData(): void {

        let textdbJson = {operators: {}, links: {}};
        var operators = [];
        var links = [];

        var listAttributes : string[] = ["attributes", "dictionaryEntries"]

        for (var operatorIndex in this.allOperatorData.jsonData.operators) {
            var currentOperator = this.allOperatorData.jsonData['operators'];
            if (currentOperator.hasOwnProperty(operatorIndex)) {
                var attributes = {};
                attributes["operatorID"] = operatorIndex;
                for (var attribute in currentOperator[operatorIndex]['properties']['attributes']) {
                    if (currentOperator[operatorIndex]['properties']['attributes'].hasOwnProperty(attribute)) {
                        attributes[attribute] = currentOperator[operatorIndex]['properties']['attributes'][attribute];
                        // if attribute is an array property, and it's not an array
                        if (jQuery.inArray(attribute, listAttributes) != -1 && ! Array.isArray(attributes[attribute])) {
                          attributes[attribute] = attributes[attribute].split(",").map((item) => item.trim());
                        }
                        // if the value is a string and can be converted to a boolean value
                        if (attributes[attribute] instanceof String && Boolean(attributes[attribute])) {
                          attributes[attribute] = (attributes[attribute].toLowerCase() === 'true')
                        }
                    }
                }
                operators.push(attributes);
            }
        }
        for(var link in this.allOperatorData.jsonData.links){
            var destination = {};
            var currentLink = this.allOperatorData.jsonData['links'];
            if (currentLink[link].hasOwnProperty("fromOperator")){
                destination["origin"] = currentLink[link]['fromOperator'].toString();
                destination["destination"] = currentLink[link]['toOperator'].toString();
                links.push(destination);
            }
        }

        textdbJson.operators = operators;
        textdbJson.links = links;
        this.sendRequest(textdbJson);
    }

    private sendRequest(textdbJson: any): void {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        console.log("TextDB JSON is:");
        console.log(JSON.stringify(textdbJson));
        this.http.post(textdbUrl, JSON.stringify(textdbJson), {headers: headers})
            .subscribe(
                data => {
                    this.checkPressed.next(data.json());
                },
                err => {
                    this.checkPressed.next(err.json());
                }
            );
    }

    getMetadata(): void {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        this.http.get(metadataUrl, {headers: headers})
            .subscribe(
                data => {
                    let result = (JSON.parse(data.json().message));
                    let metadata: Array<TableMetadata> = [];
                    result.forEach((x, y) =>
                        metadata.push(new TableMetadata(x.tableName, x.schema.attributes))
                    );
                    this.metadataRetrieved.next(metadata);
                },
                err => {
                    console.log("Error at getMetadata() in current-data-service.ts \n Error: "+err);
                }
            );
    }

    uploadDictionary(file: File) {
        let formData:FormData = new FormData();
        formData.append('file', file, file.name);
        this.http.post(uploadDictionaryUrl, formData, null)
          .subscribe(
            data => {
              alert(file.name + ' is uploaded');
              // after adding a new dictionary, refresh the list
              this.getDictionaries();
            },
            err => {
                alert('Error occurred while uploading ' + file.name);
                console.log('Error occurred while uploading ' + file.name + '\nError message: ' + err);
            }
          );
    }

    getDictionaries(): void {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        this.http.get(getDictionariesUrl, {headers: headers})
            .subscribe(
                data => {
                    let result = JSON.parse(data.json().message);
                    this.dictionaryNames.next(result);
                },
                err => {
                    console.log("Error at getDictionaries() in current-data-service.ts \n Error: "+err);
                }
            );
    }

    getDictionaryContent(name: string): void {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        this.http.get(getDictionaryContentUrl+name, {headers: headers})
            .subscribe(
                data => {
                    let result = (data.json().message).split(",");

                    this.dictionaryContent.next(result);
                },
                err => {
                    console.log("Error at getDictionaries() in current-data-service.ts \n Error: "+err);
                }
            );
    }

    downloadExcel(result: string): void {


        // create a workbook from the HTML_table object

        // var worksheet = XLSX.utils.table_to_book(document.getElementById('result-table'));
        // console.log(worksheet)

        // create a workbook using the JSON Results

        var wb = { SheetNames:[], Sheets:{} };
        var ws = XLSX.utils.json_to_sheet(result);
        var ws_name = "Sheet1";
        wb.SheetNames.push(ws_name);
        wb.Sheets[ws_name] = ws;


        /* bookType can be any supported output type (xlsx in this case) */
        var wopts = { bookType:'xlsx', bookSST:false, type:'binary' };


        // convert to an excel file (in types specified above)

        var wbout = XLSX.write(wb,wopts);

        function s2ab(s) {
          var buf = new ArrayBuffer(s.length);
          var view = new Uint8Array(buf);
          for (var i=0; i!=s.length; ++i){
            view[i] = s.charCodeAt(i) & 0xFF;
          }
          return buf;
        }

        // browser generates binary blob and forces a "download" to client
        // generate a file called "TextDB_Result.xlsx"
        saveAs(new Blob([s2ab(wbout)],{type:"application/octet-stream"}), "TextDB_Result.xlsx");

    }

}
