package org.example.converterservice.converter;

import java.io.IOException;
import java.io.InputStream;

public interface Converter {
    boolean supports(String fileType);

    byte[] convert(InputStream inputStream) throws IOException;
}
