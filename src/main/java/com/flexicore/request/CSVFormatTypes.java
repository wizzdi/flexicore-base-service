package com.flexicore.request;

import org.apache.commons.csv.CSVFormat;

public enum CSVFormatTypes {

    DEFAULT(CSVFormat.DEFAULT),
    EXCEL(CSVFormat.EXCEL),
    INFORMIX_UNLOAD(CSVFormat.INFORMIX_UNLOAD),
    INFORMIX_UNLOAD_CSV(CSVFormat.INFORMIX_UNLOAD_CSV),
    MONGODB_CSV(CSVFormat.MONGODB_CSV),
    MONGODB_TSV(CSVFormat.MONGODB_TSV),
    MYSQL(CSVFormat.MYSQL),
    ORACLE(CSVFormat.ORACLE),
    POSTGRESQL_CSV(CSVFormat.POSTGRESQL_CSV),
    POSTGRESQL_TEXT(CSVFormat.POSTGRESQL_TEXT),
    RFC4180(CSVFormat.RFC4180),
    TDF(CSVFormat.TDF);
    private final CSVFormat csvFormat;

    CSVFormatTypes(CSVFormat csvFormat) {
        this.csvFormat=csvFormat;
    }
    public CSVFormat getCsvFormat(){
        return csvFormat;
    }
}
