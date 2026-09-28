package com.gharnata.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.stat.descriptive.summary.Product;

@Data
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String ref;

    public ProductDTO(Long id, String name, String ref) {
        this.id = id;
        this.name = name;
        this.ref = ref;
    }
    public ProductDTO(String name, String ref) {
        this.name = name;
        this.ref = ref;
    }
    public ProductDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
