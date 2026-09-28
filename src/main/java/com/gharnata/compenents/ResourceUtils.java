package com.gharnata.compenents;

import org.springframework.core.io.ClassPathResource;

public class ResourceUtils {

    public static byte[] load(String path) throws Exception {
        return new ClassPathResource(path)
                .getInputStream()
                .readAllBytes();
    }

}
