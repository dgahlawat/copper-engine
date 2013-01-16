/*
 * Copyright 2002-2012 SCOOP Software GmbH
 *
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
 */
package de.scoopgmbh.copper.gui.ui.workflowinstance.filter;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.converter.LongStringConverter;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class WorkflowInstanceFilterController implements Initializable, FilterController<WorkflowInstanceFilterModel>, FxmlController {
	WorkflowInstanceFilterModel model = new WorkflowInstanceFilterModel();



    @FXML //  fx:id="majorVersion"
    private TextField majorVersion; // Value injected by FXMLLoader

    @FXML //  fx:id="minorVersion"
    private TextField minorVersion; // Value injected by FXMLLoader

    @FXML //  fx:id="priorityField"
    private TextField priorityField; // Value injected by FXMLLoader

    @FXML //  fx:id="stateChoice"
    private ChoiceBox<WorkflowInstanceState> stateChoice; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TextField workflowClass; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert majorVersion != null : "fx:id=\"majorVersion\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert minorVersion != null : "fx:id=\"minorVersion\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert priorityField != null : "fx:id=\"priorityField\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert stateChoice != null : "fx:id=\"stateChoice\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert workflowClass != null : "fx:id=\"workflowClass\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";

        priorityField.textProperty().bind(model.priority.asString());
        workflowClass.textProperty().bind(model.version.classname);
        majorVersion.textProperty().bindBidirectional(model.version.versionMajor, new LongStringConverter());
        minorVersion.textProperty().bindBidirectional(model.version.versionMinor, new LongStringConverter());
        
        stateChoice.setItems(FXCollections.observableList(Arrays.asList(WorkflowInstanceState.values())));
        stateChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<WorkflowInstanceState>() {
            public void changed(ObservableValue<? extends WorkflowInstanceState> observableValue, WorkflowInstanceState anEnum, WorkflowInstanceState anEnum1) {
            	model.state.setValue(anEnum1);
            }
        });

	}

	@Override
	public WorkflowInstanceFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceFilter.fxml");
	}
	
	
}