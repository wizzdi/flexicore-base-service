package com.flexicore.request;

import com.wizzdi.flexicore.boot.dynamic.invokers.request.ExecuteInvokerRequest;
import org.apache.commons.csv.CSVFormat;

import java.util.Map;

public class ExportDynamicInvoker extends ExecuteInvokerRequest {



    private CSVFormatTypes csvFormat;
    private Map<String, FieldProperties> fieldProperties;

    public Map<String, FieldProperties> getFieldProperties() {
        return fieldProperties;
    }

    public <T extends ExportDynamicInvoker> T setFieldProperties(Map<String, FieldProperties> fieldProperties) {
        this.fieldProperties = fieldProperties;
        return (T) this;
    }

    public CSVFormatTypes getCsvFormat() {
        return csvFormat;
    }

    public <T extends ExportDynamicInvoker> T setCsvFormat(CSVFormatTypes csvFormat) {
        this.csvFormat = csvFormat;
        return (T) this;
    }
}
