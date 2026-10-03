package com.gharnata.entity.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientDTO {
    private Long id;
    private String ste;
    private String ice;

    public ClientDTO(Long id, String ste, String ice) {
        this.id = id;
        this.ste = ste;
        this.ice = ice;
    }

    public ClientDTO(String ste, String ice) {
        this.ste = ste;
        this.ice = ice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSte() {
        return ste;
    }

    public void setSte(String ste) {
        this.ste = ste;
    }

    public String getIce() {
        return ice;
    }

    public void setIce(String ice) {
        this.ice = ice;
    }
}
