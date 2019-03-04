/*
 * Copyright 2018 Your Organisation.
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
package org.vns.javafx.scene.control.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

/**
 *
 * @author Valery
 */
public class StringTextField extends TextField {

    protected static final String NULL = "UUID:2cff9fb4-d4f4-4d19-9f5f-8bfa53f2ad02";

    private final StringProperty defaultValue = new SimpleStringProperty("");

    private static final PseudoClass ERROR_FOUND_PSEUDO_CLASS = PseudoClass.getPseudoClass("errorfound");

    private StringProperty lastValidText = new SimpleStringProperty();
    private boolean created;
    private String lastText;

    private boolean nullable;

    protected List<Integer> errorItemIndexes = new ArrayList<>();

    private final ObservableMap<Integer, String> errorItems = FXCollections.observableHashMap();
    private final ObservableList<Predicate<String>> validators = FXCollections.observableArrayList();
    private final ObservableList<Predicate<String>> filterValidators = FXCollections.observableArrayList();
    private ObservableList<Predicate<String>> substitutionFilterValidators = FXCollections.observableArrayList();

    private ObservableList<StringTransformer> stringTransformers = FXCollections.observableArrayList();
    private StringTransformer emptyStringTransformer = null;

    private final StringProperty nullSubstitution = new SimpleStringProperty(null);
    private StringProperty emptySubstitution = new SimpleStringProperty();

    private String singleEmptyItemSubstitution;

    private String separator = null;

    private ErrorMarkerBuilder errorMarkerBuilder;

    private final ChangeListener<? super String> lastValidValueChangeListener;

    private final BooleanProperty errorFound = new BooleanPropertyBase(false) {

        @Override
        protected void invalidated() {
            pseudoClassStateChanged(ERROR_FOUND_PSEUDO_CLASS, get());
        }

        @Override
        public Object getBean() {
            return StringTextField.this;
        }

        @Override
        public String getName() {
            return "errorFound";
        }
    };

    public StringTextField() {
        this("");
    }

    public StringTextField(String defaultValue) {
        setDefaultValue(defaultValue);
        this.lastValidValueChangeListener = (v, ov, nv) -> {
            invalidateFormatterValue();
        };
    
        init();
    }

    private void init() {
        created = false;
        lastText = "";
        setEmptyStringTransformer(it -> {
            return getDefaultValue();
        });
        getStyleClass().add("string-textfield");
        setErrorMarkerBuilder(new ErrorMarkerBuilder(this));

        setTextFormatter(createTextFormatter());

        setOnMouseClicked(this::mouseClicked);
        lastValidTextProperty().addListener(lastValidValueChangeListener);
        emptySubstitution.addListener((v, ov, nv) -> {
            if (ov == null && nv != null) {
                if (getText().isEmpty()) {
                    setText(nv);
                    if (getLastText().isEmpty()) {
                        setLastText(nv);
                    }
                    selectAll();
                }
            }
        });
    }

    protected boolean isCreated() {
        return created;
    }

    protected void setCreated(boolean created) {
        this.created = created;
    }

    protected StringProperty defaultValueProperty() {
        return defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.set(defaultValue);
    }

    protected TextFormatter createTextFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            setCreated(true);
            String txt = getText();
            if (txt == null && isNullable() && getNullSubstitution() == null) {
                return change;
            }
            if (isNull(txt) && !isNullable()) {
                if (lastText.isEmpty() && (getEmptySubstitution() == null || getEmptySubstitution().isEmpty())) {
                    change.setText(NULL);
                    return change;
                } else if (lastText.isEmpty() && (getEmptySubstitution() != null && !getEmptySubstitution().isEmpty())) {
                    change.setText(getEmptySubstitution());
                    return change;
                } else {
                    change.setText(lastText);
                    return change;
                }
            }

            if (txt == null && getNullSubstitution() != null) {
                change.setText(getNullSubstitution());
                return change;
            } else if (txt == null && getEmptySubstitution() != null) {
                change.setText(getEmptySubstitution());
                return change;
            } else if (txt == null) {
                change.setText(NULL);
                return change;
            }
            if (change.getControlNewText().isEmpty() && getEmptySubstitution() != null) {
                change.setText(getEmptySubstitution());
                return change;
            }
            if (!change.getControlNewText().isEmpty() && change.getControlNewText().equals(getEmptySubstitution())) {
                return change;
            }
            if (!change.getControlNewText().isEmpty() && isNullable() && change.getControlNewText().equals(getNullSubstitution())) {
                return change;
            }
            if (!change.getControlNewText().isEmpty() && change.getControlNewText().equals(getSingleEmptyItemSubstitution())) {
                return change;
            }
            
            if (isAcceptable(change.getControlNewText())) {
                return change;
            } else {
                return null;
            }

        };
        return new TextFormatter(new FormatterConverter(this), getDefaultValue(), filter);
    }

    protected List<Integer> getErrorItemIndexes() {
        return errorItemIndexes;
    }

    @Override
    public String getUserAgentStylesheet() {
        return PropertyEditor.class.getResource("resources/styles/styles.css").toExternalForm();
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    protected void mouseClicked(MouseEvent ev) {
        if ((getNullSubstitution() == null || getNullSubstitution().isEmpty()) && getEmptySubstitution() == null && getSingleEmptyItemSubstitution() == null) {
            return;
        }
        IndexRange range = getItemRange();
        if (range == null) {
            return;
        }
        String sub = getNullSubstitution();
        if (sub == null || sub.isEmpty()) {
            sub = null;
        } else {
            sub = getNullSubstitution();
        }
        if (sub != null && getText() != null && sub.equals(getText())) {
            selectRange(range.getStart(), range.getEnd());
            return;
        } else {
            sub = null;
        }
        if (sub == null && getEmptySubstitution() != null) {
            sub = getEmptySubstitution();
        }
        if (sub != null && getText() != null && sub.equals(getText())) {
            selectRange(range.getStart(), range.getEnd());
            return;
        } else {
            sub = null;
        }

        if (sub == null && getSingleEmptyItemSubstitution() != null) {
            sub = getSingleEmptyItemSubstitution();
        }
        if (sub != null && sub.equals(getText().substring(range.getStart(), range.getEnd()))) {
            selectRange(range.getStart(), range.getEnd());
        }
    }

    public void setErrorFound(Boolean found) {
        this.errorFound.set(found);
    }

    protected ChangeListener<? super String> getLastValidValueChangeListener() {
        return lastValidValueChangeListener;
    }

    public IndexRange getItemRange() {
        IndexRange retval = null;
        String[] items = split(getText(), false);
        if (items == null) {
            return null;
        }
        int caretPos = getCaretPosition();
        int itemPos = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                continue;
            }
            if (itemPos <= caretPos && itemPos + items[i].length() >= caretPos) {
                retval = new IndexRange(itemPos, itemPos + items[i].length());
                break;
            }
            itemPos += items[i].length() + getSeparator().length();
        }
        return retval;
    }

    public static String[] split(String txt, String separator) {
        return split(txt, separator, true);
    }

    public static String[] split(String txt, String separator, boolean ignoreQuotes) {
        String[] retval = new String[]{txt};
        if (txt == null || txt != null && (separator == null || !txt.contains(separator))) {
            return retval;
        }

        retval = null;
        List<String> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder(txt);
        int n = 0;

        while (true) {
            if (n >= sb.length()) {
                list.add(sb.toString());
                break;
            }
            if (!ignoreQuotes && sb.charAt(n) == '"' && n < sb.length() && sb.lastIndexOf("\"", n + 1) >= 0) {
                n = sb.lastIndexOf("\"", n + 1) + 1;
                continue;
            }
            if (separator.equals(sb.substring(n, n + separator.length()))) {
                if (n == 0) {
                    list.add("");
                } else {
                    list.add(sb.substring(0, n));
                }
                sb = sb.delete(0, n + separator.length());
                n = 0;
                continue;
            }
            n++;

        }
        retval = list.toArray(new String[0]);
        return retval;

    }

    public String[] split(String txt) {
        return split(txt, true);
    }

    public String[] split(String txt, boolean ignoreQuotes) {
        return split(txt, getSeparator(), ignoreQuotes);
    }

    /**
     * The method is called when the content of the observable list is changed.
     * We must raise Invalidation event for {@link #lastValidText }
     * in the formatter in order to make the formatter to execute the method {@link FormatterConverter#toString(java.lang.String)
     * }. First set the {@code value} property of the {@code TextFormatter} to
     * {@code null} and then assign again the {@link #lastValidText } of this
     * control.
     * <p>
     * We must enforce the {@code TextFormatter} to invoke the method
     * </p>
     *
     */
    protected void invalidateFormatterValue() {
        //
        // We must raise Invalidation event for lastValidText property in the formatter 
        // in order to make the formatter to execute the converter method toString().
        // First set formatter'item property to null and then assign again
        // the value from this control
        //
        String txt = getLastValidText();
        setText(txt);
        //((TextFormatter<String>) getTextFormatter()).setValue(txt);
        //MouseEvent ev = new MouseEvent(MouseEvent.MOUSE_CLICKED,0,0,0,0,MouseButton.PRIMARY,0,false,false,false,false,false,false,false,false,false,false,null);
        //MouseEvent.fireEvent(this, ev);
        //fireEvent(new MouseEvent(this,this,MouseEvent.MOUSE_CLICKED,0,0,0,0,MouseButton.PRIMARY,0,false,false,false,false,false,false,false,false,false,false,null));
        //commitValue();
    }

    public StringProperty lastValidTextProperty() {
        return lastValidText;
    }

    /**
     * Returns the last value which represents the last result of the invocation
     * of the {@link FormatterConverter#toString(java.lang.String) }.
     *
     * @return the last value which represents the last result of the invocation
     * of the {@link FormatterConverter#toString(java.lang.String) }.
     */
    protected String getLastText() {
        return lastText;
    }

    /**
     * Sets the last value which represents the last result of the invocation of
     * the {@link FormatterConverter#toString(java.lang.String) }.
     *
     * @param lastText the last value which represents the last result of the invocation
     * of the {@link FormatterConverter#toString(java.lang.String) }.
     */
    protected void setLastText(String lastText) {
        this.lastText = lastText;
    }

    public String getLastValidText() {
        return lastValidText.get();
    }

    public void setLastValidText(String value) {
        this.lastValidText.set(value);
    }

    protected boolean isAcceptable(String txt) {
        if (getSeparator() == null || getSeparator().isEmpty()) {
            return testFilterValidators(txt);
        }
        if (getFilterValidators().isEmpty()) {
            return true;
        }
        String[] items = split(txt, false);
        boolean retval = true;
        for (String item : items) {
            if (!testFilterValidators(item)) {
                retval = false;
                break;
            }
        }
        return retval;

    }

    public StringTransformer getEmptyStringTransformer() {
        return emptyStringTransformer;
    }

    public void setEmptyStringTransformer(StringTransformer emptyStringTransformer) {
        this.emptyStringTransformer = emptyStringTransformer;
    }

    public ObservableList<StringTransformer> getStringTransformers() {
        return this.stringTransformers;
    }

    @Override
    protected ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    protected List<Integer> getErrorIndexes(String[] items) {
        List<Integer> errorIndexes = FXCollections.observableArrayList();
        errorItems.clear();
        int d = 0;
        for (int i = 0; i < items.length; i++) {
            
            String item = items[i];
            if ( getNullSubstitution() != null && getNullSubstitution().equals(item)) {
                continue;
            }
            //
            // We skip empty items and must take into account the actual index
            // of the converted lastValidValue in the result ObservableList
            //
            //    d++;
            //    continue;
            //}
            if (testValidators(item)) {
                continue;
            } else {
                errorIndexes.add(i - d);
                errorItems.put(i - d, item);
            }
        }
        return errorIndexes;
    }

    public boolean hasErrorItems() {
        return !getErrorItems().isEmpty();
    }

    public ObservableMap<Integer, String> getErrorItems() {
        return errorItems;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public boolean isNullString(String txt) {
        boolean retval = false;
        if (getNullSubstitution() != null) {
            String t = txt;
            if (!getNullSubstitution().isEmpty()) {
                t = t.trim();
            }
            if (t.equals(getNullSubstitution())) {
                retval = true;
            }
        }
        return retval;
    }

    public StringProperty nullSubstitutionProperty() {
        return nullSubstitution;
    }

    public String getNullSubstitution() {
        return nullSubstitution.get();
    }

    public void setNullSubstitution(String nullSubstitution) {
        this.nullSubstitution.set(nullSubstitution);
    }

    public String getEmptySubstitution() {
        return emptySubstitution.get();
    }

    public void setEmptySubstitution(String emptySubstitution) {
        this.emptySubstitution.set(emptySubstitution);
    }

    public String getSingleEmptyItemSubstitution() {
        return singleEmptyItemSubstitution;
    }

    public void setSingleEmptyItemSubstitution(String singleEmptyItemSubstitution) {
        this.singleEmptyItemSubstitution = singleEmptyItemSubstitution;
    }

   
   
    /**
     * Returns a list of validators. May be empty.
     *
     * @return Returns a list of validators.
     */
    public ObservableList<Predicate<String>> getValidators() {
        return validators;
    }

    public ObservableList<Predicate<String>> getFilterValidators() {
        return filterValidators;
    }

    public ObservableList<Predicate<String>> getSubstitutionFilterValidators() {
        return substitutionFilterValidators;
    }

    public ErrorMarkerBuilder getErrorMarkerBuilder() {
        return errorMarkerBuilder;
    }

    public void setErrorMarkerBuilder(ErrorMarkerBuilder errorMarkerBuilder) {
        this.errorMarkerBuilder = errorMarkerBuilder;
    }

    protected void clearErrorMarkers() {
        errorItemIndexes.clear();
        if (getErrorMarkerBuilder() != null && getErrorMarkerBuilder().getErrorMarkers() != null) {
            getChildren().removeAll(getErrorMarkerBuilder().getErrorMarkers());
        }
        setErrorFound(Boolean.FALSE);
    }

    protected boolean testFilterValidators(String txt) {
        txt = transform(txt);
        boolean retval = true;
        for (Predicate<String> p : getSubstitutionFilterValidators()) {
            if (p.test(txt)) {
                return true;
            }
        }

        for (Predicate<String> p : getFilterValidators()) {
            if (!p.test(txt)) {
                retval = false;
                break;
            }
        }
        return retval;
    }

    protected boolean testText(String text) {
        if (isNullable() && getNullSubstitution() != null && getNullSubstitution().equals(text)) {
            return true;
        } else if (!isNullable() && getNullSubstitution() != null && getNullSubstitution().equals(text)) {
            return false;
        }
        if (text != null && getEmptySubstitution() != null && getEmptySubstitution().equals(text)) {
            return true;
        }
        //
        //  This situation occurs when the instance of TextField was just created
        //
        if (text != null && text.isEmpty() && getEmptySubstitution() != null) {
            return true;
        }

        return false;

    }

    protected boolean testValidators(String item) {
        boolean retval = true;
        for (Predicate<String> v : getValidators()) {
            if (!v.test(item)) {
                retval = false;
                break;
            }
        }
        return retval;
    }

    /**
     * Tests whether the specified item is {@code null} or has a value which is
     * a {@code null substitution}. For example if the property
     * {@link #nullSubstitution} is set to {@code "NULL"} the the method will
     * return {@code true} if the item is equal to {@code "NULL"}. This method
     * should be used in {@code validators } instead of the expression like {@code item == null
     * }.
     *
     * @param item the string item to be tested
     * @return true if the given item is {@code null} or has a value which is a
     * {@code null substitution}.
     */
    public boolean isNull(String item) {
        return item == null || item.equals(getNullSubstitution());
    }

    /**
     * Tests whether the specified string item is empty or has a value which is
     * a {@code null empty list substitution}. For example if the property
     * {@link #emptySubstitution} is set to {@code "EMPTY"} the the method will
     * return {@code true} if the item is equal to {@code "EMPTY"}. This method
     * should be used in {@code validators } instead of the expression like {@code item.isEmpty }.
     * 
     * @param item the string item to be tested
     * @return true if the given item is empty or has a value which is aa null
     * null null null null null null null null null null null null null null
     * null null null null null null null     {@code empty list substitution}.
     */
    public boolean isEmpty(String item) {
        return item == "" || (getEmptySubstitution() != null && getEmptySubstitution().equals(item));
    }

    /**
     * Returns the result of a substitution or {@code null} if no substitution
     * were applied.
     *
     * @param txt the value to be replaced
     * @return null if no substitution were applied or the new text
     * representation
     */
    protected String applySubstitutions(String txt) {
        String retval = getNullSubstitution();
        if (txt == null && retval != null) {
            return retval;
        }
        retval = null;
        return retval;
    }


    protected void applyStringTransformers(String[] items, int idx) {
        
        String item = items[idx];
        for (StringTransformer st : getStringTransformers()) {
            if (item == null && st.transform(item) == null) {
                continue;
            } else if (item == null || st.transform(item) == null) {
                items[idx] = st.transform(item);
                break;
            }
            if (!item.equals(st.transform(item))) {
                items[idx] = st.transform(item);
                break;
            }
        }
    }

    protected String transform(String item) {
        String retval = item;
        for (StringTransformer st : getStringTransformers()) {
            if (item == null && st.transform(item) == null) {
                continue;
            } else if (item == null || st.transform(item) == null) {
                retval = st.transform(item);
                break;
            }
            if (!item.equals(st.transform(item))) {
                retval = st.transform(item);
                break;
            }
        }
        return retval;
    }

    public static class FormatterConverter extends StringConverter<String> {

        private final StringTextField textField;
        private boolean updating = false;
        private String updatingText = "";

        public FormatterConverter(StringTextField textField) {
            this.textField = textField;
        }

        @Override
        public String toString(String txt) {
            if (!textField.isCreated()) {
                textField.setLastText(textField.getDefaultValue());
                return textField.getDefaultValue();
            }
            //       if (true) {

            if (updating) {
                return updatingText;
            }
            String retval = "";
            try {
                updating = true;
                if (textField.NULL.equals(txt)) {
                    txt = textField.getLastText();
                }

                if (txt == null) {
                    txt = "";
                }

                String[] items;

                if (textField.getSeparator() != null) {
                    items = textField.split(txt, false);
                } else {
                    items = new String[]{txt};
                }

                textField.clearErrorMarkers();

                textField.setErrorFound(Boolean.FALSE);

                if (items.length == 1 && textField.getText() == null && textField.isNullable() && textField.getNullSubstitution() == null) {
                    textField.setLastText(items[0]);
                    if (!items[0].equals(textField.getLastValidText())) {
                        updateLastValidText(items[0]);
                    }
                    retval = items[0];
                    return items[0];
                } else if (items.length == 1 && textField.isNullable() && items[0].equals(textField.getNullSubstitution())) {
                    textField.setLastText(textField.getNullSubstitution());
                    if (!textField.getNullSubstitution().equals(textField.getLastValidText())) {
                        updateLastValidText(textField.getNullSubstitution());
                    }
                    return retval = textField.getNullSubstitution();
                } else if (items.length == 1 && items[0].equals(textField.getEmptySubstitution())) {
                    textField.setLastText(textField.getEmptySubstitution());
                    if (!textField.getEmptySubstitution().equals(textField.getLastValidText())) {
                        updateLastValidText(textField.getEmptySubstitution());
                    }
                    return retval = textField.getEmptySubstitution();
                } else if (items.length == 1 && items[0].equals(textField.getSingleEmptyItemSubstitution())) {
                    textField.setLastText(textField.getSingleEmptyItemSubstitution());
                    if (!textField.getSingleEmptyItemSubstitution().equals(textField.getLastValidText())) {
                        updateLastValidText(textField.getSingleEmptyItemSubstitution());
                    }
                    return retval = textField.getSingleEmptyItemSubstitution();
                }
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null && items[i].isEmpty() && textField.getEmptyStringTransformer() != null) {
                        items[i] = textField.getEmptyStringTransformer().transform(items[i]);
                    }
                    textField.applyStringTransformers(items, i);
                }
 
                textField.getErrorItemIndexes().addAll(textField.getErrorIndexes(items));

                if (!textField.getErrorItemIndexes().isEmpty()) {
                    textField.setErrorFound(Boolean.TRUE);
                } else {
                    textField.setErrorFound(Boolean.FALSE);
                }
                if (textField.getErrorMarkerBuilder() != null) {
                  
                    final String sourceText = txt;
                    Platform.runLater(() -> {
                        if (!textField.getErrorItemIndexes().isEmpty()) {
                            Integer[] e = textField.getErrorItemIndexes().toArray(new Integer[textField.getErrorItemIndexes().size()]);
                            textField.getErrorMarkerBuilder().showErrorMarkers(sourceText, items, e);
                        }
                    });
                }

                StringBuilder sb = new StringBuilder();
                StringBuilder sbValue = new StringBuilder();
                int n = items.length - textField.getErrorItemIndexes().size();
                for (int i = 0; i < items.length; i++) {
                    String item = items[i];
                    sb.append(item);
                    if (!textField.getErrorItemIndexes().contains(i)) {

                        sbValue.append(item);
                        --n;
                        if (textField.getSeparator() != null && n > 0) {
                            sbValue.append(textField.getSeparator());
                        }
                    }

                    if (textField.getSeparator() != null && !textField.getSeparator().isEmpty() && i != items.length - 1) {
                        sb.append(textField.getSeparator());
                    }

                }
                retval = sb.toString();
                if (textField.getErrorItemIndexes().isEmpty()) {
                    updateLastValidText(sbValue.toString());
                }

            } finally {
                textField.setLastText(retval);
                ((TextFormatter<String>) textField.getTextFormatter()).setValue(retval);
                updating = false;
                updatingText = retval;
            }
            return retval;

        }

        @Override
        public String fromString(String txt) {
            return txt;
        }

        private void updateLastValidText(String validText) {
            if ( textField.lastValidTextProperty().isBound() ) {
                return;
            }
            textField.lastValidTextProperty().removeListener(textField.getLastValidValueChangeListener());
            textField.setLastValidText(validText);
            textField.lastValidTextProperty().addListener(textField.getLastValidValueChangeListener());
        }

    }//class FormatterConverter

}
