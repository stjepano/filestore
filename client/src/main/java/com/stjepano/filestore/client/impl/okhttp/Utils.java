package com.stjepano.filestore.client.impl.okhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stjepano.filestore.common.ErrorResponse;
import okhttp3.Response;

import java.io.IOException;

class Utils {

    public static ErrorResponse from(Response response, ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(
                response.body().string(),
                ErrorResponse.class
        );
    }

}
