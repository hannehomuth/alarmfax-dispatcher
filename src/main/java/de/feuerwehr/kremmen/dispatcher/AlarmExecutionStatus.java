/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.feuerwehr.kremmen.dispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse beschreibt den Ausführungsstatus eines Alarms
 *
 * @author jhomuth
 */
public class AlarmExecutionStatus {

    /**
     * Flag ob dieser Alarm ausgeführt werden darf oder nicht
     */
    private Boolean isAllowedToBeFired;

    /**
     * Validierungsfehler sofern dieser Alarm nicht ausgeführt werden darf.
     */
    private List<AlarmValidationFailure> validationErrors = new ArrayList<>();

    /**
     * Get the value of validationErrors
     *
     * @return the value of validationErrors
     */
    public List<AlarmValidationFailure> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Set the value of validationErrors
     *
     * @param validationError new value of validationErrors
     */
    public void addValidationError(AlarmValidationFailure validationError) {
        this.validationErrors.add(validationError);
    }

    /**
     * Get the value of isAllowedToBeFired
     *
     * @return the value of isAllowedToBeFired
     */
    public Boolean getIsAllowedToBeFired() {
        return isAllowedToBeFired;
    }

    /**
     * Set the value of isAllowedToBeFired
     *
     * @param isAllowedToBeFired new value of isAllowedToBeFired
     */
    public void setIsAllowedToBeFired(Boolean isAllowedToBeFired) {
        this.isAllowedToBeFired = isAllowedToBeFired;
    }

}
