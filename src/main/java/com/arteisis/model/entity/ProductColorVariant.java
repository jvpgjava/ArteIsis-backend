package com.arteisis.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Variante de cor persistida em JSON (catálogo / admin). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductColorVariant {

    private String hex;
    private String imageUrl;
    private boolean available = true;
}
